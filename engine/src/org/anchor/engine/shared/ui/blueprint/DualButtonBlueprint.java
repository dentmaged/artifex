package org.anchor.engine.shared.ui.blueprint;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import org.anchor.engine.shared.ui.listener.ButtonListener;
import org.anchor.engine.shared.ui.listener.UIListener;
import org.anchor.engine.shared.ui.swing.CustomButton;

public class DualButtonBlueprint extends UIBlueprint {

    protected String leftName, rightName;
    protected UIListener rightListener;
    protected CustomButton left, right;

    public DualButtonBlueprint(String leftName, String rightName, ButtonListener listener, ButtonListener rightListener) {
        super(listener);
        this.rightListener = rightListener;
        this.leftName = leftName;
        this.rightName = rightName;

        left = new CustomButton(leftName, listener);
        right = new CustomButton(rightName, listener);

        if (listener != null)
            listener.setComponent(left);
    }

    public String getLeftButtonName() {
        return leftName;
    }

    public String getRightButtonName() {
        return rightName;
    }

    public void setListener(ButtonListener listener) {
        this.listener = listener;
        this.left.setListener(listener);
        this.listener.setComponent(left);
    }

    public void setRightListener(ButtonListener rightListener) {
        this.rightListener = rightListener;
        this.right.setListener(rightListener);
        this.rightListener.setComponent(right);
    }

    @Override
    public List<Component> build(int x, int y, int width) {
        int w = width / 2 - 5;
        left.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                ((ButtonListener) listener).onButtonClick(left);
            }

        });
        left.setBounds(x, y, w - 5, 23);

        right.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                ((ButtonListener) rightListener).onButtonClick(right);
            }

        });
        right.setBounds(x + w, y, w - 5, 23);

        return Arrays.asList(left, right);
    }

    @Override
    public int getHeight() {
        return 23;
    }

}
