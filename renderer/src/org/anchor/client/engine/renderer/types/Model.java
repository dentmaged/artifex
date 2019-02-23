package org.anchor.client.engine.renderer.types;

import org.anchor.client.engine.renderer.types.mesh.Mesh;
import org.anchor.client.engine.renderer.types.mesh.MeshRequest;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

public class Model {

    protected MeshRequest mesh;

    public Model(MeshRequest mesh) {
        this.mesh = mesh;
    }

    public Mesh getMesh() {
        return mesh.getMesh();
    }

    public void setMesh(MeshRequest mesh) {
        this.mesh = mesh;
    }

    public String getName() {
        return mesh.getName();
    }

    public boolean isLoaded() {
        return mesh.isLoaded();
    }

    public void unload() {
        if (!isLoaded())
            return;

        GL30.glDeleteVertexArrays(mesh.getMesh().getVAO());
        for (int i = 0; i < mesh.getMesh().getDimensions(); i++)
            GL15.glDeleteBuffers(mesh.getMesh().getFirstVBO() + i);
    }

}
