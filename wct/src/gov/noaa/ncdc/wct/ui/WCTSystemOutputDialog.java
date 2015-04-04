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


package gov.noaa.ncdc.wct.ui;

import java.awt.Frame;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class WCTSystemOutputDialog extends WCTTextDialog {


	private static final long serialVersionUID = -2739585931868989360L;

	
	private static WCTSystemOutputDialog sharedDialog = null;
	
	/**
	 * 
	 * @param parent -- only applies the first time this is called
	 * @return
	 */
	public static WCTSystemOutputDialog getSharedSystemOutputDialog(Frame parent) {
		if (sharedDialog == null) {
			sharedDialog = new WCTSystemOutputDialog(parent, "WCT Output Log\n\n", "WCT Output Log", true);
		}
		return sharedDialog;
	}
	
	
	

	private WCTSystemOutputDialog(Frame parent, String initialText,
			String title, boolean visible) {

		super(parent, initialText, title, visible);

		redirectSystemStreams();
	}


	private void updateTextPane(final String text) {
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Document doc = getTextArea().getDocument();
				try {
					doc.insertString(doc.getLength(), text, null);
					
					getTextArea().setCaretPosition(getTextArea().getDocument().getLength());
					
				} catch (BadLocationException e) {
					throw new RuntimeException(e);
				}
				
//				getTextArea().setCaretPosition(doc.getLength() - text.length() - 1);
			}
		});
	}

	private void redirectSystemStreams() {
		OutputStream out = new OutputStream() {
			@Override
			public void write(final int b) throws IOException {
				updateTextPane(String.valueOf((char) b));
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				updateTextPane(new String(b, off, len));
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};

		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));
	}



}
