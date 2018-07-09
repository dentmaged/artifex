package org.anchor.game.editor.properties;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.anchor.client.engine.renderer.types.Model;
import org.anchor.client.engine.renderer.types.texture.ModelTexture;
import org.anchor.engine.common.utils.EnumUtils;
import org.anchor.engine.common.utils.Pointer;
import org.anchor.engine.common.utils.StringUtils;
import org.anchor.engine.shared.components.IComponent;
import org.anchor.engine.shared.entity.Entity;
import org.anchor.engine.shared.ui.UIKit;
import org.anchor.engine.shared.utils.Property;
import org.anchor.game.client.loaders.AssetLoader;
import org.anchor.game.client.utils.Sound;
import org.anchor.game.editor.GameEditor;
import org.anchor.game.editor.utils.AssetManager;
import org.lwjgl.util.vector.Vector3f;

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
                for (Component swingComponenet : createComponents(entity, component, field, property.value(), height, y))
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
                y += 28;
            }
        }

        height.set(y + last - 25);

        return panel;
    }

    private static List<Component> createComponents(Entity entity, IComponent icomponent, Field field, String name, Pointer<Integer> height, int y) {
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
                PropertyDropdown dropdown = new PropertyDropdown(entity, field, icomponent, AssetManager.getModels()) {

                    private static final long serialVersionUID = 5436389935838674534L;

                    @Override
                    public Object convert(String value) {
                        entity.setValue("model", value);
                        GameEditor.getInstance().getLevelEditor().updateList();

                        return AssetLoader.loadModel(value);
                    }

                    @Override
                    public void update() {
                        try {
                            Model model = (Model) field.get(icomponent);
                            if (model == null)
                                setSelectedIndex(AssetManager.getIndex(this, entity.getValue("model")));
                            else
                                setSelectedIndex(AssetManager.getIndex(this, model.getName()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                };
                dropdown.update();
                dropdown.setBounds(x, y, remainingWidth, 23);
                components.add(dropdown);
            } else if (field.getType() == ModelTexture.class) {
                PropertyDropdown dropdown = new PropertyDropdown(entity, field, icomponent, AssetManager.getTextures()) {

                    private static final long serialVersionUID = -842084660378969213L;

                    @Override
                    public Object convert(String value) {
                        entity.setValue("texture", value);
                        GameEditor.getInstance().getLevelEditor().updateList();

                        return AssetLoader.loadDecalTexture(value);
                    }

                    @Override
                    public void update() {
                        try {
                            ModelTexture modelTexture = (ModelTexture) field.get(icomponent);
                            if (modelTexture == null)
                                setSelectedIndex(AssetManager.getIndex(this, entity.getValue("texture")));
                            else
                                setSelectedIndex(AssetManager.getIndex(this, modelTexture.getName()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                };
                dropdown.update();
                dropdown.setBounds(x, y, remainingWidth, 23);
                components.add(dropdown);
            } else if (field.getType().isEnum()) {
                List<String> names = new ArrayList<String>();
                for (Object object : field.getType().getEnumConstants())
                    names.add(StringUtils.upperCaseFirst(object.toString().toLowerCase()));

                PropertyDropdown dropdown = new PropertyDropdown(entity, field, icomponent, names) {

                    private static final long serialVersionUID = 7503855159428548350L;

                    @Override
                    public Object convert(String value) {
                        entity.setValue(StringUtils.lowerCaseFirst(field.getType().getSimpleName()), value);
                        GameEditor.getInstance().getLevelEditor().updateList();

                        return EnumUtils.getEnumValue(field.getType().getEnumConstants(), value);
                    }

                    @Override
                    public void update() {
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
                        GameEditor.getInstance().getLevelEditor().updateList();

                        return new Sound(value);
                    }

                    @Override
                    public void update() {
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
                dropdown.update();
                dropdown.setBounds(x, y, remainingWidth, 23);
                components.add(dropdown);
            } else if (field.getType() == Vector3f.class) {
                int remainingComponentWidth = remainingWidth / 3;
                Vector3f vector = (Vector3f) field.get(icomponent);
                String[] names = {
                        "x", "y", "z"
                };

                for (int i = 0; i < 3; i++) {
                    PropertyTextField textField = new PropertyTextField(Vector3f.class.getField(names[i]), vector) {

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
    }

}
