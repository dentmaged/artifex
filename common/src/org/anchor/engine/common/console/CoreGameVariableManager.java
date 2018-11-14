package org.anchor.engine.common.console;

import java.util.ArrayList;
import java.util.List;

public class CoreGameVariableManager {

    private static List<IGameVariable> variables = new ArrayList<IGameVariable>();

    public static void register(IGameVariable var) {
        variables.add(var);
    }

    public static IGameVariable getByName(String name) {
        for (IGameVariable var : variables)
            if (var.getName().equalsIgnoreCase(name))
                return var;

        return null;
    }

    public static List<IGameVariable> getVariables() {
        return variables;
    }

}
