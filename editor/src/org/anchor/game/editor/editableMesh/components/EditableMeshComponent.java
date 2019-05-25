package org.anchor.game.editor.editableMesh.components;

import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.utils.Property;
import org.anchor.game.editor.editableMesh.types.SelectionMode;

public class EditableMeshComponent implements IComponent {

    @Property("Selection Mode")
    public SelectionMode selectionMode = SelectionMode.ELEMENT;

    @Override
    public void precache(Entity entity) {

    }

    @Override
    public void spawn() {

    }

    @Override
    public void setValue(String key, String value) {

    }

    @Override
    public IComponent copy() {
        return null;
    }

}
