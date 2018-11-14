package org.anchor.client.engine.renderer.menu;

import org.anchor.client.engine.renderer.font.Text;

public abstract class MenuItem {

    protected String text;

    public MenuItem(String text) {
        this.text = text;
    }

    public abstract void run(Menu parent);

    public float renderExtra(Text text, float x, float y, float longest) {
        return 0;
    }

    public MenuItemStatus getStatus() {
        return MenuItemStatus.DEFAULT;
    }

    public String getText() {
        return text;
    }

}
