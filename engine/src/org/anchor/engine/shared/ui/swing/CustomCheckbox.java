package org.anchor.engine.shared.ui.swing;

import javax.swing.JCheckBox;

import org.anchor.engine.shared.ui.listener.UIListener;

public class CustomCheckbox extends JCheckBox {

    private static final long serialVersionUID = 6488961091998919751L;

    private UIListener listener;

    public CustomCheckbox(UIListener listener) {
        super();

        this.listener = listener;
    }

    public UIListener getListener() {
        return listener;
    }

    public void setListener(UIListener listener) {
        this.listener = listener;
    }

}
