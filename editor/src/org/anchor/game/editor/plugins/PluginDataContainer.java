package org.anchor.game.editor.plugins;

public class PluginDataContainer {

    public String name, description, authors[], version, mainClass;

    public PluginDataContainer() {

    }

    public PluginDataContainer(String name, String mainClass) {
        this.name = name;
        this.mainClass = mainClass;
    }

}
