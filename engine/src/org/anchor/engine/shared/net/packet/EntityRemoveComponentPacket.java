package org.anchor.engine.shared.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.net.CorePacketManager;

public class EntityRemoveComponentPacket implements IPacket {

    public int id;
    public Class<? extends IComponent> clazz;

    public EntityRemoveComponentPacket() {

    }

    public EntityRemoveComponentPacket(int id, Class<? extends IComponent> clazz) {
        this.id = id;
        this.clazz = clazz;
    }

    public int getId() {
        return CorePacketManager.ENTITY_REMOVE_COMPONENT_PACKET;
    }

    @Override
    public void write(DataOutputStream stream) throws Exception {
        stream.writeInt(id);
        stream.writeUTF(clazz.getCanonicalName());
    }

    @Override
    public void read(DataInputStream stream) throws Exception {
        id = stream.readInt();
        clazz = (Class<? extends IComponent>) Class.forName(stream.readUTF());
    }

}
