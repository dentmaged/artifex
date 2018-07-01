package org.anchor.game.editor.terrain.brush;

import org.anchor.game.client.types.ClientTerrain;
import org.anchor.game.editor.terrain.shape.Shape;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

public class IncreaseDecreaseHeightBrush extends TerrainBrush {

    @Override
    public void perform(Shape shape, ClientTerrain terrain, Vector3f point, float radius, float scale, float strength) {
        float multiply = strength;
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
            multiply = -multiply;

        for (float x = -radius; x < radius; x++)
            for (float z = -radius; z < radius; z++)
                terrain.increaseHeightAtPoint(point.x + x * scale, point.z + z * scale, multiply * shape.getStrengthAtPixel(x, z, radius));
    }

    @Override
    public String getName() {
        return "Up/Down";
    }

}
