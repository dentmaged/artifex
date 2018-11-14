package org.anchor.engine.shared.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.shared.net.CorePacketManager;

public class SendMessagePacket implements IPacket {

    public String message;
    public int type;

    public SendMessagePacket() {

    }

    public SendMessagePacket(String message, int type) {
        this.message = message;
        this.type = type;
    }

    public int getId() {
        return CorePacketManager.SEND_MESSAGE_PACKET;
    }

    @Override
    public void write(DataOutputStream stream) throws Exception {
        stream.writeUTF(message);
        stream.writeInt(type);
    }

    @Override
    public void read(DataInputStream stream) throws Exception {
        message = stream.readUTF();
        type = stream.readInt();
    }

}
