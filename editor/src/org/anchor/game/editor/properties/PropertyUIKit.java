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
import org.anchor.engine.common.utils.Pointer;
import org.anchor.engine.common.utils.StringUtils;
import org.anchor.engine.shared.components.ScriptComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.entity.IComponent;
import org.anchor.engine.shared.ui.UIKit;
import org.anchor.engine.shared.utils.Property;
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

    public static JPanel createPanel(Entity entity, IComponent component, Pointer<Integer> height) {
        JPanel panel = new JPanel();
        panel.setLayout(null);

        int y = 0;
        int last = 0;

        for (Field field : component.getClass().getFields()) {
            Property property = field.getAnnotation(Property.class);
            if (property != null) {
                for (Component swingComponenet : createComponents(entity, component, new FieldWrapper(field), property.value(), height, y))
                    panel.add(swingComponenet);

                last = height.get() + 5;
                y += last;
            }
        }
        if (component instanceof ScriptComponent) {
            for (ScriptProperty property : ((ScriptComponent) component).script.getProperties()) {
                for (Component swingComponenet : createComponents(entity, component, new ScriptField(property), property.getName(), height, y))
                    panel.add(swingComponenet);

                last = height.get() + 5;
                y += last;
            }
        }

        for (Method method : component.getClass().getMethods()) {
            Property property = method.getAnnotation(Property.class);
            if (property != null) {
                JButton button = new JButton(property.value());
                button.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        try {
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

    private static List<Component> createComponents(Entity entity, IComponent icomponent, JavaField field, String name, Pointer<Integer> height, int y) {
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
                JButton button = AssetPicker.create(entity.getValue("model"), "obj", new FileCallback() {

                    @Override
                    public void run(File file) {
                        String path = FileHelper.removeFileExtension(FileHelper.localFileName(file));
                        entity.setValue("model", path);

                        Undo.fieldSet(field, icomponent, AssetLoader.loadModel(path));
                    }

                });
                button.setBounds(x, y, remainingWidth, 24);
                components.add(button);
            } else if (field.getType() == Material.class) {
                JButton button = AssetPicker.create(entity.getValue("material"), "aem", new FileCallback() {

                    @Override
                    public void run(File file) {
                        String path = FileHelper.removeFileExtension(FileHelper.localFileName(file));
                        entity.setValue("material", path);

                        Undo.fieldSet(field, icomponent, AssetLoader.loadMaterial(path));
                    }

                });
                button.setBounds(x, y, remainingWidth, 24);
                components.add(button);
            } else if (field.getType() == Script.class) {
                JButton button = AssetPicker.create(entity.getValue("script"), "js", new FileCallback() {

                    @Override
                    public void run(File file) {
                        String path = FileHelper.removeFileExtension(FileHelper.localFileName(file));
                        entity.setValue("script", path);

                        Undo.fieldSet(field, icomponent, ScriptComponent.load((ScriptComponent) icomponent, path));
                        LevelEditor.getInstance().reloadEntityComponents(LevelEditor.getInstance().getSelectedEntity());
                    }

                });
                button.setBounds(x, y, remainingWidth, 24);
                components.add(button);
            } else if (field.getType() == ParticleTexture.class) {
                JButton button = AssetPicker.create(entity.getValue("particleTexture"), "aep", new FileCallback() {

                    @Override
                    public void run(File file) {
                        String path = FileHelper.removeFileExtension(FileHelper.localFileName(file));
                        entity.setValue("particleTexture", path);

                        Undo.fieldSet(field, icomponent, AssetLoader.loadParticle(path));
                    }

                });
                button.setBounds(x, y, remainingWidth, 24);
                components.add(button);
            } else if (field.getType().isEnum()) {
                List<String> names = new ArrayList<String>();
                for (Object object : field.getType().getEnumConstants())
                    names.add(StringUtils.upperCaseFirst(object.toString().toLowerCase()));

                PropertyDropdown dropdown = new PropertyDropdown(entity, field, icomponent, names) {

                    private static final long serialVersionUID = 7503855159428548350L;

                    @Override
                    public Object convert(String value) {
                        entity.setValue(StringUtils.lowerCaseFirst(field.getType().getSimpleName()), value);
                        LevelEditor.getInstance().updateList();

                        return EnumUtils.getEnumValue(field.getType().getEnumConstants(), value);
                    }

                    @Override
                    public void updateDropdown() {
                        try {
                            Enum<?> value = (Enum<?>) field.get(icomponent);
                            if (value == null)
                                setSelectedIndex(AssetManager.getIndex(this, entity.getValue(StringUtils.lowerCaseFirst(field.getType().getSimpleName()))));
                            else
                                setSelectedIndex(AssetManager.getIndex(this, value.name()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                };
                dropdown.update();
                dropdown.setBounds(x, y, remainingWidth, 23);
                components.add(dropdown);
            } else if (field.getType() == Sound.class) {
                PropertyDropdown dropdown = new PropertyDropdown(entity, field, icomponent, AssetManager.getModels()) {

                    private static final long serialVersionUID = 3559333278954472616L;

                    @Override
                    public Object convert(String value) {
                        entity.setValue("sound", value);
                        LevelEditor.getInstance().updateList();

                        return new Sound(value);
                    }

                    @Override
                    public void updateDropdown() {
                        try {
                            Model model = (Model) field.get(icomponent);
                            if (model == null)
                                setSelectedIndex(AssetManager.getIndex(this, entity.getValue("sound")));
                            else
                                setSelectedIndex(AssetManager.getIndex(this, model.getName()));
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
                Vector2f vector = (Vector2f) field.get(icomponent);
                String[] names = { "x", "y" };

                for (int i = 0; i < 2; i++) {
                    PropertyTextField textField = new PropertyTextField(new FieldWrapper(Vector2f.class.getField(names[i])), vector) {

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
            } else if (field.getType() == Vector3f.class) {
                int remainingComponentWidth = remainingWidth / 3;
                Vector3f vector = (Vector3f) field.get(icomponent);
                String[] names = { "x", "y", "z" };

                for (int i = 0; i < 3; i++) {
                    PropertyTextField textField = new PropertyTextField(new FieldWrapper(Vector3f.class.getField(names[i])), vector) {

                        private static final long serialVersionUID = -253402699823299470L;

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
                Vector4f vector = (Vector4f) field.get(icomponent);
                String[] names = { "x", "y", "z", "w" };

                for (int i = 0; i < 4; i++) {
                    PropertyTextField textField = new PropertyTextField(new FieldWrapper(Vector4f.class.getField(names[i])), vector) {

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
                PropertyTextField textField = new PropertyTextField(field, icomponent) {

                    private static final long serialVersionUID = -8267851313266217719L;

                    @Override
                    public Object convert(String value) {
                        return parseInt(value);
                    }

                };

                textField.setBounds(x, y, remainingWidth - 5, 23);
                components.add(textField);
            } else if (field.getType() == float.class) {
                PropertyTextField textField = new PropertyTextField(field, icomponent) {

                    private static final long serialVersionUID = -2561233497546796876L;

                    @Override
                    public Object convert(String value) {
                        return parseFloat(value);
                    }

                };

                textField.setBounds(x, y, remainingWidth - 5, 23);
                components.add(textField);
            } else if (field.getType() == boolean.class) {
                PropertyCheckbox checkbox = new PropertyCheckbox(field, icomponent);

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

}
