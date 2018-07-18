package org.anchor.game.server;

import org.anchor.engine.common.net.server.ServerThread;
import org.anchor.engine.shared.console.GameCommand;
import org.anchor.engine.shared.console.GameVariable;
import org.anchor.engine.shared.net.IUser;

public class ServerUser implements IUser {

    private String name;
    private ServerThread thread;

    public ServerUser(String name, ServerThread thread) {
        this.name = name;
        this.thread = thread;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void sendChatMessage(String message) {

    }

    @Override
    public boolean canRunCommand(GameCommand command) {
        return thread.getHostname().equals("127.0.0.1"); // localhost is by default admin
    }

    @Override
    public boolean canSetVariable(GameVariable variable) {
        return thread.getHostname().equals("127.0.0.1");
    }

}
