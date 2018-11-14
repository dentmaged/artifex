package org.anchor.game.editor.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.anchor.game.editor.utils.Filter;

public class FileSystemModel implements TreeModel {

    private File root;
    private Vector<TreeModelListener> listeners;

    protected Filter filter;

    public FileSystemModel(File root) {
        this(root, new Filter() {

            @Override
            public boolean allow(File file) {
                if (file.isDirectory())
                    return true;

                return file.getName().toLowerCase().endsWith(".png") || file.getName().toLowerCase().endsWith(".obj") || file.getName().toLowerCase().endsWith(".pcl") || file.getName().toLowerCase().endsWith(".aem");
            }

        });
    }

    public FileSystemModel(File root, Filter filter) {
        this.root = root.getAbsoluteFile();
        this.listeners = new Vector<TreeModelListener>();
        this.filter = filter;
    }

    private CustomMutableTreeNode createFromFile(File file) {
        CustomMutableTreeNode node = new CustomMutableTreeNode(file.getName());
        node.setStorage(file);

        return node;
    }

    private List<File> listFiles(File parent) {
        List<File> files = new ArrayList<File>();
        for (File file : parent.listFiles())
            if (file.isHidden() || file.getName().startsWith(".") || !filter.allow(file))
                continue;
            else
                files.add(file);

        return files;
    }

    @Override
    public Object getRoot() {
        CustomMutableTreeNode node = createFromFile(root);
        node.setUserObject(root.getName());

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
