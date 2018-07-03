package org.anchor.game.client.components;

import java.util.Arrays;
import java.util.List;

import org.anchor.client.engine.renderer.types.light.LightType;
import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.entity.Entity;

public class SunComponent implements IComponent {

    @Override
    public void spawn(Entity entity) {
        entity.getComponent(LightComponent.class).type = LightType.DIRECTIONAL;
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
