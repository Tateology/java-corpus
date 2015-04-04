package steve.test.grass;

import gov.noaa.ncdc.common.RiverLayout;
import gov.noaa.ncdc.wct.ui.WCTTextPanel;
import gov.noaa.ncdc.wct.ui.WCTTextPanel.SearchBarProperties;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.xswingx.PromptSupport;
import org.jdesktop.xswingx.PromptSupport.FocusBehavior;

import steve.test.grass.GRASSUtils.OutputListener;

public class GEGrassCommandPanel extends JPanel {
	private static final long serialVersionUID = -3602745292525814103L;

	private GRASSEarthTest parent = null;
	
	private JTextField commandTextField = new JTextField(30);
	private WCTTextPanel outputTextPanel = null;
	private ArrayList<String> commandHistory = new ArrayList<String>();
	
	public GEGrassCommandPanel(GRASSEarthTest parent) {
		this.parent = parent;
		init();
	}
	
	
	private void init() {
		
		this.setLayout(new BorderLayout());
		
		outputTextPanel = new WCTTextPanel(parent, "", SearchBarProperties.SEARCH_BAR_COMPACT);
		
		outputTextPanel.setPreferredSize(new Dimension((int)outputTextPanel.getPreferredSize().getWidth(), 500));
		
		outputTextPanel.getTextArea().setFont(Font.decode(Font.MONOSPACED));		
		PromptSupport.setPrompt("Enter GRASS Command and press [Enter] (or [Up]/[Down] for history)", commandTextField);
		PromptSupport.setFocusBehavior(FocusBehavior.SHOW_PROMPT, commandTextField);
		PromptSupport.setPrompt("Command Output (Ctrl-F for Search)", outputTextPanel.getTextArea());
		PromptSupport.setFocusBehavior(FocusBehavior.SHOW_PROMPT, outputTextPanel.getTextArea());
		
		
		commandTextField.addKeyListener(new CommandKeyListener());
		
//		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		JPanel tpanel = new JPanel(new RiverLayout());
		tpanel.add(commandTextField, "hfill br");
		tpanel.add(outputTextPanel, "p vfill hfill");

//		splitPane.setTopComponent(commandTextField);
		

//		JPanel bpanel = new JPanel(new BorderLayout());
//		bpanel.add(new JScrollPane(outputTextArea), BorderLayout.CENTER);
//		splitPane.setBottomComponent(bpanel);
	
//		this.add(splitPane, BorderLayout.CENTER);
		
		
//		this.add(commandTextField, BorderLayout.NORTH);
//		this.add(outputTextPanel, BorderLayout.CENTER);
		this.add(tpanel);

		
		
		try {
			loadCommandListFromCache(commandHistory);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void executeCommand(String command, boolean addToHistory) {
		commandTextField.setText(command);
		executeCommand(addToHistory);
	}
	
	public void executeCommand() {
		executeCommand(true);
	}
	
	public void executeCommand(boolean addToHistory) {
		String command = commandTextField.getText();
		if (command.trim().length() == 0) {
			return;
		}

		int length = outputTextPanel.getTextArea().getDocument().getLength();
		int pad = 65;
		if (command.length() > pad) {
			pad = command.length();
		}
		outputTextPanel.getTextArea().append(StringUtils.rightPad(command, pad)+"\n");
		try {
			outputTextPanel.getTextArea().getHighlighter().addHighlight(length, length+pad, 
					new DefaultHighlighter.DefaultHighlightPainter(new Color(220, 220, 200)));
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		

		
		parent.getGrassUtils().executeGRASS(command, new OutputListener() {
			@Override
			public void reportOutput(String c) {
				outputTextPanel.getTextArea().append(c);
			}
		});;
		
		if (command.startsWith("d.")) {
			parent.refreshView(true);
		}
		
		outputTextPanel.getTextArea().setCaretPosition(outputTextPanel.getTextArea().getDocument().getLength());

		// ignore repeat commands
		if (addToHistory && (
				commandHistory.size() == 0 || ! command.equals(commandHistory.get(commandHistory.size()-1)))) {
			commandHistory.add(command);
			try {
				syncCommandListToCache();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		commandTextField.setText("");
	}
	
	private void syncCommandListToCache() throws IOException {
		File commandHistoryFile = new File(GRASSUtils.GEGRASS_CACHE_DIR.toString()+File.separator+"commandHistory.txt");
		FileUtils.writeLines(commandHistoryFile, commandHistory);
	}
	
	private void loadCommandListFromCache(ArrayList<String> commandListToPopulate) throws IOException {
		File commandHistoryFile = new File(GRASSUtils.GEGRASS_CACHE_DIR.toString()+File.separator+"commandHistory.txt");
		if (commandHistoryFile.exists()) {
			List<String> commandList = FileUtils.readLines(commandHistoryFile);
			for (String s : commandList) {
				commandListToPopulate.add(s);
			}
		}
	}
	
	
	private class CommandKeyListener implements KeyListener {

		private int historyIndex = 0;
		
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_UP) {
				if (commandHistory.size() > 0 && historyIndex < commandHistory.size()) {
					int index = commandHistory.size()-(++historyIndex);					
					commandTextField.setText(commandHistory.get(index));
				}
			}
			if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				if (commandHistory.size() > 0 && historyIndex > 1) {
					int index = commandHistory.size()-(--historyIndex);
					commandTextField.setText(commandHistory.get(index));
				}
			}
		}
		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				
		        SwingUtilities.invokeLater(new Runnable() {
		            public void run() {
						System.out.println("executing: "+commandTextField.getText());
						executeCommand();
						historyIndex = 0;
		            }
		        });
			}

		}
		@Override
		public void keyTyped(KeyEvent e) {		
		}		
	}
	
	
}
