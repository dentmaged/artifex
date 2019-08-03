package org.anchor.engine.shared.console;

import org.anchor.engine.common.console.CoreGameVariableManager;
import org.anchor.engine.common.console.GameVariableType;

public class GameVariableManager {

    public static GameVariable getByName(String name) {
        return (GameVariable) CoreGameVariableManager.getByName(name);
    }

    public static GameVariable get(String name, String defaultValue, String description, GameVariableType type) {
        GameVariable value = getByName(name);
        if (value != null)
            return value;

        return new GameVariable(name, defaultValue, description, type);
    }

}
