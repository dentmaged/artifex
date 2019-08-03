package org.anchor.engine.common.net.server;

import java.net.InetAddress;
import java.net.ServerSocket;

import org.anchor.engine.common.net.packet.IPacketHandler;

public class Server {

    protected IPacketHandler handler;
    protected ServerSocket socket;

    public Server(IPacketHandler handler, String ip, int port) {
        try {
            this.handler = handler;
            this.socket = new ServerSocket(port, 0, InetAddress.getByName(ip));

            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
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
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            socket.close();
            socket = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
