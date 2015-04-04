package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.common.DList;
import gov.noaa.ncdc.common.DragListener;
import gov.noaa.ncdc.common.RiverLayout;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

public class BookmarkUI extends JDialog {

	private static final long serialVersionUID = -8007691275867370676L;

	private WCTViewer viewer;
	private final DefaultListModel listModel = new DefaultListModel();
	private final DList bmList = new DList(listModel);

	public BookmarkUI(WCTViewer viewer) {
		super(viewer, "Bookmark Editor", false);

		this.viewer = viewer;

		createGUI();        
	}


	private void createGUI() {

		// SwingLabs configuration of JXList
		bmList.addHighlighter(new ColorHighlighter(HighlightPredicate.ROLLOVER_ROW, 
				new Color(240, 240, 220), Color.BLACK));
		bmList.setRolloverEnabled(true);


		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new RiverLayout());


		bmList.addDragListener(new DragListener() {
//			@Override
			public void dropComplete() {

				System.out.println("DROP COMPLETE");
				syncListModelToBookmarks(BookmarkManager.getInstance().getBookmarks());
				try {
					BookmarkManager.getInstance().saveObjectData();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}			
		});


		Vector<Bookmark> bookmarks = BookmarkManager.getInstance().getBookmarks();
//		bmList.setListData(bookmarks);
		syncListModelFromBookmarks(bookmarks);
		bmList.addMouseListener(new MouseAdapter() {
//			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
					viewer.setCurrentExtent(BookmarkManager.getInstance().getBookmarks().get(bmList.getSelectedIndex()).getExtent());
				}
			}
		});
		if (bookmarks.size() > 0) {
			bmList.setSelectedIndex(0);
		}
//		bmList.setSize(400, 80);


		JButton viewButton = new JButton("View");
		viewButton.addActionListener(new ActionListener() {
//			@Override
			public void actionPerformed(ActionEvent e) {
				viewer.setCurrentExtent(BookmarkManager.getInstance().getBookmarks().get(bmList.getSelectedIndex()).getExtent());
			}
		});
		JButton addButton = new JButton("Add");
		addButton.addActionListener(new ActionListener() {
//			@Override
			public void actionPerformed(ActionEvent e) {	
				addBookmark();
			}
		});
		JButton editButton = new JButton("Edit");
		editButton.addActionListener(new ActionListener() {
//			@Override
			public void actionPerformed(ActionEvent e) {
				Vector<Bookmark> bookmarks = BookmarkManager.getInstance().getBookmarks();
				int index = bmList.getSelectedIndex();
				Bookmark b = bookmarks.get(index);
				b.setTitle(getBookmark().getTitle());
				BookmarkManager.getInstance().replaceBookmark(index, b);
//				bmList.setListData(BookmarkManager.getInstance().getBookmarks());
				syncListModelFromBookmarks(BookmarkManager.getInstance().getBookmarks());
				bmList.setSelectedIndex(index);
			}
		});
		JButton rmButton = new JButton("Remove");
		rmButton.addActionListener(new ActionListener() {
//			@Override
			public void actionPerformed(ActionEvent e) {
				int index = bmList.getSelectedIndex();
				BookmarkManager.getInstance().removeBookmark(index);
//				bmList.setListData(BookmarkManager.getInstance().getBookmarks());
				syncListModelFromBookmarks(BookmarkManager.getInstance().getBookmarks());
				if (index > 0) {
					bmList.setSelectedIndex(index-1);
				}
				else {
					bmList.setSelectedIndex(0);
				}
			}
		});

		mainPanel.add(new JLabel("Bookmarks of View Extents"), "center br");
        mainPanel.add(new JLabel("(click 'Add' to bookmark the current view extent)"), "center br");
		mainPanel.add(new JScrollPane(bmList), "center p hfill vfill");
		mainPanel.add(viewButton, "center br");
		mainPanel.add(addButton);
		mainPanel.add(editButton);
		mainPanel.add(rmButton);

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

	private void syncListModelFromBookmarks(Vector<Bookmark> bookmarks) {
		listModel.clear();
//		listModel.setSize(bookmarks.size());
		for (int n=0; n<bookmarks.size(); n++) {
			if (bookmarks.get(n) != null) {
				listModel.addElement(bookmarks.get(n));
			}
//			listModel.add(n, bookmarks.get(n));
//			System.out.println("syncListModelFromBookmarks: ["+n+"] "+bookmarks.get(n));
		}
	}
	private void syncListModelToBookmarks(Vector<Bookmark> bookmarks) {
		bookmarks.clear();
//		bookmarks.setSize(listModel.size());
		for (int n=0; n<listModel.size(); n++) {
			if (listModel.get(n) != null) {
				bookmarks.addElement((Bookmark)listModel.get(n));
			}
//			bookmarks.add(n, (Bookmark)listModel.get(n));
//			System.out.println("syncListModelToBookmarks: ["+n+"] "+listModel.get(n));
		}
	}


	public Bookmark getBookmark() {
		String title = JOptionPane.showInputDialog(viewer, "Enter Bookmark Name");

		if (title == null) {
			return null;
		}

		if (viewer == null) {
			return new Bookmark(title+"  VIEWER IS NULL", null);
		}

		Bookmark b = new Bookmark(title, viewer.getCurrentExtent());
		return b;
	}

	public void addBookmark() {
		Bookmark b = getBookmark();
		if (b == null) {
			return;
		}
		BookmarkManager.getInstance().addBookmark(b);
//		bmList.setListData(BookmarkManager.getInstance().getBookmarks());
		syncListModelFromBookmarks(BookmarkManager.getInstance().getBookmarks());
		bmList.setSelectedIndex(BookmarkManager.getInstance().getBookmarks().size()-1);
	}

}
