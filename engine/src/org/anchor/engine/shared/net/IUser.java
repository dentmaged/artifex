package org.anchor.engine.shared.net;

import org.anchor.engine.shared.console.GameCommand;
import org.anchor.engine.shared.console.GameVariable;
import org.anchor.engine.shared.entity.Entity;

public interface IUser {

    public String getName();

    public void sendMessage(String message);

    public void sendChatMessage(String message);

    public boolean canRunCommand(GameCommand command);

    public boolean canSetVariable(GameVariable variable);

    public Entity getPlayer();

    public void setGameVariable(GameVariable var, String value);

    public void runCommand(String command, GameCommand cmd, String[] args);

}
