package org.anchor.game.editor.properties;

import java.lang.reflect.Field;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class PropertyTextField extends JTextField {

    protected Object object;
    protected Field field;

    protected String previousValue;

    private static final long serialVersionUID = -586156248454660986L;

    public PropertyTextField(Field field, Object object) {
        super(get(field, object));
        getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (isEnabled())
                    set();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (isEnabled())
                    set();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (isEnabled())
                    set();
            }

            private void set() {
                String value = getText();
                if (value.equals(previousValue))
                    return;

                previousValue = value;
                try {
                    field.set(object, convert(value));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

        this.field = field;
        this.object = object;
    }

    public void update() {
        setText(get(field, object));
    }

    public static String get(Field field, Object object) {
        try {
            return field.get(object).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public abstract Object convert(String value);

    protected float parseFloat(String input) {
        try {
            return Float.parseFloat(input);
        } catch (Exception e) {
        }

        return 0;
    }

    protected int parseInt(String input) {
        try {
            return Integer.parseInt(input);
        } catch (Exception e) {
        }

        return 0;
    }

}
