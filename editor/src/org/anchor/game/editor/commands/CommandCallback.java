package org.anchor.game.editor.commands;

public interface CommandCallback {

    public void undo();

    public void redo();

    public String getName();

}
