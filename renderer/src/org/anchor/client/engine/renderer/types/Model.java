package org.anchor.client.engine.renderer.types;

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

    public ModelTexture getTexture() {
        return texture;
    }

    public boolean isLoaded() {
        return mesh.isLoaded() && texture.isLoaded();
    }

}
