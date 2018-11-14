package org.anchor.engine.shared.ui.blueprint;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;

import org.anchor.engine.shared.ui.listener.ButtonListener;
import org.anchor.engine.shared.ui.swing.CustomButton;

public class LabelledButtonBlueprint extends UIBlueprint {

    protected String label, name;
    protected CustomButton button;

    public LabelledButtonBlueprint(String label, String name, ButtonListener listener) {
        super(listener);
        this.label = label;
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
        JLabel label = new JLabel(this.label);
        label.setToolTipText(this.label);
        label.setLabelFor(button);
        int w = width / 2 - 30;
        label.setBounds(x, y + 4, w, 14);

        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                ((ButtonListener) listener).onButtonClick(button);
            }

        });
        button.setBounds(x + w + 5, y, width - w - 15, 23);

        return Arrays.asList(label, button);
    }

    @Override
    public int getHeight() {
        return 23;
    }

}
