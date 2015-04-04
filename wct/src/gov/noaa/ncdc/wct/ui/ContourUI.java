package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.ui.WCTViewer.RenderCompleteListener;
import gov.noaa.ncdc.wct.unidata.contour.ContourManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

public class ContourUI extends JDialog {

	private WCTViewer viewer = null;
	
	
	private JCheckBox jcbShowContours = new JCheckBox("Show Contours");
	private RenderCompleteListener rcl = new RenderCompleteListener() {
		@Override
		public void renderProgress(int progressPercent) {
		}
		@Override
		public void renderComplete() {
			try {
				ContourManager.contour(viewer);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	
	

	
	public ContourUI(WCTViewer viewer) {
		super(viewer, "Contour Editor", false);
		this.viewer = viewer;
		createGUI();
	}
	
	private void createGUI() {
		
		JPanel mainPanel = new JPanel(new RiverLayout());
		mainPanel.setBorder(WCTUiUtils.myTitledBorder("Contour Editor", 5));
		
		jcbShowContours.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				handleContours();
			}
		});
		
		mainPanel.add(jcbShowContours);
		
		this.add(mainPanel);
		
		
        JRootPane rootPane = this.getRootPane();
        InputMap iMap = rootPane.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");

        ActionMap aMap = rootPane.getActionMap();
        aMap.put("escape", new AbstractAction()
            {
                public void actionPerformed(ActionEvent e)
                {
                    dispose();
                }
            });

	}
	
	
	
	
	private void handleContours() {
	
		if (jcbShowContours.isSelected()) {
				
			try {
				ContourManager.contour(viewer);
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			} catch (InstantiationException e1) {
				e1.printStackTrace();
			}

			viewer.addRenderCompleteListener(rcl);
		}
		else {
			viewer.removeRenderCompleteListener(rcl);
			viewer.setContourGridCoverage(null);
			viewer.getContourRenderedGridCoverage().setVisible(false);
		}		

	}
	
}
