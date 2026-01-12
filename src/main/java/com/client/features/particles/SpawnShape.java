package com.client.features.particles;

public interface SpawnShape {
    /**
     * Returns a spawn point offset (relative to the system/base position).
     * The caller usually does .addLocal(basePos) after this.
     */
    Vector getPoint(java.util.Random random);
}
