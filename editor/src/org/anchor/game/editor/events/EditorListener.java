package org.anchor.game.editor.events;

import org.anchor.engine.common.events.Listener;
import org.anchor.engine.common.events.handler.EventHandler;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.events.component.AddComponentEvent;
import org.anchor.engine.shared.events.entity.EntityDestroyEvent;
import org.anchor.game.editor.ui.LevelEditor;

public class EditorListener implements Listener {

    @EventHandler
    public void onComponentAdd(AddComponentEvent event) {
        Entity entity = event.getEntity();

        if (LevelEditor.getInstance().getSelectedObjects().contains(entity))
            LevelEditor.getInstance().reloadEntityComponents(LevelEditor.getInstance().getSelectedObjects());
    }

    @EventHandler
    public void onEntityDestroy(EntityDestroyEvent event) {
        Entity entity = event.getEntity();

        if (LevelEditor.getInstance().getSelectedObjects().contains(entity))
            LevelEditor.getInstance().removeFromSelection(entity);
    }

}
