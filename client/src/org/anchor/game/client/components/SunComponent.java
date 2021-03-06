package org.anchor.game.client.components;

import java.util.Arrays;
import java.util.List;

import org.anchor.client.engine.renderer.types.light.LightType;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;

public class SunComponent implements IComponent {

    @Override
    public void precache(Entity entity) {
        entity.getComponent(LightComponent.class).type = LightType.DIRECTIONAL;
    }

    @Override
    public void spawn() {

    }

    @Override
    public void setValue(String key, String value) {

    }

    @Override
    public List<Class<? extends IComponent>> getDependencies() {
        return Arrays.asList(LightComponent.class);
    }

    @Override
    public IComponent copy() {
        return new SunComponent();
    }

}
