package org.anchor.engine.shared.ui.listener;

import org.anchor.engine.shared.ui.swing.CustomCheckbox;

public abstract class CheckboxListener extends UIListener {

    public CheckboxListener() {
        super();
    }

    public CheckboxListener(boolean add) {
        super(add);
    }

    public abstract void onCheckboxEdit(CustomCheckbox checkbox);

}
