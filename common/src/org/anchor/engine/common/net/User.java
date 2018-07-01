package org.anchor.engine.common.net;

import org.anchor.engine.common.console.GameCommand;

public interface User {

    public String getName();

    public void sendChatMessage(String message);

    public boolean canRunCommand(GameCommand command);

}
