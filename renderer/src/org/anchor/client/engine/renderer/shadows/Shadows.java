package org.anchor.client.engine.renderer.shadows;

import org.anchor.client.engine.renderer.Settings;
import org.anchor.client.engine.renderer.blur.Blur;
import org.anchor.client.engine.renderer.types.Framebuffer;
import org.anchor.client.engine.renderer.types.ImageFormat;
import org.anchor.client.engine.renderer.types.light.Light;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class Shadows {

    private static Framebuffer[] shadowFBOs;
    private static Blur vsm;
    private ShadowFrustum[] frustums;

    private Matrix4f lightViewMatrices[];
    private Matrix4f projectionViewMatrices[];
    private Matrix4f toShadowMapSpaceMatrices[];

    private Matrix4f projectionMatrix = new Matrix4f();
    private Matrix4f offset = createOffset();

    private Light sun;

    public Shadows(Light sun) {
        if (shadowFBOs == null) {
            shadowFBOs = new Framebuffer[Settings.shadowSplits];
            for (int i = 0; i < shadowFBOs.length; i++)
                shadowFBOs[i] = new Framebuffer(Settings.shadowResolution, Settings.shadowResolution, Framebuffer.NONE, ImageFormat.RG32F);
            vsm = new Blur(Settings.shadowResolution, Settings.shadowResolution, ImageFormat.RG32F);
        }

        lightViewMatrices = new Matrix4f[Settings.shadowSplits];
        for (int i = 0; i < lightViewMatrices.length; i++)
            lightViewMatrices[i] = new Matrix4f();

        projectionViewMatrices = new Matrix4f[Settings.shadowSplits];
        for (int i = 0; i < projectionViewMatrices.length; i++)
            projectionViewMatrices[i] = new Matrix4f();

        toShadowMapSpaceMatrices = new Matrix4f[Settings.shadowSplits];
        for (int i = 0; i < toShadowMapSpaceMatrices.length; i++)
            toShadowMapSpaceMatrices[i] = new Matrix4f();

        frustums = new ShadowFrustum[Settings.shadowSplits];
        for (int i = 0; i < frustums.length; i++)
            frustums[i] = new ShadowFrustum(lightViewMatrices[0], Settings.shadowExtents[i]);

        this.sun = sun;
    }

    public boolean start(Matrix4f inverseViewMatrix, Vector3f position, float pitch, float yaw) {
        if (sun == null)
            return false;

        for (ShadowFrustum frustum : frustums)
            frustum.update(position, pitch, yaw);

        Vector3f direction = new Vector3f(sun.getDirection());
        direction.negate();

        for (int i = 0; i < Settings.shadowSplits; i++)
            prepare(inverseViewMatrix, direction, position, i);
        GL11.glClearColor(1, 1, 1, 1);

        return true;
    }

    public void bind(int shadow) {
        shadowFBOs[shadow].bindFramebuffer();

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    public void finish() {
        vsm.perform(shadowFBOs[0].getColourTexture());
        GL11.glClearColor(Settings.clearR, Settings.clearG, Settings.clearB, 1);
    }

    public Matrix4f getToShadowMapSpaceMatrix(int shadow) {
        return toShadowMapSpaceMatrices[shadow];
    }

    public Matrix4f getProjectionViewMatrix(int shadow) {
        return projectionViewMatrices[shadow];
    }

    public void shutdown() {
        for (Framebuffer shadowFBO : shadowFBOs)
            shadowFBO.shutdown();
    }

    public int getShadowMap(int map) {
        if (map == 0)
            return vsm.getOutputFBO().getColourTexture();

        return shadowFBOs[map].getColourTexture();
    }

    public float getExtents(int shadow) {
        return frustums[shadow].getFarPlane() * 0.5f;
    }

    public void setSun(Light sun) {
        this.sun = sun;
    }

    private void prepare(Matrix4f inverseViewMatrix, Vector3f lightDirection, Vector3f position, int shadow) {
        ShadowFrustum box = frustums[shadow];
        updateOrthoProjectionMatrix(box.getWidth(), box.getHeight(), box.getLength());
        updateLightViewMatrix(lightViewMatrices[shadow], lightDirection, box.getCenter());

        Matrix4f.mul(projectionMatrix, lightViewMatrices[shadow], projectionViewMatrices[shadow]);
        Matrix4f.mul(Matrix4f.mul(offset, projectionViewMatrices[shadow], null), inverseViewMatrix, toShadowMapSpaceMatrices[shadow]);
    }

    private void updateLightViewMatrix(Matrix4f lightViewMatrix, Vector3f direction, Vector3f center) {
        center.negate();
        lightViewMatrix.setIdentity();

        float pitch = (float) Math.acos(new Vector2f(direction.x, direction.z).length());
        Matrix4f.rotate(pitch, new Vector3f(1, 0, 0), lightViewMatrix, lightViewMatrix);

        float yaw = (float) Math.toDegrees(((float) Math.atan(direction.x / direction.z)));
        Matrix4f.rotate((float) -Math.toRadians(direction.z > 0 ? yaw - 180 : yaw), new Vector3f(0, 1, 0), lightViewMatrix, lightViewMatrix);
        Matrix4f.translate(center, lightViewMatrix, lightViewMatrix);
    }

    private void updateOrthoProjectionMatrix(float width, float height, float length) {
        projectionMatrix.setIdentity();

        projectionMatrix.m00 = 2f / width;
        projectionMatrix.m11 = 2f / height;

        projectionMatrix.m22 = -2f / length;
        projectionMatrix.m33 = 1;
    }

    private static Matrix4f createOffset() {
        Matrix4f offset = new Matrix4f();

        Matrix4f.translate(new Vector3f(0.5f, 0.5f, 0.5f), offset, offset);
        Matrix4f.scale(new Vector3f(0.5f, 0.5f, 0.5f), offset, offset);

        return offset;
    }

}
