package org.anchor.game.client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.anchor.client.engine.renderer.Engine;
import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.Renderer;
import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.bloom.Bloom;
import org.anchor.client.engine.renderer.deferred.DeferredShading;
import org.anchor.client.engine.renderer.font.Font;
import org.anchor.client.engine.renderer.font.FontRenderer;
import org.anchor.client.engine.renderer.font.Text;
import org.anchor.client.engine.renderer.fxaa.FXAA;
import org.anchor.client.engine.renderer.godrays.Godrays;
import org.anchor.client.engine.renderer.gui.GUI;
import org.anchor.client.engine.renderer.gui.GUIRenderer;
import org.anchor.client.engine.renderer.shadows.Shadows;
import org.anchor.client.engine.renderer.types.Framebuffer;
import org.anchor.client.engine.renderer.types.Light;
import org.anchor.client.engine.renderer.vignette.Vignette;
import org.anchor.engine.common.net.BaseNetworkable;
import org.anchor.engine.common.net.client.Client;
import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.common.net.packet.IPacketHandler;
import org.anchor.engine.common.utils.FileHelper;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.components.SpawnComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.net.CorePacketManager;
import org.anchor.engine.shared.net.packet.LevelChangePacket;
import org.anchor.engine.shared.net.packet.PlayerMovementPacket;
import org.anchor.engine.shared.physics.PhysicsEngine;
import org.anchor.engine.shared.scheduler.IRunnable;
import org.anchor.engine.shared.scheduler.Scheduler;
import org.anchor.engine.shared.terrain.Terrain;
import org.anchor.game.client.app.AppManager;
import org.anchor.game.client.app.Game;
import org.anchor.game.client.async.Requester;
import org.anchor.game.client.audio.Audio;
import org.anchor.game.client.components.ClientInputComponent;
import org.anchor.game.client.components.LightComponent;
import org.anchor.game.client.components.MeshComponent;
import org.anchor.game.client.components.SkyComponent;
import org.anchor.game.client.loaders.ModelLoader;
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
    protected float accumulator;

    protected LightComponent lightComponent;

    protected List<Text> texts;
    protected List<GUI> guis;
    protected Vector3f spawn;

    protected Text gpuTime, cpuTime, yetAnother, crosshair;
    protected boolean loaded;

    protected Client client;
    protected Font font;

    @Override
    public void init() {
        Engine.init();
        Audio.init();
        System.out.println("APP INIT");

        sceneFBO = new Framebuffer(Display.getWidth(), Display.getHeight(), Framebuffer.NONE);
        fxaa = new FXAA();
        deferred = new DeferredShading();
        bloom = new Bloom();
        godrays = new Godrays();
        vignette = new Vignette();

        player = new Entity(ClientInputComponent.class);
        player.setValue("collisionMesh", "player");
        livingComponent = player.getComponent(LivingComponent.class);
        livingComponent.gravity = true;
        livingComponent.noPhysicsSpeed = 50;
        livingComponent.selectedSpeed = 15;
        player.getPosition().set(450, 0, 450);
        player.spawn();

        gun = new Entity(MeshComponent.class);
        gun.setValue("model", "deagle");
        gun.getPosition().set(5, 0, 0);
        gun.spawn();
        gun.getComponent(MeshComponent.class).disableFrustumCulling = true;
        gun.getComponent(MeshComponent.class).shader = ViewmodelShader.getInstance();

        texts = new ArrayList<Text>();
        font = new Font("trebuchet");
        texts.add(gpuTime = new Text(new Vector2f(-0.985f, -0.92f), "GPU: 0 ms", font, 1));
        texts.add(cpuTime = new Text(new Vector2f(-0.985f, -0.97f), "CPU: 0 ms", font, 1));
        yetAnother = new Text(new Vector2f(), "YET ANOTHER", font, 6, new Vector3f(1, 1, 1), true);
        texts.add(crosshair = new Text(new Vector2f(), "+", font, 1.3f, new Vector3f(), true));

        guis = new ArrayList<GUI>();

        CorePacketManager.register();
        client = new Client(this);
        client.connect("localhost", 24964);
    }

    @Override
    public void update() {
        Requester.perform();
        MouseUtils.update();
        KeyboardUtils.update();
        client.handle();    

        if (!Mouse.isGrabbed())
            Mouse.setGrabbed(true);

        accumulator += AppManager.getFrameTimeSeconds();
        while (accumulator >= PhysicsEngine.TICK_DELAY) {
            accumulator -= PhysicsEngine.TICK_DELAY;

            if (accumulator > 0.2)
                accumulator = 0;

            livingComponent.move(scene, getTerrainByPoint(player.getPosition()));
            if (scene != null)
                scene.updateFixed();
            Scheduler.tick();

            FrustumCull.update();
            client.sendPacket(new PlayerMovementPacket(KeyboardUtils.isKeyDown(Keyboard.KEY_W), KeyboardUtils.isKeyDown(Keyboard.KEY_A), KeyboardUtils.isKeyDown(Keyboard.KEY_S), KeyboardUtils.isKeyDown(Keyboard.KEY_D), livingComponent.pitch, livingComponent.yaw));
        }

        checkForInteractions();

        gun.getPosition().set(0.2f, -0.5f, -0.1f);
        gun.getRotation().set(0, 0, 0);
        gun.getScale().set(0.075f, 0.075f, 0.075f);
        player.update();
        gun.update();
        if (scene != null)
            scene.update();

        gpuTime.setText(String.format("GPU: %.02f", AppManager.gpuTime) + " ms");
        cpuTime.setText(String.format("CPU: %.02f", AppManager.cpuTime) + " ms");
        if (KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_ESCAPE)) {
            shutdown();
            System.exit(0);
        }

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
                    yetAnother.setAlpha(Math.min(percentage * 3, 1));
                }

                @Override
                public void finish() {
                    texts.remove(yetAnother);
                    texts.add(crosshair);
                }

            }, 1);
        }
        loaded = scene != null ? scene.isLoaded() : false;

        Settings.wireframe = false;
        if (Mouse.isButtonDown(2))
            Settings.wireframe = true;
    }

    @Override
    public void render() {
        if (scene == null)
            return;

        if (shadows != null && shadows.start(livingComponent.getInverseViewMatrix(), livingComponent.getEyePosition(), livingComponent.pitch, livingComponent.yaw)) {
            scene.renderShadows(shadows);
            shadows.stop();
        }

        deferred.start();
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
        GL11.glClearColor(0, 0, 0, 0);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        if (Settings.wireframe)
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

        scene.render();
        GL11.glDepthRange(0, 0.99f);
        // ClientScene.renderEntity(gun);
        GL11.glDepthRange(0, 1);

        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        deferred.stop(sceneFBO, livingComponent.getViewMatrix(), livingComponent.getInverseViewMatrix(), getLights(), sky.baseColour, sky.topColour, sky.getSkybox(), sky.getIrradiance(), sky.getPrefilter(), shadows);

        if (Settings.wireframe)
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

        scene.renderBlending();
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);

        deferred.reflective();

        if (Settings.wireframe)
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

        scene.renderReflective();
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);

        deferred.output();
        sceneFBO.bindFramebuffer();
        GUIRenderer.perform(deferred.getOutputFBO().getColourTexture());
        sceneFBO.unbindFramebuffer();

        fxaa.perform(deferred.getOutputFBO().getColourTexture());
        bloom.perform(fxaa.getOutputFBO().getColourTexture(), deferred.getBloomFBO().getColourTexture());
        godrays.perform(bloom.getOutputFBO().getColourTexture(), deferred.getGodraysFBO().getColourTexture(), bloom.getExposureTexture(), livingComponent.getViewMatrix(), lightComponent);
        vignette.perform(godrays.getOutputFBO().getColourTexture());

        if (!loaded)
            GUIRenderer.perform(0);
        GUIRenderer.render(guis);
        FontRenderer.render(texts);
        Engine.frameEnd();
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

    }

    @Override
    public void handlePacket(BaseNetworkable net, IPacket packet) {
        if (packet.getId() == CorePacketManager.LEVEL_CHANGE_PACKET)
            loadMap(FileHelper.newGameFile("maps", ((LevelChangePacket) packet).level + ".asg"));
    }

    @Override
    public void disconnect(BaseNetworkable net) {

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
                ModelLoader.removeModel(entity.getComponent(MeshComponent.class).model);
            }

            for (Terrain terrain : scene.getTerrains())
                ((ClientTerrain) terrain).unload();
        }

        scene = new GameMap(map).getScene();
        List<Entity> lights = scene.getEntitiesWithComponent(LightComponent.class);
        if (lights.size() > 0) {
            List<Entity> skies = scene.getEntitiesWithComponent(SkyComponent.class);
            if (skies.size() > 0) {
                this.sky = skies.get(0).getComponent(SkyComponent.class);
                this.sky.setLight(lights.get(0));
            }

            lightComponent = lights.get(0).getComponent(LightComponent.class);
            shadows = new Shadows(lightComponent);
        }

        for (Entity entity : scene.getEntitiesWithComponent(SpawnComponent.class))
            spawn = entity.getPosition();
        player.getPosition().set(spawn);
        loaded = false;
    }

    public static void main(String[] args) {
        AppManager.create(new GameClient());
    }

}
