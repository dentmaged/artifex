package org.anchor.engine.shared.ui.listener;

import org.anchor.engine.shared.ui.swing.CustomTextField;

public abstract class TextFieldListener extends UIListener {

    public TextFieldListener() {
        super();
    }

    public TextFieldListener(boolean add) {
        super(add);
    }

    public abstract void onTextFieldEdit(CustomTextField field);

}
