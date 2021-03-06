package org.anchor.engine.shared.monitoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.scene.Scene;

public class SceneMonitor {

    private Scene scene;
    private Map<Entity, EntityMonitor> monitors;

    public SceneMonitor(Scene scene) {
        this.scene = scene;
        this.monitors = new HashMap<Entity, EntityMonitor>();
    }

    public void check() {
        for (Entity entity : scene.getEntities()) {
            EntityMonitor monitor = monitors.get(entity);
            if (monitor == null) {
                monitor = new EntityMonitor(entity);
                monitors.put(entity, monitor);
            }

            monitor.check();
        }

        List<Entity> entities = new ArrayList<Entity>(monitors.keySet());
        for (Entity entity : entities)
            if (!scene.getEntities().contains(entity))
                monitors.remove(entity);
    }

}
