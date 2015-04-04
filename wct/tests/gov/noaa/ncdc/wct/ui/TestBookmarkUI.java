package gov.noaa.ncdc.wct.ui;

import javax.swing.JFrame;

public class TestBookmarkUI {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		JFrame frame = new JFrame();
		frame.setSize(100, 100);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		BookmarkManager bm = BookmarkManager.getInstance();
		bm.addBookmark(new Bookmark("Test", new java.awt.geom.Rectangle2D.Double(-90, 40, 5, 5)));
		
		BookmarkUI bui = new BookmarkUI(null);
		bui.setSize(300, 490);
		bui.setVisible(true);
		

	}

}
