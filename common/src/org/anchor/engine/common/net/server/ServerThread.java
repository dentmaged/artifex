package org.anchor.engine.common.net.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import org.anchor.engine.common.net.BaseNetworkable;
import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.common.net.packet.IPacketHandler;
import org.anchor.engine.common.net.packet.PacketManager;

public class ServerThread extends BaseNetworkable implements Runnable {

    public ServerThread(Socket socket, IPacketHandler handler) {
        super(handler);
        this.socket = socket;

        try {
            this.input = new DataInputStream(socket.getInputStream());
            this.output = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            handler.connect(this);
            while (isConnected()) {
                int id = input.readInt();
                IPacket packet = PacketManager.getPacketByID(id);
                packet.read(input);

                handler.handlePacket(this, packet);
            }
            handler.disconnect(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public String getHostname() {
        return socket.getInetAddress().getHostAddress();
    }

}
