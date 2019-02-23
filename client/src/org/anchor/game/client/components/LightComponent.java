package org.anchor.game.client.components;

import org.anchor.client.engine.renderer.types.light.Light;
import org.anchor.client.engine.renderer.types.light.LightType;
import org.anchor.engine.common.utils.CoreMaths;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.utils.Property;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class LightComponent implements IComponent, Light {

    @Property("Relative Position")
    public Vector3f relativePosition = new Vector3f();

    @Property("Colour")
    public Vector3f colour = new Vector3f(1, 1, 1);

    @Property("Attenuation")
    public Vector3f attenuation = new Vector3f(1, 0, 0.001f);

    @Property("Cutoff (degrees)")
    public float cutoff = 35;

    @Property("Outer cutoff (degrees)")
    public float outerCutoff = 45;

    @Property("Volumetric Light")
    public boolean volumetric = true;

    @Property("Type")
    public LightType type = LightType.POINT;

    private Entity entity;

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
    public Vector3f getDirection() {
        if (entity == null)
            return new Vector3f(0, 0, -1);

        return VectorUtils.normalise(new Vector3f(Matrix4f.transform(CoreMaths.createTransformationMatrix(new Vector3f(), entity.getRotation(), new Vector3f(1, 1, 1)), new Vector4f(0, 0, -1, 0), null)));
    }

    @Override
    public float getCutoff() {
        return cutoff;
    }

    @Override
    public float getOuterCutoff() {
        return outerCutoff;
    }

    @Override
    public boolean isVolumetricLight() {
        return volumetric;
    }

    @Override
    public LightType getLightType() {
        return type;
    }

    @Override
    public IComponent copy() {
        LightComponent copy = new LightComponent();
        copy.relativePosition = new Vector3f(relativePosition);
        copy.colour = new Vector3f(colour);
        copy.attenuation = new Vector3f(attenuation);
        copy.cutoff = cutoff;
        copy.outerCutoff = outerCutoff;
        copy.type = type;

        return copy;
    }

}
