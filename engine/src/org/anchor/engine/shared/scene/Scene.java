package org.anchor.engine.shared.scene;

import java.util.ArrayList;
import java.util.List;

import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.terrain.Terrain;

public class Scene {

    protected List<Entity> entities = new ArrayList<Entity>();
    protected List<Terrain> terrains = new ArrayList<Terrain>();

    public List<Entity> getEntities() {
        return entities;
    }

    public List<Terrain> getTerrains() {
        return terrains;
    }

    public void update() {
        for (Entity entity : entities)
            entity.update();

        for (Terrain terrain : terrains)
            terrain.update();
    }

    public void updateFixed() {
        for (Entity entity : entities)
            entity.updateFixed();
    }

    public <T extends IComponent> List<Entity> getEntitiesWithComponent(Class<T> clazz) {
        List<Entity> has = new ArrayList<Entity>();
        for (Entity entity : entities)
            if (entity.hasComponent(clazz))
                has.add(entity);

        return has;
    }

    public <T extends IComponent> List<IComponent> getComponents(Class<T> clazz) {
        List<IComponent> components = new ArrayList<IComponent>();
        for (Entity entity : entities) {
            T component = entity.getComponent(clazz);
            if (component != null)
                components.add(component);
        }

        return components;
    }

}
