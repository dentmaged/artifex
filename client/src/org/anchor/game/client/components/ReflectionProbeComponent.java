package org.anchor.game.client.components;

import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.types.cubemap.BakedCubemap;
import org.anchor.client.engine.renderer.types.cubemap.CubemapFramebuffer;
import org.anchor.client.engine.renderer.types.ibl.ReflectionProbe;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.scheduler.ScheduledRunnable;
import org.anchor.engine.shared.scheduler.Scheduler;
import org.anchor.engine.shared.utils.Property;
import org.anchor.game.client.CubemapGenerator;
import org.lwjgl.util.vector.Vector3f;

public class ReflectionProbeComponent implements IComponent, ReflectionProbe {

    public CubemapFramebuffer cubemap;
    public BakedCubemap prefilter;

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
                    cubemap = new CubemapFramebuffer(Settings.reflectionProbeSize);
                    prefilter = new BakedCubemap(Settings.reflectionProbeSize, "prefilter", 9);
                }

                CubemapGenerator.generate(entity);
                prefilter.perform(cubemap.getTexture());
            }

        }, 1);
    }

    @Override
    public IComponent copy() {
        ReflectionProbeComponent copy = new ReflectionProbeComponent();
        copy.cubemap = cubemap;
        copy.prefilter = prefilter;

        return copy;
    }

    @Override
    public int getPrefilteredCubemap() {
        if (prefilter == null)
            return 0;

        return prefilter.getTexture();
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
