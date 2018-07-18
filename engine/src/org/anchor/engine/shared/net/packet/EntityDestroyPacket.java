package org.anchor.engine.shared.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.shared.net.CorePacketManager;

public class EntityDestroyPacket implements IPacket {

    public int id;

    public EntityDestroyPacket() {

    }

    public EntityDestroyPacket(int id) {
        this.id = id;
    }

    public int getId() {
        return CorePacketManager.ENTITY_DESTROY_PACKET;
    }

    @Override
    public void write(DataOutputStream stream) throws Exception {
        stream.writeInt(id);
    }

    @Override
    public void read(DataInputStream stream) throws Exception {
        id = stream.readInt();
    }

}
