package org.anchor.game.editor.commands;

import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;

public class AddComponentCallback implements CommandCallback {

    protected Entity entity;
    protected IComponent component;

    public AddComponentCallback(Entity entity, IComponent component) {
        this.entity = entity;
        this.component = component;
    }

    @Override
    public void undo() {
        entity.removeComponent(component);
    }

    @Override
    public void redo() {
        entity.addComponent(component);
    }

    @Override
    public String getName() {
        return "Add " + component.getClass().getSimpleName().replace("Component", "") + " Component";
    }

}
