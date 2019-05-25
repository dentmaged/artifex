package org.anchor.game.editor.ui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDesktopPane;
import javax.swing.JEditorPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import org.anchor.client.engine.renderer.Settings;
import org.anchor.engine.common.Log;
import org.anchor.engine.common.TextureType;
import org.anchor.engine.common.utils.ArrayUtils;
import org.anchor.engine.common.utils.FileHelper;
import org.anchor.engine.common.utils.Pointer;
import org.anchor.engine.shared.Engine;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.components.PhysicsComponent;
import org.anchor.engine.shared.components.ScriptComponent;
import org.anchor.engine.shared.components.SpawnComponent;
import org.anchor.engine.shared.components.TransformComponent;
import org.anchor.engine.shared.editor.TransformableObject;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.scheduler.ScheduledRunnable;
import org.anchor.engine.shared.scheduler.Scheduler;
import org.anchor.engine.shared.terrain.Terrain;
import org.anchor.engine.shared.ui.UIKit;
import org.anchor.engine.shared.ui.blueprint.ButtonBlueprint;
import org.anchor.engine.shared.ui.blueprint.CheckboxBlueprint;
import org.anchor.engine.shared.ui.blueprint.DropdownBlueprint;
import org.anchor.engine.shared.ui.blueprint.DualButtonBlueprint;
import org.anchor.engine.shared.ui.blueprint.TextFieldBlueprint;
import org.anchor.engine.shared.ui.blueprint.UIBlueprint;
import org.anchor.engine.shared.ui.listener.ButtonListener;
import org.anchor.engine.shared.ui.listener.CheckboxListener;
import org.anchor.engine.shared.ui.listener.DropdownListener;
import org.anchor.engine.shared.ui.listener.TextFieldListener;
import org.anchor.engine.shared.ui.listener.UIListener;
import org.anchor.engine.shared.ui.swing.CustomButton;
import org.anchor.engine.shared.ui.swing.CustomCheckbox;
import org.anchor.engine.shared.ui.swing.CustomDropdown;
import org.anchor.engine.shared.ui.swing.CustomTextField;
import org.anchor.engine.shared.utils.Layer;
import org.anchor.engine.shared.utils.TerrainUtils;
import org.anchor.game.client.ClientGameVariables;
import org.anchor.game.client.GameClient;
import org.anchor.game.client.app.AppManager;
import org.anchor.game.client.async.Requester;
import org.anchor.game.client.components.DecalComponent;
import org.anchor.game.client.components.LightComponent;
import org.anchor.game.client.components.MeshComponent;
import org.anchor.game.client.components.ParticleSystemComponent;
import org.anchor.game.client.components.ReflectionProbeComponent;
import org.anchor.game.client.components.SlidingDoorComponent;
import org.anchor.game.client.components.SoundComponent;
import org.anchor.game.client.components.SunComponent;
import org.anchor.game.client.loaders.AssetLoader;
import org.anchor.game.client.storage.PrefabReader;
import org.anchor.game.client.types.ClientTerrain;
import org.anchor.game.editor.GameEditor;
import org.anchor.game.editor.commands.Undo;
import org.anchor.game.editor.editableMesh.EditableMesh;
import org.anchor.game.editor.editableMesh.components.EditableMeshComponent;
import org.anchor.game.editor.editableMesh.components.VertexComponent;
import org.anchor.game.editor.editableMesh.types.Vertex;
import org.anchor.game.editor.properties.PropertyUIKit;
import org.anchor.game.editor.terrain.brush.IncreaseDecreaseHeightBrush;
import org.anchor.game.editor.terrain.brush.SetHeightBrush;
import org.anchor.game.editor.terrain.brush.SmoothBrush;
import org.anchor.game.editor.terrain.brush.TerrainBrush;
import org.anchor.game.editor.terrain.shape.Shape;
import org.anchor.game.editor.utils.LogRedirector;
import org.anchor.game.editor.utils.RenderSettings;
import org.anchor.game.editor.utils.file.AssetManager;
import org.anchor.game.editor.utils.file.FilePickCallback;
import org.lwjgl.util.vector.Vector3f;

/*
 * Note: The gap between subpanels is 19px.
 */
public class LevelEditor extends JPanel {

    private JTextField xTransformField, yTransformField, zTransformField;
    private JLabel lblObjectsSelected;
    private static final long serialVersionUID = 1625891826028289732L;

    private JDesktopPane desktopPane;
    private JInternalFrame sceneFrame, actionFrame, gameFrame, logFrame;

    private JTabbedPane leftDock, rightDock;

    private JTabbedPane tabbedPane;
    private JPopupMenu scenePopupMenu, assetBrowserPopupMenu;

    private Canvas canvas;
    private JTable environmentPropertiesTable;
    private JTree tree;

    private boolean ignoreTreeEvent;
    private Layer layer;

    private Window window;
    private GameEditor gameEditor;
    private List<TransformableObject> selected = new ArrayList<TransformableObject>();
    private ClientTerrain selectedTerrain;
    private JPanel components;

    private Map<JButton, JPanel> panels = new HashMap<JButton, JPanel>();
    private Map<JButton, JButton> deleteButtons = new HashMap<JButton, JButton>();
    private Map<Entity, Integer> treePosition = new HashMap<Entity, Integer>();
    private JTextField snapField;

    private static float MB = 1024 * 1024;
    private JTextField speedField;
    private JLabel lblMbRAM;
    private boolean updateRAM, mouseDown, resizing;
    private long lastUpdateTime;

    private float radius = 5;
    private float strength = 1;
    private TerrainBrush selectedBrush = BRUSHES[0];
    private Shape selectedShape = SHAPES[0];

    protected boolean paint, terraform;

    private static Class<?>[] COMPONENTS = new Class[] { MeshComponent.class, LightComponent.class, PhysicsComponent.class, SoundComponent.class, SpawnComponent.class, SlidingDoorComponent.class, DecalComponent.class, ParticleSystemComponent.class, ReflectionProbeComponent.class, SunComponent.class, ScriptComponent.class };

    private static TerrainBrush[] BRUSHES = new TerrainBrush[] { new IncreaseDecreaseHeightBrush(), new SetHeightBrush(), new SmoothBrush() };

    private static Shape[] SHAPES = AssetManager.getShapes();
    private static LevelEditor instance;

    public LevelEditor() {
        instance = this;
        leftDock = new DraggableTabbedPane() {

            private static final long serialVersionUID = -5027117767044676642L;

            @Override
            public void remove(int index) {
                super.remove(index);

                if (getTabCount() == 0) {
                    sceneFrame.setVisible(false);

                    Rectangle bounds = gameFrame.getBounds();
                    gameFrame.setBounds(sceneFrame.getBounds().x, bounds.y, bounds.width + sceneFrame.getBounds().width, bounds.height);

                    bounds = logFrame.getBounds();
                    logFrame.setBounds(sceneFrame.getBounds().x, bounds.y, bounds.width + sceneFrame.getBounds().width, bounds.height);
                    for (Component component : logFrame.getContentPane().getComponents()) {
                        bounds = component.getBounds();
                        component.setBounds(bounds.x, bounds.y, bounds.width + sceneFrame.getBounds().width, bounds.height);
                    }
                }
            }

        };

        rightDock = new DraggableTabbedPane() {

            private static final long serialVersionUID = 1970534169216404328L;

            @Override
            public void remove(int index) {
                super.remove(index);

                if (getTabCount() == 0) {
                    actionFrame.setVisible(false);

                    Rectangle bounds = gameFrame.getBounds();
                    gameFrame.setBounds(bounds.x, bounds.y, bounds.width + actionFrame.getBounds().width, bounds.height);

                    bounds = logFrame.getBounds();
                    logFrame.setBounds(bounds.x, bounds.y, bounds.width + actionFrame.getBounds().width, bounds.height);
                    for (Component component : logFrame.getContentPane().getComponents()) {
                        bounds = component.getBounds();
                        component.setBounds(bounds.x, bounds.y, bounds.width + actionFrame.getBounds().width, bounds.height);
                    }
                }
            }
        };

        setLayout(null);
        this.window = Window.getInstance();

        desktopPane = new JDesktopPane();
        desktopPane.setBounds(0, 0, 1918, 943);
        add(desktopPane);

        gameFrame = new JInternalFrame("Perspective");
        gameFrame.getContentPane().setBackground(Color.BLACK);
        gameFrame.setBounds(316, -2, 1281, 745);
        gameFrame.setFrameIcon(null);
        gameFrame.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);

                if (mouseDown)
                    resizing = true;
            }

        });
        desktopPane.add(gameFrame);

        gameFrame.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent arg0) {
                mouseDown = true;
            }

            @Override
            public void mouseReleased(MouseEvent arg0) {
                mouseDown = false;

                if (resizing) {
                    resizing = false;

                    Dimension dimensions = gameFrame.getContentPane().getSize();
                    gameEditor.queueResize(dimensions.width, dimensions.height);
                }
            }

        });
        gameFrame.getContentPane().setLayout(new BorderLayout());

        canvas = new Canvas();
        canvas.setFocusable(false);
        gameFrame.getContentPane().add(canvas, BorderLayout.CENTER);

        logFrame = new JInternalFrame("Log");
        logFrame.setClosable(true);
        logFrame.setBounds(316, 743, 1281, 198);
        logFrame.setFrameIcon(null);
        desktopPane.add(logFrame);
        Window.getInstance().registerWindow(logFrame, Window.BOTTOM);
        logFrame.getContentPane().setLayout(null);

        JPanel panel = new JPanel();
        panel.setBackground(Color.DARK_GRAY);
        panel.setBounds(0, 135, 1280, 26);
        logFrame.getContentPane().add(panel);
        panel.setLayout(null);

        lblObjectsSelected = new JLabel("0 Objects Selected");
        lblObjectsSelected.setFont(new Font("Tahoma", Font.PLAIN, 11));
        lblObjectsSelected.setBounds(10, 6, 99, 14);
        panel.add(lblObjectsSelected);

        JLabel lblX = new JLabel("X:");
        lblX.setBounds(119, 6, 10, 14);
        panel.add(lblX);

        xTransformField = new JTextField("0.0", 10);
        xTransformField.setBounds(139, 3, 86, 20);
        addTextFieldListener(xTransformField, new Runnable() {

            @Override
            public void run() {
                if (!xTransformField.isEnabled())
                    return;

                for (TransformableObject object : selected)
                    GameEditor.getInstance().getGizmoRenderer().getVector(object).x = parseFloat(xTransformField.getText());
            }

        });
        panel.add(xTransformField);

        JLabel lblY = new JLabel("Y:");
        lblY.setBounds(235, 6, 10, 14);
        panel.add(lblY);

        yTransformField = new JTextField("0.0", 10);
        yTransformField.setBounds(255, 3, 86, 20);
        addTextFieldListener(yTransformField, new Runnable() {

            @Override
            public void run() {
                if (!yTransformField.isEnabled())
                    return;

                for (TransformableObject object : selected)
                    GameEditor.getInstance().getGizmoRenderer().getVector(object).y = parseFloat(yTransformField.getText());
            }

        });
        panel.add(yTransformField);

        JLabel lblZ = new JLabel("Z:");
        lblZ.setBounds(351, 6, 10, 14);
        panel.add(lblZ);

        zTransformField = new JTextField("0.0", 10);
        zTransformField.setBounds(371, 3, 86, 20);
        addTextFieldListener(zTransformField, new Runnable() {

            @Override
            public void run() {
                if (!zTransformField.isEnabled())
                    return;

                for (TransformableObject object : selected)
                    GameEditor.getInstance().getGizmoRenderer().getVector(object).z = parseFloat(zTransformField.getText());
            }

        });
        panel.add(zTransformField);

        JLabel lblSpeed = new JLabel("Speed: ");
        lblSpeed.setBounds(469, 6, 38, 14);
        panel.add(lblSpeed);

        speedField = new JTextField();
        speedField.setText("2");
        addTextFieldListener(speedField, new Runnable() {

            @Override
            public void run() {
                GameClient.getPlayer().getComponent(LivingComponent.class).noclipSpeed = parseFloat(speedField.getText());
            }

        });
        speedField.setColumns(10);
        speedField.setBounds(519, 2, 38, 20);
        panel.add(speedField);

        JButton btnSpeed2 = new JButton("0.4");
        btnSpeed2.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                speedField.setText("0.4");
            }

        });
        btnSpeed2.setBounds(557, 2, 38, 21);
        panel.add(btnSpeed2);

        JButton btnSpeed10 = new JButton("2");
        btnSpeed10.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                speedField.setText("2");
            }

        });
        btnSpeed10.setBounds(595, 2, 38, 21);
        panel.add(btnSpeed10);

        JButton btnSpeed25 = new JButton("5");
        btnSpeed25.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                speedField.setText("5");
            }

        });
        btnSpeed25.setBounds(633, 2, 38, 21);
        panel.add(btnSpeed25);
        lastUpdateTime = System.currentTimeMillis();

        JLabel lblSnap = new JLabel("Snap: ");
        lblSnap.setBounds(683, 6, 38, 14);
        panel.add(lblSnap);

        snapField = new JTextField();
        addTextFieldListener(snapField, new Runnable() {

            @Override
            public void run() {
                if (gameEditor != null)
                    gameEditor.setSnapAmount(parseFloat(snapField.getText()));
            }

        });
        snapField.setText("0.5");
        snapField.setColumns(10);
        snapField.setBounds(733, 2, 38, 20);
        panel.add(snapField);

        JButton btnSnap0 = new JButton("0");
        btnSnap0.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                snapField.setText("0");
            }

        });
        btnSnap0.setBounds(771, 2, 38, 21);
        panel.add(btnSnap0);

        JButton btnSnapHalf = new JButton("0.5");
        btnSnapHalf.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                snapField.setText("0.5");
            }

        });
        btnSnapHalf.setBounds(809, 2, 38, 21);
        panel.add(btnSnapHalf);

        JButton btnSnap1 = new JButton("1");
        btnSnap1.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                snapField.setText("1");
            }

        });
        btnSnap1.setBounds(847, 2, 38, 21);
        panel.add(btnSnap1);

        JButton btnSnap5 = new JButton("5");
        btnSnap5.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                snapField.setText("5");
            }

        });
        btnSnap5.setBounds(885, 2, 38, 21);
        panel.add(btnSnap5);

        lblMbRAM = new JLabel(String.format("%.1f", getUsedMemory()) + "MB (RAM)");
        lblMbRAM.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                float ram = getUsedMemory();
                System.gc();
                Log.info(String.format("%.1f", ram - getUsedMemory()) + "MB of RAM has been freed.");
            }

            @Override
            public void mouseEntered(MouseEvent arg0) {
                updateRAM = true;
            }

            @Override
            public void mouseExited(MouseEvent arg0) {
                updateRAM = false;
            }

        });
        lblMbRAM.setHorizontalTextPosition(SwingConstants.RIGHT);
        lblMbRAM.setHorizontalAlignment(SwingConstants.RIGHT);
        lblMbRAM.setBounds(1188, 4, 86, 16);
        panel.add(lblMbRAM);

        JScrollPane logScrollPane = new JScrollPane();
        logScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        logScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        logScrollPane.setBounds(0, 0, 1272, 135);
        logFrame.getContentPane().add(logScrollPane);

        JEditorPane editorPane = new JEditorPane();
        logScrollPane.setViewportView(editorPane);
        LogRedirector.redirect(editorPane);

        sceneFrame = new JInternalFrame("Scene");
        sceneFrame.setBounds(0, 0, 317, 940);
        desktopPane.add(sceneFrame);
        sceneFrame.getContentPane().setLayout(new BorderLayout());
        sceneFrame.getContentPane().add(leftDock, BorderLayout.CENTER);

        scenePopupMenu = new JPopupMenu();
        scenePopupMenu.add(new JMenuItem(new AbstractAction("Copy") {

            private static final long serialVersionUID = 1494773096597716078L;

            @Override
            public void actionPerformed(ActionEvent event) {
                for (TransformableObject object : selected) {
                    if (object instanceof Entity)
                        gameEditor.addEntity(((Entity) object).copy());
                    else if (object instanceof EditableMesh)
                        gameEditor.addEditableMesh(new EditableMesh((EditableMesh) object));
                }

                if (selectedTerrain != null) {
                    String[] split = JOptionPane.showInputDialog(null, "Grid coordinates: ", "Copy terrain", JOptionPane.QUESTION_MESSAGE).split(Pattern.quote(","));
                    if (split.length < 2) {
                        JOptionPane.showMessageDialog(null, "Invalid coordaintes", "Failed to copy terrain", JOptionPane.ERROR_MESSAGE);

                        return;
                    }

                    gameEditor.addTerrain(selectedTerrain.getHeights(), parseInt(split[0]), parseInt(split[1]));
                }

                scenePopupMenu.setVisible(false);
            }

        }));
        scenePopupMenu.add(new JMenuItem(new AbstractAction("Delete") {

            private static final long serialVersionUID = -251360568773242114L;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                for (int i = 0; i < selected.size(); i++) {
                    TransformableObject object = selected.get(i);
                    if (object instanceof Entity)
                        gameEditor.removeEntity((Entity) object);
                    else if (object instanceof EditableMesh)
                        gameEditor.removeEditableMesh((EditableMesh) object);

                    unselectAll();
                }

                if (selectedTerrain != null) {
                    gameEditor.removeTerrain(selectedTerrain);
                    setSelectedTerrain(null);
                }

                scenePopupMenu.setVisible(false);
            }

        }));
        scenePopupMenu.addSeparator();
        scenePopupMenu.add(new JMenuItem(new AbstractAction("Group") {

            private static final long serialVersionUID = -7509797304178919452L;

            @Override
            public void actionPerformed(ActionEvent event) {
                Entity parent = new Entity();
                for (TransformableObject object : selected)
                    if (object instanceof Entity)
                        ((Entity) object).setParent(parent);
                updateList();

                scenePopupMenu.setVisible(false);
            }

        }));
        scenePopupMenu.addSeparator();
        scenePopupMenu.add(new JMenuItem(new AbstractAction("Convert to Editable Mesh") {

            private static final long serialVersionUID = -1302586824769432105L;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                for (int i = 0; i < selected.size(); i++) {
                    TransformableObject object = selected.get(i);
                    if (object instanceof Entity) {
                        Scheduler.schedule(new ScheduledRunnable() {

                            @Override
                            public void tick(float time, float percentage) {

                            }

                            @Override
                            public void finish() {
                                gameEditor.addEditableMesh(new EditableMesh((Entity) object));
                                gameEditor.removeEntity((Entity) object);
                            }

                        }, 0);
                    }
                    unselectAll();
                }

                scenePopupMenu.setVisible(false);
            }

        }));
        scenePopupMenu.add(new JMenuItem(new AbstractAction("Refresh editable mesh") {

            private static final long serialVersionUID = 1968181078779498279L;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                for (int i = 0; i < selected.size(); i++) {
                    TransformableObject object = selected.get(i);
                    if (object instanceof EditableMesh) {
                        Scheduler.schedule(new ScheduledRunnable() {

                            @Override
                            public void tick(float time, float percentage) {

                            }

                            @Override
                            public void finish() {
                                ((EditableMesh) object).updateMesh(true);
                            }

                        }, 0);
                    }
                    unselectAll();
                }

                scenePopupMenu.setVisible(false);
            }

        }));

        JPanel scenePanel = new JPanel();
        tree = UIKit.createTree(scenePanel);
        tree.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent event) {
                if (SwingUtilities.isRightMouseButton(event)) {
                    int row = tree.getClosestRowForLocation(event.getX(), event.getY());
                    tree.setSelectionRow(-1);
                    tree.setSelectionRow(row);

                    scenePopupMenu.show(event.getComponent(), event.getX(), event.getY());
                } else if (SwingUtilities.isLeftMouseButton(event)) {
                    int row = tree.getClosestRowForLocation(event.getX(), event.getY());
                    tree.setSelectionRow(-1);
                    tree.setSelectionRow(row);
                }
            }

        });
        tree.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (ignoreTreeEvent) {
                    ignoreTreeEvent = false;
                    return;
                }

                CustomMutableTreeNode node = (CustomMutableTreeNode) tree.getLastSelectedPathComponent();
                if (node == null)
                    return;

                Object store = node.getStorage();
                if (store instanceof TransformableObject) {
                    unselectAll();
                    addToSelection((TransformableObject) store);
                } else if (store instanceof ClientTerrain) {
                    setSelectedTerrain((ClientTerrain) store);
                } else if (store instanceof String) {
                    updateList();
                }
            }

        });
        updateList();
        Window.getInstance().registerPanel("Scene", scenePanel, Window.LEFT);

        JPanel assetBrowserPanel = new JPanel();
        JTree assetBrowserFileSystem = UIKit.createTree(assetBrowserPanel);
        assetBrowserPopupMenu = new JPopupMenu();
        assetBrowserPopupMenu.add(new JMenuItem(new AbstractAction("Create model") {

            private static final long serialVersionUID = -2739758793155285084L;

            @Override
            public void actionPerformed(ActionEvent event) {
                File file = (File) ((CustomMutableTreeNode) assetBrowserFileSystem.getLastSelectedPathComponent()).getStorage();
                if (file.isFile() && file.getName().endsWith("obj")) {
                    String model = FileHelper.removeFileExtension(FileHelper.localFileName(file));

                    Entity entity = gameEditor.addEntity(false);
                    entity.setValue("model", model);
                    entity.addComponent(new MeshComponent());

                    unselectAll();
                    addToSelection(entity);
                }
            }

        }));
        assetBrowserPopupMenu.add(new JMenuItem(new AbstractAction("Set as model") {

            private static final long serialVersionUID = -2739758793155285084L;

            @Override
            public void actionPerformed(ActionEvent event) {
                for (TransformableObject object : selected) {
                    if (!(object instanceof Entity))
                        continue;

                    Entity selectedEntity = (Entity) object;
                    MeshComponent mesh = selectedEntity.getComponent(MeshComponent.class);
                    if (mesh != null) {
                        File file = (File) ((CustomMutableTreeNode) assetBrowserFileSystem.getLastSelectedPathComponent()).getStorage();
                        if (file.isFile() && file.getName().endsWith("obj")) {
                            String model = FileHelper.removeFileExtension(FileHelper.localFileName(file));

                            selectedEntity.setValue("model", model);
                            mesh.model = AssetLoader.loadModel(model);

                            refreshComponentValues();
                            updateList();
                        }
                    }
                }
            }

        }));
        assetBrowserPopupMenu.addSeparator();
        assetBrowserPopupMenu.add(new JMenuItem(new AbstractAction("Create terrain from heightmap") {

            private static final long serialVersionUID = -7893022628901153258L;

            @Override
            public void actionPerformed(ActionEvent event) {
                File file = (File) ((CustomMutableTreeNode) assetBrowserFileSystem.getLastSelectedPathComponent()).getStorage();
                if (file.isFile() && file.getParentFile().getName().equals("heightmap")) {
                    String heightmap = file.getName().replace(".png", "");
                    String[] split = JOptionPane.showInputDialog(null, "Grid coordinates: ", "Create terrain", JOptionPane.QUESTION_MESSAGE).split(Pattern.quote(","));
                    if (split.length < 2) {
                        JOptionPane.showMessageDialog(null, "Invalid coordaintes", "Failed to create terrain", JOptionPane.ERROR_MESSAGE);

                        return;
                    }

                    gameEditor.addTerrain(heightmap, parseInt(split[0]), parseInt(split[1]));
                }
            }

        }));
        assetBrowserPopupMenu.add(new JMenuItem(new AbstractAction("Set as heightmap") {

            private static final long serialVersionUID = -5670212139023058346L;

            @Override
            public void actionPerformed(ActionEvent event) {
                if (selectedTerrain != null) {
                    File file = (File) ((CustomMutableTreeNode) assetBrowserFileSystem.getLastSelectedPathComponent()).getStorage();
                    if (file.isFile() && file.getParentFile().getName().equals("heightmap"))
                        selectedTerrain.setHeights(TerrainUtils.loadHeightsFromHeightmap(file.getName().replace(".png", "")));
                }
            }

        }));
        assetBrowserPopupMenu.addSeparator();
        assetBrowserPopupMenu.add(new JMenuItem(new AbstractAction("Show in explorer") {

            private static final long serialVersionUID = 2487913488717280373L;

            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    Runtime.getRuntime().exec("explorer.exe /select," + ((File) ((CustomMutableTreeNode) assetBrowserFileSystem.getLastSelectedPathComponent()).getStorage()).getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }));
        assetBrowserFileSystem.setModel(new FileSystemModel(new File("res")));
        assetBrowserFileSystem.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent event) {
                if (SwingUtilities.isRightMouseButton(event)) {
                    int row = assetBrowserFileSystem.getClosestRowForLocation(event.getX(), event.getY());
                    assetBrowserFileSystem.setSelectionRow(-1);
                    assetBrowserFileSystem.setSelectionRow(row);

                    assetBrowserPopupMenu.show(event.getComponent(), event.getX(), event.getY());
                }
            }

        });
        expand(assetBrowserFileSystem, 0, assetBrowserFileSystem.getRowCount());
        Window.getInstance().registerPanel("Asset Browser", assetBrowserPanel, Window.LEFT);

        actionFrame = new JInternalFrame("Action");
        actionFrame.setBorder(null);
        actionFrame.setBounds(1599, 0, 317, 943);
        desktopPane.add(actionFrame);
        actionFrame.getContentPane().setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        actionFrame.getContentPane().add(rightDock, BorderLayout.CENTER);
        Window.getInstance().registerPanel("Action", tabbedPane, Window.RIGHT);

        JPanel addPanel = new JPanel();
        tabbedPane.addTab("Add", null, addPanel, null);
        addPanel.setLayout(null);

        Pointer<Integer> height = new Pointer<Integer>();

        TextFieldBlueprint createGridX = new TextFieldBlueprint("Grid X: ", "0");
        TextFieldBlueprint createGridZ = new TextFieldBlueprint("Grid Z: ", "0");
        TextFieldBlueprint createVertices = new TextFieldBlueprint("Vertices: ", "256");

        JScrollPane terrainCreatePanel = UIKit.createSubpanel("Terrain", Arrays.asList(createGridX, createGridZ, createVertices, new ButtonBlueprint("Create", new ButtonListener() {

            @Override
            public void onButtonClick(CustomButton field) {
                int vertices = parseInt(createVertices.getText());
                gameEditor.addTerrain(new float[vertices][vertices], parseInt(createGridX.getText()), parseInt(createGridZ.getText()));

                removeSubpanel();
            }

        })), height);
        terrainCreatePanel.setBounds(UIKit.SUBPANEL_X, 119, UIKit.SUBPANEL_WIDTH, height.get());

        Pointer<String> path = new Pointer<String>();
        JScrollPane geometryCreatePanel = UIKit.createSubpanel("Geometry", Arrays.asList(AssetPicker.create("Model: ", null, "obj", new FilePickCallback() {

            @Override
            public void run(File file) {
                path.set(FileHelper.removeFileExtension(FileHelper.localFileName(file)));
            }

        }), new ButtonBlueprint("Create", new ButtonListener() {

            @Override
            public void onButtonClick(CustomButton field) {
                if (path.get() == null)
                    return;

                Entity entity = gameEditor.addEntity(false);
                entity.setValue("model", path.get());
                entity.addComponent(new MeshComponent());

                unselectAll();
                addToSelection(entity);

                removeSubpanel();
            }

        })), height);
        geometryCreatePanel.setBounds(UIKit.SUBPANEL_X, 119, UIKit.SUBPANEL_WIDTH, height.get());

        JScrollPane prefabCreatePanel = UIKit.createSubpanel("Prefab", Arrays.asList(AssetPicker.create("Prefab: ", null, "pfb", new FilePickCallback() {

            @Override
            public void run(File file) {
                path.set(FileHelper.removeFileExtension(FileHelper.localFileName(file)));
            }

        }), new ButtonBlueprint("Load", new ButtonListener() {

            @Override
            public void onButtonClick(CustomButton field) {
                gameEditor.getScene().getEntities().addAll(PrefabReader.read(FileHelper.newGameFile("res", path.get() + ".pfb")));
                updateList();

                removeSubpanel();
            }

        })), height);
        prefabCreatePanel.setBounds(UIKit.SUBPANEL_X, 119, UIKit.SUBPANEL_WIDTH, height.get());

        JScrollPane typesPanel = UIKit.createSubpanel("Types", Arrays.asList(new DualButtonBlueprint("Entity", "Terrain", new ButtonListener() {

            @Override
            public void onButtonClick(CustomButton field) {
                gameEditor.addEntity();
            }

        }, new ButtonListener() {

            @Override
            public void onButtonClick(CustomButton field) {
                addPanel.remove(geometryCreatePanel);
                addPanel.remove(prefabCreatePanel);
                if (terrainCreatePanel.getParent() == null)
                    addPanel.add(terrainCreatePanel);
                else
                    addPanel.remove(terrainCreatePanel);

                addPanel.revalidate();
                addPanel.repaint();
            }

        }), new DualButtonBlueprint("Geometry", "Prefab", new ButtonListener() {

            @Override
            public void onButtonClick(CustomButton field) {
                addPanel.remove(terrainCreatePanel);
                addPanel.remove(prefabCreatePanel);
                if (geometryCreatePanel.getParent() == null)
                    addPanel.add(geometryCreatePanel);
                else
                    addPanel.remove(geometryCreatePanel);

                addPanel.revalidate();
                addPanel.repaint();
            }

        }, new ButtonListener() {

            @Override
            public void onButtonClick(CustomButton field) {
                addPanel.remove(terrainCreatePanel);
                addPanel.remove(geometryCreatePanel);
                if (geometryCreatePanel.getParent() == null)
                    addPanel.add(prefabCreatePanel);
                else
                    addPanel.remove(prefabCreatePanel);

                addPanel.revalidate();
                addPanel.repaint();
            }

        })), height);
        typesPanel.setBounds(UIKit.SUBPANEL_X, 26, UIKit.SUBPANEL_WIDTH, height.get());
        addPanel.add(typesPanel);

        JPanel entityPanel = new JPanel();
        tabbedPane.addTab("Entity", null, entityPanel, null);
        entityPanel.setLayout(null);

        List<UIBlueprint> blueprints = new ArrayList<UIBlueprint>();
        for (int i = 0; i < 4; i += 2) {
            Class<?> one = COMPONENTS[i];
            Class<?> two = COMPONENTS[i + 1];

            blueprints.add(new DualButtonBlueprint(one.getSimpleName().replace("Component", ""), two.getSimpleName().replace("Component", ""), new ButtonListener() {

                @Override
                public void onButtonClick(CustomButton field) {
                    List<Entity> entities = new ArrayList<Entity>();
                    for (TransformableObject object : selected)
                        if (object instanceof Entity)
                            entities.add((Entity) object);

                    Undo.addComponentToEntities(entities, one);
                    reloadEntityComponents(selected);
                }

            }, new ButtonListener() {

                @Override
                public void onButtonClick(CustomButton field) {
                    List<Entity> entities = new ArrayList<Entity>();
                    for (TransformableObject object : selected)
                        if (object instanceof Entity)
                            entities.add((Entity) object);

                    Undo.addComponentToEntities(entities, two);
                    reloadEntityComponents(selected);
                }

            }));
        }

        if (COMPONENTS.length > 4) {
            JPopupMenu menu = new JPopupMenu() {

                private static final long serialVersionUID = -328624960413116675L;

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(UIKit.SUBPANEL_WIDTH - 35, super.getPreferredSize().height);
                }

            };

            for (int i = 4; i < COMPONENTS.length; i++) {
                Class<?> component = COMPONENTS[i];

                menu.add(new JMenuItem(new AbstractAction(component.getSimpleName().replace("Component", "")) {

                    private static final long serialVersionUID = 3503145121965759747L;

                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        List<Entity> entities = new ArrayList<Entity>();
                        for (TransformableObject object : selected)
                            if (object instanceof Entity)
                                entities.add((Entity) object);

                        Undo.addComponentToEntities(entities, component);
                        menu.setVisible(false);
                    }

                }));
            }

            blueprints.add(new ButtonBlueprint("More", new ButtonListener() {

                @Override
                public void onButtonClick(CustomButton field) {
                    menu.show(field, 0, 23);
                }

            }));
        }

        JScrollPane addComponentsPanel = UIKit.createSubpanel("Add Components", blueprints, height);
        addComponentsPanel.setBounds(UIKit.SUBPANEL_X, 26, UIKit.SUBPANEL_WIDTH, height.get());
        entityPanel.add(addComponentsPanel);

        JLabel lblName = new JLabel("Name: ");
        lblName.setBounds(UIKit.SUBPANEL_X, 152, UIKit.SUBPANEL_WIDTH, 16);
        entityPanel.add(lblName);

        CustomTextField entityName = new CustomTextField(new TextFieldListener() {

            @Override
            public void onTextFieldEdit(CustomTextField field) {
                for (TransformableObject object : selected)
                    if (object instanceof Entity)
                        ((Entity) object).setValue("name", field.getText());
                updateList();
            }

            @Override
            public void onSelectionChange(TransformableObject change, boolean added) {
                reloadEntityComponents(selected);
                if (!(change instanceof Entity))
                    return;
                Entity current = (Entity) change;
                CustomTextField field = (CustomTextField) component;

                field.setEnabled(false);
                if (added) {

                    field.setText(getEntityName(current));
                    field.setEnabled(true);
                } else {
                    components.removeAll();
                    components.revalidate();
                    components.repaint();

                    field.setText("");
                }
            }

        });
        entityName.getListener().setComponent(entityName);
        lblName.setLabelFor(entityName);
        entityName.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {
                if (entityName.isEnabled())
                    ((TextFieldListener) entityName.getListener()).onTextFieldEdit(entityName);
            }

            public void removeUpdate(DocumentEvent e) {
                if (entityName.isEnabled())
                    ((TextFieldListener) entityName.getListener()).onTextFieldEdit(entityName);
            }

            public void insertUpdate(DocumentEvent e) {
                if (entityName.isEnabled())
                    ((TextFieldListener) entityName.getListener()).onTextFieldEdit(entityName);
            }

        });
        entityName.setBounds(118, 148, 148, 23);
        entityName.setColumns(10);
        entityPanel.add(entityName);

        JLabel lblLayer = new JLabel("Layer: ");
        lblLayer.setBounds(UIKit.SUBPANEL_X, 192, UIKit.SUBPANEL_WIDTH, 16);
        entityPanel.add(lblLayer);

        CustomDropdown entityLayer = new CustomDropdown(Engine.getLayerNames(), new DropdownListener() {

            @Override
            public void onDropdownSelect(CustomDropdown field) {
                for (TransformableObject object : selected)
                    if (object instanceof Entity)
                        ((Entity) object).setLayer(Engine.getLayerByName((String) field.getSelectedItem()));
            }

            @Override
            public void onSelectionChange(TransformableObject change, boolean added) {
                if (!(change instanceof Entity))
                    return;
                Entity current = (Entity) change;
                CustomDropdown field = (CustomDropdown) component;
                field.setEnabled(false);

                field.removeAllItems();
                for (String item : Engine.getLayerNames())
                    field.addItem(item);

                if (added) {
                    field.setSelectedItem(current.getLayer().getName());
                    field.setEnabled(true);
                } else {
                    components.removeAll();
                    components.revalidate();
                    components.repaint();

                    field.setSelectedIndex(-1);
                }
            }

        });
        entityLayer.getListener().setComponent(entityLayer);
        lblLayer.setLabelFor(entityLayer);
        entityLayer.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (entityLayer.isEnabled())
                    ((DropdownListener) entityLayer.getListener()).onDropdownSelect(entityLayer);
            }

        });
        entityLayer.setBounds(118, 188, 148, 23);
        entityPanel.add(entityLayer);

        JPanel componentsPanel = new JPanel();
        componentsPanel.setBorder(new TitledBorder(null, "Components", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        componentsPanel.setBounds(UIKit.SUBPANEL_X, 223, UIKit.SUBPANEL_WIDTH, 669);
        entityPanel.add(componentsPanel);
        componentsPanel.setLayout(new BorderLayout(0, 0));

        JScrollPane componentsScrollPane = new JScrollPane();
        components = new JPanel();
        components.setLayout(null);
        componentsScrollPane.setViewportView(components);
        componentsPanel.add(componentsScrollPane, BorderLayout.CENTER);

        JPanel terrainPanel = new JPanel();
        tabbedPane.addTab("Terrain", null, terrainPanel, null);
        terrainPanel.setLayout(null);

        JScrollPane terrainPaintPanel = UIKit.createSubpanel("Paint", Arrays.asList(new DropdownBlueprint("Texture: ", Arrays.asList(), new DropdownListener() {

            @Override
            public void onDropdownSelect(CustomDropdown field) {

            }

            @Override
            public void onTerrainSelect(Terrain previous, Terrain current) {
                CustomDropdown dropdown = (CustomDropdown) component;

                dropdown.setEnabled(false);
                dropdown.removeAllItems();
                if (current != null) {
                    ClientTerrain terrain = (ClientTerrain) current;

                    dropdown.addItem(terrain.getTextures().getBackgroundName());
                    dropdown.addItem(terrain.getTextures().getRedName());
                    dropdown.addItem(terrain.getTextures().getGreenName());
                    dropdown.addItem(terrain.getTextures().getBlueName());
                    dropdown.setEnabled(true);
                }
            }

        })), height);
        terrainPaintPanel.setBounds(UIKit.SUBPANEL_X, 432, UIKit.SUBPANEL_WIDTH, height.get());

        List<String> brushes = new ArrayList<String>();
        for (TerrainBrush brush : BRUSHES)
            brushes.add(brush.getName());

        JScrollPane terrainTerraformPanel = UIKit.createSubpanel("Terraform", Arrays.asList(new DropdownBlueprint("Brush: ", brushes, new DropdownListener() {

            @Override
            public void onDropdownSelect(CustomDropdown field) {
                selectedBrush = BRUSHES[field.getSelectedIndex()];
            }

        })), height);
        terrainTerraformPanel.setBounds(UIKit.SUBPANEL_X, 432, UIKit.SUBPANEL_WIDTH, height.get());

        JScrollPane terrainSettingsPanel = UIKit.createSubpanel("Terrain Settings", Arrays.asList(new TextFieldBlueprint("Size: ", Terrain.DEFAULT_SIZE + "", new TextFieldListener() {

            @Override
            public void onTextFieldEdit(CustomTextField field) {
                if (selectedTerrain != null)
                    selectedTerrain.setSize(parseFloat(field.getText()));
            }

            @Override
            public void onTerrainSelect(Terrain previous, Terrain current) {
                CustomTextField field = (CustomTextField) component;

                field.setEnabled(false);
                if (current != null) {
                    field.setText(current.getSize() + "");
                    field.setEnabled(true);
                } else {
                    field.setText(Terrain.DEFAULT_SIZE + "");
                }
            }

        }), new TextFieldBlueprint("Grid X: ", "0", new TextFieldListener() {

            @Override
            public void onTextFieldEdit(CustomTextField field) {
                if (selectedTerrain != null)
                    selectedTerrain.setGridX(parseInt(field.getText()));
            }

            @Override
            public void onTerrainSelect(Terrain previous, Terrain current) {
                CustomTextField field = (CustomTextField) component;

                field.setEnabled(false);
                if (current != null) {
                    field.setText(current.getGX() + "");
                    field.setEnabled(true);
                } else {
                    field.setText("0");
                }
            }

        }), new TextFieldBlueprint("Grid Z: ", "0", new TextFieldListener() {

            @Override
            public void onTextFieldEdit(CustomTextField field) {
                if (selectedTerrain != null)
                    selectedTerrain.setGridZ(parseInt(field.getText()));
            }

            @Override
            public void onTerrainSelect(Terrain previous, Terrain current) {
                CustomTextField field = (CustomTextField) component;

                field.setEnabled(false);
                if (current != null) {
                    field.setText(current.getGZ() + "");
                    field.setEnabled(true);
                } else {
                    field.setText("0");
                }
            }

        }), new DropdownBlueprint("Background Texture: ", AssetManager.getTerrainTextures(), new DropdownListener() {

            @Override
            public void onDropdownSelect(CustomDropdown field) {
                if (field.isEnabled() && selectedTerrain != null)
                    selectedTerrain.getTextures().setBackgroundTexture(Requester.requestTexture(TextureType.TERRAIN, (String) field.getSelectedItem()));
            }

            @Override
            public void onTerrainSelect(Terrain previous, Terrain current) {
                CustomDropdown dropdown = (CustomDropdown) component;

                dropdown.setEnabled(false);
                if (current != null) {
                    dropdown.setSelectedIndex(AssetManager.getIndex(dropdown, ((ClientTerrain) current).getTextures().getBackgroundName()));
                    dropdown.setEnabled(true);
                } else {
                    dropdown.setSelectedIndex(0);
                }
            }

        }), new DropdownBlueprint("Red Texture: ", AssetManager.getTerrainTextures(), new DropdownListener() {

            @Override
            public void onDropdownSelect(CustomDropdown field) {
                if (field.isEnabled() && selectedTerrain != null)
                    selectedTerrain.getTextures().setRedTexture(Requester.requestTexture(TextureType.TERRAIN, (String) field.getSelectedItem()));
            }

            @Override
            public void onTerrainSelect(Terrain previous, Terrain current) {
                CustomDropdown dropdown = (CustomDropdown) component;

                dropdown.setEnabled(false);
                if (current != null) {
                    dropdown.setSelectedIndex(AssetManager.getIndex(dropdown, ((ClientTerrain) current).getTextures().getRedName()));
                    dropdown.setEnabled(true);
                } else {
                    dropdown.setSelectedIndex(0);
                }
            }

        }), new DropdownBlueprint("Green Texture: ", AssetManager.getTerrainTextures(), new DropdownListener() {

            @Override
            public void onDropdownSelect(CustomDropdown field) {
                if (field.isEnabled() && selectedTerrain != null)
                    selectedTerrain.getTextures().setGreenTexture(Requester.requestTexture(TextureType.TERRAIN, (String) field.getSelectedItem()));
            }

            @Override
            public void onTerrainSelect(Terrain previous, Terrain current) {
                CustomDropdown dropdown = (CustomDropdown) component;

                dropdown.setEnabled(false);
                if (current != null) {
                    dropdown.setSelectedIndex(AssetManager.getIndex(dropdown, ((ClientTerrain) current).getTextures().getGreenName()));
                    dropdown.setEnabled(true);
                } else {
                    dropdown.setSelectedIndex(0);
                }
            }

        }), new DropdownBlueprint("Blue Texture: ", AssetManager.getTerrainTextures(), new DropdownListener() {

            @Override
            public void onDropdownSelect(CustomDropdown field) {
                if (field.isEnabled() && selectedTerrain != null)
                    selectedTerrain.getTextures().setBlueTexture(Requester.requestTexture(TextureType.TERRAIN, (String) field.getSelectedItem()));
            }

            @Override
            public void onTerrainSelect(Terrain previous, Terrain current) {
                CustomDropdown dropdown = (CustomDropdown) component;

                dropdown.setEnabled(false);
                if (current != null) {
                    dropdown.setSelectedIndex(AssetManager.getIndex(dropdown, ((ClientTerrain) current).getTextures().getBlueName()));
                    dropdown.setEnabled(true);
                } else {
                    dropdown.setSelectedIndex(0);
                }
            }

        }), new DualButtonBlueprint("Terraform", "Paint", new ButtonListener() {

            @Override
            public void onButtonClick(CustomButton field) {
                terraform = !terraform;

                if (terraform) {
                    terrainPanel.remove(terrainPaintPanel);
                    terrainPanel.add(terrainTerraformPanel);
                } else {
                    terrainPanel.remove(terrainTerraformPanel);
                }

                terrainPanel.revalidate();
                terrainPanel.repaint();
            }

            @Override
            public void onTerrainSelect(Terrain previous, Terrain current) {
                terraform = false;

                terrainPanel.remove(terrainTerraformPanel);
                terrainPanel.revalidate();
                terrainPanel.repaint();
            }

        }, new ButtonListener() {

            @Override
            public void onButtonClick(CustomButton field) {
                paint = !paint;

                if (paint) {
                    terrainPanel.remove(terrainTerraformPanel);
                    terrainPanel.add(terrainPaintPanel);
                } else {
                    terrainPanel.remove(terrainPaintPanel);
                }

                terrainPanel.revalidate();
                terrainPanel.repaint();
            }

            @Override
            public void onTerrainSelect(Terrain previous, Terrain current) {
                paint = false;

                terrainPanel.remove(terrainPaintPanel);
                terrainPanel.revalidate();
                terrainPanel.repaint();
            }

        })), height);
        terrainSettingsPanel.setBounds(UIKit.SUBPANEL_X, 26, UIKit.SUBPANEL_WIDTH, height.get());
        terrainPanel.add(terrainSettingsPanel);

        List<String> shapes = new ArrayList<String>();
        for (Shape shape : SHAPES)
            shapes.add(shape.getName());

        JScrollPane terrainBrushPanel = UIKit.createSubpanel("Brush", Arrays.asList(new TextFieldBlueprint("Radius: ", "5", new TextFieldListener() {

            @Override
            public void onTextFieldEdit(CustomTextField field) {
                radius = parseFloat(field.getText());
            }

            @Override
            public void onTerrainSelect(Terrain previous, Terrain current) {
                CustomTextField field = (CustomTextField) component;

                field.setEnabled(false);
                if (current != null) {
                    field.setText(radius + "");
                    field.setEnabled(true);
                }
            }

        }), new TextFieldBlueprint("Strength: ", "1", new TextFieldListener() {

            @Override
            public void onTextFieldEdit(CustomTextField field) {
                strength = parseFloat(field.getText());
            }

            @Override
            public void onTerrainSelect(Terrain previous, Terrain current) {
                CustomTextField field = (CustomTextField) component;

                field.setEnabled(false);
                if (current != null) {
                    field.setText(strength + "");
                    field.setEnabled(true);
                }
            }

        }), new DropdownBlueprint("Shape: ", shapes, new DropdownListener() {

            @Override
            public void onDropdownSelect(CustomDropdown field) {
                selectedShape = SHAPES[field.getSelectedIndex()];
            }

        })), height);
        terrainBrushPanel.setBounds(UIKit.SUBPANEL_X, 310, UIKit.SUBPANEL_WIDTH, height.get());
        terrainPanel.add(terrainBrushPanel);

        JPanel environmentPanel = new JPanel();
        tabbedPane.addTab("Environment", null, environmentPanel, null);
        environmentPanel.setLayout(new BorderLayout());

        JScrollPane environmentScrollPane = new JScrollPane();
        environmentPanel.add(environmentScrollPane, BorderLayout.CENTER);

        environmentPropertiesTable = new JTable() {

            private static final long serialVersionUID = 3136676021572178400L;

            public TableCellRenderer getCellRenderer(int row, int column) {
                Object object = environmentPropertiesTable.getModel().getValueAt(row, column);
                if (object instanceof Boolean)
                    return super.getDefaultRenderer(Boolean.class);
                if (object instanceof Float || object instanceof Double)
                    return super.getDefaultRenderer(Float.class);
                if (object instanceof Number)
                    return super.getDefaultRenderer(Number.class);

                return super.getCellRenderer(row, column);
            }

            public TableCellEditor getCellEditor(int row, int column) {
                Object object = environmentPropertiesTable.getModel().getValueAt(row, column);
                if (object instanceof Boolean)
                    return super.getDefaultEditor(Boolean.class);
                if (object instanceof Number)
                    return super.getDefaultEditor(Number.class);

                return super.getCellEditor(row, column);
            }

        };

        environmentPropertiesTable.setModel(new DefaultTableModel(new Object[][] { { "Wireframe", ClientGameVariables.r_wireframe.getValueAsBool() }, { "Procedural Skybox", Settings.proceduralSky }, { "Perform Lighting", ClientGameVariables.r_performLighting.getValueAsBool() }, { "Perform SSAO", ClientGameVariables.r_performSSAO.getValueAsBool() }, { "Show Lightmaps", ClientGameVariables.r_showLightmaps.getValueAsBool() }, { "Fog Density", Settings.density }, { "Fog Gradient", Settings.gradient }, { "Exposure Speed", Settings.exposureSpeed }, { "SSAO Bias", Settings.ssaoBias }, { "SSAO Radius", Settings.ssaoRadius } }, new String[] { "Property", "Value" }) {

            private static final long serialVersionUID = 3225320749175276075L;

            Class<?>[] columnTypes = new Class[] { String.class, Object.class };

            public Class<?> getColumnClass(int columnIndex) {
                return columnTypes[columnIndex];
            }

            boolean[] columnEditables = new boolean[] { false, true };

            public boolean isCellEditable(int row, int column) {
                return columnEditables[column];
            }

        });
        environmentPropertiesTable.getModel().addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent event) {
                int row = event.getFirstRow();
                int col = event.getColumn();

                RenderSettings.set(environmentPropertiesTable.getModel().getValueAt(row, col - 1).toString(), environmentPropertiesTable.getModel().getValueAt(row, col).toString());
            }

        });
        environmentPropertiesTable.setFillsViewportHeight(true);
        environmentScrollPane.setViewportView(environmentPropertiesTable);

        JPanel layersPanel = new JPanel();
        layersPanel.setLayout(null);

        JButton createLayerButton = new JButton("Create");
        createLayerButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog(null, "Name: ", "Anchor Engine Editor", JOptionPane.QUESTION_MESSAGE);
                if (name.equals(""))
                    return;

                Color colour = JColorChooser.showDialog(null, "Anchor Engine Editor", new Color(133, 213, 214));
                if (colour == null)
                    return;

                Engine.getLayers().add(new Layer(name, colour));
                layersPanel.repaint();
            }

        });
        createLayerButton.setBounds(0, 0, 317, 23);
        layersPanel.add(createLayerButton);

        JPopupMenu layersPopupMenu = new JPopupMenu();
        layersPopupMenu.add(new JMenuItem(new AbstractAction("Rename") {

            private static final long serialVersionUID = -2341604367379331217L;

            @Override
            public void actionPerformed(ActionEvent event) {
                String name = JOptionPane.showInputDialog(null, "Rename " + layer.getName(), "Anchor Engine Editor", JOptionPane.QUESTION_MESSAGE);
                if (name.equals(""))
                    return;

                layer.setName(name);
                layersPanel.repaint();
            }

        }));
        layersPopupMenu.add(new JMenuItem(new AbstractAction("Change colour") {

            private static final long serialVersionUID = -1342640139711215514L;

            @Override
            public void actionPerformed(ActionEvent event) {
                Color colour = JColorChooser.showDialog(null, "Anchor Engine Editor", new Color(133, 213, 214));
                if (colour == null)
                    return;

                layer.setColour(colour);
                layersPanel.repaint();
            }

        }));
        layersPopupMenu.add(new JMenuItem(new AbstractAction("Delete") {

            private static final long serialVersionUID = 9216970922260879244L;

            @Override
            public void actionPerformed(ActionEvent event) {
                for (Entity entity : Engine.getEntities())
                    if (entity.getLayer() == layer)
                        entity.setLayer(Engine.getDefaultLayer());

                Engine.getLayers().remove(layer);
                layersPanel.repaint();
            }

        }));

        Font title = new Font("Segoe UI", Font.PLAIN, 10);
        Font text = new Font("Segoe UI", Font.PLAIN, 15);
        JPanel listPanel = new JPanel() {

            private static final long serialVersionUID = -3841338472978260323L;

            @Override
            public void paint(Graphics g) {
                super.paint(g);

                g.setFont(title);
                g.drawString("Visi...", 2, 8);
                g.drawString("Pick...", 28, 8);
                g.drawString("Name", 58, 8);

                int y = 12;
                g.setFont(text);

                for (Layer layer : Engine.getLayers()) {
                    g.fillRect(0, y - 1, 317, 1);
                    g.setColor(layer.getColour());
                    g.fillRect(55, y, leftDock.getBounds().width, 25);

                    g.setColor(Color.BLACK);
                    g.drawString(layer.getName(), 60, y + 18);

                    g.drawRect(1, y + 1, 23, 23);
                    g.drawRect(29, y + 1, 23, 23);

                    g.setColor(Color.GRAY);

                    if (layer.isVisible())
                        g.drawString("X", 9, y + 18);
                    if (layer.isPickable())
                        g.drawString("X", 37, y + 18);

                    g.setColor(Color.WHITE);
                    g.fillRect(0, y + 25, 317, 1);

                    y += 26;
                }
            }

        };

        listPanel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent event) {
                int mouseX = event.getX();
                int mouseY = event.getY();
                int y = 12;

                for (Layer layer : Engine.getLayers()) {
                    if (event.getButton() == 1) {
                        if (mouseX >= 1 && mouseX <= 23 && mouseY >= y + 1 && mouseY <= y + 24)
                            layer.setVisible(!layer.isVisible());

                        if (mouseX >= 29 && mouseX <= 51 && mouseY >= y + 1 && mouseY <= y + 24)
                            layer.setPickable(!layer.isPickable());
                    } else if (event.getButton() == 3) {
                        if (mouseX >= 51 && mouseX <= 317 && mouseY >= y && mouseY <= y + 26) {
                            if (layer == Engine.getDefaultLayer())
                                return;

                            LevelEditor.this.layer = layer;
                            layersPopupMenu.show(event.getComponent(), mouseX, mouseY);
                        }
                    }

                    y += 26;
                }

                layersPanel.repaint();
            }

        });
        listPanel.setBounds(0, 25, 317, 500);
        layersPanel.add(listPanel);

        tabbedPane.addTab("Layers", null, layersPanel, null);

        JPanel modelPanel = new JPanel();
        Window.getInstance().registerPanel("Material Settings", modelPanel);
        modelPanel.setLayout(null);

        JScrollPane modelSettingsPanel = UIKit.createSubpanel("Material Settings", Arrays.asList(AssetPicker.create("Albedo: ", null, "png", new FilePickCallback() {

            @Override
            public void onSelectionChange(TransformableObject change, boolean added) {
                if (!(change instanceof Entity))
                    return;
                Entity current = (Entity) change;

                if (added) {
                    MeshComponent mesh = current.getComponent(MeshComponent.class);
                    if (mesh != null && mesh.model != null && mesh.model.getMesh() != null) {
                        String albedo = mesh.material != null ? mesh.material.getAlbedoName() : null;

                        ((CustomButton) component).setText(albedo != null ? albedo : "Choose File");
                        component.setEnabled(true);
                    }
                } else {
                    ((CustomButton) component).setText("Choose File");
                    component.setEnabled(false);
                }
            }

            @Override
            public void run(File file) {
                for (TransformableObject object : selected) {
                    if (!(object instanceof Entity))
                        continue;
                    Entity selectedEntity = (Entity) object;

                    selectedEntity.getComponent(MeshComponent.class).material.setAlbedo(Requester.requestTexture(FileHelper.removeFileExtension(FileHelper.localFileName(file))));
                    selectedEntity.getComponent(MeshComponent.class).refreshShader();
                }
            }

        }), AssetPicker.create("Normal Map: ", null, "png", new FilePickCallback() {

            @Override
            public void onSelectionChange(TransformableObject change, boolean added) {
                if (!(change instanceof Entity))
                    return;
                Entity current = (Entity) change;

                if (added) {
                    MeshComponent mesh = current.getComponent(MeshComponent.class);
                    if (mesh != null && mesh.model != null && mesh.model.getMesh() != null) {
                        String normalMap = mesh.material != null ? mesh.material.getNormalMapName() : null;

                        ((CustomButton) component).setText(normalMap != null ? normalMap : "Choose File");
                        component.setEnabled(true);
                    }
                } else {
                    ((CustomButton) component).setText("Choose File");
                    component.setEnabled(false);
                }
            }

            @Override
            public void run(File file) {
                for (TransformableObject object : selected) {
                    if (!(object instanceof Entity))
                        continue;
                    Entity selectedEntity = (Entity) object;

                    selectedEntity.getComponent(MeshComponent.class).material.setNormalMap(Requester.requestTexture(FileHelper.removeFileExtension(FileHelper.localFileName(file))));
                    selectedEntity.getComponent(MeshComponent.class).refreshShader();
                }
            }

        }), AssetPicker.create("Specular Map: ", null, "png", new FilePickCallback() {

            @Override
            public void onSelectionChange(TransformableObject change, boolean added) {
                if (!(change instanceof Entity))
                    return;
                Entity current = (Entity) change;

                if (added) {
                    MeshComponent mesh = current.getComponent(MeshComponent.class);
                    if (mesh != null && mesh.model != null && mesh.model.getMesh() != null) {
                        String specularMap = mesh.material != null ? mesh.material.getSpecularMapName() : null;

                        ((CustomButton) component).setText(specularMap != null ? specularMap : "Choose File");
                        component.setEnabled(true);
                    }
                } else {
                    ((CustomButton) component).setText("Choose File");
                    component.setEnabled(false);
                }
            }

            @Override
            public void run(File file) {
                for (TransformableObject object : selected) {
                    if (!(object instanceof Entity))
                        continue;
                    Entity selectedEntity = (Entity) object;

                    selectedEntity.getComponent(MeshComponent.class).material.setSpecularMap(Requester.requestTexture(FileHelper.removeFileExtension(FileHelper.localFileName(file))));
                    selectedEntity.getComponent(MeshComponent.class).refreshShader();
                }
            }

        }), AssetPicker.create("Metallic: ", null, "png", new FilePickCallback() {

            @Override
            public void onSelectionChange(TransformableObject change, boolean added) {
                if (!(change instanceof Entity))
                    return;
                Entity current = (Entity) change;

                if (added) {
                    MeshComponent mesh = current.getComponent(MeshComponent.class);
                    if (mesh != null && mesh.model != null && mesh.model.getMesh() != null) {
                        String metallicMap = mesh.material != null ? mesh.material.getMetallicMapName() : null;

                        ((CustomButton) component).setText(metallicMap != null ? metallicMap : "Choose File");
                        component.setEnabled(true);
                    }
                } else {
                    ((CustomButton) component).setText("Choose File");
                    component.setEnabled(false);
                }
            }

            @Override
            public void run(File file) {
                for (TransformableObject object : selected) {
                    if (!(object instanceof Entity))
                        continue;
                    Entity selectedEntity = (Entity) object;

                    selectedEntity.getComponent(MeshComponent.class).material.setMetallicMap(Requester.requestTexture(FileHelper.removeFileExtension(FileHelper.localFileName(file))));
                    selectedEntity.getComponent(MeshComponent.class).refreshShader();
                }
            }

        }), AssetPicker.create("Roughness Map: ", null, "png", new FilePickCallback() {

            @Override
            public void onSelectionChange(TransformableObject change, boolean added) {
                if (!(change instanceof Entity))
                    return;
                Entity current = (Entity) change;

                if (added) {
                    MeshComponent mesh = current.getComponent(MeshComponent.class);
                    if (mesh != null && mesh.model != null && mesh.model.getMesh() != null) {
                        String roughnessMap = mesh.material != null ? mesh.material.getRoughnessMapName() : null;

                        ((CustomButton) component).setText(roughnessMap != null ? roughnessMap : "Choose File");
                        component.setEnabled(true);
                    }
                } else {
                    ((CustomButton) component).setText("Choose File");
                    component.setEnabled(false);
                }
            }

            @Override
            public void run(File file) {
                for (TransformableObject object : selected) {
                    if (!(object instanceof Entity))
                        continue;
                    Entity selectedEntity = (Entity) object;

                    selectedEntity.getComponent(MeshComponent.class).material.setRoughnessMap(Requester.requestTexture(FileHelper.removeFileExtension(FileHelper.localFileName(file))));
                    selectedEntity.getComponent(MeshComponent.class).refreshShader();
                }
            }

        }), new TextFieldBlueprint("Rows: ", "1", new TextFieldListener() {

            @Override
            public void onTextFieldEdit(CustomTextField field) {
                for (TransformableObject object : selected)
                    if (object instanceof Entity)
                        ((Entity) object).getComponent(MeshComponent.class).material.setNumberOfRows(parseInt(field.getText(), 1));
            }

            @Override
            public void onSelectionChange(TransformableObject change, boolean added) {
                if (!(change instanceof Entity))
                    return;
                Entity current = (Entity) change;
                CustomTextField field = (CustomTextField) component;

                field.setEnabled(false);
                if (added) {
                    MeshComponent mesh = current.getComponent(MeshComponent.class);
                    if (mesh != null && mesh.model != null && mesh.model.getMesh() != null) {
                        field.setText(mesh.material.getNumberOfRows() + "");
                        field.setEnabled(true);
                    }
                } else {
                    field.setText("1");
                }
            }

        }), new CheckboxBlueprint("Backface culling: ", false, new CheckboxListener() {

            @Override
            public void onSelectionChange(TransformableObject change, boolean added) {
                if (!(change instanceof Entity))
                    return;
                Entity current = (Entity) change;
                CustomCheckbox checkbox = (CustomCheckbox) component;

                checkbox.setEnabled(false);
                if (added) {
                    MeshComponent mesh = current.getComponent(MeshComponent.class);
                    if (mesh != null && mesh.model != null && mesh.model.getMesh() != null) {
                        checkbox.setSelected(mesh.material.isCullingEnabled());
                        checkbox.setEnabled(true);
                    }
                } else {
                    checkbox.setSelected(false);
                }
            }

            @Override
            public void onCheckboxEdit(CustomCheckbox checkbox) {
                for (TransformableObject object : selected)
                    if (object instanceof Entity)
                        ((Entity) object).getComponent(MeshComponent.class).material.setCullingEnabled(checkbox.isSelected());
            }

        }), new CheckboxBlueprint("Blending: ", false, new CheckboxListener() {

            @Override
            public void onSelectionChange(TransformableObject change, boolean added) {
                if (!(change instanceof Entity))
                    return;
                Entity current = (Entity) change;
                CustomCheckbox checkbox = (CustomCheckbox) component;

                checkbox.setEnabled(false);
                if (added) {
                    MeshComponent mesh = current.getComponent(MeshComponent.class);
                    if (mesh != null && mesh.model != null && mesh.model.getMesh() != null) {
                        checkbox.setSelected(mesh.material.isBlendingEnabled());
                        checkbox.setEnabled(true);
                    }
                } else {
                    checkbox.setSelected(false);
                }
            }

            @Override
            public void onCheckboxEdit(CustomCheckbox checkbox) {
                for (TransformableObject object : selected)
                    if (object instanceof Entity)
                        ((Entity) object).getComponent(MeshComponent.class).material.setBlendingEnabled(checkbox.isSelected());
            }

        })), height);
        modelSettingsPanel.setBounds(UIKit.SUBPANEL_X, 26, UIKit.SUBPANEL_WIDTH, height.get());
        modelPanel.add(modelSettingsPanel);

        actionFrame.setVisible(true);
        sceneFrame.setVisible(true);
        logFrame.setVisible(true);
        gameFrame.setVisible(true);

        hideTitle(sceneFrame);
        hideTitle(actionFrame);

        unselectAll();
        setSelectedTerrain(null);
    }

    private static void selectionChange(TransformableObject change, boolean added) {
        UIListener.selectionChange(change, added);
    }

    private static void terrainSelected(ClientTerrain selected, ClientTerrain terrain) {
        if (selected != null)
            selected.getColour().w = 0;

        UIListener.terrainSelected(selected, terrain);
    }

    public void addAllToSelection(List<TransformableObject> objects) {
        for (TransformableObject object : objects)
            addToSelection(object);
    }

    public void addToSelection(TransformableObject object) {
        if (selected.contains(object))
            return;

        if (object instanceof Entity) {
            Entity entity = (Entity) object;
            if (entity != null) {
                if (entity.isHidden())
                    entity = null;
                else
                    tabbedPane.setSelectedIndex(1);
            }
        }

        selected.add(object);
        selectionChange(object, true);

        terrainSelected(selectedTerrain, null);
        selectedTerrain = null;

        tabbedPane.setSelectedIndex(1);
        ignoreTreeEvent = true;
//        if (tree != null && treePosition != null) {
//            int[] rows = tree.getSelectionRows();
//            int[] selected = new int[rows.length + 1];
//            System.arraycopy(rows, 0, selected, 0, rows.length);
//            selected[rows.length] = treePosition.get(object);
//        }
        refreshTransformationXYZ();

        if (object instanceof Entity) {
            Entity entity = (Entity) object;
            MeshComponent render = entity.getComponent(MeshComponent.class);
            if (render != null)
                render.colour.set(1, 0, 0, 0.65f);
        } else if (object instanceof EditableMesh) {
            ((EditableMesh) object).colour.set(1, 0, 0, 0.65f);
        }

        lblObjectsSelected.setText(selected.size() + " Object" + (selected.size() == 1 ? "" : "s") + " Selected");
        Log.info("Entity selected at " + object.getPosition().x + ", " + object.getPosition().y + ", " + object.getPosition().z);
    }

    public void unselectAll() {
        removeAllFromSelection(new ArrayList<TransformableObject>(selected));
    }

    public void removeAllFromSelection(List<TransformableObject> objects) {
        for (TransformableObject object : objects)
            removeFromSelection(object);
    }

    public void removeFromSelection(TransformableObject object) {
        selected.remove(object);

        lblObjectsSelected.setText(selected.size() + " Object" + (selected.size() == 1 ? "" : "s") + " Selected");
        selectionChange(object, false);
        refreshTransformationXYZ();

        if (object instanceof Entity) {
            Entity entity = (Entity) object;
            MeshComponent render = entity.getComponent(MeshComponent.class);
            if (render != null)
                render.colour.w = 0;
        } else if (object instanceof EditableMesh) {
            ((EditableMesh) object).colour.w = 0;
        }

        tree.setSelectionRow(-1);
        if (selected.size() == 0)
            tabbedPane.setSelectedIndex(0);
    }

    public void setSelectedTerrain(ClientTerrain terrain) {
        if (terrain != null)
            tabbedPane.setSelectedIndex(2);

        if (terrain == selectedTerrain && terrain != null)
            return;

        terrainSelected(selectedTerrain, terrain);
        this.selectedTerrain = terrain;

        if (terrain != null) {
            unselectAll();
            tabbedPane.setSelectedIndex(2);

            ignoreTreeEvent = true;
            int i = 0;
            for (; i < gameEditor.getScene().getTerrains().size(); i++)
                if (terrain == gameEditor.getScene().getTerrains().get(i))
                    break;
            tree.setSelectionRow(i);

            Log.info("Terrain selected at " + terrain.getGX() + ", " + terrain.getGZ());
            terrain.getColour().set(0, 0, 1, 0.65f);

            lblObjectsSelected.setText("1 Object Selected");
        } else {
            lblObjectsSelected.setText("0 Objects Selected");

            tree.setSelectionRow(-1);
            tabbedPane.setSelectedIndex(0);
        }
    }

    public void update() {
        if (updateRAM) {
            long currentTime = System.currentTimeMillis();
            if (lastUpdateTime + 1000 <= currentTime) {
                lblMbRAM.setText(String.format("%.1f", getUsedMemory()) + "MB (RAM)");
                lastUpdateTime = currentTime;
            }
        }
    }

    public void editTerrain(Vector3f point) {
        if (terraform) {
            if (selectedTerrain != null) {
                selectedBrush.perform(selectedShape, selectedTerrain, point, radius, 1f / selectedTerrain.getIncrement(), strength * AppManager.getFrameTimeSeconds());
                selectedTerrain.reloadHeights();
            }
        } else if (paint) {

        }
    }

    public void reloadEntityComponents(List<TransformableObject> objects) {
        Pointer<Integer> height = new Pointer<Integer>();
        int y = 0;

        components.removeAll();
        Map<Class<?>, List<IComponent>> allComponents = new HashMap<Class<?>, List<IComponent>>();
        for (TransformableObject object : objects) {
            if (!(object instanceof Entity)) {
                List<IComponent> components = new ArrayList<IComponent>();
                if (object instanceof EditableMesh) {
                    components.add(((EditableMesh) object).transformComponent);
                    components.add(EditableMesh.editableMeshComponent);
                } else {
                    TransformComponent component = new TransformComponent();

                    component.position = object.getPosition();
                    component.rotation = object.getRotation();
                    component.scale = object.getScale();

                    components.add(component);

                    if (object instanceof Vertex)
                        components.add(new VertexComponent());
                }

                for (IComponent component : components) {
                    List<IComponent> value = allComponents.get(component.getClass());
                    if (value == null)
                        allComponents.put(component.getClass(), value = new ArrayList<IComponent>());

                    value.add(component);
                }
            } else {
                for (IComponent component : ((Entity) object).getComponents()) {
                    List<IComponent> value = allComponents.get(component.getClass());
                    if (value == null)
                        allComponents.put(component.getClass(), value = new ArrayList<IComponent>());

                    value.add(component);
                }
            }
        }

        for (TransformableObject object : objects) {
            List<Class<?>> classes = new ArrayList<Class<?>>();
            if (object instanceof Entity) {
                for (IComponent current : ((Entity) object).getComponents())
                    classes.add(current.getClass());
            } else if (object instanceof EditableMesh) {
                classes.add(TransformComponent.class);
                classes.add(EditableMeshComponent.class);
            } else {
                classes.add(TransformComponent.class);
                if (object instanceof Vertex)
                    classes.add(VertexComponent.class);
            }

            List<Class<?>> keys = new ArrayList<Class<?>>(allComponents.keySet());
            for (int i = 0; i < keys.size(); i++)
                if (!classes.contains(keys.get(i)))
                    allComponents.remove(keys.get(i));
        }

        List<Entity> entities = new ArrayList<Entity>();
        for (TransformableObject object : objects)
            if (object instanceof Entity)
                entities.add((Entity) object);

        List<Entry<Class<?>, List<IComponent>>> entries = new ArrayList<Entry<Class<?>, List<IComponent>>>(allComponents.entrySet());
        Entry<Class<?>, List<IComponent>> transformEntry = null;
        for (Entry<Class<?>, List<IComponent>> entry : entries) {
            if (entry.getKey() == TransformComponent.class) {
                transformEntry = entry;
                break;
            }
        }
        entries = ArrayUtils.rearrange(entries, transformEntry);

        for (Entry<Class<?>, List<IComponent>> entry : entries) {
            JPanel panel = PropertyUIKit.createPanel(entities, entry.getValue(), height);
            JButton button = new JButton(entry.getKey().getSimpleName().replace("Component", ""));
            panels.put(button, panel);
            button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    int y = panel.getBounds().y;

                    double addition = panel.getBounds().getHeight();
                    if (panel.getParent() == null) {
                        components.add(panel);
                    } else {
                        addition *= -1;
                        components.remove(panel);
                    }

                    for (Component component : components.getComponents()) {
                        if (component.getBounds().y >= y && component != panel && component instanceof JButton && panels.containsKey(component)) {
                            component.setBounds(component.getBounds().x, component.getBounds().y + (int) addition, component.getBounds().width, component.getBounds().height);

                            JPanel pan = panels.get(component);
                            pan.setBounds(pan.getBounds().x, component.getBounds().y + 29, pan.getBounds().width, pan.getBounds().height);

                            JButton delete = deleteButtons.get(component);
                            delete.setBounds(delete.getBounds().x, component.getBounds().y, delete.getBounds().width, delete.getBounds().height);

                            pan.revalidate();
                            pan.repaint();
                        }
                    }

                    components.revalidate();
                    components.repaint();
                }

            });

            int width = UIKit.SUBPANEL_WIDTH - 105;
            if (entry.getKey() == TransformComponent.class)
                width = UIKit.SUBPANEL_WIDTH - 35;
            button.setBounds(10, y, width, 24);
            components.add(button);
            JButton delete = new JButton("Delete");
            deleteButtons.put(button, delete);
            delete.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    for (Entity entity : entities)
                        entity.removeComponent(entity.getComponent((Class<IComponent>) entry.getKey()));
                    reloadEntityComponents(selected);
                }

            });
            delete.setBounds(UIKit.SUBPANEL_WIDTH - 90, y, 65, 24);
            components.add(delete);
            y += 29;

            panel.setBounds(10, y, UIKit.SUBPANEL_WIDTH - 35, height.get());
            y += height.get() + 5;

            components.add(panel);
            components.revalidate();
            components.repaint();
        }
    }

    public void refreshTransformationXYZ() {
        Vector3f average = new Vector3f();
        boolean shouldCalculateAverage = gameEditor.getMode() == 1; // translation
        if (!shouldCalculateAverage && selected.size() > 0)
            average.set(gameEditor.getGizmoRenderer().getVector(selected.get(0)));

        float count = 0;
        for (TransformableObject object : selected) {
            Vector3f vector = gameEditor.getGizmoRenderer().getVector(object);
            if (shouldCalculateAverage) {
                Vector3f.add(average, vector, average);
                count++;
            } else {
                if (average.x != vector.x)
                    average.x = 0;
                if (average.y != vector.y)
                    average.y = 0;
                if (average.z != vector.z)
                    average.z = 0;
            }
        }

        if (shouldCalculateAverage && count > 0) {
            average.x /= count;
            average.y /= count;
            average.z /= count;
        }

        xTransformField.setEnabled(false);
        xTransformField.setText(average.x + "");
        xTransformField.setEnabled(true);

        yTransformField.setEnabled(false);
        yTransformField.setText(average.y + "");
        yTransformField.setEnabled(true);

        zTransformField.setEnabled(false);
        zTransformField.setText(average.z + "");
        zTransformField.setEnabled(true);
    }

    public void refreshComponentValues() {
        refreshTransformationXYZ();
        PropertyUIKit.refresh(components);
    }

    private float getUsedMemory() {
        return (float) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / MB;
    }

    public void showPopupMenu() {
        scenePopupMenu.show(null, window.getWindowSpaceMouseX(), window.getWindowSpaceMouseY());
    }

    public void hidePopupMenu() {
        scenePopupMenu.setVisible(false);
    }

    private String getEntityName(Entity entity) {
        String name;
        if (!entity.containsKey("name")) {
            if (entity.containsKey("model")) {
                name = entity.getValue("model");
            } else if (entity.hasComponent(LightComponent.class)) {
                name = "Light";
            } else if (entity.hasComponent(DecalComponent.class)) {
                name = "Decal";
            } else {
                name = "Entity";
            }
        } else {
            name = entity.getValue("name");
        }

        return name;
    }

    public List<TransformableObject> getSelectedObjects() {
        return selected;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public JDesktopPane getDesktopPane() {
        return desktopPane;
    }

    public JInternalFrame getGameFrame() {
        return gameFrame;
    }

    public JTabbedPane getLeftDock() {
        return leftDock;
    }

    public JTabbedPane getRightDock() {
        return rightDock;
    }

    public static LevelEditor getInstance() {
        return instance;
    }

    public void setGameEditor(GameEditor gameEditor) {
        this.gameEditor = gameEditor;
    }

    public boolean isEditingTerrain() {
        return terraform || paint;
    }

    private void hideTitle(JInternalFrame frame) {
        ((BasicInternalFrameUI) frame.getUI()).setNorthPane(null);
        frame.setBorder(null);
    }

    public void updateList() {
        CustomMutableTreeNode root = new CustomMutableTreeNode("Scene");

        if (gameEditor != null && gameEditor.getScene() != null) {
            int i = 0;
            treePosition.clear();

            for (Terrain terrain : gameEditor.getScene().getTerrains()) {
                CustomMutableTreeNode node = new CustomMutableTreeNode("Terrain (" + terrain.getGX() + ", " + terrain.getGZ() + ")");
                node.setStorage(terrain);

                i++;
                root.add(node);
            }

            for (Entity entity : gameEditor.getScene().getEntities()) {
                if (entity.isHidden() || entity.getParent() != null)
                    continue;

                CustomMutableTreeNode node = new CustomMutableTreeNode(getEntityName(entity));
                node.setStorage(entity);

                i++;
                treePosition.put(entity, i);

                for (Entity child : entity.getChildren()) {
                    CustomMutableTreeNode childNode = new CustomMutableTreeNode(getEntityName(child));
                    childNode.setStorage(child);

                    i++;
                    treePosition.put(child, i);

                    node.add(childNode);
                }

                root.add(node);
            }

            for (EditableMesh mesh : gameEditor.getEditableMeshes()) {
                CustomMutableTreeNode node = new CustomMutableTreeNode("Editable Mesh");
                node.setStorage(mesh);

                i++;
                root.add(node);
            }
        } else {
            CustomMutableTreeNode node = new CustomMutableTreeNode("Loading...");
            node.setStorage("Reload");

            root.add(node);
        }

        ((DefaultTreeModel) tree.getModel()).setRoot(root);
        expand(tree, 0, tree.getRowCount());
    }

    private void expand(JTree tree, int start, int count) {
        for (int i = start; i < count; i++)
            tree.expandRow(i);

        if (tree.getRowCount() != count)
            expand(tree, count, tree.getRowCount());
    }

    public void addTextFieldListener(JTextField textField, Runnable runnable) {
        textField.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {
                if (textField.isEnabled())
                    runnable.run();
            }

            public void removeUpdate(DocumentEvent e) {
                if (textField.isEnabled())
                    runnable.run();
            }

            public void insertUpdate(DocumentEvent e) {
                if (textField.isEnabled())
                    runnable.run();
            }

        });
    }

    public void startDrag() {
        if (!sceneFrame.isVisible()) {
            sceneFrame.setVisible(true);

            Rectangle bounds = gameFrame.getBounds();
            gameFrame.setBounds(sceneFrame.getBounds().x + sceneFrame.getBounds().width, bounds.y, bounds.width - sceneFrame.getBounds().width, bounds.height);

            bounds = logFrame.getBounds();
            logFrame.setBounds(sceneFrame.getBounds().x + sceneFrame.getBounds().width, bounds.y, bounds.width - sceneFrame.getBounds().width, bounds.height);
            for (Component component : logFrame.getContentPane().getComponents()) {
                bounds = component.getBounds();
                component.setBounds(bounds.x, bounds.y, bounds.width - sceneFrame.getBounds().width, bounds.height);
            }
        }

        if (!actionFrame.isVisible()) {
            actionFrame.setVisible(true);

            Rectangle bounds = gameFrame.getBounds();
            gameFrame.setBounds(bounds.x, bounds.y, bounds.width - actionFrame.getBounds().width, bounds.height);

            bounds = logFrame.getBounds();
            logFrame.setBounds(bounds.x, bounds.y, bounds.width - actionFrame.getBounds().width, bounds.height);
            for (Component component : logFrame.getContentPane().getComponents()) {
                bounds = component.getBounds();
                component.setBounds(bounds.x, bounds.y, bounds.width - actionFrame.getBounds().width, bounds.height);
            }
        }
    }

    public void stopDrag(JTabbedPane pane) {
        if (leftDock.getTabCount() == 0 && pane != leftDock) {
            sceneFrame.setVisible(false);

            Rectangle bounds = gameFrame.getBounds();
            gameFrame.setBounds(sceneFrame.getBounds().x, bounds.y, bounds.width + sceneFrame.getBounds().width, bounds.height);

            bounds = logFrame.getBounds();
            logFrame.setBounds(sceneFrame.getBounds().x, bounds.y, bounds.width + sceneFrame.getBounds().width, bounds.height);
            for (Component component : logFrame.getContentPane().getComponents()) {
                bounds = component.getBounds();
                component.setBounds(bounds.x, bounds.y, bounds.width + sceneFrame.getBounds().width, bounds.height);
            }
        }

        if (rightDock.getTabCount() == 0 && pane != rightDock) {
            actionFrame.setVisible(false);

            Rectangle bounds = gameFrame.getBounds();
            gameFrame.setBounds(bounds.x, bounds.y, bounds.width + actionFrame.getBounds().width, bounds.height);

            bounds = logFrame.getBounds();
            logFrame.setBounds(bounds.x, bounds.y, bounds.width + actionFrame.getBounds().width, bounds.height);
            for (Component component : logFrame.getContentPane().getComponents()) {
                bounds = component.getBounds();
                component.setBounds(bounds.x, bounds.y, bounds.width + actionFrame.getBounds().width, bounds.height);
            }
        }
    }

    private int parseInt(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
        }

        return 0;
    }

    private float parseFloat(String input) {
        try {
            return Float.parseFloat(input);
        } catch (NumberFormatException e) {
        }

        return 0;
    }

}
