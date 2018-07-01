package org.anchor.game.client.components;

import org.anchor.client.engine.renderer.types.Light;
import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.utils.Property;
import org.lwjgl.util.vector.Vector3f;

public class LightComponent implements IComponent, Light {

    private Entity entity;
    @Property("Relative Position")
    public Vector3f relativePosition = new Vector3f();

    @Property("Colour")
    public Vector3f colour = new Vector3f(1, 1, 1);

    @Property("Attenuation")
    public Vector3f attenuation = new Vector3f(1, 0, 0.001f);

    @Property("Directional")
    public boolean directional = false;

    @Override
    public void spawn(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void setValue(String key, String value) {

    }

    @Override
    public Vector3f getPosition() {
        if (entity == null)
            return relativePosition;

        return Vector3f.add(entity.getPosition(), relativePosition, null);
    }

    @Override
    public Vector3f getColour() {
        return colour;
    }

    @Override
    public Vector3f getAttenuation() {
        return attenuation;
    }

    @Override
    public boolean isDirectionalLight() {
        return directional;
    }

    @Override
    public IComponent copy() {
        LightComponent copy = new LightComponent();
        copy.relativePosition = new Vector3f(relativePosition);
        copy.colour = new Vector3f(colour);
        copy.attenuation = new Vector3f(attenuation);

        return copy;
    }

}
