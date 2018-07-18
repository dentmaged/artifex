package org.anchor.game;

import org.anchor.game.client.GameClient;
import org.anchor.game.client.app.AppManager;

public class GameStart {

    public static void gameVarInit() {

    }

    public static void gameInit() {
        AppManager.create(new GameClient());
    }

}
