package org.anchor.engine.shared.weapon.implementation;

import org.anchor.engine.common.utils.Mathf;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.Engine;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.utils.EntityRaycast;
import org.anchor.engine.shared.weapon.GunData;
import org.anchor.engine.shared.weapon.Weapon;
import org.lwjgl.util.vector.Vector3f;

public class ShotgunData extends GunData {

    public static final float ANGLE = 20;
    public static Vector3f CONE = new Vector3f(Mathf.sin(ANGLE) * 0.25f, Mathf.sin(ANGLE) * 0.25f, Mathf.sin(ANGLE) * 0.25f);

    public ShotgunData() {
        super("Shotgun", new Vector3f(), new Vector3f(), new Vector3f(1, 1, 1), 13, 8, 64, 0.75f, 2);
    }

    @Override
    public void perform(Weapon weapon, Entity owner) {
        if (Engine.isClientSide()) // TODO: Play particle effect on client
            return;

        LivingComponent livingComponent = owner.getComponent(LivingComponent.class);
        Vector3f origin = livingComponent.getEyePosition();
        Vector3f ray = livingComponent.getForwardVector();

        for (float x = -1; x <= 1; x++) {
            for (float y = -1; y <= 1; y++) {
                EntityRaycast raycast = Engine.raycast(origin, Vector3f.add(ray, VectorUtils.mul(CONE, new Vector3f(x, y, 0)), null));
                if (raycast.getEntity() != null) {
                    LivingComponent component = raycast.getEntity().getComponent(LivingComponent.class);
                    if (component != null)
                        component.damage(damage);
                }
            }
        }
    }

}
