package org.anchor.engine.shared.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.shared.net.CorePacketManager;
import org.lwjgl.util.vector.Vector3f;

public class PlayerPositionPacket implements IPacket {

    public Vector3f position;

    public PlayerPositionPacket() {

    }

    public PlayerPositionPacket(Vector3f position) {
        this.position = position;
    }

    @Override
    public int getId() {
        return CorePacketManager.PLAYER_POSITION_PACKET;
    }

    @Override
    public void write(DataOutputStream stream) throws Exception {
        stream.writeFloat(position.x);
        stream.writeFloat(position.y);
        stream.writeFloat(position.z);
    }

    @Override
    public void read(DataInputStream stream) throws Exception {
        position = new Vector3f();

        position.x = stream.readFloat();
        position.y = stream.readFloat();
        position.z = stream.readFloat();
    }

}
