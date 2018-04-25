package org.anchor.client.engine.renderer.types;

import org.anchor.engine.common.utils.AABB;
import org.lwjgl.util.vector.Vector3f;

public class Mesh {

    protected int vao, firstVBO, vertexCount, dimensions;
    protected AABB aabb;
    protected Vector3f furthestVertex;

    public Mesh(int vao, int firstVBO, int vertexCount, int dimensions) {
        this.vao = vao;
        this.firstVBO = firstVBO;
        this.vertexCount = vertexCount;
        this.dimensions = dimensions;
        this.furthestVertex = new Vector3f();
    }

    public int getVAO() {
        return vao;
    }

    public int getFirstVBO() {
        return firstVBO;
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
