package org.anchor.game.client.components;

import org.anchor.client.engine.renderer.types.texture.ModelTexture;
import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.utils.Property;
import org.anchor.game.client.loaders.AssetLoader;

public class DecalComponent implements IComponent {

    @Property("Texture")
    public ModelTexture texture;

    protected Entity entity;

    @Override
    public void spawn(Entity entity) {
        this.entity = entity;
        if (texture == null && entity.containsKey("texture"))
            texture = AssetLoader.loadDecalTexture(entity.getValue("texture"));
    }

    @Override
    public void setValue(String key, String value) {

    }

    @Override
    public IComponent copy() {
        DecalComponent copy = new DecalComponent();
        copy.texture = texture;

        return copy;
    }

}