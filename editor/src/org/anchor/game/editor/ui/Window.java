package org.anchor.game.editor.ui;

import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.anchor.game.editor.GameEditor;
import org.anchor.game.editor.gizmo.Gizmo;
import org.anchor.game.editor.utils.TransformationMode;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel;

public class Window {

    private JFrame frmEditor;
    private JTabbedPane tabbedPane;

    private static Window instance;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new SubstanceGraphiteLookAndFeel());
            UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        instance = new Window();
        instance.frmEditor.setVisible(true);
        try {
            instance.frmEditor.setIconImage(ImageIO.read(Window.class.getResourceAsStream("/icon.png")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the application.
     */
    public Window() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frmEditor = new JFrame("Anchor Engine Editor");
        frmEditor.setSize(1934, 1056);
        frmEditor.setLocation(-7, 0);
        frmEditor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmEditor.getContentPane().setLayout(null);

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBounds(0, 0, 1904, 21);
        frmEditor.getContentPane().add(menuBar);

        JMenu mnFile = new JMenu("File");
        mnFile.setMnemonic('F');
        menuBar.add(mnFile);

        JMenuItem mntmNew = new JMenuItem("New");
        mntmNew.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                GameEditor.create();
            }

        });
        mntmNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
        mnFile.add(mntmNew);

        mnFile.addSeparator();

        JMenuItem mntmOpen = new JMenuItem("Open");
        mntmOpen.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (GameEditor.chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
                    GameEditor.open(GameEditor.chooser.getSelectedFile());
            }

        });
        mntmOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        mnFile.add(mntmOpen);

        JMenuItem mntmSave = new JMenuItem("Save");
        mntmSave.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                GameEditor.save();
            }

        });
        mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        mnFile.add(mntmSave);

        JMenuItem mntmSaveAs = new JMenuItem("Save As");
        mntmSaveAs.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (GameEditor.chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
                    GameEditor.saveAs(GameEditor.chooser.getSelectedFile());
            }

        });
        mntmSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK + InputEvent.SHIFT_MASK));
        mnFile.add(mntmSaveAs);

        mnFile.addSeparator();

        JMenuItem mntmClose = new JMenuItem("Close");
        mntmClose.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                System.exit(0);
            }

        });
        mnFile.add(mntmClose);

        JMenu mnEdit = new JMenu("Edit");
        mnEdit.setMnemonic('E');
        menuBar.add(mnEdit);

        JMenuItem mntmUndo = new JMenuItem("Undo");
        mntmUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
        mnEdit.add(mntmUndo);

        JMenuItem mntmRedo = new JMenuItem("Redo");
        mntmRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
        mnEdit.add(mntmRedo);

        JMenuItem mntmCopy = new JMenuItem("Copy");
        mntmCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
        mnEdit.add(mntmCopy);

        JMenuItem mntmPaste = new JMenuItem("Paste");
        mntmPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
        mnEdit.add(mntmPaste);

        JMenu mnView = new JMenu("View");
        mnView.setMnemonic('V');
        menuBar.add(mnView);

        JToolBar ioToolbar = new JToolBar();
        ioToolbar.setRollover(true);
        ioToolbar.setBounds(0, 21, 168, 30);
        frmEditor.getContentPane().add(ioToolbar);

        JButton btnOpen = new JButton("Open");
        btnOpen.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                if (GameEditor.chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
                    GameEditor.open(GameEditor.chooser.getSelectedFile());
            }

        });
        ioToolbar.add(btnOpen);

        JButton btnSave = new JButton("Save");
        btnSave.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                GameEditor.save();
            }

        });
        ioToolbar.add(btnSave);

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBounds(0, 53, 1918, 964);
        frmEditor.getContentPane().add(tabbedPane);

        tabbedPane.addTab("Startup", null, new StartupPanel(), null);

        JToolBar toolBar = new JToolBar();
        toolBar.setBounds(168, 21, 528, 30);
        frmEditor.getContentPane().add(toolBar);

        JButton btnUndo = new JButton("Undo");
        toolBar.add(btnUndo);

        JButton btnRedo = new JButton("Redo");
        toolBar.add(btnRedo);

        JButton btnSelect = new JButton("Select");
        btnSelect.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                GameEditor.getInstance().setMode(Gizmo.SELECT_MODE);
            }

        });
        toolBar.add(btnSelect);

        JButton btnTranslate = new JButton("Translate");
        btnTranslate.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                GameEditor.getInstance().setMode(Gizmo.TRANSLATE_MODE);
            }

        });
        toolBar.add(btnTranslate);

        JButton btnRotate = new JButton("Rotate");
        btnRotate.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                GameEditor.getInstance().setMode(Gizmo.ROTATE_MODE);
            }

        });
        toolBar.add(btnRotate);

        JButton btnScale = new JButton("Scale");
        btnScale.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                GameEditor.getInstance().setMode(Gizmo.SCALE_MODE);
            }

        });
        toolBar.add(btnScale);

        JComboBox<TransformationMode> transformationModeDropdown = new JComboBox<TransformationMode>();
        transformationModeDropdown.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                GameEditor.getInstance().setTransformationMode((TransformationMode) transformationModeDropdown.getSelectedItem());
            }

        });
        transformationModeDropdown.setModel(new DefaultComboBoxModel<TransformationMode>(TransformationMode.values()));
        toolBar.add(transformationModeDropdown);
    }

    public void addTab(String name, JPanel panel) {
        tabbedPane.addTab(name, null, panel, null);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
    }

    public void setTabName(String name) {
        tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), name);
    }

    public JFrame getFrame() {
        return frmEditor;
    }

    public int getWindowSpaceMouseX() {
        return MouseInfo.getPointerInfo().getLocation().x - frmEditor.getLocationOnScreen().x;
    }

    public int getWindowSpaceMouseY() {
        return MouseInfo.getPointerInfo().getLocation().y - frmEditor.getLocationOnScreen().y;
    }

    public static Window getInstance() {
        return instance;
    }

}
