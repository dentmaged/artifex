package org.anchor.game.editor.properties;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComboBox;

import org.anchor.engine.common.utils.JavaField;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.game.editor.commands.Undo;

public abstract class PropertyDropdown extends JComboBox<String> {

    protected Entity entity;
    protected JavaField field;
    protected Object object;

    protected String previousValue;

    private static final long serialVersionUID = -586156248454660986L;

    public PropertyDropdown(Entity entity, JavaField field, Object object, List<String> values) {
        for (String value : values)
            addItem(value);

        addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isEnabled())
                    return;

                String value = (String) getSelectedItem();
                if (value.equals(previousValue))
                    return;

                previousValue = value;
                try {
                    Undo.fieldSet(field, object, convert(value));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        this.entity = entity;
        this.field = field;
        this.object = object;
    }

    public void update() {
        setEnabled(false);
        updateDropdown();
        setEnabled(true);
    }

    protected abstract void updateDropdown();

    public abstract Object convert(String value);

}
