package org.anchor.engine.shared.components;

import java.util.Arrays;
import java.util.List;

import org.anchor.engine.common.utils.Mathf;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.physics.PhysicsEngine;
import org.anchor.engine.shared.physics.collision.CollisionEngine;
import org.anchor.engine.shared.physics.collision.narrowphase.NarrowphaseCollisionResult;
import org.anchor.engine.shared.scene.Scene;
import org.anchor.engine.shared.terrain.Terrain;
import org.anchor.engine.shared.utils.Maths;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class LivingComponent implements IComponent {

    public float forwards, sideways, fallingDistance, pitch, health = 100, noPhysicsSpeed = 10;
    public boolean voluntaryJump, gravity = true, isInAir, isInWater, damageable = true;
    public Entity standingOn;

    protected Entity entity;
    protected Matrix4f viewMatrix = new Matrix4f();
    protected Matrix4f inverseViewMatrix = new Matrix4f();

    public static final float GRAVITY = -0.02f;
    protected static final float JUMP_POWER = 0.3f;
    protected static final float INVERSE_MASS = 1;
    protected static final float SQRT2 = Mathf.sqrt(2);
    protected static final float constant = 1f / SQRT2;
    protected static final float pi = 3.14159265358979f;

    protected float SELECTED_SPEED = 4;

    @Override
    public void spawn(Entity entity) {
        this.entity = entity;
    }

    public void move(Scene scene, Terrain terrain) {
        float friction = 0.65f;
        if (isInAir)
            friction = 1;

        Vector3f velocity = entity.getVelocity();
        Vector3f rotation = entity.getRotation();
        Vector3f position = entity.getPosition();

        velocity.x *= friction;
        velocity.z *= friction;

        checkInput();

        float dx = 0;
        float dy = 0;
        float dz = 0;

        standingOn = null;
        if (gravity) {
            velocity.y += GRAVITY;

            for (NarrowphaseCollisionResult result : CollisionEngine.collisions(entity, scene.getEntitiesWithComponent(PhysicsComponent.class))) {
                Entity other = result.getOther(entity);
                PhysicsComponent secondary = other.getComponent(PhysicsComponent.class);

                float normal = Mathf.abs(result.getNormal().y);
                if (normal > 0.8) {
                    standingOn = other;

                    if (!voluntaryJump) {
                        velocity.y = 0;
                        Vector3f point = secondary.raycast(getEyePosition(), new Vector3f(0, -1, 0));

                        if (point != null)
                            position.y = point.y - 0.01f;
                    }
                } else {
                    Vector3f.sub(position, velocity, position);

                    Vector3f relative = Vector3f.sub(velocity, secondary.velocity, null);
                    float dot = Vector3f.dot(relative, result.getNormal());
                    Vector3f i = VectorUtils.mul(result.getNormal(), dot);
                    Vector3f.sub(velocity, i, velocity);
                }
            }

            dx = velocity.x;
            dy = velocity.y;
            dz = velocity.z;
        } else {
            float reducingFactor = pitch;
            if (reducingFactor < 0)
                reducingFactor *= -1;

            reducingFactor = (90 - reducingFactor) / 90;

            dx += Math.sin(rotation.y / 180 * pi) * forwards * PhysicsEngine.TICK_DELAY * reducingFactor * noPhysicsSpeed;
            dz -= Math.cos(rotation.y / 180 * pi) * forwards * PhysicsEngine.TICK_DELAY * reducingFactor * noPhysicsSpeed;

            dy -= (Math.sin(pitch / 180 * pi)) * forwards * PhysicsEngine.TICK_DELAY * noPhysicsSpeed;

            dx += Math.sin((rotation.y - 90) / 180 * pi) * sideways * PhysicsEngine.TICK_DELAY * noPhysicsSpeed;
            dz -= Math.cos((rotation.y - 90) / 180 * pi) * sideways * PhysicsEngine.TICK_DELAY * noPhysicsSpeed;
        }

        position.x += dx;
        position.y += dy;
        position.z += dz;

        setSoundData();

        if (standingOn != null) {
            isInAir = false;
            voluntaryJump = false;
            fallingDistance = 0;
        }

        float terrainHeight = -Terrain.MAX_HEIGHT;
        if (terrain != null)
            terrainHeight = terrain.getHeightOfTerrain(position.x, position.z);

        if (position.y - terrainHeight < 0.1f) {
            isInAir = false;
            voluntaryJump = false;

            position.y = terrainHeight;
            velocity.y = 0;
            fallingDistance = 0;
        } else if (standingOn == null) {
            isInAir = true;
            fallingDistance = position.y - terrainHeight;
        }

        Maths.createViewMatrix(viewMatrix, entity, this);
        Matrix4f.invert(viewMatrix, inverseViewMatrix);
    }

    protected void checkInput() {

    }

    public Vector3f getEyePosition() {
        return Vector3f.add(entity.getPosition(), new Vector3f(0, 1.68f, 0), null);
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public Matrix4f getInverseViewMatrix() {
        return inverseViewMatrix;
    }

    public Vector3f getBackwardVector() {
        return new Vector3f(viewMatrix.m02, viewMatrix.m12, viewMatrix.m22);
    }

    public Vector3f getForwardVector() {
        return new Vector3f(-viewMatrix.m02, -viewMatrix.m12, -viewMatrix.m22);
    }

    public Vector3f getUpVector() {
        return new Vector3f(viewMatrix.m01, viewMatrix.m11, viewMatrix.m21);
    }

    public Vector3f getDownVector() {
        return new Vector3f(-viewMatrix.m01, -viewMatrix.m11, -viewMatrix.m21);
    }

    public Vector3f getRightVector() {
        return new Vector3f(viewMatrix.m00, viewMatrix.m10, viewMatrix.m20);
    }

    public Vector3f getLeftVector() {
        return new Vector3f(-viewMatrix.m00, -viewMatrix.m10, -viewMatrix.m20);
    }

    @Override
    public void setValue(String key, String value) {
        if (key.equals("gravity"))
            gravity = Boolean.parseBoolean(value);

        if (key.equals("health"))
            health = Float.parseFloat(value);

        if (key.equals("damageable"))
            damageable = Boolean.parseBoolean(value);
    }

    public void damage(float amount) {
        if (!damageable)
            return;

        health -= amount;
    }

    @Override
    public List<Class<? extends IComponent>> getDependencies() {
        return Arrays.asList(PhysicsComponent.class);
    }

    @Override
    public IComponent copy() {
        return new LivingComponent();
    }

    public Matrix4f getNormalMatrix(Matrix4f transformationMatrix) {
        Matrix4f inverted = Matrix4f.invert(Matrix4f.mul(viewMatrix, transformationMatrix, null), null);
        if (inverted == null)
            return new Matrix4f();

        return Matrix4f.transpose(inverted, null);
    }

    public void setSoundData() {

    }

}
