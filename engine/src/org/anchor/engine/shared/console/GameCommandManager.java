package org.anchor.engine.shared.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.anchor.engine.shared.net.IUser;

public class GameCommandManager {

    private static List<GameCommand> commands = new ArrayList<GameCommand>();

    public static void register(GameCommand cmd) {
        commands.add(cmd);
    }

    public static GameCommand getByName(String name) {
        for (GameCommand cmd : commands)
            if (cmd.getName().equalsIgnoreCase(name))
                return cmd;

        return null;
    }

    public static void run(IUser user, String command) {
        String[] split = command.split(" ");
        GameCommand cmd = getByName(split[0]);
        if (cmd == null) {
            user.sendMessage(command);
            user.sendMessage("Command '" + split[0] + "' not found!");

            return;
        }

        if (cmd.isCheat() && EngineGameVariables.sv_cheats.getValueAsBool()) {
            user.sendMessage("sv_cheats is disabled.");

            return;
        }

        if (!user.canRunCommand(cmd))
            return;

        user.runCommand(command, cmd, Arrays.copyOfRange(split, 1, split.length));
    }

}
