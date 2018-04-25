package org.anchor.engine.shared.components;

import org.anchor.engine.shared.entity.Entity;

public class SpawnComponent implements IComponent {

    @Override
    public void spawn(Entity entity) {

    }

    @Override
    public void setValue(String key, String value) {

    }

    @Override
    public IComponent copy() {
        return new SpawnComponent();
    }

}
