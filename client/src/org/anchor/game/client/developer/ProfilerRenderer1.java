package org.anchor.game.client.developer;

import java.util.List;
import java.util.Map.Entry;

import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.font.Alignment;
import org.anchor.client.engine.renderer.font.FontRenderer;
import org.anchor.client.engine.renderer.font.Text;
import org.anchor.client.engine.renderer.font.TextBuilder;
import org.anchor.client.engine.renderer.gui.GUI;
import org.anchor.client.engine.renderer.gui.GUIRenderer;
import org.anchor.engine.shared.profiler.Profile;
import org.anchor.engine.shared.profiler.Profiler;
import org.lwjgl.util.vector.Vector2f;

public class ProfilerRenderer1 {

    private static GUI gui;
    private static Text text;
    private static int[] textures;

    public static void init() {
        textures = new int[] { Loader.getInstance().loadColour(1, 0, 0), Loader.getInstance().loadColour(0, 1, 0), Loader.getInstance().loadColour(0, 0, 1) };
        gui = new GUI(textures[0]);
        text = new TextBuilder().colour(1, 1, 1).build();
    }

    public static void render() {
        float y = 0.8f;

        for (Entry<Thread, List<Profile>> entry : Profiler.getThreads().entrySet()) {
            float x = -0.8f;

            text.setAlignment(Alignment.RIGHT);
            text.setText(entry.getKey().getName());
            text.getPosition().set(x, y + 0.055f);
            FontRenderer.render(text);
            text.setAlignment(Alignment.LEFT);

            float totalDuration = 0;
            for (Profile profile : entry.getValue())
                totalDuration += profile.getDuration();

            int i = 0;
            float deepest = 0;
            for (Profile profile : entry.getValue()) {
                gui.setTexture(textures[i % textures.length]);

                Vector2f result = render(profile, totalDuration, x, y, 0);
                x = result.x;

                deepest = Math.max(deepest, result.y);
                i++;
            }

            y -= (deepest + 1) * 0.1f;
        }
    }

    private static Vector2f render(Profile profile, float totalDuration, float x, float y, float depth) {
        gui.getScale().set(profile.getDuration() / totalDuration * 0.875f, 0.05f);
        gui.getPosition().set(x + gui.getScale().x, y + gui.getScale().y);

        GUIRenderer.render(gui);

        text.setText(profile.getName());
        text.getPosition().set(x + 0.02f, y + 0.055f);
        FontRenderer.render(text);

        float increment = gui.getScale().x * 2;

        int i = 0;
        float childX = x;
        float childTotal = 0;
        for (Profile child : profile.getChildren())
            childTotal += child.getDuration();

        float deepest = depth + 1;
        for (Profile child : profile.getChildren()) {
            gui.setTexture(textures[i % textures.length]);

            Vector2f result = render(child, childTotal / (increment / 1.75f), childX, y - 0.1f, depth + 1);
            childX = result.x;

            deepest = Math.max(deepest, result.y);
            i++;
        }

        return new Vector2f(x + increment, deepest);
    }

}
