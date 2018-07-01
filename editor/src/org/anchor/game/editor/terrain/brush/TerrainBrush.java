package org.anchor.game.editor.terrain.brush;

import org.anchor.game.client.types.ClientTerrain;
import org.anchor.game.editor.terrain.shape.Shape;
import org.lwjgl.util.vector.Vector3f;

public abstract class TerrainBrush {

    public abstract void perform(Shape shape, ClientTerrain terrain, Vector3f point, float radius, float scale, float strength);

    public abstract String getName();

}
