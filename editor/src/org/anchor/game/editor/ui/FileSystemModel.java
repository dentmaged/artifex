package org.anchor.game.editor.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class FileSystemModel implements TreeModel {

    private File root;
    private Vector<TreeModelListener> listeners;

    public FileSystemModel(File root) {
        this.root = root.getAbsoluteFile();
        listeners = new Vector<TreeModelListener>();
    }

    private CustomMutableTreeNode createFromFile(File file) {
        CustomMutableTreeNode node = new CustomMutableTreeNode(file.getName());
        node.setStorage(file);

        return node;
    }

    private List<File> listFiles(File parent) {
        List<File> files = new ArrayList<File>();
        for (File file : parent.listFiles())
            if (file.isHidden() || file.getName().startsWith(".") || !allowEnding(file))
                continue;
            else
                files.add(file);

        return files;
    }

    private boolean allowEnding(File file) {
        if (file.isDirectory())
            return true;

        String[] parts = file.getName().split(Pattern.quote("."));
        if (parts.length < 2)
            return false;

        return parts[1].equals("png") || parts[1].equals("obj");
    }

    @Override
    public Object getRoot() {
        CustomMutableTreeNode node = createFromFile(root);
        node.setUserObject("Asset Browser");

        return node;
    }

    @Override
    public Object getChild(Object parent, int i) {
        return createFromFile(listFiles(((File) (((CustomMutableTreeNode) parent).getStorage()))).get(i));
    }

    @Override
    public int getChildCount(Object parent) {
        CustomMutableTreeNode node = (CustomMutableTreeNode) parent;
        File file = (File) node.getStorage();
        if (file.isDirectory())
            return listFiles(file).size();

        return 0;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        CustomMutableTreeNode parentNode = (CustomMutableTreeNode) parent;
        CustomMutableTreeNode childNode = (CustomMutableTreeNode) child;

        List<File> children = listFiles(((File) parentNode.getStorage()));
        int result = -1;

        int i = 0;
        for (File sibling : children) {
            if (sibling == childNode.getStorage()) {
                result = i;

                break;
            }

            i++;
        }

        return result;
    }

    @Override
    public boolean isLeaf(Object node) {
        return ((File) ((CustomMutableTreeNode) node).getStorage()).isFile();
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        if (l != null && !listeners.contains(l))
            listeners.addElement(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        if (l != null)
            listeners.removeElement(l);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    public void fireTreeNodesInserted(TreeModelEvent e) {
        Enumeration<TreeModelListener> listenerCount = listeners.elements();
        while (listenerCount.hasMoreElements())
            ((TreeModelListener) listenerCount.nextElement()).treeNodesInserted(e);
    }

    public void fireTreeNodesRemoved(TreeModelEvent e) {
        Enumeration<TreeModelListener> listenerCount = listeners.elements();
        while (listenerCount.hasMoreElements())
            ((TreeModelListener) listenerCount.nextElement()).treeNodesRemoved(e);
    }

    public void fireTreeNodesChanged(TreeModelEvent e) {
        Enumeration<TreeModelListener> listenerCount = listeners.elements();
        while (listenerCount.hasMoreElements())
            ((TreeModelListener) listenerCount.nextElement()).treeNodesChanged(e);
    }

    public void fireTreeStructureChanged(TreeModelEvent e) {
        Enumeration<TreeModelListener> listenerCount = listeners.elements();
        while (listenerCount.hasMoreElements())
            ((TreeModelListener) listenerCount.nextElement()).treeStructureChanged(e);
    }

}
