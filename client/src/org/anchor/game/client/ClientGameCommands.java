package org.anchor.game.client;

import org.anchor.client.engine.renderer.Renderer;
import org.anchor.client.engine.renderer.keyboard.Binds;
import org.anchor.engine.shared.console.GameCommand;
import org.anchor.engine.shared.net.IUser;

public class ClientGameCommands {

    public static void init() {
        new GameCommand("r_reloadShaders", "Reloads shaders") {

            @Override
            public void run(IUser sender, String[] args) {
                Renderer.reloadShaders();
            }

        };

        new GameCommand("quit", "Quits the game") {

            @Override
            public void run(IUser sender, String[] args) {
                GameClient.shutdownGame();
                System.exit(0);
            }

        };

        new GameCommand("bind", "Runs the specified command when the key is pressed") {

            @Override
            public void run(IUser sender, String[] args) {
                if (args.length < 2) {
                    printDescription();
                    return;
                }

                String key = args[0];
                String command = getJoinedString(1, args);

                Binds.getInstance().addBind(key, command);
            }

        };
    }

}
