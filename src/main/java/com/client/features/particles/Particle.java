package com.client.features.particles;

public class Particle {

    private int zBuffer;
    private int age = 0;

    private int color;
    private float size;
    private Vector velocity;
    private float alpha;

    // Absolute (camera-space or screen-space) position used for rendering
    private final Vector position;
    // NEW: bind particle to a specific emitter + keep motion in local offset-space
    private int emitterIndex;
    private final Vector localOffset;
    private final Vector localOrigin; // spawn offset (for drift clamping)
    // particle state
    private int stickTicks = 0; // 0 = free, >0 = still attached for smoothing


    // Legacy-only (keep for compatibility if anything still constructs via old path)
    private ParticleDefinition definition = null;

    // ---- New constructor (used by your current ParticleSystem) ----
    public Particle(int color, float size, Vector velocity, Vector position, float alpha, int zBuffer,
                    int emitterIndex, Vector localOffset) {
        this.color = color;
        this.size = size;
        this.velocity = velocity;
        this.alpha = alpha;
        this.zBuffer = zBuffer;

        this.position = position;

        this.emitterIndex = emitterIndex;
        this.localOffset = localOffset;
        this.localOrigin = localOffset.clone();
    }

    // ---- Legacy constructor (kept so old code still compiles) ----
    public Particle(ParticleDefinition pd, Vector position, int zbuffer, int definitionID) {
        this(
                pd.getStartColor(),
                pd.getStartSize(),
                pd.getStartVelocity(definitionID).clone(),
                pd.getSpawnShape().getPoint(ParticleDefinition.RANDOM).addLocal(position),
                pd.getStartAlpha(),
                zbuffer,
                -1,
                Vector.ZERO.clone()
        );
        this.definition = pd;
    }

    // ---- Getters / setters used by ParticleSystem ----
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public float getSize() { return size; }
    public void setSize(float size) { this.size = size; }

    public Vector getVelocity() { return velocity; }
    public void setVelocity(Vector velocity) { this.velocity = velocity; }

    public float getAlpha() { return alpha; }
    public void setAlpha(float alpha) { this.alpha = alpha; }

    public Vector getPosition() { return position; }

    public int getZbuffer() { return zBuffer; }
    public void setZbuffer(int zBuffer) { this.zBuffer = zBuffer; }

    public int getEmitterIndex() { return emitterIndex; }
    public void setEmitterIndex(int emitterIndex) { this.emitterIndex = emitterIndex; }
    public Vector getLocalOffset() { return localOffset; }
    public Vector getLocalOrigin() { return localOrigin; }

    // Legacy accessor (optional)
    public ParticleDefinition getDefinition() { return definition; }


    public int getStickTicks() { return stickTicks; }
    public void setStickTicks(int stickTicks) { this.stickTicks = stickTicks; }

}
