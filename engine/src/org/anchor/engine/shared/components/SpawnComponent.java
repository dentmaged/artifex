package org.anchor.engine.shared.components;

import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;

public class SpawnComponent implements IComponent {

    @Override
    public void precache(Entity entity) {

    }

    @Override
    public void spawn() {

    }

    @Override
    public void setValue(String key, String value) {

    }

    @Override
    public IComponent copy() {
        return new SpawnComponent();
    }

}
