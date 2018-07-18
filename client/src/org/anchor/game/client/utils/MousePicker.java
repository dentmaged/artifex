package org.anchor.game.client.utils;

import org.anchor.client.engine.renderer.Renderer;
import org.anchor.engine.common.utils.AABB;
import org.anchor.engine.common.utils.VectorUtils;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.scene.Scene;
import org.anchor.engine.shared.terrain.Terrain;
import org.anchor.engine.shared.utils.EntityRaycast;
import org.anchor.engine.shared.utils.TerrainRaycast;
import org.anchor.game.client.GameClient;
import org.anchor.game.client.components.MeshComponent;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class MousePicker {

    private static final int RECURSION_COUNT = 200;
    private static final float RAY_RANGE = 600;

    private Matrix4f inverseViewMatrix;

    public Vector3f getRay(int x, int y) {
        inverseViewMatrix = GameClient.getPlayer().getComponent(LivingComponent.class).getInverseViewMatrix();

        return calculateRay(x, y);
    }

    public TerrainRaycast getTerrain(int x, int y) {
        return getTerrain(calculateRay(x, y));
    }

    public TerrainRaycast getTerrain(Vector3f ray) {
        if (intersectionInRange(0, RAY_RANGE, ray))
            return binarySearch(0, 0, RAY_RANGE, ray);

        return null;
    }

    public EntityRaycast getEntity(Scene scene, Vector3f ray) {
        ray.normalise();
        Vector3f origin = GameClient.getPlayer().getComponent(LivingComponent.class).getEyePosition();

        float min = Float.MAX_VALUE;
        Entity intersection = null;
        Vector3f dirfrac = new Vector3f(1f / ray.x, 1f / ray.y, 1f / ray.z);

        for (int i = 0; i < scene.getEntities().size(); i++) {
            Entity entity = scene.getEntities().get(i);
            if (entity.isHidden())
                continue;

            MeshComponent render = entity.getComponent(MeshComponent.class);
            if (render == null)
                continue;

            AABB aabb = render.getAABB();
            if (aabb == null)
                continue;

            float t1 = (aabb.getX1() - origin.x) * dirfrac.x;
            float t2 = (aabb.getX2() - origin.x) * dirfrac.x;

            float t3 = (aabb.getY1() - origin.y) * dirfrac.y;
            float t4 = (aabb.getY2() - origin.y) * dirfrac.y;

            float t5 = (aabb.getZ1() - origin.z) * dirfrac.z;
            float t6 = (aabb.getZ2() - origin.z) * dirfrac.z;

            float tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
            float tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

            if (tmax < 0 || tmin > tmax)
                continue;

            if (tmin < min) {
                min = tmin;
                intersection = entity;
            }
        }

        if (intersection == null)
            return null;

        return new EntityRaycast(intersection, min, ray, origin);
    }

    private Vector3f calculateRay(float x, float y) {
        Vector4f clip = new Vector4f((2f * x) / (float) Display.getWidth() - 1, (2f * y) / (float) Display.getHeight() - 1, -1, 1);
        Vector4f eye = Matrix4f.transform(Renderer.getInverseProjectionMatrix(), clip, null);
        eye.set(eye.x, eye.y, -1, 0);
        Vector3f world = new Vector3f(Matrix4f.transform(inverseViewMatrix, eye, null));
        world.normalise();

        return world;
    }

    private Vector3f getPointOnRay(Vector3f ray, float distance) {
        return Vector3f.add(GameClient.getPlayer().getPosition(), VectorUtils.mul(ray, distance), null);
    }

    private TerrainRaycast binarySearch(int count, float start, float finish, Vector3f ray) {
        float half = start + ((finish - start) / 2f);
        if (count >= RECURSION_COUNT) {
            Vector3f point = getPointOnRay(ray, half);
            Terrain terrain = GameClient.getTerrainByPoint(point);
            if (terrain != null)
                return new TerrainRaycast(terrain, half, ray, point);
            else
                return null;
        }

        if (intersectionInRange(start, half, ray))
            return binarySearch(count + 1, start, half, ray);
        else
            return binarySearch(count + 1, half, finish, ray);
    }

    private boolean intersectionInRange(float start, float finish, Vector3f ray) {
        Vector3f startPoint = getPointOnRay(ray, start);
        Vector3f endPoint = getPointOnRay(ray, finish);

        if (!isUnderGround(startPoint) && isUnderGround(endPoint))
            return true;
        else
            return false;
    }

    private boolean isUnderGround(Vector3f testPoint) {
        Terrain terrain = GameClient.getTerrainByPoint(testPoint);
        float height = 0;

        if (terrain != null)
            height = terrain.getHeightOfTerrain(testPoint.x, testPoint.z);

        return testPoint.y < height;
    }

}
