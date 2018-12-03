package org.anchor.game.editor.properties;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.anchor.engine.common.utils.JavaField;
import org.anchor.game.editor.commands.Undo;

public abstract class PropertyTextField extends JTextField {

    protected Object object;
    protected JavaField field;

    protected String previousValue;

    private static final long serialVersionUID = -586156248454660986L;

    public PropertyTextField(JavaField field, Object object) {
        super(String.valueOf(field.get(object)));
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
                if (!isEnabled())
                    return;

                String value = getText();
                if (value.equals(previousValue))
                    return;

                previousValue = value;
                Undo.fieldSet(field, object, convert(value));
            }

        });

        this.field = field;
        this.object = object;
    }

    public void update() {
        setEnabled(false);
        setText(String.valueOf(field.get(object)));
        setEnabled(true);
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
