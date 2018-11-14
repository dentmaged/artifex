package org.anchor.engine.shared.console;

import org.anchor.engine.common.console.CoreGameVariableManager;
import org.anchor.engine.common.console.GameVariableType;
import org.anchor.engine.common.console.IGameVariable;

public class GameVariableManager {

    public static final GameVariable sv_cheats = new GameVariable("sv_cheats", "0", "Enables game cheats", GameVariableType.GAMEMODE) {

        @Override
        public void setValue(String value) {
            super.setValue(value);

            if (!getValueAsBool())
                for (IGameVariable variable : CoreGameVariableManager.getVariables())
                    if (variable != this)
                        variable.setValue(variable.getDefaultValue());
        }

    };

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
