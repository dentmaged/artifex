package org.anchor.game.editor.commands;

import org.anchor.game.editor.ui.LevelEditor;
import org.lwjgl.util.vector.Vector3f;

public class VectorChangeCallback implements CommandCallback {

    protected Vector3f target, difference;

    public VectorChangeCallback(Vector3f target, Vector3f difference) {
        this.target = target;
        this.difference = difference;
    }

    @Override
    public void undo() {
        Vector3f.sub(target, difference, target);
        LevelEditor.getInstance().refreshComponentValues();
    }

    @Override
    public void redo() {
        Vector3f.add(target, difference, target);
        LevelEditor.getInstance().refreshComponentValues();
    }

    @Override
    public String getName() {
        return "Modify transformation (" + target.x + " " + target.y + " " + target.z + ")";
    }

}
