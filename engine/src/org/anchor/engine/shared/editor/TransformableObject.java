package org.anchor.engine.shared.editor;

import org.lwjgl.util.vector.Vector3f;

public interface TransformableObject {

    public Vector3f getPosition();

    public Vector3f getRotation();

    public Vector3f getScale();

}
