package com.client.features.particles;

import com.client.DrawingArea;
import com.client.Rasterizer;
import com.client.WorldController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleSystem {

    private final ParticleDefinition definition;
    private final boolean screenSpace;

    private final List<Particle> currentParticles;
    private final Random random;

    /** WORLD-space emitter positions when screenSpace=false */
    private final ArrayList<Vector> emitters = new ArrayList<>();

    private final Vector velocityStep;
    private final float sizeStep;

    // ---- color lerp (RGB channels) ----
    private static int clamp255(int v) { return v < 0 ? 0 : (v > 255 ? 255 : v); }
    private final int startR, startG, startB;
    private final float stepR, stepG, stepB;

    public ParticleSystem(ParticleDefinition definition) {
        this(definition, false);
    }

    public ParticleSystem(ParticleDefinition definition, boolean screenSpace) {
        this.definition = definition;
        this.screenSpace = screenSpace;

        int sc = definition.getStartColor();
        int ec = definition.getEndColor();
        if (ec == 0) ec = sc;

        startR = (sc >> 16) & 255;
        startG = (sc >> 8) & 255;
        startB = sc & 255;

        int endR = (ec >> 16) & 255;
        int endG = (ec >> 8) & 255;
        int endB = ec & 255;

        float life = Math.max(1, definition.getLifespan());
        stepR = (endR - startR) / life;
        stepG = (endG - startG) / life;
        stepB = (endB - startB) / life;

        this.currentParticles = new ArrayList<>(definition.getMaxParticles());
        this.random = new Random(System.currentTimeMillis());

        this.sizeStep = (definition.getEndSize() - definition.getStartSize()) / life;

        // Applied during FREE drift
        this.velocityStep = definition.getEndVelocity()
                .subtract(definition.getStartVelocity(0))
                .divide((int) life);
    }

    // ---- emitters ----
    public void clearEmitters() {
        emitters.clear();
    }

    /** WORLD-space emitter */
    public void addEmitter(int wx, int wy, int wz) {
        emitters.add(new Vector(wx, wy, wz));
    }

    public void tick() {
        // 1) update & cull
        ArrayList<Particle> dead = new ArrayList<>();

        for (Particle p : currentParticles) {
            if (p == null) continue;

            if (p.getAge() >= definition.getLifespan()) {
                dead.add(p);
                continue;
            }

            p.setAge(p.getAge() + 1);

            // color lerp
            int a = p.getAge();
            int r = clamp255((int) (startR + stepR * a));
            int g = clamp255((int) (startG + stepG * a));
            int b = clamp255((int) (startB + stepB * a));
            p.setColor((r << 16) | (g << 8) | b);

            // size
            p.setSize(p.getSize() + sizeStep);

            if (screenSpace) {
                // screen particles: just integrate in screen coords
                p.getPosition().addLocal(p.getVelocity());
                p.getVelocity().addLocal(velocityStep);
                continue;
            }

            // ---- WORLD-space behaviour ----
            // Stick briefly to cape so emission feels anchored.
            // Then detach and drift = RS3 trail.
            if (p.getStickTicks() > 0) {
                int idx = p.getEmitterIndex();
                if (idx >= 0 && idx < emitters.size()) {
                    Vector e = emitters.get(idx);
                    Vector off = p.getLocalOffset();

                    p.getPosition().set(
                            e.getX() + off.getX(),
                            e.getY() + off.getY(),
                            e.getZ() + off.getZ()
                    );
                }
                p.setStickTicks(p.getStickTicks() - 1);
                if (p.getStickTicks() == 0) {
                    // fully detached
                    p.setEmitterIndex(-1);
                }
            } else {
                // Free drift
                Vector vel = p.getVelocity();

                vel.addLocal(velocityStep);

                if (definition.getGravity() != null) {
                    vel.addLocal(definition.getGravity());
                }

                p.getPosition().addLocal(vel);

                // drag (tighter = smaller number)
                vel.set(
                        (int) (vel.getX() * 0.96f),
                        (int) (vel.getY() * 0.96f),
                        (int) (vel.getZ() * 0.96f)
                );
            }
        }

        currentParticles.removeAll(dead);

        // 2) spawn
        if (currentParticles.size() >= definition.getMaxParticles()) return;
        if (emitters.isEmpty()) return;

        int canSpawn = Math.min(definition.getSpawnRate(),
                definition.getMaxParticles() - currentParticles.size());

        for (int i = 0; i < canSpawn; i++) {
            int emitterIndex = random.nextInt(emitters.size());
            Vector emitter = emitters.get(emitterIndex);

            Vector raw = definition.getSpawnShape().getPoint(random);

            // keep spawn tight to cloth
            final int JITTER = 6;
            int ox = raw.getX();
            int oy = raw.getY();
            int oz = raw.getZ();

            if (ox >  JITTER) ox =  JITTER;
            if (ox < -JITTER) ox = -JITTER;
            if (oz >  JITTER) oz =  JITTER;
            if (oz < -JITTER) oz = -JITTER;

            Vector localOffset = new Vector(ox, oy, oz);

            Vector spawnPos = new Vector(
                    emitter.getX() + localOffset.getX(),
                    emitter.getY() + localOffset.getY(),
                    emitter.getZ() + localOffset.getZ()
            );

            Vector vel = definition.getStartVelocity(0).clone();

            // Optional: keep it mostly “smoke up”
            // vel.set(0, vel.getY(), 0);

            Particle p = new Particle(
                    definition.getStartColor(),
                    definition.getStartSize(),
                    vel,
                    spawnPos,
                    definition.getStartAlpha(),
                    definition.getzBuffer(),
                    emitterIndex,
                    localOffset
            );

            // RS3-ish: stick 2..4 ticks then detach
            p.setStickTicks(3);

            currentParticles.add(p);
        }
    }

    public void render() {
        tick();

        boolean spriteMode = definition.getSprite() != null;

        for (Particle p : currentParticles) {
            int sx, sy;
            int zForDepth;

            if (screenSpace) {
                sx = p.getPosition().getX();
                sy = p.getPosition().getY();
                zForDepth = 999999;
            } else {
                // WORLD -> CAMERA -> SCREEN (THIS is the bit you were missing)
                final int camWX = WorldController.getCameraWorldX();
                final int camWY = WorldController.getCameraWorldY();
                final int camWZ = WorldController.getCameraWorldZ();

                final int sinPitch = WorldController.getCameraSinPitch();
                final int cosPitch = WorldController.getCameraCosPitch();
                final int sinYaw   = WorldController.getCameraSinYaw();
                final int cosYaw   = WorldController.getCameraCosYaw();

                int dx = p.getPosition().getX() - camWX;
                int dy = p.getPosition().getY() - camWY;
                int dz = p.getPosition().getZ() - camWZ;

                // yaw
                int cx = (dz * sinYaw + dx * cosYaw) >> 16;
                int cz1 = (dz * cosYaw - dx * sinYaw) >> 16;

                // pitch
                int cy = (dy * cosPitch - cz1 * sinPitch) >> 16;
                int cz = (dy * sinPitch + cz1 * cosPitch) >> 16;

                if (cz <= 50) continue;

                sx = Rasterizer.textureInt1 + (cx * WorldController.focalLength) / cz;
                sy = Rasterizer.textureInt2 + (cy * WorldController.focalLength) / cz;
                zForDepth = cz;

                // Depth test (AA-safe)
                float[] db = DrawingArea.depthBuffer;
                int w = DrawingArea.width;
                int h = DrawingArea.height;

                if (db != null && sx >= 0 && sy >= 0 && sx < w && sy < h) {
                    float scene = db[sy * w + sx];

                    if (Float.isFinite(scene) && scene > 0f) {
                        if (scene < 1f) {
                            float inv = 1f / (float) zForDepth;
                            if (inv <= scene) continue;
                        } else {
                            if (zForDepth >= scene) continue;
                        }
                    }
                }
            }

            float lifeT = p.getAge() / (definition.getLifespan() * 1f);
            if (lifeT < 0f) lifeT = 0f;
            if (lifeT > 1f) lifeT = 1f;

            float a = (1f - lifeT) * p.getAlpha();
            if (a < 0f) a = 0f;
            if (a > 1f) a = 1f;

            int alphaI = (int) (a * 255f);
            if (alphaI <= 0) continue;

            int radius;
            if (screenSpace) {
                radius = (int) (p.getSize() * 4);
            } else {
                float scale = WorldController.focalLength / (zForDepth * 1f);
                final float SPRITE_WORLD_SCALE = 30f;
                radius = (int) (p.getSize() * SPRITE_WORLD_SCALE * scale);
                if (radius < 2) radius = 2;
                if (radius > 64) radius = 64;
            }

            if (sx < -50 || sy < -50 || sx > DrawingArea.width + 50 || sy > DrawingArea.height + 50) continue;

            if (spriteMode) {
                int diameter = radius * 2;
                int drawX = sx - (diameter / 2);
                int drawY = sy - (diameter / 2);

                int[] spx = definition.getSprite().myPixels;
                int sw = definition.getSprite().myWidth;
                int sh = definition.getSprite().myHeight;
                if (definition.isAdditive()) {
                    DrawingArea.drawAdditiveTintedScaledSprite(
                            spx, sw, sh,
                            drawX, drawY, diameter, diameter,
                            p.getColor(),
                            alphaI
                    );
                } else {
                    DrawingArea.drawTintedScaledSprite(
                            spx, sw, sh,
                            drawX, drawY, diameter, diameter,
                            p.getColor(),
                            alphaI
                    );
                }
            } else {
                if (definition.isAdditive()) {
                    DrawingArea.drawAdditiveFilledCircle(sx, sy, radius, p.getColor(), alphaI);
                } else {
                    DrawingArea.drawFilledCircle(sx, sy, radius, p.getColor(), alphaI);
                }

            }
        }
    }

    public List<Particle> getCurrentParticles() {
        return currentParticles;
    }

    public ParticleDefinition getDefinition() {
        return definition;
    }
}
