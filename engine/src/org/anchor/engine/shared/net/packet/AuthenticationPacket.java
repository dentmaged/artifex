package org.anchor.engine.shared.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.shared.net.CorePacketManager;

public class AuthenticationPacket implements IPacket {

    public int protocolVersion;

    public AuthenticationPacket() {

    }

    public AuthenticationPacket(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    @Override
    public int getId() {
        return CorePacketManager.AUTHENTICATION_PACKET;
    }

    @Override
    public void write(DataOutputStream stream) throws Exception {
        stream.writeInt(protocolVersion);
    }

    @Override
    public void read(DataInputStream stream) throws Exception {
        protocolVersion = stream.readInt();
    }

}
