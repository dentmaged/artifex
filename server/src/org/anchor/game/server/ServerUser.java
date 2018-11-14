package org.anchor.game.server;

import org.anchor.engine.common.net.server.ServerThread;
import org.anchor.engine.shared.console.GameCommand;
import org.anchor.engine.shared.console.GameVariable;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.net.IUser;
import org.anchor.engine.shared.net.packet.SendMessagePacket;

public class ServerUser implements IUser {

    private String name;
    private ServerThread thread;
    private Entity player;

    public ServerUser(String name, ServerThread thread, Entity player) {
        this.name = name;
        this.thread = thread;
        this.player = player;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void sendMessage(String message) {
        thread.sendPacket(new SendMessagePacket(message, 0));
    }

    @Override
    public void sendChatMessage(String message) {
        thread.sendPacket(new SendMessagePacket(message, 1));
    }

    @Override
    public boolean canRunCommand(GameCommand command) {
        return thread.getHostname().equals("127.0.0.1"); // localhost is by default admin
    }

    @Override
    public boolean canSetVariable(GameVariable variable) {
        return thread.getHostname().equals("127.0.0.1");
    }

    @Override
    public Entity getPlayer() {
        return player;
    }

}
