package org.anchor.client.engine.renderer.menu;

import java.util.ArrayList;
import java.util.List;

import org.anchor.client.engine.renderer.KeyboardUtils;
import org.anchor.client.engine.renderer.Loader;
import org.anchor.client.engine.renderer.font.Alignment;
import org.anchor.client.engine.renderer.font.FontRenderer;
import org.anchor.client.engine.renderer.font.Text;
import org.anchor.client.engine.renderer.gui.GUI;
import org.anchor.client.engine.renderer.gui.GUIRenderer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class Menu extends MenuItem {

    protected String title;
    protected Vector2f position;
    protected List<MenuItem> items;
    protected boolean visible;
    protected int selected;
    protected float longest = 0;

    private Menu parent;

    private static List<Menu> instances = new ArrayList<Menu>();
    private static int background = Loader.getInstance().loadColour(0.1f, 0.1f, 0.1f);
    private static GUI gui = new GUI(background);
    private static Text text = new Text(new Vector2f(), "", FontRenderer.defaultFont, 1, new Vector3f(0.75f, 0.75f, 0.75f), Alignment.LEFT);
    private static Text arrowText = new Text(new Vector2f(), ">", FontRenderer.defaultFont, 1, new Vector3f(1, 1, 1), Alignment.LEFT);

    public Menu(String title, MenuItem... items) {
        this(title, new Vector2f(-0.935f, 0.925f), items);
    }

    public Menu(String title, Vector2f position, MenuItem... items) {
        super(title);

        this.title = title;
        this.position = position;

        this.items = new ArrayList<MenuItem>();
        for (MenuItem item : items)
            this.items.add(item);

        instances.add(this);
    }

    @Override
    public void run(Menu parent) {
        parent.visible = false;
        this.parent = parent;

        show();
    }

    public void show() {
        for (Menu instance : instances)
            if (instance.visible)
                return;

        visible = true;
    }

    public void close() {
        visible = false;
        if (parent != null) {
            parent.visible = true;

            parent = null;
        }
    }

    public void toggle() {
        if (visible)
            close();
        else
            show();
    }

    public boolean isVisible() {
        return visible;
    }

    public static void render() {
        for (Menu instance : instances) {
            if (!instance.visible)
                continue;

            float height = (instance.items.size() + 1) * 0.055f;
            gui.getPosition().set(instance.position.x + instance.longest * 0.5f - 0.0175f, instance.position.y - height * 0.5f + 0.02f);
            gui.getScale().set(instance.longest * 0.5f + 0.03f, height * 0.5f + 0.01f);
            GUIRenderer.render(gui);

            float x = instance.position.x + 0.001f;
            float y = instance.position.y - 0.055f;

            int i = 0;

            {
                text.getPosition().set(x - 0.03f, y + 0.055f);
                text.setText(instance.title);

                Vector3f colour = MenuItemStatus.DEFAULT.getColour();
                text.getColour().set(colour.x, colour.y, colour.z, 1);
                instance.longest = Math.max(instance.longest, text.getLength());

                FontRenderer.render(text);
            }

            for (MenuItem item : instance.items) {
                text.getPosition().set(x, y);
                text.setText(item.getText());

                Vector3f colour = item.getStatus().getColour();
                text.getColour().set(colour.x, colour.y, colour.z, 1);

                float length = text.getLength();
                instance.longest = Math.max(instance.longest, length);

                FontRenderer.render(text);
                instance.longest = Math.max(instance.longest, length + item.renderExtra(text, x, y, instance.longest));

                if (i == instance.selected) {
                    arrowText.getPosition().set(x - 0.023f, y + 0.005f);
                    FontRenderer.render(arrowText);
                }

                y -= 0.055f;
                i++;
            }
        }
    }

    public static void update() {
        boolean up = KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_UP);
        boolean down = KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_DOWN);
        boolean select = KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_RETURN);
        boolean back = KeyboardUtils.wasKeyJustPressed(Keyboard.KEY_ESCAPE);

        for (Menu instance : instances) {
            if (!instance.visible)
                continue;

            if (up)
                instance.selected--;
            if (down)
                instance.selected++;

            if (instance.selected < 0)
                instance.selected = instance.items.size() + instance.selected;
            if (instance.selected >= instance.items.size())
                instance.selected -= instance.items.size();

            if (select)
                instance.items.get(instance.selected).run(instance);

            if (back) {
                instance.close();
                back = false;
            }
        }
    }

}
