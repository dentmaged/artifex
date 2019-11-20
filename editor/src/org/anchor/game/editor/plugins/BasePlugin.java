package org.anchor.game.editor.plugins;

public abstract class BasePlugin {

    private PluginDataContainer data;

    public abstract void enable();

    public abstract void disable();

    public PluginDataContainer getData() {
        return data;
    }

}
