package org.anchor.client.engine.renderer.shadows;

import org.anchor.client.engine.renderer.types.Framebuffer;
import org.anchor.client.engine.renderer.types.Light;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class Shadows {

    public static final int SHADOW_MAP_SIZE = 2048;
    private static Framebuffer shadowFBO;
    private ShadowFrustum frustum;

    private Matrix4f projectionMatrix = new Matrix4f();
    private Matrix4f lightViewMatrix = new Matrix4f();
    private Matrix4f projectionViewMatrix = new Matrix4f();
    private Matrix4f offset = createOffset();

    private Light sun;

    public Shadows(Light sun) {
        if (shadowFBO == null)
            shadowFBO = new Framebuffer(SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, Framebuffer.DEPTH_TEXTURE);
        frustum = new ShadowFrustum(sun, lightViewMatrix);

        this.sun = sun;
    }

    public boolean start(Vector3f position, float pitch, float yaw) {
        if (sun == null)
            return false;

        frustum.update(position, pitch, yaw);

        Vector3f direction = new Vector3f(sun.getPosition());
        direction.negate();
        if (sun.getPosition().lengthSquared() > 0)
            direction.normalise();

        prepare(direction, frustum);

        return true;
    }

    public void stop() {
        shadowFBO.unbindFrameBuffer();
    }

    public Matrix4f getToShadowMapSpaceMatrix() {
        return Matrix4f.mul(offset, projectionViewMatrix, null);
    }

    public Matrix4f getProjectionViewMatrix() {
        return projectionViewMatrix;
    }

    public ShadowFrustum getShadowBox() {
        return frustum;
    }

    public void shutdown() {
        shadowFBO.shutdown();
    }

    public int getPCFShadowMap() {
        return shadowFBO.getDepthTexture();
    }

    public void setSun(Light sun) {
        this.sun = sun;

        frustum.setSun(sun);
    }

    private void prepare(Vector3f lightDirection, ShadowFrustum box) {
        updateOrthoProjectionMatrix(box.getWidth(), box.getHeight(), box.getLength());
        updateLightViewMatrix(lightDirection, box.getCenter());

        Matrix4f.mul(projectionMatrix, lightViewMatrix, projectionViewMatrix);
        shadowFBO.bindFrameBuffer();

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    private void updateLightViewMatrix(Vector3f direction, Vector3f center) {
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
