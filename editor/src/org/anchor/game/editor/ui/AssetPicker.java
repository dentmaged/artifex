package org.anchor.game.editor.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;

import org.anchor.engine.common.utils.FileHelper;
import org.anchor.engine.shared.editor.TransformableObject;
import org.anchor.engine.shared.terrain.Terrain;
import org.anchor.engine.shared.ui.UIKit;
import org.anchor.engine.shared.ui.blueprint.LabelledButtonBlueprint;
import org.anchor.engine.shared.ui.listener.ButtonListener;
import org.anchor.engine.shared.ui.swing.CustomButton;
import org.anchor.game.editor.utils.file.FileCallback;
import org.anchor.game.editor.utils.file.FilePickCallback;
import org.anchor.game.editor.utils.file.Filter;

public class AssetPicker {

    public static void pick(String extension, FileCallback callback) {
        JFrame frame = new JFrame();
        frame.setAlwaysOnTop(true);
        frame.setTitle("Asset Browser");
        frame.setSize(300, 525);
        frame.setLocationRelativeTo(Window.getInstance().getFrame());
        frame.setResizable(false);

        JPanel panel = new JPanel();
        frame.add(panel);

        JTree tree = UIKit.createTree(panel);
        tree.setModel(new FileSystemModel(new File("res"), new Filter() {

            @Override
            public boolean allow(File file) {
                if (file.isDirectory())
                    return true;

                return file.getName().toLowerCase().endsWith("." + extension);
            }

        }));
        tree.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    if (tree.getLastSelectedPathComponent() == null) // sometimes happens
                        return;

                    File file = (File) ((CustomMutableTreeNode) tree.getLastSelectedPathComponent()).getStorage();
                    if (file.isDirectory()) {
                        if (file.equals(new File("res")))
                            callback.run(null);
                        else
                            return;
                    }

                    callback.run(file);
                    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                }
            }

        });

        expand(tree, 0, tree.getRowCount());
        frame.setVisible(true);
    }

    public static LabelledButtonBlueprint create(String name, String current, String extension, FilePickCallback callback) {
        return new LabelledButtonBlueprint(name, current != null ? current : "Choose file", new ButtonListener() {

            public void onSelectionChange(TransformableObject object, boolean added) {
                callback.component = component;
                callback.onSelectionChange(object, added);
            }

            public void onTerrainSelect(Terrain previous, Terrain current) {
                callback.component = component;
                callback.onTerrainSelect(previous, current);
            }

            @Override
            public void onButtonClick(CustomButton button) {
                pick(extension, new FileCallback() {

                    @Override
                    public void run(File file) {
                        button.setText(FileHelper.localFileName(file));
                        callback.run(file);
                    }

                });
            }

        });
    }

    public static JButton create(String current, String extension, FileCallback callback) {
        JButton button = new JButton(current != null ? current : "Choose file");
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                pick(extension, new FileCallback() {

                    @Override
                    public void run(File file) {
                        button.setText(FileHelper.localFileName(file));

                        callback.run(file);
                    }

                });
            }

        });

        return button;
    }

    private static void expand(JTree tree, int start, int count) {
        for (int i = start; i < count; i++)
            tree.expandRow(i);

        if (tree.getRowCount() != count)
            expand(tree, count, tree.getRowCount());
    }

}
