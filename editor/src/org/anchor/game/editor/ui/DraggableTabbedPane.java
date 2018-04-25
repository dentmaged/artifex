package org.anchor.game.editor.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

public class DraggableTabbedPane extends JTabbedPane {

    private static final long serialVersionUID = 7520861466989967060L;

    private static final int LINE_WIDTH = 3;
    private static final String NAME = "TabTransferData";

    private final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);
    private static GhostGlassPane glassPane = new GhostGlassPane();

    private boolean isDrawRect = false;
    private final Rectangle2D lineRect = new Rectangle2D.Double();

    private final Color lineColor = new Color(0, 100, 255);
    private TabAcceptor acceptor = null;
    private boolean hasGhost = true;

    public DraggableTabbedPane() {
        this(TOP);
    }

    public DraggableTabbedPane(int position) {
        super(position);
        DragSourceListener dsl = new DragSourceListener() {

            @Override
            public void dragEnter(DragSourceDragEvent e) {
                e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
            }

            @Override
            public void dragExit(DragSourceEvent e) {
                e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
                lineRect.setRect(0, 0, 0, 0);

                isDrawRect = false;

                glassPane.setPoint(new Point(-1000, -1000));
                glassPane.repaint();
            }

            @Override
            public void dragOver(DragSourceDragEvent e) {
                TabTransferData data = getTabTransferData(e);
                if (data == null) {
                    e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);

                    return;
                }

                e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
            }

            @Override
            public void dragDropEnd(DragSourceDropEvent e) {
                isDrawRect = false;
                lineRect.setRect(0, 0, 0, 0);

                if (hasGhost()) {
                    glassPane.setVisible(false);
                    glassPane.setImage(null);
                }
            }

            @Override
            public void dropActionChanged(DragSourceDragEvent e) {

            }

        };

        DragGestureListener dgl = new DragGestureListener() {

            @Override
            public void dragGestureRecognized(DragGestureEvent e) {
                Point tabPt = e.getDragOrigin();
                int dragTabIndex = indexAtLocation(tabPt.x, tabPt.y);

                if (dragTabIndex < 0)
                    return;
                initGlassPane(e.getComponent(), e.getDragOrigin(), dragTabIndex);

                try {
                    e.startDrag(DragSource.DefaultMoveDrop, new TabTransferable(DraggableTabbedPane.this, dragTabIndex), dsl);
                } catch (InvalidDnDOperationException ex) {
                    ex.printStackTrace();
                }
            }

        };

        new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new CDropTargetListener(), true);
        new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, dgl);

        acceptor = new TabAcceptor() {

            @Override
            public boolean isDropAcceptable(DraggableTabbedPane component, int index) {
                return true;
            }

        };
    }

    public TabAcceptor getTabAcceptor() {
        return acceptor;
    }

    public void setTabAcceptor(TabAcceptor acceptor) {
        this.acceptor = acceptor;
    }

    private TabTransferData getTabTransferData(DropTargetDropEvent event) {
        try {
            return (TabTransferData) event.getTransferable().getTransferData(FLAVOR);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private TabTransferData getTabTransferData(DropTargetDragEvent event) {
        try {
            return (TabTransferData) event.getTransferable().getTransferData(FLAVOR);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private TabTransferData getTabTransferData(DragSourceDragEvent event) {
        try {
            return (TabTransferData) event.getDragSourceContext().getTransferable().getTransferData(FLAVOR);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    class TabTransferable implements Transferable {

        private TabTransferData data = null;

        public TabTransferable(DraggableTabbedPane tabbedPane, int tabIndex) {
            data = new TabTransferData(DraggableTabbedPane.this, tabIndex);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) {
            return data;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            DataFlavor[] f = new DataFlavor[1];
            f[0] = FLAVOR;

            return f;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.getHumanPresentableName().equals(NAME);
        }

    }

    class TabTransferData {

        private DraggableTabbedPane tabbedPane;
        private int tabIndex;

        public TabTransferData() {
            this(null, -1);
        }

        public TabTransferData(DraggableTabbedPane tabbedPane, int tabIndex) {
            this.tabbedPane = tabbedPane;
            this.tabIndex = tabIndex;
        }

        public DraggableTabbedPane getTabbedPane() {
            return tabbedPane;
        }

        public void setTabbedPane(DraggableTabbedPane pane) {
            this.tabbedPane = pane;
        }

        public int getTabIndex() {
            return tabIndex;
        }

        public void setTabIndex(int index) {
            this.tabIndex = index;
        }

    }

    private Point buildGhostLocation(Point location) {
        Point retval = new Point(location);

        switch (getTabPlacement()) {
        case JTabbedPane.TOP: {
            retval.y = 1;
            retval.x -= glassPane.getGhostWidth() / 2;
        }
            break;

        case JTabbedPane.BOTTOM: {
            retval.y = getHeight() - 1 - glassPane.getGhostHeight();
            retval.x -= glassPane.getGhostWidth() / 2;
        }
            break;

        case JTabbedPane.LEFT: {
            retval.x = 1;
            retval.y -= glassPane.getGhostHeight() / 2;
        }
            break;

        case JTabbedPane.RIGHT: {
            retval.x = getWidth() - 1 - glassPane.getGhostWidth();
            retval.y -= glassPane.getGhostHeight() / 2;
        }
            break;
        }// switch

        retval = SwingUtilities.convertPoint(DraggableTabbedPane.this, retval, glassPane);
        return retval;
    }

    class CDropTargetListener implements DropTargetListener {

        @Override
        public void dragEnter(DropTargetDragEvent e) {
            if (isDragAcceptable(e))
                e.acceptDrag(e.getDropAction());
            else
                e.rejectDrag();
        }

        @Override
        public void dragExit(DropTargetEvent e) {
            isDrawRect = false;
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent e) {

        }

        @Override
        public void dragOver(final DropTargetDragEvent e) {
            TabTransferData data = getTabTransferData(e);

            if (getTabPlacement() == JTabbedPane.TOP || getTabPlacement() == JTabbedPane.BOTTOM)
                initTargetLeftRightLine(getTargetTabIndex(e.getLocation()), data);
            else
                initTargetTopBottomLine(getTargetTabIndex(e.getLocation()), data);

            repaint();
            if (hasGhost()) {
                glassPane.setPoint(buildGhostLocation(e.getLocation()));
                glassPane.repaint();
            }
        }

        @Override
        public void drop(DropTargetDropEvent event) {
            if (isDropAcceptable(event)) {
                convertTab(getTabTransferData(event), getTargetTabIndex(event.getLocation()));
                event.dropComplete(true);
            } else {
                event.dropComplete(false);
            }

            isDrawRect = false;
            repaint();
        }

        public boolean isDragAcceptable(DropTargetDragEvent e) {
            Transferable transferable = e.getTransferable();
            if (transferable == null)
                return false;

            DataFlavor[] flavor = e.getCurrentDataFlavors();
            if (!transferable.isDataFlavorSupported(flavor[0]))
                return false;

            TabTransferData data = getTabTransferData(e);
            if (DraggableTabbedPane.this == data.getTabbedPane() && data.getTabIndex() >= 0)
                return true;

            if (DraggableTabbedPane.this != data.getTabbedPane())
                if (acceptor != null)
                    return acceptor.isDropAcceptable(data.getTabbedPane(), data.getTabIndex());

            return false;
        }

        public boolean isDropAcceptable(DropTargetDropEvent e) {
            Transferable transferable = e.getTransferable();
            if (transferable == null)
                return false;

            DataFlavor[] flavor = e.getCurrentDataFlavors();
            if (!transferable.isDataFlavorSupported(flavor[0]))
                return false;

            TabTransferData data = getTabTransferData(e);
            if (DraggableTabbedPane.this == data.getTabbedPane() && data.getTabIndex() >= 0)
                return true;

            if (DraggableTabbedPane.this != data.getTabbedPane())
                if (acceptor != null)
                    return acceptor.isDropAcceptable(data.getTabbedPane(), data.getTabIndex());

            return false;
        }

    }

    public void setPaintGhost(boolean hasGhost) {
        this.hasGhost = hasGhost;
    }

    public boolean hasGhost() {
        return hasGhost;
    }

    private int getTargetTabIndex(Point point) {
        boolean isTopOrBottom = getTabPlacement() == JTabbedPane.TOP || getTabPlacement() == JTabbedPane.BOTTOM;

        if (getTabCount() == 0)
            return 0;

        for (int i = 0; i < getTabCount(); i++) {
            Rectangle r = getBoundsAt(i);

            if (isTopOrBottom)
                r.setRect(r.x - r.width / 2, r.y, r.width, r.height);
            else
                r.setRect(r.x, r.y - r.height / 2, r.width, r.height);

            if (r.contains(point))
                return i;
        }

        Rectangle r = getBoundsAt(getTabCount() - 1);
        if (isTopOrBottom) {
            int x = r.x + r.width / 2;
            r.setRect(x, r.y, getWidth() - x, r.height);
        } else {
            int y = r.y + r.height / 2;
            r.setRect(r.x, y, r.width, getHeight() - y);
        }

        return r.contains(point) ? getTabCount() : -1;
    }

    private void convertTab(TabTransferData data, int targetIndex) {
        DraggableTabbedPane source = data.getTabbedPane();
        int sourceIndex = data.getTabIndex();

        if (sourceIndex < 0)
            return;
        Component cmp = source.getComponentAt(sourceIndex);
        String str = source.getTitleAt(sourceIndex);

        if (source != this) {
            source.remove(sourceIndex);

            if (targetIndex == getTabCount()) {
                addTab(str, cmp);
            } else {
                if (targetIndex < 0)
                    targetIndex = 0;

                insertTab(str, null, cmp, null, targetIndex);

            }

            setSelectedComponent(cmp);
        } else {
            if (targetIndex < 0 || sourceIndex == targetIndex)
                return;

            if (targetIndex == getTabCount()) {
                source.remove(sourceIndex);
                addTab(str, cmp);

                setSelectedIndex(getTabCount() - 1);
            } else if (sourceIndex > targetIndex) {
                source.remove(sourceIndex);
                insertTab(str, null, cmp, null, targetIndex);

                setSelectedIndex(targetIndex);
            } else {
                source.remove(sourceIndex);
                insertTab(str, null, cmp, null, targetIndex - 1);

                setSelectedIndex(targetIndex - 1);
            }
        }
    }

    private void initTargetLeftRightLine(int next, TabTransferData data) {
        if (next < 0) {
            lineRect.setRect(0, 0, 0, 0);
            isDrawRect = false;
            return;
        }

        if ((data.getTabbedPane() == this) && (data.getTabIndex() == next || next - data.getTabIndex() == 1)) {
            lineRect.setRect(0, 0, 0, 0);
            isDrawRect = false;
        } else if (getTabCount() == 0) {
            lineRect.setRect(0, 0, 0, 0);
            isDrawRect = false;
            return;
        } else if (next == 0) {
            Rectangle rect = getBoundsAt(0);
            lineRect.setRect(-LINE_WIDTH / 2, rect.y, LINE_WIDTH, rect.height);
            isDrawRect = true;
        } else if (next == getTabCount()) {
            Rectangle rect = getBoundsAt(getTabCount() - 1);
            lineRect.setRect(rect.x + rect.width - LINE_WIDTH / 2, rect.y, LINE_WIDTH, rect.height);
            isDrawRect = true;
        } else {
            Rectangle rect = getBoundsAt(next - 1);
            lineRect.setRect(rect.x + rect.width - LINE_WIDTH / 2, rect.y, LINE_WIDTH, rect.height);
            isDrawRect = true;
        }
    }

    private void initTargetTopBottomLine(int next, TabTransferData data) {
        if (next < 0) {
            lineRect.setRect(0, 0, 0, 0);
            isDrawRect = false;
            return;
        }

        if ((data.getTabbedPane() == this) && (data.getTabIndex() == next || next - data.getTabIndex() == 1)) {
            lineRect.setRect(0, 0, 0, 0);
            isDrawRect = false;
        } else if (getTabCount() == 0) {
            lineRect.setRect(0, 0, 0, 0);
            isDrawRect = false;
            return;
        } else if (next == getTabCount()) {
            Rectangle rect = getBoundsAt(getTabCount() - 1);
            lineRect.setRect(rect.x, rect.y + rect.height - LINE_WIDTH / 2, rect.width, LINE_WIDTH);
            isDrawRect = true;
        } else if (next == 0) {
            Rectangle rect = getBoundsAt(0);
            lineRect.setRect(rect.x, -LINE_WIDTH / 2, rect.width, LINE_WIDTH);
            isDrawRect = true;
        } else {
            Rectangle rect = getBoundsAt(next - 1);
            lineRect.setRect(rect.x, rect.y + rect.height - LINE_WIDTH / 2, rect.width, LINE_WIDTH);
            isDrawRect = true;
        }
    }

    private void initGlassPane(Component c, Point tabPt, int tabIndex) {
        getRootPane().setGlassPane(glassPane);

        if (hasGhost()) {
            Rectangle rect = getBoundsAt(tabIndex);
            BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);

            Graphics g = image.getGraphics();
            c.paint(g);

            image = image.getSubimage(rect.x, rect.y, rect.width, rect.height);
            glassPane.setImage(image);
        }

        glassPane.setPoint(buildGhostLocation(tabPt));
        glassPane.setVisible(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (isDrawRect) {
            Graphics2D graphics = (Graphics2D) g;

            graphics.setPaint(lineColor);
            graphics.fill(lineRect);
        }
    }

    public interface TabAcceptor {

        boolean isDropAcceptable(DraggableTabbedPane component, int index);

    }

}

class GhostGlassPane extends JPanel {

    private AlphaComposite composite;
    private Point location = new Point(0, 0);
    private BufferedImage draggingGhost = null;

    private static final long serialVersionUID = 987815915985529085L;

    public GhostGlassPane() {
        setOpaque(false);
        composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
    }

    public void setImage(BufferedImage draggingGhost) {
        this.draggingGhost = draggingGhost;
    }

    public void setPoint(Point location) {
        this.location.x = location.x;
        this.location.y = location.y;
    }

    public int getGhostWidth() {
        if (draggingGhost == null)
            return 0;

        return draggingGhost.getWidth(this);
    }

    public int getGhostHeight() {
        if (draggingGhost == null)
            return 0;

        return draggingGhost.getHeight(this);
    }

    @Override
    public void paintComponent(Graphics g) {
        if (draggingGhost == null)
            return;
        Graphics2D graphics = (Graphics2D) g;

        graphics.setComposite(this.composite);
        graphics.drawImage(this.draggingGhost, (int) this.location.getX(), (int) this.location.getY(), null);
    }

}
