package org.anchor.game.client;

import java.io.File;
import java.util.List;

import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.bloom.Bloom;
import org.anchor.client.engine.renderer.deferred.DeferredShading;
import org.anchor.client.engine.renderer.fxaa.FXAA;
import org.anchor.client.engine.renderer.godrays.Godrays;
import org.anchor.client.engine.renderer.shadows.Shadows;
import org.anchor.client.engine.renderer.types.Framebuffer;
import org.anchor.client.engine.renderer.types.Light;
import org.anchor.client.engine.renderer.vignette.Vignette;
import org.anchor.engine.common.utils.FileHelper;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.components.PhysicsComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.physics.PhysicsEngine;
import org.anchor.engine.shared.terrain.Terrain;
import org.anchor.game.client.app.AppManager;
import org.anchor.game.client.app.Game;
import org.anchor.game.client.async.Requester;
import org.anchor.game.client.audio.Audio;
import org.anchor.game.client.components.ClientInputComponent;
import org.anchor.game.client.components.LightComponent;
import org.anchor.game.client.components.MeshComponent;
import org.anchor.game.client.components.SkyComponent;
import org.anchor.game.client.components.WaterComponent;
import org.anchor.game.client.storage.GameMap;
import org.anchor.game.client.utils.FrustumCull;
import org.anchor.game.client.utils.KeyboardUtils;
import org.anchor.game.client.utils.MouseUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class GameClient extends Game {

    protected FXAA fxaa;
    protected Bloom bloom;
    protected Godrays godrays;
    protected Vignette vignette;

    protected float accumulator;
    protected boolean wireframe;

    protected LightComponent lightComponent;
    protected LivingComponent livingComponent;

    @Override
    public void init() {
        Audio.init();
        System.out.println("APP INIT");

        fxaa = new FXAA();
        deferred = new DeferredShading();
        bloom = new Bloom();
        godrays = new Godrays();
        vignette = new Vignette();

        player = new Entity(ClientInputComponent.class);
        player.setValue("collisionMesh", "player");
        livingComponent = player.getComponent(LivingComponent.class);
        livingComponent.gravity = true;
        player.getPosition().set(60, 0, 60);
        player.spawn();

        loadMap(FileHelper.newGameFile("maps", "test.asg"));

        Entity water = new Entity(MeshComponent.class, PhysicsComponent.class, WaterComponent.class);
        water.setValue("model", "editor/cube");
        water.setValue("collisionMesh", "aabb");
        water.getPosition().set(55, 0, 55);
        water.getScale().set(75, 1, 75);
        water.spawn();
        water.getComponent(PhysicsComponent.class).gravity = false;
        scene.getEntities().add(water);
    }

    @Override
    public void update() {
        Requester.perform();
        MouseUtils.update();
        KeyboardUtils.update();

        if (!Mouse.isGrabbed())
            Mouse.setGrabbed(true);

        accumulator += AppManager.getFrameTimeSeconds();
        while (accumulator >= PhysicsEngine.TICK_DELAY) {
            accumulator -= PhysicsEngine.TICK_DELAY;

            if (accumulator > 0.2)
                accumulator = 0;

            livingComponent.move(scene, getTerrainByPoint(player.getPosition()));
            scene.updateFixed();
            FrustumCull.update();
        }
        scene.update();

        if (KeyboardUtils.isKeyPressed(Keyboard.KEY_ESCAPE)) {
            shutdown();
            System.exit(0);
        }

        wireframe = false;
        if (Mouse.isButtonDown(2))
            wireframe = true;
    }

    @Override
    public void render() {
        if (shadows.start(livingComponent.getEyePosition(), player.getComponent(LivingComponent.class).pitch, player.getRotation().y)) {
            scene.renderShadows(shadows);
            shadows.stop();
        }

        deferred.start();
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
        GL11.glClearColor(0, 0, 0, 0);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        if (wireframe)
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

        scene.render();
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        deferred.stop(livingComponent.getViewMatrix(), livingComponent.getInverseViewMatrix(), getLights(), sky.baseColour, shadows);

        if (wireframe)
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

        scene.renderBlending();
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);

        deferred.reflective();
        scene.renderReflective();

        deferred.output();
        fxaa.perform(deferred.getOutputFBO().getColourTexture());
        bloom.perform(fxaa.getOutputFBO().getColourTexture(), deferred.getBloomFBO().getColourTexture());
        godrays.perform(bloom.getOutputFBO().getColourTexture(), deferred.getGodraysFBO().getColourTexture(), player.getComponent(LivingComponent.class).getViewMatrix(), lightComponent);
        vignette.perform(godrays.getOutputFBO().getColourTexture());
    }

    @Override
    public void shutdown() {
        shadows.shutdown();
        deferred.shutdown();
        bloom.shutdown();
        godrays.shutdown();
        vignette.shutdown();

        Requester.shutdown();
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

        GL11.glViewport(0, 0, width, height);
    }

    @Override
    public String getTitle() {
        return "GameClient";
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

    public static int getShadowMap() {
        return ((Game) AppManager.getInstance()).shadows.getPCFShadowMap();
    }

    public static Matrix4f getToShadowMapSpaceMatrix() {
        return ((Game) AppManager.getInstance()).shadows.getToShadowMapSpaceMatrix();
    }

    public static Terrain getTerrainByPoint(Vector3f point) {
        int gridX = (int) Math.floor(point.x / Terrain.SIZE);
        int gridZ = (int) Math.floor(point.z / Terrain.SIZE);

        for (Terrain terrain : ((Game) AppManager.getInstance()).scene.getTerrains())
            if (gridX == terrain.getGridX() && gridZ == terrain.getGridZ())
                return terrain;

        return null;
    }

    public void loadMap(File map) {
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
    }

    public static void main(String[] args) {
        AppManager.create(new GameClient());
    }

}
