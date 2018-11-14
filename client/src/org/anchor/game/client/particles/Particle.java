package org.anchor.game.client.particles;

import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.physics.PhysicsEngine;
import org.anchor.game.client.GameClient;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class Particle {

    protected ParticleTexture texture;
    protected Vector3f position, velocity;
    protected float rotation, size, life, totalLife, gravity, blend, distance;
    protected Vector2f tcOffset1, tcOffset2;

    public Particle(ParticleTexture texture, Vector3f position, Vector3f velocity, float rotation, float size, float life, float gravity) {
        this.texture = texture;

        this.position = position;
        this.velocity = velocity;
        this.rotation = rotation;

        this.size = size;
        this.gravity = gravity;

        this.life = life;
        this.totalLife = life;

        this.tcOffset1 = new Vector2f();
        this.tcOffset2 = new Vector2f();
    }

    public ParticleTexture getTexture() {
        return texture;
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getRotation() {
        return rotation;
    }

    public float getSize() {
        return size;
    }

    public float getLife() {
        return life;
    }

    public Vector2f getTcOffset1() {
        return tcOffset1;
    }

    public Vector2f getTcOffset2() {
        return tcOffset2;
    }

    public float getBlendFactor() {
        return blend;
    }

    public float getDistance() {
        return distance;
    }

    public void update() {
        life -= PhysicsEngine.TICK_DELAY;
        distance = Vector3f.sub(GameClient.getPlayer().getPosition(), position, null).length();

        velocity.y += PhysicsEngine.GRAVITY * PhysicsEngine.TICK_DELAY * gravity;
        Vector3f.add(position, VectorUtils.mul(velocity, PhysicsEngine.TICK_DELAY), position);

        float lifePercentage = 1 - (life / totalLife);
        int stages = texture.getRows() * texture.getRows();
        float atlas = lifePercentage * stages;

        blend = atlas % 1;
        int index1 = (int) Math.floor(atlas);

        setOffset(tcOffset1, index1);
        setOffset(tcOffset2, Math.min(stages - 1, index1 + 1));
    }

    private void setOffset(Vector2f offset, int index) {
        float column = index % texture.getRows();
        float row = index / texture.getRows();

        offset.set(column / texture.getRows(), row / texture.getRows());
    }

}
