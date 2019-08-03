package org.anchor.game;

import java.util.Map;

import org.anchor.game.client.GameClient;
import org.anchor.game.client.app.AppManager;

public class GameStart {

    public static void gameVarInit(Map<String, String> args) {

    }

    public static void gameInit() {
        AppManager.create(new GameClient());
    }

}
