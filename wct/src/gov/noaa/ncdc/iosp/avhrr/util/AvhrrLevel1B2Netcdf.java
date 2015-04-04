/**
 *      Copyright (c) 2008 Work of U.S. Government.
 *      No rights may be assigned.
 *
 * LIST OF CONDITIONS
 * Redistribution and use of this program in source and binary forms, with or
 * without modification, are permitted for any purpose (including commercial purposes) 
 * provided that the following conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice,
 *     this list of conditions, and the following disclaimer.
 *
 * 2.  Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions, and the following disclaimer in the documentation
 *    and/or materials provided with the distribution.
 *
 * 3.  In addition, redistributions of modified forms of the source or binary
 *     code must carry prominent notices stating that the original code was
 *     changed, the author of the revisions, and the date of the change.
 *
 * 4.  All publications or advertising materials mentioning features or use of
 *     this software are asked, but not required, to acknowledge that it was
 *     developed at the NOAA's National Climatic Data Center in Asheville, NC and to
 *     credit the contributors.
 *
 * 5.  THIS SOFTWARE IS PROVIDED BY THE GOVERNMENT AND THE CONTRIBUTORS  "AS IS"
 *     WITH NO WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED.  In no event
 *     shall the Government or the Contributors be liable for any damages
 *     suffered by the users arising out of the use of this software, even if
 *     advised of the possibility of such damage.
 */

package gov.noaa.ncdc.iosp.avhrr.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ProgressMonitor;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

// import org.apache.commons.logging.Log;
// import org.apache.commons.logging.LogFactory;
// import org.apache.log4j.FileAppender;
// import org.apache.log4j.PatternLayout;
// import org.apache.log4j.PropertyConfigurator;

/**
 * AvhrrLevel1B2Netcdf.java This is the main class for hte Avhrr Gac to netcdf
 * converter. Defines the GUI and most of functionality.
 * 
 * @author afotos@noaa.gov Created on January 14, 2008, 3:13 PM
 */
public class AvhrrLevel1B2Netcdf extends javax.swing.JFrame implements ConverterConstants {

	// private static Log log = LogFactory.getLog(AvhrrLevel1B2Netcdf.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static String helpText = "<html>\n<body>\n<h1>NCDC sat2netcdf Converter</h1>\n<a href=\"#license\">License & Disclaimer</a><br></br>\n<a href=\"#general\">General Information</a><br></br>\n<a href=\"#requirements\">Requirements</a><br></br>\n<a href=\"#files\">Supported Files</a><br></br>\n<a href=\"#options\">Options</a><br></br>\n<a href=\"#limitations\">Limitations</a><br></br>\n</br>\n\n<a name=\"license\"></a>\nLicense:<br></br>\nCopyright (c) 2008 Work of U.S. Government.<br></br>\nNo rights may be assigned.\n<br></br>\nLIST OF CONDITIONS<br></br>\nRedistribution and use of this program in source and binary forms, \nwith or without modification, are permitted for any purpose (including commercial purposes) provided that the following conditions are met:\n<ul>\n<li>1.  Redistributions of source code must retain the above copyright notice, this list of conditions, and the following disclaimer.\n\n<li>2.  Redistributions in binary form must reproduce the above copyright notice, this list of conditions, and the following disclaimer in the documentation and/or materials provided with the distribution.\n\n<li>3.  In addition, redistributions of modified forms of the source or binary code must carry prominent notices stating that the original code was changed, the author of the revisions, and the date of the change.\n\n<li>4.  All publications or advertising materials mentioning features or \nuse of this software are asked, but not required, to acknowledge that it was developed at the NOAA's National Climatic Data Center in \nAsheville, NC and to credit the contributors.\n\n<li>5.  THIS SOFTWARE IS PROVIDED BY THE GOVERNMENT AND THE CONTRIBUTORS \n\"AS IS\" WITH NO WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED.  In no \nevent shall the Government or the Contributors be liable for any damages suffered by the users arising out of the use of this software, \neven if advised of the possibility of such damage.\n</ul>\n\nDisclaimer: <br></br> \n    This work was prepared as an account of work by employees of an agency of the United States Government.  Neither the United States Government nor any of its employees, makes any warranty, express or implied, or\nassumes any liability or responsibility for the accuracy, completeness, or usefulness of any information, apparatus, product, or process disclosed, or represents that its use would not infringe privately-\nowned rights.  Reference herein to any specific commercial products, process, or service by trade name, trademark, manufacturer, or otherwise, does not necessarily constitute or imply its endorsement, recommendation, or favoring by the United States Government.  The views and opinions of authors expressed herein do not necessarily state or reflect those of the United States Government, and shall not be used for advertising or product endorsement purposes.\n<br></br>\n<a name=\"general\"></a>\n<H3>General Information</H3>\n<div name=\"general\">\nThis application is experimental and any output has not been fully tested. This software is designed to convert\nAvhrr Gac file to netcdf-3 format. Not all variables are for each dataset are available at this time.  For pre-KLM files all\nvariables are available except for Telemetry variables. For KLM datasets a majority of the calibration and quality variables are available.\nCalibration calculations have not been tested.  Pre launch values for slope and intercept have been used when calibrating visible channels.\n\nMore information about the datasets and calibration can be found here:<br></br>\nKLM datasets: http://www2.ncdc.noaa.gov/docs/klm/cover.htm <br></br>\npre-KLM datasets: http://www2.ncdc.noaa.gov/docs/podug/cover.htm \n<br></br>\nThis tool can be run without the User Interface by running with the following argument:\njava -Xmx1024M -jar avhrr2netcdf.jar -nogui.\nThis will list all the available options when running without the User Interface.\n</div>\n\n<a name=\"requirements\"></a>\n<H3>Requirements</H3>\n<div name=\"requirements\">\nJava JRE 1.5 or greater is required to run this application.\nThis application should be run from a command line using the following command: <br></br>\njava -Xmx1024M -jar avhrr2netcdf.jar.\nClasspath issues can result from launching the application by double-clicking the jar.\n1024M of maximum heap space is recommended.  This can be adjusted with the -Xmx runtime argument.\nIf Out of memory: heap space exceptions occur increase the maximum heap size.\n</div>\n\n<H3>Supported Files</H3>\n<a name=\"files\"></a>\n<div id=\"files\">\n<p>\nThis program is designed to convert Avhrr Global Area Coverage (GAC) 4km Level 1B files downloaded from CLASS to netcdf-3 format.\nCurrently, only 10 bit/pixel data format is supported. Files with or without the Archive Header are supported. Satellites starting with Tiros-N thru\nNOAA-N are supported. \n</p>\n</div>\n<a name=\"options\">\n<H3>Options</H3>\n<div id=\"options\">\nChannels:<br></br>\n This option allows the user to select which channels will be included in the netcdf file.  For KLM datasets, data for channel 3B and 3B\n will be included for the Channel 3 option.\n<br></br>\nRaw Data:<br></br>\nCounts for selected channels will be output.<br></br>\nRadiance:<br></br>\nCalculated radiance values will be output.<br></br>\nBrightness Temperature:<br></br>\nCalculated Albedo values will be output for visible channels.  Calculated Brightness Temperature will be output for IR channels.<br></br>\nAll Variables:<br></br>\nThis option will output all available variables. Warning! This option can create very large files, approximately 7 times the size of the original file.<br></br>\nQuality:<br></br>\nThis option will output variables related to scan line quality.<br></br>\nCalibration<br></br>\nThis option will out variables used to calibrate the data.<br></br>\nLat/Lon  <br></br>\nThis option will output interpolated values for lat and lon for each pixel on the scan line.  Lagrangian interpolation is used for values of\nlatitude between -85.0 and 85.0 degrees.  Gnomic interpolation is used for values outside that range.  The accuracy of the interpolation has not been verified!  \n\n<br></br>\nMetadata <br></br> \nFor pre-KLM files the following variables will be output: number of zenith angles, zenith angles, anchor lat, anchor lon.\nFor KLM files the following variables will be output: anchor lat, anchor lon, cloud flag, clavr status.\n\n</div>\n\n<a name=\"limitations\"></a>\n<h3>Limitations</h3>\n<div id=\"limitations\">\nOnly 10-bit packed files are supported.\n</div>\n</body>\n</html>";

	/** Creates new form Avhrr2Netcdf */
	public AvhrrLevel1B2Netcdf() {
		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {
		// Create a file chooser
		this.setTitle("NCDC sat2netcdf converter");

		aboutDialog = new javax.swing.JDialog();
		aboutButton = new javax.swing.JButton();
		aboutLabel = new javax.swing.JLabel();
		aboutDialog.setTitle("About");
		aboutDialog.setName("aboutDialog"); // NOI18N
		aboutDialog.setSize(250, 200);

		aboutButton.setText("OK");
		aboutButton.setName("aboutButton"); // NOI18N
		aboutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				aboutDialog.hide();
			}
		});

		aboutLabel
				.setText("<html>\nNCDC sat2netcdf Converter<br> </br>\nCopyright (c) 2008 Work of U.S. Government.<br></br>\nNCDC<br></br>\nContact: ncdc.satorder@noaa.gov\n</html>");

		fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);

		// output directory file chooser
		outFc = new JFileChooser();
		outFc.setMultiSelectionEnabled(false);
		outFc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		activityMonitor = new Timer(100, new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				convertMonitorActionPerformed(evt);
			}
		});

		menuBar = new JMenuBar();
		logMenu = new JMenu("Log");

		logMenuItem = new JMenuItem("View Log");
		logMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				logDialog.show();
			}
		});
		logMenu.add(logMenuItem);

		menuBar.add(logMenu);
		helpMenu = new JMenu("Help");
		menuBar.add(helpMenu);

		helpMenuItem = new JMenuItem("Help");
		helpMenuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				helpMenuActionPerformed(evt);
			}
		});
		helpMenu.add(helpMenuItem);
		aboutMenuItem = new JMenuItem("About");
		aboutMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				aboutDialog.show();
			}
		});
		helpMenu.add(aboutMenuItem);

		this.setJMenuBar(menuBar);

		jButton4 = new javax.swing.JButton();
		openButton = new javax.swing.JButton();
		jScrollPane1 = new javax.swing.JScrollPane();
		jTable1 = new javax.swing.JTable();
		jLabel1 = new javax.swing.JLabel();
		outdirButton = new javax.swing.JButton();
		outdirText = new javax.swing.JTextField();
		jPanel1 = new javax.swing.JPanel();
		allChanCheckBox = new javax.swing.JCheckBox();
		jLabel3 = new javax.swing.JLabel();
		jLabel4 = new javax.swing.JLabel();
		jLabel5 = new javax.swing.JLabel();
		chan1CheckBox = new javax.swing.JCheckBox();
		chan2CheckBox = new javax.swing.JCheckBox();
		chan3CheckBox = new javax.swing.JCheckBox();
		chan4CheckBox = new javax.swing.JCheckBox();
		chan5CheckBox = new javax.swing.JCheckBox();
		rawCheckBox = new javax.swing.JCheckBox();
		radCheckBox = new javax.swing.JCheckBox();
		tempCheckBox = new javax.swing.JCheckBox();
		allVarCheckBox = new javax.swing.JCheckBox();
		qualityCheckBox = new javax.swing.JCheckBox();
		calCheckBox = new javax.swing.JCheckBox();
		latlonCheckBox = new javax.swing.JCheckBox();
		metaCheckBox = new javax.swing.JCheckBox();
		jLabel2 = new javax.swing.JLabel();
		jLabel6 = new javax.swing.JLabel();
		convertButton = new javax.swing.JButton();
		exitButton = new javax.swing.JButton();

		jButton4.setText("jButton4");

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		openButton.setText("Select Files");
		openButton.setName("selectButton"); // NOI18N
		openButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				openButtonActionPerformed(evt);
			}
		});

		fm = new FileModel();
		jTable1 = new JTable(fm);
		jTable1.setName("table"); // NOI18N
		jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		setTableCellRenderer(jTable1, new ToolCellRenderer());
		// remove popup for table
		removeItem = new JMenuItem("Remove");
		removeItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				int[] selected = jTable1.getSelectedRows();
				for (int i : selected) {
					logTextArea.append("Removed File: " + fm.getFileAtRow(i).getName() + "\n");
				}
				fm.removeRows(selected);
			}
		});
		popup = new JPopupMenu();
		popup.add(removeItem);
		// Add listener to the jtable so the popup menu can come up.
		// MouseListener popupListener = new PopupListener(jTable1);
		// jTable1.addMouseListener(popupListener);

		jTable1.addMouseListener(new MouseListener() {
			public void mousePressed(MouseEvent e) {
				int button = e.getButton();
				int[] selected = jTable1.getSelectedRows();
				if (button != 1 && selected.length > 0) {
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			public void mouseClicked(MouseEvent e) {
				int button = e.getButton();
				int column = jTable1.getSelectedColumn();
				if (0 == column && 1 == button) {
					updateOutputSize();
				}
			}

			public void mouseEntered(MouseEvent e) {

			}

			public void mouseExited(MouseEvent e) {

			}

			public void mouseReleased(MouseEvent e) {

			}
		});

		TableColumn col = jTable1.getColumnModel().getColumn(1);
		col.setPreferredWidth(250);
		col = jTable1.getColumnModel().getColumn(3);
		col.setPreferredWidth(100);

		jScrollPane1.setViewportView(jTable1);

		jLabel1.setText("Select output directory");

		outdirButton.setText("Browse");
		outdirButton.setName("outdirButton"); // NOI18N
		outdirButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				outdirButtonActionPerformed(evt);
			}
		});
		outdirText.setName("outdirText"); // NOI18N
		try {
			outdirText.setText(new File(".").getCanonicalPath());
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
		jPanel1.setName("optionPanel"); // NOI18N

		allChanCheckBox.setText("All Channels");
		allChanCheckBox.setSelected(true);
		allChanCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleChannelBoxes("jCheckBox1", allChanCheckBox);
				updateFileSize();
			}
		});

		jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		jLabel3.setText("Channels");

		jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		jLabel4.setText("Calibration");
		jLabel4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

		jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		jLabel5.setText("Additional Data");
		jLabel5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

		chan1CheckBox.setText("Channel 1");
		chan1CheckBox.setSelected(true);
		chan1CheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleChannelBoxes("chan1CheckBox", chan1CheckBox);
				updateFileSize();
			}
		});
		chan2CheckBox.setText("Channel 2");
		chan2CheckBox.setSelected(true);
		chan2CheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleChannelBoxes("chan2CheckBox", chan2CheckBox);
				updateFileSize();
			}
		});

		chan3CheckBox.setText("Channel 3A/B");
		chan3CheckBox.setSelected(true);
		chan3CheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleChannelBoxes("chan3CheckBox", chan3CheckBox);
				updateFileSize();
			}
		});

		chan4CheckBox.setLabel("Channel 4");
		chan4CheckBox.setSelected(true);
		chan4CheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleChannelBoxes("chan4CheckBox", chan4CheckBox);
				updateFileSize();
			}
		});
		chan5CheckBox.setLabel("Channel 5");
		chan5CheckBox.setSelected(true);
		chan5CheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleChannelBoxes("chan5CheckBox", chan5CheckBox);
				updateFileSize();
			}
		});
		rawCheckBox.setText("Raw Data");
		rawCheckBox.setSelected(true);
		rawCheckBox.setToolTipText("Counts for each pixel");
		rawCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleCalibrationCheckBoxes(rawCheckBox);
				updateFileSize();
			}
		});
		radCheckBox.setText("Radiance");
		radCheckBox.setToolTipText("Radiance Values");
		radCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleCalibrationCheckBoxes(radCheckBox);
				updateFileSize();
			}
		});
		tempCheckBox.setText("Brightness Temperature");
		tempCheckBox.setToolTipText("Albedo for visible channels, Brightness Temperature for IR channels");
		tempCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleCalibrationCheckBoxes(tempCheckBox);
				updateFileSize();
			}
		});
		allVarCheckBox.setText("All Variables");
		allVarCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleAllVariablesCheckBox();
				updateFileSize();
			}
		});
		qualityCheckBox.setText("Quality Flags");
		qualityCheckBox.setToolTipText("Quality Variables");
		qualityCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleOtherCheckbox(qualityCheckBox);
				updateFileSize();
			}
		});

		calCheckBox.setText("Calibration Coefficients");
		calCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleOtherCheckbox(calCheckBox);
				updateFileSize();
			}
		});

		jLabel2.setText("Select output options");

		convertButton.setText("Convert");
		convertButton.setName("convertButton"); // NOI18N
		convertButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				convertButtonActionPerformed(evt);
			}
		});

		exitButton.setText("Exit");
		exitButton.setName("exitButton"); // NOI18N
		exitButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				exitButtonActionPerformed(evt);
			}
		});

		// help dialog
		helpDialog = new javax.swing.JDialog();
		helpCloseButton = new javax.swing.JButton("Close");
		helpScrollPane = new javax.swing.JScrollPane();
		
//		helpEditorPane = new javax.swing.JEditorPane("text/rtf", helpText);
		helpEditorPane = new javax.swing.JEditorPane();
		helpEditorPane.setContentType("text/html");
		InputStream stream = null;
		try {
			stream = AvhrrLevel1B2Netcdf.class.getResourceAsStream("/help.html");
			if(stream != null){
				helpEditorPane.read(stream, "html");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		helpScrollPane.setViewportView(helpEditorPane);
		helpDialog.setSize(450, 500);
		helpCloseButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				helpDialog.dispose();
			}
		});

		// Log Dialog
		logDialog = new javax.swing.JDialog();
		logScrollPane = new javax.swing.JScrollPane();
		logTextArea = new javax.swing.JTextArea();
		logCloseButton = new javax.swing.JButton();
		clearButton = new javax.swing.JButton();

		logDialog.setTitle("Log");
		logDialog.setName("logDialog"); // NOI18N
		logDialog.setSize(450, 500);
		logScrollPane.setName("logScrollPane"); // NOI18N

		logTextArea.setColumns(20);
		logTextArea.setRows(5);
		logTextArea.setName("logTextArea"); // NOI18N
		logScrollPane.setViewportView(logTextArea);

		logCloseButton.setText("Close");
		logCloseButton.setName("logCloseButton"); // NOI18N
		logCloseButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				logDialog.hide();
			}
		});

		clearButton.setText("Clear");
		clearButton.setName("clearButton"); // NOI18N
		clearButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				logTextArea.setText("");
			}
		});

		latlonCheckBox.setText("Lat/Lon");
		latlonCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleLatLonCheckbox();
				updateFileSize();
			}
		});

		metaCheckBox.setText("Other metadata");
		metaCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleMetadataCheckbox();
				updateFileSize();
			}
		});

		jLabel2.setText("Select output options");

		convertButton.setText("Convert");
		convertButton.setName("convertButton");

		exitButton.setText("Exit");
		exitButton.setName("exitButton");
		exitButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				exitButtonActionPerformed(evt);
			}
		});

		jLabel2.setText("Select output options"); // NOI18N

		convertButton.setText("Convert"); // NOI18N
		convertButton.setName("convertButton"); // NOI18N

		exitButton.setText("Exit"); // NOI18N
		exitButton.setName("exitButton"); // NOI18N
		exitButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				exitButtonActionPerformed(evt);
			}
		});

		jLabel6.setFont(new java.awt.Font("Dialog", 1, 11));
		jLabel6.setText("Estimated output size: ");

		org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout
				.setHorizontalGroup(jPanel1Layout
						.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								jPanel1Layout
										.createSequentialGroup()
										.addContainerGap()
										.add(
												jPanel1Layout
														.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
														.add(chan5CheckBox)
														.add(
																jPanel1Layout
																		.createSequentialGroup()
																		.add(
																				jPanel1Layout
																						.createParallelGroup(
																								org.jdesktop.layout.GroupLayout.LEADING)
																						.add(
																								jPanel1Layout
																										.createSequentialGroup()
																										.add(
																												jPanel1Layout
																														.createParallelGroup(
																																org.jdesktop.layout.GroupLayout.LEADING)
																														.add(
																																jPanel1Layout
																																		.createParallelGroup(
																																				org.jdesktop.layout.GroupLayout.TRAILING,
																																				false)
																																		.add(
																																				org.jdesktop.layout.GroupLayout.LEADING,
																																				jLabel3,
																																				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																																				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																																				Short.MAX_VALUE)
																																		.add(
																																				org.jdesktop.layout.GroupLayout.LEADING,
																																				allChanCheckBox,
																																				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																																				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																																				Short.MAX_VALUE))
																														.add(
																																chan1CheckBox)
																														.add(
																																chan2CheckBox))
																										.add(36, 36, 36)
																										.add(
																												jPanel1Layout
																														.createParallelGroup(
																																org.jdesktop.layout.GroupLayout.LEADING,
																																false)
																														.add(
																																radCheckBox)
																														.add(
																																tempCheckBox,
																																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																																163,
																																Short.MAX_VALUE)
																														.add(
																																rawCheckBox)
																														.add(
																																jLabel4,
																																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																																Short.MAX_VALUE)))
																						.add(chan3CheckBox).add(chan4CheckBox))
																		.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
																		.add(
																				jPanel1Layout
																						.createParallelGroup(
																								org.jdesktop.layout.GroupLayout.LEADING)
																						.add(metaCheckBox)
																						.add(latlonCheckBox)
																						.add(allVarCheckBox)
																						.add(
																								jLabel5,
																								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																								113,
																								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
																						.add(qualityCheckBox).add(calCheckBox)))).add(
												67, 67, 67)));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
				jPanel1Layout.createSequentialGroup().addContainerGap().add(
						jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel3).add(jLabel5).add(
								jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
						org.jdesktop.layout.LayoutStyle.UNRELATED).add(
						jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(allChanCheckBox)
								.add(allVarCheckBox).add(rawCheckBox)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(
						jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(chan1CheckBox)
								.add(qualityCheckBox).add(radCheckBox)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(
						jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(chan2CheckBox)
								.add(calCheckBox).add(tempCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
						org.jdesktop.layout.LayoutStyle.RELATED).add(
						jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(chan3CheckBox)
								.add(latlonCheckBox)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(
						jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(chan4CheckBox)
								.add(metaCheckBox)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(chan5CheckBox)
						.addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		jLabel2.setText("Select output options"); // NOI18N

		convertButton.setText("Convert"); // NOI18N
		convertButton.setName("convertButton"); // NOI18N

		exitButton.setText("Exit"); // NOI18N
		exitButton.setName("exitButton"); // NOI18N
		exitButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				exitButtonActionPerformed(evt);
			}
		});

		jLabel6.setFont(new java.awt.Font("Dialog", 1, 11));
		jLabel6.setText("Estimated output size: ");

		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
				layout.createSequentialGroup().addContainerGap().add(
						layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel1,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE).add(
								layout.createSequentialGroup().add(outdirButton).addPreferredGap(
										org.jdesktop.layout.LayoutStyle.RELATED).add(outdirText,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)).add(jLabel1,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE).add(openButton)
								.add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 291,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(
										layout.createSequentialGroup().add(convertButton).addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED).add(jLabel6,
												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE))
								.add(exitButton).add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 531,
										Short.MAX_VALUE)).addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
				layout.createSequentialGroup().addContainerGap().add(openButton).addPreferredGap(
						org.jdesktop.layout.LayoutStyle.UNRELATED).add(jScrollPane1,
						org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 173, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jLabel1).addPreferredGap(
								org.jdesktop.layout.LayoutStyle.RELATED).add(
								layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(outdirButton).add(
										outdirText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27,
										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
								org.jdesktop.layout.LayoutStyle.RELATED).add(jLabel2,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel1,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(
								org.jdesktop.layout.LayoutStyle.RELATED).add(
								layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false).add(convertButton).add(
										jLabel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addPreferredGap(
								org.jdesktop.layout.LayoutStyle.UNRELATED).add(exitButton).add(12, 12, 12)));
		// log dialog layout
		org.jdesktop.layout.GroupLayout logDialogLayout = new org.jdesktop.layout.GroupLayout(logDialog.getContentPane());
		logDialog.getContentPane().setLayout(logDialogLayout);
		logDialogLayout.setHorizontalGroup(logDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
				org.jdesktop.layout.GroupLayout.TRAILING,
				logDialogLayout.createSequentialGroup().addContainerGap().add(
						logDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(
								org.jdesktop.layout.GroupLayout.LEADING, logScrollPane,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE).add(
								logDialogLayout.createSequentialGroup().add(clearButton).addPreferredGap(
										org.jdesktop.layout.LayoutStyle.UNRELATED).add(logCloseButton))).addContainerGap()));
		logDialogLayout.setVerticalGroup(logDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
				org.jdesktop.layout.GroupLayout.TRAILING,
				logDialogLayout.createSequentialGroup().addContainerGap().add(logScrollPane,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE).add(18, 18, 18).add(
						logDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(logCloseButton).add(
								clearButton)).addContainerGap()));

		// help dialog layout
		org.jdesktop.layout.GroupLayout helpDialogLayout = new org.jdesktop.layout.GroupLayout(helpDialog.getContentPane());
		helpDialog.getContentPane().setLayout(helpDialogLayout);
		helpDialogLayout.setHorizontalGroup(helpDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
				org.jdesktop.layout.GroupLayout.TRAILING,
				helpDialogLayout.createSequentialGroup().addContainerGap().add(
						helpDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(
								org.jdesktop.layout.GroupLayout.LEADING, helpScrollPane,
								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE).add(
								helpDialogLayout.createSequentialGroup().addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
										.add(helpCloseButton))).addContainerGap()));
		helpDialogLayout.setVerticalGroup(helpDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
				org.jdesktop.layout.GroupLayout.TRAILING,
				helpDialogLayout.createSequentialGroup().addContainerGap().add(helpScrollPane,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE).add(18, 18, 18).add(
						helpDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(helpCloseButton))
						.addContainerGap()));

		// about dialog layout
		org.jdesktop.layout.GroupLayout aboutDialogLayout = new org.jdesktop.layout.GroupLayout(aboutDialog.getContentPane());
		aboutDialog.getContentPane().setLayout(aboutDialogLayout);
		aboutDialogLayout.setHorizontalGroup(aboutDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
				org.jdesktop.layout.GroupLayout.TRAILING,
				aboutDialogLayout.createSequentialGroup().addContainerGap().add(
						aboutDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(
								org.jdesktop.layout.GroupLayout.LEADING, aboutLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
								227, Short.MAX_VALUE).add(aboutButton)).addContainerGap()));
		aboutDialogLayout.setVerticalGroup(aboutDialogLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
				org.jdesktop.layout.GroupLayout.TRAILING,
				aboutDialogLayout.createSequentialGroup().addContainerGap().add(aboutLabel,
						org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 103, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 35, Short.MAX_VALUE).add(aboutButton).add(21, 21,
								21)));

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		pack();
		
		JOptionPane.showMessageDialog(this,
		"Warning! \nThis program is untested.\nPlease read help file for\nsupported files, limitations and license.\n");
//				+ "Please view Help for more information.");
	}

	private void convertButtonActionPerformed(ActionEvent evt) {
		files = fm.getFilesToConvert();
		if (files == null || files.size() == 0) {
			JOptionPane.showMessageDialog(this, "No valid files were selected!", "Message", JOptionPane.ERROR_MESSAGE);
		} else {
			List<String> options = determineOptions();
			activity = new NetcdfConverter(files, options, outdirText.getText());
			// launch progress dialog
			progressDialog = new ProgressMonitor(AvhrrLevel1B2Netcdf.this, "Converting Files....", null, -1,
					activity.getTotal() + 2);
			progressDialog.setMillisToDecideToPopup(0);
			activityMonitor.start();
			activity.start();
			// start timer
			convertButton.setEnabled(false);
		}
	}

	private void convertMonitorActionPerformed(ActionEvent evt) {
		int current = activity.getCurrent();
		// log.append(current + "\n");
		progressDialog.setNote(Integer.toString(current));
		progressDialog.setMillisToPopup(0);
		progressDialog.setMillisToDecideToPopup(1);
		progressDialog.setProgress(current);
		// check if task is completed or canceled
		if (current == activity.getTotal() + 1 || progressDialog.isCanceled()) {
			activityMonitor.stop();
			progressDialog.close();
			activity.interrupt();
			convertButton.setEnabled(true);
			JOptionPane.showMessageDialog(this, "Conversion Complete.", "Message", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void exitButtonActionPerformed(java.awt.event.ActionEvent evt) {
		System.exit(1);
	}

	private void helpMenuActionPerformed(ActionEvent evt) {
		// HelpFrame hframe = new HelpFrame();
		helpDialog.show();
	}

	private void outdirButtonActionPerformed(java.awt.event.ActionEvent evt) {
		int returnVal = outFc.showOpenDialog(AvhrrLevel1B2Netcdf.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = outFc.getSelectedFile();
			outdirText.setText(file.getAbsolutePath());
		}
	}

	private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {
		int returnVal = fc.showOpenDialog(AvhrrLevel1B2Netcdf.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File[] files = fc.getSelectedFiles();
			if (files != null && files.length > 0) {
				fm.updateTable(files);
				updateFileSize();
				convertButton.setEnabled(true);
				for (File f : files) {
					logTextArea.append("Added File: " + f.getName() + "\n");
					// log.info("Added File: " + f.getName());
				}
			}
		} else {
			// log.append("Open command cancelled by user." + newline);
		}
		// log.setCaretPosition(log.getDocument().getLength());

	}

	private List<String> determineOptions() {
		List<String> options = new ArrayList<String>();
		// first lets check to see if "all variables" is selected - if so just
		// write everything
		if (allVarCheckBox.isSelected()) {
			options.add(OPTION_ALLVAR);
		} else {
			if (allChanCheckBox.isSelected()) {
				options.add(OPTION_ALLCHAN);
			} else {
				if (chan1CheckBox.isSelected()) {
					options.add(OPTION_CH1);
				}
				if (chan2CheckBox.isSelected()) {
					options.add(OPTION_CH2);
				}
				if (chan3CheckBox.isSelected()) {
					options.add(OPTION_CH3);
				}
				if (chan4CheckBox.isSelected()) {
					options.add(OPTION_CH4);
				}
				if (chan5CheckBox.isSelected()) {
					options.add(OPTION_CH5);
				}
			}
			if (rawCheckBox.isSelected()) {
				options.add(OPTION_RAW);
			}
			if (radCheckBox.isSelected()) {
				options.add(OPTION_RADIANCE);
			}
			if (tempCheckBox.isSelected()) {
				options.add(OPTION_TEMP);
			}
			if (qualityCheckBox.isSelected()) {
				options.add(OPTION_QUALITY);
			}
			if (calCheckBox.isSelected()) {
				options.add(OPTION_CALIBRATION);
			}
			if (latlonCheckBox.isSelected()) {
				options.add(OPTION_LATLON);
			}
			if (metaCheckBox.isSelected()) {
				options.add(OPTION_METADATA);
			}
		}
		return options;
	}

	private void handleChannelBoxes(String name, JCheckBox cb) {
		if (allChanCheckBox.equals(cb)) {
			chan1CheckBox.setSelected(true);
			chan2CheckBox.setSelected(true);
			chan3CheckBox.setSelected(true);
			chan4CheckBox.setSelected(true);
			chan5CheckBox.setSelected(true);
			allChanCheckBox.setSelected(true);
		} else {
			allChanCheckBox.setSelected(false);
			allVarCheckBox.setSelected(false);
		}
	}

	public void handleCalibrationCheckBoxes(JCheckBox cb) {
		if (rawCheckBox.equals(cb)) {
			// if no other checkbox is selected then keep selected
			if (!rawCheckBox.isSelected()) {
				allVarCheckBox.setSelected(false);
				if (radCheckBox.isSelected() || tempCheckBox.isSelected()) {

				} else {
					rawCheckBox.setSelected(true);
				}
			}
		} else if (radCheckBox.equals(cb)) {
			if (!radCheckBox.isSelected()) {
				allVarCheckBox.setSelected(false);
			}
			// if user deselects and BT checkbox is deselected - select raw data
			// CB
			if (!tempCheckBox.isSelected()) {
				rawCheckBox.setSelected(true);
			}
		} else if (tempCheckBox.equals(cb)) {
			// if user deselects and Radiance CB is deselected - select raw data
			// CB
			if (!tempCheckBox.isSelected()) {
				allVarCheckBox.setSelected(false);
			}
			if (!radCheckBox.isSelected()) {
				rawCheckBox.setSelected(true);
			}
		}
	}

	public void handleAllVariablesCheckBox() {
		if (allVarCheckBox.isSelected()) {
			allChanCheckBox.setSelected(true);
			chan1CheckBox.setSelected(true);
			chan2CheckBox.setSelected(true);
			chan3CheckBox.setSelected(true);
			chan4CheckBox.setSelected(true);
			chan5CheckBox.setSelected(true);
			rawCheckBox.setSelected(true);
			radCheckBox.setSelected(true);
			tempCheckBox.setSelected(true);
			qualityCheckBox.setSelected(true);
			calCheckBox.setSelected(true);
			latlonCheckBox.setSelected(true);
			metaCheckBox.setSelected(true);
		}
	}

	public void handleOtherCheckbox(JCheckBox cb) {
		if (!cb.isSelected()) {
			allVarCheckBox.setSelected(false);
		}
	}

	public void handleLatLonCheckbox() {
		if (!latlonCheckBox.isSelected()) {
			allVarCheckBox.setSelected(false);
		}
	}

	public void handleMetadataCheckbox() {
		if (!metaCheckBox.isSelected()) {
			allVarCheckBox.setSelected(false);
		}
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) throws Exception {
		// create the Options object
		Options options = new Options();
		options.addOption(OPTION_NOGUI, false, "command line mode");
		options.addOption(OPTION_ALLVAR, false, "Process all channels");
		options.addOption(OPTION_CH1, false, "Process channel 1");
		options.addOption(OPTION_CH2, false, "Process channel 2");
		options.addOption(OPTION_CH3, false, "Process channel 3");
		options.addOption(OPTION_CH4, false, "Process channel 4");
		options.addOption(OPTION_CH5, false, "Process channel 5");
		options.addOption(OPTION_DEFAULT, false, "Output All Channels, Brightness Temperature and Latitude and  Longitude");
		options.addOption(OPTION_TEMP, false, "Output calibrated temperatures/albedos");
		options.addOption(OPTION_RAW, false, "Output raw values");
		options.addOption(OPTION_RADIANCE, false, "Output radiance values");
		options.addOption(OPTION_LATLON, false, "Output latitude and longitude");
		options.addOption(OPTION_ALLVAR, false, "Output all variables)");
		options.addOption(OPTION_CLOBBER, false, "Overwrite existing netCDF file.");
		options.addOption(OPTION_CALIBRATION, false, "Output calibration variables");
		options.addOption(OPTION_QUALITY, false, "Output quality variables.");
		options.addOption(OPTION_METADATA, false, "Output metadata variables.");
		options.addOption("memory", false, "java heap space.");
		
		Option logOption = new Option("logFile", true, "Send output to file");
		options.addOption(logOption);
		Option outdirOption = new Option("outputdir", true, "Output Directory.");
		options.addOption(outdirOption);

		// 2nd way is by creating an Option and then adding it
		Option timeOption = new Option(OPTION_AVHRR, true, "Process the Level1B file");
		timeOption.setRequired(false);
		options.addOption(timeOption);

		// now lets parse the input
		CommandLineParser parser = new BasicParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException pe) {
			usage(options);
			return;
		}

		// now lets interrogate the options and execute the relevant parts
		if (cmd.hasOption(OPTION_NOGUI)) {
			// process command line args - need to refactor this (doing this
			// twice--BAD!!!!!)
			List<String> list = processCommandLineArgs(cmd);
			String file = cmd.getOptionValue(OPTION_AVHRR);
			if (null == file) {
				System.out.println("You must specify a file to convert!! missing -avhrr argument");
				usage(options);
				return;
			}

			String outdir = cmd.getOptionValue("outputdir");
			if (null == outdir || outdir.trim().length() < 1) {
				File f = new File(".");
				outdir = f.getAbsolutePath();
			}
//			try {
				Avhrr2Netcdf.convert(file, list, outdir);
//			} catch (Exception e) {
//				System.out.println("this blew up");
				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		} else {

			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
					for (int i = 0; i < info.length; i++) {
						// Get the name of the look and feel that is suitable
						// for
						// display to the user
						String className = info[i].getClassName();
					}
					String javaLF = UIManager.getSystemLookAndFeelClassName();
					try {
						// UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
						UIManager.setLookAndFeel(javaLF);
					} catch (ClassNotFoundException e) {
						// log.error(e.getMessage());
					} catch (InstantiationException e) {
						// log.error(e.getMessage());
					} catch (IllegalAccessException e) {
						// log.error(e.getMessage());
					} catch (UnsupportedLookAndFeelException e) {
						// log.error(e.getMessage());
					}
					mFrame = new AvhrrLevel1B2Netcdf();
					mFrame.setVisible(true);
				}
			});
		}
	}

	/**
	 * return an options list based on command line args passed in
	 * 
	 * @param cmd
	 * @return
	 */
	private static List<String> processCommandLineArgs(CommandLine cmd) {
		List<String> list = new ArrayList<String>();

		if (cmd.hasOption(OPTION_CLOBBER)) {
			list.add(OPTION_CLOBBER);
		}

		if (cmd.hasOption(OPTION_ALLVAR)) {
			list.add(OPTION_ALLVAR);
		} else if (cmd.hasOption(OPTION_DEFAULT)) {
			list.add(OPTION_ALLCHAN);
			list.add(OPTION_LATLON);
			list.add(OPTION_TEMP);
		} else {
			if (cmd.hasOption(OPTION_ALLCHAN)) {
				list.add(OPTION_ALLCHAN);
			} else {
				if (cmd.hasOption(OPTION_CH1)) {
					list.add(OPTION_CH1);
				}
				if (cmd.hasOption(OPTION_CH2)) {
					list.add(OPTION_CH2);
				}
				if (cmd.hasOption(OPTION_CH3)) {
					list.add(OPTION_CH3);
				}
				if (cmd.hasOption(OPTION_CH4)) {
					list.add(OPTION_CH4);
				}
				if (cmd.hasOption(OPTION_CH5)) {
					list.add(OPTION_CH5);
				}
			}
			if (cmd.hasOption(OPTION_RAW)) {
				list.add(OPTION_RAW);
			}
			if (cmd.hasOption(OPTION_RADIANCE)) {
				list.add(OPTION_RADIANCE);
			}
			if (cmd.hasOption(OPTION_TEMP)) {
				list.add(OPTION_TEMP);
			}
			if (cmd.hasOption(OPTION_QUALITY)) {
				list.add(OPTION_QUALITY);
			}
			if (cmd.hasOption(OPTION_CALIBRATION)) {
				list.add(OPTION_CALIBRATION);
			}
			if (cmd.hasOption(OPTION_LATLON)) {
				list.add(OPTION_LATLON);
			}
			if (cmd.hasOption(OPTION_METADATA)) {
				list.add(OPTION_METADATA);
			}
		}

		return list;
	}

	/**
	 * 
	 */
	private double updateFileSize() {
		int rows = fm.getRowCount();
		double outsize = 0;
		double fileout = 0;

		for (int i = 0; i < rows; i++) {
			String type = "";
			String satname = (String) fm.getValueAt(i, 3);
			if (satname.equals("NOAA-15") || satname.equals("NOAA-16") || satname.equals("NOAA-17") || satname.equals("NOAA-N")) {
				type = "KLM";
				fileout = DEFAULT_VARS_SIZE_KLM;
			} else if(satname.startsWith("GOES-1 ") || satname.startsWith("GOES-2") || satname.startsWith("GOES-3") || satname.startsWith("GOES-4")
					|| satname.startsWith("GOES-5") || satname.startsWith("GOES-6") || satname.startsWith("GOES-7")) {
				type = "GOES";
				fileout = DEFAULT_VARS_SIZE_GOES;
			} else if(satname.equals("NOAA-6") || satname.equals("NOAA-7") || satname.equals("NOAA-8") || satname.equals("NOAA-9") ||
					satname.equals("NOAA-10") || satname.equals("NOAA-11") || satname.equals("NOAA-12") || satname.equals("NOAA-13") 
					|| satname.equals("NOAA-14") || satname.equals("TIROSN")){
				type = "Ver1";
				fileout = DEFAULT_VARS_SIZE_VER1;
			} else if(satname.startsWith("GOES")){
				type = "GVAR";
				fileout = DEFAULT_VARS_SIZE_GVAR;
			}else{
				type= "GOES-NETCDF";
			}

			if (allVarCheckBox.isSelected()) {
				float origSize = (Float) fm.getValueAt(i, 2);
				if ("Ver1".equals(type)) {
					fileout = (origSize * ALL_VARS_SIZE_VER1 * 1000000);
				} else if("KLM".equals(type)){
					fileout = (origSize * ALL_VARS_SIZE_KLM * 1000000);
				} else if("GOES".equals(type)){
					fileout = (origSize * ALL_VARS_SIZE_GOES * 1000000);
				}else if("GVAR".equals(type)){
					fileout = (origSize * ALL_VARS_SIZE_GVAR * 1000000);
				}else if("GOES-NETCDF".equals(type)){
					fileout=(origSize * ALL_VARS_SIZE_GOES_NETCDF * 1000000);
				}
			} else {

				int scanlines = (Integer) fm.getValueAt(i, 5);
				int pixels = (Integer)fm.getValueAt(i,6);
				// quality flags
				if (qualityCheckBox.isSelected()) {
					if ("KLM".equals(type)) {
						fileout += QUALITY_SIZE_KLM;
					} else if("VER1".equals(type)) {
						fileout += QUALITY_SIZE_VER1;
					} else if("GOES".equals(type)){
						fileout += QUALITY_SIZE_GOES;
					} else if("GVAR".equals(type)){
						fileout =+ QUALITY_SIZE_GVAR;
					} else if("GOES-NETCDF".equals(type)){
						fileout =+ QUALITY_SIZE_GOES_NETCDF;
					}
				}
				// calibartion flag
				if (calCheckBox.isSelected()) {
					if ("KLM".equals(type)) {
						fileout += CALIBRATION_SIZE_KLM;
					} else if("VER1".equals(type)){
						fileout += CALIBRATION_SIZE_VER1;
					} else if("GOES".equals(type)){
						fileout += CALIBRATION_SIZE_GOES;
					} else if("GVAR".equals(type)){
						fileout += CALIBRATION_SIZE_GVAR;
					} else if("GOES-NETCDF".equals(type)){
						fileout += CALIBRATION_SIZE_GOES_NETCDF;
					}
				}

				// latlon
				if (latlonCheckBox.isSelected()) {
					if ("KLM".equals(type) || "VER1".equals(type) ) {
						fileout += LAT_LON_SIZE * scanlines;
					} else if("GOES".equals(type) || "GVAR".equals(type) || "GOES-NETCDF".equals(type)){
						fileout += scanlines * pixels * 7.8;
					}
				}

				// metadata
				if (metaCheckBox.isSelected()) {
					if ("KLM".equals(type)) {
						fileout += METADATA_SIZE_KLM * scanlines;
					} else if("VER1".equals(type)) {
						fileout += METADATA_SIZE_VER1 * scanlines;
					} else if("GOES".equals(type)){
						fileout += METADATA_SIZE_GOES + scanlines * 128;
					} else if("GVAR".equals(type)){
						fileout += 2340 + 628 * scanlines;
					} else if("GOES-NETCDF".equals(type)){
						fileout += METADATA_SIZE_GOES_NETCDF;
					}
				}

				int chans = 0;
				if (chan1CheckBox.isSelected()) {
					chans++;
				}
				if (chan2CheckBox.isSelected()) {
					chans++;
				}
				if (chan3CheckBox.isSelected()) {
					chans++;
					if ("KLM".equals(type)) {
						chans++;
					}
				}
				if (chan4CheckBox.isSelected()) {
					chans++;
				}
				if (chan5CheckBox.isSelected()) {
					chans++;
				}
				if (rawCheckBox.isSelected()) {
					if("GOES".equals(type) || "GVAR".equals(type)){
						fileout += scanlines * pixels * 2;
					}else if("GOES-NETCDF".equals(type)){
						fileout += scanlines * pixels * 3.8;
					}else{
						fileout += (scanlines * 409 * 3.8) * chans;
					}
				}
				if (radCheckBox.isSelected()) {
					if(type.equals("GVAR") || type.equals("GOES") || "GOES-NETCDF".equals(type)){
						fileout += (scanlines * pixels * 3.8);
					}else{
						fileout += (scanlines * 409 * 3.8) * chans;
					}
				}
				if (tempCheckBox.isSelected()) {
					if(type.equals("GVAR") || "GOES".equals(type) || "GOES-NETCDF".equals(type)){
						fileout += scanlines * pixels * 3.8 ;
					}else{
						fileout += (scanlines * 409 * 3.8) * chans;
					}
				}
			}
			fileout = (float) (fileout * 1e-6);
			outsize += fileout;
			fm.setValueAt(fileout, i, 7);
		}

		// update the totoal output size
		updateOutputSize();
		return outsize;
	}

	private void updateOutputSize() {
		double outsize = 0;
		int rows = fm.getRowCount();
		for (int i = 0; i < rows; i++) {
			boolean b = (Boolean) fm.getValueAt(i, 0);
			double f = (Double) fm.getValueAt(i, 7);
			if (b) {
				outsize += f;
			}
		}
		jLabel6.setText("Estimated output size: " + (float) (outsize) + "(MB)");
	}

	/**
	 * prints program options to screen
	 * 
	 * @param options
	 */
	private static void usage(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("AvhrrLevel1B2Netcdf", options);
	}

	public static AvhrrLevel1B2Netcdf getFrame() {
		return mFrame;
	}

	public static void updateLog(String s) {
		// log.info(s);
	}

	static AvhrrLevel1B2Netcdf mFrame;

	private JFileChooser fc, outFc;

	private FileModel fm;

	private Timer activityMonitor;

	private ProgressMonitor progressDialog;

	private NetcdfConverter activity;

	private Vector<File> files;

	private JMenuBar menuBar;

	private JMenu helpMenu;

	private JMenuItem helpMenuItem, aboutMenuItem;

	private JMenu logMenu;

	private JMenuItem logMenuItem;

	JPopupMenu popup;

	JMenuItem removeItem;

	// Log Dialog vars
	private javax.swing.JButton logCloseButton;

	private javax.swing.JDialog logDialog;

	private javax.swing.JScrollPane logScrollPane;

	private javax.swing.JTextArea logTextArea;

	private javax.swing.JButton clearButton;

	// about dialog
	private javax.swing.JDialog aboutDialog;

	private javax.swing.JLabel aboutLabel;

	private javax.swing.JButton aboutButton;

	// help dialog
	private javax.swing.JDialog helpDialog;

	private javax.swing.JButton helpCloseButton;

	private javax.swing.JScrollPane helpScrollPane;

	private javax.swing.JEditorPane helpEditorPane;

	// Variables declaration - do not modify
	private javax.swing.JButton exitButton;

	private javax.swing.JButton outdirButton;

	private javax.swing.JButton convertButton;

	private javax.swing.JButton jButton4;

	private javax.swing.JCheckBox allChanCheckBox;

	private javax.swing.JCheckBox allVarCheckBox;

	private javax.swing.JCheckBox qualityCheckBox;

	private javax.swing.JCheckBox calCheckBox;

	private javax.swing.JCheckBox latlonCheckBox;

	private javax.swing.JCheckBox metaCheckBox;

	private javax.swing.JCheckBox chan1CheckBox;

	private javax.swing.JCheckBox chan2CheckBox;

	private javax.swing.JCheckBox chan3CheckBox;

	private javax.swing.JCheckBox chan4CheckBox;

	private javax.swing.JCheckBox chan5CheckBox;

	private javax.swing.JCheckBox rawCheckBox;

	private javax.swing.JCheckBox radCheckBox;

	private javax.swing.JCheckBox tempCheckBox;

	private javax.swing.JLabel jLabel1;

	private javax.swing.JLabel jLabel2;

	private javax.swing.JLabel jLabel3;

	private javax.swing.JLabel jLabel4;

	private javax.swing.JLabel jLabel5;

	private javax.swing.JPanel jPanel1;

	private javax.swing.JScrollPane jScrollPane1;

	private javax.swing.JTable jTable1;

	private javax.swing.JTextField outdirText;

	private javax.swing.JButton openButton;

	private javax.swing.JLabel jLabel6;

	// End of variables declaration

	public javax.swing.JTextArea getLogTextArea() {
		return logTextArea;
	}

    private void setTableCellRenderer(JTable table, TableCellRenderer renderer) {
        TableColumnModel columnModel = table.getColumnModel();
        int columnCount = columnModel.getColumnCount();

        for (int i = 0; i < columnCount; i++) {
            TableColumn column = columnModel.getColumn(i);
            
            if(i == 0){
            	column.setMaxWidth(90);
            }else{
            	if(i > 1){
            		column.setMaxWidth(90);
            	}
            	if(i == (columnCount -1)){
            		column.setMaxWidth(0);
            	}
            	if(i == (columnCount -2)){
            		column.setMaxWidth(150);
            	}
            	column.setCellRenderer(renderer);
            }
        }
    }

	public javax.swing.JTable getJTable1() {
		return jTable1;
	}
	
	public int getRowForFile(String filename){
		int row = 0;
		FileModel fm = (FileModel)jTable1.getModel();
		int rows = jTable1.getRowCount();
		for(int i=0;i<rows;i++){
			File f = fm.getFileAtRow(i);
			if(filename.equals(f.getName())){
				return i;
			}
		}
	
		return row;
	}
}
