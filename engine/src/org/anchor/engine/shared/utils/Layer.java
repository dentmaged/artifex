package org.anchor.engine.shared.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.anchor.engine.shared.entity.Entity;

public class Layer {

    private String name;
    private List<Entity> entities;
    private Color colour;
    private boolean pickable, visible;

    public Layer(String name, Color colour) {
        this(name, colour, true, true);
    }

    public Layer(String name, Color colour, boolean pickable, boolean visible) {
        this.name = name;
        this.entities = new ArrayList<Entity>();
        this.colour = colour;
        this.pickable = pickable;
        this.visible = visible;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public Color getColour() {
        return colour;
    }

    public void setColour(Color colour) {
        this.colour = colour;
    }

    public boolean isPickable() {
        return pickable;
    }

    public void setPickable(boolean pickable) {
        this.pickable = pickable;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

}
