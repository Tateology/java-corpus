package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.wct.WCTConstants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class FavoritesManager {

	private static FavoritesManager manager;
	private ArrayList<Favorite> favoriteList = new ArrayList<Favorite>();

	private File objFile = new File(WCTConstants.getInstance().getCacheLocation()+File.separator+
			"objdata"+File.separator+"current-favorites-list.tsv");
	
	public enum LoadAction { APPEND, REPLACE };
	
	// don't use this, use singleton factory method instead
	private FavoritesManager() {
//		try {
//			loadCurrentFavoritesFromProfile();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
	public static FavoritesManager getInstance() {
		if (manager == null) {
			manager = new FavoritesManager();
		}
		return manager;
	}


	/**
	 * Load the current favorites from the file $user.home/.wct-cache/objdata/current-favorites-list.tsv .  
	 * The file format is a tab separated file of $url <tab> $description .  
	 * Default behavior is to replace to existing favorites list.
	 * @throws IOException
	 */
	public void loadCurrentFavoritesFromProfile() throws IOException {
		loadFavorites(objFile, LoadAction.REPLACE);
	}

	/**
	 * Load a favorites file.  
	 * The file format is a tab separated file of $url <tab> $description
	 * @param infile
	 * @param loadAction Either APPEND or REPLACE already loaded favorites.
	 * @throws IOException
	 */
	public void loadFavorites(File infile, LoadAction loadAction) throws IOException {
		
		if (! infile.exists()) {
			return;
		}

		if (loadAction == LoadAction.REPLACE) {
			favoriteList.clear();
		}
		
		String line;
		BufferedReader br = new BufferedReader(new FileReader(infile));
		while ( (line = br.readLine()) != null) {
			String[] cols = line.split("\t");
			if (cols.length != 2) {
				continue;
			}
			
			if (cols[0].trim().equals("null")) {
				continue;
			}
			
			Favorite fav = new Favorite();
			fav.setDataURL(new URL(cols[0]));
			fav.setDisplayString(cols[1]);
			
//			System.out.println("adding favorite: "+fav);
			
			favoriteList.add(fav);
		}
	}

	public ArrayList<Favorite> getCurrentFavoriteList() {
		return favoriteList;
	}
	
	/**
	 * Save the current favorites to the file $user.home/.wct-cache/objdata/current-favorites-list.tsv .  
	 * The file format is a tab separated file of $url <tab> $description
	 * @throws IOException
	 */
	public void saveCurrentFavoritesToProfile() throws IOException {
		saveFavorites(objFile);
	}

	/**
	 * The file format is a tab separated file of $url <tab> $description
	 * @param outfile
	 * @throws IOException
	 */
	public void saveFavorites(File outfile) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
		for (Favorite f : favoriteList) {
			bw.write(f.getDataURL()+"\t"+f.getDisplayString());
			bw.newLine();
		}
		bw.close();
	}
}	
	
