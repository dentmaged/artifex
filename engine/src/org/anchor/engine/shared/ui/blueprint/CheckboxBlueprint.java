package org.anchor.engine.shared.ui.blueprint;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.anchor.engine.shared.ui.listener.CheckboxListener;
import org.anchor.engine.shared.ui.listener.TextFieldListener;
import org.anchor.engine.shared.ui.listener.adapter.CheckboxAdapter;
import org.anchor.engine.shared.ui.swing.CustomCheckbox;

public class CheckboxBlueprint extends UIBlueprint {

    protected String name;
    protected CustomCheckbox checkbox;

    public CheckboxBlueprint(String name, CheckboxListener listener) {
        this(name, false, listener);
    }

    public CheckboxBlueprint(String name, boolean selected) {
        this(name, selected, new CheckboxAdapter());
    }

    public CheckboxBlueprint(String name, boolean selected, CheckboxListener listener) {
        super(listener);
        this.name = name;

        checkbox = new CustomCheckbox(listener);
        checkbox.setSelected(selected);

        if (listener != null)
            listener.setComponent(checkbox);
    }

    public String getName() {
        return name;
    }

    public void setListener(TextFieldListener listener) {
        this.listener = listener;
        this.checkbox.setListener(listener);
        this.listener.setComponent(checkbox);
    }

    @Override
    public List<Component> build(int x, int y, int width) {
        JLabel label = new JLabel(name);
        label.setLabelFor(checkbox);
        int w = width / 2 - 30;
        label.setBounds(x, y + 4, w, 14);

        checkbox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (checkbox.isEnabled())
                    ((CheckboxListener) listener).onCheckboxEdit(checkbox);
            }

        });
        checkbox.setBounds(x + w + 5, y, width - w - 20, 23);
        checkbox.setHorizontalAlignment(SwingConstants.CENTER);

        return Arrays.asList(label, checkbox);
    }

    @Override
    public int getHeight() {
        return 23;
    }

    public CustomCheckbox getTextField() {
        return checkbox;
    }

    public String getText() {
        return checkbox.getText();
    }

}
