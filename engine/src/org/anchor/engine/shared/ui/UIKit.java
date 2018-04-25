package org.anchor.engine.shared.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.anchor.engine.common.utils.Pointer;
import org.anchor.engine.shared.ui.blueprint.UIBlueprint;

public class UIKit {

    public static int DOCK_WIDTH = 317;
    public static int SUBPANEL_WIDTH = 212;
    public static int SUBPANEL_X = (DOCK_WIDTH - SUBPANEL_WIDTH) / 2;

    public static JPanel create(List<UIBlueprint> blueprints, Pointer<Integer> height) {
        JPanel parent = new JPanel();
        JPanel panel = createJPanel(parent);

        int x = 10;
        int y = 10;
        int width = DOCK_WIDTH - 20;
        int last = 0;

        for (UIBlueprint blueprint : blueprints) {
            for (Component component : blueprint.build(x, y, width))
                panel.add(component);

            last = blueprint.getHeight() + 5;
            y += last;
        }
        height.set(y + last - 5);

        return parent;
    }

    public static JScrollPane createSubpanel(String name, List<UIBlueprint> blueprints, Pointer<Integer> height) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        JScrollPane pane = new JScrollPane(panel);
        pane.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), name, TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));

        int x = 10;
        int y = 0;
        int width = SUBPANEL_WIDTH - 20;
        int last = 0;

        for (UIBlueprint blueprint : blueprints) {
            for (Component component : blueprint.build(x, y, width))
                panel.add(component);

            last = blueprint.getHeight() + 5;
            y += last;
        }
        height.set(y + last - 5);

        return pane;
    }

    public static JPanel createJPanel(JPanel parent) {
        parent.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(null);

        JScrollPane scrollPane = new JScrollPane(panel);
        parent.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    public static JTree createTree(JPanel parent, int x, int y, int width, int height) {
        parent.setLayout(new BorderLayout());
        parent.setBounds(x, y, width, height);

        JTree tree = new JTree();
        JScrollPane scrollPane = new JScrollPane(tree);
        parent.add(scrollPane, BorderLayout.CENTER);

        return tree;
    }

    public static JTree createTree(JPanel panel) {
        panel.setLayout(new BorderLayout());
        JTree tree = new JTree();

        JScrollPane scrollPane = new JScrollPane(tree);
        panel.add(scrollPane, BorderLayout.CENTER);

        return tree;
    }

    public static void move(JInternalFrame internal) {
        JFrame frame = new JFrame(internal.getTitle());
        Rectangle bounds = internal.getBounds();
        frame.setSize(bounds.width - 3, bounds.height - 3);
        frame.setResizable(internal.isResizable());

        frame.getContentPane().setLayout(internal.getContentPane().getLayout());
        for (Component component : internal.getContentPane().getComponents())
            frame.getContentPane().add(component);

        internal.setVisible(false);

        Container parent = internal.getParent();
        parent.remove(internal);

        parent.revalidate();
        parent.repaint();

        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent arg0) {
                internal.setBounds(bounds.x, bounds.y, frame.getSize().width + 3, frame.getSize().height + 3);

                internal.getContentPane().setLayout(frame.getContentPane().getLayout());
                for (Component component : frame.getContentPane().getComponents())
                    internal.getContentPane().add(component);

                frame.setVisible(false);
                internal.setVisible(true);
                parent.add(internal);

                parent.revalidate();
                parent.repaint();
            }

        });
        frame.setVisible(true);
    }
}
