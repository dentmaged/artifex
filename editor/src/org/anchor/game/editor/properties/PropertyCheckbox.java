package org.anchor.game.editor.properties;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBox;

import org.anchor.engine.common.utils.JavaField;
import org.anchor.engine.common.utils.ObjectUtils;
import org.anchor.game.editor.commands.Undo;

public class PropertyCheckbox extends JCheckBox {

    protected List<Object> objects;
    protected JavaField field;

    protected String previousValue;

    private static final long serialVersionUID = -586156248454660986L;

    public PropertyCheckbox(JavaField field, List<Object> objects) {
        setSelected(getBoolean(get(field, objects)));
        addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isEnabled())
                    return;

                Undo.fieldsSet(field, objects, isSelected());
            }

        });

        this.field = field;
        this.objects = objects;
    }

    public void update() {
        setEnabled(false);
        setSelected(getBoolean(get(field, objects)));
        setEnabled(true);
    }

    private static Object get(JavaField field, List<Object> objects) {
        Object value = field.get(objects.get(0));
        for (Object object : objects)
            if (!ObjectUtils.compare(value, field.get(object)))
                value = null;

        return value;
    }

    private static boolean getBoolean(Object object) {
        if (object == null || !(object instanceof Boolean))
            return false;

        return (boolean) object;
    }

}
