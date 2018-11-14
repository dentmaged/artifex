package org.anchor.client.engine.renderer.menu;

import java.lang.reflect.Field;

import org.anchor.client.engine.renderer.font.Alignment;
import org.anchor.client.engine.renderer.font.FontRenderer;
import org.anchor.client.engine.renderer.font.Text;

public class ReflectionMenuItem extends MenuItem {

    protected Field field;
    protected Object object;

    public ReflectionMenuItem(Class<?> clazz, String humanName) {
        this(clazz, humanName, toJavaName(humanName));
    }

    public ReflectionMenuItem(Class<?> clazz, String humanName, String field) {
        this(clazz, humanName, field, null);
    }

    public ReflectionMenuItem(Class<?> clazz, String humanName, String field, Object object) {
        super(humanName);

        try {
            this.field = clazz.getField(field);
            this.object = object;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(Menu parent) {
        try {
            if (field.getType() == boolean.class)
                field.set(object, !((boolean) field.get(object)));

            if (field.getType().isEnum())
                field.set(object, getNext(field.get(object), field.getType().getEnumConstants()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public float renderExtra(Text text, float x, float y, float longest) {
        if (field.getType() == boolean.class)
            return 0;

        try {
            Object value = field.get(object);
            if (value == null)
                value = "null";

            text.setText(value.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        text.getPosition().x += longest + 0.01f;
        text.setAlignment(Alignment.RIGHT);

        FontRenderer.render(text);

        text.setAlignment(Alignment.LEFT);
        return text.getLength();
    }

    @Override
    public MenuItemStatus getStatus() {
        Object value = null;
        try {
            value = field.get(object);
            if (value == null)
                value = "null";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return field.getType() == boolean.class && ((boolean) value) ? MenuItemStatus.ON : MenuItemStatus.DEFAULT;
    }

    protected Object getNext(Object object, Object[] objects) {
        int i = 0;
        for (; i < objects.length; i++)
            if (objects[i] == object)
                break;

        i++;
        if (i < 0)
            i = objects.length + i;
        if (i >= objects.length)
            i -= objects.length;

        return objects[i];
    }

    protected static String toJavaName(String input) {
        char[] output = new char[input.length()];
        boolean uppercaseNext = false;

        for (int i = 0; i < output.length; i++) {
            char c = Character.toLowerCase(input.charAt(i));
            if (c == ' ') {
                uppercaseNext = true;
                output[i] = c;
            } else {
                output[i] = c;
                if (uppercaseNext)
                    output[i] = Character.toUpperCase(c);
                uppercaseNext = false;
            }
        }

        return new String(output).replaceAll("\\s", "");
    }

}
