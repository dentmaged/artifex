package org.anchor.game.client.developer.debug;

import java.util.ArrayList;
import java.util.List;

import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.scheduler.ScheduledRunnable;
import org.anchor.engine.shared.scheduler.Scheduler;
import org.anchor.game.client.GameClient;
import org.lwjgl.util.vector.Vector3f;

public class Debug {

    private static List<DebugShape> shapes = new ArrayList<DebugShape>();

    public static void box(Vector3f position, Vector3f rotation, float duration) {
        box(position, rotation, new Vector3f(1, 1, 1), new Vector3f(1, 0, 0), duration);
    }

    public static void box(Vector3f position, Vector3f rotation, Vector3f scale, Vector3f colour, float duration) {
        DebugBox box;
        shapes.add(box = new DebugBox(position, rotation, scale, colour));
        Scheduler.schedule(new ScheduledRunnable() {

            @Override
            public void tick(float time, float percentage) {

            }

            @Override
            public void finish() {
                shapes.remove(box);
            }

        }, duration);
    }

    public static void circle(Vector3f position, Vector3f rotation, float duration) {
        circle(position, rotation, new Vector3f(1, 1, 1), new Vector3f(1, 0, 0), duration);
    }

    public static void circle(Vector3f position, Vector3f rotation, Vector3f scale, Vector3f colour, float duration) {
        DebugCircle circle;
        shapes.add(circle = new DebugCircle(position, rotation, scale, colour));
        Scheduler.schedule(new ScheduledRunnable() {

            @Override
            public void tick(float time, float percentage) {

            }

            @Override
            public void finish() {
                shapes.remove(circle);
            }

        }, duration);
    }

    public static void render() {
        for (DebugShape shape : shapes)
            shape.render(GameClient.getPlayer().getComponent(LivingComponent.class).getViewMatrix());
    }

    public static int getShapeCount() {
        return shapes.size();
    }

}
