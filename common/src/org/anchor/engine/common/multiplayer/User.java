package org.anchor.engine.common.multiplayer;

import java.io.Serializable;

public interface User {

    public void sendChatMessage(String message);

    public void sendPacket(Serializable packet);

}
