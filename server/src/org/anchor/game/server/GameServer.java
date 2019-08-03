package org.anchor.game.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anchor.engine.common.app.App;
import org.anchor.engine.common.console.CoreGameVariableManager;
import org.anchor.engine.common.console.GameVariableType;
import org.anchor.engine.common.console.IGameVariable;
import org.anchor.engine.common.net.BaseNetworkable;
import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.common.net.packet.IPacketHandler;
import org.anchor.engine.common.net.server.Server;
import org.anchor.engine.common.net.server.ServerThread;
import org.anchor.engine.common.utils.FileHelper;
import org.anchor.engine.shared.Engine;
import org.anchor.engine.shared.components.IInteractable;
import org.anchor.engine.shared.components.PhysicsComponent;
import org.anchor.engine.shared.components.redirections.EngineMeshComponent;
import org.anchor.engine.shared.console.EngineGameCommands;
import org.anchor.engine.shared.console.EngineGameVariables;
import org.anchor.engine.shared.console.GameCommandManager;
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
import org.anchor.engine.shared.net.packet.RunCommandPacket;
import org.anchor.engine.shared.physics.PhysicsEngine;
import org.anchor.engine.shared.profiler.Profiler;
import org.anchor.engine.shared.scene.Scene;
import org.anchor.engine.shared.scheduler.Scheduler;
import org.anchor.engine.shared.terrain.Terrain;
import org.anchor.engine.shared.utils.Side;
import org.anchor.game.server.components.ServerInputComponent;
import org.anchor.game.server.components.ServerThreadComponent;
import org.anchor.game.server.events.ServerListener;
import org.lwjgl.util.vector.Vector3f;

public class GameServer extends App implements IPacketHandler {

    protected Server server;
    protected String level = Settings.map;

    protected Scene scene;
    protected SceneMonitor monitor;
    protected Map<Entity, EntityMonitor> clientMonitors = new HashMap<Entity, EntityMonitor>();

    protected float accumulator;
    protected PhysicsEngine physics;

    protected Map<ServerThread, Entity> clients = new HashMap<ServerThread, Entity>();

    public static int ENTITY_ID = 1;

    @Override
    public void init() {
        Engine.init(Side.SERVER);
        Engine.bus.registerEvents(new ServerListener());
        CorePacketManager.register();
        server = new Server(this, Settings.ip, Settings.port);

        EngineGameVariables.sv_running.setValue(true);
        EngineGameCommands.init();
        scene = new GameMap(FileHelper.newGameFile("maps", level + ".asg")).getScene();
        Engine.scene = scene;

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

            for (Entity player : clients.values())
                player.updateFixed();

            if (scene != null) {
                scene.updateFixed();
                physics.update(scene);
            }
            Scheduler.tick();

            for (Entity player : clients.values()) {
                ServerInputComponent livingComponent = player.getComponent(ServerInputComponent.class);
                livingComponent.move(scene, getTerrainByPoint(player.getPosition()));
                checkForInteractions(livingComponent);

                player.getComponent(ServerThreadComponent.class).net.sendPacket(new PlayerPositionPacket(player.getPosition()));
            }

            if (monitor != null)
                monitor.check();

            for (EntityMonitor clientMonitor : clientMonitors.values())
                clientMonitor.check();
        }
        Profiler.end("Physics");

        Profiler.start("Scene Update");
        for (Entity player : clients.values())
            player.update();

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
        server.stop();
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
                System.out.println("Received auth packet.");

                ServerThread thread = (ServerThread) net;
                Entity player = new Entity(ServerInputComponent.class, ServerThreadComponent.class, EngineMeshComponent.class);
                player.getPosition().set(scene.getSpawn());
                player.getVelocity().set(0, 0, 0);

                player.setValue("collisionMesh", "player");
                player.setValue("model", "player");

                player.getComponent(ServerThreadComponent.class).net = thread;
                player.getComponent(ServerThreadComponent.class).user = new ServerUser("", thread, player);

                player.getComponent(PhysicsComponent.class).gravity = false;
                player.getComponent(PhysicsComponent.class).inverseMass = 0;

                player.spawn();

                // sync game variables
                boolean cheatsEnabled = EngineGameVariables.sv_cheats.getValueAsBool();
                net.sendPacket(new GameVariablePacket("sv_cheats", cheatsEnabled ? "1" : "0"));
                for (IGameVariable variable : CoreGameVariableManager.getVariables()) {
                    if (!cheatsEnabled && variable.getType() == GameVariableType.CHEAT)
                        continue;

                    if (variable.getType() == GameVariableType.INTERNAL)
                        continue;

                    net.sendPacket(new GameVariablePacket(variable.getName(), variable.getValueAsString()));
                }
                net.sendPacket(new LevelChangePacket(level));
                net.sendPacket(new EntityLinkPacket(player.getId(), -1));

                for (Entity entity : scene.getEntities())
                    if (entity.getLineIndex() == -1)
                        net.sendPacket(new EntitySpawnPacket(entity));
                    else
                        net.sendPacket(new EntityLinkPacket(entity.getId(), entity.getLineIndex()));
                for (Entity entity : clients.values())
                    net.sendPacket(new EntitySpawnPacket(entity));

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
            if (var.getType() == GameVariableType.INTERNAL)
                return;

            if (clients.get(net).getComponent(ServerThreadComponent.class).user.canSetVariable(var))
                var.setValue(packet.value);
        } else if (receivedPacket.getId() == CorePacketManager.RUN_COMMAND_PACKET) {
            GameCommandManager.run(clients.get(net).getComponent(ServerThreadComponent.class).user, ((RunCommandPacket) receivedPacket).command);
        }
    }

    @Override
    public void handleException(Exception e) {
        e.printStackTrace();
    }

    @Override
    public void handleDisconnect(BaseNetworkable net) {
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

    public void checkForInteractions(ServerInputComponent livingComponent) {
        if (livingComponent.playerMovementPacket != null && livingComponent.playerMovementPacket.interact) {
            for (Entity entity : scene.getEntities()) {
                IInteractable interactable = entity.getComponent(IInteractable.class);
                if (interactable == null)
                    continue;

                PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
                if (physics != null) {
                    Vector3f point = physics.raycast(livingComponent.getEyePosition(), livingComponent.getForwardVector());
                    if (point != null && Vector3f.sub(livingComponent.getEyePosition(), point, null).lengthSquared() <= EngineGameVariables.mp_reachDistance.getValueAsFloat())
                        interactable.interact();
                }
            }
        }
    }

}
