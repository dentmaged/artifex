package org.anchor.engine.shared.console;

import org.anchor.engine.common.Log;
import org.anchor.engine.shared.net.IUser;

public abstract class GameCommand {

    private String name, description;
    private boolean serverOnly, cheat;

    public GameCommand(String name, String description) {
        this(name, description, false, false);
    }

    public GameCommand(String name, String description, boolean serverOnly, boolean cheat) {
        this.name = name;
        this.description = description;
        this.serverOnly = serverOnly;
        this.cheat = cheat;

        GameCommandManager.register(this);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isServerOnly() {
        return serverOnly;
    }

    public boolean isCheat() {
        return cheat;
    }

    public void printDescription() {
        Log.print(description);
    }

    public abstract void run(IUser sender, String[] args);

    protected String getJoinedString(int index, String[] args) {
        StringBuilder builder = new StringBuilder();
        for (int i = index; i < args.length; i++)
            builder.append(args[i]).append(" ");
        builder.setLength(builder.length() - 1);

        return builder.toString();
    }

}
