package gov.noaa.ncdc.wct.ui;

import javax.swing.JFrame;

public class TestCdoServicesUI {

    /**
     * @param args
     */
    public static void main(String[] args) {

        JFrame frame = new JFrame();
        
        CdoServicesUI ui = new CdoServicesUI(frame);
       
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
      
        ui.setVisible(true);

    }

}
