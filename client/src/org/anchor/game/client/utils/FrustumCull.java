package org.anchor.game.client.utils;

import java.util.ArrayList;
import java.util.List;

import org.anchor.client.engine.renderer.Settings;
import org.anchor.engine.common.utils.Mathf;
import org.anchor.engine.common.utils.Plane;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.terrain.Terrain;
import org.anchor.game.client.GameClient;
import org.anchor.game.client.components.MeshComponent;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector3f;

public class FrustumCull {

    private static List<Plane> planes;
    private static float heightFar, widthFar;
    private static Vector3f farPlaneCenter;
    private static LivingComponent living;

    static {
        heightFar = 2f * Settings.farPlane * (float) Math.tan(Mathf.toRadians(Settings.fov * 0.5f));
        widthFar = heightFar * (float) Display.getWidth() / (float) Display.getHeight();
        living = GameClient.getPlayer().getComponent(LivingComponent.class);

        planes = new ArrayList<Plane>();
        planes.add(null);
        planes.add(null);

        update();
    }

    public static void update() {
        Vector3f cameraPosition = living.getEyePosition();
        farPlaneCenter = Vector3f.add(cameraPosition, VectorUtils.mul(living.getForwardVector(), Settings.farPlane), null);

        planes.set(0, new Plane(cameraPosition, calculatePlaneNormal(cameraPosition, living.getUpVector(), widthFar / 2f)));
        planes.set(1, new Plane(cameraPosition, calculatePlaneNormal(cameraPosition, living.getDownVector(), -widthFar / 2f)));
    }

    public static boolean isVisible(Entity entity) {
        MeshComponent component = entity.getComponent(MeshComponent.class);
        if (component == null)
            return false;

        if (component.disableFrustumCulling)
            return true;

        float furthest = component.getFurthestVertex();
        Vector3f position = entity.getAbsolutePosition();

        for (Plane plane : planes) {
            if (plane.signedDistanceTo(position) < -furthest)
                return false;
        }

        return true;
    }

    public static boolean isVisible(Terrain terrain) {
        Vector3f position = terrain.getCenter();
        for (Plane plane : planes) {
            if (plane.signedDistanceTo(position) < -terrain.getSize() * 0.7f)
                return false;
        }

        return true;
    }

    private static Vector3f calculatePlaneNormal(Vector3f cameraPosition, Vector3f plane, float length) {
        Vector3f side = Vector3f.add(farPlaneCenter, VectorUtils.mul(living.getRightVector(), length), null);
        Vector3f along = Vector3f.sub(side, cameraPosition, null);
        along.normalise();

        return Vector3f.cross(plane, along, null);
    }

}
