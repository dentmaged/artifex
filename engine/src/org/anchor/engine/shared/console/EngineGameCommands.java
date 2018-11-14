package org.anchor.engine.shared.console;

import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.net.IUser;

public class EngineGameCommands {

    public static void init() {
        new GameCommand("noclip", "Enables/disables collision detection", true, true) {

            @Override
            public void run(IUser sender, String[] args) {
                sender.sendMessage("Noclip enabled.");
                System.out.println("noclip");
                sender.getPlayer().getComponent(LivingComponent.class).gravity = false;
            }

        };
    }

}
