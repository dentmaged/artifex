package org.anchor.game.client;

import org.anchor.engine.common.Log;
import org.anchor.engine.shared.console.GameCommand;
import org.anchor.engine.shared.console.GameVariable;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.net.IUser;

public class ClientUser implements IUser {

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

}
