/*
 * $Id: StackedBox.java.txt,v 1.1 2006/03/09 20:48:47 rbair Exp $
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.jdesktop.swingx;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.ui.WCTToolBar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

/**
 * Stacks components vertically in boxes. Each box is created with a title and a
 * component.<br>
 * 
 * <p>
 * The <code>StackedBox</code> can be added to a
 * {@link javax.swing.JScrollPane}.
 * 
 * <p>
 * Note: this class is not part of the SwingX core classes. It is just an
 * example of what can be achieved with the components.
 * 
 * Steve Ansari:  Added remove capability (depends on RiverLayout and cancel button icon)
 *                Added box added/removed listeners
 * 
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 */
public class StackedBox extends JPanel implements Scrollable {

    private Color titleBackgroundColor;
    private Color titleForegroundColor;
    private Color separatorColor;
    private Border separatorBorder;

    private static final Icon cancelIcon = new ImageIcon(WCTToolBar.class.getResource("/icons/dialog-cancel.png"));

    private ArrayList<BoxListener> listeners = new ArrayList<BoxListener>();
    
    private ArrayList<String> boxNameList = new ArrayList<String>();
    
    public StackedBox() {
        setLayout(new VerticalLayout());
        setOpaque(true);
        setBackground(Color.WHITE);

        separatorBorder = new SeparatorBorder();
        setTitleForegroundColor(Color.BLACK);
        setTitleBackgroundColor(new Color(248, 248, 248));
        setSeparatorColor(new Color(214, 223, 247));
    }

    public Color getSeparatorColor() {
        return separatorColor;
    }

    public void setSeparatorColor(Color separatorColor) {
        this.separatorColor = separatorColor;
    }

    public Color getTitleForegroundColor() {
        return titleForegroundColor;
    }

    public void setTitleForegroundColor(Color titleForegroundColor) {
        this.titleForegroundColor = titleForegroundColor;
    }

    public Color getTitleBackgroundColor() {
        return titleBackgroundColor;
    }

    public void setTitleBackgroundColor(Color titleBackgroundColor) {
        this.titleBackgroundColor = titleBackgroundColor;
    }

    /**
     * Adds a new component to this <code>StackedBox</code>
     * 
     * @param title
     * @param component
     */
    public void addBox(final String title, final Component component) {
        addBox(title, component, false);
    }
    
    /**
     * Adds a new component to this <code>StackedBox</code>
     * 
     * @param title
     * @param component
     * @param isRemovable - add a remove button to remove this box?
     */
    public void addBox(final String title, final Component component, final boolean isRemovable) {
        addBox(title, component, isRemovable, -1);
    }

    /**
     * Adds a new component to this <code>StackedBox</code>
     * 
     * @param title
     * @param component
     * @param isRemovable - add a remove button to remove this box?
     * @param index - index to insert the box into.
     */
    public void addBox(final String title, final Component component, final boolean isRemovable, final int index) {
    	addBox(title, component, isRemovable, index, false);
    }

    
    /**
     * Adds a new component to this <code>StackedBox</code>
     * 
     * @param title
     * @param component
     * @param isRemovable - add a remove button to remove this box?
     * @param index - index to insert the box into. 
     * @param isCollapsed - is the box collapsed?
     */
    public void addBox(final String title, final Component component, final boolean isRemovable, final int index, final boolean isCollapsed) {


        final JXCollapsiblePane collapsible = new JXCollapsiblePane();
//        collapsible.getContentPane().setBackground(Color.WHITE);
        collapsible.add(component);
//        collapsible.setBorder(new CompoundBorder(separatorBorder, collapsible.getBorder()));

        
//        collapsible.getContentPane().setBackground(getTitleBackgroundColor().darker());

        

        collapsible.setCollapsed(isCollapsed);

        Action toggleAction = collapsible.getActionMap().get(
                JXCollapsiblePane.TOGGLE_ACTION);
        // use the collapse/expand icons from the JTree UI
        toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON, UIManager
                .getIcon("Tree.expandedIcon"));
        toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON, UIManager
                .getIcon("Tree.collapsedIcon"));

        final JXHyperlink link = new JXHyperlink(toggleAction);
        link.setText(title);
        link.setFont(link.getFont().deriveFont(Font.BOLD));
        link.setOpaque(true);
        link.setBackground(getTitleBackgroundColor());
        link.setFocusPainted(false);

        link.setUnclickedColor(getTitleForegroundColor());
        link.setClickedColor(getTitleForegroundColor());

        link.setBorder(new CompoundBorder(separatorBorder, BorderFactory
                .createEmptyBorder(2, 4, 2, 4)));
        link.setBorderPainted(true);

        final JPanel boxPanel = new JPanel(new VerticalLayout());
        
        if (isRemovable) {
            final JPanel linkPanel = new JPanel(new RiverLayout(0, 0));
            JButton removeButton = new JButton(cancelIcon);
            removeButton.setPreferredSize(new Dimension(22, 22));
            removeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
//                    remove(linkPanel);
//                    remove(collapsible);
                    remove(boxPanel);
                    validate();
                    repaint();
                    BoxEvent event = new BoxEvent();
                    event.setTitle(link.getText());
                    for (BoxListener l : listeners) {
                        l.removedBox(event);
                    }
                    getBoxNameList().remove(title);
                }
            });
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(removeButton);
            linkPanel.add(link, "left hfill");
            linkPanel.add(buttonPanel, "vcenter right vfill");
            
//            add(linkPanel);
            boxPanel.add(linkPanel);
        }
        else {        
//            add(link);
            boxPanel.add(link);
        }
        
//        add(collapsible);
        boxPanel.add(collapsible);
        
        if (index == -1) {
            add(boxPanel);
            getBoxNameList().add(title);
        }
        else {
            add(boxPanel, index);
            getBoxNameList().add(index, title);
        }
        
        BoxEvent event = new BoxEvent();
        event.setTitle(link.getText());
        for (BoxListener l : listeners) {
            l.addedBox(event);
        }

    }
    
    public void addDivider() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLUE));
        add(panel);
    }

    /**
     * @see Scrollable#getPreferredScrollableViewportSize()
     */
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    /**
     * @see Scrollable#getScrollableBlockIncrement(java.awt.Rectangle, int, int)
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect,
            int orientation, int direction) {
        return 10;
    }

    /**
     * @see Scrollable#getScrollableTracksViewportHeight()
     */
    public boolean getScrollableTracksViewportHeight() {
        if (getParent() instanceof JViewport) {
            return (((JViewport)getParent()).getHeight() > getPreferredSize().height);
        } else {
            return false;
        }
    }

    /**
     * @see Scrollable#getScrollableTracksViewportWidth()
     */
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    /**
     * @see Scrollable#getScrollableUnitIncrement(java.awt.Rectangle, int, int)
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation,
            int direction) {
        return 10;
    }

    /**
     * The border between the stack components. It separates each component with a
     * fine line border.
     */
    class SeparatorBorder implements Border {

        boolean isFirst(Component c) {
            return c.getParent() == null || c.getParent().getComponent(0) == c;
        }

        public Insets getBorderInsets(Component c) {
            // if the collapsible is collapsed, we do not want its border to be
            // painted.
            if (c instanceof JXCollapsiblePane) {
                if (((JXCollapsiblePane)c).isCollapsed()) { return new Insets(0, 0, 0,
                        0); }
            }
            return new Insets(isFirst(c)?4:1, 0, 1, 0);
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width,
                int height) {
            g.setColor(getSeparatorColor());
            if (isFirst(c)) {
                g.drawLine(x, y + 2, x + width, y + 2);
            }
            g.drawLine(x, y + height - 1, x + width, y + height - 1);
        }
    }

    
    public void moveBoxUp(int index) {
        Component comp = getComponent(index);
        remove(index);
        add(comp, index-1);
        
        String boxName = getBoxNameList().get(index);
        getBoxNameList().remove(index);
        getBoxNameList().add(index-1, boxName);
        
//        validateTree();
        validate();
    }
    
    public void moveBoxDown(int index) {
        Component comp = getComponent(index);
        remove(index);
        add(comp, index+1);
        
        String boxName = getBoxNameList().get(index);
        getBoxNameList().remove(index);
        getBoxNameList().add(index+1, boxName);
        
//        validateTree();
        validate();
    }
    
    public int getIndexOf(String boxTitle) {
        return getBoxNameList().indexOf(boxTitle);
    }
    
    
    
    
    
    public void addBoxListener(BoxListener l) {
        listeners.add(l);
    }
    
    public void removeBoxListener(BoxListener l) {
        listeners.remove(l);
    }
    
    
    
    public List<String> getBoxNameList() {
        return boxNameList;
    }

    
    
    public interface BoxListener {
        public void addedBox(BoxEvent e);
        public void removedBox(BoxEvent e);
    }
    
    public class BoxEvent {
        private String title;

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }
}

