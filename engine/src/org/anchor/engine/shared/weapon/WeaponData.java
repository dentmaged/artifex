package org.anchor.engine.shared.weapon;

import org.anchor.engine.shared.entity.Entity;
import org.lwjgl.util.vector.Vector3f;

public abstract class WeaponData {

    protected String name;
    protected Vector3f position, rotation, scale;
    protected float damage;
    protected long attackDuration;

    public WeaponData(String name, Vector3f position, Vector3f rotation, Vector3f scale, float damage, float attackDuration) {
        this.name = name;
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
        this.damage = damage;
        this.attackDuration = (long) (attackDuration * 1000000000f);
    }

    public String getName() {
        return name;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public Vector3f getScale() {
        return scale;
    }

    public float getDamage() {
        return damage;
    }

    public long getAttackDuration() {
        return attackDuration;
    }

    public abstract void perform(Weapon weapon, Entity owner);

}
