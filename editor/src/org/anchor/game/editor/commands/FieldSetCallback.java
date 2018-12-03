package org.anchor.game.editor.commands;

import org.anchor.engine.common.utils.JavaField;
import org.anchor.game.editor.ui.LevelEditor;

public class FieldSetCallback implements CommandCallback {

    protected JavaField field;
    protected Object target, value, previous;

    public FieldSetCallback(JavaField field, Object target, Object value, Object previous) {
        this.field = field;
        this.target = target;
        this.value = value;
        this.previous = previous;
    }

    @Override
    public void undo() {
        try {
            field.set(target, previous);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LevelEditor.getInstance().refreshComponentValues();
    }

    @Override
    public void redo() {
        try {
            field.set(target, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LevelEditor.getInstance().refreshComponentValues();
    }

    @Override
    public String getName() {
        return "Modify " + target.getClass().getSimpleName() + "'s " + field.getName();
    }

}
