package org.anchor.client.engine.renderer.menu;

import org.lwjgl.util.vector.Vector3f;

public enum MenuItemStatus {

    ON(new Vector3f(0.75f, 0.75f, 0.15f)), DEFAULT(new Vector3f(0.75f, 0.75f, 0.75f));

    protected Vector3f colour;

    private MenuItemStatus(Vector3f colour) {
        this.colour = colour;
    }

    public Vector3f getColour() {
        return colour;
    }

}
