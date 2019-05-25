package org.anchor.game.editor.editableMesh.types;

import java.util.ArrayList;
import java.util.List;

import org.anchor.engine.common.utils.CoreMaths;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.editor.TransformableObject;
import org.anchor.game.editor.editableMesh.EditableMesh;
import org.anchor.game.editor.utils.MultiVector3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class Polygon implements TransformableObject, OpenGLData {

    public List<Vertex> vertices = new ArrayList<Vertex>();
    public List<Edge> edges = new ArrayList<Edge>();
    private EditableMesh mesh;
    private Vector3f scale = new Vector3f(1, 1, 1);

    public Polygon(EditableMesh mesh, List<Vertex> vertices) {
        this.mesh = mesh;
        this.vertices.addAll(vertices);
    }

    @Override
    public EditableMesh getMesh() {
        return mesh;
    }

    public Vector3f getNormal() {
        Vector3f normal = new Vector3f();

        // from http://www.iquilezles.org/www/articles/areas/areas.htm
        for (int i = 0; i < vertices.size(); i++)
            Vector3f.add(normal, Vector3f.cross(vertices.get(i).position, vertices.get((i + 1) % vertices.size()).position, null), normal);
        float length = normal.length();
        if (length == 0)
            return null;

        return VectorUtils.div(normal, length);
    }

    @Override
    public Vector3f getPosition() {
        Vector3f average = new Vector3f();
        List<Vector3f> vectors = new ArrayList<Vector3f>();
        for (Vertex vertex : vertices) {
            vectors.add(vertex.position);
            Vector3f.add(average, vertex.position, average);
        }

        return new MultiVector3f(vectors, VectorUtils.div(average, vertices.size()), 0); // ugly hack to allow polygon's vertices to be moved by gizmo
    }

    @Override
    public Vector3f getRotation() {
        return new Vector3f();
    }

    @Override
    public Vector3f getScale() {
        List<Vector3f> vectors = new ArrayList<Vector3f>();
        for (Vertex vertex : vertices)
            vectors.add(vertex.position);

        return new MultiVector3f(vectors, scale, 1);
    }

    @Override
    public Matrix4f getTransformationMatrix() {
        return Matrix4f.mul(mesh.getTransformationMatrix(), CoreMaths.createTransformationMatrix(getPosition(), getRotation(), getScale()), null);
    }

    @Override
    public String toString() {
        return String.format("Polygon[position=%s, normal=%s, vertices=%s, edges=%s]", getPosition(), getNormal(), vertices, edges);
    }

}
