package org.anchor.engine.shared.physics;

import org.anchor.engine.shared.entity.Entity;

public interface PhysicsTouchListener {

    public void touch(Entity primary, Entity secondary);

}
