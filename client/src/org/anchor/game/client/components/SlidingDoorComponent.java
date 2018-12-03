package org.anchor.game.client.components;

import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.components.IInteractable;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.physics.PhysicsEngine;
import org.anchor.engine.shared.scheduler.ScheduledRunnable;
import org.anchor.engine.shared.scheduler.Scheduler;
import org.anchor.engine.shared.utils.Property;
import org.lwjgl.util.vector.Vector3f;

public class SlidingDoorComponent implements IInteractable {

    @Property("Duration")
    public float duration = 0.5f;

    @Property("Move Distance")
    public float moveDistance = 2;

    @Property("Move Direction")
    public Vector3f direction = new Vector3f(1, 0, 0);

    private Entity entity;
    private boolean open;

    @Override
    public void spawn(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void setValue(String key, String value) {

    }

    @Override
    public void interact() {
        Vector3f distancePerTick = VectorUtils.mul(direction, moveDistance * PhysicsEngine.TICK_DELAY / duration);

        Scheduler.schedule(new ScheduledRunnable() {

            @Override
            public void tick(float time, float percentage) {
                if (open)
                    Vector3f.sub(entity.getPosition(), distancePerTick, entity.getPosition());
                else
                    Vector3f.add(entity.getPosition(), distancePerTick, entity.getPosition());
            }

            @Override
            public void finish() {
                open = !open;
            }

        }, duration);
    }

    @Override
    public IComponent copy() {
        SlidingDoorComponent copy = new SlidingDoorComponent();
        copy.duration = duration;
        copy.moveDistance = moveDistance;
        copy.open = open;

        return copy;
    }

}
