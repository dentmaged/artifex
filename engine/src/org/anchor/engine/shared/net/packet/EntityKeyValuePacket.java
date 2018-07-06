package org.anchor.engine.shared.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.shared.net.CorePacketManager;

public class EntityKeyValuePacket implements IPacket {

    public int id;
    public String key, value;

    public EntityKeyValuePacket() {

    }

    public EntityKeyValuePacket(int id, String key, String value) {
        this.id = id;
        this.key = key;
        this.value = value;
    }

    public int getId() {
        return CorePacketManager.ENTITY_KEY_VALUE_PACKET;
    }

    @Override
    public void write(DataOutputStream stream) throws Exception {
        stream.writeInt(id);
        stream.writeUTF(key);
        stream.writeUTF(value);
    }

    @Override
    public void read(DataInputStream stream) throws Exception {
        id = stream.readInt();
        key = stream.readUTF();
        value = stream.readUTF();
    }

}
