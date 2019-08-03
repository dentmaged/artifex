package org.anchor.game.client;

import org.anchor.engine.common.Log;
import org.anchor.engine.shared.console.GameCommand;
import org.anchor.engine.shared.console.GameVariable;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.net.IUser;
import org.anchor.engine.shared.net.packet.GameVariablePacket;
import org.anchor.engine.shared.net.packet.RunCommandPacket;

public class LocalUser implements IUser {

    @Override
    public String getName() {
        return ""; // TODO steam integration
    }

    @Override
    public void sendMessage(String message) {
        Log.print(message);
    }

    @Override
    public void sendChatMessage(String message) {
        // TODO chat
    }

    @Override
    public boolean canRunCommand(GameCommand command) {
        return true;
    }

    @Override
    public boolean canSetVariable(GameVariable variable) {
        return true;
    }

    @Override
    public Entity getPlayer() {
        return GameClient.getPlayer();
    }

    @Override
    public void setGameVariable(GameVariable var, String value) {
        if (GameClient.getClient() == null)
            var.setValue(value);
        else
            GameClient.getClient().sendPacket(new GameVariablePacket(var.getName(), value));
    }

    @Override
    public void runCommand(String command, GameCommand cmd, String[] args) {
        if (cmd.isServerOnly()) {
            if (GameClient.getClient() == null)
                return;

            GameClient.getClient().sendPacket(new RunCommandPacket(command));
        } else {
            cmd.run(this, args);
        }
    }

}
