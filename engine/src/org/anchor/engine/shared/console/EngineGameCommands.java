package org.anchor.engine.shared.console;

import java.io.File;
import java.util.regex.Pattern;

import org.anchor.engine.common.utils.FileHelper;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.net.IUser;

public class EngineGameCommands {

    public static void init() {
        new GameCommand("noclip", "Enables/disables collision detection", true, true) {

            @Override
            public void run(IUser sender, String[] args) {
                LivingComponent living = sender.getPlayer().getComponent(LivingComponent.class);
                if (living.gravity)
                    sender.sendMessage("noclip ON");
                else
                    sender.sendMessage("noclip OFF");

                System.out.println("noclip toggled");

                living.gravity = !living.gravity;
            }

        };

        new GameCommand("exec", "Runs the specific config file (in /cfg)") {

            @Override
            public void run(IUser sender, String[] args) {
                if (args.length == 0) {
                    printDescription();
                    return;
                }

                String file = getJoinedString(0, args).replaceAll(Pattern.quote(".."), "");
                while (file.startsWith("./"))
                    file = file.substring(2);

                for (String line : FileHelper.read(new File("cfg", file + ".cfg")).split("\n"))
                    if (line.trim().length() > 0)
                        Console.run(sender, line.trim());
            }

        };
    }

}
