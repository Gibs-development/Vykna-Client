package com.client.features.particles;

import com.client.WorldController;

import java.util.ArrayList;
import java.util.IdentityHashMap;

public final class ParticlePostPass {

    private static final ArrayList<ParticleSystem> systems = new ArrayList<>();
    private static final IdentityHashMap<ParticleSystem, Boolean> cleared = new IdentityHashMap<>();

    private ParticlePostPass() {}

    /** Call once per frame BEFORE worldController.draw(...) */
    public static void beginFrame() {
        systems.clear();
        cleared.clear();
    }

    /** Called by models while they're being drawn (collect emitters, don't render yet). */
    public static void addEmitter(ParticleSystem ps, int cx, int cy, int cz) {
        if (ps == null) return;

        if (!cleared.containsKey(ps)) {
            ps.clearEmitters();
            cleared.put(ps, Boolean.TRUE);
            systems.add(ps);
        }

        // CAMERA -> WORLD
        final int sinPitch = WorldController.getCameraSinPitch();
        final int cosPitch = WorldController.getCameraCosPitch();
        final int sinYaw   = WorldController.getCameraSinYaw();
        final int cosYaw   = WorldController.getCameraCosYaw();

        int wyRel = (cy * cosPitch + cz * sinPitch) >> 16;
        int z1    = (cz * cosPitch - cy * sinPitch) >> 16;

        int wxRel = (cx * cosYaw - z1 * sinYaw) >> 16;
        int wzRel = (cx * sinYaw + z1 * cosYaw) >> 16;

        int wx = WorldController.getCameraWorldX() + wxRel;
        int wy = WorldController.getCameraWorldY() + wyRel;
        int wz = WorldController.getCameraWorldZ() + wzRel;

        ps.addEmitter(wx, wy, wz);
    }


    /** Call once per frame AFTER worldController.draw(...) */
    public static void flush() {
        for (int i = 0; i < systems.size(); i++) {
            systems.get(i).render();
        }
    }
}
