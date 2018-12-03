package org.anchor.game.editor.gizmo;

import org.anchor.client.engine.renderer.KeyboardUtils;
import org.anchor.engine.common.utils.CoreMaths;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.GameClient;
import org.anchor.game.editor.GameEditor;
import org.anchor.game.editor.commands.Undo;
import org.anchor.game.editor.gizmo.types.RotateGizmo;
import org.anchor.game.editor.gizmo.types.ScaleGizmo;
import org.anchor.game.editor.gizmo.types.SelectGizmo;
import org.anchor.game.editor.gizmo.types.TranslateGizmo;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL40;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class GizmoRenderer {

    private static GizmoShader shader = GizmoShader.getInstance();

    private static Vector3f x = new Vector3f(1, 0, 0);
    private static Vector3f y = new Vector3f(0, 1, 0);
    private static Vector3f z = new Vector3f(0, 0, 1);

    private static Vector3f xz = new Vector3f(1, 0, 1);
    private static Vector3f xy = new Vector3f(1, 1, 0);
    private static Vector3f yz = new Vector3f(0, 1, 1);

    private static Gizmo[] gizmos = { new SelectGizmo(), new TranslateGizmo(), new RotateGizmo(), new ScaleGizmo() };

    protected Matrix4f xMatrix, yMatrix, zMatrix;
    protected boolean xAxis, yAxis, zAxis, xyPlane, xzPlane, yzPlane, dragging, copied;
    protected int startMouseX, startMouseY;
    protected Vector3f axis, axisOffset, original, scale = new Vector3f();
    protected GameEditor editor;

    public GizmoRenderer(GameEditor editor) {
        this.editor = editor;
    }

    public boolean update(Entity entity, Vector3f ray) {
        Vector3f position = entity.getPosition();
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

        if (gizmo.getPlane() != null) {
            Plane plane = gizmo.getPlane();

            Vector3f xyPlaneOffset = plane.intersection(zMatrix, origin, ray, position, gizmo.getZRotation(entity.getRotation(), editor.getTransformationMode()), scale);
            Vector3f xzPlaneOffset = plane.intersection(yMatrix, origin, ray, position, gizmo.getYRotation(entity.getRotation(), editor.getTransformationMode()), scale);
            Vector3f yzPlaneOffset = plane.intersection(xMatrix, origin, ray, position, gizmo.getXRotation(entity.getRotation(), editor.getTransformationMode()), scale);

            xyPlane = xyPlaneOffset != null;
            xzPlane = xzPlaneOffset != null;
            yzPlane = yzPlaneOffset != null;

            if ((xyPlane || xzPlane || yzPlane) && !dragging && Mouse.isButtonDown(0)) {
                if (KeyboardUtils.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    entity = entity.copy();
                    editor.addEntity(entity);

                    copied = true;
                }

                startMouseX = Mouse.getX();
                startMouseY = Mouse.getY();

                original = new Vector3f(gizmo.getVector(entity));
                dragging = true;

                if (xyPlane) {
                    axis = xy;
                    axisOffset = Vector3f.sub(xyPlaneOffset, position, null);
                } else if (xzPlane) {
                    axis = xz;
                    axisOffset = Vector3f.sub(xzPlaneOffset, position, null);
                } else if (yzPlane) {
                    axis = yz;
                    axisOffset = Vector3f.sub(yzPlaneOffset, position, null);
                }
            }
        }

        if (!Mouse.isButtonDown(0)) {
            if (dragging) {
                Vector3f vector = gizmo.getVector(entity);
                if (!original.equals(vector))
                    Undo.registerChange(vector, Vector3f.sub(vector, original, null));
            }

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
                gizmo.getVector(entity).set(VectorUtils.clamp(Vector3f.add(original, snap(gizmo.performMove(axis, Vector3f.add(original, axisOffset, null), position, entity.getRotation(), origin, ray, Mouse.getX() - startMouseX, Mouse.getY() - startMouseY, axisOffset), editor.getSnapAmount()), null), gizmo.getMin(), gizmo.getMax()));
                editor.refreshComponenetValues();
            }
        }

        return xAxis || yAxis || zAxis || xyPlane || xzPlane || yzPlane;
    }

    public void render() {
        if (xMatrix == null || yMatrix == null || zMatrix == null)
            return;
        Gizmo gizmo = gizmos[editor.getMode()];
        Plane plane = gizmo.getPlane();

        shader.start();
        GL11.glEnable(GL11.GL_BLEND);
        GL40.glBlendFunci(0, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        gizmo.bind();
        renderGizmo(gizmo, xMatrix, x, xAxis || axis == x);
        renderGizmo(gizmo, yMatrix, y, yAxis || axis == y);
        renderGizmo(gizmo, zMatrix, z, zAxis || axis == z);
        gizmo.unbind();

        if (plane != null) {
            plane.bind();
            renderPlane(plane, zMatrix, z, xyPlane || axis == xy);
            renderPlane(plane, yMatrix, y, xzPlane || axis == xz);
            renderPlane(plane, xMatrix, x, yzPlane || axis == yz);
            plane.unbind();
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        shader.stop();
    }

    private static void renderGizmo(Gizmo gizmo, Matrix4f matrix, Vector3f axisColour, boolean axis) {
        shader.loadInformation(matrix, axis ? new Vector3f(1, 1, 1) : axisColour, 1);
        gizmo.render();
    }

    private static void renderPlane(Plane plane, Matrix4f matrix, Vector3f axisColour, boolean axis) {
        shader.loadInformation(matrix, axisColour, axis ? 1 : 0.1f);
        plane.render();
    }

    private static Vector3f snap(Vector3f value, float snap) {
        if (snap > 0)
            return VectorUtils.mul(VectorUtils.floor(VectorUtils.mul(value, 1f / snap)), snap);

        return value;
    }

    public void recreate() {
        for (Gizmo gizmo : gizmos)
            gizmo.shutdown();

        gizmos = new Gizmo[] { new SelectGizmo(), new TranslateGizmo(), new RotateGizmo(), new ScaleGizmo() };
    }

    public Vector3f getVector(Entity entity) {
        return gizmos[editor.getMode()].getVector(entity);
    }

    public boolean isDragging() {
        return dragging;
    }

}
