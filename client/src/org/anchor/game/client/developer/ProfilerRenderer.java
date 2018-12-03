package org.anchor.game.client.developer;

public class ProfilerRenderer {

    public static void init() {
        ProfilerRenderer1.init();
    }

    public static void render(int mode) {
        if (mode == 1)
            ProfilerRenderer1.render();
    }

}
