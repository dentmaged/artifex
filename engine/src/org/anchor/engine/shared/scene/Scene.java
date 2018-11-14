package org.anchor.engine.shared.scene;

import java.util.ArrayList;
import java.util.List;

import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
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
        for (int i = 0; i < entities.size(); i++)
            entities.get(i).update();

        for (int i = 0; i < terrains.size(); i++)
            terrains.get(i).update();
    }

    public void updateFixed() {
        for (int i = 0; i < entities.size(); i++)
            entities.get(i).updateFixed();
    }

    public <T extends IComponent> List<Entity> getEntitiesWithComponent(Class<T> clazz) {
        List<Entity> has = new ArrayList<Entity>();
        for (int i = 0; i < entities.size(); i++)
            if (entities.get(i).hasComponent(clazz))
                has.add(entities.get(i));

        return has;
    }

    public <T extends IComponent> List<T> getComponents(Class<T> clazz) {
        List<T> components = new ArrayList<T>();
        for (int i = 0; i < entities.size(); i++) {
            T component = entities.get(i).getComponent(clazz);
            if (component != null)
                components.add(component);
        }

        return components;
    }

}
