package com.client.features.particles;

import java.util.ArrayList;

public final class ParticleRenderQueue {
    private static final ArrayList<ParticleSystem> queued = new ArrayList<>(64);

    private ParticleRenderQueue() {}

    public static void beginFrame() {
        queued.clear();
    }

    public static void queue(ParticleSystem ps) {
        if (ps == null) return;
        // avoid ticking/rendering the same system twice in one frame
        if (!queued.contains(ps)) queued.add(ps);
    }

    public static void renderAll() {
        for (int i = 0; i < queued.size(); i++) {
            queued.get(i).render(); // tick + draw
        }
    }
}
