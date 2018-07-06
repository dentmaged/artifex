package org.anchor.engine.shared.monitoring;

import java.util.HashMap;
import java.util.Map;

import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.entity.Entity;

public class EntityMonitor {

    private Entity entity;
    private Map<IComponent, InstanceMonitor> monitors;

    public EntityMonitor(Entity entity) {
        this.entity = entity;
        this.monitors = new HashMap<IComponent, InstanceMonitor>();
    }

    public void check() {
        for (IComponent component : entity.getComponents()) {
            InstanceMonitor monitor = monitors.get(component);
            if (monitor == null) {
                monitor = new InstanceMonitor(entity, component);
                monitors.put(component, monitor);
            }

            monitor.check();
        }

        for (IComponent component : monitors.keySet())
            if (entity.getComponent(component.getClass()) == null)
                monitors.remove(component);
    }

}
