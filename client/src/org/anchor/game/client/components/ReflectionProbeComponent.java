package org.anchor.game.client.components;

import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.types.cubemap.CubemapFramebuffer;
import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.entity.Entity;

public class ReflectionProbeComponent implements IComponent {

    public CubemapFramebuffer cubemap;

    @Override
    public void spawn(Entity entity) {
        cubemap = new CubemapFramebuffer(Settings.reflectionProbeSize);
    }

    @Override
    public void setValue(String key, String value) {

    }

    @Override
    public IComponent copy() {
        ReflectionProbeComponent copy = new ReflectionProbeComponent();
        copy.cubemap = cubemap;

        return copy;
    }

}
