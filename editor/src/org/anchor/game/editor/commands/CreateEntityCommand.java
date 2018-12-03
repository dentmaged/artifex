package org.anchor.game.editor.commands;

import org.anchor.engine.shared.Engine;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.editor.ui.LevelEditor;

public class CreateEntityCommand implements CommandCallback {

    protected Entity entity;

    public CreateEntityCommand(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void undo() {
        entity.destroy();
        LevelEditor.getInstance().updateList();
    }

    @Override
    public void redo() {
        Engine.getInstance().onEntityCreate(entity);
        Engine.getEntities().add(entity);
        LevelEditor.getInstance().updateList();
    }

    @Override
    public String getName() {
        return "Create entity";
    }

}
