package org.anchor.game.client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.Renderer;
import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.bloom.Bloom;
import org.anchor.client.engine.renderer.deferred.DeferredShading;
import org.anchor.client.engine.renderer.font.Alignment;
import org.anchor.client.engine.renderer.font.Font;
import org.anchor.client.engine.renderer.font.FontRenderer;
import org.anchor.client.engine.renderer.font.Text;
import org.anchor.client.engine.renderer.fxaa.FXAA;
import org.anchor.client.engine.renderer.godrays.Godrays;
import org.anchor.client.engine.renderer.gui.GUI;
import org.anchor.client.engine.renderer.gui.GUIRenderer;
import org.anchor.client.engine.renderer.shadows.Shadows;
import org.anchor.client.engine.renderer.types.Framebuffer;
import org.anchor.client.engine.renderer.types.light.Light;
import org.anchor.client.engine.renderer.vignette.Vignette;
import org.anchor.engine.common.net.BaseNetworkable;
import org.anchor.engine.common.net.client.Client;
import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.common.net.packet.IPacketHandler;
import org.anchor.engine.common.utils.FileHelper;
import org.anchor.engine.shared.Engine;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.components.SpawnComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.net.CorePacketManager;
import org.anchor.engine.shared.net.packet.AuthenticationPacket;
import org.anchor.engine.shared.net.packet.EntityAddComponentPacket;
import org.anchor.engine.shared.net.packet.EntityComponentVariableChangePacket;
import org.anchor.engine.shared.net.packet.EntityKeyValuePacket;
import org.anchor.engine.shared.net.packet.EntityLinkPacket;
import org.anchor.engine.shared.net.packet.EntityRemoveComponentPacket;
import org.anchor.engine.shared.net.packet.EntityRemovePacket;
import org.anchor.engine.shared.net.packet.EntitySpawnPacket;
import org.anchor.engine.shared.net.packet.LevelChangePacket;
import org.anchor.engine.shared.net.packet.PlayerMovementPacket;
import org.anchor.engine.shared.net.packet.PlayerPositionPacket;
import org.anchor.engine.shared.physics.PhysicsEngine;
import org.anchor.engine.shared.profiler.Profiler;
import org.anchor.engine.shared.scheduler.IRunnable;
import org.anchor.engine.shared.scheduler.Scheduler;
import org.anchor.engine.shared.terrain.Terrain;
import org.anchor.engine.shared.utils.Side;
import org.anchor.engine.shared.weapon.Gun;
import org.anchor.engine.shared.weapon.Weapon;
import org.anchor.game.client.app.AppManager;
import org.anchor.game.client.app.Game;
import org.anchor.game.client.async.Requester;
import org.anchor.game.client.audio.Audio;
import org.anchor.game.client.components.ClientInputComponent;
import org.anchor.game.client.components.DecalComponent;
import org.anchor.game.client.components.LightComponent;
import org.anchor.game.client.components.MeshComponent;
import org.anchor.game.client.components.SkyComponent;
import org.anchor.game.client.components.SunComponent;
import org.anchor.game.client.loaders.AssetLoader;
import org.anchor.game.client.shaders.ViewmodelShader;
import org.anchor.game.client.storage.GameMap;
import org.anchor.game.client.types.ClientScene;
import org.anchor.game.client.types.ClientTerrain;
import org.anchor.game.client.utils.FrustumCull;
import org.anchor.game.client.utils.KeyboardUtils;
import org.anchor.game.client.utils.MouseUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class GameClient extends Game implements IPacketHandler {

    protected FXAA fxaa;
    protected Bloom bloom;
    protected Godrays godrays;
    protected Vignette vignette;

    private Entity gun;
    protected PhysicsEngine physics;
    protected float accumulator;

    protected LightComponent lightComponent;

    protected List<Text> texts;
    protected List<GUI> guis;
    protected Vector3f spawn;

    protected Text gpuTime, cpuTime, yetAnother, crosshair, ammo, reserveAmmo;
    protected boolean loaded;

    protected Client client;
    protected Font font;

    @Override
    public void init() {
        Engine.init(Side.CLIENT);
        Graphics.init();
        Audio.init();
        System.out.println("APP INIT");

        sceneFBO = new Framebuffer(Display.getWidth(), Display.getHeight(), Framebuffer.NONE);
        fxaa = new FXAA();
        deferred = new DeferredShading();
        bloom = new Bloom();
        godrays = new Godrays();
        vignette = new Vignette();

        Renderer.setCubeModel(AssetLoader.loadModel("editor/cube"));

        physics = new PhysicsEngine();

        player = new Entity(ClientInputComponent.class);
        player.setValue("collisionMesh", "player");
        livingComponent = player.getComponent(LivingComponent.class);
        player.getPosition().set(450, 0, 450);
        player.spawn();

        gun = new Entity(MeshComponent.class);
        gun.setValue("model", "deagle");
        gun.getPosition().set(0.2f, -0.5f, -0.1f);
        gun.getRotation().set(0, 0, 0);
        gun.getScale().set(0.075f, 0.075f, 0.075f);
        gun.spawn();
        gun.getComponent(MeshComponent.class).disableFrustumCulling = true;
        gun.getComponent(MeshComponent.class).shader = ViewmodelShader.getInstance();

        texts = new ArrayList<Text>();
        font = new Font("trebuchet");
        texts.add(gpuTime = new Text(new Vector2f(-0.985f, -0.92f), "GPU: 0 ms", font, 1));
        texts.add(cpuTime = new Text(new Vector2f(-0.985f, -0.97f), "CPU: 0 ms", font, 1));
        yetAnother = new Text(new Vector2f(), "YET ANOTHER", font, 6, new Vector3f(1, 1, 1), Alignment.CENTER);
        texts.add(crosshair = new Text(new Vector2f(), "+", font, 1.3f, new Vector3f(), Alignment.CENTER));

        texts.add(ammo = new Text(new Vector2f(0.945f, -0.9265f), "", font, 2, new Vector3f(0, 0, 0), Alignment.RIGHT));
        texts.add(reserveAmmo = new Text(new Vector2f(0.995f, -0.9375f), "", font, 1.5f, new Vector3f(0, 0, 0), Alignment.RIGHT));

        guis = new ArrayList<GUI>();

        CorePacketManager.register();
        client = new Client(this);
        client.connect("localhost", 24964);
    }

    @Override
    public void update() {
        Profiler.start("Game Update");
        Requester.perform();
        MouseUtils.update();
        KeyboardUtils.update();
        client.handle();

        if (!Mouse.isGrabbed())
            Mouse.setGrabbed(true);

        boolean interacting = KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_E);
        Profiler.start("Physics");
        accumulator += AppManager.getFrameTimeSeconds();
        while (accumulator >= PhysicsEngine.TICK_DELAY) {
            accumulator -= PhysicsEngine.TICK_DELAY;

            if (accumulator > 0.2)
                accumulator = 0;

            livingComponent.move(scene, getTerrainByPoint(player.getPosition()));
            if (scene != null) {
                scene.updateFixed();
                physics.update(scene);
            }
            Scheduler.tick();

            FrustumCull.update();
            client.sendPacket(new PlayerMovementPacket(KeyboardUtils.isKeyDown(Keyboard.KEY_W), KeyboardUtils.isKeyDown(Keyboard.KEY_A), KeyboardUtils.isKeyDown(Keyboard.KEY_S), KeyboardUtils.isKeyDown(Keyboard.KEY_D), player.getComponent(ClientInputComponent.class).space, KeyboardUtils.isKeyDown(Keyboard.KEY_LSHIFT), interacting, livingComponent.fire, livingComponent.reload, livingComponent.getSelectedIndex(), livingComponent.pitch, livingComponent.yaw));

            if (player.getPosition().y < -15)
                livingComponent.health = 0;

            if (livingComponent.health <= 0) {
                livingComponent.health = 100;
                player.getVelocity().set(0, 0, 0);
                player.getPosition().set(spawn);
                texts.add(yetAnother);
                texts.remove(crosshair);

                Scheduler.schedule(new IRunnable() {

                    @Override
                    public void tick(float time, float percentage) {
                        yetAnother.setAlpha(Math.min(percentage * 6, 1));
                    }

                    @Override
                    public void finish() {
                        texts.remove(yetAnother);
                        texts.add(crosshair);
                    }

                }, 2);
            }
        }

        checkForInteractions(interacting);
        Profiler.end("Physics");

        Profiler.start("Scene Update");
        player.update();
        gun.update();
        if (scene != null)
            scene.update();
        Profiler.end("Scene Update");

        gpuTime.setText(String.format("GPU: %.02f", AppManager.gpuTime) + " ms");
        cpuTime.setText(String.format("CPU: %.02f", AppManager.cpuTime) + " ms");

        Weapon weapon = livingComponent.getSelectedWeapon();
        if (weapon instanceof Gun) {
            Gun gun = (Gun) weapon;

            ammo.setText(gun.getAmmo() + "");
            reserveAmmo.setText(gun.getReserveAmmo() + "");
        } else {
            ammo.setText("");
            reserveAmmo.setText("");
        }

        if (KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_ESCAPE)) {
            shutdown();
            System.exit(0);
        }
        loaded = scene != null ? scene.isLoaded() : false;

        Settings.wireframe = false;
        if (Mouse.isButtonDown(2))
            Settings.wireframe = true;
        Profiler.end("Game Update");
    }

    @Override
    public void render() {
        if (scene == null)
            return;

        Profiler.start("Scene");
        Profiler.start("Shadows (Full)");
        if (shadows != null && shadows.start(livingComponent.getInverseViewMatrix(), livingComponent.getEyePosition(), livingComponent.pitch, livingComponent.yaw)) {
            scene.renderShadows(shadows);
            shadows.stop();
        }
        Profiler.end("Shadows (Full)");

        deferred.start();
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
        GL11.glClearColor(0, 0, 0, 0);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        if (Settings.wireframe)
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

        scene.render();
        GL11.glDepthRange(0, 0.99f);
        ClientScene.renderEntity(gun);
        GL11.glDepthRange(0, 1);

        deferred.decals();
        scene.renderDecals(deferred.getOutputFBO().getDepthTexture());
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);

        Profiler.start("Deferred (Lighting)");
        deferred.stop(sceneFBO, livingComponent.getViewMatrix(), livingComponent.getInverseViewMatrix(), getLights(), sky.baseColour, sky.topColour, sky.getSkybox(), sky.getIrradiance(), sky.getPrefilter(), shadows);
        Profiler.end("Deferred (Lighting)");

        if (Settings.wireframe)
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

        scene.renderBlending();
        deferred.reflective();
        scene.renderReflective();

        deferred.output();
        Profiler.end("Scene");
        sceneFBO.bindFramebuffer();
        GUIRenderer.perform(deferred.getOutputFBO().getColourTexture());
        sceneFBO.unbindFramebuffer();

        Profiler.start("Post processing");
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        fxaa.perform(deferred.getOutputFBO().getColourTexture());
        bloom.perform(fxaa.getOutputFBO().getColourTexture(), deferred.getBloomFBO().getColourTexture());
        godrays.perform(bloom.getOutputFBO().getColourTexture(), deferred.getGodraysFBO().getColourTexture(), bloom.getExposureTexture(), livingComponent.getViewMatrix(), lightComponent);
        vignette.perform(godrays.getOutputFBO().getColourTexture());

        if (!loaded)
            GUIRenderer.perform(0);
        Profiler.end("Post processing");

        Profiler.start("GUI");
        GUIRenderer.render(guis);
        FontRenderer.render(texts);
        Profiler.end("GUI");

        Profiler.frameEnd();
        Graphics.frameEnd();
    }

    @Override
    public void shutdown() {
        if (shadows != null)
            shadows.shutdown();
        fxaa.shutdown();
        deferred.shutdown();
        bloom.shutdown();
        godrays.shutdown();
        vignette.shutdown();

        client.shutdown();
        Requester.shutdown();
        Audio.shutdown();
        Loader.getInstance().shutdown();
    }

    @Override
    public void resize(int width, int height) {
        deferred.shutdown();
        bloom.shutdown();
        godrays.shutdown();
        vignette.shutdown();

        deferred = new DeferredShading();
        bloom = new Bloom();
        godrays = new Godrays();
        vignette = new Vignette();

        Settings.width = width;
        Settings.height = height;
        Renderer.refreshProjectionMatrix();
        GL11.glViewport(0, 0, width, height);
    }

    @Override
    public void connect(BaseNetworkable net) {
        net.sendPacket(new AuthenticationPacket(Engine.PROTOCOL_VERSION));
    }

    @Override
    public void handlePacket(BaseNetworkable net, IPacket receivedPacket) {
        if (receivedPacket.getId() == CorePacketManager.LEVEL_CHANGE_PACKET) {
            loadMap(FileHelper.newGameFile("maps", ((LevelChangePacket) receivedPacket).level + ".asg"));
        } else if (receivedPacket.getId() == CorePacketManager.PLAYER_POSITION_PACKET) {
            player.getPosition().set(((PlayerPositionPacket) receivedPacket).position);
        }

        if (receivedPacket.getId() == CorePacketManager.ENTITY_SPAWN_PACKET) {
            scene.getEntities().add(((EntitySpawnPacket) receivedPacket).entity);
        } else if (receivedPacket.getId() == CorePacketManager.ENTITY_ADD_COMPONENT_PACKET) {
            EntityAddComponentPacket packet = (EntityAddComponentPacket) receivedPacket;

            getNetworkedEntityFromId(packet.id).addComponent(packet.component);
        } else if (receivedPacket.getId() == CorePacketManager.ENTITY_COMPONENT_VARIABLE_CHANGE_PACKET) {
            EntityComponentVariableChangePacket packet = (EntityComponentVariableChangePacket) receivedPacket;

            try {
                packet.field.set(getNetworkedEntityFromId(packet.id).getComponent(packet.clazz), packet.value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (receivedPacket.getId() == CorePacketManager.ENTITY_REMOVE_PACKET) {
            EntityRemoveComponentPacket packet = (EntityRemoveComponentPacket) receivedPacket;

            getNetworkedEntityFromId(packet.id).removeComponent(packet.clazz);
        } else if (receivedPacket.getId() == CorePacketManager.ENTITY_KEY_VALUE_PACKET) {
            EntityKeyValuePacket packet = (EntityKeyValuePacket) receivedPacket;

            getNetworkedEntityFromId(packet.id).setValue(packet.key, packet.value);
        } else if (receivedPacket.getId() == CorePacketManager.ENTITY_REMOVE_PACKET) {
            scene.getEntities().remove(getNetworkedEntityFromId(((EntityRemovePacket) receivedPacket).id));
        } else if (receivedPacket.getId() == CorePacketManager.ENTITY_LINK_PACKET) {
            EntityLinkPacket packet = (EntityLinkPacket) receivedPacket;

            for (Entity entity : scene.getEntities()) {
                if (entity.getLineIndex() == packet.lineIndex) {
                    entity.setId(packet.id);
                    break;
                }
            }
        }
    }

    @Override
    public void disconnect(BaseNetworkable net) {

    }

    public Entity getNetworkedEntityFromId(int id) {
        for (Entity entity : scene.getEntities())
            if (entity.getId() == id)
                return entity;

        return null;
    }

    @Override
    public String getTitle() {
        return "Yet Another";
    }

    public static List<Light> getSceneLights() {
        return ((Game) AppManager.getInstance()).getLights();
    }

    public static Entity getPlayer() {
        return ((Game) AppManager.getInstance()).player;
    }

    public static SkyComponent getSky() {
        return ((Game) AppManager.getInstance()).sky;
    }

    public static Framebuffer getSceneFramebuffer() {
        return ((Game) AppManager.getInstance()).deferred.getOutputFBO();
    }

    public static int getAmbientOcclusionTexture() {
        return ((Game) AppManager.getInstance()).deferred.getAmbientOcclusionTexture();
    }

    public static int getBRDF() {
        return ((Game) AppManager.getInstance()).deferred.getBRDF();
    }

    public static int getShadowMap(int map) {
        return ((Game) AppManager.getInstance()).shadows.getPCFShadowMap(map);
    }

    public static Matrix4f getToShadowMapSpaceMatrix(int map) {
        return ((Game) AppManager.getInstance()).shadows.getToShadowMapSpaceMatrix(map);
    }

    public static float getShadowExtents(int map) {
        return ((Game) AppManager.getInstance()).shadows.getExtents(map);
    }

    public static Terrain getTerrainByPoint(Vector3f point) {
        ClientScene scene = ((Game) AppManager.getInstance()).scene;
        if (scene == null)
            return null;

        for (Terrain terrain : scene.getTerrains()) {
            if (point.x >= terrain.getX() && point.x <= terrain.getX() + terrain.getSize() && point.z >= terrain.getZ() && point.z <= terrain.getZ() + terrain.getSize())
                return terrain;
        }

        return null;
    }

    public void loadMap(File map) {
        if (scene != null) {
            for (Entity entity : scene.getEntitiesWithComponent(MeshComponent.class)) {
                entity.getComponent(MeshComponent.class).model.unload();
                AssetLoader.removeModel(entity.getComponent(MeshComponent.class).model);
            }

            for (Terrain terrain : scene.getTerrains())
                ((ClientTerrain) terrain).unload();
        }

        scene = new GameMap(map).getScene();
        for (Entity entity : scene.getEntitiesWithComponent(SunComponent.class)) {
            List<Entity> skies = scene.getEntitiesWithComponent(SkyComponent.class);
            if (skies.size() > 0) {
                this.sky = skies.get(0).getComponent(SkyComponent.class);
                this.sky.setLight(entity);
            }

            lightComponent = entity.getComponent(LightComponent.class);
            shadows = new Shadows(lightComponent);
        }

        for (Entity entity : scene.getEntitiesWithComponent(SpawnComponent.class))
            spawn = entity.getPosition();
        player.getPosition().set(spawn);
        loaded = false;

        Entity decal = new Entity(DecalComponent.class);
        decal.setValue("texture", "editor/cube_diffuse_3");
        decal.getPosition().set(4.5f, 7, 16f);
        decal.getScale().set(2, 2, 2);
        decal.spawn();
        scene.getEntities().add(decal);
    }

    public static void main(String[] args) {
        AppManager.create(new GameClient());
    }

}
