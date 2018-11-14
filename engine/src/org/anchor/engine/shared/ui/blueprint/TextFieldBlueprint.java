package org.anchor.engine.shared.ui.blueprint;

import java.awt.Component;
import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.anchor.engine.shared.ui.listener.TextFieldListener;
import org.anchor.engine.shared.ui.listener.adapter.TextFieldAdapter;
import org.anchor.engine.shared.ui.swing.CustomTextField;

public class TextFieldBlueprint extends UIBlueprint {

    protected String name;
    protected CustomTextField textField;

    public TextFieldBlueprint(String name, TextFieldListener listener) {
        this(name, "", listener);
    }

    public TextFieldBlueprint(String name, String text) {
        this(name, text, new TextFieldAdapter());
    }

    public TextFieldBlueprint(String name, String text, TextFieldListener listener) {
        super(listener);
        this.name = name;

        textField = new CustomTextField(listener);
        textField.setText(text);

        if (listener != null)
            listener.setComponent(textField);
    }

    public String getName() {
        return name;
    }

    public void setListener(TextFieldListener listener) {
        this.listener = listener;
        this.textField.setListener(listener);
        this.listener.setComponent(textField);
    }

    @Override
    public List<Component> build(int x, int y, int width) {
        JLabel label = new JLabel(name);
        label.setToolTipText(name);
        label.setLabelFor(textField);
        int w = width / 2 - 30;
        label.setBounds(x, y + 4, w, 14);

        textField.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {
                if (textField.isEnabled())
                    ((TextFieldListener) listener).onTextFieldEdit(textField);
            }

            public void removeUpdate(DocumentEvent e) {
                if (textField.isEnabled())
                    ((TextFieldListener) listener).onTextFieldEdit(textField);
            }

            public void insertUpdate(DocumentEvent e) {
                if (textField.isEnabled())
                    ((TextFieldListener) listener).onTextFieldEdit(textField);
            }

        });
        textField.setBounds(x + w + 5, y, width - w - 20, 23);

        return Arrays.asList(label, textField);
    }

    @Override
    public int getHeight() {
        return 23;
    }

    public CustomTextField getTextField() {
        return textField;
    }

    public String getText() {
        return textField.getText();
    }

}
