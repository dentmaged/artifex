package org.anchor.game.client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.UIManager;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.Renderer;
import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.bloom.Bloom;
import org.anchor.client.engine.renderer.clear.ClearColour;
import org.anchor.client.engine.renderer.deferred.DeferredShading;
import org.anchor.client.engine.renderer.fog.Fog;
import org.anchor.client.engine.renderer.font.Alignment;
import org.anchor.client.engine.renderer.font.FontRenderer;
import org.anchor.client.engine.renderer.font.Text;
import org.anchor.client.engine.renderer.font.TextBuilder;
import org.anchor.client.engine.renderer.fxaa.FXAA;
import org.anchor.client.engine.renderer.gui.GUI;
import org.anchor.client.engine.renderer.gui.GUIRenderer;
import org.anchor.client.engine.renderer.ibl.IBL;
import org.anchor.client.engine.renderer.keyboard.Binds;
import org.anchor.client.engine.renderer.keyboard.KeyboardUtils;
import org.anchor.client.engine.renderer.keyboard.Keys;
import org.anchor.client.engine.renderer.keyboard.RunCommandCallback;
import org.anchor.client.engine.renderer.menu.Menu;
import org.anchor.client.engine.renderer.menu.MenuItem;
import org.anchor.client.engine.renderer.shadows.Shadows;
import org.anchor.client.engine.renderer.types.Framebuffer;
import org.anchor.client.engine.renderer.types.ibl.LightProbe;
import org.anchor.client.engine.renderer.types.ibl.ReflectionProbe;
import org.anchor.client.engine.renderer.types.light.Light;
import org.anchor.client.engine.renderer.vignette.Vignette;
import org.anchor.client.engine.renderer.volumetrics.scattering.VolumetricScattering;
import org.anchor.engine.common.Log;
import org.anchor.engine.common.net.BaseNetworkable;
import org.anchor.engine.common.net.client.Client;
import org.anchor.engine.common.net.packet.IPacket;
import org.anchor.engine.common.net.packet.IPacketHandler;
import org.anchor.engine.common.utils.AABB;
import org.anchor.engine.common.utils.FileHelper;
import org.anchor.engine.common.utils.Mathf;
import org.anchor.engine.common.utils.StringUtils;
import org.anchor.engine.common.vfs.VirtualFileSystem;
import org.anchor.engine.shared.Engine;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.console.EngineGameCommands;
import org.anchor.engine.shared.console.GameCommand;
import org.anchor.engine.shared.console.GameCommandManager;
import org.anchor.engine.shared.console.GameVariable;
import org.anchor.engine.shared.console.GameVariableManager;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.net.CorePacketManager;
import org.anchor.engine.shared.net.IUser;
import org.anchor.engine.shared.net.packet.AuthenticationPacket;
import org.anchor.engine.shared.net.packet.EntityAddComponentPacket;
import org.anchor.engine.shared.net.packet.EntityComponentVariableChangePacket;
import org.anchor.engine.shared.net.packet.EntityDestroyPacket;
import org.anchor.engine.shared.net.packet.EntityKeyValuePacket;
import org.anchor.engine.shared.net.packet.EntityLinkPacket;
import org.anchor.engine.shared.net.packet.EntityRemoveComponentPacket;
import org.anchor.engine.shared.net.packet.EntitySpawnPacket;
import org.anchor.engine.shared.net.packet.GameVariablePacket;
import org.anchor.engine.shared.net.packet.LevelChangePacket;
import org.anchor.engine.shared.net.packet.PlayerMovementPacket;
import org.anchor.engine.shared.net.packet.PlayerPositionPacket;
import org.anchor.engine.shared.net.packet.RunCommandPacket;
import org.anchor.engine.shared.net.packet.SendMessagePacket;
import org.anchor.engine.shared.physics.PhysicsEngine;
import org.anchor.engine.shared.profiler.Profiler;
import org.anchor.engine.shared.scheduler.ScheduledRunnable;
import org.anchor.engine.shared.scheduler.Scheduler;
import org.anchor.engine.shared.terrain.Terrain;
import org.anchor.engine.shared.weapon.Gun;
import org.anchor.engine.shared.weapon.Weapon;
import org.anchor.game.client.app.AppManager;
import org.anchor.game.client.app.Game;
import org.anchor.game.client.async.Requester;
import org.anchor.game.client.audio.Audio;
import org.anchor.game.client.components.ClientInputComponent;
import org.anchor.game.client.components.LightComponent;
import org.anchor.game.client.components.LightProbeComponent;
import org.anchor.game.client.components.MeshComponent;
import org.anchor.game.client.components.ParticleSystemComponent;
import org.anchor.game.client.components.PostProcessVolumeComponent;
import org.anchor.game.client.components.ReflectionProbeComponent;
import org.anchor.game.client.components.SkyComponent;
import org.anchor.game.client.components.SunComponent;
import org.anchor.game.client.developer.ConsoleRenderer;
import org.anchor.game.client.developer.LogRenderer;
import org.anchor.game.client.developer.ProfilerRenderer;
import org.anchor.game.client.developer.debug.Debug;
import org.anchor.game.client.events.RedirectListener;
import org.anchor.game.client.loaders.AssetLoader;
import org.anchor.game.client.particles.ParticleRenderer;
import org.anchor.game.client.shaders.ViewmodelShader;
import org.anchor.game.client.storage.GameMap;
import org.anchor.game.client.types.ClientScene;
import org.anchor.game.client.types.ClientTerrain;
import org.anchor.game.client.utils.FrustumCull;
import org.anchor.game.client.utils.MouseUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL40;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class GameClient extends Game implements IPacketHandler {

    protected ClearColour clearColour;
    protected FXAA fxaa;
    protected Bloom bloom;
    protected VolumetricScattering volumetricScattering;
    protected Vignette vignette;

    private Entity gun;
    protected PhysicsEngine physics;
    protected float accumulator;

    protected LightComponent lightComponent;

    protected List<Text> texts;
    protected List<GUI> guis;

    protected Text gpuTime, cpuTime, dead, crosshair, health, ammo, reserveAmmo, pressAnyKey, playerPosition, playerRotation;
    protected boolean loaded;
    protected State state;

    protected Menu mainMenu, optionsMenu, pauseMenu, previousMenu;
    protected boolean console;

    protected String currentLevel, mainMenuPath = FileHelper.newGameFile("maps", "main-menu.asg").getAbsolutePath();

    public static Thread mainThread;

    @Override
    public void init() {
        mainThread = Thread.currentThread();
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        VirtualFileSystem.init();
        ClientGameVariables.init(); // should already be initialised by GameStart
        Engine.bus.registerEvents(new RedirectListener());
        Graphics.init();
        Audio.init();

        LogRenderer.init();
        ConsoleRenderer.init();
        ProfilerRenderer.init();
        ParticleRenderer.init();

        Binds.getInstance().init(new RunCommandCallback() {

            @Override
            public void runCommand(String cmd) {
                GameClient.runCommand(cmd);
            }

        });

        user = new LocalUser();
        clearColour = new ClearColour();
        fxaa = new FXAA();
        deferred = new DeferredShading();
        ibl = new IBL(deferred);
        fog = new Fog();
        bloom = new Bloom();
        volumetricScattering = new VolumetricScattering();
        vignette = new Vignette();

        Renderer.setCubeModel(AssetLoader.loadModel("editor/cube"));

        physics = new PhysicsEngine();

        player = new Entity(ClientInputComponent.class);
        player.setLineIndex(-1);
        player.setValue("collisionMesh", "player");
        livingComponent = player.getComponent(LivingComponent.class);
        player.spawn();

        state = State.MAIN_MENU;
        loadMap(FileHelper.newGameFile("maps", "main-menu.asg"));

        gun = new Entity(MeshComponent.class);
        gun.setValue("model", "deagle");
        gun.getPosition().set(0.2f, -0.5f, -0.1f);
        gun.getRotation().set(0, 0, 0);
        gun.getScale().set(0.075f, 0.075f, 0.075f);
        gun.spawn();
        gun.getComponent(MeshComponent.class).disableFrustumCulling = true;
        gun.getComponent(MeshComponent.class).shader = ViewmodelShader.getInstance();

        texts = new ArrayList<Text>();

        texts.add(gpuTime = new TextBuilder().position(0.985f, 0.97f).align(Alignment.RIGHT).build());
        texts.add(cpuTime = new TextBuilder().position(0.985f, 0.92f).align(Alignment.RIGHT).build());

        texts.add(dead = new TextBuilder().text("DIED").size(6).colour(1, 1, 1, 0).align(Alignment.CENTER).build());
        texts.add(crosshair = new TextBuilder().text("+").size(1.3f).align(Alignment.CENTER).build());

        texts.add(health = new TextBuilder().position(-0.97f, -0.9265f).size(2).colour(0.75f, 0, 0).align(Alignment.LEFT).build());
        texts.add(ammo = new TextBuilder().position(0.945f, -0.9265f).size(2).colour(0.75f, 0, 0).align(Alignment.RIGHT).build());
        texts.add(reserveAmmo = new TextBuilder().position(0.995f, -0.9375f).size(1.5f).colour(0.75f, 0, 0).align(Alignment.RIGHT).build());
        texts.add(pressAnyKey = new TextBuilder().text("Press any key").position(0, -0.75f).size(2).align(Alignment.CENTER).build());

        texts.add(playerPosition = new TextBuilder().text(StringUtils.toString(player.getPosition())).position(0.785f, 0.97f).align(Alignment.RIGHT).build());
        texts.add(playerRotation = new TextBuilder().text(livingComponent.pitch + " " + livingComponent.yaw + " " + livingComponent.roll).position(0.785f, 0.92f).align(Alignment.RIGHT).build());

        guis = new ArrayList<GUI>();

        mainMenu = new Menu("Main Menu", new MenuItem("Join localhost") {

            @Override
            public void run(Menu parent) {
                GameClient.runCommand("connect 127.0.0.1");

                parent.close();
            }

        }, new MenuItem("Options") {

            @Override
            public void run(Menu parent) {
                parent.close();
                previousMenu = parent;

                optionsMenu.show();
            }

        }, new MenuItem("Quit") {

            @Override
            public void run(Menu parent) {
                shutdown();
                System.exit(0);
            }

        });

        optionsMenu = new Menu("Options", new Menu("Display", new MenuItem("Fullscreen") {

            @Override
            public void run(Menu parent) {
                try {
                    Display.setFullscreen(!Display.isFullscreen());
                } catch (LWJGLException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public float renderExtra(Text text, float x, float y, float longest) {
                text.setText(Display.isFullscreen() ? "Yes" : "No");
                text.getPosition().x += longest + 0.0f;
                text.setAlignment(Alignment.RIGHT);

                FontRenderer.render(text);

                text.setAlignment(Alignment.LEFT);

                return text.getLength();
            }

        }), new MenuItem("Back") {

            @Override
            public void run(Menu parent) {
                parent.close();
                previousMenu.show();
            }

        });

        pauseMenu = new Menu("Paused", new MenuItem("Resume") {

            @Override
            public void run(Menu parent) {
                state = State.IN_GAME;

                parent.close();
            }

        }, new MenuItem("Options") {

            @Override
            public void run(Menu parent) {
                parent.close();
                previousMenu = parent;

                optionsMenu.show();
            }

        }, new MenuItem("Exit to main menu") {

            @Override
            public void run(Menu parent) {
                parent.close();
                disconnect();
            }

        });

        new GameCommand("connect", "Connects to the specified server") {

            @Override
            public void run(IUser sender, String[] args) {
                if (args.length == 0) {
                    printDescription();
                    return;
                }

                if (client != null)
                    disconnect();

                pressAnyKey.setAlpha(0);
                crosshair.setText("+");
                state = State.IN_GAME;

                client = new Client(GameClient.this);
                client.connect(args[0], 24964);
                loaded = false;
            }

        };

        new GameCommand("disconnect", "Disconnects from the current server") {

            @Override
            public void run(IUser sender, String[] args) {
                disconnect();
            }

        };

        ClientGameCommands.init();
        EngineGameCommands.init();
        runCommand("exec autoexec");

        CorePacketManager.register();
        Log.info("App initialised.");
    }

    @Override
    public void update() {
        Profiler.start("Game Update");
        Requester.perform();
        MouseUtils.update();
        KeyboardUtils.update();
        Binds.getInstance().update();

        if (KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_PAUSE) || KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_END)) {
            console = !console;
            Binds.inUI = console;
            if (!Binds.inUI)
                Binds.inUI = Menu.isAnyMenuVisible();

            if (console)
                KeyboardUtils.getPressedCharacters().clear();
        }

        if (state == State.MAIN_MENU) {
            if (Mouse.isGrabbed())
                Mouse.setGrabbed(false);
            livingComponent.pitch = 0;
            livingComponent.yaw = 0;

            player.update();
            livingComponent.updateViewMatrix();

            pressAnyKey.setAlpha(mainMenu.isVisible() ? 0 : 1);
            if (KeyboardUtils.hasAnyKeyJustBeenPressed() && !console)
                mainMenu.show();

            boolean wasLoaded = loaded;
            loaded = scene != null ? scene.isLoaded() : false;
            if (loaded && !wasLoaded)
                scene.update();

            crosshair.setText("");
        } else if (state == State.IN_GAME) {
            if (!Binds.inUI) {
                if (!Mouse.isGrabbed())
                    Mouse.setGrabbed(true);
            } else {
                if (Mouse.isGrabbed())
                    Mouse.setGrabbed(false);
            }

            client.handle();

            boolean interacting = KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_E);
            if (KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_1))
                livingComponent.selectedIndex = 0;
            else if (KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_2))
                livingComponent.selectedIndex = 1;

            Profiler.start("Physics");
            accumulator += AppManager.getFrameTimeSeconds();
            while (accumulator >= PhysicsEngine.TICK_DELAY) {
                accumulator -= PhysicsEngine.TICK_DELAY;

                if (accumulator > 0.2)
                    accumulator = 0;

                player.updateFixed();
                if (scene != null) {
                    scene.updateFixed();
                    physics.update(scene);
                }
                livingComponent.move(scene, getTerrainByPoint(player.getPosition()));
                Scheduler.tick();

                FrustumCull.update();

                client.sendPacket(new PlayerMovementPacket(Keys.isKeyPressed("forward"), Keys.isKeyPressed("left"), Keys.isKeyPressed("back"), Keys.isKeyPressed("right"), player.getComponent(ClientInputComponent.class).space, KeyboardUtils.isKeyDown(Keyboard.KEY_LSHIFT), interacting, livingComponent.fire, livingComponent.reload, livingComponent.getSelectedIndex(), livingComponent.pitch, livingComponent.yaw));
            }
            Profiler.end("Physics");

            Profiler.start("Scene Update");
            player.update();
            gun.update();
            currentPostProcessVolume = null;
            if (scene != null) {
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

                scene.update();
            }

            if (currentPostProcessVolume != null) {
                Settings.clearR = Mathf.pow(currentPostProcessVolume.fogColour.x, 2.2f);
                Settings.clearG = Mathf.pow(currentPostProcessVolume.fogColour.y, 2.2f);
                Settings.clearB = Mathf.pow(currentPostProcessVolume.fogColour.z, 2.2f);
            } else {
                Settings.clearR = Settings.clearG = Settings.clearB = 0;
            }
            Profiler.end("Scene Update");

            dead.setAlpha(livingComponent.health <= 0 ? 1 : 0);
            health.setText((int) livingComponent.health + "");
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
                state = State.PAUSED;
                pauseMenu.show();
            }

            loaded = scene != null ? scene.isLoaded() : false;
        } else if (state == State.PAUSED) {
            if (!pauseMenu.isVisible())
                state = State.IN_GAME;
        }

        if (console)
            ConsoleRenderer.update();
        else
            Menu.update();

        if (ClientGameVariables.cl_showPerformanceInformation.getValueAsBool()) {
            gpuTime.setText(String.format("GPU: %.02f", AppManager.gpuTime) + " ms");
            cpuTime.setText(String.format("CPU: %.02f", AppManager.cpuTime) + " ms");
        } else {
            gpuTime.setText("");
            cpuTime.setText("");
        }

        if (ClientGameVariables.cl_showPosition.getValueAsBool()) {
            playerPosition.setText(StringUtils.toString(player.getPosition()));
            playerRotation.setText(livingComponent.pitch + " " + livingComponent.yaw + " " + livingComponent.roll);
        } else {
            playerPosition.setText("");
            playerRotation.setText("");
        }

        Profiler.end("Game Update");
    }

    @Override
    public void render() {
        if (scene == null)
            return;

        Profiler.start("Scene");
        Profiler.start("Shadows (Full)");
        if (shadows != null && shadows.start(livingComponent.getInverseViewMatrix(), livingComponent.getEyePosition(), livingComponent.pitch, livingComponent.yaw))
            scene.renderShadows(shadows);
        Profiler.end("Shadows (Full)");

        deferred.start();
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        clearColour.perform();
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        if (ClientGameVariables.r_wireframe.getValueAsBool())
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

        scene.render();
        GL11.glDepthRange(0, 0.99f);
        if (state == State.IN_GAME || state == State.PAUSED)
            ClientScene.renderEntity(gun);
        GL11.glDepthRange(0, 1);

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

        if (!loaded)
            GUIRenderer.perform(0);

        Profiler.end("Post processing");

        Profiler.start("GUI");
        GL11.glEnable(GL11.GL_BLEND);
        GL40.glBlendFunci(0, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GUIRenderer.render(guis);
        FontRenderer.render(texts);

        if (console) {
            ConsoleRenderer.render();
        } else {
            if (ClientGameVariables.developer.getValueAsBool())
                LogRenderer.render();
            Menu.render();
        }

        Profiler.end("GUI");

        ProfilerRenderer.render(ClientGameVariables.cl_showProfiler.getValueAsInt());

        if (KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_G)) {
            Profiler.dump();
        }

        GL11.glDisable(GL11.GL_BLEND);
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

        if (client != null)
            client.shutdown();
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
            if (scene == null)
                return;

            scene.getEntities().add(((EntitySpawnPacket) receivedPacket).entity);
        } else if (receivedPacket.getId() == CorePacketManager.ENTITY_ADD_COMPONENT_PACKET) {
            EntityAddComponentPacket packet = (EntityAddComponentPacket) receivedPacket;

            Entity entity = getNetworkedEntityFromId(packet.id);
            if (entity == null)
                return;

            entity.addComponent(packet.component);
        } else if (receivedPacket.getId() == CorePacketManager.ENTITY_COMPONENT_VARIABLE_CHANGE_PACKET) {
            EntityComponentVariableChangePacket packet = (EntityComponentVariableChangePacket) receivedPacket;
            if (packet.field == null)
                return;

            try {
                Entity entity = getNetworkedEntityFromId(packet.id);
                if (entity == null)
                    return;

                packet.field.set(entity.getComponent(packet.clazz), packet.value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (receivedPacket.getId() == CorePacketManager.ENTITY_REMOVE_COMPONENT_PACKET) {
            EntityRemoveComponentPacket packet = (EntityRemoveComponentPacket) receivedPacket;

            Entity entity = getNetworkedEntityFromId(packet.id);
            if (entity == null)
                return;

            entity.removeComponent(packet.clazz);
        } else if (receivedPacket.getId() == CorePacketManager.ENTITY_KEY_VALUE_PACKET) {
            EntityKeyValuePacket packet = (EntityKeyValuePacket) receivedPacket;

            Entity entity = getNetworkedEntityFromId(packet.id);
            if (entity == null)
                return;

            entity.setValue(packet.key, packet.value);
        } else if (receivedPacket.getId() == CorePacketManager.ENTITY_DESTROY_PACKET) {
            if (scene == null)
                return;

            scene.getEntities().remove(getNetworkedEntityFromId(((EntityDestroyPacket) receivedPacket).id));
        } else if (receivedPacket.getId() == CorePacketManager.ENTITY_LINK_PACKET) {
            EntityLinkPacket packet = (EntityLinkPacket) receivedPacket;

            for (Entity entity : scene.getEntities()) {
                if (entity.getLineIndex() == packet.lineIndex) {
                    entity.setId(packet.id);
                    break;
                }
            }
        } else if (receivedPacket.getId() == CorePacketManager.GAME_VARIABLE_PACKET) {
            GameVariablePacket packet = (GameVariablePacket) receivedPacket;

            GameVariable variable = GameVariableManager.getByName(packet.name);
            if (variable == null)
                return;

            variable.setValue(packet.value); // value is trusted - could be modified via MITM
        } else if (receivedPacket.getId() == CorePacketManager.RUN_COMMAND_PACKET) {
            GameCommandManager.run(user, ((RunCommandPacket) receivedPacket).command);
        } else if (receivedPacket.getId() == CorePacketManager.SEND_MESSAGE_PACKET) {
            SendMessagePacket packet = (SendMessagePacket) receivedPacket;
            System.out.println(packet.message + " " + packet.type);

            if (packet.type == 0)
                Log.print(packet.message);
            // TODO chat message - packet.type 1
        }
    }

    @Override
    public void handleException(Exception e) {
        Log.info(e.getMessage());
        disconnect();
    }

    @Override
    public void handleDisconnect(BaseNetworkable net) {
        state = State.MAIN_MENU;

        client = null;
        if (!currentLevel.equals(mainMenuPath))
            loadMap(FileHelper.newGameFile("maps", "main-menu.asg"));
    }

    public Entity getNetworkedEntityFromId(int id) {
        for (Entity entity : scene.getEntities())
            if (entity.getId() == id)
                return entity;

        return null;
    }

    public void disconnect() {
        if (client == null)
            return;

        state = State.MAIN_MENU;

        client.disconnect();
        client = null;

        if (!currentLevel.equals(mainMenuPath))
            loadMap(FileHelper.newGameFile("maps", "main-menu.asg"));
    }

    @Override
    public String getTitle() {
        return "Yet Another";
    }

    public static GameClient getInstance() {
        return (GameClient) AppManager.getInstance();
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

    public static PostProcessVolumeComponent getCurrentPostProcessVolume() {
        return ((Game) AppManager.getInstance()).currentPostProcessVolume;
    }

    public static Framebuffer getSceneFramebuffer() {
        return ((Game) AppManager.getInstance()).deferred.getOutputFBO();
    }

    public static int getAmbientOcclusionTexture() {
        return ((Game) AppManager.getInstance()).deferred.getAmbientOcclusionTexture();
    }

    public static int getBRDF() {
        return ((Game) AppManager.getInstance()).ibl.getBRDF();
    }

    public static Shadows getShadows() {
        return ((Game) AppManager.getInstance()).shadows;
    }

    public static int getShadowMap(int map) {
        return ((Game) AppManager.getInstance()).shadows.getShadowMap(map);
    }

    public static Matrix4f getToShadowMapSpaceMatrix(int map) {
        return ((Game) AppManager.getInstance()).shadows.getToShadowMapSpaceMatrix(map);
    }

    public static float getShadowExtents(int map) {
        return ((Game) AppManager.getInstance()).shadows.getExtents(map);
    }

    public static Client getClient() {
        return ((Game) AppManager.getInstance()).client;
    }

    public static void shutdownGame() {
        ((Game) AppManager.getInstance()).shutdown();
    }

    public static void runCommand(String command) {
        GameCommandManager.run(((Game) AppManager.getInstance()).user, command);
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
        currentLevel = map.getAbsolutePath();

        boolean wait = false;
        if (scene != null) {
            List<Entity> entities = scene.getEntitiesWithComponent(MeshComponent.class);
            List<Terrain> terrains = scene.getTerrains();
            VirtualFileSystem vfs = scene.getVirtualFileSystem();

            if (Thread.currentThread() != mainThread) {
                wait = true;

                Scheduler.schedule(new ScheduledRunnable() {

                    @Override
                    public void tick(float time, float percentage) {

                    }

                    @Override
                    public void finish() {
                        for (Entity entity : entities) {
                            entity.getComponent(MeshComponent.class).model.unload();
                            AssetLoader.removeModel(entity.getComponent(MeshComponent.class).model);
                        }

                        for (Terrain terrain : terrains)
                            ((ClientTerrain) terrain).unload();

                        if (vfs != null)
                            vfs.unload();
                        scene = null;
                    }

                }, 0);
            } else {
                for (Entity entity : entities) {
                    entity.getComponent(MeshComponent.class).model.unload();
                    AssetLoader.removeModel(entity.getComponent(MeshComponent.class).model);
                }

                for (Terrain terrain : terrains)
                    ((ClientTerrain) terrain).unload();

                if (vfs != null)
                    vfs.unload();
                scene = null;
            }
        }

        if (wait) {
            try {
                while (scene != null)
                    Thread.sleep(10);
            } catch (Exception e) {

            }
        }

        scene = new GameMap(map).getScene();
        Engine.scene = scene;

        for (Entity entity : scene.getEntitiesWithComponent(SkyComponent.class))
            sky = entity.getComponent(SkyComponent.class);

        for (Entity entity : scene.getEntitiesWithComponent(SunComponent.class)) {
            if (sky != null)
                sky.setLight(entity);
            lightComponent = entity.getComponent(LightComponent.class);
            shadows = new Shadows(lightComponent);
        }

        player.getPosition().set(scene.getSpawn());
        player.getVelocity().set(0, 0, 0);
        loaded = false;
    }

    public IUser getUser() {
        return user;
    }

    public static void main(String[] args) {
        AppManager.create(new GameClient());
    }

}
