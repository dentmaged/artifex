package org.anchor.game.editor.gizmo;

import org.anchor.engine.common.utils.CoreMaths;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.GameClient;
import org.anchor.game.client.utils.KeyboardUtils;
import org.anchor.game.editor.GameEditor;
import org.anchor.game.editor.gizmo.types.RotateGizmo;
import org.anchor.game.editor.gizmo.types.ScaleGizmo;
import org.anchor.game.editor.gizmo.types.SelectGizmo;
import org.anchor.game.editor.gizmo.types.TranslateGizmo;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class GizmoRenderer {

    private static GizmoShader shader = GizmoShader.getInstance();

    private static Vector3f x = new Vector3f(1, 0, 0);
    private static Vector3f y = new Vector3f(0, 1, 0);
    private static Vector3f z = new Vector3f(0, 0, 1);

    private static Gizmo[] gizmos = {
            new SelectGizmo(), new TranslateGizmo(), new RotateGizmo(), new ScaleGizmo()
    };

    protected Matrix4f xMatrix, yMatrix, zMatrix;
    protected boolean xAxis, yAxis, zAxis, dragging, copied;
    protected int startMouseX, startMouseY;
    protected Vector3f axis, axisOffset, original, scale = new Vector3f();
    protected GameEditor editor;

    public GizmoRenderer(GameEditor editor) {
        this.editor = editor;
    }

    public boolean update(Entity entity, Vector3f ray) {
        Vector3f position = new Vector3f(Matrix4f.transform(entity.getTransformationMatrix(), new Vector4f(0, 0, 0, 1), null));
        float distance = Vector3f.sub(GameClient.getPlayer().getPosition(), position, null).length();
        distance *= 0.2f;
        scale.set(distance, distance, distance);

        Gizmo gizmo = gizmos[editor.getMode()];
        Vector3f origin = GameClient.getPlayer().getComponent(LivingComponent.class).getEyePosition();

        xMatrix = CoreMaths.createTransformationMatrix(position, gizmo.getXRotation(entity.getRotation(), editor.getTransformationMode()), scale);
        yMatrix = CoreMaths.createTransformationMatrix(position, gizmo.getYRotation(entity.getRotation(), editor.getTransformationMode()), scale);
        zMatrix = CoreMaths.createTransformationMatrix(position, gizmo.getZRotation(entity.getRotation(), editor.getTransformationMode()), scale);

        Vector3f xAxisOffset = gizmo.intersection(xMatrix, origin, ray, position, gizmo.getXRotation(entity.getRotation(), editor.getTransformationMode()), scale);
        Vector3f yAxisOffset = gizmo.intersection(yMatrix, origin, ray, position, gizmo.getYRotation(entity.getRotation(), editor.getTransformationMode()), scale);
        Vector3f zAxisOffset = gizmo.intersection(zMatrix, origin, ray, position, gizmo.getZRotation(entity.getRotation(), editor.getTransformationMode()), scale);

        xAxis = xAxisOffset != null;
        yAxis = yAxisOffset != null;
        zAxis = zAxisOffset != null;

        if ((xAxis || yAxis || zAxis) && !dragging && Mouse.isButtonDown(0)) {
            if (KeyboardUtils.isKeyDown(Keyboard.KEY_LSHIFT)) {
                entity = entity.copy();
                editor.addEntity(entity);

                copied = true;
            }

            startMouseX = Mouse.getX();
            startMouseY = Mouse.getY();

            original = new Vector3f(gizmo.getVector(entity));
            dragging = true;

            if (xAxis) {
                axis = x;
                axisOffset = Vector3f.sub(xAxisOffset, position, null);
            } else if (yAxis) {
                axis = y;
                axisOffset = Vector3f.sub(yAxisOffset, position, null);
            } else if (zAxis) {
                axis = z;
                axisOffset = Vector3f.sub(zAxisOffset, position, null);
            }
        }

        if (!Mouse.isButtonDown(0)) {
            copied = false;
            dragging = false;
            axis = null;
        }

        if (dragging) {
            if (KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_ESCAPE)) {
                dragging = false;
                axis = null;

                if (!copied)
                    gizmo.getVector(entity).set(original);
                else
                    editor.removeEntity(entity);
                copied = false;
            } else {
                Vector3f.add(original, snap(gizmo.performMove(axis, Vector3f.add(original, axisOffset, null), position, entity.getRotation(), origin, ray, Mouse.getX() - startMouseX, Mouse.getY() - startMouseY), editor.getSnapAmount()), gizmo.getVector(entity));
                editor.refreshComponenetValues();
            }
        }

        return xAxis || yAxis || zAxis;
    }

    public void render() {
        if (xMatrix == null || yMatrix == null || zMatrix == null)
            return;
        Gizmo gizmo = gizmos[editor.getMode()];

        shader.start();
        gizmo.bind();
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        renderGizmo(gizmo, xMatrix, x, xAxis || axis == x);
        renderGizmo(gizmo, yMatrix, y, yAxis || axis == y);
        renderGizmo(gizmo, zMatrix, z, zAxis || axis == z);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
        shader.stop();
    }

    private static void renderGizmo(Gizmo gizmo, Matrix4f matrix, Vector3f axisColour, boolean axis) {
        shader.loadInformation(matrix, axis ? new Vector3f(1, 1, 1) : axisColour);
        gizmo.render();
    }

    private static Vector3f snap(Vector3f value, float snap) {
        if (snap > 0)
            return VectorUtils.mul(VectorUtils.floor(VectorUtils.mul(value, 1f / snap)), snap);

        return value;
    }

}
