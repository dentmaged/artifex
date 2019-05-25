package org.anchor.game.editor.utils;

import java.util.ArrayList;
import java.util.List;

import org.anchor.engine.common.utils.VectorUtils;
import org.lwjgl.util.vector.ReadableVector3f;
import org.lwjgl.util.vector.Vector3f;

public class MultiVector3f extends Vector3f {

    private static final long serialVersionUID = -2345458950819716978L;

    public List<Vector3f> elements = new ArrayList<Vector3f>();
    public Vector3f clone;
    public int mode;

    public MultiVector3f(List<Vector3f> elements, Vector3f clone, int mode) {
        this.elements.addAll(elements);

        this.x = clone.x;
        this.y = clone.y;
        this.z = clone.z;
        this.clone = clone;
        this.mode = mode;
    }

    @Override
    public Vector3f set(ReadableVector3f target) {
        if (mode == 0) {
            Vector3f change = new Vector3f(target.getX() - x, target.getY() - y, target.getZ() - z);
            for (Vector3f element : elements)
                Vector3f.add(element, change, element);
        } else if (mode == 1) {
            Vector3f change = new Vector3f(target.getX() / x, target.getY() / y, target.getZ() / z);
            for (Vector3f element : elements)
                element.set(VectorUtils.mul(element, change));
        }

        clone.set(target);
        return super.set(target);
    }

}
