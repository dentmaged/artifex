package org.anchor.game;

import org.anchor.game.server.AppManager;
import org.anchor.game.server.GameServer;

public class GameStart {

    public static void gameVarInit() {

    }

    public static void gameInit() {
        AppManager.create(new GameServer());
    }

}
