package org.anchor.engine.shared.monitoring;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.anchor.engine.common.utils.ObjectUtils;
import org.anchor.engine.shared.Engine;
import org.anchor.engine.shared.events.MonitorVariableChangeEvent;
import org.anchor.engine.shared.monitoring.cache.Vector3fCacheInformation;
import org.anchor.engine.shared.utils.Property;
import org.lwjgl.util.vector.Vector3f;

public class InstanceMonitor {

    private Object parent, target;
    private Map<String, Object> cache;

    public InstanceMonitor(Object parent, Object target) {
        this.parent = parent;
        this.target = target;
        this.cache = new HashMap<String, Object>();

        try {
            for (Field field : target.getClass().getFields())
                if (field.isAnnotationPresent(Property.class))
                    cache.put(field.getName(), getCacheInformation(field.get(target)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void check() {
        try {
            for (Field field : target.getClass().getFields()) {
                if (field.isAnnotationPresent(Property.class)) {
                    Object current = getCacheInformation(field.get(target));
                    Object previous = cache.get(field.getName());

                    if (!ObjectUtils.compare(previous, current)) {
                        Engine.bus.fireEvent(new MonitorVariableChangeEvent(parent, target, field, previous, current));
                        cache.put(field.getName(), current);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object getCacheInformation(Object object) {
        if (object instanceof Vector3f)
            return new Vector3fCacheInformation((Vector3f) object);

        return object;
    }

}
