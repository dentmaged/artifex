package org.anchor.game.client.components;

import java.util.Arrays;
import java.util.List;

import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.utils.Property;
import org.anchor.game.client.app.AppManager;
import org.anchor.game.client.shaders.WaterShader;
import org.lwjgl.util.vector.Vector3f;

public class WaterComponent implements IComponent {

    @Property("Colour")
    public Vector3f colour = new Vector3f(0, 0.075f, 0.125f);

    @Property("Water Speed")
    public float speed = 0.03f;

    @Property("Clarity")
    public float clarity = 3;

    public float time;

    @Override
    public void precache(Entity entity) {
        entity.getComponent(MeshComponent.class).shader = WaterShader.getInstance();
    }

    @Override
    public void spawn() {

    }

    @Override
    public void setValue(String key, String value) {

    }

    @Override
    public void update() {
        time += AppManager.getFrameTimeSeconds() * speed;
        time %= 1;
    }

    @Override
    public IComponent copy() {
        WaterComponent copy = new WaterComponent();
        copy.colour = new Vector3f(colour);
        copy.speed = speed;
        copy.time = time;

        return copy;
    }

    @Override
    public List<Class<? extends IComponent>> getDependencies() {
        return Arrays.asList(MeshComponent.class);
    }

}
