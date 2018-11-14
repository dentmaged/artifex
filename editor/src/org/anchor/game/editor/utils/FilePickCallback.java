package org.anchor.game.editor.utils;

import java.awt.Component;

import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.terrain.Terrain;

public abstract class FilePickCallback implements FileCallback {

    public Component component;

    public void onEntitySelect(Entity previous, Entity current) {

    }

    public void onTerrainSelect(Terrain previous, Terrain current) {

    }

}
