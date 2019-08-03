package org.anchor.game.editor.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.anchor.engine.common.utils.AABB;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.Engine;
import org.anchor.engine.shared.components.PhysicsComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.components.MeshComponent;
import org.lwjgl.util.vector.Vector3f;

public class RoomUtils {

    public static List<Entity> createRoom(Entity entity) {
        Vector3f position = entity.getAbsolutePosition();
        Vector3f scale = entity.getScale();
        Vector3f half = VectorUtils.mul(scale, 0.5f);

        Map<AABB, Entity> existingWalls = new HashMap<AABB, Entity>();
        for (Entity existing : Engine.scene.getEntitiesWithComponent(MeshComponent.class)) {
            MeshComponent mesh = existing.getComponent(MeshComponent.class);
            if (!mesh.model.getName().equals("editor/cube"))
                continue;

            existingWalls.put(mesh.getAABB(), existing);
        }

        List<Entity> results = new ArrayList<Entity>();
        Vector3f[] directions = new Vector3f[] { new Vector3f(0, 0, -1), new Vector3f(0, 0, 1), new Vector3f(0, 1, 0), new Vector3f(0, -1, 0), new Vector3f(1, 0, 0), new Vector3f(-1, 0, 0), };
        for (Vector3f direction : directions) {
            Vector3f absDirection = VectorUtils.abs(direction);
            Vector3f unitScale = Vector3f.sub(new Vector3f(1, 1, 1), absDirection, null);

            Vector3f wallPosition = Vector3f.add(position, VectorUtils.mul(direction, half), null);
            Vector3f wallScale = VectorUtils.mul(scale, unitScale);
            outer: {
                boolean wip = true;
                for (Entry<AABB, Entity> entry : existingWalls.entrySet()) {
                    if (wip)
                        break;

                    if (!entry.getKey().inside(wallPosition))
                        continue;

                    if (VectorUtils.mul(entry.getValue().getScale(), absDirection).length() > 1)
                        continue;

                    try {
                        Vector3f targetScale = VectorUtils.mul(entry.getValue().getScale(), unitScale);
                        for (String fieldName : new String[] { "x", "y", "z" }) {
                            Field field = Vector3f.class.getField(fieldName);
                            float val = field.getFloat(targetScale);
                            if (val == 0)
                                continue;

                            if (field.getFloat(entry.getValue().getPosition()) + val * 0.5f < field.getFloat(wallPosition) + field.getFloat(wallScale) * 0.5f) {
                                System.out.println("NEEDS ADJUSTMENT");
                            }

                            if (field.getFloat(entry.getValue().getPosition()) - val * 0.5f > field.getFloat(wallPosition) - field.getFloat(wallScale) * 0.5f) {
                                System.out.println("NEEDS ADJUSTMENT");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break outer; // skip the next section (creating the entity and adding it to results)
                }

                Vector3f.add(wallScale, VectorUtils.mul(absDirection, 0.1f), wallScale); // give the wall some thickness
                Vector3f.add(wallPosition, VectorUtils.mul(direction, 0.05f), wallPosition); // move the wall out by half as well

                results.add(createWall(wallPosition, wallScale));
            }
        }

        return results;
    }

    private static Entity createWall(Vector3f position, Vector3f scale) {
        Entity entity = new Entity();
        entity.setValue("model", "editor/cube");
        entity.addComponent(new MeshComponent());

        entity.getPosition().set(position);
        entity.getScale().set(scale);

        entity.precache();
        entity.spawn();

        entity.getComponent(MeshComponent.class).setCollisionMesh();
        entity.getComponent(PhysicsComponent.class).gravity = false;
        entity.getComponent(PhysicsComponent.class).inverseMass = 0;

        return entity;
    }

}
