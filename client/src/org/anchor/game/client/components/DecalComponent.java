package org.anchor.game.client.components;

import org.anchor.client.engine.renderer.types.Material;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.utils.Property;
import org.anchor.game.client.loaders.AssetLoader;

public class DecalComponent implements IComponent {

    @Property("Texture")
    public Material material;

    protected Entity entity;

    @Override
    public void spawn(Entity entity) {
        this.entity = entity;
        if (material == null && entity.containsKey("material"))
            material = AssetLoader.loadMaterial(entity.getValue("material"));
    }

    @Override
    public void setValue(String key, String value) {

    }

    @Override
    public IComponent copy() {
        DecalComponent copy = new DecalComponent();
        copy.material = material;

        return copy;
    }

}
