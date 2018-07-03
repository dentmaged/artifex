package org.anchor.client.engine.renderer.types.texture;

public class ModelTexture {

    protected TextureRequest albedo, normal, metallic, roughness, ao, specular;
    protected int numberOfRows = 1;
    protected boolean culling, blending;

    public ModelTexture(TextureRequest texture) {
        this(texture, true);
    }

    public ModelTexture(TextureRequest texture, boolean culling) {
        this(texture, culling, false);
    }

    public ModelTexture(TextureRequest texture, boolean culling, boolean blending) {
        this.albedo = texture;
        this.culling = culling;
        this.blending = blending;
    }

    public int getId() {
        return albedo.getTexture();
    }

    public boolean isCullingEnabled() {
        return culling;
    }

    public void setCullingEnabled(boolean culling) {
        this.culling = culling;
    }

    public boolean isBlendingEnabled() {
        return blending;
    }

    public void setBlendingEnabled(boolean blending) {
        this.blending = blending;
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public void setNumberOfRows(int numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

    public int getNormalMap() {
        if (normal == null)
            return -1;

        return normal.getTexture();
    }

    public void setNormalMap(TextureRequest normal) {
        this.normal = normal;
    }

    public int getSpecularMap() {
        if (specular == null)
            return -1;

        return specular.getTexture();
    }

    public void setSpecularMap(TextureRequest specular) {
        this.specular = specular;
    }

    public int getMetallicMap() {
        if (metallic == null)
            return -1;

        return metallic.getTexture();
    }

    public void setMetallicMap(TextureRequest metallic) {
        this.metallic = metallic;
    }

    public int getRoughnessMap() {
        if (roughness == null)
            return -1;

        return roughness.getTexture();
    }

    public void setRoughnessMap(TextureRequest roughness) {
        this.roughness = roughness;
    }

    public int getAmbientOcclusionMap() {
        if (ao == null)
            return -1;

        return ao.getTexture();
    }

    public void setAmbientOcclusionMap(TextureRequest ao) {
        this.ao = ao;
    }

    public boolean isLoaded() {
        boolean normalLoaded = true;
        if (normal != null)
            normalLoaded = normal.isLoaded();

        boolean specularLoaded = true;
        if (specular != null)
            specularLoaded = specular.isLoaded();

        boolean metallicLoaded = true;
        if (metallic != null)
            metallicLoaded = metallic.isLoaded();

        boolean roughnessLoaded = true;
        if (roughness != null)
            roughnessLoaded = roughness.isLoaded();

        boolean aoLoaded = true;
        if (ao != null)
            aoLoaded = ao.isLoaded();

        return albedo.isLoaded() && normalLoaded && specularLoaded && metallicLoaded && roughnessLoaded && aoLoaded;
    }

    public String getName() {
        return albedo.getName();
    }

}
