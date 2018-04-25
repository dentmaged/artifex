package org.anchor.engine.shared.physics;

public class Material {

    protected float restitution, density, friction;

    public static final Material WOOD = new Material(0.6f, 0.75f, 0.25f);

    public Material(float restitution, float density, float friction) {
        this.restitution = restitution;
        this.density = density;
        this.friction = friction;
    }

    public float getRestition() {
        return restitution;
    }

    public float getDensity() {
        return density;
    }

    public float getFriction() {
        return friction;
    }

}
