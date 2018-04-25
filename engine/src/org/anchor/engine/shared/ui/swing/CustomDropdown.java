package org.anchor.engine.shared.ui.swing;

import java.util.List;

import javax.swing.JComboBox;

import org.anchor.engine.shared.ui.listener.UIListener;

public class CustomDropdown extends JComboBox<String> {

    private static final long serialVersionUID = 6488961091998919751L;

    private UIListener listener;

    public CustomDropdown(List<String> values, UIListener listener) {
        super();

        for (String value : values)
            addItem(value);

        this.listener = listener;
    }

    public UIListener getListener() {
        return listener;
    }

    public void setListener(UIListener listener) {
        this.listener = listener;
    }

}
