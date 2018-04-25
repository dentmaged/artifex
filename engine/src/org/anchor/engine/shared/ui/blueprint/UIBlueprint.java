package org.anchor.engine.shared.ui.blueprint;

import java.awt.Component;
import java.util.List;

import org.anchor.engine.shared.ui.listener.UIListener;

public abstract class UIBlueprint {

    protected UIListener listener;

    public UIBlueprint(UIListener listener) {
        this.listener = listener;
    }

    public UIListener getListener() {
        return listener;
    }

    public abstract List<Component> build(int x, int y, int width);

    public abstract int getHeight();

}
