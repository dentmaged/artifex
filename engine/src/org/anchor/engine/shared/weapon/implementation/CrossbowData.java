package org.anchor.engine.shared.weapon.implementation;

import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.weapon.GunData;
import org.lwjgl.util.vector.Vector3f;

public class CrossbowData extends GunData {

    public CrossbowData() {
        super("Crossbow", new Vector3f(), new Vector3f(), new Vector3f(1, 1, 1), 200, 5, 10, 2.5f, 3);
    }

    @Override
    public void perform(Entity owner) {

    }

}
