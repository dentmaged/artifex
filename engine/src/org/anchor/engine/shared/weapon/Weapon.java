package org.anchor.engine.shared.weapon;

import org.anchor.engine.shared.entity.Entity;

public class Weapon {

    protected Entity owner;
    protected WeaponData data;
    protected long lastAttackTime;

    public Weapon(Entity owner, WeaponData data) {
        this.owner = owner;
        this.data = data;
    }

    public boolean attack() {
        return true;
    }

    public WeaponData getData() {
        return data;
    }

}
