package org.anchor.client.engine.renderer.types;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.types.texture.TextureRequest;
import org.lwjgl.opengl.GL11;

public class Material {

    protected TextureRequest albedo, normal, metallic, roughness, ao, specular;
    protected int numberOfRows = 1;
    protected boolean culling, blending;

    public Material() {
        this(null);
    }

    public Material(TextureRequest albedo) {
        this(albedo, true);
    }

    public Material(TextureRequest albedo, boolean culling) {
        this(albedo, culling, false);
    }

    public Material(TextureRequest albedo, boolean culling, boolean blending) {
        this.albedo = albedo;
        this.culling = culling;
        this.blending = blending;
    }

    public void bind() {
        Graphics.bind2DTexture(albedo, 0);
        Graphics.bind2DTexture(normal, 1);
        Graphics.bind2DTexture(specular, 2);
        Graphics.bind2DTexture(metallic, 3);
        Graphics.bind2DTexture(roughness, 4);
        Graphics.bind2DTexture(ao, 5);

        if (culling) {
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glCullFace(GL11.GL_BACK);
        } else {
            GL11.glDisable(GL11.GL_CULL_FACE);
        }
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

    public void setAlbedo(TextureRequest albedo) {
        this.albedo = albedo;
    }

    public String getAlbedoName() {
        return albedo != null ? albedo.getName() : null;
    }

    public void setNormalMap(TextureRequest normal) {
        this.normal = normal;
    }

    public String getNormalMapName() {
        return normal != null ? normal.getName() : null;
    }

    public boolean hasNormalMap() {
        return normal != null;
    }

    public void setSpecularMap(TextureRequest specular) {
        this.specular = specular;
    }

    public String getSpecularMapName() {
        return specular != null ? specular.getName() : null;
    }

    public void setMetallicMap(TextureRequest metallic) {
        this.metallic = metallic;
    }

    public String getMetallicMapName() {
        return metallic != null ? metallic.getName() : null;
    }

    public void setRoughnessMap(TextureRequest roughness) {
        this.roughness = roughness;
    }

    public String getRoughnessMapName() {
        return roughness != null ? roughness.getName() : null;
    }

    public void setAmbientOcclusionMap(TextureRequest ao) {
        this.ao = ao;
    }

    public boolean hasAmbientOcclusionMap() {
        return ao != null;
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

}
