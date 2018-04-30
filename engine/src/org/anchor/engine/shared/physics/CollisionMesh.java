package org.anchor.engine.shared.physics;

import java.util.ArrayList;
import java.util.List;

import org.anchor.engine.common.utils.AABB;
import org.anchor.engine.common.utils.VectorUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class CollisionMesh {

    protected List<Vector4f> vertices, normals;
    protected int[] indices;
    protected AABB aabb;

    public CollisionMesh(List<Vector3f> vertices, List<Vector3f> normals, List<Integer> indices) {
        this.vertices = new ArrayList<Vector4f>();
        this.normals = new ArrayList<Vector4f>();
        this.aabb = AABB.generateAABB(vertices);

        for (Vector3f vertex : vertices)
            this.vertices.add(new Vector4f(vertex.x, vertex.y, vertex.z, 1));

        for (Vector3f normal : normals)
            this.normals.add(new Vector4f(normal.x, normal.y, normal.z, 0));

        this.indices = new int[indices.size()];
        for (int i = 0; i < this.indices.length; i++)
            this.indices[i] = indices.get(i);
    }

    public List<Vector3f> getVertices(Matrix4f transformationMatrix) {
        List<Vector3f> transformedVertices = new ArrayList<Vector3f>();

        for (Vector4f vertex : vertices)
            transformedVertices.add(new Vector3f(Matrix4f.transform(transformationMatrix, vertex, null)));

        return transformedVertices;
    }

    public List<Vector3f> getNormals(Matrix4f transformationMatrix) {
        List<Vector3f> transformedNormals = new ArrayList<Vector3f>();
        Matrix4f normalMatrix = Matrix4f.transpose(Matrix4f.invert(transformationMatrix, null), null);

        for (Vector4f normal : normals)
            transformedNormals.add(new Vector3f(Matrix4f.transform(normalMatrix, normal, null)));

        return transformedNormals;
    }

    public AABB getAABB(Matrix4f transformationMatrix) {
        return AABB.generateAABB(aabb, transformationMatrix);
    }

    public Vector3f raycastAABB(Matrix4f transformationMatrix, Vector3f origin, Vector3f ray) {
        return getAABB(transformationMatrix).raycast(origin, ray);
    }

    public float raycastDistanceAABB(Matrix4f transformationMatrix, Vector3f origin, Vector3f ray) {
        return getAABB(transformationMatrix).raycastDistance(origin, ray);
    }

    public Vector3f raycast(Matrix4f transformationMatrix, Vector3f origin, Vector3f ray) {
        float distance = raycastDistance(transformationMatrix, origin, ray);
        if (distance == -1)
            return null;

        return Vector3f.add(origin, VectorUtils.mul(ray, distance), null);
    }

    public float raycastDistance(Matrix4f transformationMatrix, Vector3f origin, Vector3f ray) {
        Vector3f aabbPoint = raycastAABB(transformationMatrix, origin, ray);
        if (aabbPoint == null)
            return -1;

        List<Vector3f> vertices = getVertices(transformationMatrix);
        for (int i = 0; i < indices.length; i += 3) {
            Vector3f p0 = vertices.get(indices[i] - 1);
            Vector3f p1 = vertices.get(indices[i + 1] - 1);
            Vector3f p2 = vertices.get(indices[i + 2] - 1);

            Vector3f e1 = Vector3f.sub(p1, p0, null);
            Vector3f e2 = Vector3f.sub(p2, p0, null);

            Vector3f h = Vector3f.cross(ray, e2, null);
            float a = Vector3f.dot(e1, h);

            if (a > -0.00001f && a < 0.00001f)
                continue;

            if (a < 0)
                continue;

            float f = 1f / a;
            Vector3f s = Vector3f.sub(origin, p0, null);
            float u = f * Vector3f.dot(s, h);

            if (u < 0 || u > 1)
                continue;

            Vector3f q = Vector3f.cross(s, e1, null);
            float v = f * Vector3f.dot(ray, q);

            if (v < 0 || u + v > 1)
                continue;

            float distance = f * Vector3f.dot(e2, q);
            if (distance > 1e-6)
                return distance;
        }

        return -1;
    }

}
