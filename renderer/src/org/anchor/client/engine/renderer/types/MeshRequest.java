package org.anchor.client.engine.renderer.types;

public class MeshRequest {

    private String name;
    private Mesh mesh;
    private MeshType type;

    public MeshRequest(String name) {
        this(name, MeshType.PLAIN);
    }

    public MeshRequest(String name, MeshType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public MeshType getType() {
        return type;
    }

    public boolean isLoaded() {
        return mesh != null;
    }

}
