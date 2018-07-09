package org.anchor.engine.shared.weapon;

import org.lwjgl.util.vector.Vector3f;

public abstract class GunData extends WeaponData {

    protected int ammoPerClip, reserveAmmo;
    protected long reloadDuration;

    public GunData(String name, Vector3f position, Vector3f rotation, Vector3f scale, float damage, int ammoPerClip, int reserveAmmo, float delayBetweenShots, float reloadDuration) {
        super(name, position, rotation, scale, damage, delayBetweenShots);

        this.ammoPerClip = ammoPerClip;
        this.reserveAmmo = reserveAmmo;
        this.reloadDuration = (long) (reloadDuration * 1000000000f);
    }

    public int getAmmoPerClip() {
        return ammoPerClip;
    }

    public int getReserveAmmo() {
        return reserveAmmo;
    }

    public long getReloadDuration() {
        return reloadDuration;
    }

}
