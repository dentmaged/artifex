package org.anchor.engine.shared.weapon.implementation;

import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.Engine;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.components.PhysicsComponent;
import org.anchor.engine.shared.components.redirections.EngineMeshComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.physics.PhysicsTouchListener;
import org.anchor.engine.shared.weapon.GunData;
import org.anchor.engine.shared.weapon.Weapon;
import org.lwjgl.util.vector.Vector3f;

public class CrossbowData extends GunData {

    public CrossbowData() {
        super("Crossbow", new Vector3f(), new Vector3f(), new Vector3f(1, 1, 1), 200, 5, 10, 2.5f, 3);
    }

    @Override
    public void perform(Weapon weapon, Entity owner) {
        if (Engine.isClientSide())
            return;

        LivingComponent livingComponent = owner.getComponent(LivingComponent.class);

        Entity projectile = new Entity(PhysicsComponent.class, EngineMeshComponent.class);
        projectile.setValue("model", "editor/cube");
        projectile.setValue("collisionMesh", "aabb");
        PhysicsComponent physics = projectile.getComponent(PhysicsComponent.class);
        physics.inverseMass = 2;
        physics.gravity = false;
        physics.listeners.add(new PhysicsTouchListener() {

            @Override
            public void touch(Entity primary, Entity secondary) {
                Entity other = primary == projectile ? secondary : primary;

                LivingComponent component = other.getComponent(LivingComponent.class);
                if (component != null)
                    component.damage(damage);
                projectile.destroy();
            }

        });
        projectile.getPosition().set(livingComponent.getEyePosition());
        projectile.getVelocity().set(VectorUtils.mul(livingComponent.getForwardVector(), 50));
        projectile.spawn();
        Engine.getEntities().add(projectile);
    }

}
