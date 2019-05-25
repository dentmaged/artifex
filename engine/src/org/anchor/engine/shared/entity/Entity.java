package org.anchor.engine.shared.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.anchor.engine.common.Log;
import org.anchor.engine.common.utils.CoreMaths;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.Engine;
import org.anchor.engine.shared.components.PhysicsComponent;
import org.anchor.engine.shared.components.TransformComponent;
import org.anchor.engine.shared.editor.TransformableObject;
import org.anchor.engine.shared.utils.Layer;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class Entity implements TransformableObject {

    protected int id, lineIndex = -1;
    protected boolean hidden, precached, spawned, updated;
    protected Layer layer;

    protected Entity parent;
    protected List<Entity> children = new ArrayList<Entity>();

    protected Map<String, String> data = new HashMap<String, String>();
    protected List<IComponent> components = new ArrayList<IComponent>(), spawnComponents = new ArrayList<IComponent>();

    protected Matrix4f transformationMatrix = new Matrix4f(), previousTransformationMatrix = new Matrix4f();

    public Entity() {
        addComponent(new TransformComponent());

        layer = Engine.getDefaultLayer();
        Engine.getInstance().onEntityCreate(this);
    }

    public Entity(Class<? extends IComponent>... clazzes) {
        addComponent(new TransformComponent());

        layer = Engine.getDefaultLayer();
        for (Class<? extends IComponent> clazz : clazzes) {
            try {
                addComponent(clazz.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Engine.getInstance().onEntityCreate(this);
    }

    public void precache() {
        if (layer == null)
            layer = Engine.getDefaultLayer();

        for (IComponent component : components)
            component.precache(this);

        precached = true;
        Engine.getInstance().onEntityPrecache(this);
    }

    public void spawn() {
        if (!precached)
            precache();

        Engine.getInstance().onEntityPreSpawn(this);
        for (IComponent component : components)
            component.spawn();

        spawned = true;
        Engine.getInstance().onEntitySpawn(this);
    }

    public void destroy() {
        Engine.getEntities().remove(this);
        Engine.getInstance().onEntityDestroy(this);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLineIndex() {
        return lineIndex;
    }

    public void setLineIndex(int lineIndex) {
        this.lineIndex = lineIndex;
    }

    public void setLayer(Layer layer) {
        if (this.layer != null)
            this.layer.getEntities().remove(this);

        setValue("layer", layer.getName());
        this.layer = layer;
        this.layer.getEntities().add(this);
    }

    public Layer getLayer() {
        return layer;
    }

    @Override
    public Vector3f getPosition() {
        return getComponent(TransformComponent.class).position;
    }

    public Vector3f getAbsolutePosition() {
        Matrix4f matrix = new Matrix4f();
        if (parent != null)
            matrix = parent.getTransformationMatrix();
        Vector3f position = getComponent(TransformComponent.class).position;

        return new Vector3f(Matrix4f.transform(matrix, new Vector4f(position.x, position.y, position.z, 1), null));
    }

    @Override
    public Vector3f getRotation() {
        return getComponent(TransformComponent.class).rotation;
    }

    @Override
    public Vector3f getScale() {
        return getComponent(TransformComponent.class).scale;
    }

    @Override
    public Matrix4f getTransformationMatrix() {
        return transformationMatrix;
    }

    public Vector3f getVelocity() {
        if (!hasComponent(PhysicsComponent.class)) {
            Log.warning("Entity doesn't contain PhysicsComponent");

            return new Vector3f();
        }

        return getComponent(PhysicsComponent.class).velocity;
    }

    public void setValue(String key, String value) {
        data.put(key, value);

        if (key.equals("hidden"))
            this.hidden = Boolean.parseBoolean(value);

        for (IComponent component : components)
            component.setValue(key, value);

        Engine.getInstance().onEntityKeyValueChange(this, key, value);
    }

    public boolean containsKey(String key) {
        return data.containsKey(key);
    }

    public String getValue(String key) {
        return data.get(key);
    }

    public Set<Entry<String, String>> entrySet() {
        return data.entrySet();
    }

    public boolean isStatic() {
        PhysicsComponent component = getComponent(PhysicsComponent.class);
        if (component == null)
            return true;

        return component.inverseMass == 0;
    }

    public boolean hasSpawned() {
        return spawned;
    }

    public <T extends IComponent> T getComponent(Class<T> clazz) {
        for (IComponent component : components)
            if (clazz.isInstance(component))
                return (T) component;

        return null;
    }

    public <T extends IComponent> void addComponent(T component) {
        if (hasComponent(component.getClass()))
            return;

        components.add(component);
        if (spawned)
            spawnComponents.add(component);

        for (Class<? extends IComponent> clazz : component.getDependencies()) {
            try {
                addComponent(clazz.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Engine.getInstance().onComponentAdd(this, component);
    }

    public <T extends IComponent> void removeComponent(T component) {
        Engine.getInstance().onComponentRemove(this, component);
        components.remove(component);
    }

    public <T extends IComponent> void removeComponent(Class<T> clazz) {
        Engine.getInstance().onComponentAdd(this, getComponent(clazz));
        components.remove(getComponent(clazz));
    }

    public List<IComponent> getComponents() {
        return components;
    }

    public <T extends IComponent> boolean hasComponent(Class<T> clazz) {
        for (IComponent component : components)
            if (clazz.isInstance(component))
                return true;

        return false;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;

        setValue("hidden", hidden + "");
    }

    public boolean isHidden() {
        return hidden;
    }

    public Entity copy() {
        Entity copy = new Entity();
        copy.layer = layer;
        copy.hidden = hidden;
        copy.removeComponent(TransformComponent.class);

        for (IComponent component : components)
            copy.addComponent(component.copy());

        for (Entry<String, String> entry : data.entrySet())
            copy.setValue(entry.getKey(), entry.getValue());
        copy.spawn();

        return copy;
    }

    public void updateFixed() {
        updated = false;
        if (!transformationMatrix.equals(previousTransformationMatrix)) {
            CoreMaths.set(transformationMatrix, previousTransformationMatrix);
            updated = true;
        }

        for (IComponent spawn : spawnComponents) {
            spawn.precache(this);
            spawn.spawn();
        }

        spawnComponents.clear();
        for (IComponent component : components)
            component.updateFixed();
    }

    public void update() {
        if (layer == null)
            layer = Engine.getDefaultLayer();

        VectorUtils.set(transformationMatrix, getComponent(TransformComponent.class).getTransformationMatrix());

        if (parent != null)
            Matrix4f.mul(parent.getTransformationMatrix(), transformationMatrix, transformationMatrix);

        for (IComponent spawn : spawnComponents) {
            spawn.precache(this);
            spawn.spawn();
        }

        spawnComponents.clear();
        for (IComponent component : components)
            component.update();
    }

    public void setParent(Entity parent) {
        if (this.parent != null)
            this.parent.children.remove(this);

        this.parent = parent;
        if (parent != null)
            this.parent.children.add(this);
    }

    public Entity getParent() {
        return parent;
    }

    public List<Entity> getChildren() {
        return children;
    }

    public boolean hasUpdated() {
        return updated;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Entity[");
        builder.append("memid=").append(super.toString().split("@")[1]).append(", ");
        builder.append("id=").append(id).append(", ");
        builder.append("components=[");
        for (int i = 0; i < components.size(); i++)
            builder.append(components.get(i).getClass().getName()).append(i < components.size() - 1 ? ", " : "");
        builder.append("], ").append("data=[");

        int i = 0;
        for (Entry<String, String> entry : data.entrySet()) {
            builder.append(entry.getKey()).append("=").append(entry.getValue()).append(i < data.size() - 1 ? ", " : "");

            i++;
        }
        builder.append("]").append("]");

        return builder.toString();
    }

}
