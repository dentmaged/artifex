package org.anchor.game.editor.commands;

import java.util.List;

import org.anchor.engine.common.utils.JavaField;
import org.anchor.game.editor.ui.LevelEditor;

public class FieldsSetArrayCallback implements CommandCallback {

    protected JavaField field;
    protected List<Object> targets, values, previous;

    public FieldsSetArrayCallback(JavaField field, List<Object> targets, List<Object> values, List<Object> previous) {
        this.field = field;
        this.targets = targets;
        this.values = values;
        this.previous = previous;
    }

    @Override
    public void undo() {
        try {
            for (int i = 0; i < targets.size(); i++)
                field.set(targets.get(i), previous.get(i));
        } catch (Exception e) {
            e.printStackTrace();
        }
        LevelEditor.getInstance().refreshComponentValues();
    }

    @Override
    public void redo() {
        try {
            for (int i = 0; i < targets.size(); i++)
                field.set(targets.get(i), values.get(i));
        } catch (Exception e) {
            e.printStackTrace();
        }
        LevelEditor.getInstance().refreshComponentValues();
    }

    @Override
    public String getName() {
        return "Modify " + targets.get(0).getClass().getSimpleName() + "'s " + field.getName();
    }

}
