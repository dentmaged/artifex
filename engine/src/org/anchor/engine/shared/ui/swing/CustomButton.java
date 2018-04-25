package org.anchor.engine.shared.ui.swing;

import javax.swing.JButton;

import org.anchor.engine.shared.ui.listener.UIListener;

public class CustomButton extends JButton {

    private static final long serialVersionUID = 1079589786419679460L;

    private UIListener listener;

    public CustomButton(String text, UIListener listener) {
        super(text);
        this.listener = listener;
    }

    public UIListener getListener() {
        return listener;
    }

    public void setListener(UIListener listener) {
        this.listener = listener;
    }

}
