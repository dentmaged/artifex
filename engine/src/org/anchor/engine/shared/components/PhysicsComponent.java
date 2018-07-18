package org.anchor.engine.shared.components;

import java.util.ArrayList;
import java.util.List;

import org.anchor.engine.common.utils.AABB;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.Engine;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.physics.CollisionMesh;
import org.anchor.engine.shared.physics.Material;
import org.anchor.engine.shared.physics.PhysicsTouchListener;
import org.anchor.engine.shared.utils.CollisionMeshLoader;
import org.anchor.engine.shared.utils.Property;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class PhysicsComponent implements IComponent {

    public List<CollisionMesh> meshes = new ArrayList<CollisionMesh>();

    public List<PhysicsTouchListener> listeners = new ArrayList<PhysicsTouchListener>();

    @Property("Velocity")
    public Vector3f velocity = new Vector3f();

    @Property("Collidable")
    public boolean collidable = true;

    @Property("Gravity")
    public boolean gravity = true;

    @Property("Inverse Mass")
    public float inverseMass = 1;

    @Property("Material")
    public Material material = Material.WOOD;

    private Entity entity;

    @Override
    public void spawn(Entity entity) {
        this.entity = entity;

        if (meshes.size() == 0 && entity.containsKey("collisionMesh"))
            meshes.addAll(CollisionMeshLoader.loadCollisionMeshes(entity.getValue("collisionMesh")));
    }

    @Override
    public void setValue(String key, String value) {
        if (key.equals("velocity"))
            velocity.set(VectorUtils.stringToVector(value));

        if (key.equals("collidable"))
            collidable = Boolean.parseBoolean(value);

        if (key.equals("gravity"))
            gravity = Boolean.parseBoolean(value);
    }

    public boolean isCollidable() {
        return collidable && meshes.size() > 0;
    }

    public boolean canCollideWith(Entity other) {
        if (Engine.isClientSide())
            return !other.hasComponent(PlayerComponent.class);

        return true;
    }

    public int getMeshCount() {
        return meshes.size();
    }

    public List<Vector3f> getVertices(int mesh) {
        if (entity == null)
            return new ArrayList<Vector3f>();

        return meshes.get(mesh).getVertices(entity.getTransformationMatrix());
    }

    public List<Vector3f> getNormals(int mesh) {
        if (entity == null)
            return new ArrayList<Vector3f>();

        return meshes.get(mesh).getNormals(entity.getTransformationMatrix());
    }

    public int[] getIndices(int mesh) {
        return meshes.get(mesh).getIndices();
    }

    public AABB getAABB(int mesh) {
        if (entity == null)
            return null;

        return meshes.get(mesh).getAABB(entity.getTransformationMatrix());
    }

    public AABB getWholeAABB() {
        if (entity == null)
            return null;

        if (meshes.size() == 1)
            return getAABB(0);

        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;

        // JAVA BUG: Float.MIN_VALUE is sometimes wrong when comparing
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        float maxZ = -Float.MAX_VALUE;

        Matrix4f transformationMatrix = entity.getTransformationMatrix();
        for (CollisionMesh mesh : meshes) {
            for (Vector3f vertex : mesh.getAABB(transformationMatrix).getCorners()) {
                minX = Math.min(minX, vertex.x);
                minY = Math.min(minY, vertex.y);
                minZ = Math.min(minZ, vertex.z);

                maxX = Math.max(maxX, vertex.x);
                maxY = Math.max(maxY, vertex.y);
                maxZ = Math.max(maxZ, vertex.z);
            }
        }

        return new AABB(minX, maxX, minY, maxY, minZ, maxZ);
    }

    public Vector3f raycast(Vector3f origin, Vector3f ray) {
        float distance = raycastDistance(origin, ray);
        if (distance == -1)
            return null;

        return Vector3f.add(origin, VectorUtils.mul(ray, distance), null);
    }

    public float raycastDistance(Vector3f origin, Vector3f ray) {
        float closest = Float.MAX_VALUE;
        for (CollisionMesh mesh : meshes) {
            float current = mesh.raycastDistance(entity.getTransformationMatrix(), origin, ray);
            if (current == -1)
                continue;

            closest = Math.min(closest, current);
        }

        if (closest == Float.MAX_VALUE)
            return -1;

        return closest;
    }

    @Override
    public IComponent copy() {
        PhysicsComponent copy = new PhysicsComponent();
        copy.meshes = new ArrayList<CollisionMesh>(meshes);
        copy.velocity = new Vector3f(velocity);
        copy.collidable = collidable;
        copy.gravity = gravity;
        copy.inverseMass = inverseMass;
        copy.material = material;

        return copy;
    }

}
