package org.anchor.engine.shared.console;

import java.util.ArrayList;
import java.util.List;

public class GameVariableManager {

    private static List<GameVariable> variables = new ArrayList<GameVariable>();

    public static final GameVariable cheats = new GameVariable("sv_cheats", "0", "Enables game cheats", GameVariableType.GAMEMODE);

    public static void register(GameVariable var) {
        variables.add(var);
    }

    public static GameVariable getByName(String name) {
        for (GameVariable var : variables)
            if (var.getName().equalsIgnoreCase(name))
                return var;

        return null;
    }

}
