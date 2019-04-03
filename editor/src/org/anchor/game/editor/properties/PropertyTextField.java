package org.anchor.game.editor.properties;

import java.util.List;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.anchor.engine.common.utils.JavaField;
import org.anchor.engine.common.utils.ObjectUtils;
import org.anchor.game.editor.commands.Undo;

public abstract class PropertyTextField extends JTextField {

    protected List<Object> objects;
    protected JavaField field;

    protected String previousValue;

    private static final long serialVersionUID = -586156248454660986L;

    public PropertyTextField(JavaField field, List<Object> objects) {
        super(String.valueOf(get(field, objects)));
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
                Undo.fieldsSet(field, objects, convert(value));
            }

        });

        this.field = field;
        this.objects = objects;
    }

    public void update() {
        setEnabled(false);
        setText(String.valueOf(get(field, objects)));
        setEnabled(true);
    }

    public abstract Object convert(String value);

    @Override
    public void setText(String text) {
        super.setText("null".equals(text) ? "" : text);
    }

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

    private static Object get(JavaField field, List<Object> objects) {
        Object value = field.get(objects.get(0));
        for (Object object : objects)
            if (!ObjectUtils.compare(value, field.get(object)))
                value = null;

        return value;
    }

}
