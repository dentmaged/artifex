package org.anchor.engine.common.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.common.net.packet.IPacketHandler;

public class BaseNetworkable {

    protected IPacketHandler handler;
    protected Socket socket;
    protected DataInputStream input;
    protected DataOutputStream output;
    protected Thread thread;

    public BaseNetworkable(IPacketHandler handler) {
        this.handler = handler;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void disconnect() {
        if (!isConnected())
            return;

        handler.disconnect(this);
        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        socket = null;
    }

    public void sendPacket(IPacket packet) {
        if (!isConnected())
            return;

        try {
            output.writeInt(packet.getId());
            packet.write(output);
        } catch (Exception e) {
            disconnect();
            e.printStackTrace();
        }
    }

}
