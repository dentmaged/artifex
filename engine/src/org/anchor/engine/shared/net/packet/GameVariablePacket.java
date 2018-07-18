package org.anchor.engine.shared.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.shared.net.CorePacketManager;

public class GameVariablePacket implements IPacket {

    public String name, value;

    public GameVariablePacket() {

    }

    public GameVariablePacket(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public int getId() {
        return CorePacketManager.GAME_VARIABLE_PACKET;
    }

    @Override
    public void write(DataOutputStream stream) throws Exception {
        stream.writeUTF(name);
        stream.writeUTF(value);
    }

    @Override
    public void read(DataInputStream stream) throws Exception {
        name = stream.readUTF();
        value = stream.readUTF();
    }

}
