package org.anchor.game.editor.utils.raycast;

import org.anchor.engine.common.utils.AABB;
import org.anchor.engine.shared.editor.TransformableObject;

public class PossibleRaycast {

    private TransformableObject object;
    private AABB aabb;

    public PossibleRaycast(TransformableObject object, AABB aabb) {
        this.object = object;
        this.aabb = aabb;
    }

    public TransformableObject getObject() {
        return object;
    }
    
    public AABB getAABB() {
        return aabb;
    }

}
