package org.anchor.engine.shared.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.shared.net.CorePacketManager;

public class EntityLinkPacket implements IPacket {

    public int id, lineIndex;

    public EntityLinkPacket() {

    }

    public EntityLinkPacket(int id, int lineIndex) {
        this.id = id;
        this.lineIndex = lineIndex;
    }

    public int getId() {
        return CorePacketManager.ENTITY_LINK_PACKET;
    }

    @Override
    public void write(DataOutputStream stream) throws Exception {
        stream.writeInt(id);
        stream.writeInt(lineIndex);
    }

    @Override
    public void read(DataInputStream stream) throws Exception {
        id = stream.readInt();
        lineIndex = stream.readInt();
    }

}
