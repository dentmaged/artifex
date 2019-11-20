package org.anchor.game.client.components;

import org.anchor.client.engine.renderer.fog.IFogManager;
import org.anchor.engine.common.utils.AABB;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.utils.Property;
import org.anchor.game.client.GameClient;
import org.lwjgl.util.vector.Vector3f;

public class PostProcessVolumeComponent implements IComponent, IFogManager {

    @Property("Fog")
    public boolean fog = true;

    @Property("Fog Colour")
    public Vector3f fogColour = new Vector3f(0.42f, 0.49f, 0.6f);

    @Property("Fog Density")
    public float fogDensity = 0.005f;

    @Property("Fog Gradient")
    public float fogGradient = 3;

    @Property("Fog Sun Power")
    public float fogSunPower = 24;

    @Property("Horizon Blend Start")
    public float horizonBlendStart = 0.9f;

    @Property("Horizon Blend End")
    public float horizonBlendEnd = 0.95f;
    
    @Property("Volumetric Scattering")
    public boolean volumetric = false;

    @Property("G Scattering")
    public float gScattering = 0.0924f;

    private Entity entity;

    private static final AABB defaultAABB = new AABB(new Vector3f(-0.5f, -0.5f, -0.5f), new Vector3f(0.5f, 0.5f, 0.5f));

    @Override
    public void precache(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void spawn() {

    }

    @Override
    public void setValue(String key, String value) {

    }

    public AABB getAABB() {
        if (entity == null)
            return null;

        return AABB.generateAABB(defaultAABB, entity.getTransformationMatrix());
    }

    @Property("Set fog colour to base colour")
    public void setFogColourToBase() {
        fogColour.set(GameClient.getSky().baseColour);
    }

    @Override
    public boolean isFogEnabled() {
        return fog;
    }

    @Override
    public Vector3f getFogColour() {
        return fogColour;
    }

    @Override
    public float getFogDensity() {
        return fogDensity;
    }

    @Override
    public float getFogGradient() {
        return fogGradient;
    }

    @Override
    public float getFogSunPower() {
        return fogSunPower;
    }

    @Override
    public float getHorizonBlendStart() {
        return horizonBlendStart;
    }

    @Override
    public float getHorizonBlendEnd() {
        return horizonBlendEnd;
    }

    public boolean isWaterPostProcess() {
        return entity.hasComponent(WaterComponent.class);
    }

    @Override
    public IComponent copy() {
        PostProcessVolumeComponent copy = new PostProcessVolumeComponent();
        copy.fog = fog;
        copy.fogColour = fogColour;
        copy.fogDensity = fogDensity;
        copy.fogGradient = fogGradient;
        copy.horizonBlendStart = horizonBlendStart;
        copy.horizonBlendEnd = horizonBlendEnd;

        return copy;
    }

}
