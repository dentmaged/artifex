package org.anchor.engine.shared.weapon;

import org.anchor.engine.shared.entity.Entity;

public class Gun extends Weapon {

    protected int ammo, reserveAmmo;
    protected long reloadStartTime;
    protected boolean canShoot;

    public Gun(Entity owner, GunData data, boolean canShoot) {
        super(owner, data);

        this.ammo = data.getAmmoPerClip();
        this.reserveAmmo = data.getReserveAmmo();
        this.canShoot = canShoot;
    }

    @Override
    public boolean attack() {
        long time = System.nanoTime();
        if (time < lastAttackTime + data.getAttackDuration())
            return false;

        if (time < reloadStartTime + ((GunData) data).getReloadDuration())
            return false;

        if (ammo == 0) {
            reload();

            return false;
        }

        ammo--;
        lastAttackTime = time;

        if (canShoot)
            data.perform(this, owner);

        if (ammo == 0)
            reload();

        return true;
    }

    public void reload() {
        long time = System.nanoTime();
        if (reserveAmmo <= 0)
            return;

        if (isReloading())
            return;

        reloadStartTime = time;
        reserveAmmo -= ((GunData) data).getAmmoPerClip() - ammo;
        ammo = ((GunData) data).getAmmoPerClip();

    }

    public boolean isReloading() {
        return System.nanoTime() < reloadStartTime + ((GunData) data).getReloadDuration();
    }

    public int getAmmo() {
        return ammo;
    }

    public int getReserveAmmo() {
        return reserveAmmo;
    }

    public GunData getData() {
        return (GunData) data;
    }

}
