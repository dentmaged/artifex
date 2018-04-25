package org.anchor.game.client.app;

import java.util.ArrayList;
import java.util.List;

import org.anchor.client.engine.renderer.deferred.DeferredShading;
import org.anchor.client.engine.renderer.shadows.Shadows;
import org.anchor.client.engine.renderer.types.Light;
import org.anchor.engine.common.app.App;
import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.components.LightComponent;
import org.anchor.game.client.components.SkyComponent;
import org.anchor.game.client.types.ClientScene;

public abstract class Game extends App {

    public Entity player;
    public ClientScene scene;
    public SkyComponent sky;
    public DeferredShading deferred;
    public Shadows shadows;

    public List<Light> getLights() {
        List<Light> lights = new ArrayList<Light>();
        if (scene == null)
            return lights;

        for (IComponent component : scene.getComponents(LightComponent.class))
            lights.add((Light) component);

        return lights;
    }

}
