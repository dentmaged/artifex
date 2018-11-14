package org.anchor.engine.shared.console;

import java.util.Arrays;

import org.anchor.engine.shared.net.IUser;

public class Console {

    public static void run(IUser user, String command) {
        String[] split = command.split(" ");
        GameVariable variable = GameVariableManager.getByName(split[0]);
        if (variable == null) {
            GameCommand cmd = GameCommandManager.getByName(split[0]);
            if (cmd == null) {
                user.sendMessage("Invalid variable or command.");

                return;
            }

            GameCommandManager.run(user, command);
        } else {
            if (split.length == 1) {
                user.sendMessage(variable.getName() + " = " + variable.getValueAsString() + " (default " + variable.getDefaultValue() + ")");
                user.sendMessage("\t" + variable.getDescription());

                return;
            }

            if (user.canSetVariable(variable))
                variable.setValue(String.join(" ", Arrays.copyOfRange(split, 1, split.length)).trim());
        }
    }

}
