package org.anchor.engine.shared.physics.collision.broadphase;

import org.anchor.engine.shared.entity.Entity;

public class BroadphaseCheck {

    private Entity one, two;

    public BroadphaseCheck(Entity one, Entity two) {
        this.one = one;
        this.two = two;
    }

    public Entity getOne() {
        return one;
    }

    public Entity getTwo() {
        return two;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof BroadphaseCheck))
            return false;

        BroadphaseCheck other = (BroadphaseCheck) object;

        if (this == other)
            return true;

        return (other.one.equals(two) && other.two.equals(one)) || (other.one.equals(one) && other.two.equals(two));
    }

}
