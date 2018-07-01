package org.anchor.game.server;

import java.util.HashMap;
import java.util.Map;

import org.anchor.engine.common.app.App;
import org.anchor.engine.common.net.BaseNetworkable;
import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.common.net.packet.IPacketHandler;
import org.anchor.engine.common.net.server.Server;
import org.anchor.engine.common.net.server.ServerThread;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.net.CorePacketManager;
import org.anchor.engine.shared.net.packet.LevelChangePacket;
import org.anchor.game.server.components.ServerThreadComponent;

public class GameServer extends App implements IPacketHandler {

    protected Server server;
    protected String level = "level0";

    protected Map<ServerThread, Entity> clients = new HashMap<ServerThread, Entity>();

    @Override
    public void init() {
        CorePacketManager.register();
        server = new Server(this, 24964);
    }

    @Override
    public void update() {

    }

    @Override
    public void render() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void connect(BaseNetworkable net) {
        ServerThread thread = (ServerThread) net;
        Entity entity = new Entity();
        clients.put(thread, entity);

        ServerThreadComponent component = new ServerThreadComponent();
        component.net = thread;
        entity.addComponent(component);

        net.sendPacket(new LevelChangePacket(level));
    }

    @Override
    public void handlePacket(BaseNetworkable net, IPacket packet) {

    }

    @Override
    public void disconnect(BaseNetworkable net) {

    }

    @Override
    public String getTitle() {
        return "GameServer";
    }

    public static void main(String[] args) {
        AppManager.create(new GameServer());
    }

}
