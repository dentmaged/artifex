package org.anchor.engine.shared.entity;

import java.util.ArrayList;
import java.util.List;

public interface IComponent {

    public void spawn(Entity entity);

    default void update() {

    }

    default void updateFixed() {

    }

    public void setValue(String key, String value);

    public IComponent copy();

    default List<Class<? extends IComponent>> getDependencies() {
        return new ArrayList<Class<? extends IComponent>>();
    }

}
