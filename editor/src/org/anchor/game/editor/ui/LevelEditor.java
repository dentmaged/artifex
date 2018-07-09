package org.anchor.game.editor.ui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JButton;
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
import javax.swing.tree.TreePath;

import org.anchor.client.engine.renderer.Settings;
import org.anchor.engine.common.TextureType;
import org.anchor.engine.common.utils.FileHelper;
import org.anchor.engine.common.utils.Pointer;
import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.components.LivingComponent;
import org.anchor.engine.shared.components.PhysicsComponent;
import org.anchor.engine.shared.components.SpawnComponent;
import org.anchor.engine.shared.components.TransformComponent;
import org.anchor.engine.shared.entity.Entity;
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
import org.anchor.game.client.GameClient;
import org.anchor.game.client.app.AppManager;
import org.anchor.game.client.async.Requester;
import org.anchor.game.client.components.DecalComponent;
import org.anchor.game.client.components.LightComponent;
import org.anchor.game.client.components.MeshComponent;
import org.anchor.game.client.components.ReflectionProbeComponent;
import org.anchor.game.client.components.SoundComponent;
import org.anchor.game.client.components.SlidingDoorComponent;
import org.anchor.game.client.components.WaterComponent;
import org.anchor.game.client.loaders.AssetLoader;
import org.anchor.game.client.storage.PrefabReader;
import org.anchor.game.client.types.ClientTerrain;
import org.anchor.game.editor.GameEditor;
import org.anchor.game.editor.properties.PropertyUIKit;
import org.anchor.game.editor.terrain.brush.IncreaseDecreaseHeightBrush;
import org.anchor.game.editor.terrain.brush.SetHeightBrush;
import org.anchor.game.editor.terrain.brush.SmoothBrush;
import org.anchor.game.editor.terrain.brush.TerrainBrush;
import org.anchor.game.editor.terrain.shape.Shape;
import org.anchor.game.editor.utils.AssetManager;
import org.anchor.game.editor.utils.LogRedirector;
import org.anchor.game.editor.utils.RenderSettings;
import org.lwjgl.util.vector.Vector3f;

public class LevelEditor extends JPanel {

    private JTextField xTransformField;
    private JTextField yTransformField;
    private JTextField zTransformField;
    private JLabel lblObjectsSelected;
    private static final long serialVersionUID = 1625891826028289732L;

    private JTabbedPane LEFT_DOCK_STATION = new DraggableTabbedPane();
    private JTabbedPane RIGHT_DOCK_STATION = new DraggableTabbedPane();

    private JTabbedPane tabbedPane;
    private JPopupMenu scenePopupMenu, assetBrowserPopupMenu;

    private Canvas canvas;
    private JTable environmentPropertiesTable;
    private JTree tree;

    private boolean ignoreTreeEvent;

    private Window window;
    private GameEditor gameEditor;
    private Entity selectedEntity;
    private ClientTerrain selectedTerrain;
    private JPanel components;

    private Map<JButton, JPanel> panels = new HashMap<JButton, JPanel>();
    private Map<JButton, JButton> deleteButtons = new HashMap<JButton, JButton>();
    private Map<Entity, Integer> treePosition = new HashMap<Entity, Integer>();
    private JTextField snapField;

    private static float MB = 1024 * 1024;
    private JTextField speedField;
    private JLabel lblMbRAM;
    private boolean updateRAM;
    private long lastUpdateTime;

    private float radius = 5;
    private float strength = 1;
    private TerrainBrush selectedBrush = BRUSHES[0];
    private Shape selectedShape = SHAPES[0];

    protected boolean paint, terraform;

    private static Class<?>[] COMPONENTS = new Class[] {
            MeshComponent.class, LightComponent.class, PhysicsComponent.class, SoundComponent.class, WaterComponent.class, SpawnComponent.class, SlidingDoorComponent.class, DecalComponent.class, ReflectionProbeComponent.class
    };

    private static TerrainBrush[] BRUSHES = new TerrainBrush[] {
            new IncreaseDecreaseHeightBrush(), new SetHeightBrush(), new SmoothBrush()
    };

    private static Shape[] SHAPES = AssetManager.getShapes();

    public LevelEditor() {
        setLayout(null);
        this.window = Window.getInstance();

        JDesktopPane desktopPane = new JDesktopPane();
        desktopPane.setBounds(0, 0, 1918, 943);
        add(desktopPane);

        JInternalFrame gameFrame = new JInternalFrame("Perspective");
        gameFrame.getContentPane().setBackground(Color.BLACK);
        gameFrame.setBounds(316, -2, 1281, 745);
        gameFrame.setFrameIcon(null);
        desktopPane.add(gameFrame);
        gameFrame.getContentPane().setLayout(null);

        canvas = new Canvas();
        canvas.setBounds(0, 0, 1280, 720);
        gameFrame.getContentPane().add(canvas);

        JInternalFrame logFrame = new JInternalFrame("Log");
        logFrame.setBounds(316, 744, 1281, 197);
        logFrame.setFrameIcon(null);
        desktopPane.add(logFrame);
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

        xTransformField = new JTextField();
        xTransformField.setBounds(139, 3, 86, 20);
        panel.add(xTransformField);
        xTransformField.setColumns(10);

        JLabel lblY = new JLabel("Y:");
        lblY.setBounds(235, 6, 10, 14);
        panel.add(lblY);

        yTransformField = new JTextField();
        yTransformField.setColumns(10);
        yTransformField.setBounds(255, 3, 86, 20);
        panel.add(yTransformField);

        JLabel lblZ = new JLabel("Z:");
        lblZ.setBounds(351, 6, 10, 14);
        panel.add(lblZ);

        zTransformField = new JTextField();
        zTransformField.setColumns(10);
        zTransformField.setBounds(371, 3, 86, 20);
        panel.add(zTransformField);

        JLabel lblSpeed = new JLabel("Speed: ");
        lblSpeed.setBounds(469, 6, 38, 14);
        panel.add(lblSpeed);

        speedField = new JTextField();
        speedField.setText("2");
        addTextFieldListener(speedField, new Runnable() {

            @Override
            public void run() {
                GameClient.getPlayer().getComponent(LivingComponent.class).noPhysicsSpeed = parseFloat(speedField.getText());
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
                System.out.println(String.format("%.1f", ram - getUsedMemory()) + "MB of RAM has been freed.");
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

        JInternalFrame sceneFrame = new JInternalFrame("Scene");
        sceneFrame.setBounds(0, 0, 317, 940);
        desktopPane.add(sceneFrame);
        sceneFrame.getContentPane().setLayout(new BorderLayout());
        sceneFrame.getContentPane().add(LEFT_DOCK_STATION, BorderLayout.CENTER);

        scenePopupMenu = new JPopupMenu();
        scenePopupMenu.add(new JMenuItem(new AbstractAction("Copy") {

            private static final long serialVersionUID = 1494773096597716078L;

            @Override
            public void actionPerformed(ActionEvent event) {
                if (selectedEntity != null) {
                    gameEditor.addEntity(selectedEntity.copy());
                    selectedEntity.getPosition().set(5, 0, -8);
                }

                if (selectedTerrain != null) {
                    String[] split = JOptionPane.showInputDialog(null, "Grid coordinates: ", "Copy terrain", JOptionPane.QUESTION_MESSAGE).split(Pattern.quote(","));
                    if (split.length < 2) {
                        JOptionPane.showMessageDialog(null, "Invalid coordaintes", "Failed to copy terrain", JOptionPane.ERROR_MESSAGE);

                        return;
                    }

                    gameEditor.addTerrain(selectedTerrain.getHeightmap(), parseInt(split[0]), parseInt(split[1]));
                }

                scenePopupMenu.setVisible(false);
            }

        }));
        scenePopupMenu.add(new JMenuItem(new AbstractAction("Delete") {

            private static final long serialVersionUID = -251360568773242114L;

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (selectedEntity != null) {
                    gameEditor.removeEntity(selectedEntity);
                    setSelectedEntity(null);
                }

                if (selectedTerrain != null) {
                    gameEditor.removeTerrain(selectedTerrain);
                    setSelectedTerrain(null);
                }

                scenePopupMenu.setVisible(false);
            }

        }));
        scenePopupMenu.add(new JMenuItem(new AbstractAction("Set parent") {

            private static final long serialVersionUID = -7509797304178919452L;

            @Override
            public void actionPerformed(ActionEvent event) {
                if (selectedEntity != null) {
                    String name = JOptionPane.showInputDialog(null, "Type the name of the entity: ", "Set entity parent", JOptionPane.PLAIN_MESSAGE);
                    selectedEntity.setParent(null);
                    for (Entity entity : gameEditor.getScene().getEntities()) {
                        if (entity.isHidden())
                            continue;

                        if (getEntityName(entity).equalsIgnoreCase(name)) {
                            selectedEntity.setParent(entity);

                            break;
                        }
                    }
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
                if (store instanceof Entity)
                    setSelectedEntity((Entity) store);
                else if (store instanceof ClientTerrain)
                    setSelectedTerrain((ClientTerrain) store);
                else if (store instanceof String)
                    updateList();
            }

        });
        updateList();
        LEFT_DOCK_STATION.addTab("Scene", scenePanel);

        JPanel assetBrowserPanel = new JPanel();
        JTree assetBrowserFileSystem = UIKit.createTree(assetBrowserPanel);
        assetBrowserPopupMenu = new JPopupMenu();
        assetBrowserPopupMenu.add(new JMenuItem(new AbstractAction("Create model") {

            private static final long serialVersionUID = -2739758793155285084L;

            @Override
            public void actionPerformed(ActionEvent event) {
                File file = (File) ((CustomMutableTreeNode) assetBrowserFileSystem.getLastSelectedPathComponent()).getStorage();
                if (file.isFile() && file.getName().endsWith("obj")) {
                    String model = file.getName().replace(".obj", "");

                    Entity entity = gameEditor.addEntity(false);
                    entity.setValue("model", model);
                    entity.addComponent(new MeshComponent());
                    setSelectedEntity(entity);
                }
            }

        }));
        assetBrowserPopupMenu.add(new JMenuItem(new AbstractAction("Set as model") {

            private static final long serialVersionUID = -2739758793155285084L;

            @Override
            public void actionPerformed(ActionEvent event) {
                if (selectedEntity != null) {
                    MeshComponent mesh = selectedEntity.getComponent(MeshComponent.class);
                    if (mesh != null) {
                        File file = (File) ((CustomMutableTreeNode) assetBrowserFileSystem.getLastSelectedPathComponent()).getStorage();
                        if (file.isFile() && file.getName().endsWith("obj")) {
                            String model = file.getName().replace(".obj", "");

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
                        selectedTerrain.loadHeightsFromHeightmap(file.getName().replace(".png", ""));
                }
            }

        }));
        assetBrowserPopupMenu.addSeparator();
        assetBrowserPopupMenu.add(new JMenuItem(new AbstractAction("Open in explorer") {

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
        expand(assetBrowserFileSystem, new TreePath(assetBrowserFileSystem.getModel().getRoot()));
        LEFT_DOCK_STATION.addTab("Asset Browser", assetBrowserPanel);

        JInternalFrame actionFrame = new JInternalFrame("Action");
        actionFrame.setBorder(null);
        actionFrame.setBounds(1599, 0, 317, 943);
        desktopPane.add(actionFrame);
        actionFrame.getContentPane().setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        actionFrame.getContentPane().add(RIGHT_DOCK_STATION, BorderLayout.CENTER);
        RIGHT_DOCK_STATION.addTab("Action", tabbedPane);

        JPanel addPanel = new JPanel();
        tabbedPane.addTab("Add", null, addPanel, null);
        addPanel.setLayout(null);

        Pointer<Integer> height = new Pointer<Integer>();

        TextFieldBlueprint createGridX = new TextFieldBlueprint("Grid X: ", "0");
        TextFieldBlueprint createGridZ = new TextFieldBlueprint("Grid Z: ", "0");
        DropdownBlueprint createHeightmaps = new DropdownBlueprint("Heightmap: ", AssetManager.getHeightmaps());

        JScrollPane terrainCreatePanel = UIKit.createSubpanel("Terrain", Arrays.asList(createGridX, createGridZ, createHeightmaps, new ButtonBlueprint("Create", new ButtonListener() {

            @Override
            public void onButtonClick(CustomButton field) {
                gameEditor.addTerrain(createHeightmaps.getSelectedItem(), parseInt(createGridX.getText()), parseInt(createGridZ.getText()));

                removeSubpanel();
            }

        })), height);
        terrainCreatePanel.setBounds(UIKit.SUBPANEL_X, 119, UIKit.SUBPANEL_WIDTH, height.get());

        DropdownBlueprint createModel = new DropdownBlueprint("Model: ", AssetManager.getModels());
        JScrollPane geometryCreatePanel = UIKit.createSubpanel("Geometry", Arrays.asList(createModel, new ButtonBlueprint("Create", new ButtonListener() {

            @Override
            public void onButtonClick(CustomButton field) {
                Entity entity = gameEditor.addEntity(false);
                entity.setValue("model", (String) createModel.getSelectedItem());
                entity.addComponent(new MeshComponent());
                setSelectedEntity(entity);

                removeSubpanel();
            }

        })), height);
        geometryCreatePanel.setBounds(UIKit.SUBPANEL_X, 119, UIKit.SUBPANEL_WIDTH, height.get());

        DropdownBlueprint prefabDropdown = new DropdownBlueprint("Prefab: ", AssetManager.getPrefabs());
        JScrollPane prefabCreatePanel = UIKit.createSubpanel("Prefab", Arrays.asList(prefabDropdown, new ButtonBlueprint("Load", new ButtonListener() {

            @Override
            public void onButtonClick(CustomButton field) {
                gameEditor.getScene().getEntities().addAll(PrefabReader.read(FileHelper.newGameFile("res", prefabDropdown.getSelectedItem() + ".pfb")));
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
                    try {
                        if (selectedEntity != null) {
                            selectedEntity.addComponent((IComponent) one.newInstance());
                            reloadEntityComponents(selectedEntity);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }, new ButtonListener() {

                @Override
                public void onButtonClick(CustomButton field) {
                    try {
                        if (selectedEntity != null) {
                            selectedEntity.addComponent((IComponent) two.newInstance());
                            reloadEntityComponents(selectedEntity);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
                        try {
                            if (selectedEntity != null) {
                                selectedEntity.addComponent((IComponent) component.newInstance());

                                menu.setVisible(false);
                                reloadEntityComponents(selectedEntity);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
                if (selectedEntity != null)
                    selectedEntity.setValue("name", field.getText());
                updateList();
            }

            @Override
            public void onEntitySelect(Entity previous, Entity current) {
                CustomTextField field = (CustomTextField) component;

                field.setEnabled(false);
                if (current != null) {
                    reloadEntityComponents(current);

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
        entityPanel.add(entityName);
        entityName.setColumns(10);

        JPanel componentsPanel = new JPanel();
        componentsPanel.setBorder(new TitledBorder(null, "Components", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        componentsPanel.setBounds(UIKit.SUBPANEL_X, 183, UIKit.SUBPANEL_WIDTH, 709);
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

        }), new DropdownBlueprint("Heightmap: ", AssetManager.getHeightmaps(), new DropdownListener() {

            @Override
            public void onDropdownSelect(CustomDropdown field) {
                if (field.isEnabled() && selectedTerrain != null)
                    selectedTerrain.loadHeightsFromHeightmap((String) field.getSelectedItem());
            }

            @Override
            public void onTerrainSelect(Terrain previous, Terrain current) {
                CustomDropdown dropdown = (CustomDropdown) component;

                dropdown.setEnabled(false);
                if (current != null) {
                    dropdown.setSelectedIndex(AssetManager.getIndex(dropdown, current.getHeightmap()));
                    dropdown.setEnabled(true);
                } else {
                    dropdown.setSelectedIndex(0);
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
        System.out.println(terrainBrushPanel.getBounds().y + height.get() + 19);

        JPanel environmentPanel = new JPanel();
        tabbedPane.addTab("Environment", null, environmentPanel, null);
        environmentPanel.setLayout(new BorderLayout(0, 0));

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

        environmentPropertiesTable.setModel(new DefaultTableModel(new Object[][] {
                {
                        "Wireframe", Settings.wireframe
                }, {
                        "Procedural Skybox", Settings.proceduralSky
                }, {
                        "Perform Lighting", Settings.performLighting
                }, {
                        "Perform SSAO", Settings.performSSAO
                }, {
                        "Show Lightmaps", Settings.showLightmaps
                }, {
                        "Minimum Diffuse", Settings.minDiffuse
                }, {
                        "Fog Density", Settings.density
                }, {
                        "Fog Gradient", Settings.gradient
                }, {
                        "Exposure Speed", Settings.exposureSpeed
                }
        }, new String[] {
                "Property", "Value"
        }) {

            private static final long serialVersionUID = 3225320749175276075L;

            Class<?>[] columnTypes = new Class[] {
                    String.class, Object.class
            };

            public Class<?> getColumnClass(int columnIndex) {
                return columnTypes[columnIndex];
            }

            boolean[] columnEditables = new boolean[] {
                    false, true
            };

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

        JPanel modelPanel = new JPanel();
        RIGHT_DOCK_STATION.addTab("Model Settings", modelPanel);
        modelPanel.setLayout(null);

        JScrollPane modelSettingsPanel = UIKit.createSubpanel("Model Settings", Arrays.asList(new TextFieldBlueprint("Rows: ", "1", new TextFieldListener() {

            @Override
            public void onTextFieldEdit(CustomTextField field) {
                if (selectedEntity != null)
                    selectedEntity.getComponent(MeshComponent.class).model.getTexture().setNumberOfRows(parseInt(field.getText(), 1));
            }

            @Override
            public void onEntitySelect(Entity previous, Entity current) {
                CustomTextField field = (CustomTextField) component;

                field.setEnabled(false);
                if (current != null) {
                    MeshComponent mesh = current.getComponent(MeshComponent.class);
                    if (mesh != null && mesh.model != null && mesh.model.getMesh() != null) {
                        field.setText(mesh.model.getTexture().getNumberOfRows() + "");
                        field.setEnabled(true);
                    }
                } else {
                    field.setText("1");
                }
            }

        }), new CheckboxBlueprint("Backface culling: ", false, new CheckboxListener() {

            @Override
            public void onCheckboxEdit(CustomCheckbox checkbox) {
                if (selectedEntity != null)
                    selectedEntity.getComponent(MeshComponent.class).model.getTexture().setCullingEnabled(checkbox.isSelected());
            }

            @Override
            public void onEntitySelect(Entity previous, Entity current) {
                CustomCheckbox checkbox = (CustomCheckbox) component;

                checkbox.setEnabled(false);
                if (current != null) {
                    MeshComponent mesh = current.getComponent(MeshComponent.class);
                    if (mesh != null && mesh.model != null && mesh.model.getMesh() != null) {
                        checkbox.setSelected(mesh.model.getTexture().isCullingEnabled());
                        checkbox.setEnabled(true);
                    }
                } else {
                    checkbox.setSelected(false);
                }
            }

        }), new CheckboxBlueprint("Blending: ", false, new CheckboxListener() {

            @Override
            public void onCheckboxEdit(CustomCheckbox checkbox) {
                if (selectedEntity != null)
                    selectedEntity.getComponent(MeshComponent.class).model.getTexture().setBlendingEnabled(checkbox.isSelected());
            }

            @Override
            public void onEntitySelect(Entity previous, Entity current) {
                CustomCheckbox checkbox = (CustomCheckbox) component;

                checkbox.setEnabled(false);
                if (current != null) {
                    MeshComponent mesh = current.getComponent(MeshComponent.class);
                    if (mesh != null && mesh.model != null && mesh.model.getMesh() != null) {
                        checkbox.setSelected(mesh.model.getTexture().isBlendingEnabled());
                        checkbox.setEnabled(true);
                    }
                } else {
                    checkbox.setSelected(false);
                }
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

        setSelectedEntity(null);
        setSelectedTerrain(null);
    }

    private static void entitySelected(Entity selected, Entity entity) {
        if (selected != null && selected.hasComponent(MeshComponent.class))
            selected.getComponent(MeshComponent.class).colour.w = 0;

        UIListener.entitySelected(selected, entity);
    }

    private static void terrainSelected(ClientTerrain selected, ClientTerrain terrain) {
        if (selected != null)
            selected.getColour().w = 0;

        UIListener.terrainSelected(selected, terrain);
    }

    public void setSelectedEntity(Entity entity) {
        if (entity != null) {
            if (entity.isHidden())
                entity = null;
            else
                tabbedPane.setSelectedIndex(1);
        }

        if (entity == selectedEntity && entity != null)
            return;

        entitySelected(selectedEntity, entity);
        this.selectedEntity = entity;
        if (entity != null) {
            terrainSelected(selectedTerrain, null);

            selectedTerrain = null;
            tabbedPane.setSelectedIndex(1);

            ignoreTreeEvent = true;
            tree.setSelectionRow(treePosition.get(entity));

            MeshComponent render = entity.getComponent(MeshComponent.class);
            if (render != null)
                render.colour.set(1, 0, 0, 0.65f);

            lblObjectsSelected.setText("1 Object Selected");
            System.out.println("Entity selected at " + entity.getPosition().x + ", " + entity.getPosition().y + ", " + entity.getPosition().z);
        } else {
            lblObjectsSelected.setText("0 Objects Selected");

            tree.setSelectionRow(-1);
            tabbedPane.setSelectedIndex(0);
        }
    }

    public void setSelectedTerrain(ClientTerrain terrain) {
        if (terrain != null)
            tabbedPane.setSelectedIndex(2);

        if (terrain == selectedTerrain && terrain != null)
            return;

        terrainSelected(selectedTerrain, terrain);
        this.selectedTerrain = terrain;

        if (terrain != null) {
            entitySelected(selectedEntity, null);

            selectedEntity = null;
            tabbedPane.setSelectedIndex(2);

            ignoreTreeEvent = true;
            int i = 0;
            for (; i < gameEditor.getScene().getTerrains().size(); i++)
                if (terrain == gameEditor.getScene().getTerrains().get(i))
                    break;
            tree.setSelectionRow(i);

            System.out.println("Terrain selected at " + terrain.getGX() + ", " + terrain.getGZ());
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

    private void reloadEntityComponents(Entity entity) {
        Pointer<Integer> height = new Pointer<Integer>();
        int y = 0;

        components.removeAll();

        for (IComponent component : entity.getComponents()) {
            JPanel panel = PropertyUIKit.createPanel(entity, component, height);
            JButton button = new JButton(component.getClass().getSimpleName().replace("Component", ""));
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
            if (component.getClass() == TransformComponent.class)
                width = UIKit.SUBPANEL_WIDTH - 35;
            button.setBounds(10, y, width, 24);
            components.add(button);
            JButton delete = new JButton("Delete");
            deleteButtons.put(button, delete);
            delete.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    entity.removeComponent(component);
                    reloadEntityComponents(entity);
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

    public void refreshComponentValues() {
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

    public Entity getSelectedEntity() {
        return selectedEntity;
    }

    public Canvas getCanvas() {
        return canvas;
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
        } else {
            CustomMutableTreeNode node = new CustomMutableTreeNode("Loading...");
            node.setStorage("Reload");

            root.add(node);
        }

        ((DefaultTreeModel) tree.getModel()).setRoot(root);
        expand(tree, new TreePath(root));
    }

    private void expand(JTree tree, TreePath path) {
        CustomMutableTreeNode node = (CustomMutableTreeNode) path.getLastPathComponent();

        if (node.getChildCount() > 0) {
            Enumeration<?> enumeration = node.children();
            while (enumeration.hasMoreElements())
                expand(tree, path.pathByAddingChild(enumeration.nextElement()));
        }

        tree.expandPath(path);
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
