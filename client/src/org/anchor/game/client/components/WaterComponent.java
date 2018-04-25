package org.anchor.game.client.components;

import org.anchor.client.engine.renderer.Loader;
import org.anchor.engine.common.TextureType;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.utils.Property;
import org.anchor.game.client.app.AppManager;
import org.anchor.game.client.shaders.WaterShader;
import org.lwjgl.util.vector.Vector4f;

public class WaterComponent implements IComponent {

    public String dudvMapName = "dudv", normalMapName = "normal";
    public int dudvMap, normalMap;
    public float moveFactor;

    @Property("Wave Speed")
    public float waveSpeed;

    @Property("Wave Strength")
    public float waveStrength;

    @Property("Colour")
    public Vector4f colour = new Vector4f(0, 0.075f, 0.125f, 0.5f);

    private Entity entity;
    private boolean set;

    @Override
    public void spawn(Entity entity) {
        this.dudvMap = Loader.getInstance().loadTexture(TextureType.WATER, dudvMapName);
        this.normalMap = Loader.getInstance().loadTexture(TextureType.WATER, normalMapName);
        this.waveSpeed = 0.03f;
        this.waveStrength = 0.1f;

        this.entity = entity;
        if (entity.hasComponent(MeshComponent.class)) {
            entity.getComponent(MeshComponent.class).shader = WaterShader.getInstance();
            entity.getComponent(MeshComponent.class).castsShadows = false;
            set = true;
        }
    }

    @Override
    public void setValue(String key, String value) {
        if (key.equals("dudvMap"))
            dudvMapName = value;

        if (key.equals("normalMap"))
            normalMapName = value;

        if (key.equals("waveSpeed"))
            waveSpeed = Float.parseFloat(value);

        if (key.equals("waveStrength"))
            waveStrength = Float.parseFloat(value);

        if (key.equals("colour"))
            colour.set(VectorUtils.stringToFourVector(value));
    }

    @Override
    public void update() {
        if (entity.hasComponent(MeshComponent.class) && !set) {
            entity.getComponent(MeshComponent.class).shader = WaterShader.getInstance();
            entity.getComponent(MeshComponent.class).castsShadows = false;
            set = true;
        }

        moveFactor += waveSpeed * AppManager.getFrameTimeSeconds();
        moveFactor %= 1;
    }

    @Override
    public IComponent copy() {
        WaterComponent copy = new WaterComponent();
        copy.waveSpeed = waveSpeed;
        copy.waveStrength = waveStrength;
        copy.colour = new Vector4f(colour);

        return copy;
    }

}
