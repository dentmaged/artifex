package org.anchor.client.engine.renderer.types;

public class ModelTexture {

    protected TextureRequest texture, normal, specular;
    protected int numberOfRows = 1;
    protected boolean culling, blending;

    protected float shineDamper = 1, reflectivity = 0;

    public ModelTexture(TextureRequest texture) {
        this(texture, true);
    }

    public ModelTexture(TextureRequest texture, boolean culling) {
        this(texture, culling, false);
    }

    public ModelTexture(TextureRequest texture, boolean culling, boolean blending) {
        this.texture = texture;
        this.culling = culling;
        this.blending = blending;
    }

    public int getId() {
        return texture.getTexture();
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

    public float getShineDamper() {
        return shineDamper;
    }

    public void setShineDamper(float shineDamper) {
        this.shineDamper = shineDamper;
    }

    public float getReflectivity() {
        return reflectivity;
    }

    public void setReflectivity(float reflectivity) {
        this.reflectivity = reflectivity;
    }

    public boolean isLoaded() {
        boolean normalLoaded = true;
        if (normal != null)
            normalLoaded = normal.isLoaded();

        boolean specularLoaded = true;
        if (specular != null)
            specularLoaded = specular.isLoaded();

        return texture.isLoaded() && normalLoaded && specularLoaded;
    }

}
