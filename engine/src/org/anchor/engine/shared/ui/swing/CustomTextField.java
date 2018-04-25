package org.anchor.engine.shared.ui.swing;

import javax.swing.JTextField;

import org.anchor.engine.shared.ui.listener.UIListener;

public class CustomTextField extends JTextField {

    private static final long serialVersionUID = -1906102877197920315L;

    private UIListener listener;

    public CustomTextField(UIListener listener) {
        this.listener = listener;
    }

    public UIListener getListener() {
        return listener;
    }

    public void setListener(UIListener listener) {
        this.listener = listener;
    }

}
