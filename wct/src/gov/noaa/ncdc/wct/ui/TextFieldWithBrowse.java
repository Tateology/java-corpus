package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.WCTProperties;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jidesoft.hints.FileIntelliHints;
import com.jidesoft.swing.FolderChooser;
import com.jidesoft.swing.SelectAllUtils;

public class TextFieldWithBrowse extends JPanel {
	
	private JTextField jtfLocation = new JTextField(50);
	private JButton jbBrowse = new JButton("Browse");
	private File lastFolder = null;
	private String title = null;
	private String id = "";
	private boolean isDirOnly;
	private boolean hideBrowseButton;
	
	public TextFieldWithBrowse(String title) {
		this(title, "");
	}
	
	public TextFieldWithBrowse(String title, String id) {
		this(title, id, false);
	}

	public TextFieldWithBrowse(String title, String id, boolean isDirOnly) {
		this(title, id, isDirOnly, false);
	}
	
	public TextFieldWithBrowse(String title, String id, boolean isDirOnly, boolean hideBrowseButton) {
		super();
		this.title = title;
		this.id = id;
		this.isDirOnly = isDirOnly;
		this.hideBrowseButton = hideBrowseButton;
		createUI();
	}
	
	
	
	private void createUI() {
		this.setLayout(new RiverLayout());
		
		final Component finalThis = this;
		
		jbBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {                
                
                // Set up File Chooser
                if (lastFolder == null) {
                    String prop = WCTProperties.getWCTProperty("_textFieldWithBrowse["+id+"]-lastBrowseFolder");
                    if (prop != null) {
                        lastFolder = new File(prop);
                    }
                    else {
                        lastFolder = new File(System.getProperty("user.home"));
                    }
                }

                
                
                

                if (isDirOnly) {

                	FolderChooser folderChooser = new FolderChooser() {
                		protected JDialog createDialog(Component parent)
                				throws HeadlessException {
                			JDialog dlg = super.createDialog(parent);
                			dlg.setTitle("Select Directory");
                			dlg.setLocation((int)finalThis.getLocationOnScreen().getX()+finalThis.getWidth()+4, 
                					(int)finalThis.getLocationOnScreen().getY());
                			return dlg;
                		}
                	};

                	folderChooser.setRecentListVisible(false);
                	folderChooser.setAvailableButtons(FolderChooser.BUTTON_REFRESH | FolderChooser.BUTTON_DESKTOP 
                			| FolderChooser.BUTTON_MY_DOCUMENTS);
                	try {
                		if (lastFolder != null && lastFolder.length() > 0) {
                			lastFolder = folderChooser.getFileSystemView().createFileObject(lastFolder.toString());
                		}
                	} catch (Exception ex) {
                	}
                	folderChooser.setCurrentDirectory(lastFolder);
                	folderChooser.setFileHidingEnabled(true);
                	folderChooser.setPreferredSize(new Dimension(320, 500));
                	int returnVal = folderChooser.showOpenDialog(finalThis.getParent());
                	if (returnVal == FolderChooser.APPROVE_OPTION) {
                		lastFolder = folderChooser.getSelectedFile();
                		WCTProperties.setWCTProperty("_textFieldWithBrowse["+id+"]-lastBrowseFolder", lastFolder.toString());
                		jtfLocation.setText(lastFolder.toString());

                	}

                }
                else {


                	JFileChooser fc = new JFileChooser(lastFolder);
                	int returnVal = fc.showOpenDialog(jbBrowse);
                	File file = null;
                	if (returnVal == JFileChooser.APPROVE_OPTION) {
                		file = fc.getSelectedFile();
                		lastFolder = file.getParentFile();
                		WCTProperties.setWCTProperty("_textFieldWithBrowse["+id+"]-lastBrowseFolder", lastFolder.toString());
                		//This is where a real application would open the file.
                		System.out.println("Opening: " + file.getName() + ".");

                		jtfLocation.setText(file.toString());
                	}

                }
            }
		});

		
		
        String prop = WCTProperties.getWCTProperty("_textFieldWithBrowse["+id+"]-lastText");
        if (prop != null) {
            jtfLocation.setText(prop);
        }
        
        
        if (! hideBrowseButton) {
        	// JIDE stuff
        	jtfLocation.setName("File IntelliHint");
        	SelectAllUtils.install(jtfLocation);
        	new FileIntelliHints(jtfLocation).setFolderOnly(true);
        }
        
        
        jtfLocation.getDocument().addDocumentListener(new TextChangeListener());
		
        if (title != null && title.trim().length() > 0) {
        	this.add(new JLabel(title));
    		this.add(jtfLocation, "br hfill");
        }
        else {
    		this.add(jtfLocation, "hfill");
        }
        if (! hideBrowseButton) {
        	this.add(jbBrowse);
        }
		
	}

	public void setSelectedLocation(String selectedLocation) {
		jtfLocation.setText(selectedLocation);
	}

	public String getSelectedLocation() {
		return jtfLocation.getText();
	}
	
	
    private class TextChangeListener implements DocumentListener {
        private int waitTime = 1000;

        Timer timer = new Timer();
        TimerTask task = null;


        @Override
        public void changedUpdate(DocumentEvent evt) {
//        	System.out.println("event 1");
        	processEvent();
        }

        @Override
        public void insertUpdate(DocumentEvent evt) {
//        	System.out.println("event 2");
        	processEvent();
        }
        @Override
        public void removeUpdate(DocumentEvent evt) {
//        	System.out.println("event 3");
        	processEvent();
        }
        
        
        private void processEvent() {
            if (task != null) {
                try {
                    task.cancel();
                } catch (Exception ex) {
                }
            }
            task = new TimerTask() {
                public void run() {
//                    System.out.println(" EXECUTING TEXT CHANGE EVENT ");
                    WCTProperties.setWCTProperty("_textFieldWithBrowse["+id+"]-lastText", jtfLocation.getText());
                }
            };
            timer.schedule(task , waitTime);

        }
    }

}
