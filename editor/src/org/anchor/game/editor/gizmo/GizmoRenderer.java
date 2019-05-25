package org.anchor.game.editor.gizmo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anchor.client.engine.renderer.KeyboardUtils;
import org.anchor.engine.common.utils.CoreMaths;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.editor.TransformableObject;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.client.GameClient;
import org.anchor.game.editor.GameEditor;
import org.anchor.game.editor.commands.Undo;
import org.anchor.game.editor.editableMesh.EditableMesh;
import org.anchor.game.editor.editableMesh.types.OpenGLData;
import org.anchor.game.editor.gizmo.types.RotateGizmo;
import org.anchor.game.editor.gizmo.types.ScaleGizmo;
import org.anchor.game.editor.gizmo.types.SelectGizmo;
import org.anchor.game.editor.gizmo.types.TranslateGizmo;
import org.anchor.game.editor.ui.LevelEditor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL40;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

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
    protected Vector3f axis, axisOffset, scale = new Vector3f(), startPosition, lastChange = new Vector3f();
    protected Map<TransformableObject, Vector3f> originals = new HashMap<TransformableObject, Vector3f>();
    protected GameEditor editor;

    public GizmoRenderer(GameEditor editor) {
        this.editor = editor;
    }

    public boolean update(List<TransformableObject> objects, Vector3f ray) {
        if (objects.size() == 0)
            return false;

        float count = 0;
        Vector3f position = new Vector3f();
        for (TransformableObject object : objects) {
            Vector3f.add(position, new Vector3f(Matrix4f.transform(object.getTransformationMatrix(), new Vector4f(0, 0, 0, 1), null)), position);
            count++;
        }

        position.x /= count;
        position.y /= count;
        position.z /= count;
        if (startPosition == null)
            startPosition = new Vector3f(position);

        count = 0;
        Vector3f rotation = new Vector3f();
        for (TransformableObject object : objects) {
            Vector3f.add(rotation, object.getRotation(), rotation);
            count++;
        }

        rotation.x /= count;
        rotation.y /= count;
        rotation.z /= count;

        float distance = Vector3f.sub(GameClient.getPlayer().getPosition(), position, null).length();
        distance *= 0.2f;
        scale.set(distance, distance, distance);

        Gizmo gizmo = gizmos[editor.getMode()];
        Vector3f origin = GameClient.getPlayer().getComponent(LivingComponent.class).getEyePosition();

        xMatrix = CoreMaths.createTransformationMatrix(position, gizmo.getXRotation(rotation, editor.getTransformationMode()), scale);
        yMatrix = CoreMaths.createTransformationMatrix(position, gizmo.getYRotation(rotation, editor.getTransformationMode()), scale);
        zMatrix = CoreMaths.createTransformationMatrix(position, gizmo.getZRotation(rotation, editor.getTransformationMode()), scale);

        Vector3f xAxisOffset = gizmo.intersection(xMatrix, origin, ray, position, gizmo.getXRotation(rotation, editor.getTransformationMode()), scale);
        Vector3f yAxisOffset = gizmo.intersection(yMatrix, origin, ray, position, gizmo.getYRotation(rotation, editor.getTransformationMode()), scale);
        Vector3f zAxisOffset = gizmo.intersection(zMatrix, origin, ray, position, gizmo.getZRotation(rotation, editor.getTransformationMode()), scale);

        xAxis = xAxisOffset != null;
        yAxis = yAxisOffset != null;
        zAxis = zAxisOffset != null;

        int m = 0;
        if (xAxis)
            m++;
        if (yAxis)
            m++;
        if (zAxis)
            m++;
        if (m > 1)
            xAxis = yAxis = zAxis = false;

        if ((xAxis || yAxis || zAxis) && !dragging && Mouse.isButtonDown(0)) {
            if (KeyboardUtils.isKeyDown(Keyboard.KEY_LSHIFT)) {
                List<TransformableObject> copy = new ArrayList<TransformableObject>();
                objects = new ArrayList<TransformableObject>(objects);
                for (TransformableObject object : objects) {
                    if (object instanceof Entity) {
                        object = ((Entity) object).copy();
                        editor.addEntity((Entity) object);

                        copy.add(object);
                    } else if (object instanceof EditableMesh) {
                        object = new EditableMesh((EditableMesh) object);
                        editor.addEditableMesh((EditableMesh) object);

                        copy.add(object);
                    }
                }

                if (copy.size() > 0) {
                    objects = copy;

                    LevelEditor.getInstance().unselectAll();
                    LevelEditor.getInstance().addAllToSelection(copy);

                    copied = true;
                }
            }

            startMouseX = Mouse.getX();
            startMouseY = Mouse.getY();
            lastChange.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

            for (TransformableObject object : objects)
                originals.put(object, new Vector3f(gizmo.getVector(object)));
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

            Vector3f xyPlaneOffset = plane.intersection(zMatrix, origin, ray, position, gizmo.getZRotation(rotation, editor.getTransformationMode()), scale);
            Vector3f xzPlaneOffset = plane.intersection(yMatrix, origin, ray, position, gizmo.getYRotation(rotation, editor.getTransformationMode()), scale);
            Vector3f yzPlaneOffset = plane.intersection(xMatrix, origin, ray, position, gizmo.getXRotation(rotation, editor.getTransformationMode()), scale);

            xyPlane = xyPlaneOffset != null;
            xzPlane = xzPlaneOffset != null;
            yzPlane = yzPlaneOffset != null;

            m = 0;
            if (xyPlane)
                m++;
            if (xzPlane)
                m++;
            if (yzPlane)
                m++;
            if (m > 1)
                xyPlane = xzPlane = yzPlane = false;

            if ((xyPlane || xzPlane || yzPlane) && !dragging && Mouse.isButtonDown(0)) {
                if (KeyboardUtils.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    List<TransformableObject> copy = new ArrayList<TransformableObject>();
                    objects = new ArrayList<TransformableObject>(objects);
                    for (TransformableObject object : objects) {
                        if (object instanceof Entity) {
                            object = ((Entity) object).copy();
                            editor.addEntity((Entity) object);

                            copy.add(object);
                        } else if (object instanceof EditableMesh) {
                            object = new EditableMesh((EditableMesh) object);
                            editor.addEditableMesh((EditableMesh) object);

                            copy.add(object);
                        }
                    }

                    if (copy.size() > 0) {
                        objects = copy;

                        LevelEditor.getInstance().unselectAll();
                        LevelEditor.getInstance().addAllToSelection(copy);

                        copied = true;
                    }
                }

                startMouseX = Mouse.getX();
                startMouseY = Mouse.getY();
                lastChange.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

                for (TransformableObject object : objects)
                    originals.put(object, new Vector3f(gizmo.getVector(object)));
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
                Map<EditableMesh, Byte> meshes = new HashMap<EditableMesh, Byte>(); // use map to avoid duplicates
                for (TransformableObject object : objects) {
                    if (object instanceof OpenGLData)
                        meshes.put(((OpenGLData) object).getMesh(), (byte) 0);

                    Vector3f vector = gizmo.getVector(object);
                    if (!originals.get(object).equals(vector))
                        Undo.registerChange(vector, Vector3f.sub(vector, originals.remove(object), null));
                }

                for (EditableMesh mesh : meshes.keySet())
                    mesh.updateMesh(true);
            }

            originals.clear();
            copied = false;
            dragging = false;
            axis = null;
            startPosition = null;
            lastChange.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        }

        if (dragging) {
            if (KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_ESCAPE)) {
                dragging = false;
                axis = null;
                startPosition = null;

                if (!copied) {
                    Map<EditableMesh, Byte> meshes = new HashMap<EditableMesh, Byte>(); // use map to avoid duplicates
                    for (TransformableObject object : objects) {
                        if (object instanceof OpenGLData)
                            meshes.put(((OpenGLData) object).getMesh(), (byte) 0);
                        gizmo.getVector(object).set(originals.get(object));
                    }

                    originals.clear();
                    for (EditableMesh mesh : meshes.keySet())
                        mesh.updateMesh(true);
                } else {
                    for (TransformableObject object : objects)
                        if (object instanceof Entity)
                            editor.removeEntity((Entity) object);
                }
                copied = false;
            } else {
                Vector3f change = snap(gizmo.performMove(axis, Vector3f.add(startPosition, axisOffset, null), position, rotation, origin, ray, Mouse.getX() - startMouseX, Mouse.getY() - startMouseY, axisOffset), editor.getSnapAmount());
                if (!lastChange.equals(change)) {
                    lastChange.set(change);

                    Map<EditableMesh, Byte> meshes = new HashMap<EditableMesh, Byte>(); // use map to avoid duplicates
                    for (TransformableObject object : objects) {
                        if (object instanceof OpenGLData)
                            meshes.put(((OpenGLData) object).getMesh(), (byte) 0);

                        gizmo.getVector(object).set(VectorUtils.clamp(Vector3f.add(originals.get(object), change, null), gizmo.getMin(), gizmo.getMax()));
                        editor.refreshComponenetValues();
                    }

                    for (EditableMesh mesh : meshes.keySet())
                        mesh.updateMesh(false);
                }
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

    public Vector3f getVector(TransformableObject object) {
        return gizmos[editor.getMode()].getVector(object);
    }

    public boolean isDragging() {
        return dragging;
    }

}
