package org.anchor.game.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.Renderer;
import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.bloom.Bloom;
import org.anchor.client.engine.renderer.debug.DebugRenderer;
import org.anchor.client.engine.renderer.deferred.DeferredShading;
import org.anchor.client.engine.renderer.fxaa.FXAA;
import org.anchor.client.engine.renderer.godrays.Godrays;
import org.anchor.client.engine.renderer.gui.GUIRenderer;
import org.anchor.client.engine.renderer.shadows.Shadows;
import org.anchor.client.engine.renderer.types.Framebuffer;
import org.anchor.client.engine.renderer.types.cubemap.BakedCubemap;
import org.anchor.client.engine.renderer.types.light.Light;
import org.anchor.client.engine.renderer.vignette.Vignette;
import org.anchor.engine.common.utils.FileHelper;
import org.anchor.engine.shared.Engine;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.components.SpawnComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.physics.PhysicsEngine;
import org.anchor.engine.shared.profiler.Profiler;
import org.anchor.engine.shared.scheduler.Scheduler;
import org.anchor.engine.shared.utils.Side;
import org.anchor.game.client.GameClient;
import org.anchor.game.client.app.AppManager;
import org.anchor.game.client.app.Game;
import org.anchor.game.client.async.Requester;
import org.anchor.game.client.audio.Audio;
import org.anchor.game.client.components.LightComponent;
import org.anchor.game.client.components.MeshComponent;
import org.anchor.game.client.components.SkyComponent;
import org.anchor.game.client.components.SunComponent;
import org.anchor.game.client.loaders.AssetLoader;
import org.anchor.game.client.shaders.ForwardStaticShader;
import org.anchor.game.client.shaders.NormalShader;
import org.anchor.game.client.shaders.SkyShader;
import org.anchor.game.client.shaders.StaticShader;
import org.anchor.game.client.shaders.WaterShader;
import org.anchor.game.client.storage.GameMap;
import org.anchor.game.client.types.ClientScene;
import org.anchor.game.client.types.ClientTerrain;
import org.anchor.game.client.types.TerrainTexture;
import org.anchor.game.client.utils.EntityRaycast;
import org.anchor.game.client.utils.FrustumCull;
import org.anchor.game.client.utils.KeyboardUtils;
import org.anchor.game.client.utils.MousePicker;
import org.anchor.game.client.utils.MouseUtils;
import org.anchor.game.client.utils.TerrainRaycast;
import org.anchor.game.editor.components.EditorInputComponent;
import org.anchor.game.editor.gizmo.GizmoRenderer;
import org.anchor.game.editor.ui.LevelEditor;
import org.anchor.game.editor.ui.Window;
import org.anchor.game.editor.utils.MapWriter;
import org.anchor.game.editor.utils.TerrainCreate;
import org.anchor.game.editor.utils.TransformationMode;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

public class GameEditor extends Game {

    protected FXAA fxaa;
    protected Bloom bloom;
    protected Godrays godrays;
    protected Vignette vignette;

    protected float accumulator, snapAmount = 0.5f;
    protected int mode = 0; // select, translate, rotate, scale
    protected TransformationMode transformationMode = TransformationMode.LOCAL;
    protected boolean game;
    protected GizmoRenderer gizmo;

    protected LightComponent lightComponent;
    protected PhysicsEngine physics;

    private LevelEditor editor;
    private MousePicker picker;
    private Vector3f ray;
    private File level;

    private ClientScene originalScene;
    private Vector3f originalPlayerPosition = new Vector3f();
    private float originalPlayerPitch, originalPlayerYaw;

    private boolean modifiedSinceLastSave;

    private List<TerrainCreate> terrains = new ArrayList<TerrainCreate>();

    public static final JFileChooser chooser = new JFileChooser();

    static {
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Anchor Engine Map (*.asg)", "asg");
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);

        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    }

    public GameEditor(LevelEditor editor, File level) {
        this.editor = editor;
        this.level = level;
    }

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

        player = new Entity(EditorInputComponent.class);
        player.setValue("collisionMesh", "player");
        livingComponent = player.getComponent(LivingComponent.class);
        livingComponent.noPhysicsSpeed = 2;
        livingComponent.gravity = false;
        player.spawn();

        physics = new PhysicsEngine();

        // reference shaders to load them in - only required in the editor
        shadows = new Shadows(null); // WaterShader requires shadows to be initialised
        NormalShader.getInstance();
        StaticShader.getInstance();
        WaterShader.getInstance();
        ForwardStaticShader.getInstance();
        BakedCubemap.getShader("irradianceConvolution");
        BakedCubemap.getShader("prefilter");

        if (level == null)
            createScene();
        else
            loadMap(level);

        gizmo = new GizmoRenderer(this);
        picker = new MousePicker();
    }

    @Override
    public void update() {
        Profiler.start("Game Update");
        Requester.perform();
        MouseUtils.update();
        KeyboardUtils.update();

        editor.update();
        if (!game) {
            if (!Mouse.isGrabbed() && Mouse.isButtonDown(1))
                Mouse.setGrabbed(true);

            if (Mouse.isGrabbed() && !Mouse.isButtonDown(1))
                Mouse.setGrabbed(false);
        } else {
            if (!Mouse.isGrabbed())
                Mouse.setGrabbed(true);
        }

        for (TerrainCreate terrain : terrains) {
            System.out.println("Creating terrain at " + terrain.x + ", " + terrain.z);
            scene.getTerrains().add(new ClientTerrain(terrain.x, terrain.z, terrain.heightmap, new TerrainTexture("blendmap", "default", "default", "default", "default")));
        }

        if (terrains.size() > 0)
            editor.updateList();

        terrains.clear();

        if (KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_G)) {
            game = !game;
            livingComponent.gravity = game;

            if (game) {
                originalScene = scene;
                scene = new ClientScene();
                scene.getTerrains().addAll(originalScene.getTerrains());

                Vector3f backupPosition = new Vector3f(player.getPosition());
                float backupPitch = livingComponent.pitch;
                float backupYaw = livingComponent.yaw;

                List<Entity> spawn = originalScene.getEntitiesWithComponent(SpawnComponent.class);
                if (spawn.size() > 0)
                    player.getPosition().set(spawn.get(0).getPosition());
                else
                    player.getPosition().set(0, 0, 0);
                player.getVelocity().set(0, 0, 0);

                livingComponent.pitch = originalPlayerPitch;
                livingComponent.yaw = originalPlayerYaw;

                originalPlayerPosition = backupPosition;
                originalPlayerPitch = backupPitch;
                originalPlayerYaw = backupYaw;

                for (Entity entity : originalScene.getEntities()) {
                    if (entity.getParent() != null)
                        continue;

                    Entity parent = entity.copy();
                    scene.getEntities().add(parent);

                    for (Entity child : entity.getChildren()) {
                        Entity copy = child.copy();
                        copy.setParent(parent);

                        scene.getEntities().add(copy);
                    }
                }

                editor.setSelectedEntity(null);
                editor.setSelectedTerrain(null);
                editor.updateList();
            } else {
                scene = originalScene;
                editor.updateList();

                float backupPitch = livingComponent.pitch;
                float backupYaw = livingComponent.yaw;

                player.getPosition().set(originalPlayerPosition);
                livingComponent.pitch = originalPlayerPitch;
                livingComponent.yaw = originalPlayerYaw;

                originalPlayerPitch = backupPitch;
                originalPlayerYaw = backupYaw;
            }
        }

        Profiler.start("Physics");
        accumulator += AppManager.getFrameTimeSeconds();
        while (accumulator >= PhysicsEngine.TICK_DELAY) {
            accumulator -= PhysicsEngine.TICK_DELAY;

            if (accumulator > 0.2)
                accumulator = 0;

            livingComponent.move(scene, GameClient.getTerrainByPoint(player.getPosition()));
            Scheduler.tick();
            if (game) {
                scene.updateFixed();
                physics.update(scene);
            }

            FrustumCull.update();
        }
        Profiler.end("Physics");

        Profiler.start("Scene Update");
        player.update();
        scene.update();
        Profiler.end("Scene Update");

        Profiler.start("Editor");
        if (!game) {
            ray = picker.getRay(Mouse.getX(), Mouse.getY());
            TerrainRaycast terrainRaycast = picker.getTerrain(ray);
            EntityRaycast entityRaycast = picker.getEntity(scene, ray);

            boolean ignore = false;
            if (editor.getSelectedEntity() != null)
                ignore = gizmo.update(editor.getSelectedEntity(), ray);

            if (Mouse.isButtonDown(0)) {
                if (editor.isEditingTerrain()) {
                    if (isTerrainRaycast(terrainRaycast, entityRaycast)) {
                        ignore = true;

                        editor.editTerrain(terrainRaycast.getPoint());
                    }
                }
            }

            if (MouseUtils.canLeftClick() || MouseUtils.canRightClick() || MouseUtils.canMiddleClick()) {
                editor.hidePopupMenu();

                if ((MouseUtils.canLeftClick() || MouseUtils.canMiddleClick()) && !ignore) {
                    if (terrainRaycast == null && entityRaycast == null) {
                        editor.setSelectedEntity(null);
                        editor.setSelectedTerrain(null);
                    } else {
                        if (!KeyboardUtils.isKeyDown(Keyboard.KEY_LMENU)) {
                            if (isEntityRaycast(terrainRaycast, entityRaycast)) {
                                editor.setSelectedEntity(entityRaycast.getEntity());
                            } else if (isTerrainRaycast(terrainRaycast, entityRaycast)) {
                                if (!editor.isEditingTerrain())
                                    editor.setSelectedTerrain((ClientTerrain) terrainRaycast.getTerrain());
                            }
                        } else if (editor.getSelectedEntity() != null) {
                            Vector3f point = null;
                            if (isEntityRaycast(terrainRaycast, entityRaycast))
                                point = entityRaycast.getPoint();
                            else if (isTerrainRaycast(terrainRaycast, entityRaycast))
                                point = terrainRaycast.getPoint();

                            if (point != null) {
                                editor.getSelectedEntity().getPosition().set(point);
                                editor.refreshComponentValues();
                            }
                        }

                        if (MouseUtils.canMiddleClick())
                            editor.showPopupMenu();
                    }
                }
            }

            if (!Mouse.isGrabbed()) {
                if (KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_Q))
                    mode = 0;
                else if (KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_W))
                    mode = 1;
                else if (KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_E))
                    mode = 2;
                else if (KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_R))
                    mode = 3;
            }
        } else {
            checkForInteractions(KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_E));
        }
        Profiler.end("Editor");
        Profiler.end("Game Update");
    }

    @Override
    public void render() {
        Profiler.start("Scene");
        Profiler.start("Shadows (Full)");
        if (shadows.start(livingComponent.getInverseViewMatrix(), livingComponent.getEyePosition(), livingComponent.pitch, livingComponent.yaw)) {
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

        if (!game && editor.getSelectedEntity() != null) {
            gizmo.render();

            LightComponent component = editor.getSelectedEntity().getComponent(LightComponent.class);
            if (component != null) {
                float radius = component.getRadius();
                DebugRenderer.circle(livingComponent.getViewMatrix(), editor.getSelectedEntity().getPosition(), new Vector3f(), new Vector3f(radius, radius, radius), new Vector3f(1, 1, 1));
            }
        }

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
        Profiler.end("Post processing");

        Profiler.frameEnd();
        Graphics.frameEnd();
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
        return "Anchor Engine Editor";
    }

    public void createScene() {
        Entity light = new Entity(LightComponent.class, SunComponent.class);
        light.setValue("name", "Sun");
        lightComponent = light.getComponent(LightComponent.class);
        lightComponent.attenuation.set(1, 0, 0);
        light.getRotation().set(109, 23, 0);
        light.spawn();
        shadows = new Shadows(lightComponent);

        Entity skydome = new Entity(MeshComponent.class, SkyComponent.class);
        skydome.setValue("model", "core/skybox");
        skydome.spawn();

        skydome.setHidden(true);
        skydome.getComponent(MeshComponent.class).shader = SkyShader.getInstance();
        skydome.getComponent(MeshComponent.class).model.getTexture().setBlendingEnabled(true);
        sky = skydome.getComponent(SkyComponent.class);
        sky.setLight(light);

        scene = new ClientScene();
        scene.getEntities().add(skydome);
        scene.getEntities().add(light);
        editor.updateList();
    }

    public void loadMap(File map) {
        this.level = map;

        scene = new GameMap(map).getScene();
        for (Entity entity : scene.getEntitiesWithComponent(SkyComponent.class))
            sky = entity.getComponent(SkyComponent.class);

        for (Entity entity : scene.getEntitiesWithComponent(SunComponent.class)) {
            sky.setLight(entity);
            lightComponent = entity.getComponent(LightComponent.class);
            shadows = new Shadows(lightComponent);
        }

        for (Entity entity : scene.getEntitiesWithComponent(SpawnComponent.class))
            player.getPosition().set(entity.getPosition());

        for (Entity entity : scene.getEntities())
            if (entity.hasComponent(SkyComponent.class))
                entity.setHidden(true);

        editor.updateList();
        System.out.println("Loaded " + map.getAbsolutePath());
    }

    public Entity addEntity() {
        return addEntity(true);
    }

    public Entity addEntity(boolean select) {
        Entity entity = new Entity();
        entity.spawn();

        scene.getEntities().add(entity);
        editor.updateList();

        if (select)
            editor.setSelectedEntity(entity);

        return entity;
    }

    public void addEntity(Entity entity) {
        scene.getEntities().add(entity);
        editor.updateList();

        editor.setSelectedEntity(entity);
    }

    public void removeEntity(Entity entity) {
        scene.getEntities().remove(entity);
        editor.updateList();
    }

    public void addTerrain(String heightmap, int x, int z) {
        terrains.add(new TerrainCreate(heightmap, x, z));
    }

    public void removeTerrain(ClientTerrain terrain) {
        scene.getTerrains().remove(terrain);
        editor.updateList();
    }

    public void refreshComponenetValues() {
        editor.refreshComponentValues();
    }

    public ClientScene getScene() {
        return scene;
    }

    public float getSnapAmount() {
        return snapAmount;
    }

    public void setSnapAmount(float snapAmount) {
        this.snapAmount = snapAmount;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public TransformationMode getTransformationMode() {
        return transformationMode;
    }

    public void setTransformationMode(TransformationMode transformationMode) {
        this.transformationMode = transformationMode;
    }

    public static List<Light> getSceneLights() {
        return ((Game) AppManager.getInstance()).getLights();
    }

    public static GameEditor getInstance() {
        return (GameEditor) AppManager.getInstance();
    }

    public static void create() {
        if (getInstance() == null) {
            LevelEditor editor = new LevelEditor();
            Window.getInstance().addTab("Untitled", editor);
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Display.setParent(editor.getCanvas());
                    } catch (LWJGLException e) {
                        e.printStackTrace();
                    }

                    GameEditor game = new GameEditor(editor, null);
                    editor.setGameEditor(game);
                    AppManager.create(game);
                }

            }).start();
        } else {
            Window.getInstance().setTabName("Untitled");
            getInstance().createScene();
        }
    }

    public static void open(File file) {
        if (getInstance() == null) {
            LevelEditor editor = new LevelEditor();
            Window.getInstance().addTab(GameEditor.chooser.getSelectedFile().getName(), editor);
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Display.setParent(editor.getCanvas());
                    } catch (LWJGLException e) {
                        e.printStackTrace();
                    }

                    GameEditor game = new GameEditor(editor, file);
                    editor.setGameEditor(game);
                    AppManager.create(game);
                }

            }).start();
        } else {
            if (getInstance().modifiedSinceLastSave) {
                if (JOptionPane.showConfirmDialog(null, "If you continue, your unsaved changes will be lost!", "Are you sure you want to continue?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                    getInstance().loadMap(file);
            } else {
                getInstance().loadMap(file);
            }
        }
    }

    public static void save() {
        if (getInstance().level == null) {
            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
                saveAs(chooser.getSelectedFile());
        } else {
            FileHelper.write(getInstance().level, MapWriter.write(getInstance().scene));
            getInstance().modifiedSinceLastSave = false;
            System.out.println("Saved to " + getInstance().level.getAbsolutePath());
        }
    }

    public static void saveAs(File file) {
        if (!file.getName().endsWith(".asg"))
            file = new File(file.getAbsolutePath() + ".asg");

        FileHelper.write(file, MapWriter.write(getInstance().scene));
        getInstance().modifiedSinceLastSave = false;
        getInstance().level = file;

        Window.getInstance().setTabName(file.getName());
        System.out.println("Saved to " + getInstance().level.getAbsolutePath());
    }

    public boolean isEntityRaycast(TerrainRaycast terrainRaycast, EntityRaycast entityRaycast) {
        if (terrainRaycast == null) {
            return entityRaycast != null;
        } else if (entityRaycast == null) {
            return terrainRaycast == null;
        } else {
            if (entityRaycast.getDistance() < terrainRaycast.getDistance())
                return true;
            else
                return false;
        }
    }

    public boolean isTerrainRaycast(TerrainRaycast terrainRaycast, EntityRaycast entityRaycast) {
        if (entityRaycast == null) {
            return terrainRaycast != null;
        } else if (terrainRaycast == null) {
            return entityRaycast == null;
        } else {
            if (terrainRaycast.getDistance() < entityRaycast.getDistance())
                return true;
            else
                return false;
        }
    }

    public LevelEditor getLevelEditor() {
        return editor;
    }

    public static boolean isInGame() {
        return ((GameEditor) AppManager.getInstance()).game;
    }

}
