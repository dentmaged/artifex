package org.anchor.engine.common.console;

import org.anchor.engine.common.multiplayer.User;

public abstract class GameCommand {

    private String name, description;
    private boolean serverOnly;

    public GameCommand(String name, String description) {
        this(name, description, false);
    }

    public GameCommand(String name, String description, boolean serverOnly) {
        this.name = name;
        this.description = description;
        this.serverOnly = serverOnly;
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

    public void printDescription() {
        System.out.println(description);
    }

    public abstract void run(User sender, String[] args);

}