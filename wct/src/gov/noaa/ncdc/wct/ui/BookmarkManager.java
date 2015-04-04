package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.wct.WCTConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

public class BookmarkManager {
	
	private static BookmarkManager manager;
	private Vector<Bookmark> bookmarkList = new Vector<Bookmark>();
	private File objFile = new File(WCTConstants.getInstance().getCacheLocation()+File.separator+
			"objdata"+File.separator+this.getClass().getName()+".data");
	
	// don't use this, use singleton factory method instead
	private BookmarkManager() {
		try {
			loadObjectData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static BookmarkManager getInstance() {
		if (manager == null) {
			manager = new BookmarkManager();
		}
		return manager;
	}
	
	public void addBookmark(Bookmark bookmark) {
		bookmarkList.add(bookmark);
		try {
			saveObjectData();
		} catch (Exception e) {
			System.err.println("UNABLE TO SAVE BOOKMARK LIST OBJECT TO: "+objFile);
		}
	}
	
	public void removeBookmark(int index) {
		bookmarkList.remove(index);
		try {
			saveObjectData();
		} catch (Exception e) {
			System.err.println("UNABLE TO SAVE BOOKMARK LIST OBJECT TO: "+objFile);
		}
	}
	
	public void replaceBookmark(int index, Bookmark bookmark) {
		bookmarkList.set(index, bookmark);
		try {
			saveObjectData();
		} catch (Exception e) {
			System.err.println("UNABLE TO SAVE BOOKMARK LIST OBJECT TO: "+objFile);
		}
	}
	
	public Vector<Bookmark> getBookmarks() {
		return bookmarkList;
	}
	
	
	
	public void loadObjectData() throws IOException, ClassNotFoundException {

		System.out.println("LOADING FROM: "+objFile);
		
		if (! objFile.exists()) {
			System.out.println("NO BOOKMARK LIST OBJECT EXISTS...");
		}
		else {
			// Read from disk using FileInputStream.
			FileInputStream fin = new FileInputStream (objFile);

			// Read object using ObjectInputStream.
			ObjectInputStream objIn = new ObjectInputStream (fin);

			// Read an object.
			Object obj = objIn.readObject();

			// Is the object that you read in, say, an instance
			// of the Vector class?
			if (obj instanceof Vector) {
			  // Cast object to a Vector
			  bookmarkList = (Vector<Bookmark>) obj;
			}
			else {
				System.err.println("COULD NOT LOAD BOOKMARK LIST OBJECT\n" +
						objFile+" IS OF TYPE: "+obj.toString());
				
			}
			objIn.close();
			fin.close();
		}
	}
	
	public void saveObjectData() throws IOException {
		// create dir if needed
		if (! objFile.getParentFile().exists()) {
			objFile.getParentFile().mkdirs();
		}
		
		// Use a FileOutputStream to send data to a file
		FileOutputStream fout = new FileOutputStream (objFile);

		// Use an ObjectOutputStream to send object data to the
		// FileOutputStream for writing to disk.
		ObjectOutputStream objOut = new ObjectOutputStream(fout);

		// Pass our object to the ObjectOutputStream's
		// writeObject() method to cause it to be written out
		// to disk.
		objOut.writeObject(bookmarkList);
		
		objOut.close();
		fout.close();
	}

}
