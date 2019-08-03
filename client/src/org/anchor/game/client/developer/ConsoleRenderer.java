package org.anchor.game.client.developer;

import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.font.FontRenderer;
import org.anchor.client.engine.renderer.font.Text;
import org.anchor.client.engine.renderer.font.TextBuilder;
import org.anchor.client.engine.renderer.gui.GUI;
import org.anchor.client.engine.renderer.gui.GUIRenderer;
import org.anchor.client.engine.renderer.keyboard.KeyboardUtils;
import org.anchor.engine.common.Log;
import org.anchor.engine.common.utils.LogCallback;
import org.anchor.engine.common.utils.Mathf;
import org.anchor.engine.common.utils.StringUtils;
import org.anchor.engine.shared.console.Console;
import org.anchor.game.client.GameClient;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

public class ConsoleRenderer {

    private static GUI background, scroll;
    private static Text text, arrow, textbox;
    private static float scrollPercentage;

    public static void init() {
        background = new GUI(Loader.getInstance().loadColour(0, 0, 0, 0.6f), new Vector2f(0, 0.5f), new Vector2f(1, 0.5f));
        scroll = new GUI(background.getTexture(), new Vector2f(0.9875f, 0.94f), new Vector2f(0.003f, 0.04f));

        Log.callbacks.add(new LogCallback() {

            @Override
            public void log(String text) {
                ConsoleRenderer.log(text);
            }

        });

        text = new TextBuilder().position(-0.97f, 0.97f).build();
        arrow = new TextBuilder().text("> ").position(-0.985f, 0.05f).build();
        textbox = new TextBuilder().position(-0.97f, 0.05f).build();
    }

    public static void log(String message) {
        text.setText(text.getText() + message + "\n");

        int count = text.getText().split("\n").length;
        if (count > 20)
            text.getPosition().y = 0.97f + ((count - 20) * 0.045f * scrollPercentage);
    }

    public static void update() {
        float count = text.getText().split("\n").length;
        if (count > 20) {
            scrollPercentage += (float) Mouse.getDWheel() / count * 0.0025f;
            scrollPercentage = Mathf.clamp(scrollPercentage, 0, 1);
            text.getPosition().y = 0.97f + ((count - 20) * 0.045f * scrollPercentage);
        } else {
            scrollPercentage = 1;
        }

        for (char c : KeyboardUtils.getPressedCharacters()) {
            if (!StringUtils.isValidCharacter(c))
                continue;

            textbox.setText(textbox.getText() + c);
        }
        KeyboardUtils.getPressedCharacters().clear();

        if (KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_BACK) && textbox.getText().length() > 0)
            textbox.setText(textbox.getText().substring(0, textbox.getText().length() - 1));

        if (KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_RETURN)) {
            Log.print("] " + textbox.getText());
            Console.run(GameClient.getInstance().getUser(), textbox.getText());

            textbox.setText("");
        }
    }

    public static void render() {
        scroll.getPosition().y = 0.94f - 0.81f * scrollPercentage;

        GUIRenderer.render(background);
        GUIRenderer.render(scroll);

        int y = (int) (Display.getHeight() * (0.5f + FontRenderer.VERTICAL_PADDING));
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(0, y, Display.getWidth(), Display.getHeight() - y);

        FontRenderer.render(text);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        FontRenderer.render(arrow);
        FontRenderer.render(textbox);
    }

}
