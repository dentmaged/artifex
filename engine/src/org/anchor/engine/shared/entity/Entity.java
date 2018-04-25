package org.anchor.engine.shared.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.components.PhysicsComponent;
import org.anchor.engine.shared.components.TransformComponent;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class Entity {

    protected int id;
    protected boolean hidden, spawned;

    protected Entity parent;
    protected List<Entity> children = new ArrayList<Entity>();

    protected Map<String, String> data = new HashMap<String, String>();
    protected List<IComponent> components = new ArrayList<IComponent>(), spawnComponents = new ArrayList<IComponent>();

    public Entity() {
        addComponent(new TransformComponent());
    }

    public Entity(Class<? extends IComponent>... clazzes) {
        addComponent(new TransformComponent());
        for (Class<? extends IComponent> clazz : clazzes) {
            try {
                addComponent(clazz.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void spawn() {
        for (IComponent component : components)
            component.spawn(this);
        spawned = true;
    }

    public int getId() {
        return id;
    }

    public Vector3f getPosition() {
        return getComponent(TransformComponent.class).position;
    }

    public Vector3f getRotation() {
        return getComponent(TransformComponent.class).rotation;
    }

    public Vector3f getScale() {
        return getComponent(TransformComponent.class).scale;
    }

    public Matrix4f getTransformationMatrix() {
        Matrix4f current = getComponent(TransformComponent.class).getTransformationMatrix();

        if (parent != null)
            Matrix4f.mul(parent.getTransformationMatrix(), current, current);

        return current;
    }

    public Vector3f getVelocity() {
        if (!hasComponent(PhysicsComponent.class)) {
            System.err.println("WARN: Entity doesn't contain PhysicsComponent");

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
    }

    public <T extends IComponent> void removeComponent(T component) {
        components.remove(component);
    }

    public <T extends IComponent> void removeComponent(Class<T> clazz) {
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

        data.put("hidden", hidden + "");
    }

    public boolean isHidden() {
        return hidden;
    }

    public Entity copy() {
        Entity copy = new Entity();
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
        for (IComponent spawn : spawnComponents)
            spawn.spawn(this);

        spawnComponents.clear();
        for (IComponent component : components)
            component.updateFixed();
    }

    public void update() {
        for (IComponent spawn : spawnComponents)
            spawn.spawn(this);

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

}
