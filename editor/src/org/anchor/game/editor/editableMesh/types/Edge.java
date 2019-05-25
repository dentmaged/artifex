package org.anchor.game.editor.editableMesh.types;

import org.anchor.engine.common.utils.CoreMaths;
import org.anchor.engine.shared.editor.TransformableObject;
import org.anchor.game.editor.editableMesh.EditableMesh;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class Edge implements TransformableObject, OpenGLData {

    public Vector3f position;
    private EditableMesh mesh;

    public Edge(EditableMesh mesh) {
        this.mesh = mesh;
    }

    @Override
    public EditableMesh getMesh() {
        return mesh;
    }

    @Override
    public Vector3f getPosition() {
        return position;
    }

    @Override
    public Vector3f getRotation() {
        return new Vector3f();
    }

    @Override
    public Vector3f getScale() {
        return new Vector3f(1, 1, 1);
    }

    @Override
    public Matrix4f getTransformationMatrix() {
        return Matrix4f.mul(mesh.getTransformationMatrix(), CoreMaths.createTransformationMatrix(position, getRotation(), getScale()), null);
    }

}
