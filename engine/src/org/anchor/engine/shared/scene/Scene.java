package org.anchor.engine.shared.scene;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.anchor.engine.common.vfs.VirtualFileSystem;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.terrain.Terrain;
import org.anchor.engine.shared.utils.Layer;
import org.lwjgl.util.vector.Vector3f;

public class Scene {

    protected List<Entity> entities = new ArrayList<Entity>();
    protected List<Terrain> terrains = new ArrayList<Terrain>();
    protected List<Layer> layers = new ArrayList<Layer>(Arrays.asList(new Layer("Default", new Color(132, 217, 132))));
    protected Vector3f spawn = new Vector3f();
    protected VirtualFileSystem virtualFileSystem;

    public List<Entity> getEntities() {
        return entities;
    }

    public List<Terrain> getTerrains() {
        return terrains;
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public Layer getDefaultLayer() {
        return layers.get(0);
    }

    public List<String> getLayerNames() {
        List<String> names = new ArrayList<String>();
        for (Layer layer : layers)
            names.add(layer.getName());

        return names;
    }

    public Layer getLayerByName(String name) {
        for (Layer layer : layers)
            if (layer.getName().equalsIgnoreCase(name))
                return layer;

        return null;
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

    public Vector3f getSpawn() {
        return spawn;
    }

    public VirtualFileSystem getVirtualFileSystem() {
        return virtualFileSystem;
    }

    public void setVirtualFileSystem(VirtualFileSystem virtualFileSystem) {
        this.virtualFileSystem = virtualFileSystem;
    }

}
