package org.anchor.game.editor.properties;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.anchor.client.engine.renderer.types.Material;
import org.anchor.client.engine.renderer.types.Model;
import org.anchor.engine.common.script.Script;
import org.anchor.engine.common.script.ScriptField;
import org.anchor.engine.common.script.ScriptProperty;
import org.anchor.engine.common.utils.EnumUtils;
import org.anchor.engine.common.utils.FieldWrapper;
import org.anchor.engine.common.utils.FileHelper;
import org.anchor.engine.common.utils.JavaField;
import org.anchor.engine.common.utils.ObjectUtils;
import org.anchor.engine.common.utils.Pointer;
import org.anchor.engine.common.utils.StringUtils;
import org.anchor.engine.shared.components.ScriptComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.ui.UIKit;
import org.anchor.engine.shared.utils.Property;
import org.anchor.game.client.components.MeshComponent;
import org.anchor.game.client.loaders.AssetLoader;
import org.anchor.game.client.particles.ParticleTexture;
import org.anchor.game.client.utils.Sound;
import org.anchor.game.editor.commands.Undo;
import org.anchor.game.editor.ui.AssetPicker;
import org.anchor.game.editor.ui.LevelEditor;
import org.anchor.game.editor.utils.AssetManager;
import org.anchor.game.editor.utils.FileCallback;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class PropertyUIKit {

    public static int WIDTH = UIKit.SUBPANEL_WIDTH - 40;

    public static JPanel createPanel(List<Entity> entities, List<IComponent> components, Pointer<Integer> height) {
        JPanel panel = new JPanel();
        panel.setLayout(null);

        int y = 0;
        int last = 0;

        Class<?> clazz = components.get(0).getClass();
        for (Field field : clazz.getFields()) {
            Property property = field.getAnnotation(Property.class);
            if (property != null) {
                for (Component swingComponenet : createComponents(entities, components, new FieldWrapper(field), property.value(), height, y))
                    panel.add(swingComponenet);

                last = height.get() + 5;
                y += last;
            }
        }

        if (clazz == ScriptComponent.class) {
            for (ScriptProperty property : ((ScriptComponent) components.get(0)).script.getProperties()) {
                for (Component swingComponenet : createComponents(entities, components, new ScriptField(property), property.getName(), height, y))
                    panel.add(swingComponenet);

                last = height.get() + 5;
                y += last;
            }
        }

        for (Method method : clazz.getMethods()) {
            Property property = method.getAnnotation(Property.class);
            if (property != null) {
                JButton button = new JButton(property.value());
                button.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        try {
                            for (IComponent component : components)
                                method.invoke(component);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                });
                button.setBounds(10, y, WIDTH - 20, 23);
                panel.add(button);

                last = height.get() + 5;
                y += last;
            }
        }

        height.set(y + last - 25);

        return panel;
    }

    private static List<Component> createComponents(List<Entity> entities, List<IComponent> icomponents, JavaField field, String name, Pointer<Integer> height, int y) {
        List<Component> components = new ArrayList<Component>();
        int splitWidth = WIDTH / 2 - 30;
        int x = 10;

        JLabel label = new JLabel(name);
        label.setBounds(x, y + 4, splitWidth, 14);
        components.add(label);

        x += splitWidth + 5;
        int remainingWidth = WIDTH - splitWidth - 20;

        try {
            if (field.getType() == Model.class) {
                JButton button = AssetPicker.create(getValue(entities, "model"), "obj", new FileCallback() {

                    @Override
                    public void run(File file) {
                        String path = FileHelper.removeFileExtension(FileHelper.localFileName(file));
                        for (Entity entity : entities)
                            entity.setValue("model", path);

                        Undo.fieldsSet(field, new ArrayList<Object>(icomponents), AssetLoader.loadModel(path));
                    }

                });
                button.setBounds(x, y, remainingWidth, 24);
                components.add(button);
            } else if (field.getType() == Material.class) {
                JButton button = AssetPicker.create(getValue(entities, "material"), "aem", new FileCallback() {

                    @Override
                    public void run(File file) {
                        String path = FileHelper.removeFileExtension(FileHelper.localFileName(file));
                        for (Entity entity : entities)
                            entity.setValue("material", path);

                        Undo.fieldsSet(field, new ArrayList<Object>(icomponents), AssetLoader.loadMaterial(path));
                        for (IComponent icomponent : icomponents)
                            if (icomponent instanceof MeshComponent)
                                ((MeshComponent) icomponent).refreshShader();
                    }

                });
                button.setBounds(x, y, remainingWidth, 24);
                components.add(button);
            } else if (field.getType() == Script.class) {
                JButton button = AssetPicker.create(getValue(entities, "script"), "js", new FileCallback() {

                    @Override
                    public void run(File file) {
                        String path = FileHelper.removeFileExtension(FileHelper.localFileName(file));
                        for (Entity entity : entities)
                            entity.setValue("script", path);

                        List<Script> scripts = new ArrayList<Script>();
                        for (IComponent icomponent : icomponents)
                            scripts.add(ScriptComponent.load((ScriptComponent) icomponent, path));

                        Undo.fieldsSetArray(field, new ArrayList<Object>(icomponents), new ArrayList<Object>(scripts));
                        LevelEditor.getInstance().reloadEntityComponents(LevelEditor.getInstance().getSelectedObjects());
                    }

                });
                button.setBounds(x, y, remainingWidth, 24);
                components.add(button);
            } else if (field.getType() == ParticleTexture.class) {
                JButton button = AssetPicker.create(getValue(entities, "particleTexture"), "aep", new FileCallback() {

                    @Override
                    public void run(File file) {
                        String path = FileHelper.removeFileExtension(FileHelper.localFileName(file));
                        for (Entity entity : entities)
                            entity.setValue("particleTexture", path);

                        Undo.fieldsSet(field, new ArrayList<Object>(icomponents), AssetLoader.loadParticle(path));
                    }

                });
                button.setBounds(x, y, remainingWidth, 24);
                components.add(button);
            } else if (field.getType().isEnum()) {
                List<String> names = new ArrayList<String>();
                for (Object object : field.getType().getEnumConstants())
                    names.add(StringUtils.upperCaseFirst(object.toString().toLowerCase()));

                PropertyDropdown dropdown = new PropertyDropdown(field, new ArrayList<Object>(icomponents), names) {

                    private static final long serialVersionUID = 7503855159428548350L;

                    @Override
                    public Object convert(String value) {
                        for (Entity entity : entities)
                            entity.setValue(StringUtils.lowerCaseFirst(field.getType().getSimpleName()), value);
                        LevelEditor.getInstance().updateList();

                        return EnumUtils.getEnumValue(field.getType().getEnumConstants(), value);
                    }

                    @Override
                    public void updateDropdown() {
                        try {
                            Enum<?> target = (Enum<?>) field.get(icomponents.get(0));
                            for (IComponent icomponent : icomponents) {
                                Enum<?> value = (Enum<?>) field.get(icomponent);
                                if (target != value)
                                    target = null;
                            }

                            if (target == null)
                                setSelectedIndex(AssetManager.getIndex(this, getValue(entities, StringUtils.lowerCaseFirst(field.getType().getSimpleName()))));
                            else
                                setSelectedIndex(AssetManager.getIndex(this, target.name()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                };
                dropdown.update();
                dropdown.setBounds(x, y, remainingWidth, 23);
                components.add(dropdown);
            } else if (field.getType() == Sound.class) {
                PropertyDropdown dropdown = new PropertyDropdown(field, new ArrayList<Object>(icomponents), AssetManager.getModels()) {

                    private static final long serialVersionUID = 3559333278954472616L;

                    @Override
                    public Object convert(String value) {
                        for (Entity entity : entities)
                            entity.setValue("sound", value);
                        LevelEditor.getInstance().updateList();

                        return new Sound(value);
                    }

                    @Override
                    public void updateDropdown() {
                        try {
                            Sound target = (Sound) field.get(icomponents.get(0));
                            for (IComponent icomponent : icomponents) {
                                Sound value = (Sound) field.get(icomponent);
                                if (target != value)
                                    target = null;
                            }

                            if (target == null)
                                setSelectedIndex(AssetManager.getIndex(this, getValue(entities, "sound")));
                            else
                                setSelectedIndex(AssetManager.getIndex(this, target.getName()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                };
                dropdown.updateDropdown();
                dropdown.setBounds(x, y, remainingWidth, 23);
                components.add(dropdown);
            } else if (field.getType() == Vector2f.class) {
                int remainingComponentWidth = remainingWidth / 2;
                List<Object> vectors = new ArrayList<Object>();
                String[] names = { "x", "y" };

                for (IComponent icomponent : icomponents)
                    vectors.add(field.get(icomponent));

                for (int i = 0; i < 2; i++) {
                    PropertyTextField textField = new PropertyTextField(new FieldWrapper(Vector2f.class.getField(names[i])), vectors) {

                        private static final long serialVersionUID = 6683094598924013971L;

                        @Override
                        public Object convert(String value) {
                            return parseFloat(value);
                        }

                    };
                    textField.setBounds(x, y, remainingComponentWidth - 5, 23);
                    x += remainingComponentWidth;
                    components.add(textField);
                }
            } else if (field.getType() == Vector3f.class) {
                int remainingComponentWidth = remainingWidth / 3;
                List<Object> vectors = new ArrayList<Object>();
                String[] names = { "x", "y", "z" };

                for (IComponent icomponent : icomponents)
                    vectors.add(field.get(icomponent));

                for (int i = 0; i < 3; i++) {
                    PropertyTextField textField = new PropertyTextField(new FieldWrapper(Vector3f.class.getField(names[i])), vectors) {

                        private static final long serialVersionUID = -7852004572494187156L;

                        @Override
                        public Object convert(String value) {
                            return parseFloat(value);
                        }

                    };
                    textField.setBounds(x, y, remainingComponentWidth - 5, 23);
                    x += remainingComponentWidth;
                    components.add(textField);
                }
            } else if (field.getType() == Vector4f.class) {
                int remainingComponentWidth = remainingWidth / 4;
                List<Object> vectors = new ArrayList<Object>();
                String[] names = { "x", "y", "z", "w" };

                for (IComponent icomponent : icomponents)
                    vectors.add(field.get(icomponent));

                for (int i = 0; i < 4; i++) {
                    PropertyTextField textField = new PropertyTextField(new FieldWrapper(Vector4f.class.getField(names[i])), vectors) {

                        private static final long serialVersionUID = -795014457507193137L;

                        @Override
                        public Object convert(String value) {
                            return parseFloat(value);
                        }

                    };
                    textField.setBounds(x, y, remainingComponentWidth - 5, 23);
                    x += remainingComponentWidth;
                    components.add(textField);
                }
            } else if (field.getType() == int.class) {
                PropertyTextField textField = new PropertyTextField(field, new ArrayList<Object>(icomponents)) {

                    private static final long serialVersionUID = -8267851313266217719L;

                    @Override
                    public Object convert(String value) {
                        return parseInt(value);
                    }

                };

                textField.setBounds(x, y, remainingWidth - 5, 23);
                components.add(textField);
            } else if (field.getType() == float.class) {
                PropertyTextField textField = new PropertyTextField(field, new ArrayList<Object>(icomponents)) {

                    private static final long serialVersionUID = -2561233497546796876L;

                    @Override
                    public Object convert(String value) {
                        return parseFloat(value);
                    }

                };

                textField.setBounds(x, y, remainingWidth - 5, 23);
                components.add(textField);
            } else if (field.getType() == boolean.class) {
                PropertyCheckbox checkbox = new PropertyCheckbox(field, new ArrayList<Object>(icomponents));

                checkbox.setBounds(x, y, remainingWidth - 5, 23);
                components.add(checkbox);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        height.set(23);

        return components;
    }

    public static void refresh(Component component) {
        if (component instanceof Container)
            for (Component comp : ((Container) component).getComponents())
                refresh(comp);

        if (component instanceof PropertyTextField)
            ((PropertyTextField) component).update();
        else if (component instanceof PropertyDropdown)
            ((PropertyDropdown) component).update();
        else if (component instanceof PropertyCheckbox)
            ((PropertyCheckbox) component).update();
    }

    private static String getValue(List<Entity> entities, String key) {
        if (entities.size() == 0)
            return null;

        String value = entities.get(0).getValue(key);
        for (Entity entity : entities)
            if (ObjectUtils.compare(value, entity.getValue(key)))
                value = null;

        return value;
    }

}
