package org.anchor.engine.shared.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.shared.net.CorePacketManager;

public class LevelChangePacket implements IPacket {

    public String level;

    public LevelChangePacket() {

    }

    public LevelChangePacket(String level) {
        this.level = level;
    }

    public int getId() {
        return CorePacketManager.LEVEL_CHANGE_PACKET;
    }

    @Override
    public void write(DataOutputStream stream) throws Exception {
        stream.writeUTF(level);
    }

    @Override
    public void read(DataInputStream stream) throws Exception {
        level = stream.readUTF();
    }

}
