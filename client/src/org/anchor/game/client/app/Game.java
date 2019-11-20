package org.anchor.game.client.app;

import java.util.ArrayList;
import java.util.List;

import org.anchor.client.engine.renderer.deferred.DeferredShading;
import org.anchor.client.engine.renderer.fog.Fog;
import org.anchor.client.engine.renderer.ibl.IBL;
import org.anchor.client.engine.renderer.shadows.Shadows;
import org.anchor.client.engine.renderer.types.light.Light;
import org.anchor.engine.common.app.App;
import org.anchor.engine.common.net.client.Client;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.net.IUser;
import org.anchor.game.client.components.LightComponent;
import org.anchor.game.client.components.PostProcessVolumeComponent;
import org.anchor.game.client.components.SkyComponent;
import org.anchor.game.client.types.ClientScene;

public abstract class Game extends App {

    public Entity player;
    public ClientScene scene;

    public PostProcessVolumeComponent currentPostProcessVolume;
    public SkyComponent sky;
    public LivingComponent livingComponent;

    public Shadows shadows;
    public DeferredShading deferred;

    public IBL ibl;
    public Fog fog;

    public Client client;
    public IUser user;

    public List<Light> getLights() {
        List<Light> lights = new ArrayList<Light>();
        if (scene == null)
            return lights;

        for (IComponent component : scene.getComponents(LightComponent.class))
            lights.add((Light) component);

        return lights;
    }

}
