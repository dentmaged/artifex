package org.anchor.engine.shared.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.anchor.engine.common.utils.CoreMaths;
import org.anchor.engine.common.utils.Mathf;
import org.anchor.engine.common.utils.Plane;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.Engine;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.physics.PhysicsEngine;
import org.anchor.engine.shared.physics.PhysicsTouchListener;
import org.anchor.engine.shared.physics.collision.PlayerCollisionPacket;
import org.anchor.engine.shared.physics.collision.broadphase.Broadphase;
import org.anchor.engine.shared.physics.collision.broadphase.BroadphaseCollisionResult;
import org.anchor.engine.shared.scene.Scene;
import org.anchor.engine.shared.scheduler.ScheduledRunnable;
import org.anchor.engine.shared.scheduler.Scheduler;
import org.anchor.engine.shared.terrain.Terrain;
import org.anchor.engine.shared.utils.Maths;
import org.anchor.engine.shared.utils.Property;
import org.anchor.engine.shared.weapon.Gun;
import org.anchor.engine.shared.weapon.Weapon;
import org.anchor.engine.shared.weapon.implementation.CrossbowData;
import org.anchor.engine.shared.weapon.implementation.ShotgunData;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class LivingComponent implements IComponent {

    public float forwards, sideways, fallingDistance, pitch, yaw, roll, noclipSpeed = 2;

    public List<PhysicsTouchListener> listeners = new ArrayList<PhysicsTouchListener>();

    @Property("Health")
    public float health = 100;

    @Property("Gravity Enabled")
    public boolean gravity = true;

    public boolean isInAir, isInLiquid, damageable = true, fire, reload;
    public Entity standingOn;

    public Weapon[] weapons;
    public int selectedIndex;

    protected boolean voluntaryJump, gravityStep;
    protected boolean canShoot;

    protected Entity entity;
    protected Matrix4f viewMatrix = new Matrix4f();
    protected Matrix4f inverseViewMatrix = new Matrix4f();

    public static final float GRAVITY = -0.02f;

    protected static final float FRICTION_GROUND = 0.65f;
    protected static final float FRICTION_AIR = 0;

    protected static final float MAX_SPEED_GROUND = 0.1f;
    protected static final float MAX_SPEED_AIR = 0.175f;
    protected static final float ACCELERATE_GROUND = 4.5f;
    protected static final float ACCELERATE_AIR = 0.25f;

    protected static final float JUMP_POWER = 0.225f;
    protected static final float INVERSE_MASS = 1;
    protected static final float SQRT2 = Mathf.sqrt(2);
    protected static final float constant = 1f / SQRT2;
    protected static final float pi = 3.14159265358979f;

    protected static final float ROLL_ANGLE = 2;
    protected static final float ROLL_SPEED = 200;
    protected static final float ROLL_MULTIPLIER = 1024;

    public float selectedSpeed = 15;

    @Override
    public void precache(Entity entity) {
        this.entity = entity;

        weapons = new Weapon[] { new Gun(entity, new CrossbowData(), canShoot), new Gun(entity, new ShotgunData(), canShoot), };

        listeners.add(new PhysicsTouchListener() {

            @Override
            public void touch(Entity primary, Entity secondary) {
                if (gravityStep)
                    standingOn = secondary;
            }

        });
        entity.getComponent(PhysicsComponent.class).inverseMass = 0;
    }

    @Override
    public void spawn() {

    }

    public void move(Scene scene, Terrain terrain) {
        if (health <= 0)
            return;

        Vector3f velocity = entity.getVelocity();
        Vector3f position = entity.getPosition();

        float friction = FRICTION_GROUND;
        if (isInAir)
            friction = FRICTION_AIR;

        float speed = velocity.length();
        if (speed != 0) {
            float percentage = Math.max(speed - (speed * friction), 0) / speed;

            velocity.x *= percentage;
            velocity.y *= percentage;
            velocity.z *= percentage;
        }

        checkInput();

        float dx = 0;
        float dy = 0;
        float dz = 0;

        standingOn = null;
        if (gravity) {
            PlayerCollisionPacket packet = new PlayerCollisionPacket();
            packet.scene = scene;
            packet.eRadius = new Vector3f(0.3f, 1.8f, 0.3f);

            if (scene != null)
                collideAndSlide(packet, position, velocity, new Vector3f(0, GRAVITY, 0));
        } else {
            float reducingFactor = pitch;
            if (reducingFactor < 0)
                reducingFactor *= -1;

            reducingFactor = (90 - reducingFactor) / 90;

            dx += Mathf.sinDegrees(yaw) * forwards * reducingFactor * noclipSpeed * PhysicsEngine.TICK_DELAY;
            dz -= Mathf.cosDegrees(yaw) * forwards * reducingFactor * noclipSpeed * PhysicsEngine.TICK_DELAY;

            dy -= Mathf.sinDegrees(pitch) * forwards * noclipSpeed * PhysicsEngine.TICK_DELAY;

            dx += Mathf.sinDegrees(yaw - 90) * sideways * noclipSpeed * PhysicsEngine.TICK_DELAY;
            dz -= Mathf.cosDegrees(yaw - 90) * sideways * noclipSpeed * PhysicsEngine.TICK_DELAY;
        }

        if (standingOn != null) {
            isInAir = false;
            voluntaryJump = false;
            fallingDistance = 0;
        }

        position.x += dx;
        position.y += dy;
        position.z += dz;

        setSoundData();

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

        if (fire)
            getSelectedWeapon().attack();
        else if (reload && getSelectedWeapon() instanceof Gun)
            ((Gun) getSelectedWeapon()).reload();

        updateViewMatrix();
    }

    protected void checkInput() {

    }

    public void updateViewMatrix() {
        Maths.createViewMatrix(viewMatrix, entity, this);
        Matrix4f.invert(viewMatrix, inverseViewMatrix);
    }

    public Vector3f getEyePosition() {
        return Vector3f.add(entity.getPosition(), new Vector3f(0, 1.68f, 0), null);
    }

    public void setEyePosition(Vector3f position) {
        entity.getPosition().set(Vector3f.sub(position, new Vector3f(0, 1.68f, 0), null));
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
        if (health <= 0) {
            health = 0;
            Scheduler.schedule(new ScheduledRunnable() {

                @Override
                public void tick(float time, float percentage) {

                }

                @Override
                public void finish() {
                    health = 100;
                    entity.getVelocity().set(0, 0, 0);
                    entity.getPosition().set(Engine.getSpawn());
                }

            }, 3);
        }
    }

    @Override
    public List<Class<? extends IComponent>> getDependencies() {
        return Arrays.asList(PhysicsComponent.class);
    }

    @Override
    public IComponent copy() {
        return new LivingComponent();
    }

    public Weapon getSelectedWeapon() {
        return weapons[selectedIndex];
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public Matrix4f getNormalMatrix(Entity entity) {
        return getNormalMatrix(entity.getRotation());
    }

    public Matrix4f getNormalMatrix(Vector3f rotation) {
        Matrix4f inverted = Matrix4f.invert(Matrix4f.mul(viewMatrix, CoreMaths.createTransformationMatrix(new Vector3f(), rotation, new Vector3f(1, 1, 1)), null), null);
        if (inverted == null)
            return new Matrix4f();

        return Matrix4f.transpose(inverted, null);
    }

    public void setSoundData() {

    }

    public void collideAndSlide(PlayerCollisionPacket packet, Vector3f position, Vector3f velocity, Vector3f gravity) {
        packet.R3Position = position;
        packet.R3Velocity = velocity;

        Vector3f eSpacePosition = VectorUtils.div(packet.R3Position, packet.eRadius);
        Vector3f eSpaceVelocity = VectorUtils.div(packet.R3Velocity, packet.eRadius);
        eSpacePosition.y += 1f; // player position is at their feet. Add 1 to the y as the ellipsis is
                                // transformed into a unit sphere.
        Vector3f finalPosition = collideWithWorld(packet, eSpacePosition, eSpaceVelocity, 0);

        gravityStep = true;
        packet.R3Position = VectorUtils.mul(finalPosition, packet.eRadius);
        packet.R3Velocity = gravity;

        eSpaceVelocity = VectorUtils.div(gravity, packet.eRadius);
        finalPosition = collideWithWorld(packet, finalPosition, eSpaceVelocity, 0);
        gravityStep = false;

        finalPosition.y -= 1f;
        Vector3f dest = VectorUtils.mul(finalPosition, packet.eRadius);
        Vector3f.sub(dest, position, velocity);
        position.set(dest);
    }

    private Vector3f collideWithWorld(PlayerCollisionPacket packet, Vector3f position, Vector3f velocity, int recursion) {
        float veryCloseDistance = 0.00005f;

        if (recursion > 5)
            return position;

        packet.velocity = velocity;
        packet.normalisedVelocity = new Vector3f(velocity);
        if (velocity.lengthSquared() > 0)
            packet.normalisedVelocity.normalise();
        packet.basePoint = position;
        packet.foundCollision = false;

        checkCollision(packet);

        if (!packet.foundCollision)
            return Vector3f.add(position, velocity, null);

        Vector3f destination = Vector3f.add(position, velocity, null);
        Vector3f newBasePoint = new Vector3f(position);

        if (packet.nearestDistance >= veryCloseDistance) {
            Vector3f V = new Vector3f(velocity);
            V.normalise();
            V.scale(packet.nearestDistance - veryCloseDistance);
            newBasePoint = Vector3f.add(packet.basePoint, V, null);

            if (V.lengthSquared() > 0)
                V.normalise();
            Vector3f.sub(packet.intersectionPoint, VectorUtils.mul(V, veryCloseDistance), packet.intersectionPoint);
        }

        Vector3f slidePlaneOrigin = packet.intersectionPoint;
        Vector3f slidePlaneNormal = Vector3f.sub(newBasePoint, packet.intersectionPoint, null);
        slidePlaneNormal.normalise();

        Plane slidingPlane = new Plane(slidePlaneOrigin, slidePlaneNormal);
        Vector3f newDestinationPoint = Vector3f.sub(destination, VectorUtils.mul(slidePlaneNormal, slidingPlane.signedDistanceTo(destination)), null);

        Vector3f newVelocityVector = Vector3f.sub(newDestinationPoint, packet.intersectionPoint, null);
        if (newVelocityVector.length() < veryCloseDistance)
            return newBasePoint;

        return collideWithWorld(packet, newBasePoint, newVelocityVector, recursion + 1);
    }

    private void checkCollision(PlayerCollisionPacket packet) {
        List<BroadphaseCollisionResult> results = Broadphase.collisions(entity, packet.scene.getEntitiesWithComponent(PhysicsComponent.class));
        for (BroadphaseCollisionResult result : results) {
            packet.foundCollisionWith = false;
            Entity other = result.getOther(entity);
            PhysicsComponent secondary = other.getComponent(PhysicsComponent.class);

            for (int mesh = 0; mesh < secondary.getMeshCount(); mesh++) {
                List<Vector3f> vertices = secondary.getVertices(mesh);
                int[] indices = secondary.getIndices(mesh);

                for (int i = 0; i < indices.length; i += 3) {
                    Vector3f p1 = vertices.get(indices[i] - 1);
                    Vector3f p2 = vertices.get(indices[i + 1] - 1);
                    Vector3f p3 = vertices.get(indices[i + 2] - 1);

                    checkTriangle(packet, VectorUtils.div(p1, packet.eRadius), VectorUtils.div(p2, packet.eRadius), VectorUtils.div(p3, packet.eRadius));
                }
            }

            if (packet.foundCollisionWith) {
                for (PhysicsTouchListener listener : listeners)
                    listener.touch(entity, other);
                for (PhysicsTouchListener listener : secondary.listeners)
                    listener.touch(entity, other);
            }
        }
    }

    private void checkTriangle(PlayerCollisionPacket packet, Vector3f p1, Vector3f p2, Vector3f p3) {
        Plane trianglePlane = new Plane(p1, p2, p3);

        if (trianglePlane.isFrontFacingTo(packet.normalisedVelocity)) {
            float t0 = 0, t1 = 0;
            boolean embeddedInPlane = false;

            float signedDistanceToTrianglePlane = trianglePlane.signedDistanceTo(packet.basePoint);
            float normalDotVelocity = Vector3f.dot(trianglePlane.getNormal(), packet.velocity);

            if (normalDotVelocity == 0) {
                if (Math.abs(signedDistanceToTrianglePlane) >= 1) {
                    return;
                } else {
                    embeddedInPlane = true;
                    t0 = 0;
                    t1 = 1;
                }
            } else {
                t0 = (-1f - signedDistanceToTrianglePlane) / normalDotVelocity;
                t1 = (1f - signedDistanceToTrianglePlane) / normalDotVelocity;

                if (t0 > t1) {
                    float t = t1;
                    t1 = t0;
                    t0 = t;
                }

                if (t0 > 1 || t1 < 0)
                    return;

                if (t0 < 0)
                    t0 = 0;
                if (t1 < 0)
                    t1 = 0;
                if (t0 > 1)
                    t0 = 1;
                if (t1 > 1)
                    t1 = 1;
            }

            Vector3f collisionPoint = null;
            boolean found = false;
            float t = 1;

            if (!embeddedInPlane) {
                Vector3f planeIntersectionPoint = Vector3f.add(Vector3f.sub(packet.basePoint, trianglePlane.getNormal(), null), VectorUtils.mul(packet.velocity, t0), null);

                if (checkPointInTriangle(planeIntersectionPoint, p1, p2, p3)) {
                    found = true;
                    t = t0;
                    collisionPoint = planeIntersectionPoint;
                }
            }

            if (!found) {
                Vector3f velocity = packet.velocity;
                Vector3f base = packet.basePoint;

                float velocitySquaredLength = velocity.lengthSquared();
                float a, b, c, newT;

                a = velocitySquaredLength;
                b = 2 * Vector3f.dot(velocity, Vector3f.sub(base, p1, null));
                c = Vector3f.sub(p1, base, null).lengthSquared() - 1;
                if ((newT = getLowestRoot(a, b, c, t)) >= 0) {
                    found = true;
                    t = newT;
                    collisionPoint = p1;
                }

                b = 2 * Vector3f.dot(velocity, Vector3f.sub(base, p2, null));
                c = Vector3f.sub(p2, base, null).lengthSquared() - 1;
                if ((newT = getLowestRoot(a, b, c, t)) >= 0) {
                    found = true;
                    t = newT;
                    collisionPoint = p2;
                }

                b = 2 * Vector3f.dot(velocity, Vector3f.sub(base, p3, null));
                c = Vector3f.sub(p3, base, null).lengthSquared() - 1;
                if ((newT = getLowestRoot(a, b, c, t)) >= 0) {
                    found = true;
                    t = newT;
                    collisionPoint = p3;
                }

                Vector3f edge = Vector3f.sub(p2, p1, null);
                Vector3f baseToVertex = Vector3f.sub(p1, base, null);

                float edgeSquaredLength = edge.lengthSquared();
                float edgeDotVelocity = Vector3f.dot(edge, velocity);
                float edgeDotBaseToVertex = Vector3f.dot(edge, baseToVertex);

                a = edgeSquaredLength * -velocitySquaredLength + edgeDotVelocity * edgeDotVelocity;
                b = edgeSquaredLength * (2 * Vector3f.dot(velocity, baseToVertex)) - 2 * edgeDotVelocity * edgeDotBaseToVertex;
                c = edgeSquaredLength * (1 - baseToVertex.lengthSquared()) + edgeDotBaseToVertex * edgeDotBaseToVertex;

                if ((newT = getLowestRoot(a, b, c, t)) >= 0) {
                    float f = (edgeDotVelocity * newT - edgeDotBaseToVertex) / edgeSquaredLength;

                    if (f >= 0 && f <= 1) {
                        found = true;
                        t = newT;
                        collisionPoint = Vector3f.add(p1, VectorUtils.mul(edge, f), null);
                    }
                }

                edge = Vector3f.sub(p3, p2, null);
                baseToVertex = Vector3f.sub(p2, base, null);

                edgeSquaredLength = edge.lengthSquared();
                edgeDotVelocity = Vector3f.dot(edge, velocity);
                edgeDotBaseToVertex = Vector3f.dot(edge, baseToVertex);

                a = edgeSquaredLength * -velocitySquaredLength + edgeDotVelocity * edgeDotVelocity;
                b = edgeSquaredLength * (2 * Vector3f.dot(velocity, baseToVertex)) - 2 * edgeDotVelocity * edgeDotBaseToVertex;
                c = edgeSquaredLength * (1 - baseToVertex.lengthSquared()) + edgeDotBaseToVertex * edgeDotBaseToVertex;

                if ((newT = getLowestRoot(a, b, c, t)) >= 0) {
                    float f = (edgeDotVelocity * newT - edgeDotBaseToVertex) / edgeSquaredLength;

                    if (f >= 0 && f <= 1) {
                        found = true;
                        t = newT;
                        collisionPoint = Vector3f.add(p2, VectorUtils.mul(edge, f), null);
                    }
                }

                edge = Vector3f.sub(p1, p3, null);
                baseToVertex = Vector3f.sub(p3, base, null);

                edgeSquaredLength = edge.lengthSquared();
                edgeDotVelocity = Vector3f.dot(edge, velocity);
                edgeDotBaseToVertex = Vector3f.dot(edge, baseToVertex);

                a = edgeSquaredLength * -velocitySquaredLength + edgeDotVelocity * edgeDotVelocity;
                b = edgeSquaredLength * (2 * Vector3f.dot(velocity, baseToVertex)) - 2 * edgeDotVelocity * edgeDotBaseToVertex;
                c = edgeSquaredLength * (1 - baseToVertex.lengthSquared()) + edgeDotBaseToVertex * edgeDotBaseToVertex;

                if ((newT = getLowestRoot(a, b, c, t)) >= 0) {
                    float f = (edgeDotVelocity * newT - edgeDotBaseToVertex) / edgeSquaredLength;

                    if (f >= 0 && f <= 1) {
                        found = true;
                        t = newT;
                        collisionPoint = Vector3f.add(p3, VectorUtils.mul(edge, f), null);
                    }
                }
            }

            if (found) {
                float distToCollision = t * packet.velocity.length();

                if (!packet.foundCollision || distToCollision < packet.nearestDistance) {
                    packet.nearestDistance = distToCollision;
                    packet.intersectionPoint = collisionPoint;
                    packet.foundCollision = true;
                    packet.foundCollisionWith = true;
                    packet.normal = trianglePlane.getNormal();
                }
            }
        }
    }

    private boolean checkPointInTriangle(Vector3f point, Vector3f pa, Vector3f pb, Vector3f pc) {
        Vector3f e10 = Vector3f.sub(pb, pa, null);
        Vector3f e20 = Vector3f.sub(pc, pa, null);

        float a = Vector3f.dot(e10, e10);
        float b = Vector3f.dot(e10, e20);
        float c = Vector3f.dot(e20, e20);
        float ac_bb = (a * c) - (b * b);

        Vector3f vp = new Vector3f(point.x - pa.x, point.y - pa.y, point.z - pa.z);
        float d = Vector3f.dot(vp, e10);
        float e = Vector3f.dot(vp, e20);

        float x = (d * c) - (e * b);
        float y = (e * a) - (d * b);
        float z = x + y - ac_bb;

        return z < 0 && x >= 0 && y >= 0;
    }

    private float getLowestRoot(float a, float b, float c, float maxR) {
        float determinant = b * b - 4 * a * c;
        if (determinant < 0)
            return -1;

        float sqrtD = Mathf.sqrt(determinant);
        float r1 = (-b - sqrtD) / (2f * a);
        float r2 = (-b + sqrtD) / (2f * a);

        if (r1 > r2) {
            float temp = r1;
            r1 = r2;
            r2 = temp;
        }

        if (r1 > 0 && r1 < maxR)
            return r1;

        if (r2 > 0 && r2 < maxR)
            return r2;

        return -1;

    }

}
