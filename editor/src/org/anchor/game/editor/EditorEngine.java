package org.anchor.game.editor;

import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.game.client.ClientEngine;
import org.anchor.game.editor.ui.LevelEditor;

public class EditorEngine extends ClientEngine {

    @Override
    public void onComponentAdd(Entity entity, IComponent component) {
        super.onComponentAdd(entity, component);

        if (LevelEditor.getInstance().getSelectedObjects().contains(entity))
            LevelEditor.getInstance().reloadEntityComponents(LevelEditor.getInstance().getSelectedObjects());
    }

    @Override
    public void onEntityDestroy(Entity entity) {
        super.onEntityDestroy(entity);

        if (LevelEditor.getInstance().getSelectedObjects().contains(entity))
            LevelEditor.getInstance().removeFromSelection(entity);
    }

}
