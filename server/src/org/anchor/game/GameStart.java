package org.anchor.game;

import java.util.Map;

import org.anchor.engine.shared.console.EngineGameCommands;
import org.anchor.engine.shared.console.EngineGameVariables;
import org.anchor.game.server.AppManager;
import org.anchor.game.server.GameServer;
import org.anchor.game.server.ServerGameCommands;
import org.anchor.game.server.ServerGameVariables;
import org.anchor.game.server.Settings;

public class GameStart {

    public static void gameVarInit(Map<String, String> args) {
        if (args.containsKey("ip"))
            Settings.ip = args.get("ip");

        if (args.containsKey("port"))
            Settings.port = Integer.parseInt(args.get("port"));

        if (args.containsKey("hostname"))
            Settings.hostname = args.get("hostname");

        EngineGameVariables.init();
        EngineGameCommands.init();

        ServerGameVariables.init();
        ServerGameCommands.init();
    }

    public static void gameInit() {
        AppManager.create(new GameServer());
    }

}
