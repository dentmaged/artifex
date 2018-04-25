package org.anchor.engine.shared.ui.listener;

import org.anchor.engine.shared.ui.swing.CustomDropdown;

public abstract class DropdownListener extends UIListener {

    public DropdownListener() {
        super();
    }

    public DropdownListener(boolean add) {
        super(add);
    }

    public abstract void onDropdownSelect(CustomDropdown field);

}
