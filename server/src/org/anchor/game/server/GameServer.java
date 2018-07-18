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
import org.anchor.engine.shared.components.PhysicsComponent;
import org.anchor.engine.shared.components.SpawnComponent;
import org.anchor.engine.shared.components.redirections.EngineMeshComponent;
import org.anchor.engine.shared.console.GameVariable;
import org.anchor.engine.shared.console.GameVariableManager;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.monitoring.EntityMonitor;
import org.anchor.engine.shared.monitoring.SceneMonitor;
import org.anchor.engine.shared.net.CorePacketManager;
import org.anchor.engine.shared.net.packet.AuthenticationPacket;
import org.anchor.engine.shared.net.packet.EntityLinkPacket;
import org.anchor.engine.shared.net.packet.EntitySpawnPacket;
import org.anchor.engine.shared.net.packet.GameVariablePacket;
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
import org.lwjgl.util.vector.Vector3f;

public class GameServer extends App implements IPacketHandler {

    protected Server server;
    protected String level = "test";

    protected Scene scene;
    protected SceneMonitor monitor;
    protected Map<Entity, EntityMonitor> clientMonitors = new HashMap<Entity, EntityMonitor>();
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
        Engine.scene = scene;
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
                LivingComponent livingComponent = player.getComponent(LivingComponent.class);

                if (player.getPosition().y < -15)
                    livingComponent.health = 0;

                if (livingComponent.health <= 0) {
                    livingComponent.health = 100;

                    player.getVelocity().set(0, 0, 0);
                    player.getPosition().set(spawn);
                }

                player.getComponent(ServerThreadComponent.class).net.sendPacket(new PlayerPositionPacket(player.getPosition()));
            }

            if (monitor != null)
                monitor.check();

            for (EntityMonitor entityMonitor : clientMonitors.values())
                entityMonitor.check();
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

    }

    @Override
    public void handlePacket(BaseNetworkable net, IPacket receivedPacket) {
        if (receivedPacket.getId() == CorePacketManager.AUTHENTICATION_PACKET) {
            if (((AuthenticationPacket) receivedPacket).protocolVersion == Engine.PROTOCOL_VERSION) {
                ServerThread thread = (ServerThread) net;
                Entity player = new Entity(ServerInputComponent.class, ServerThreadComponent.class, EngineMeshComponent.class);
                player.setValue("collisionMesh", "player");
                player.setValue("model", "player");

                player.getComponent(ServerThreadComponent.class).net = thread;
                player.getComponent(ServerThreadComponent.class).user = new ServerUser("", thread);

                player.getComponent(PhysicsComponent.class).gravity = false;

                player.spawn();

                net.sendPacket(new LevelChangePacket(level));

                for (Entity entity : scene.getEntities())
                    if (entity.getLineIndex() == -1)
                        net.sendPacket(new EntitySpawnPacket(entity));
                    else
                        net.sendPacket(new EntityLinkPacket(entity.getId(), entity.getLineIndex()));
                for (Entity entity : clients.values()) {
                    System.out.println("SENDING CLIENT INFO");

                    net.sendPacket(new EntitySpawnPacket(entity));
                    entity.getComponent(ServerThreadComponent.class).net.sendPacket(new EntitySpawnPacket(player));
                }

                clients.put(thread, player);
                clientMonitors.put(player, new EntityMonitor(player));
            } else {
                net.disconnect();
            }
        } else if (receivedPacket.getId() == CorePacketManager.PLAYER_MOVEMENT_PACKET) {
            clients.get(net).getComponent(ServerInputComponent.class).playerMovementPacket = (PlayerMovementPacket) receivedPacket;
        } else if (receivedPacket.getId() == CorePacketManager.GAME_VARIABLE_PACKET) {
            GameVariablePacket packet = (GameVariablePacket) receivedPacket;
            GameVariable var = GameVariableManager.getByName(packet.name);

            if (clients.get(net).getComponent(ServerThreadComponent.class).user.canSetVariable(var))
                var.setValue(packet.value);
        }
    }

    @Override
    public void disconnect(BaseNetworkable net) {
        clients.remove(net);
        clientMonitors.remove(clients.get(net));
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
