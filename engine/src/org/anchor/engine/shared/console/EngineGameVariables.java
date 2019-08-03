package org.anchor.engine.shared.console;

import org.anchor.engine.common.console.CoreGameVariableManager;
import org.anchor.engine.common.console.GameVariableType;
import org.anchor.engine.common.console.IGameVariable;

public class EngineGameVariables {

    public static final GameVariable sv_cheats = new GameVariable("sv_cheats", "0", "Enables game cheats", GameVariableType.GAMEMODE) {

        @Override
        public void setValue(String value) {
            super.setValue(value);

            if (!getValueAsBool())
                for (IGameVariable variable : CoreGameVariableManager.getVariables())
                    if (variable != this && variable.getType() == GameVariableType.CHEAT)
                        variable.setValue(variable.getDefaultValue());
        }

    };

    public static final GameVariable sv_running = new GameVariable("sv_running", "0", "Is a dedicated or listen server running", GameVariableType.INTERNAL);

    public static final GameVariable mp_reachDistance = new GameVariable("mp_reachDistance", "4", "Maximum distance between player and interactable object", GameVariableType.GAMEMODE);

    public static void init() {

    }

}
