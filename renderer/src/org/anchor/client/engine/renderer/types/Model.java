package org.anchor.client.engine.renderer.types;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

public class Model {

    protected MeshRequest mesh;
    protected ModelTexture texture;

    public Model(MeshRequest mesh, TextureRequest texture) {
        this(mesh, new ModelTexture(texture));
    }

    public Model(MeshRequest mesh, ModelTexture texture) {
        this.mesh = mesh;
        this.texture = texture;
    }

    public Mesh getMesh() {
        return mesh.getMesh();
    }

    public String getName() {
        return mesh.getName();
    }

    public ModelTexture getTexture() {
        return texture;
    }

    public boolean isLoaded() {
        return mesh.isLoaded() && texture.isLoaded();
    }

    public void unload() {
        if (!isLoaded())
            return;

        GL30.glDeleteVertexArrays(mesh.getMesh().getVAO());
        for (int i = 0; i < mesh.getMesh().getDimensions(); i++)
            GL15.glDeleteBuffers(mesh.getMesh().getFirstVBO() + i);
    }

}
