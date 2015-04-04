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

package gov.noaa.ncdc.nexradiv;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class OpenFileFilter extends FileFilter {

    private String ext;
    private boolean directories;
    private String description;

    // Constructor
    public OpenFileFilter(String ext, boolean directories, String description) {
       this.ext = ext;
       this.directories = directories;
       this.description = description;
    }


    public boolean accept(File f) {
        // Accept or reject directories
        if (f.isDirectory()) {
            return directories;
        }

        // Extract extension
        String extension = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 &&  i < s.length() - 1) {
            extension = s.substring(i+1).toLowerCase();
        }
        // Apply filter
        if (extension != null) {
           if (extension.equals(ext))
              return true;
           else 
              return false;
        }

        return false;
    }

    //The description of this filter
    public String getDescription() {
        return description;
    }
}

