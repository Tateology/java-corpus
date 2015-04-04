/**
 * NOAA's National Climatic Data Center
 * NOAA/NESDIS/NCDC
 * 151 Patton Ave, Asheville, NC  28801
 * 
 * THIS SOFTWARE AND ITS DOCUMENTATION ARE CONSIDERED TO BE IN THE 
 * PUBLIC DOMAIN AND THUS ARE AVAILABLE FOR UNRESTRICTED PUBLIC USE.  
 * THEY ARE FURNISHED "AS IS." THE AUTHORS, THE UNITED STATES GOVERNMENT, ITS
 * INSTRUMENTALITIES, OFFICERS, EMPLOYEES, AND AGENTS MAKE NO WARRANTY,
 * EXPRESS OR IMPLIED, AS TO THE USEFULNESS OF THE SOFTWARE AND
 * DOCUMENTATION FOR ANY PURPOSE. THEY ASSUME NO RESPONSIBILITY (1)
 * FOR THE USE OF THE SOFTWARE AND DOCUMENTATION; OR (2) TO PROVIDE
 * TECHNICAL SUPPORT TO USERS.
 */

package gov.noaa.ncdc.wct.ui.animation;

import gov.noaa.ncdc.wct.ui.WCTFrame;
import gov.noaa.ncdc.wct.ui.WCTViewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class CaptureAnimator extends WCTFrame 
implements ActionListener
{
	private AnimationFrame animationFrame;
	private JPanel mainPanel, choicePanel;
	private JButton jbLoad, jbClear;
	private JLabel jlInstructions;
	private JList captureList;
	private DefaultListModel listModel;
	private WCTViewer viewer;
	private int[] indices;
	private Vector<BufferedImage> vImageIcons;


	public CaptureAnimator(WCTViewer viewer) {
		super("Viewer Capture Animation");
		this.viewer=viewer;
		vImageIcons = new Vector<BufferedImage>(); // initialize the vector
		createGUI();
		pack();
		//setVisible(true);
	} 

	private void createGUI() {

		jlInstructions = new JLabel("Select Capture Frames to Animate", JLabel.CENTER);
		jlInstructions.setBorder(BorderFactory.createEmptyBorder(5, 55, 5, 55));
		
		// Initial File List panel
		listModel = new DefaultListModel();
		captureList = new JList(listModel);
		captureList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane listScrollPane = new JScrollPane(captureList);

		// Create Action Buttons
		choicePanel = new JPanel(); 
		jbLoad = new JButton("Load");
		jbLoad.addActionListener(this);
		jbClear = new JButton("Clear");
		jbClear.addActionListener(this);
		choicePanel.add(jbLoad);
		choicePanel.add(jbClear);

		mainPanel = new JPanel(); 
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(jlInstructions, "North");
		mainPanel.add(listScrollPane, "Center");
		mainPanel.add(choicePanel, "South");

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(mainPanel, "Center");

	} // END METHOD createGUI()

	public void addImage(BufferedImage img) {
		addImage(img, "Capture "+(listModel.getSize()+1));
	}

	public void addImage(BufferedImage img, String name) {
		vImageIcons.add(img);
		listModel.addElement(name);
		if (listModel.getSize() == 1) {
			jbClear.setText("   Clear   ");
			pack();
		}
	} // END METHOD addAWTImage

	// Implementation of ActionListener interface.
	public void actionPerformed(ActionEvent event) {

		Object source = event.getSource();
		if (source == jbClear) {
			if (jbClear.getText().trim().equals("Clear")) {
				vImageIcons = new Vector<BufferedImage>();
				listModel.clear();
				jbClear.setText("Close");
				pack();
			}
			else {
				dispose();
			}

		}
		else if (source == jbLoad) {
			indices = captureList.getSelectedIndices();
			//         iconLabelTitle = captureList.getSelectedValues();

			if (animationFrame != null) {
				animationFrame.dispose();
			}
			
			
			try {

            foxtrot.Worker.post(new foxtrot.Task() {
                public Object run() {

                    try {
                    	loadCaptureFrames();
                    	viewer.updateMemoryLabel();
                    } catch (Exception e) {
                    	e.printStackTrace();
                    	JOptionPane.showMessageDialog(viewer, 
                    			"--- General Capture Animation Error ---\n"+
                    			"Please submit a bug report to Steve.Ansari@noaa.gov\n", 
                    			"General Capture Animation Error", JOptionPane.ERROR_MESSAGE);
                    }

                    return "DONE";
                }
            });


			} catch (Exception e) {
				e.printStackTrace();
            	JOptionPane.showMessageDialog(viewer, 
            			"--- General Capture Animation Error ---\n"+
            			"Please submit a bug report to Steve.Ansari@noaa.gov\n", 
            			"General Capture Animation Error", JOptionPane.ERROR_MESSAGE);
			}
		}		
		
	} // actionPerformed
	
	
	
	
	private void loadCaptureFrames() {
		
		

        // Set up progress bar 
        JFrame frame = new WCTFrame("Export Progress");
        JProgressBar progressBar = new JProgressBar();
        // set up progressbar
        progressBar.setMinimum(0);
        progressBar.setMaximum(indices.length);
        progressBar.setStringPainted(true);
        progressBar.setString("Saving Image 1 of "+indices.length);
        progressBar.setValue(0);
        frame.getContentPane().add(progressBar);
        frame.setBounds(10, 10, 300, 50);
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);

		
		animationFrame = new AnimationFrame();
		for (int n=0; n<indices.length; n++) {
			try {
				animationFrame.addImage(vImageIcons.get(indices[n]), "Captured Image Frame "+(indices[n]+1));
				progressBar.setValue(n+1);
	            progressBar.setString("Preparing Image "+(n+1)+" of "+indices.length);
			} catch (IOException e) {
				e.printStackTrace();
				String string = "-- General Capture Animation Error --\n" +
				"Please report as a bug to Steve.Ansari@noaa.gov";
				JOptionPane.showMessageDialog(this, string,
						"ANIMATION ERROR", JOptionPane.ERROR_MESSAGE);
				break;
			}
		}
		
		frame.dispose();
		
        animationFrame.setAnimationIcon(0);
        animationFrame.validate();
        animationFrame.pack();
        animationFrame.setSize(new Dimension(viewer.getSize().width-58, viewer.getSize().height+8));

		animationFrame.setVisible(true);
		animationFrame.startAnimation();

	}
	
	

} // END CLASS
