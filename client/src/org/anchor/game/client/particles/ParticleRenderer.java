package org.anchor.game.client.particles;

import java.nio.FloatBuffer;
import java.util.List;

import org.anchor.client.engine.renderer.Graphics;
import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.Renderer;
import org.anchor.client.engine.renderer.types.mesh.Mesh;
import org.anchor.engine.common.utils.Mathf;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.game.client.GameClient;
import org.anchor.game.client.components.ParticleSystemComponent;
import org.anchor.game.client.shaders.ParticleShader;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL40;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class ParticleRenderer {

    private static Mesh mesh;
    private static int vbo;
    private static FloatBuffer buffer;

    private static Matrix4f transformationMatrix = new Matrix4f();
    private static Vector4f offset = new Vector4f();

    public static final int MAX_PARTICLES = 10000;
    public static final int DATA_LENGTH = 21;

    public static void init() {
        mesh = Loader.getInstance().loadToVAO(new float[] {
                -1, 1, -1, -1, 1, 1, 1, -1
        }, 2);
        vbo = Loader.getInstance().createEmptyVBO(MAX_PARTICLES * DATA_LENGTH);
        buffer = BufferUtils.createFloatBuffer(MAX_PARTICLES * DATA_LENGTH);

        // projectionViewTransformationMatrix
        Loader.getInstance().addInstancedAttribute(mesh.getVAO(), vbo, 1, 4, DATA_LENGTH, 0);
        Loader.getInstance().addInstancedAttribute(mesh.getVAO(), vbo, 2, 4, DATA_LENGTH, 4);
        Loader.getInstance().addInstancedAttribute(mesh.getVAO(), vbo, 3, 4, DATA_LENGTH, 8);
        Loader.getInstance().addInstancedAttribute(mesh.getVAO(), vbo, 4, 4, DATA_LENGTH, 12);

        // offsets
        Loader.getInstance().addInstancedAttribute(mesh.getVAO(), vbo, 5, 4, DATA_LENGTH, 16);

        // blend
        Loader.getInstance().addInstancedAttribute(mesh.getVAO(), vbo, 6, 1, DATA_LENGTH, 20);
    }

    public static void render(List<ParticleSystemComponent> systems) {
        GL30.glColorMaski(1, false, false, false, false);
        GL30.glColorMaski(2, false, false, false, false);

        GL30.glBindVertexArray(mesh.getVAO());
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        GL20.glEnableVertexAttribArray(3);
        GL20.glEnableVertexAttribArray(4);
        GL20.glEnableVertexAttribArray(5);
        GL20.glEnableVertexAttribArray(6);

        ParticleShader shader = ParticleShader.getInstance();
        shader.start();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDepthMask(false);
        for (ParticleSystemComponent system : systems) {
            if (system.getTexture() == null)
                continue;

            Graphics.bind2DTexture(system.getTexture().getTexture(), 0);
            GL40.glBlendFunci(0, GL11.GL_SRC_ALPHA, system.getTexture().usesAdditiveBlending() ? GL11.GL_ONE : GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL40.glBlendFunci(3, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            sort(system.getParticles());
            float[] data = new float[system.getParticles().size() * DATA_LENGTH];
            int pointer = 0;

            for (Particle particle : system.getParticles()) {
                transformationMatrix.setIdentity();
                Matrix4f viewMatrix = GameClient.getPlayer().getComponent(LivingComponent.class).getViewMatrix();
                Matrix4f.translate(particle.getPosition(), transformationMatrix, transformationMatrix);

                transformationMatrix.m00 = viewMatrix.m00;
                transformationMatrix.m01 = viewMatrix.m10;
                transformationMatrix.m02 = viewMatrix.m20;

                transformationMatrix.m10 = viewMatrix.m01;
                transformationMatrix.m11 = viewMatrix.m11;
                transformationMatrix.m12 = viewMatrix.m21;

                transformationMatrix.m20 = viewMatrix.m02;
                transformationMatrix.m21 = viewMatrix.m12;
                transformationMatrix.m22 = viewMatrix.m22;

                Matrix4f.rotate(Mathf.toRadians(particle.getRotation()), new Vector3f(0, 0, 1), transformationMatrix, transformationMatrix);
                Matrix4f.scale(new Vector3f(particle.getSize(), particle.getSize(), particle.getSize()), transformationMatrix, transformationMatrix);
                transformationMatrix = Matrix4f.mul(Renderer.getProjectionMatrix(), Matrix4f.mul(viewMatrix, transformationMatrix, null), null);

                data[pointer++] = transformationMatrix.m00;
                data[pointer++] = transformationMatrix.m01;
                data[pointer++] = transformationMatrix.m02;
                data[pointer++] = transformationMatrix.m03;
                data[pointer++] = transformationMatrix.m10;
                data[pointer++] = transformationMatrix.m11;
                data[pointer++] = transformationMatrix.m12;
                data[pointer++] = transformationMatrix.m13;
                data[pointer++] = transformationMatrix.m20;
                data[pointer++] = transformationMatrix.m21;
                data[pointer++] = transformationMatrix.m22;
                data[pointer++] = transformationMatrix.m23;
                data[pointer++] = transformationMatrix.m30;
                data[pointer++] = transformationMatrix.m31;
                data[pointer++] = transformationMatrix.m32;
                data[pointer++] = transformationMatrix.m33;

                offset.set(particle.getTcOffset1().x, particle.getTcOffset1().y, particle.getTcOffset2().x, particle.getTcOffset2().y);
                data[pointer++] = offset.x;
                data[pointer++] = offset.y;
                data[pointer++] = offset.z;
                data[pointer++] = offset.w;

                data[pointer++] = particle.getBlendFactor();
            }

            Loader.getInstance().updateVBO(vbo, data, buffer);
            shader.loadRows(system.getTexture().getRows());
            GL31.glDrawArraysInstanced(GL11.GL_TRIANGLE_STRIP, 0, 4, system.getParticles().size());
        }
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);

        shader.stop();

        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL20.glDisableVertexAttribArray(3);
        GL20.glDisableVertexAttribArray(4);
        GL20.glDisableVertexAttribArray(5);
        GL20.glDisableVertexAttribArray(6);
        GL30.glBindVertexArray(0);

        GL30.glColorMaski(2, true, true, true, true);
        GL30.glColorMaski(1, true, true, true, true);
    }

    private static void sort(List<Particle> list) {
        for (int i = 1; i < list.size(); i++) {
            Particle item = list.get(i);

            if (item.getDistance() > list.get(i - 1).getDistance())
                perform(list, i);
        }
    }

    private static void perform(List<Particle> list, int i) {
        Particle item = list.get(i);
        int attemptPos = i - 1;

        while (attemptPos != 0 && list.get(attemptPos - 1).getDistance() < item.getDistance())
            attemptPos--;

        list.remove(i);
        list.add(attemptPos, item);
    }
}
