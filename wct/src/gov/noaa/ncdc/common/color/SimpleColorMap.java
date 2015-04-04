/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package gov.noaa.ncdc.common.color;

import java.awt.Color;

/**
 * Maps numerical levels to colors using either a lookup table, color interpolation, or both.
 * A simple implementation of the ColorMap interface.
 *
 * <ol>
 * <li> Method 1: a color table.  The user can provide an array of Colors; if the numerical value,
 * cast into an integer, is between 0 and (the length of this array - 1), then the appropriate Color is returned.
 *
 * <li> Method 2: color interpolation.  The user can provide a min-level, min-Color, max-level, and max-Color.
 * If the numerical value is below min-level, then minColor is provided.  If it's above max-level, then max-Color
 * is provided.  If it's between min-level and max-level, then a linear interpolation between min-Color and
 * max-Color is provided.
 * </ol>
 *
 * <p>The user can provide both a color table <i>and</i> an interpolation; in this case, the color table takes
 * precedence over the interpolation in that region where the color table is relevant.  You specify a color
 * table with setColorTable(), and you specify an interpolation range with setLevels().
 *
 * <p>validLevel() is set to return true if the level range is between min-level and max-level, or
 * if it's inside the color table range.
 *
 * <p>defaultValue() is set to return 0 if the color table exists, else min-level is provided.
 *
 * @author Sean Luke
 * 
 */

// Color interpolation relies on a cache so we don't make billions of colors.  The slightly slower cache,
// turned on by default, lets us search to a bucket, then wander through the bucket to find the right Color.
// The faster cache creates a hard-set Color array.  This has lower resolution in colors (probably 1/4 the
// true Color values) than the slower cache, but it's faster in lookup by a bit.  I've commented out the
// faster cache, but you can see how it roughly works.  


public class SimpleColorMap implements ColorMap 
    {
    public int minRed = 0;
    public int minBlue = 0;
    public int minGreen = 0;
    public int minAlpha = 0;
    public int maxRed = 0;
    public int maxBlue = 0;
    public int maxGreen = 0;
    public int maxAlpha = 0;
    public double maxLevel = 0;
    public double minLevel = 0;
    public final Color clearColor = new Color(0,0,0,0);
    public Color minColor = clearColor;  // used when minLevel = maxLevel
    
    public static final int COLOR_DISCRETIZATION = 257;
    
    /** User-provided color table */
    public Color[] colors;
    
    //      (The slower cache for color interpolation)
    Bag[] colorCache = new Bag[COLOR_DISCRETIZATION];

    //      (The faster cache)
    //      Color[] colorCache2 = new Color[COLOR_DISCRETIZATION];  
        
    public SimpleColorMap()
        {
        setLevels(0,1,Color.black,Color.white);
        }
    
    public SimpleColorMap(double minLevel, double maxLevel, Color minColor, Color maxColor)
        {
        setLevels(minLevel,maxLevel,minColor,maxColor);
        }
    
    public SimpleColorMap(Color[] colorTable)
        {
        setColorTable(colorTable);
        }
        
    public SimpleColorMap(Color[] colorTable, double minLevel, double maxLevel, Color minColor, Color maxColor)
        {
        setColorTable(colorTable);
        setLevels(minLevel,maxLevel,minColor,maxColor);
        }

    /** Sets the color levels for the ValueGridPortrayal2D values for use by the default getColor(...)
        method.  These are overridden by any array provided in setColorTable().  If the value in the IntGrid2D or DoubleGrid2D
        is less than or equal to minLevel, then minColor is used.  If the value is greater than or equal to maxColor, then
        maxColor is used.  Otherwise a linear interpolation from minColor to maxColor is used. */
    public void setLevels(double minLevel, double maxLevel, Color minColor, Color maxColor)
        {
        if (maxLevel < minLevel) throw new RuntimeException("maxLevel cannot be less than minLevel");
        minRed = minColor.getRed(); minGreen = minColor.getGreen(); minBlue = minColor.getBlue(); minAlpha = minColor.getAlpha();
        maxRed = maxColor.getRed(); maxGreen = maxColor.getGreen(); maxBlue = maxColor.getBlue(); maxAlpha = maxColor.getAlpha();
        this.maxLevel = maxLevel; this.minLevel = minLevel;
        this.minColor = minColor;

        // reset cache
        // (the slower cache)
        for(int x=0;x<COLOR_DISCRETIZATION;x++) colorCache[x] = new Bag();
                
        // (the faster cache)
        //              for(int x=0;x<COLOR_DISCRETIZATION;x++) 
        //                      {
        //                      colorCache2[x] =
        //                      new Color( x * (maxColor.getRed()-minColor.getRed()) / (COLOR_DISCRETIZATION-1) + minColor.getRed(), 
        //                              x * (maxColor.getGreen()-minColor.getGreen()) / (COLOR_DISCRETIZATION-1) + minColor.getGreen(), 
        //                              x * (maxColor.getBlue()-minColor.getBlue()) / (COLOR_DISCRETIZATION-1) + minColor.getBlue(), 
        //                              x * (maxColor.getAlpha()-minColor.getAlpha()) / (COLOR_DISCRETIZATION-1) + minColor.getAlpha());
        //                      }
        }
        
    /** Specifies that if a value (cast into an int) in the IntGrid2D or DoubleGrid2D falls in the range 0 ... colors.length,
        then that index in the colors table should be used to represent that value.  Otherwise, values in
        setLevels(...) are used.  You can remove the color table by passing in null here.  Returns the old color table. */
    public Color[] setColorTable(Color[] colorTable)
        {
        Color[] retval = colors;
        colors = colorTable;
        return retval;
        }
        
    /** Override this if you'd like to customize the color for values in the portrayal.  The default version
        looks up the value in the colors[] table, else computes the interpolated color and grabs it out of
        a predefined color cache (there can't be more than about 1024 or so interpolated colors, max). 
    */
    
    public Color getColor(double level)
        {
        if (colors != null && level >= 0 && level < colors.length)
            {
            return colors[(int)level];
            }
        else
            {
            if (level > maxLevel) level = maxLevel;
            else if (level < minLevel) level = minLevel;
            if (level == minLevel) return minColor;  // so we don't divide by zero (maxLevel - minLevel)
            
            final double interpolation = (level - minLevel) / (maxLevel - minLevel);
                        
            // look up color in cache
            // (the slower cache)
            final int alpha = (maxAlpha == minAlpha ? minAlpha : (int)(interpolation * (maxAlpha - minAlpha) + minAlpha));
            if (alpha==0) return clearColor;
            final int red = (maxRed == minRed ? minRed : (int)(interpolation * (maxRed - minRed) + minRed));
            final int green = (maxGreen == minGreen ? minGreen : (int)(interpolation * (maxGreen - minGreen) + minGreen));
            final int blue = (maxBlue == minBlue ? minBlue : (int)(interpolation * (maxBlue - minBlue) + minBlue));
            final int rgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
            Bag colors = colorCache[(int)(interpolation * (COLOR_DISCRETIZATION-1))];
            for(int x=0;x<colors.numObjs;x++)
                {
                Color c = (Color)(colors.objs[x]);
                if (c.getRGB()==rgb)  // it's the right color
                    return c;
                }
            Color c = new Color(rgb,(alpha!=0));
            colors.add(c);
            return c;
                        
            // (the faster cache)
            //                      return colorColorCache2[(int)(interpolation * (COLOR_DISCRETIZATION-1))];
            }
        }

    public int getAlpha(double level)
        {
        if (colors != null)
            {
            if (level >= 0 && level < colors.length)
                return colors[(int)level].getAlpha();
            }

        // else...
            
        if (level > maxLevel) level = maxLevel;
        else if (level < minLevel) level = minLevel;
        if (level == minLevel) return minColor.getAlpha();
            
        final double interpolation = (level - minLevel) / (maxLevel - minLevel);

        final int maxAlpha = this.maxAlpha;
        final int minAlpha = this.minAlpha;
        return (maxAlpha == minAlpha ? minAlpha : (int)(interpolation * (maxAlpha - minAlpha) + minAlpha));
        }
                
    public int getRGB(double level)
        {
        if (colors != null)
            {
            if (level >= 0 && level < colors.length)
                return colors[(int)level].getRGB();
            }
            
        // else...
            
        if (level > maxLevel) level = maxLevel;
        else if (level < minLevel) level = minLevel;
        if (level == minLevel) return minColor.getRGB();
            
        final double interpolation = (level - minLevel) / (maxLevel - minLevel);

        final int maxAlpha = this.maxAlpha;
        final int minAlpha = this.minAlpha;
        final int alpha = (maxAlpha == minAlpha ? minAlpha : (int)(interpolation * (maxAlpha - minAlpha) + minAlpha));
        if (alpha==0) return 0;

        final int maxRed = this.maxRed;
        final int minRed = this.minRed;
        final int maxGreen = this.maxGreen;
        final int minGreen = this.minGreen;
        final int maxBlue = this.maxBlue;
        final int minBlue = this.minBlue;
        final int red = (maxRed == minRed ? minRed : (int)(interpolation * (maxRed - minRed) + minRed));
        final int green = (maxGreen == minGreen ? minGreen : (int)(interpolation * (maxGreen - minGreen) + minGreen));
        final int blue = (maxBlue == minBlue ? minBlue : (int)(interpolation * (maxBlue - minBlue) + minBlue));
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
        }

    public boolean validLevel(double value)
        {
        if (colors!=null && value >= 0 && value < colors.length)
            return true;
        if (value <= maxLevel && value >= minLevel)
            return true;
        return false;
        }
        
    public double defaultValue()
        {
        if (colors != null) return 0;
        return minLevel;
        }
    }








/*
Academic Free License ("AFL") v. 3.0

This Academic Free License (the "License") applies to any original work
of authorship (the "Original Work") whose owner (the "Licensor") has
placed the following licensing notice adjacent to the copyright notice
for the Original Work:

Licensed under the Academic Free License version 3.0

1) Grant of Copyright License. Licensor grants You a worldwide,
royalty-free, non-exclusive, sublicensable license, for the duration of
the copyright, to do the following:

    a) to reproduce the Original Work in copies, either alone or as
       part of a collective work;

    b) to translate, adapt, alter, transform, modify, or arrange the
       Original Work, thereby creating derivative works ("Derivative
       Works") based upon the Original Work;

    c) to distribute or communicate copies of the Original Work and
       Derivative Works to the public, UNDER ANY LICENSE OF YOUR
       CHOICE THAT DOES NOT CONTRADICT THE TERMS AND CONDITIONS,
       INCLUDING LICENSOR'S RESERVED RIGHTS AND REMEDIES, IN THIS
       ACADEMIC FREE LICENSE;

    d) to perform the Original Work publicly; and

    e) to display the Original Work publicly.

2) Grant of Patent License. Licensor grants You a worldwide,
royalty-free, non-exclusive, sublicensable license, under patent claims
owned or controlled by the Licensor that are embodied in the Original
Work as furnished by the Licensor, for the duration of the patents, to
make, use, sell, offer for sale, have made, and import the Original Work
and Derivative Works.

3) Grant of Source Code License. The term "Source Code" means the
preferred form of the Original Work for making modifications to it and
all available documentation describing how to modify the Original Work.
Licensor agrees to provide a machine-readable copy of the Source Code of
the Original Work along with each copy of the Original Work that
Licensor distributes. Licensor reserves the right to satisfy this
obligation by placing a machine-readable copy of the Source Code in an
information repository reasonably calculated to permit inexpensive and
convenient access by You for as long as Licensor continues to distribute
the Original Work.

4) Exclusions From License Grant. Neither the names of Licensor, nor the
names of any contributors to the Original Work, nor any of their
trademarks or service marks, may be used to endorse or promote products
derived from this Original Work without express prior permission of the
Licensor. Except as expressly stated herein, nothing in this License
grants any license to Licensor's trademarks, copyrights, patents, trade
secrets or any other intellectual property. No patent license is granted
to make, use, sell, offer for sale, have made, or import embodiments of
any patent claims other than the licensed claims defined in Section 2.
No license is granted to the trademarks of Licensor even if such marks
are included in the Original Work. Nothing in this License shall be
interpreted to prohibit Licensor from licensing under terms different
from this License any Original Work that Licensor otherwise would have a
right to license.

5) External Deployment. The term "External Deployment" means the use,
distribution, or communication of the Original Work or Derivative Works
in any way such that the Original Work or Derivative Works may be used
by anyone other than You, whether those works are distributed or
communicated to those persons or made available as an application
intended for use over a network. As an express condition for the grants
of license hereunder, You must treat any External Deployment by You of
the Original Work or a Derivative Work as a distribution under section
1(c).

6) Attribution Rights. You must retain, in the Source Code of any
Derivative Works that You create, all copyright, patent, or trademark
notices from the Source Code of the Original Work, as well as any
notices of licensing and any descriptive text identified therein as an
"Attribution Notice." You must cause the Source Code for any Derivative
Works that You create to carry a prominent Attribution Notice reasonably
calculated to inform recipients that You have modified the Original
Work.

7) Warranty of Provenance and Disclaimer of Warranty. Licensor warrants
that the copyright in and to the Original Work and the patent rights
granted herein by Licensor are owned by the Licensor or are sublicensed
to You under the terms of this License with the permission of the
contributor(s) of those copyrights and patent rights. Except as
expressly stated in the immediately preceding sentence, the Original
Work is provided under this License on an "AS IS" BASIS and WITHOUT
WARRANTY, either express or implied, including, without limitation, the
warranties of non-infringement, merchantability or fitness for a
particular purpose. THE ENTIRE RISK AS TO THE QUALITY OF THE ORIGINAL
WORK IS WITH YOU. This DISCLAIMER OF WARRANTY constitutes an essential
part of this License. No license to the Original Work is granted by this
License except under this disclaimer.

8) Limitation of Liability. Under no circumstances and under no legal
theory, whether in tort (including negligence), contract, or otherwise,
shall the Licensor be liable to anyone for any indirect, special,
incidental, or consequential damages of any character arising as a
result of this License or the use of the Original Work including,
without limitation, damages for loss of goodwill, work stoppage,
computer failure or malfunction, or any and all other commercial damages
or losses. This limitation of liability shall not apply to the extent
applicable law prohibits such limitation.

9) Acceptance and Termination. If, at any time, You expressly assented
to this License, that assent indicates your clear and irrevocable
acceptance of this License and all of its terms and conditions. If You
distribute or communicate copies of the Original Work or a Derivative
Work, You must make a reasonable effort under the circumstances to
obtain the express assent of recipients to the terms of this License.
This License conditions your rights to undertake the activities listed
in Section 1, including your right to create Derivative Works based upon
the Original Work, and doing so without honoring these terms and
conditions is prohibited by copyright law and international treaty.
Nothing in this License is intended to affect copyright exceptions and
limitations (including "fair use" or "fair dealing"). This License shall
terminate immediately and You may no longer exercise any of the rights
granted to You by this License upon your failure to honor the conditions
in Section 1(c).

10) Termination for Patent Action. This License shall terminate
automatically and You may no longer exercise any of the rights granted
to You by this License as of the date You commence an action, including
a cross-claim or counterclaim, against Licensor or any licensee alleging
that the Original Work infringes a patent. This termination provision
shall not apply for an action alleging patent infringement by
combinations of the Original Work with other software or hardware.

11) Jurisdiction, Venue and Governing Law. Any action or suit relating
to this License may be brought only in the courts of a jurisdiction
wherein the Licensor resides or in which Licensor conducts its primary
business, and under the laws of that jurisdiction excluding its
conflict-of-law provisions. The application of the United Nations
Convention on Contracts for the International Sale of Goods is expressly
excluded. Any use of the Original Work outside the scope of this License
or after its termination shall be subject to the requirements and
penalties of copyright or patent law in the appropriate jurisdiction.
This section shall survive the termination of this License.

12) Attorneys' Fees. In any action to enforce the terms of this License
or seeking damages relating thereto, the prevailing party shall be
entitled to recover its costs and expenses, including, without
limitation, reasonable attorneys' fees and costs incurred in connection
with such action, including any appeal of such action. This section
shall survive the termination of this License.

13) Miscellaneous. If any provision of this License is held to be
unenforceable, such provision shall be reformed only to the extent
necessary to make it enforceable.

14) Definition of "You" in This License. "You" throughout this License,
whether in upper or lower case, means an individual or a legal entity
exercising rights under, and complying with all of the terms of, this
License. For legal entities, "You" includes any entity that controls, is
controlled by, or is under common control with you. For purposes of this
definition, "control" means (i) the power, direct or indirect, to cause
the direction or management of such entity, whether by contract or
otherwise, or (ii) ownership of fifty percent (50%) or more of the
outstanding shares, or (iii) beneficial ownership of such entity.

15) Right to Use. You may use the Original Work in all ways not
otherwise restricted or conditioned by this License or by law, and
Licensor promises not to interfere with or be responsible for such uses
by You.

16) Modification of This License. This License is Copyright (c) 2005
Lawrence Rosen. Permission is granted to copy, distribute, or
communicate this License without modification. Nothing in this License
permits You to modify this License as applied to the Original Work or to
Derivative Works. However, You may modify the text of this License and
copy, distribute or communicate your modified version (the "Modified
License") and apply it to other original works of authorship subject to
the following conditions: (i) You may not indicate in any way that your
Modified License is the "Academic Free License" or "AFL" and you may not
use those names in the name of your Modified License; (ii) You must
replace the notice specified in the first paragraph above with the
notice "Licensed under <insert your license name here>" or with a notice
of your own that is not confusingly similar to the notice in this
License; and (iii) You may not claim that your original works are open
source software unless your Modified License has been approved by Open
Source Initiative (OSI) and You comply with its license review and
certification process.


*/