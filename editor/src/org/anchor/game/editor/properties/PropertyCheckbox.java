package org.anchor.game.editor.properties;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import org.anchor.engine.common.utils.JavaField;
import org.anchor.game.editor.commands.Undo;

public class PropertyCheckbox extends JCheckBox {

    protected Object object;
    protected JavaField field;

    protected String previousValue;

    private static final long serialVersionUID = -586156248454660986L;

    public PropertyCheckbox(JavaField field, Object object) {
        setSelected((boolean) field.get(object));
        addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isEnabled())
                    return;

                Undo.fieldSet(field, object, isSelected());
            }

        });

        this.field = field;
        this.object = object;
    }

    public void update() {
        setEnabled(false);
        setSelected((boolean) field.get(object));
        setEnabled(true);
    }

}
