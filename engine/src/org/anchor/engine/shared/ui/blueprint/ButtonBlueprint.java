package org.anchor.engine.shared.ui.blueprint;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import org.anchor.engine.shared.ui.listener.ButtonListener;
import org.anchor.engine.shared.ui.swing.CustomButton;

public class ButtonBlueprint extends UIBlueprint {

    protected String name;
    protected CustomButton button;

    public ButtonBlueprint(String name, ButtonListener listener) {
        super(listener);
        this.name = name;

        button = new CustomButton(name, listener);
        if (listener != null)
            listener.setComponent(button);
    }

    public String getName() {
        return name;
    }

    public void setListener(ButtonListener listener) {
        this.listener = listener;
        this.button.setListener(listener);
        this.listener.setComponent(button);
    }

    @Override
    public List<Component> build(int x, int y, int width) {
        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                ((ButtonListener) listener).onButtonClick(button);
            }

        });
        button.setBounds(x, y, width - 15, 23);

        return Arrays.asList(button);
    }

    @Override
    public int getHeight() {
        return 23;
    }

}
