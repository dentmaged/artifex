package org.anchor.engine.shared.ui.listener;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import org.anchor.engine.shared.editor.TransformableObject;
import org.anchor.engine.shared.terrain.Terrain;

public class UIListener {

    protected Component component;

    private static List<UIListener> listeners = new ArrayList<UIListener>();

    public UIListener() {
        this(true);
    }

    public UIListener(boolean add) {
        if (add)
            listeners.add(this);
    }

    public void onSelectionChange(TransformableObject object, boolean added) {

    }

    public void onTerrainSelect(Terrain previous, Terrain current) {

    }

    protected int parseInt(String input) {
        return parseInt(input, 0);
    }

    protected float parseFloat(String input) {
        return parseFloat(input, 0);
    }

    protected int parseInt(String input, int backup) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
        }

        return backup;
    }

    protected float parseFloat(String input, float backup) {
        try {
            return Float.parseFloat(input);
        } catch (NumberFormatException e) {
        }

        return backup;
    }

    public String toNonNullString(String input) {
        return input == null ? "" : input;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    protected void removeSubpanel() {
        Container parent = component.getParent().getParent().getParent();
        Container grandparent = parent.getParent();

        grandparent.remove(parent);
        grandparent.revalidate();
        grandparent.repaint();
    }

    public static void selectionChange(TransformableObject object, boolean added) {
        for (UIListener listener : listeners)
            listener.onSelectionChange(object, added);
    }

    public static void terrainSelected(Terrain previous, Terrain current) {
        for (UIListener listener : listeners)
            listener.onTerrainSelect(previous, current);
    }

}
