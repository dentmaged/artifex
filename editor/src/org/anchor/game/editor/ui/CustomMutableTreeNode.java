package org.anchor.game.editor.ui;

import javax.swing.tree.DefaultMutableTreeNode;

public class CustomMutableTreeNode extends DefaultMutableTreeNode {

    private static final long serialVersionUID = 1391633944775476548L;

    private Object storage;

    public CustomMutableTreeNode(Object object) {
        super(object);
    }

    public void setStorage(Object storage) {
        this.storage = storage;
    }

    public Object getStorage() {
        return storage;
    }

}
