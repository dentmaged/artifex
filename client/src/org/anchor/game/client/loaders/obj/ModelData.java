package org.anchor.game.client.loaders.obj;

import org.lwjgl.util.vector.Vector3f;

public class ModelData {

    private float[] vertices, textureCoords, normals, tangents;
    private int[] indices;
    private Vector3f furthestVertex;

    public ModelData(float[] vertices, float[] textureCoords, float[] normals, float[] tangents, int[] indices, Vector3f furthestVertex) {
        this.vertices = vertices;
        this.textureCoords = textureCoords;
        this.normals = normals;
        this.indices = indices;
        this.tangents = tangents;
        this.furthestVertex = furthestVertex;
    }

    public float[] getVertices() {
        return vertices;
    }

    public float[] getTextureCoords() {
        return textureCoords;
    }

    public float[] getTangents() {
        return tangents;
    }

    public float[] getNormals() {
        return normals;
    }

    public int[] getIndices() {
        return indices;
    }

    public Vector3f getFurthestVertex() {
        return furthestVertex;
    }

    public void setFurthestVertex(Vector3f furthestVertex) {
        this.furthestVertex = furthestVertex;
    }

}
