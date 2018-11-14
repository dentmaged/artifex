package org.anchor.game.client.developer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.anchor.client.engine.renderer.font.FontRenderer;
import org.anchor.client.engine.renderer.font.Text;
import org.anchor.client.engine.renderer.font.TextBuilder;
import org.anchor.engine.common.Log;
import org.anchor.engine.common.utils.LogCallback;

public class LogRenderer {

    private static Map<Text, Long> texts = new HashMap<Text, Long>();

    public static long DURATION = 5000;

    public static void init() {
        Log.callbacks.add(new LogCallback() {

            @Override
            public void log(String text) {
                LogRenderer.log(text);
            }

        });
    }

    public static void log(String text) {
        texts.put(new TextBuilder().text(text).position(-0.985f, 0.97f - texts.size() * 0.055f).build(), System.currentTimeMillis());
    }

    public static void render() {
        long time = System.currentTimeMillis();

        Iterator<Entry<Text, Long>> iterator = texts.entrySet().iterator();
        int i = 0;

        while (iterator.hasNext()) {
            Entry<Text, Long> entry = iterator.next();
            if (time >= entry.getValue() + DURATION)
                iterator.remove();

            entry.getKey().getPosition().y = 0.97f - i * 0.055f;
            i++;
        }

        FontRenderer.render(new ArrayList<Text>(texts.keySet()));
    }

}
