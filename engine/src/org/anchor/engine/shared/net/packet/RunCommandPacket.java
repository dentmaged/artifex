package org.anchor.engine.shared.net.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.shared.net.CorePacketManager;

public class RunCommandPacket implements IPacket {

    public String command;

    public RunCommandPacket() {

    }

    public RunCommandPacket(String command) {
        this.command = command;
    }

    public int getId() {
        return CorePacketManager.RUN_COMMAND_PACKET;
    }

    @Override
    public void write(DataOutputStream stream) throws Exception {
        stream.writeUTF(command);
    }

    @Override
    public void read(DataInputStream stream) throws Exception {
        command = stream.readUTF();
    }

}
