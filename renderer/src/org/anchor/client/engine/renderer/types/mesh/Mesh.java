package org.anchor.client.engine.renderer.types.mesh;

import org.anchor.engine.common.utils.AABB;
import org.lwjgl.util.vector.Vector3f;

public class Mesh {

    protected int vao, vbos[], vertexCount, dimensions;
    protected AABB aabb;
    protected Vector3f furthestVertex;

    public Mesh(int vao, int[] vbos, int vertexCount, int dimensions) {
        this.vao = vao;
        this.vbos = vbos;
        this.vertexCount = vertexCount;
        this.dimensions = dimensions;
        this.furthestVertex = new Vector3f();
    }

    public int getVAO() {
        return vao;
    }

    public int getVBO(int attribute) {
        return vbos[attribute + 1];
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getDimensions() {
        return dimensions;
    }

    public AABB getAABB() {
        return aabb;
    }

    public void setAABB(AABB aabb) {
        this.aabb = aabb;
    }

    public Vector3f getFurthestVertex() {
        return furthestVertex;
    }

    public void setFurthestVertex(Vector3f furthestVertex) {
        this.furthestVertex = furthestVertex;
    }

}
