package org.anchor.game.client.utils;

import org.anchor.engine.shared.terrain.Terrain;
import org.lwjgl.util.vector.Vector3f;

public class TerrainRaycast {

    protected Terrain terrain;
    protected float distance;
    protected Vector3f point, direction;

    public TerrainRaycast(Terrain terrain, float distance, Vector3f direction, Vector3f point) {
        this.terrain = terrain;
        this.distance = distance;
        this.direction = direction;
        this.point = point;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public float getDistance() {
        return distance;
    }

    public Vector3f getPoint() {
        return point;
    }

    public Vector3f getDirection() {
        return direction;
    }

}
