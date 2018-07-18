package org.anchor.engine.shared.net;

import org.anchor.engine.shared.console.GameCommand;
import org.anchor.engine.shared.console.GameVariable;

public interface IUser {

    public String getName();

    public void sendChatMessage(String message);

    public boolean canRunCommand(GameCommand command);

    public boolean canSetVariable(GameVariable variable);

}
