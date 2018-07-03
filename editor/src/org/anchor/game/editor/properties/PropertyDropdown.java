package org.anchor.game.editor.properties;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.List;

import javax.swing.JComboBox;

import org.anchor.engine.shared.entity.Entity;

public abstract class PropertyDropdown extends JComboBox<String> {

    protected Entity entity;
    protected Field field;
    protected Object object;

    protected String previousValue;

    private static final long serialVersionUID = -586156248454660986L;

    public PropertyDropdown(Entity entity, Field field, Object object, List<String> values) {
        for (String value : values)
            addItem(value);

        addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String value = (String) getSelectedItem();
                if (value.equals(previousValue))
                    return;

                previousValue = value;
                try {
                    field.set(object, convert(value));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        this.entity = entity;
        this.field = field;
        this.object = object;
    }

    public abstract void update();

    public abstract Object convert(String value);

}
