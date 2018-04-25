package org.anchor.game.editor.ui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.anchor.game.editor.GameEditor;

public class StartupPanel extends JPanel {

    private static final long serialVersionUID = -7296699179392720494L;

    public StartupPanel() {
        setLayout(null);

        JButton btnNew = new JButton("New");
        btnNew.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                GameEditor.create();
            }

        });
        btnNew.setBounds(970, 388, 200, 50);
        add(btnNew);

        JButton btnOpen = new JButton("Open");
        btnOpen.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (GameEditor.chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
                    GameEditor.open(GameEditor.chooser.getSelectedFile());
            }

        });
        btnOpen.setBounds(970, 446, 200, 50);
        add(btnOpen);

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                System.exit(0);
            }

        });
        btnClose.setBounds(970, 508, 200, 50);
        add(btnClose);

        JLabel lblAnchorEditor = new JLabel("Anchor Editor");
        lblAnchorEditor.setHorizontalAlignment(SwingConstants.CENTER);
        lblAnchorEditor.setFont(new Font("Tahoma", Font.PLAIN, 29));
        lblAnchorEditor.setBounds(697, 424, 261, 50);
        add(lblAnchorEditor);

        JLabel lblCopyright = new JLabel("<html><center>\r\nVersion 1.0.0<br />\r\n\u00A9 2018 Anchor Development. All rights reserved.\r\n</center></html>");
        lblCopyright.setVerticalAlignment(SwingConstants.TOP);
        lblCopyright.setBounds(697, 482, 261, 39);
        add(lblCopyright);
    }

}
