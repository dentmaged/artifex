package org.anchor.game.editor.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.anchor.engine.shared.Engine;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.scheduler.ScheduledRunnable;
import org.anchor.engine.shared.scheduler.Scheduler;
import org.anchor.game.client.components.MeshComponent;
import org.anchor.game.client.components.SkyComponent;
import org.anchor.game.client.loaders.AssetLoader;
import org.anchor.game.editor.GameEditor;
import org.anchor.game.editor.commands.Undo;
import org.anchor.game.editor.gizmo.Gizmo;
import org.anchor.game.editor.utils.TransformationMode;
import org.lwjgl.input.Keyboard;
import org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel;

public class Window {

    private JFrame frmEditor;
    private JTabbedPane tabbedPane;

    private JMenu mnView;
    private Map<String, JCheckBoxMenuItem> items = new HashMap<String, JCheckBoxMenuItem>();

    private static Window instance;

    public static final int LEFT = 1;
    public static final int BOTTOM = 2;
    public static final int RIGHT = 3;
    public static final int HIDDEN = 4;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            UIDefaults uiDefaults = UIManager.getDefaults();
            uiDefaults.put("activeCaption", new javax.swing.plaf.ColorUIResource(Color.GRAY));
            JFrame.setDefaultLookAndFeelDecorated(true);
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
        frmEditor.setSize(1934, 1040);
        frmEditor.setLocation(-7, 0);
        frmEditor.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frmEditor.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                if (Undo.hasChangedSinceSave()) {
                    if (JOptionPane.showConfirmDialog(null, "Do you want to save your changes to " + Window.getInstance().getTabName() + "?", "Anchor Engine Editor", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                        GameEditor.save();
                        frmEditor.dispose();
                        System.exit(0);
                    } else {
                        frmEditor.dispose();
                        System.exit(0);
                    }
                } else {
                    frmEditor.dispose();
                    System.exit(0);
                }
            }

        });
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
        setAccelerator(mntmNew, Keyboard.KEY_N, InputEvent.CTRL_MASK);
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
        setAccelerator(mntmOpen, Keyboard.KEY_O, InputEvent.CTRL_MASK);
        mnFile.add(mntmOpen);

        JMenuItem mntmSave = new JMenuItem("Save");
        mntmSave.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                GameEditor.save();
            }

        });
        setAccelerator(mntmSave, Keyboard.KEY_S, InputEvent.CTRL_MASK);
        mnFile.add(mntmSave);

        JMenuItem mntmSaveAs = new JMenuItem("Save As");
        mntmSaveAs.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (GameEditor.chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
                    GameEditor.saveAs(GameEditor.chooser.getSelectedFile());
            }

        });
        setAccelerator(mntmSaveAs, Keyboard.KEY_S, InputEvent.CTRL_MASK + InputEvent.SHIFT_MASK);
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
        mntmUndo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Undo.undo();
            }

        });
        setAccelerator(mntmUndo, Keyboard.KEY_Z, InputEvent.CTRL_MASK);
        mnEdit.add(mntmUndo);

        JMenuItem mntmRedo = new JMenuItem("Redo");
        mntmRedo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Undo.redo();
            }

        });
        setAccelerator(mntmRedo, Keyboard.KEY_Y, InputEvent.CTRL_MASK);
        mnEdit.add(mntmRedo);
        mnEdit.addSeparator();

        JMenuItem mntmCopy = new JMenuItem("Copy");
        mntmCopy.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                GameEditor.copy();
            }

        });
        setAccelerator(mntmCopy, Keyboard.KEY_C, InputEvent.CTRL_MASK);
        mnEdit.add(mntmCopy);

        JMenuItem mntmPaste = new JMenuItem("Paste");
        mntmPaste.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                GameEditor.paste();
            }

        });
        setAccelerator(mntmPaste, Keyboard.KEY_V, InputEvent.CTRL_MASK);
        mnEdit.add(mntmPaste);
        mnEdit.addSeparator();

        JMenuItem mntmHideSelection = new JMenuItem("Hide Selection");
        mntmHideSelection.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (LevelEditor.getInstance().getSelectedEntity() == null)
                    return;

                LevelEditor.getInstance().getSelectedEntity().getComponent(MeshComponent.class).visible = false;
            }

        });
        setAccelerator(mntmHideSelection, Keyboard.KEY_H, 0);
        mnEdit.add(mntmHideSelection);

        JMenuItem mntmHideExceptSelection = new JMenuItem("Hide Except Selection");
        mntmHideExceptSelection.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for (Entity entity : Engine.getEntitiesWithComponent(MeshComponent.class))
                    if (entity != LevelEditor.getInstance().getSelectedEntity() && !entity.hasComponent(SkyComponent.class))
                        entity.getComponent(MeshComponent.class).visible = false;
            }

        });
        setAccelerator(mntmHideExceptSelection, Keyboard.KEY_H, InputEvent.CTRL_MASK + InputEvent.SHIFT_MASK);
        mnEdit.add(mntmHideExceptSelection);

        JMenuItem mntmUnhideAll = new JMenuItem("Unhide All");
        mntmUnhideAll.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for (Entity entity : Engine.getEntitiesWithComponent(MeshComponent.class))
                    entity.getComponent(MeshComponent.class).visible = true;
            }

        });
        setAccelerator(mntmUnhideAll, Keyboard.KEY_H, InputEvent.CTRL_MASK);
        mnEdit.add(mntmUnhideAll);
        mnEdit.addSeparator();

        JMenuItem mntmFreezeSelection = new JMenuItem("Freeze Selection");
        mntmFreezeSelection.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (LevelEditor.getInstance().getSelectedEntity() == null)
                    return;

                LevelEditor.getInstance().getSelectedEntity().setHidden(true);
            }

        });
        setAccelerator(mntmFreezeSelection, Keyboard.KEY_F, 0);
        mnEdit.add(mntmFreezeSelection);

        JMenuItem mntmFreezeExceptSelection = new JMenuItem("Freeze Except Selection");
        mntmFreezeExceptSelection.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for (Entity entity : Engine.getEntitiesWithComponent(MeshComponent.class))
                    if (entity != LevelEditor.getInstance().getSelectedEntity() && !entity.hasComponent(SkyComponent.class))
                        entity.setHidden(true);
            }

        });
        setAccelerator(mntmFreezeExceptSelection, Keyboard.KEY_F, InputEvent.CTRL_MASK + InputEvent.SHIFT_MASK);
        mnEdit.add(mntmFreezeExceptSelection);

        JMenuItem mntmUnfreezeAll = new JMenuItem("Unfreeze All");
        mntmUnfreezeAll.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for (Entity entity : Engine.getEntitiesWithComponent(MeshComponent.class))
                    if (!entity.hasComponent(SkyComponent.class))
                        entity.setHidden(false);
            }

        });
        setAccelerator(mntmUnfreezeAll, Keyboard.KEY_F, InputEvent.CTRL_MASK);
        mnEdit.add(mntmUnfreezeAll);

        JMenu mnTools = new JMenu("Tools");
        mnTools.setMnemonic('T');
        menuBar.add(mnTools);

        JMenuItem mntmRefreshModels = new JMenuItem("Reload Models");
        mntmRefreshModels.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                AssetLoader.reloadModels();
            }

        });
        mnTools.add(mntmRefreshModels);

        JMenuItem mntmRefreshMaterials = new JMenuItem("Reload Materials");
        mntmRefreshMaterials.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                AssetLoader.reloadMaterials();
            }

        });
        mnTools.add(mntmRefreshMaterials);

        JMenuItem mntmRefreshParticles = new JMenuItem("Reload Particles");
        mntmRefreshParticles.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                AssetLoader.reloadParticles();
            }

        });
        mnTools.add(mntmRefreshParticles);

        mnView = new JMenu("View");
        mnView.setMnemonic('V');
        menuBar.add(mnView);

        JCheckBoxMenuItem mntmModo = new JCheckBoxMenuItem("Use Modo Gizmo");
        mntmModo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Scheduler.schedule(new ScheduledRunnable() {

                    @Override
                    public void tick(float time, float percentage) {

                    }

                    @Override
                    public void finish() {
                        GameEditor.getInstance().setModo(mntmModo.getState());
                    }

                }, 1);
            }

        });
        mntmModo.setState(true);
        setAccelerator(mntmModo, Keyboard.KEY_M, InputEvent.CTRL_MASK + InputEvent.SHIFT_MASK);
        mnView.add(mntmModo);

        JCheckBoxMenuItem mntmShowAABBs = new JCheckBoxMenuItem("Show All AABBs");
        mntmShowAABBs.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                GameEditor.getInstance().setShowAllAABBs(mntmShowAABBs.getState());
            }

        });
        setAccelerator(mntmShowAABBs, Keyboard.KEY_B, InputEvent.CTRL_MASK + InputEvent.SHIFT_MASK);
        mnView.add(mntmShowAABBs);
        mnView.addSeparator();

        JMenuItem mntmEmpty = new JMenuItem("Empty");
        mntmEmpty.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                JFrame frame = new JFrame("Anchor Engine Editor");
                frame.setAlwaysOnTop(true);
                frame.setSize(333, 689);
                frame.setLayout(new BorderLayout());

                DraggableTabbedPane tabbedPane = new DraggableTabbedPane() {

                    private static final long serialVersionUID = -4235545834783916170L;

                    @Override
                    public void remove(int index) {
                        super.remove(index);

                        if (getTabCount() == 0)
                            frame.setVisible(false);
                    }

                };
                frame.add(tabbedPane, BorderLayout.CENTER);

                frame.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosing(WindowEvent arg0) {
                        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                            for (int j = 0; j < mnView.getItemCount(); j++) {
                                JMenuItem item = mnView.getItem(j);

                                if (item != null && item.getText().equals(tabbedPane.getTitleAt(i)))
                                    ((JCheckBoxMenuItem) item).setState(false);
                            }
                        }
                    }

                });

                frame.setVisible(true);
            }

        });
        setAccelerator(mntmEmpty, Keyboard.KEY_N, InputEvent.CTRL_MASK + InputEvent.SHIFT_MASK);
        mnView.add(mntmEmpty);
        mnView.addSeparator();

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
        btnUndo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Undo.undo();
            }

        });
        toolBar.add(btnUndo);

        JButton btnRedo = new JButton("Redo");
        btnRedo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Undo.redo();
            }

        });
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
        transformationModeDropdown.setModel(new DefaultComboBoxModel<TransformationMode>(TransformationMode.values()));
        transformationModeDropdown.setSelectedIndex(1);
        transformationModeDropdown.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                GameEditor.getInstance().setTransformationMode((TransformationMode) transformationModeDropdown.getSelectedItem());
            }

        });
        toolBar.add(transformationModeDropdown);
    }

    public void addTab(String name, JPanel panel) {
        tabbedPane.addTab(name, null, panel, null);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
    }

    public String getTabName() {
        return tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
    }

    public void setTabName(String name) {
        tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), name);
    }

    public void registerPanel(String title, JComponent component) {
        registerPanel(title, component, HIDDEN);
    }

    public void registerPanel(String title, JComponent component, int position) {
        JCheckBoxMenuItem mntm = new JCheckBoxMenuItem(title);

        mntm.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (component.getParent() != null) {
                    Object current = component.getParent().getParent().getParent().getParent().getParent();
                    if (current instanceof JFrame)
                        ((JFrame) current).setVisible(false);
                    else if (current instanceof JInternalFrame)
                        if (component.getParent() instanceof JTabbedPane && ((JTabbedPane) component.getParent()).getTabCount() == 1)
                            ((JTabbedPane) component.getParent()).remove(0); // handles hiding of window
                }

                JFrame frame = new JFrame("Anchor Engine Editor");
                frame.setAlwaysOnTop(true);
                frame.setSize(333, 689);
                frame.setLayout(new BorderLayout());
                mntm.setState(true);

                DraggableTabbedPane tabbedPane = new DraggableTabbedPane() {

                    private static final long serialVersionUID = -4235545834783916170L;

                    @Override
                    public void remove(int index) {
                        super.remove(index);

                        if (getTabCount() == 0)
                            frame.setVisible(false);
                    }

                };
                frame.add(tabbedPane, BorderLayout.CENTER);

                frame.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosing(WindowEvent arg0) {
                        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                            for (int j = 0; j < mnView.getItemCount(); j++) {
                                JMenuItem item = mnView.getItem(j);

                                if (item != null && item.getText().equals(tabbedPane.getTitleAt(i)))
                                    ((JCheckBoxMenuItem) item).setState(false);
                            }
                        }
                    }

                });

                tabbedPane.addTab(title, component);
                frame.setVisible(true);
            }

        });

        items.put(title.toLowerCase(), mntm);
        mnView.add(mntm);

        if (position == LEFT || position == RIGHT) {
            JTabbedPane target = LevelEditor.getInstance().getLeftDock();
            if (position == RIGHT)
                target = LevelEditor.getInstance().getRightDock();

            target.addTab(title, component);
            mntm.setState(true);
        }
    }

    public void registerWindow(JInternalFrame frame, int position) {
        JCheckBoxMenuItem mntm = new JCheckBoxMenuItem(frame.getTitle());

        mntm.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(mntm.getState());

                JInternalFrame gameFrame = LevelEditor.getInstance().getGameFrame();
                Rectangle bounds = gameFrame.getBounds();
                if (position == BOTTOM)
                    gameFrame.setBounds(bounds.x, bounds.y, bounds.width, bounds.height + frame.getBounds().height * (mntm.getState() ? -1 : 1));
            }

        });
        mntm.setState(true);
        frame.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
        frame.addInternalFrameListener(new InternalFrameAdapter() {

            @Override
            public void internalFrameClosing(InternalFrameEvent arg0) {
                mntm.setState(false);
                frame.setVisible(false);

                JInternalFrame gameFrame = LevelEditor.getInstance().getGameFrame();
                Rectangle bounds = gameFrame.getBounds();
                if (position == BOTTOM)
                    gameFrame.setBounds(bounds.x, bounds.y, bounds.width, bounds.height + frame.getBounds().height);
            }

        });

        items.put(frame.getTitle().toLowerCase(), mntm);
        mnView.add(mntm);
    }

    private void setAccelerator(JMenuItem item, int lwjglKey, int mask) {
        item.setAccelerator(KeyStroke.getKeyStroke(KeyStroke.getKeyStroke(Keyboard.getKeyName(lwjglKey)).getKeyCode(), mask));
        GameEditor.registerAccelerator(item, lwjglKey, mask);
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
