package org.anchor.game.client.components;

import org.anchor.client.engine.renderer.types.cubemap.BakedCubemap;
import org.anchor.client.engine.renderer.types.cubemap.CubemapFramebuffer;
import org.anchor.client.engine.renderer.types.ibl.LightProbe;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.scheduler.ScheduledRunnable;
import org.anchor.engine.shared.scheduler.Scheduler;
import org.anchor.engine.shared.utils.Property;
import org.anchor.game.client.CubemapGenerator;
import org.lwjgl.util.vector.Vector3f;

public class LightProbeComponent implements IComponent, LightProbe {

    public CubemapFramebuffer cubemap;
    public BakedCubemap irradiance;

    protected Entity entity;

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

    @Property("Bake")
    public void bake() {
        Scheduler.schedule(new ScheduledRunnable() {

            @Override
            public void tick(float time, float percentage) {

            }

            @Override
            public void finish() {
                if (cubemap == null) {
                    cubemap = new CubemapFramebuffer(32);
                    irradiance = new BakedCubemap(32, "irradianceConvolution", 1);
                }

                CubemapGenerator.generate(entity);
                irradiance.perform(cubemap.getTexture());
            }

        }, 1);
    }

    @Override
    public IComponent copy() {
        LightProbeComponent copy = new LightProbeComponent();
        copy.cubemap = cubemap;
        copy.irradiance = irradiance;

        return copy;
    }

    @Override
    public int getIrradianceCubemap() {
        if (irradiance == null)
            return 0;

        return irradiance.getTexture();
    }

    @Override
    public Vector3f getPosition() {
        return entity.getPosition();
    }

    @Override
    public float getSize() {
        return entity.getScale().x;
    }

}
