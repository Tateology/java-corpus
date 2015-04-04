package steve.test.contour;
import gov.noaa.ncdc.wct.export.raster.WCTRaster;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Arrays;

// from: 
// http://www.crm.umontreal.ca/Galerie/ContourPlotApplet_Eng.html
// and 
// http://www.mactech.com/articles/mactech/Vol.13/13.09/ContourPlottinginJava/


// This was used for testing / comparison against Unidata contour code.
// No improvement was visible.  For testing, u and v are flipped and rotated, but it works.

//----------------------------------------------------------
// "ContourPlot" is the most important class. It is a
// user-interface component which parses the data, draws
// the contour plot, and returns a string of results.
//----------------------------------------------------------
public class ContourPlot extends Canvas {

	// Below, constant data members:
	final static boolean	SHOW_NUMBERS	= true;
	final static int	BLANK		= 32,
				OPEN_SUITE	= (int)'{',
				CLOSE_SUITE	= (int)'}',
				BETWEEN_ARGS	= (int)',',
				N_CONTOURS	= 10,
				PLOT_MARGIN	= 20,
				WEE_BIT		=  3,
				NUMBER_LENGTH	=  3;
	final static double	Z_MAX_MAX	= 1.0E+10,
				Z_MIN_MIN	= -Z_MAX_MAX;
	final static String EOL	=
		System.getProperty("line.separator");

	// Below, data members which store the grid steps,
	// the z values, the interpolation flag, the dimensions
	// of the contour plot and the increments in the grid:
	int		xSteps, ySteps;
	float		z[][];
	boolean		logInterpolation = false;
	Dimension	d;
	double		deltaX, deltaY;

	// Below, data members, most of which are adapted from
	// Fortran variables in Snyder's code:
	int	ncv = N_CONTOURS;
	int	l1[] = new int[4];
	int	l2[] = new int[4];
	int	ij[] = new int[2];
	int	i1[] = new int[2];
	int	i2[] = new int[2];
	int	i3[] = new int[6];
	int	ibkey,icur,jcur,ii,jj,elle,ix,iedge,iflag,ni,ks;
	int	cntrIndex,prevIndex;
	int	idir,nxidir,k;
	double	z1,z2,cval,zMax,zMin;
	double	intersect[]	= new double[4];
	double	xy[]		= new double[2];
	double	prevXY[]	= new double[2];
	float	cv[]		= new float[ncv];
	boolean	jump;

	//-------------------------------------------------------
	// A constructor method.
	//-------------------------------------------------------
//	public ContourPlot(int x, int y) {
	public ContourPlot(ArrayList<Double> allContourValues, WCTRaster raster) {
		super();
//		xSteps = x;
//		ySteps = y;
		xSteps = raster.getWritableRaster().getWidth();
		ySteps = raster.getWritableRaster().getHeight();
		d = new Dimension(xSteps, ySteps);
		z = new float[xSteps][ySteps];
		for (int x=0; x<xSteps; x++) {
			for (int y=0; y<ySteps; y++) {
				z[x][y] = raster.getWritableRaster().getSampleFloat(x, y, 0);
			}
		}
		deltaX = 1;
		deltaY = 1;

		
		try {
			GetExtremes();
			if (zMax > Z_MAX_MAX) zMax = Z_MAX_MAX;
			if (zMin < Z_MIN_MIN) zMin = Z_MIN_MIN;
			AssignContourValues();
			
			
			System.out.println("zMin="+zMin+"  zMax="+zMax);
			System.out.println("contourValues="+Arrays.toString(cv));
			
		} catch (ParseMatrixException e) {
			e.printStackTrace();
		}

		
		setForeground(Color.black);
		setBackground(Color.white);
	}

	//-------------------------------------------------------
	int sign(int a, int b) {
		a = Math.abs(a);
		if (b < 0)	return -a;
		else		return  a;
	}

	//-------------------------------------------------------
	// "InvalidData" sets the first two components of the
	// contour value array to equal values, thus preventing
	// subsequent drawing of the contour plot.
	//-------------------------------------------------------
	void InvalidData() {
		cv[0] = (float)0.0;
		cv[1] = (float)0.0;
	}

	//-------------------------------------------------------
	// "GetExtremes" scans the data in "z" in order
	// to assign values to "zMin" and "zMax".
	//-------------------------------------------------------
	void GetExtremes() throws ParseMatrixException {
		int	i,j;
		float	here;

//		zMin = z[0][0];
//		zMax = zMin;
		zMin = Float.POSITIVE_INFINITY;
		zMax = Float.NEGATIVE_INFINITY;
		for (i = 0; i < xSteps; i++) {
			for (j = 0; j < ySteps; j++) {
				here = z[i][j];
				if (Float.isNaN(here)) {
					continue;
				}
				if (zMin > here) zMin = here;
				if (zMax < here) zMax = here;
				System.out.println(here);
			}
		}
		if (zMin == zMax) {
			InvalidData();
			throw new ParseMatrixException("invalid data 1");
		}
		return;
	}

	//-------------------------------------------------------
	// "AssignContourValues" interpolates between "zMin" and
	// "zMax", either logarithmically or linearly, in order
	// to assign contour values to the array "cv".
	//-------------------------------------------------------
	void AssignContourValues() throws ParseMatrixException {
		int	i;
		double	delta;

		if ((logInterpolation) && (zMin <= 0.0)) {
			InvalidData();
			throw new ParseMatrixException("invalid data 2");
		}
		if (logInterpolation) {
			double	temp = Math.log(zMin);

			delta = (Math.log(zMax)-temp) / ncv;
			for (i = 0; i < ncv; i++) cv[i] = (float)Math.exp(temp + (i+1)*delta);
		}
		else {
			delta = (zMax-zMin) / ncv;
			for (i = 0; i < ncv; i++) cv[i] = (float)(zMin + (i+1)*delta);
		}
	}

	//-------------------------------------------------------
	// "GetContourValuesString" returns a list of the
	// contour values for display in the results area.
	//-------------------------------------------------------
	String GetContourValuesString() {
		String	s = new String();
		int	i;

		for (i = 0; i < ncv; i++) s = s + "[" + Integer.toString(i) + "] " +
			Float.toString(cv[i]) + EOL;
		return s;
	}

	//-------------------------------------------------------
	// "SetMeasurements" determines the dimensions of
	// the contour plot and the increments in the grid.
	//-------------------------------------------------------
	void SetMeasurements() {
		d = size();
		d.width  = d.width  - 2*PLOT_MARGIN;
		d.height = d.height - 2*PLOT_MARGIN;
		deltaX = d.width  / (xSteps - 1.0);
		deltaY = d.height / (ySteps - 1.0);
	}

	//-------------------------------------------------------
	// "DrawGrid" draws the rectangular grid of gray lines
	// on top of which the contours will later be drawn.
	//-------------------------------------------------------
	void DrawGrid(Graphics g) {
		int i,j,kx,ky;

		// Interchange horizontal & vertical
		g.clearRect(0, 0, d.height+2*PLOT_MARGIN, d.width +2*PLOT_MARGIN);
		g.setColor(Color.gray);
		for (i = 0; i < xSteps; i++) {
			kx = (int)((float)i * deltaX);
			g.drawLine( PLOT_MARGIN,
			PLOT_MARGIN+kx,
			PLOT_MARGIN+d.height,
			PLOT_MARGIN+kx);
		}
		for (j = 0; j < ySteps; j++) {
			ky = (int)((float)j * deltaY);
			g.drawLine( PLOT_MARGIN+ky,
			PLOT_MARGIN,
			PLOT_MARGIN+ky,
			PLOT_MARGIN+d.width);
		}
		g.setColor(Color.black);
	}

	//-------------------------------------------------------
	// "SetColour" sets the colour of the graphics object,
	// given the contour index, by interpolating linearly
	// between "Color.blue" & "Color.red".
	//-------------------------------------------------------
	void SetColour(Graphics g) {
		Color c = new Color(
			((ncv-cntrIndex) * Color.blue.getRed()   +
				cntrIndex * Color.red.getRed())/ncv,
			((ncv-cntrIndex) * Color.blue.getGreen() +
				cntrIndex * Color.red.getGreen())/ncv,
			((ncv-cntrIndex) * Color.blue.getBlue()  +
				cntrIndex * Color.red.getBlue())/ncv);

		g.setColor(c);
	}

	//-------------------------------------------------------
	// "DrawKernel" is the guts of drawing and is called
	// directly or indirectly by "ContourPlotKernel" in order
	// to draw a segment of a contour or to set the pen
	// position "prevXY". Its action depends on "iflag":
	//
	// iflag == 1 means Continue a contour
	// iflag == 2 means Start a contour at a boundary
	// iflag == 3 means Start a contour not at a boundary
	// iflag == 4 means Finish contour at a boundary
	// iflag == 5 means Finish closed contour not at boundary
	// iflag == 6 means Set pen position
	//
	// If the constant "SHOW_NUMBERS" is true then when
	// completing a contour ("iflag" == 4 or 5) the contour
	// index is drawn adjacent to where the contour ends.
	//-------------------------------------------------------
	void DrawKernel(Graphics g) {
		int	prevU,prevV,u,v;

		if ((iflag == 1) || (iflag == 4) || (iflag == 5)) {
			if (cntrIndex != prevIndex) { // Must change colour
				SetColour(g);
				prevIndex = cntrIndex;
			}
			prevU = (int)((prevXY[0] - 1.0) * deltaX);
			prevV = (int)((prevXY[1] - 1.0) * deltaY);
			u = (int)((xy[0] - 1.0) * deltaX);
			v = (int)((xy[1] - 1.0) * deltaY);

			System.out.println("iflag="+iflag+"  u1,v1 to u2,v2: "+prevU+","+prevV+" to "+u+","+v);
			
			// Interchange horizontal & vertical
			g.drawLine(PLOT_MARGIN+prevV,PLOT_MARGIN+prevU,
				   PLOT_MARGIN+v, PLOT_MARGIN+u);
			if ((SHOW_NUMBERS) && ((iflag==4) || (iflag==5))) {
				if      (u == 0)	u = u - WEE_BIT;
				else if	(u == d.width)  u = u + PLOT_MARGIN/2;
				else if	(v == 0)	v = v - PLOT_MARGIN/2;
				else if	(v == d.height) v = v + WEE_BIT;
				g.drawString(Integer.toString(cntrIndex),
					PLOT_MARGIN+v, PLOT_MARGIN+u);
			}
		}
		prevXY[0] = xy[0];
		prevXY[1] = xy[1];
	}

	//-------------------------------------------------------
	// "DetectBoundary"
	//-------------------------------------------------------
	void DetectBoundary() {
		ix = 1;
		if (ij[1-elle] != 1) {
			ii = ij[0] - i1[1-elle];
			jj = ij[1] - i1[elle];
			if (z[ii-1][jj-1] <= Z_MAX_MAX) {
				ii = ij[0] + i2[elle];
				jj = ij[1] + i2[1-elle];
				if (z[ii-1][jj-1] < Z_MAX_MAX) ix = 0;
			}
			if (ij[1-elle] >= l1[1-elle]) {
				ix = ix + 2;
				return;
			}
		}
		ii = ij[0] + i1[1-elle];
		jj = ij[1] + i1[elle];
		if (z[ii-1][jj-1] > Z_MAX_MAX) {
			ix = ix + 2;
			return;
		}
		if (z[ij[0]][ij[1]] >= Z_MAX_MAX) ix = ix + 2;
	}

	//-------------------------------------------------------
	// "Routine_label_020" corresponds to a block of code
	// starting at label 20 in Synder's subroutine "GCONTR".
	//-------------------------------------------------------
	boolean Routine_label_020() {
		l2[0] =  ij[0];
		l2[1] =  ij[1];
		l2[2] = -ij[0];
		l2[3] = -ij[1];
		idir = 0;
		nxidir = 1;
		k = 1;
		ij[0] = Math.abs(ij[0]);
		ij[1] = Math.abs(ij[1]);
		if (z[ij[0]-1][ij[1]-1] > Z_MAX_MAX) {
			elle = idir % 2;
			ij[elle] = sign(ij[elle],l1[k-1]);
			return true;
		}
		elle = 0;
		return false;
	}

	//-------------------------------------------------------
	// "Routine_label_050" corresponds to a block of code
	// starting at label 50 in Synder's subroutine "GCONTR".
	//-------------------------------------------------------
	boolean Routine_label_050() {
		while (true) {
			if (ij[elle] >= l1[elle]) {
				if (++elle <= 1) continue;
				elle = idir % 2;
				ij[elle] = sign(ij[elle],l1[k-1]);
				if (Routine_label_150()) return true;
				continue;
			}
			ii = ij[0] + i1[elle];
			jj = ij[1] + i1[1-elle];
			if (z[ii-1][jj-1] > Z_MAX_MAX) {
				if (++elle <= 1) continue;
				elle = idir % 2;
				ij[elle] = sign(ij[elle],l1[k-1]);
				if (Routine_label_150()) return true;
				continue;
			}
			break;
		}
		jump = false;
		return false;
	}

	//-------------------------------------------------------
	// "Routine_label_150" corresponds to a block of code
	// starting at label 150 in Synder's subroutine "GCONTR".
	//-------------------------------------------------------
	boolean Routine_label_150() {
		while (true) {
			//------------------------------------------------
			// Lines from z[ij[0]-1][ij[1]-1]
			//	   to z[ij[0]  ][ij[1]-1]
			//	  and z[ij[0]-1][ij[1]]
			// are not satisfactory. Continue the spiral.
			//------------------------------------------------
			if (ij[elle] < l1[k-1]) {
				ij[elle]++;
				if (ij[elle] > l2[k-1]) {
					l2[k-1] = ij[elle];
					idir = nxidir;
					nxidir = idir + 1;
					k = nxidir;
					if (nxidir > 3) nxidir = 0;
				}
				ij[0] = Math.abs(ij[0]);
				ij[1] = Math.abs(ij[1]);
				if (z[ij[0]-1][ij[1]-1] > Z_MAX_MAX) {
					elle = idir % 2;
					ij[elle] = sign(ij[elle],l1[k-1]);
					continue;
				}
				elle = 0;
				return false;
			}
			if (idir != nxidir) {
				nxidir++;
				ij[elle] = l1[k-1];
				k = nxidir;
				elle = 1 - elle;
				ij[elle] = l2[k-1];
				if (nxidir > 3) nxidir = 0;
				continue;
			}

			if (ibkey != 0) return true;
			ibkey = 1;
			ij[0] = icur;
			ij[1] = jcur;
			if (Routine_label_020()) continue;
			return false;
		}
	}

	//-------------------------------------------------------
	// "Routine_label_200" corresponds to a block of code
	// starting at label 200 in Synder's subroutine "GCONTR".
	// It has return values 0, 1 or 2.
	//-------------------------------------------------------
	short Routine_label_200(Graphics g,  boolean workSpace[])
	{
		while (true) {
			xy[elle] = 1.0*ij[elle] + intersect[iedge-1];
			xy[1-elle] = 1.0*ij[1-elle];
			workSpace[2*(xSteps*(ySteps*cntrIndex+ij[1]-1)
				+ij[0]-1) + elle] = true;
			DrawKernel(g);
			if (iflag >= 4) {
				icur = ij[0];
				jcur = ij[1];
				return 1;
			}
			ContinueContour();
			if (!workSpace[2*(xSteps*(ySteps*cntrIndex
				+ij[1]-1)+ij[0]-1)+elle]) return 2;
			iflag = 5;		// 5. Finish a closed contour
			iedge = ks + 2;
			if (iedge > 4) iedge = iedge - 4;
			intersect[iedge-1] = intersect[ks-1];
		}
	}

	//-------------------------------------------------------
	// "CrossedByContour" is true iff the current segment in
	// the grid is crossed by one of the contour values and
	// has not already been processed for that value.
	//-------------------------------------------------------
	boolean CrossedByContour(boolean workSpace[]) {
		ii = ij[0] + i1[elle];
		jj = ij[1] + i1[1-elle];
		z1 = z[ij[0]-1][ij[1]-1];
		z2 = z[ii-1][jj-1];
		for (cntrIndex = 0; cntrIndex < ncv; cntrIndex++) {
			int i = 2*(xSteps*(ySteps*cntrIndex+ij[1]-1) + ij[0]-1) + elle;

			if (!workSpace[i]) {
				float x = cv[cntrIndex];
				if ((x>Math.min(z1,z2)) && (x<=Math.max(z1,z2))) {
					workSpace[i] = true;
					return true;
				}
			}
		}
		return false;
	}

	//-------------------------------------------------------
	// "ContinueContour" continues tracing a contour. Edges
	// are numbered clockwise, the bottom edge being # 1.
	//-------------------------------------------------------
	void ContinueContour() {
		short local_k;

		ni = 1;
		if (iedge >= 3) {
			ij[0] = ij[0] - i3[iedge-1];
			ij[1] = ij[1] - i3[iedge+1];
		}
		for (local_k = 1; local_k < 5; local_k++)
			if (local_k != iedge) {
				ii = ij[0] + i3[local_k-1];
				jj = ij[1] + i3[local_k];
				z1 = z[ii-1][jj-1];
				ii = ij[0] + i3[local_k];
				jj = ij[1] + i3[local_k+1];
				z2 = z[ii-1][jj-1];
				if ((cval > Math.min(z1,z2) && (cval <= Math.max(z1,z2)))) {
					if ((local_k == 1) || (local_k == 4)) {
						double	zz = z2;

						z2 = z1;
						z1 = zz;
					}
					intersect[local_k-1] = (cval - z1)/(z2 - z1);
					ni++;
					ks = local_k;
				}
			}
		if (ni != 2) {
			//-------------------------------------------------
			// The contour crosses all 4 edges of cell being
			// examined. Choose lines top-to-left & bottom-to-
			// right if interpolation point on top edge is
			// less than interpolation point on bottom edge.
			// Otherwise, choose the other pair. This method
			// produces the same results if axes are reversed.
			// The contour may close at any edge, but must not
			// cross itself inside any cell.
			//-------------------------------------------------
			ks = 5 - iedge;
			if (intersect[2] >= intersect[0]) {
				ks = 3 - iedge;
				if (ks <= 0) ks = ks + 4;
			}
		}
		//----------------------------------------------------
		// Determine whether the contour will close or run
		// into a boundary at edge ks of the current cell.
		//----------------------------------------------------
		elle = ks - 1;
		iflag = 1;		// 1. Continue a contour
		jump = true;
		if (ks >= 3) {
			ij[0] = ij[0] + i3[ks-1];
			ij[1] = ij[1] + i3[ks+1];
			elle = ks - 3;
		}
	}

	//-------------------------------------------------------
	// "ContourPlotKernel" is the guts of this class and
	// corresponds to Synder's subroutine "GCONTR".
	//-------------------------------------------------------
	void ContourPlotKernel(Graphics g,	boolean workSpace[])
	{
		short val_label_200;

		l1[0] = xSteps;	l1[1] = ySteps;
		l1[2] = -1;l1[3] = -1;
		i1[0] =	1; i1[1] =  0;
		i2[0] =	1; i2[1] = -1;
		i3[0] =	1; i3[1] =  0; i3[2] = 0;
		i3[3] =	1; i3[4] =  1; i3[5] = 0;
		prevXY[0] = 0.0; prevXY[1] = 0.0;
		xy[0] = 1.0; xy[1] = 1.0;
		cntrIndex = 0;
		prevIndex = -1;
		iflag = 6;
		DrawKernel(g);
		icur = Math.max(1, Math.min((int)Math.floor(xy[0]), xSteps));
		jcur = Math.max(1, Math.min((int)Math.floor(xy[1]), ySteps));
		ibkey = 0;
		ij[0] = icur;
		ij[1] = jcur;
		if (Routine_label_020() &&
			 Routine_label_150()) return;
		if (Routine_label_050()) return;
		while (true) {
			DetectBoundary();
			if (jump) {
				if (ix != 0) iflag = 4; // Finish contour at boundary
				iedge = ks + 2;
				if (iedge > 4) iedge = iedge - 4;
				intersect[iedge-1] = intersect[ks-1];
				val_label_200 = Routine_label_200(g,workSpace);
				if (val_label_200 == 1) {
					if (Routine_label_020() && Routine_label_150()) return;
					if (Routine_label_050()) return;
					continue;
				}
				if (val_label_200 == 2) continue;
				return;
			}
			if ((ix != 3) && (ix+ibkey != 0) && CrossedByContour(workSpace)) {
				//
				// An acceptable line segment has been found.
				// Follow contour until it hits a
				// boundary or closes.
				//
				iedge = elle + 1;
				cval = cv[cntrIndex];
				if (ix != 1) iedge = iedge + 2;
				iflag = 2 + ibkey;
				intersect[iedge-1] = (cval - z1) / (z2 - z1);
				val_label_200 = Routine_label_200(g,workSpace);
				if (val_label_200 == 1) {
					if (Routine_label_020() && Routine_label_150()) return;
					if (Routine_label_050()) return;
					continue;
				}
				if (val_label_200 == 2) continue;
				return;
			}
			if (++elle > 1) {
				elle = idir % 2;
				ij[elle] = sign(ij[elle],l1[k-1]);
				if (Routine_label_150()) return;
			}
			if (Routine_label_050()) return;
		}
	}

	//-------------------------------------------------------
	// "paint" overrides the superclass' "paint()" method.
	// This method draws the grid and then the contours,
	// provided that the first two contour values are not
	// equal (which would indicate invalid data).
	// The "workSpace" is used to remember which segments in
	// the grid have been crossed by which contours.
	//-------------------------------------------------------
	public void paint(Graphics g)
	{
		int workLength = 2 * xSteps * ySteps * ncv;
		boolean	workSpace[]; // Allocate below if data valid

		SetMeasurements();
		DrawGrid(g);
		if (cv[0] != cv[1]) { // Valid data
			workSpace = new boolean[workLength];
			ContourPlotKernel(g, workSpace);
		}
	}

	//-------------------------------------------------------
	// "ParseZedMatrix" parses the matrix of z values
	// which it expects to find in the string "s".
	//-------------------------------------------------------
//	public void ParseZedMatrix(String s)
//		throws ParseMatrixException, IOException
//	{
//		StringBufferInputStream i;
//		StreamTokenizer		t;
//
//		i = new StringBufferInputStream(s);
//		t = new StreamTokenizer(i);
//
//		z = null;  // Junk any existing matrix
//		EatCharacter(t,OPEN_SUITE);
//		do ParseRowVector(t);
//		while (t.nextToken() == BETWEEN_ARGS);
//		if (t.ttype != CLOSE_SUITE) {
//			InvalidData();
//			throw new ParseMatrixException("invalid data 3");
//		}
//		if (t.nextToken() != t.TT_EOF) {
//			InvalidData();
//			throw new ParseMatrixException("invalid data 4");
//		}
//		MakeMatrixRectangular();
//		GetExtremes();
//		if (zMax > Z_MAX_MAX) zMax = Z_MAX_MAX;
//		if (zMin < Z_MIN_MIN) zMin = Z_MIN_MIN;
//		AssignContourValues();
//	}

	//-------------------------------------------------------
	// "ParseRowVector" parses a row of data from the stream.
	//-------------------------------------------------------
//	public void ParseRowVector(StreamTokenizer t)
//		throws ParseMatrixException, IOException
//	{	// Parse a row of float's and
//		// insert them in a new row of z[][]
//		if (z == null) z = new float[1][];
//		else AddRow();
//		EatCharacter(t,OPEN_SUITE);
//		do {
//			if (t.nextToken() == t.TT_NUMBER) {
//				int x = z.length - 1;
//
//				if (z[x] == null) {
//					z[x] = new float[1];
//					z[x][0] = (float)t.nval;
//				}
//				else AddColumn((float)t.nval);
//			}
//			else {
//				int x = z.length - 1;
//				int y = z[x].length - 1;
//
//				InvalidData();
//				throw new ParseMatrixException("invalid data 5 [" +
//					Integer.toString(x) + "," +
//					Integer.toString(y) + "]");
//			}
//		} while (t.nextToken() == BETWEEN_ARGS);
//		if (t.ttype != CLOSE_SUITE) {
//			InvalidData();
//			throw new ParseMatrixException("invalid data 6");
//		}
//	}

	//-------------------------------------------------------
	// "AddRow" appends a new empty row to the end of "z"
	//-------------------------------------------------------
//	public void AddRow() throws ParseMatrixException {
//		int leng = z.length;
//		float temp[][];
//
////		if (leng >= ContourPlotApplet.MAX_X_STEPS)
////			throw new ParseMatrixException(
////				ContourPlotApplet.errParse + EOL +
////				ContourPlotApplet.errBounds);
//		temp = new float[leng+1][];
//		System.arraycopy(z, 0, temp, 0, leng);
//		z = temp;
//	}

	//-------------------------------------------------------
	// "AddColumn" appends "val" to end of last row in "z"
	//-------------------------------------------------------
//	public void AddColumn(float val)
//		throws ParseMatrixException
//	{
//		int i = z.length - 1;
//		int leng = z[i].length;
//		float temp[];
//
////		if (leng >= ContourPlotApplet.MAX_Y_STEPS)
////			throw new ParseMatrixException(
////				ContourPlotApplet.errParse + EOL +
////				ContourPlotApplet.errBounds);
//		temp = new float[leng+1];
//		System.arraycopy(z[i], 0, temp, 0, leng);
//		temp[leng] = val;
//		z[i] = temp;
//	}

	//-------------------------------------------------------
	// "MakeMatrixRectangular" appends zero(s) to the end of
	// any row of "z" which is shorter than the longest row.
	//-------------------------------------------------------
//	public void MakeMatrixRectangular() {
//		int	i,y,leng;
//
//		xSteps = z.length;
////		ySteps = ContourPlotApplet.MIN_Y_STEPS;
//		for (i = 0; i < xSteps; i++) {
//			y = z[i].length;
//			if (ySteps < y) ySteps = y;
//		}
//
//		for (i = 0; i < xSteps; i++) {
//			leng = z[i].length;
//			if (leng < ySteps) {
//				float temp[] = new float[ySteps];
//
//				System.arraycopy(z[i], 0, temp, 0, leng);
//				while (leng < ySteps) temp[leng++] = 0;
//				z[i] = temp;
//			}
//		}
//	}

	//-------------------------------------------------------
	// "ReturnZedMatrix" returns a string containing the
	// values in "z" for display in the results area.
	//-------------------------------------------------------
	public String ReturnZedMatrix() {
		String	s,oneValue;
		int		i,j;

		s = "";
//		s = new String(
//			ContourPlotApplet.infoStrX + xSteps + EOL +
//			ContourPlotApplet.infoStrY + ySteps + EOL);
		for (i = 0; i < xSteps; i++) {
			for (j = 0; j < ySteps; j++) {
				oneValue = Double.toString(z[i][j]);
				while (oneValue.length() < NUMBER_LENGTH)
					oneValue = " " + oneValue;
				s = s + oneValue;
				if (j < ySteps-1) s = s + " ";
			}
			s = s + EOL;
		}
		return s;
	}

	//-------------------------------------------------------
	// "EatCharacter" skips any BLANK's in the stream and
	// expects the character "c", throwing an exception if
	// the next non-BLANK character is not "c".
	//-------------------------------------------------------
	public void EatCharacter(StreamTokenizer t, int c)
		throws ParseMatrixException, IOException
	{
		while (t.nextToken() == BLANK) ;
		if (t.ttype != c) {
			InvalidData();
			throw new ParseMatrixException("invalid data 9");
		}
	}
	
	//----------------------------------------------------------
	// Class "ParseMatrixException" is used to signal an
	// error corresponding to invalid data encountered
	// when parsing the matrix of z values.
	//----------------------------------------------------------
	public class ParseMatrixException extends Exception {
		public ParseMatrixException(String message) { super(message); }
	}

}
