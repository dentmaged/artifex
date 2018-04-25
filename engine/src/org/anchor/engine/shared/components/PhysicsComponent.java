package org.anchor.engine.shared.components;

import java.util.List;

import org.anchor.engine.common.utils.AABB;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.physics.CollisionMesh;
import org.anchor.engine.shared.physics.Material;
import org.anchor.engine.shared.utils.CollisionMeshLoader;
import org.anchor.engine.shared.utils.Property;
import org.lwjgl.util.vector.Vector3f;

public class PhysicsComponent implements IComponent {

    public CollisionMesh mesh;

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

        if (mesh == null && entity.containsKey("collisionMesh"))
            mesh = CollisionMeshLoader.loadCollisionMesh(entity.getValue("collisionMesh"));
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
        return collidable && mesh != null;
    }

    public boolean canCollideWith(Entity other) {
        return true;
    }

    public List<Vector3f> getVertices() {
        return mesh.getVertices(entity.getTransformationMatrix());
    }

    public List<Vector3f> getNormals() {
        return mesh.getNormals(entity.getTransformationMatrix());
    }

    public AABB getAABB() {
        return mesh.getAABB(entity.getTransformationMatrix());
    }

    public Vector3f raycast(Vector3f origin, Vector3f ray) {
        return mesh.raycast(entity.getTransformationMatrix(), origin, ray);
    }

    @Override
    public IComponent copy() {
        PhysicsComponent copy = new PhysicsComponent();
        copy.mesh = mesh;
        copy.velocity = new Vector3f(velocity);
        copy.collidable = collidable;
        copy.gravity = gravity;
        copy.inverseMass = inverseMass;
        copy.material = material;

        return copy;
    }

}
