package org.anchor.game.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anchor.engine.common.app.App;
import org.anchor.engine.common.net.BaseNetworkable;
import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.common.net.packet.IPacketHandler;
import org.anchor.engine.common.net.server.Server;
import org.anchor.engine.common.net.server.ServerThread;
import org.anchor.engine.common.utils.FileHelper;
import org.anchor.engine.shared.Engine;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.components.SpawnComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.monitoring.SceneMonitor;
import org.anchor.engine.shared.net.CorePacketManager;
import org.anchor.engine.shared.net.packet.EntityLinkPacket;
import org.anchor.engine.shared.net.packet.LevelChangePacket;
import org.anchor.engine.shared.net.packet.PlayerMovementPacket;
import org.anchor.engine.shared.net.packet.PlayerPositionPacket;
import org.anchor.engine.shared.physics.PhysicsEngine;
import org.anchor.engine.shared.profiler.Profiler;
import org.anchor.engine.shared.scene.Scene;
import org.anchor.engine.shared.scheduler.Scheduler;
import org.anchor.engine.shared.terrain.Terrain;
import org.anchor.engine.shared.utils.Side;
import org.anchor.game.server.components.ServerInputComponent;
import org.anchor.game.server.components.ServerThreadComponent;
import org.anchor.game.server.filter.AllPlayersFilter;
import org.lwjgl.util.vector.Vector3f;

public class GameServer extends App implements IPacketHandler {

    protected Server server;
    protected String level = "level0";

    protected Scene scene;
    protected SceneMonitor monitor;
    protected Vector3f spawn = new Vector3f();

    protected float accumulator;
    protected PhysicsEngine physics;

    protected Map<ServerThread, Entity> clients = new HashMap<ServerThread, Entity>();

    @Override
    public void init() {
        Engine.init(Side.SERVER, new ServerEngine());
        CorePacketManager.register();
        server = new Server(this, 24964);

        scene = new GameMap(FileHelper.newGameFile("maps", level + ".asg")).getScene();
        for (Entity entity : scene.getEntitiesWithComponent(SpawnComponent.class))
            spawn.set(entity.getPosition());
        physics = new PhysicsEngine();
        monitor = new SceneMonitor(scene);
    }

    @Override
    public void update() {
        Profiler.start("Game Update");
        accumulator += AppManager.getFrameTimeSeconds();
        Profiler.start("Physics");
        while (accumulator >= PhysicsEngine.TICK_DELAY) {
            accumulator -= PhysicsEngine.TICK_DELAY;

            if (accumulator > 0.2)
                accumulator = 0;

            if (scene != null) {
                scene.updateFixed();
                physics.update(scene);
            }
            Scheduler.tick();

            for (Entity player : clients.values()) {
                player.getComponent(LivingComponent.class).move(scene, getTerrainByPoint(player.getPosition()));
                player.getComponent(ServerThreadComponent.class).net.sendPacket(new PlayerPositionPacket(player.getPosition()));
                LivingComponent livingComponent = player.getComponent(LivingComponent.class);

                if (player.getPosition().y < -15)
                    livingComponent.health = 0;

                if (livingComponent.health <= 0) {
                    livingComponent.health = 100;
                    player.getVelocity().set(0, 0, 0);
                    player.getPosition().set(spawn);
                }
            }

            if (monitor != null)
                monitor.check();
        }
        Profiler.end("Physics");

        Profiler.start("Scene Update");
        if (scene != null)
            scene.update();
        Profiler.end("Scene Update");
        Profiler.end("Game Update");

        Profiler.frameEnd();
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
        Entity player = new Entity(ServerInputComponent.class, ServerThreadComponent.class);
        player.setValue("collisionMesh", "player");
        player.getComponent(ServerThreadComponent.class).net = thread;
        player.spawn();

        clients.put(thread, player);
        net.sendPacket(new LevelChangePacket(level));

        for (Entity entity : scene.getEntities())
            new AllPlayersFilter().sendPacket(new EntityLinkPacket(entity.getId(), entity.getLineIndex()));
    }

    @Override
    public void handlePacket(BaseNetworkable net, IPacket packet) {
        if (packet.getId() == CorePacketManager.PLAYER_MOVEMENT_PACKET)
            clients.get(net).getComponent(ServerInputComponent.class).playerMovementPacket = (PlayerMovementPacket) packet;
    }

    @Override
    public void disconnect(BaseNetworkable net) {
        clients.remove(net);
        net.disconnect();
    }

    @Override
    public String getTitle() {
        return "GameServer";
    }

    public List<Entity> getPlayers() {
        return new ArrayList<Entity>(clients.values());
    }

    public static void main(String[] args) {
        AppManager.create(new GameServer());
    }

    public static GameServer getServer() {
        return (GameServer) AppManager.getInstance();
    }

    public static Terrain getTerrainByPoint(Vector3f point) {
        Scene scene = GameServer.getServer().scene;
        if (scene == null)
            return null;

        for (Terrain terrain : scene.getTerrains()) {
            if (point.x >= terrain.getX() && point.x <= terrain.getX() + terrain.getSize() && point.z >= terrain.getZ() && point.z <= terrain.getZ() + terrain.getSize())
                return terrain;
        }

        return null;
    }

}
