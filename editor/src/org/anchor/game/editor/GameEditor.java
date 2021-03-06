package org.anchor.game.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.Renderer;
import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.Shader;
import org.anchor.client.engine.renderer.bloom.Bloom;
import org.anchor.client.engine.renderer.clear.ClearColour;
import org.anchor.client.engine.renderer.debug.DebugRenderer;
import org.anchor.client.engine.renderer.deferred.DeferredShading;
import org.anchor.client.engine.renderer.fog.Fog;
import org.anchor.client.engine.renderer.font.Alignment;
import org.anchor.client.engine.renderer.font.FontRenderer;
import org.anchor.client.engine.renderer.font.Text;
import org.anchor.client.engine.renderer.font.TextBuilder;
import org.anchor.client.engine.renderer.fxaa.FXAA;
import org.anchor.client.engine.renderer.ibl.IBL;
import org.anchor.client.engine.renderer.keyboard.Binds;
import org.anchor.client.engine.renderer.keyboard.KeyboardUtils;
import org.anchor.client.engine.renderer.keyboard.RunCommandCallback;
import org.anchor.client.engine.renderer.shadows.Shadows;
import org.anchor.client.engine.renderer.types.cubemap.BakedCubemap;
import org.anchor.client.engine.renderer.types.ibl.LightProbe;
import org.anchor.client.engine.renderer.types.ibl.ReflectionProbe;
import org.anchor.client.engine.renderer.types.light.Light;
import org.anchor.client.engine.renderer.vignette.Vignette;
import org.anchor.client.engine.renderer.volumetrics.scattering.VolumetricScattering;
import org.anchor.engine.common.Log;
import org.anchor.engine.common.utils.AABB;
import org.anchor.engine.common.utils.Mathf;
import org.anchor.engine.common.vfs.VirtualFileSystem;
import org.anchor.engine.shared.Engine;
import org.anchor.engine.shared.components.IInteractable;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.components.PhysicsComponent;
import org.anchor.engine.shared.components.SpawnComponent;
import org.anchor.engine.shared.console.EngineGameCommands;
import org.anchor.engine.shared.console.EngineGameVariables;
import org.anchor.engine.shared.editor.TransformableObject;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.physics.PhysicsEngine;
import org.anchor.engine.shared.profiler.Profiler;
import org.anchor.engine.shared.scheduler.Scheduler;
import org.anchor.engine.shared.utils.TerrainRaycast;
import org.anchor.engine.shared.utils.TerrainUtils;
import org.anchor.game.client.ClientGameCommands;
import org.anchor.game.client.ClientGameVariables;
import org.anchor.game.client.GameClient;
import org.anchor.game.client.LocalUser;
import org.anchor.game.client.app.AppManager;
import org.anchor.game.client.app.Game;
import org.anchor.game.client.async.Requester;
import org.anchor.game.client.audio.Audio;
import org.anchor.game.client.components.LightComponent;
import org.anchor.game.client.components.LightProbeComponent;
import org.anchor.game.client.components.MeshComponent;
import org.anchor.game.client.components.ParticleSystemComponent;
import org.anchor.game.client.components.PostProcessVolumeComponent;
import org.anchor.game.client.components.ReflectionProbeComponent;
import org.anchor.game.client.components.SkyComponent;
import org.anchor.game.client.components.SunComponent;
import org.anchor.game.client.developer.debug.Debug;
import org.anchor.game.client.events.RedirectListener;
import org.anchor.game.client.loaders.AssetLoader;
import org.anchor.game.client.particles.ParticleRenderer;
import org.anchor.game.client.shaders.ForwardStaticShader;
import org.anchor.game.client.shaders.NormalShader;
import org.anchor.game.client.shaders.SkyShader;
import org.anchor.game.client.shaders.SkyTextureShader;
import org.anchor.game.client.shaders.StaticShader;
import org.anchor.game.client.shaders.WaterShader;
import org.anchor.game.client.storage.GameMap;
import org.anchor.game.client.types.ClientScene;
import org.anchor.game.client.types.ClientTerrain;
import org.anchor.game.client.types.TerrainTexture;
import org.anchor.game.client.utils.FrustumCull;
import org.anchor.game.client.utils.MouseUtils;
import org.anchor.game.editor.commands.Undo;
import org.anchor.game.editor.components.EditorInputComponent;
import org.anchor.game.editor.editableMesh.EditableMesh;
import org.anchor.game.editor.editableMesh.renderer.EditableMeshRenderer;
import org.anchor.game.editor.editableMesh.types.SelectionMode;
import org.anchor.game.editor.events.EditorListener;
import org.anchor.game.editor.gizmo.AABBRenderer;
import org.anchor.game.editor.gizmo.GizmoRenderer;
import org.anchor.game.editor.gizmo.TransformationMode;
import org.anchor.game.editor.ui.LevelEditor;
import org.anchor.game.editor.ui.Window;
import org.anchor.game.editor.utils.AcceleratorLink;
import org.anchor.game.editor.utils.MapWriter;
import org.anchor.game.editor.utils.TerrainCreate;
import org.anchor.game.editor.utils.raycast.EditorMousePicker;
import org.anchor.game.editor.utils.raycast.TransformableObjectRaycast;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL40;
import org.lwjgl.util.vector.Vector3f;

public class GameEditor extends Game {

    protected ClearColour clearColour;
    protected FXAA fxaa;
    protected Bloom bloom;
    protected VolumetricScattering volumetricScattering;
    protected Vignette vignette;

    protected float accumulator, snapAmount = 0.5f;
    protected int mode = 0; // select, translate, rotate, scale
    protected TransformationMode transformationMode = TransformationMode.WORLD;
    protected boolean game, modo;
    protected GizmoRenderer gizmo;
    protected AABBRenderer aabb;

    protected LightComponent lightComponent;
    protected PhysicsEngine physics;

    private LevelEditor editor;
    private EditorMousePicker picker;
    private Vector3f ray;
    private File level;

    private List<EditableMesh> editableMeshes = new ArrayList<EditableMesh>();
    private ClientScene originalScene;
    private Vector3f originalPlayerPosition = new Vector3f();
    private float originalPlayerPitch, originalPlayerYaw;
    private Text trianglesDrawn;

    private boolean renderEveryAABB;

    private int resizeWidth, resizeHeight;

    private List<TerrainCreate> terrains = new ArrayList<TerrainCreate>();

    public static final JFileChooser chooser = new JFileChooser(new File(new File("").getAbsolutePath()).getParent());
    private static final List<AcceleratorLink> links = new ArrayList<AcceleratorLink>();
    private static List<Entity> copies = new ArrayList<Entity>();

    static {
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Anchor Engine Map (*.asg)", "asg");
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);

        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    }

    public GameEditor(LevelEditor editor, File level) {
        this.editor = editor;
        this.level = level;

        Settings.lowerFPSFocus = false;
    }

    @Override
    public void init() {
        VirtualFileSystem.init();
        if (!("false".equals(System.getProperty("sun.java2d.d3d")) || "false".equals(System.getenv("J2D_D3D"))))
            for (int i = 0; i < 10; i++)
                Log.warning("Add -Dsun.java2d.d3d=false to your launch arguments or set the environment variable J2D_D3D to false in order to stop UI rendering bugs!");

        Engine.bus.registerEvents(new RedirectListener());
        Engine.bus.registerEvents(new EditorListener());
        Graphics.init();
        Audio.init();
        ParticleRenderer.init();

        Binds.getInstance().init(new RunCommandCallback() {

            @Override
            public void runCommand(String cmd) {
                GameClient.runCommand(cmd);
            }

        });

        clearColour = new ClearColour();
        fxaa = new FXAA();
        deferred = new DeferredShading();
        ibl = new IBL(deferred);
        fog = new Fog();
        bloom = new Bloom();
        volumetricScattering = new VolumetricScattering();
        vignette = new Vignette();

        Renderer.setCubeModel(AssetLoader.loadModel("editor/cube"));

        user = new LocalUser();
        player = new Entity(EditorInputComponent.class);
        player.setLineIndex(-1);
        player.setValue("collisionMesh", "player");
        livingComponent = player.getComponent(LivingComponent.class);
        livingComponent.gravity = false;
        player.spawn();

        physics = new PhysicsEngine();
        ClientGameVariables.init();
        EngineGameVariables.sv_cheats.setValue(true);
        ClientGameCommands.init();
        EngineGameCommands.init();
        GameClient.runCommand("exec autoexec");

        // reference shaders to load them in - only required in the editor because of
        // Swing's multithreading
        shadows = new Shadows(null); // shadows must be initialised to prevent NPEs in ForwardStaticShader
        SkyShader.getInstance();
        SkyTextureShader.getInstance();
        NormalShader.getInstance();
        WaterShader.getInstance();
        StaticShader.getInstance();
        ForwardStaticShader.getInstance();
        BakedCubemap.getShader("irradianceConvolution");
        BakedCubemap.getShader("prefilter");

        if (level == null)
            createScene();
        else
            loadMap(level);

        setModo(true);
        gizmo = new GizmoRenderer(this);
        aabb = new AABBRenderer();

        trianglesDrawn = new TextBuilder().position(1, 0.98f).align(Alignment.RIGHT).build();

        picker = new EditorMousePicker();
        Log.info("App initialised.");
    }

    @Override
    public void update() {
        if (resizeWidth > 0 && resizeHeight > 0) {
            resize(resizeWidth, resizeHeight);

            resizeWidth = 0;
            resizeHeight = 0;
        }
        Profiler.start("Game Update");
        Requester.perform();
        MouseUtils.update();
        KeyboardUtils.update();
        Binds.getInstance().update();

        editor.update();
        if (!game) {
            if (!Mouse.isGrabbed() && (Mouse.isButtonDown(1) || gizmo.isDragging()))
                Mouse.setGrabbed(true);

            if (Mouse.isGrabbed() && !(Mouse.isButtonDown(1) || gizmo.isDragging()))
                Mouse.setGrabbed(false);

            if (!Mouse.isGrabbed())
                for (AcceleratorLink link : links)
                    link.check();
        } else {
            if (!Mouse.isGrabbed())
                Mouse.setGrabbed(true);
        }

        for (TerrainCreate terrain : terrains) {
            Log.info("Creating terrain at " + terrain.x + ", " + terrain.z);
            scene.getTerrains().add(new ClientTerrain(terrain.x, terrain.z, terrain.heights, new TerrainTexture("blendmap", "default", "default", "default", "default")));
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
                Engine.scene = scene;

                scene.getTerrains().addAll(originalScene.getTerrains());
                scene.getLayers().clear();
                scene.getLayers().addAll(originalScene.getLayers());

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

                for (int i = 0; i < originalScene.getEntities().size(); i++) {
                    Entity entity = originalScene.getEntities().get(i);
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

                editor.unselectAll();
                editor.setSelectedTerrain(null);
                editor.updateList();
            } else {
                scene = originalScene;
                Engine.scene = scene;
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

            player.updateFixed();
            if (game) {
                scene.updateFixed();
                physics.update(scene);
            }
            livingComponent.move(scene, GameClient.getTerrainByPoint(player.getPosition()));
            Scheduler.tick();

            FrustumCull.update();
        }
        Profiler.end("Physics");

        Profiler.start("Scene Update");
        player.update();
        scene.update();

        {
            currentPostProcessVolume = null;
            float currentSize = Float.MAX_VALUE;
            Vector3f eye = livingComponent.getEyePosition();

            for (PostProcessVolumeComponent volume : scene.getComponents(PostProcessVolumeComponent.class)) {
                AABB aabb = volume.getAABB();
                if (aabb.inside(eye)) {
                    boolean water = volume.isWaterPostProcess();
                    if (water || aabb.getFurthest() < currentSize) {
                        currentPostProcessVolume = volume;
                        if (water)
                            break;

                        currentSize = aabb.getFurthest();
                    }
                }
            }

            if (currentPostProcessVolume != null) {
                Settings.clearR = Mathf.pow(currentPostProcessVolume.fogColour.x, 2.2f);
                Settings.clearG = Mathf.pow(currentPostProcessVolume.fogColour.y, 2.2f);
                Settings.clearB = Mathf.pow(currentPostProcessVolume.fogColour.z, 2.2f);
            } else {
                Settings.clearR = Settings.clearG = Settings.clearB = 0;
            }
        }
        Profiler.end("Scene Update");

        Profiler.start("Editor");
        if (!game) {
            ray = picker.getRay(Mouse.getX(), Mouse.getY());
            TerrainRaycast terrainRaycast = picker.getTerrain(ray);
            TransformableObjectRaycast transformableObjectRaycast = picker.getTransformableObject(ray);

            boolean ignore = gizmo.update(editor.getSelectedObjects(), ray);
            if (Mouse.isButtonDown(0)) {
                if (editor.isEditingTerrain()) {
                    if (isTerrainRaycast(terrainRaycast, transformableObjectRaycast)) {
                        ignore = true;

                        editor.editTerrain(terrainRaycast.getPoint());
                    }
                }
            }

            if (KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_BACK)) {
                List<TransformableObject> objects = new ArrayList<TransformableObject>(editor.getSelectedObjects());
                for (int i = 0; i < objects.size(); i++) {
                    TransformableObject object = objects.get(i);
                    if (object instanceof Entity)
                        ((Entity) object).destroy();
                    else if (object instanceof EditableMesh)
                        editableMeshes.remove(object);
                }

                editor.updateList();
                editor.unselectAll();
            }

            if (MouseUtils.canLeftClick() || MouseUtils.canRightClick() || MouseUtils.canMiddleClick()) {
                editor.hidePopupMenu();

                if ((MouseUtils.canLeftClick() || MouseUtils.canMiddleClick()) && !ignore) {
                    if (terrainRaycast == null && transformableObjectRaycast == null) {
                        editor.unselectAll();
                        editor.setSelectedTerrain(null);
                    } else {
                        if (!KeyboardUtils.isKeyDown(Keyboard.KEY_LMENU)) {
                            if (isTransformableObjectRaycast(terrainRaycast, transformableObjectRaycast)) {
                                if (!KeyboardUtils.isKeyDown(Keyboard.KEY_LCONTROL))
                                    editor.unselectAll();
                                editor.addToSelection(transformableObjectRaycast.getTransformableObject());
                            } else if (isTerrainRaycast(terrainRaycast, transformableObjectRaycast)) {
                                if (!editor.isEditingTerrain())
                                    editor.setSelectedTerrain((ClientTerrain) terrainRaycast.getTerrain());
                            }
                        } else if (editor.getSelectedObjects().size() > 0) {
                            Vector3f point = null;
                            if (isTransformableObjectRaycast(terrainRaycast, transformableObjectRaycast))
                                point = transformableObjectRaycast.getPoint();
                            else if (isTerrainRaycast(terrainRaycast, transformableObjectRaycast))
                                point = terrainRaycast.getPoint();

                            if (point != null) {
                                for (TransformableObject object : editor.getSelectedObjects())
                                    object.getPosition().set(point);

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
        if (shadows.start(livingComponent.getInverseViewMatrix(), livingComponent.getEyePosition(), livingComponent.pitch, livingComponent.yaw))
            scene.renderShadows(shadows);
        Profiler.end("Shadows (Full)");

        deferred.start();
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        clearColour.perform();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);

        if (ClientGameVariables.r_wireframe.getValueAsBool())
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

        scene.render();
        EditableMeshRenderer.render(editableMeshes);
        deferred.decals();
        scene.renderDecals(deferred.getOutputFBO().getDepthTexture());
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);

        Profiler.start("Deferred (Lighting)");
        deferred.stop(livingComponent.getViewMatrix(), livingComponent.getInverseViewMatrix(), getLights(), shadows);
        Profiler.end("Deferred (Lighting)");

        if (ClientGameVariables.r_wireframe.getValueAsBool())
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

        scene.renderBlending();
        ParticleRenderer.render(scene.getComponents(ParticleSystemComponent.class));

        deferred.water();
        scene.renderWater();

        if (!game) {
            if (renderEveryAABB) {
                for (Entity entity : scene.getEntities())
                    aabb.render(entity);
                for (EditableMesh mesh : editableMeshes)
                    aabb.render(mesh.getPosition(), mesh.getAABB());
            }

            if (editor.getSelectedObjects().size() > 0) {
                GL11.glDisable(GL11.GL_CULL_FACE);
                gizmo.render();
                GL11.glEnable(GL11.GL_CULL_FACE);
                if (!renderEveryAABB) {
                    for (TransformableObject object : editor.getSelectedObjects())
                        if (object instanceof Entity)
                            aabb.render((Entity) object);
                        else if (object instanceof EditableMesh && EditableMesh.editableMeshComponent.selectionMode != SelectionMode.VERTEX)
                            aabb.render(object.getPosition(), ((EditableMesh) object).getAABB());
                }

                for (TransformableObject object : editor.getSelectedObjects()) {
                    if (object instanceof Entity) {
                        Entity entity = (Entity) object;
                        if (entity.hasComponent(ReflectionProbeComponent.class))
                            DebugRenderer.box(livingComponent.getViewMatrix(), entity.getPosition(), new Vector3f(), entity.getScale(), new Vector3f(1, 0, 0));
                        else if (entity.hasComponent(LightProbeComponent.class))
                            DebugRenderer.box(livingComponent.getViewMatrix(), entity.getPosition(), new Vector3f(), entity.getScale(), new Vector3f(0, 1, 0));
                        else if (entity.hasComponent(PostProcessVolumeComponent.class))
                            DebugRenderer.box(livingComponent.getViewMatrix(), entity.getPosition(), new Vector3f(), entity.getScale(), new Vector3f(0, 0, 1));
                    }
                }

                for (TransformableObject object : editor.getSelectedObjects()) {
                    if (!(object instanceof Entity))
                        continue;

                    Entity entity = (Entity) object;
                    LightComponent component = entity.getComponent(LightComponent.class);
                    if (component != null) {
                        float radius = component.getRadius();
                        DebugRenderer.circle(livingComponent.getViewMatrix(), entity.getPosition(), new Vector3f(), new Vector3f(radius, radius, radius), new Vector3f(1, 1, 1));
                    }
                }
            }
        }
        Debug.render();

        List<ReflectionProbeComponent> reflectionProbeComponents = scene.getComponents(ReflectionProbeComponent.class);
        List<LightProbeComponent> lightProbeComponents = scene.getComponents(LightProbeComponent.class);

        FrustumCull.cull(reflectionProbeComponents);
        FrustumCull.cull(lightProbeComponents);

        List<ReflectionProbe> reflectionProbes = new ArrayList<ReflectionProbe>();
        List<LightProbe> lightProbes = new ArrayList<LightProbe>();
        for (int i = 0; i < reflectionProbeComponents.size(); i++)
            reflectionProbes.add(reflectionProbeComponents.get(i));
        for (int i = 0; i < lightProbeComponents.size(); i++)
            lightProbes.add(lightProbeComponents.get(i));

        ibl.perform(livingComponent.getViewMatrix(), livingComponent.getInverseViewMatrix(), sky.getIrradiance(), sky.getPrefilter(), reflectionProbes, lightProbes);

        deferred.fog();
        fog.perform(deferred.getOutputFBO().getColourTexture(), deferred.getOutputFBO().getDepthTexture(), currentPostProcessVolume, lightComponent, livingComponent.getViewMatrix());

        deferred.output();
        Profiler.end("Scene");

        Profiler.start("Post processing");
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        bloom.perform(deferred.getOutputFBO().getColourTexture(), deferred.getOtherFBO().getColourTexture(), deferred.getOutputFBO().getColourTexture());
        if (currentPostProcessVolume != null && currentPostProcessVolume.volumetric) {
            volumetricScattering.perform(bloom.getOutputFBO().getColourTexture(), deferred.getOutputFBO().getDepthTexture(), bloom.getExposureTexture(), deferred.getNormalFBO().getColourTexture(), getLights(), livingComponent.getViewMatrix(), shadows, currentPostProcessVolume.gScattering);
            fxaa.perform(volumetricScattering.getOutputFBO().getColourTexture());
        } else {
            fxaa.perform(bloom.getOutputFBO().getColourTexture());
        }
        vignette.perform(fxaa.getOutputFBO().getColourTexture());

        GL11.glEnable(GL11.GL_BLEND);
        GL40.glBlendFunci(0, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        trianglesDrawn.setText("Triangles Drawn: " + Renderer.triangleCount + " " + "\nDraw calls: " + Renderer.drawCalls);
        FontRenderer.render(trianglesDrawn);

        GL11.glDisable(GL11.GL_BLEND);
        Profiler.end("Post processing");

        Profiler.frameEnd();
        Graphics.frameEnd();
    }

    @Override
    public void shutdown() {
        if (shadows != null)
            shadows.shutdown();
        fxaa.shutdown();
        deferred.shutdown();
        ibl.shutdown();
        bloom.shutdown();
        volumetricScattering.shutdown();

        for (Shader shader : Shader.getShaders())
            shader.shutdown();

        Requester.shutdown();
        Audio.shutdown();
        Loader.getInstance().shutdown();
    }

    @Override
    public void resize(int width, int height) {
        Log.info("Resizing to " + width + "x" + height);

        Settings.width = width;
        Settings.height = height;
        Renderer.refreshProjectionMatrix();
        GL11.glViewport(0, 0, width, height);
    }

    @Override
    public String getTitle() {
        return "Anchor Editor";
    }

    public void createScene() {
        editor.unselectAll();

        Entity light = new Entity(LightComponent.class, SunComponent.class);
        light.setValue("name", "Sun");
        lightComponent = light.getComponent(LightComponent.class);
        lightComponent.colour.set(6, 6, 6);
        lightComponent.attenuation.set(1, 0, 0);
        lightComponent.volumetricStrength = 0.25f;
        light.getRotation().set(109, 23, 0);
        light.spawn();
        shadows = new Shadows(lightComponent);

        Entity skydome = new Entity(MeshComponent.class, SkyComponent.class);
        skydome.setValue("model", "core/skydome");
        skydome.spawn();

        skydome.setHidden(true);
        skydome.getComponent(MeshComponent.class).shader = SkyShader.getInstance();
        skydome.getComponent(MeshComponent.class).material.setBlendingEnabled(true);
        sky = skydome.getComponent(SkyComponent.class);
        sky.setLight(light);

        scene = new ClientScene();
        Engine.scene = scene;
        editableMeshes.clear();

        scene.getEntities().add(skydome);
        scene.getEntities().add(light);

        checkForEditableMeshes();

        editor.updateList();
    }

    public void loadMap(File map) {
        editor.unselectAll();
        if (!map.exists())
            return;

        Window.getInstance().setTabName(map.getName());
        this.level = map;
        Undo.clearHistory();

        scene = new GameMap(map).getScene();
        Engine.scene = scene;
        editableMeshes.clear();
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

        checkForEditableMeshes();

        editor.updateList();
        Log.info("Loaded " + map.getAbsolutePath());

        if (lightComponent == null) {
            lightComponent = new LightComponent();
            lightComponent.attenuation.set(0, 0, 0.0000001f);
            lightComponent.colour.set(0, 0, 0);
        }
    }

    public Entity addEntity() {
        return addEntity(true);
    }

    public Entity addEntity(boolean select) {
        Entity entity = new Entity();
        entity.precache();
        entity.spawn();

        scene.getEntities().add(entity);
        editor.updateList();

        if (select) {
            editor.unselectAll();
            editor.addToSelection(entity);
        }

        Undo.registerEntity(entity);

        return entity;
    }

    public void addEntity(Entity entity) {
        recursiveAddChildrenEntity(entity);
        editor.updateList();

        Undo.registerEntity(entity);
        editor.unselectAll();
        editor.addToSelection(entity);
    }

    public void removeEntity(Entity entity) {
        if (entity == null)
            return;

        entity.destroy();
        editor.updateList();
    }

    public void addEditableMesh(EditableMesh mesh) {
        editableMeshes.add(mesh);
        editor.updateList();
    }

    public void removeEditableMesh(EditableMesh mesh) {
        if (mesh == null)
            return;

        editableMeshes.remove(mesh);
        editor.updateList();
    }

    private void recursiveAddChildrenEntity(Entity entity) {
        scene.getEntities().add(entity);
        for (Entity child : entity.getChildren())
            recursiveAddChildrenEntity(child);
    }

    private void checkForEditableMeshes() {
        for (Entity entity : scene.getEntities()) {
            if (entity.containsKey("AEeditableMesh") && entity.getValue("AEeditableMesh").equals("true")) {
                try {
                    if (!entity.getComponent(MeshComponent.class).model.isLoaded())
                        Log.info("Model for editable mesh " + entity.getValue("model") + " isn't loaded! Waiting...");

                    while (!entity.getComponent(MeshComponent.class).model.isLoaded()) {
                        Requester.perform();
                        Thread.sleep(20L);
                    }
                } catch (Exception e) {

                }

                editableMeshes.add(new EditableMesh(entity));
                entity.destroy();
            }
        }
    }

    public void addTerrain(String heightmap, int x, int z) {
        addTerrain(TerrainUtils.loadHeightsFromHeightmap(heightmap), x, z);
    }

    public void addTerrain(float[][] heights, int x, int z) {
        terrains.add(new TerrainCreate(heights, x, z));
    }

    public void removeTerrain(ClientTerrain terrain) {
        scene.getTerrains().remove(terrain);
        editor.updateList();
    }

    public void queueResize(int width, int height) {
        this.resizeWidth = width;
        this.resizeHeight = height;
    }

    public void refreshComponenetValues() {
        editor.refreshComponentValues();
    }

    public ClientScene getScene() {
        return scene;
    }

    public List<EditableMesh> getEditableMeshes() {
        return editableMeshes;
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
        editor.refreshTransformationXYZ();
    }

    public boolean isModo() {
        return modo;
    }

    public void setModo(boolean modo) {
        this.modo = modo;

        if (gizmo != null)
            gizmo.recreate();
    }

    public boolean shouldShowAllAABBs() {
        return renderEveryAABB;
    }

    public void setShowAllAABBs(boolean show) {
        this.renderEveryAABB = show;
    }

    public GizmoRenderer getGizmoRenderer() {
        return gizmo;
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

    public static void copy() {
        if (LevelEditor.getInstance() == null)
            return;

        copies.clear();
        for (TransformableObject object : LevelEditor.getInstance().getSelectedObjects()) {
            if (!(object instanceof Entity))
                continue;
            Entity entity = (Entity) object;
            Entity copy = entity.copy();

            copies.add(copy);
            MeshComponent render = copy.getComponent(MeshComponent.class);
            if (render != null)
                render.colour.set(0, 0, 0, 0);
        }
    }

    public static void paste() {
        for (Entity copy : copies) {
            getInstance().scene.getEntities().add(copy.copy());
            LevelEditor.getInstance().updateUI();
        }
    }

    public static void create() {
        if (getInstance() == null) {
            ClientGameVariables.init();
            EngineGameVariables.sv_cheats.setValue(true);
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
            getInstance().level = null;
            Undo.clearHistory();

            getInstance().createScene();
        }
    }

    public static void open(File file) {
        if (getInstance() == null) {
            if (!file.exists()) {
                if (file.getName().indexOf('.') == -1) {
                    File other = new File(file.getAbsoluteFile() + ".asg");
                    if (other.exists())
                        file = other;
                } else {
                    JOptionPane.showMessageDialog(null, "File not found!", "Error", JOptionPane.OK_OPTION);
                    return;
                }
            }

            ClientGameVariables.init();
            EngineGameVariables.sv_cheats.setValue(true);
            LevelEditor editor = new LevelEditor();
            Window.getInstance().addTab(GameEditor.chooser.getSelectedFile().getName(), editor);

            final File f = file; // Java is annoying
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Display.setParent(editor.getCanvas());
                    } catch (LWJGLException e) {
                        e.printStackTrace();
                    }

                    GameEditor game = new GameEditor(editor, f);
                    editor.setGameEditor(game);
                    AppManager.create(game);
                }

            }).start();
        } else {
            if (Undo.hasChangedSinceSave()) {
                if (JOptionPane.showConfirmDialog(null, "Do you want to save your changes to " + Window.getInstance().getTabName() + "?", "Anchor Editor", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION)
                    save();
                else
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
            Undo.saved();

            MapWriter.write(getInstance().level, getInstance().scene);
            Log.info("Saved to " + getInstance().level.getAbsolutePath());
        }
    }

    public static void saveAs(File file) {
        if (!file.getName().endsWith(".asg"))
            file = new File(file.getAbsolutePath() + ".asg");
        Undo.saved();

        MapWriter.write(file, getInstance().scene);
        getInstance().level = file;

        Window.getInstance().setTabName(file.getName());
        Log.info("Saved to " + getInstance().level.getAbsolutePath());
    }

    public void checkForInteractions(boolean interacting) {
        if (interacting) {
            for (Entity entity : scene.getEntities()) {
                IInteractable interactable = entity.getComponent(IInteractable.class);
                if (interactable == null)
                    continue;

                MeshComponent mesh = entity.getComponent(MeshComponent.class);
                PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);

                if (mesh != null && physics == null) {
                    AABB aabb = mesh.getAABB();
                    if (aabb != null) {
                        Vector3f point = aabb.raycast(livingComponent.getEyePosition(), livingComponent.getForwardVector());
                        if (point != null && Vector3f.sub(livingComponent.getEyePosition(), point, null).lengthSquared() <= EngineGameVariables.mp_reachDistance.getValueAsFloat())
                            interactable.interact();
                    }
                } else if (physics != null) {
                    Vector3f point = physics.raycast(livingComponent.getEyePosition(), livingComponent.getForwardVector());
                    if (point != null && Vector3f.sub(livingComponent.getEyePosition(), point, null).lengthSquared() <= EngineGameVariables.mp_reachDistance.getValueAsFloat())
                        interactable.interact();
                }
            }
        }
    }

    public boolean isTransformableObjectRaycast(TerrainRaycast terrainRaycast, TransformableObjectRaycast transformableObjectRaycast) {
        if (terrainRaycast == null) {
            return transformableObjectRaycast != null;
        } else if (transformableObjectRaycast == null) {
            return terrainRaycast == null;
        } else {
            if (transformableObjectRaycast.getDistance() < terrainRaycast.getDistance())
                return true;
            else
                return false;
        }
    }

    public boolean isTerrainRaycast(TerrainRaycast terrainRaycast, TransformableObjectRaycast transformableObjectRaycast) {
        if (transformableObjectRaycast == null) {
            return terrainRaycast != null;
        } else if (terrainRaycast == null) {
            return transformableObjectRaycast == null;
        } else {
            if (terrainRaycast.getDistance() < transformableObjectRaycast.getDistance())
                return true;
            else
                return false;
        }
    }

    public static void registerAccelerator(JMenuItem item, int key, int mask) {
        links.add(new AcceleratorLink(item, key, mask));
    }

    public LevelEditor getLevelEditor() {
        return editor;
    }

    public static boolean isInGame() {
        return ((GameEditor) AppManager.getInstance()).game;
    }

}
