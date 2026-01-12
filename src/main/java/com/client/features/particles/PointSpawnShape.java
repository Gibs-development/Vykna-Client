package com.client.features.particles;

public class PointSpawnShape implements SpawnShape {

    private final Vector point;

    public PointSpawnShape(Vector point) {
        this.point = point;
    }

    @Override
    public Vector getPoint(java.util.Random random) {
        // Always spawn at a single fixed offset (a "point").
        return point.clone();
    }
}
