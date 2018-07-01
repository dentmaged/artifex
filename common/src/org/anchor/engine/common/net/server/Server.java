package org.anchor.engine.common.net.server;

import java.net.ServerSocket;

import org.anchor.engine.common.net.packet.IPacketHandler;

public class Server {

    protected IPacketHandler handler;
    protected ServerSocket socket;

    public Server(IPacketHandler handler, int port) {
        try {
            this.handler = handler;
            this.socket = new ServerSocket(port);

            while (true) {
                ServerThread runnable = new ServerThread(socket.accept(), handler);
                Thread thread = new Thread(runnable);

                runnable.setThread(thread);
                thread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
