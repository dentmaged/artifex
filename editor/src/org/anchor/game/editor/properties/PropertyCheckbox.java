package org.anchor.game.editor.properties;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;

import javax.swing.JCheckBox;

public class PropertyCheckbox extends JCheckBox {

    protected Object object;
    protected Field field;

    protected String previousValue;

    private static final long serialVersionUID = -586156248454660986L;

    public PropertyCheckbox(Field field, Object object) {
        setSelected(get(field, object));
        addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    field.set(object, isSelected());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        });

        this.field = field;
        this.object = object;
    }

    public void update() {
        setSelected(get(field, object));
    }

    public static boolean get(Field field, Object object) {
        try {
            return (boolean) field.get(object);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

}
