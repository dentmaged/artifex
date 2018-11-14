package org.anchor.engine.shared.physics.collision;

import org.anchor.engine.shared.scene.Scene;
import org.lwjgl.util.vector.Vector3f;

public class PlayerCollisionPacket {

    public Vector3f eRadius;

    public Vector3f R3Velocity;
    public Vector3f R3Position;

    public Vector3f velocity;
    public Vector3f normal;
    public Vector3f normalisedVelocity;
    public Vector3f basePoint;

    public boolean foundCollision, foundCollisionWith;
    public float nearestDistance;
    public Vector3f intersectionPoint;

    public Scene scene;

}
