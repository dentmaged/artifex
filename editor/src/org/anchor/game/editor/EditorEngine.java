package org.anchor.game.editor;

import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.game.client.ClientEngine;

public class EditorEngine extends ClientEngine {

    @Override
    public void onComponentAdd(Entity entity, IComponent component) {
        super.onComponentAdd(entity, component);

        if (GameEditor.getInstance().getLevelEditor().getSelectedEntity() == entity)
            GameEditor.getInstance().getLevelEditor().reloadEntityComponents(entity);
    }

}
