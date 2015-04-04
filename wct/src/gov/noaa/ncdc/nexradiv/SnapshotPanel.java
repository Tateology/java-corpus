package gov.noaa.ncdc.nexradiv;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.ui.WCTToolBar;
import gov.noaa.ncdc.wct.ui.WCTViewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.geotools.renderer.j2d.RenderedLayer;
import org.jdesktop.swingx.StackedBox;

public class SnapshotPanel extends JPanel {

    private WCTViewer viewer;
    private StackedBox dataLayersStack;
    private SnapshotLayer snapshotLayer;

    private static final Icon upIcon = new ImageIcon(WCTToolBar.class.getResource("/icons/go-up.png"));
    private static final Icon downIcon = new ImageIcon(WCTToolBar.class.getResource("/icons/go-down.png"));
    private static final Icon zoomToIcon = new ImageIcon(WCTToolBar.class.getResource("/icons/view-fullscreen.png"));


    final JComboBox jcomboTransparency = new JComboBox(new Object[] {
            " Default", "  0 %", " 10 %", " 20 %", " 30 %", " 40 %", " 50 %", 
            " 60 %", " 70 %", " 80 %", " 90 %", "100 %"
    });      



    public SnapshotPanel(WCTViewer viewer, StackedBox dataLayersStack, SnapshotLayer snapshotLayer) {
        super(new RiverLayout());
        this.viewer = viewer;
        this.dataLayersStack = dataLayersStack;
        this.snapshotLayer = snapshotLayer;

        createUI();
    }

    private void createUI() {

        final JCheckBox jcbVisible = new JCheckBox("Visible", true);
        jcbVisible.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                snapshotLayer.getRenderedGridCoverage().setVisible(jcbVisible.isSelected());
                viewer.fireRenderCompleteEvent();
            }
        });
        this.add(jcbVisible);


        jcomboTransparency.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                snapshotLayer.setAlpha(getTransparencyAlpha());
                viewer.fireRenderCompleteEvent();
            }
        });
        jcomboTransparency.setEditable(true);




        //        JPanel transPanel = new JPanel();
        //        transPanel.setLayout(new RiverLayout());
        //        transPanel.add(new JLabel("Transparency"), "center");
        //        transPanel.add(jcomboTransparency, "br center");
        //        this.add(transPanel, "tab");
        this.add(new JLabel("Transparency: "), "tab");
        this.add(jcomboTransparency);

        snapshotLayer.getRenderedGridCoverage().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
//                System.out.println(evt.getPropertyName() + "  | "+evt.getOldValue() + " to "+evt.getNewValue());
                if (evt.getPropertyName().equals("visible")) {
                    jcbVisible.setSelected(Boolean.valueOf(evt.getNewValue().toString()));
                }
            }

        });


        final JButton jbIsolate = new JButton("Isolate");
        jbIsolate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (SnapshotLayer s : viewer.getSnapshotLayers()) {
                    s.getRenderedGridCoverage().setVisible(false);
                }
                snapshotLayer.getRenderedGridCoverage().setVisible(true);
                viewer.fireRenderCompleteEvent();
            }
        });
        this.add(jbIsolate, "tab");


        final JButton jbZoom = new JButton(zoomToIcon);
        jbZoom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewer.setCurrentExtent(snapshotLayer.getRenderedGridCoverage().getGridCoverage().getEnvelope().toRectangle2D());
                viewer.fireRenderCompleteEvent();
            }            
        });
        this.add(jbZoom, "tab");


        final JButton jbMoveUp = new JButton(upIcon);
        jbMoveUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                RenderedLayer[] layers = viewer.getMapPane().getRenderer().getLayers();
                List<RenderedLayer> layerList = Arrays.asList(layers);

                int index = layerList.indexOf(snapshotLayer.getRenderedGridCoverage());
                int gridSatelliteIndex = layerList.indexOf(viewer.getGridSatelliteRenderedGridCoverage());
                if ( Math.abs(index-gridSatelliteIndex) == 1 ) {
                    System.out.println("next to grid/sat rgc - doing nothing");
                    return;
                }

                dataLayersStack.moveBoxUp(dataLayersStack.getIndexOf(snapshotLayer.getName()));

                for (RenderedLayer rl : viewer.getMapPane().getRenderer().getLayers()) {
                    System.out.println("before: "+rl.toString());
                }

                RenderedLayer switchLayer = layers[index+1];
                switchLayer.setZOrder(switchLayer.getZOrder()-0.001f);

                RenderedLayer mainLayer = snapshotLayer.getRenderedGridCoverage();
                mainLayer.setZOrder(mainLayer.getZOrder()+0.001f);

                layers[index] = switchLayer;
                layers[index+1] = mainLayer;

                for (RenderedLayer rl : viewer.getMapPane().getRenderer().getLayers()) {
                    System.out.println("after: "+rl.toString());
                }

                System.out.println("switching "+snapshotLayer.getRenderedGridCoverage()+"  with  "+switchLayer);
                
                viewer.fireRenderCompleteEvent();

            }
        });
        final JButton jbMoveDown = new JButton(downIcon);
        jbMoveDown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                RenderedLayer[] layers = viewer.getMapPane().getRenderer().getLayers();
                List<RenderedLayer> layerList = Arrays.asList(layers);

                int index = layerList.indexOf(snapshotLayer.getRenderedGridCoverage());
                int gridSatelliteIndex = layerList.indexOf(viewer.getGridSatelliteRenderedGridCoverage());
                if ( Math.abs(index-gridSatelliteIndex) == viewer.getSnapshotLayers().size() ) {
                    System.out.println("at bottom- doing nothing");
                    return;
                }


                dataLayersStack.moveBoxDown(dataLayersStack.getIndexOf(snapshotLayer.getName()));

                //                for (RenderedLayer rl : viewer.getMapPane().getRenderer().getLayers()) {
                //                    System.out.println("before: "+rl.toString());
                //                }


                RenderedLayer switchLayer = layers[index-1];
                switchLayer.setZOrder(switchLayer.getZOrder()+0.001f);

                RenderedLayer mainLayer = snapshotLayer.getRenderedGridCoverage();
                mainLayer.setZOrder(mainLayer.getZOrder()-0.001f);

                layers[index] = switchLayer;
                layers[index-1] = mainLayer;

                //                for (RenderedLayer rl : viewer.getMapPane().getRenderer().getLayers()) {
                //                    System.out.println("after: "+rl.toString());
                //                }

                System.out.println("switching "+snapshotLayer.getRenderedGridCoverage()+"  with  "+switchLayer);

                viewer.fireRenderCompleteEvent();
            }
        });
        this.add(jbMoveUp, "tab");
        this.add(jbMoveDown);

        
        JButton jbLegend = new JButton("Legend");
        jbLegend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JDialog legendDialog = new JDialog(viewer);
                legendDialog.getContentPane().setLayout(new RiverLayout());
                legendDialog.getContentPane().add(new JLabel(new ImageIcon(snapshotLayer.getLegendImage())));
                legendDialog.setTitle(snapshotLayer.getName());
                legendDialog.pack();
                legendDialog.setVisible(true);
            }
        });
        this.add(jbLegend, "tab");
                

    }

    public int getTransparencyAlpha() {
        if (jcomboTransparency.getSelectedItem().toString().trim().equalsIgnoreCase("Default")) {
            return -1;
        }
        else {
            int percent = Integer.parseInt(jcomboTransparency.getSelectedItem().toString().replaceAll("%", "").trim());
            int alpha = 255-(int)(percent*0.01*255);
            return alpha;
        }
    }
}
