/**
 * Copyright (C) 2007 Rene M.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * Created on 06.04.2007
 */


package freecs.util;


import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;

public class FadeColor {	
	/**
	public static void main(String[] args) {
		getFadeColorUsername("FF0000", "66FF99", "ijfiewjrfijfirefiowhuwef");
	}*/
	public static StringBuffer getFadeColorUsername(String stcolor, String ecolor, String name){
		if (stcolor == null || ecolor == null || name.length()<3)
			return null;
		int steps = name.length();
		Color startcolor = new Color( Integer.parseInt(stcolor, 16));
	    int r_start = startcolor.getRed();
		int g_start = startcolor.getGreen();
		int b_start = startcolor.getBlue();
	
		Color endcolor = new Color( Integer.parseInt(ecolor, 16));
	    int r_end = endcolor.getRed();
		int g_end = endcolor.getGreen();
		int b_end = endcolor.getBlue();
		
		int diff_r = r_start - r_end;
		int diff_g = g_start - g_end;
		int diff_b = b_start - b_end;

        int step_r = (diff_r/(steps-1))*-1;
        int step_g = (diff_g/(steps-1))*-1;
        int step_b = (diff_b/(steps-1))*-1;
        
        ArrayList<StringBuffer> fadecolor = new ArrayList<StringBuffer>();
        int r_temp = r_start;
        int g_temp = g_start;
        int b_temp = b_start;

        for (int i=1;i<steps-1;i++){
        	r_temp=r_temp+step_r;
        	g_temp=g_temp+step_g;
        	b_temp=b_temp+step_b;
         	fadecolor.add(rgbToHex(r_temp,g_temp,b_temp));
        }
        StringBuffer fadeUsername = new StringBuffer(generateFadeUsername(stcolor, ecolor, name, fadecolor));
		return fadeUsername;
	}
	
	private static StringBuffer rgbToHex(int r, int g, int b){      
        StringBuffer red   = new StringBuffer(Integer.toHexString(r));
        StringBuffer green = new StringBuffer(Integer.toHexString(g));
        StringBuffer blue  = new StringBuffer(Integer.toHexString(b));

        if (red.toString().length()   != 2 ) { red   = new StringBuffer("0").append(red);   }
        if (green.length() != 2 ) { green = new StringBuffer("0").append(green); }
        if (blue.length()  != 2 ) { blue  = new StringBuffer("0").append(blue);  }       
		StringBuffer webcolor= new StringBuffer(red).append(green).append(blue);
		return webcolor;
	}
	
	private static StringBuffer generateFadeUsername(String stcolor,String ecolor,String name, ArrayList<StringBuffer> fadecolor){
		String     un[] = new String[name.length()+1];
		for(int i=0;i<name.length();i++){
			un[i]= EntityDecoder.charToHtml(name.substring(i,i+1));
		}
		StringBuffer link = new StringBuffer();
        int l=0;
        link.append("<span style=\"color:#");
        link.append(stcolor).append("\">");
    	link.append(un[l]);
    	link.append("</span>");
        for (Iterator<StringBuffer> i =fadecolor.iterator();i.hasNext();){
        	l++;
        	link.append("<span style=\"color:#");
        	StringBuffer color =  (StringBuffer) i.next();
        	link.append(color).append("\">");
        	link.append(un[l]);
        	link.append("</span>");
        }
        l++;
        link.append("<span style=\"color:#");
        link.append(ecolor).append("\">");
    	link.append(un[l]);
    	link.append("</span>");
        return link;
	}
}