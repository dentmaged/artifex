package org.anchor.game.editor.commands;

import java.util.List;

import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;

public class AddComponentToEntitiesCallback implements CommandCallback {

    protected List<Entity> entities;
    protected Class<?> clazz;

    public AddComponentToEntitiesCallback(List<Entity> entities, Class<?> clazz) {
        this.entities = entities;
        this.clazz = clazz;
    }

    @Override
    public void undo() {
        for (Entity entity : entities)
            entity.removeComponent(entity.getComponent((Class<IComponent>) clazz));
    }

    @Override
    public void redo() {
        for (Entity entity : entities) {
            try {
                entity.addComponent((IComponent) clazz.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getName() {
        return "Add " + clazz.getSimpleName().replace("Component", "") + " Component";
    }

}
