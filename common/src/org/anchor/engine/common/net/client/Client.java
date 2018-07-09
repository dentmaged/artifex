package org.anchor.engine.common.net.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import org.anchor.engine.common.net.BaseNetworkable;
import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.common.net.packet.IPacketHandler;
import org.anchor.engine.common.net.packet.PacketManager;

public class Client extends BaseNetworkable {

    protected LinkedBlockingQueue<IPacket> packets = new LinkedBlockingQueue<IPacket>();

    public Client(IPacketHandler handler) {
        super(handler);
    }

    public void connect(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        System.out.println("Connected to " + ip + ":" + port);
                        handler.connect(Client.this);
                        while (isConnected()) {
                            int id = input.readInt();
                            IPacket packet = PacketManager.getPacketByID(id);
                            packet.read(input);

                            packets.offer(packet);
                        }
                        handler.disconnect(Client.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            });
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handle() {
        IPacket packet;
        while ((packet = packets.poll()) != null)
            handler.handlePacket(this, packet);
    }

    public void shutdown() {
        disconnect();
        thread = null;
    }

}
