package org.anchor.game.editor.terrain.brush;

import org.anchor.engine.common.utils.Mathf;
import org.anchor.game.client.types.ClientTerrain;
import org.anchor.game.editor.terrain.shape.Shape;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

public class SetHeightBrush extends TerrainBrush {

    private static float target;

    @Override
    public void perform(Shape shape, ClientTerrain terrain, Vector3f point, float radius, float scale, float strength) {
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            target = point.y;

            return;
        }

        for (float x = -radius; x < radius; x++) {
            for (float z = -radius; z < radius; z++) {
                float length = Mathf.sqrt(x * x + z * z);
                if (length > radius)
                    continue;

                if (shape.getStrengthAtPixel(x, z, radius) < 1)
                    continue;

                terrain.setHeightAtPoint(point.x + x * scale, point.z + z * scale, target);
            }
        }
    }

    @Override
    public String getName() {
        return "Set";
    }

}
