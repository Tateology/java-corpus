package gov.noaa.ncdc.wct.ui.event;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

public class MousePopupListener extends MouseAdapter {

    private Component invoker;
    private JPopupMenu menu;

    public MousePopupListener(Component invoker, JPopupMenu menu) {
        this.invoker = invoker;
        this.menu = menu;
    }




    public void mousePressed(MouseEvent e) {
//      System.out.println("PRESSED");
        checkPopup(e);
    }

    public void mouseClicked(MouseEvent e) {
//      System.out.println("CLICKED");
        checkPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
//      System.out.println("RELEASED");
        checkPopup(e);
    }

    private void checkPopup(MouseEvent e) {
//      if (e.isPopupTrigger()) {
//        menu.show(invoker, e.getX(), e.getY());
//      }

        if (! menu.isShowing()) {
            menu.show(invoker, invoker.getWidth(), 0);
        }
//        else {
//            System.out.println("SETTING setVisible to FALSE");
//            menu.setVisible(false);
//        }
    }
}

