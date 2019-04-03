package org.anchor.game.editor.utils;

import java.util.ArrayList;
import java.util.List;

import org.anchor.engine.shared.editor.TransformableObject;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.utils.Layer;

public class ShadowEntity extends Entity {

    protected List<Entity> entities;

    public ShadowEntity(List<TransformableObject> objects) {
        entities = new ArrayList<Entity>();
        for (TransformableObject object : objects)
            if (object instanceof Entity)
                entities.add((Entity) object);
    }

    @Override
    public void setLayer(Layer layer) {
        for (Entity entity : entities)
            entity.setLayer(layer);
    }

    @Override
    public void setValue(String key, String value) {
        for (Entity entity : entities)
            entity.setValue(key, value);
    }

}
