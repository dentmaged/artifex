package org.anchor.engine.shared.ui.blueprint;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;

import org.anchor.engine.shared.ui.listener.DropdownListener;
import org.anchor.engine.shared.ui.listener.adapter.DropdownAdapter;
import org.anchor.engine.shared.ui.swing.CustomDropdown;

public class DropdownBlueprint extends UIBlueprint {

    protected String name;
    protected CustomDropdown dropdown;

    public DropdownBlueprint(String name, List<String> values) {
        this(name, values, new DropdownAdapter());
    }

    public DropdownBlueprint(String name, List<String> values, DropdownListener listener) {
        super(listener);
        this.name = name;

        dropdown = new CustomDropdown(values, listener);
        if (listener != null)
            listener.setComponent(dropdown);
    }

    public void setListener(DropdownListener listener) {
        this.listener = listener;
        this.dropdown.setListener(listener);
        this.listener.setComponent(dropdown);
    }

    @Override
    public List<Component> build(int x, int y, int width) {
        JLabel label = new JLabel(name);
        label.setLabelFor(dropdown);
        int w = width / 2 - 30;
        label.setBounds(x, y + 4, w, 14);

        dropdown.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (dropdown.isEnabled())
                    ((DropdownListener) listener).onDropdownSelect(dropdown);
            }

        });
        dropdown.setBounds(x + w + 5, y, width - w - 20, 23);

        return Arrays.asList(label, dropdown);
    }

    @Override
    public int getHeight() {
        return 21;
    }

    public int getSelectedIndex() {
        return dropdown.getSelectedIndex();
    }

    public String getSelectedItem() {
        return (String) dropdown.getSelectedItem();
    }

}
