package org.anchor.engine.common.net.packet;

import org.anchor.engine.common.net.BaseNetworkable;

public interface IPacketHandler {

    public void connect(BaseNetworkable net);

    public void handlePacket(BaseNetworkable net, IPacket packet);

    public void handleException(Exception e);

    public void disconnect(BaseNetworkable net);

}
