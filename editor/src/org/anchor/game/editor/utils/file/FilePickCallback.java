package org.anchor.game.editor.utils.file;

import java.awt.Component;

import org.anchor.engine.shared.editor.TransformableObject;
import org.anchor.engine.shared.terrain.Terrain;

public abstract class FilePickCallback implements FileCallback {

    public Component component;

    public void onSelectionChange(TransformableObject object, boolean added) {

    }

    public void onTerrainSelect(Terrain previous, Terrain current) {

    }

}
