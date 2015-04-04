package gov.noaa.ncdc.wct.isosurface;


import java.util.ArrayList;

///////////////////////////////////////////////////////////////////////////
//
// File: SurfaceGen.C
//
// Author: Keith Doolittle 10/97
//
// Purpose: Isosurface generation
// 
// Isosurface extraction algorithm from:
//
//        "Exploiting Triangulated Surface Extraction Using
//         Tetrahedral Decomposition", Andre Gueziec, Robert Hummel,
//        IEEE Transactions on Visualization and Computer Graphics,
//        Vol. 1, No. 4, December 1995.
//
///////////////////////////////////////////////////////////////////////////
/*#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <sys/types.h>
#include <TDL/Surface.h>
#include <TDL/ItXVolume.h>
#include <TDL/ItXVolumeUtils.h>
#include <TDL/SurfaceGen.h>
#include <string.h>
#include <math.h>
#include <TDL/cell_table.h>*/

//#ifndef _SurfaceGen_h_
//#define _SurfaceGen_h_

//#include <OS.h>
//#include <TDL/ItXWorkClass.h>

//class Surface;
//class ItXVolume;

//#define TABLE_SIZE 9967

//typedef struct hashtype hash;

//struct hashtype  {
//  float *v1, *n1;
//  float **v;
//  struct hashtype *next;
//};

//class hashtype  {
  //float *v1, *n1;
  //float **v;
  //hashtype next;
//};


public class SurfaceGen {

    private int NORMAL_TYPE;
    private int VERBOSE;
    private int XDIMYDIM;

    private int avail;
    //private hash *cur;
    //private hash *h;


    private float[] VERTICES;        // a pointer to the x coordinates 
    private float[] NORMALS;         // a pointer to the x normals 
    private int NUM_VERTICES;       // number of vertices currently stored 
    private int VERT_LIMIT;         // currently allocated space for vertices 
    private int VERT_INCR;          // allocate space for this many vertices at a time 

    //private hash *htable[TABLE_SIZE];
    //private hash *manage;
		
    //private int hashit(float *v);
	
    //private float ***tricompactn(float *v, float *n, int nv);
	
    //private float ***tricompact(float *v,int nv);
		
    //private hash * newhash();

    private float xtrans,ytrans,ztrans;

    private CellTable cell_table;

    private float EPSILON = 0.0f;
//#endif


    
    // ansari stuff
    private ArrayList<TriangleListener> listeners = new ArrayList<TriangleListener>();

//
// constructor    
//
public SurfaceGen()
{
    System.out.println(" In Constructor ");
    cell_table = new CellTable();
    EPSILON = 0.0f;
}

//
//
// calls get_cell_polys_and_norms, get_cell_polys, calc_index, calc_cell_normals, get_cell_verts_and_norms, get_cell_verts
//
private int iso_surface(float[] data, int xdim, int ydim, int zdim, float threshold)
{
    int x, y, z;
    int xdim1, ydim1, zdim1;
    int[] index;
    int npolys;
    float[] crossings;
    float[] cell_norms;
    float[] normals;

    zdim1 = zdim - 1;
    ydim1 = ydim - 1;
    xdim1 = xdim - 1;

    XDIMYDIM = xdim * ydim;

    npolys = 0;

    index = new int[xdim1];
    crossings = new float[xdim1*13*3];
    if(NORMAL_TYPE==1) {
	// gradient normals - scary stuff kids 
        normals = new float[xdim1*13*3];
        cell_norms = new float[xdim1*8*3];
	for (z = 0; z < zdim1; z++) {
                for (y = 0; y < ydim1; y++) {
                    calc_index(index, data, y, z, xdim, threshold);
                    calc_cell_normals(index, data, y, z, xdim, ydim, zdim, cell_norms);
                    get_cell_verts_and_norms(index, data, y, z, xdim, xtrans, ytrans, ztrans, threshold, crossings, cell_norms, normals);
                    npolys += get_cell_polys_and_norms(index, xdim, crossings, normals);
                }
            }
        normals = null;
        cell_norms = null;
    } else {
	for(z = 0; z < zdim1; z++) {
                for (y = 0; y < ydim1; y++) {
                    calc_index(index, data, y, z, xdim, threshold);
                    get_cell_verts(index, data, y, z, xdim, xtrans, ytrans, ztrans, threshold, crossings);
                    npolys += get_cell_polys(index, xdim, crossings);
                }
            }
    }
    index = null;
    crossings = null;

    // don't do this when its time for multiple iso-surfaces 
    return 0;

}


//
// This subroutine calculates the index and creates some global 
// temporary variables (for speed). 
//
// called by iso_surface
//
private void calc_index(int[] index, float[] data, int y1, int z1, int xdim, float thresh)
{
    int tmp;
    float threshold = thresh;
    int x1;
    int i = 0;

    // first compute index of first cube 

    tmp = (z1 * XDIMYDIM) + (y1 * xdim) + 0;
    if (threshold <= data[tmp + 0]){i += 1 * 1;}
    if (threshold <= data[tmp + 1]){i += 1 * 2;}

    tmp += xdim;
    if (threshold <= data[tmp + 1]){i += 1 * 4;}
    if (threshold <= data[tmp + 0]){i += 1 * 8;}

    tmp = tmp - xdim + XDIMYDIM;
    if (threshold <= data[tmp + 0]){i += 1 * 16;}
    if (threshold <= data[tmp + 1]){i += 1 * 32;}

    tmp += xdim;
    if (threshold <= data[tmp + 1]){i += 1 * 64;}
    if (threshold <= data[tmp + 0]){i += 1 * 128;}

    index[0] = i;

    // now compute rest 

    tmp -= xdim + XDIMYDIM;
    for (x1 = 1; x1 < xdim-1; x1++) {
	++tmp;

	// resuse 4 of the bits 
	i = ((i&0x44)<<1) | ((i&0x22)>>1);

	if(threshold <= data[tmp + 1]){i += 1 * 2;}
	if(threshold <= data[tmp + xdim+1]){i += 1 * 4;}
	if(threshold <= data[tmp + XDIMYDIM+1]){i += 1 * 32;}
	if(threshold <= data[tmp + XDIMYDIM+xdim+1]){i += 1 * 64;}

	index[x1] = i;
    }
}


//
// called by iso_surface
//
// calls linterp
//
private void get_cell_verts(int[] index, float[] data, int y1, int z1, int xdim, 
			float xtrans, float ytrans, float ztrans, float threshold, float[] crossings)
{
    int x1, y2, z2;

    y2 = y1 + 1;
    z2 = z1 + 1;
    for (x1 = 0; x1 < xdim-1; x1++) {
	float cx, cy, cz;
	int nedges;
	int crnt_edge;
	int x2 = x1 + 1;
	int i;
	int v1, v4, v5, v8;


	if (index[x1]==0) {
                continue;
            }
        
        //data
	v1 = z1*XDIMYDIM + y1*xdim + x1;
	v4 = v1 + xdim;
	v5 = v1 + XDIMYDIM;
	v8 = v4 + XDIMYDIM;

	nedges = cell_table.getCellEntry(index[x1]).nedges;
	for (i = 0; i < nedges; i++) {
	    crnt_edge = cell_table.getCellEntry(index[x1]).edges[i];
	    cx = xtrans; cy = ytrans; cz = ztrans;
	    switch (crnt_edge) {
	    case 1:
	    cx += linterp(data[v1 + 0], data[v1 + 1], threshold, x1, x2);
	    cy += (float) y1;
	    cz += (float) z1;
	    break;

	    case 2:
	    cy += linterp(data[v1 + 1], data[v4 + 1], threshold, y1, y2);
	    cx += (float) x2;
	    cz += (float) z1;
	    break;

	    case 3:
	    cx += linterp(data[v4 + 0], data[v4 + 1], threshold, x1, x2);
	    cy += (float) y2;
	    cz += (float) z1;
	    break;

	    case 4:
	    cy += linterp(data[v1 + 0], data[v4 + 0], threshold, y1, y2);
	    cx += (float) x1;
	    cz += (float) z1;
	    break;

	    case 5:
	    cx += linterp(data[v5 + 0], data[v5 + 1], threshold, x1, x2);
	    cy += (float) y1;
	    cz += (float) z2;
	    break;

	    case 6:
	    cy += linterp(data[v5 + 1], data[v8 + 1], threshold, y1, y2);
	    cx += (float) x2;
	    cz += (float) z2;
	    break;

	    case 7:
	    cx += linterp(data[v8 + 0], data[v8 + 1], threshold, x1, x2);
	    cy += (float) y2;
	    cz += (float) z2;
	    break;

	    case 8:
	    cy += linterp(data[v5 + 0], data[v8 + 0], threshold, y1, y2);
	    cx += (float) x1;
	    cz += (float) z2;
	    break;

	    case 9:
	    cz += linterp(data[v1 + 0], data[v5 + 0], threshold, z1, z2);
	    cy += (float) y1;
	    cx += (float) x1;
	    break;

	    case 10:
	    cz += linterp(data[v1 + 1], data[v5 + 1], threshold, z1, z2);
	    cy += (float) y1;
	    cx += (float) x2;
	    break;

	    case 11:
	    cz += linterp(data[v4 + 0], data[v8 + 0], threshold, z1, z2);
	    cy += (float) y2;
	    cx += (float) x1;
	    break;

	    case 12:
	    cz += linterp(data[v4 + 1], data[v8 + 1], threshold, z1, z2);
	    cy += (float) y2;
	    cx += (float) x2;
	    break;

	    } 
            crossings[x1*13*3+crnt_edge*3+0] = cx;
            crossings[x1*13*3+crnt_edge*3+1] = cy;
            crossings[x1*13*3+crnt_edge*3+2] = cz;
	} 
    }
}

//
// This subroutine will calculate the polygons 
//
// called by iso_surface
//
// calls add_polygon, getCellEntry, calc_normal
//
private int get_cell_polys(int[] index, int xdim, float[] crossings)
{
    int num_o_polys, polys = 0;
    int poly;
    float[] p1 = new float[3];
    float[] p2 = new float[3];
    float[] p3 = new float[3];
    float[] n1 = new float[3];
    float[] n2 = new float[3];
    float[] n3 = new float[3];
    int x1;
    int count;
    for (count=0;count<3;count++){
        n1[count] = 0.0f;
        n2[count] = 0.0f;
        n3[count] = 0.0f;
        p1[count] = 0.0f;
        p2[count] = 0.0f;
        p3[count] = 0.0f;
    }
    int offset = 0;
    

    for (x1 = 0; x1 < xdim-1; x1++) {
	if (index[x1]==0) {
                continue;
            }
	num_o_polys = cell_table.getCellEntry(index[x1]).npolys;
	for (poly = 0; poly < num_o_polys; poly++) {

	    offset = x1*13*3+(cell_table.getCellEntry(index[x1]).polys[poly*3])*3+0;
            for (count=0;count<3;count++){
                p1[count] = crossings[offset+count];
            }
	    offset = x1*13*3+(cell_table.getCellEntry(index[x1]).polys[poly*3 + 1])*3+0;
            for (count=0;count<3;count++){
                p2[count] = crossings[offset+count];
            }
	    offset = x1*13*3+(cell_table.getCellEntry(index[x1]).polys[poly*3 + 2])*3+0;
            for (count=0;count<3;count++){
                p3[count] = crossings[offset+count];
            }
            
	    if ((p1[0] == p2[0] && p1[1] == p2[1] && p1[2] == p2[2]) ||
		(p1[0] == p3[0] && p1[1] == p3[1] && p1[2] == p3[2]) ||
		(p2[0] == p3[0] && p2[1] == p3[1] && p2[2] == p3[2]))  {
//		printf("addpoly - degenerate triangle\n");
		polys--;
		continue;
	    }
	    n1 = calc_normal(p1, p2, p3, n1);
	    add_polygon(p1, p2, p3, n1, n1, n1);
	}

	polys += num_o_polys;
    }
    return polys;
}

//
// called by calc_cell_normals
//
private float D(float[] data, int xdim, int x, int y, int z){
    float result = data[(z)*XDIMYDIM + (y)*xdim + (x)];
    return(result);
}

//
// called by calc_cell_normals
//
private float[] normalize(float[] norms, int x1, int i) { 
        float d = (float) Math.sqrt(norms[x1*24+(3*(i)+0)] * norms[x1*24+(3*(i)+0)] + 
                       norms[x1*24+(3*(i)+1)] * norms[x1*24+(3*(i)+1)] + 
                       norms[x1*24+(3*(i)+2)] * norms[x1*24+(3*(i)+2)]); 
        if (d > EPSILON) {	
            norms[x1*24+(3*(i)+0)] /= d; norms[x1*24+(3*(i)+1)] /= d; norms[x1*24+(3*(i)+2)] /= d; 
        }	
        return(norms);
}


//
// This subroutine calculates the vertex normals using a central difference 
//
// called by calc_cell_normals
//
// calls normalize, D
//
private void calc_cell_normals(int[] index, float[] data, int y1, int z1, int xdim, int ydim, int zdim, float[] norms)
{

    int i, x1;
    float[] nn;
    int y11, z11;
    int offset = 0;

    y11 = y1+1; z11 = z1+1;
    if (y1 == 0){
        y1++;
    } else if (y1 == ydim - 2){
        y11--;
    }
    if (z1 == 0){
        z1++;
    } else if (z1 == zdim - 2) {
        z11--;
    }
    nn = norms;

    if (index[0]==1) {
	nn[0] = D(data, xdim, 2, y1, z1) - D(data, xdim, 0, y1, z1);
	nn[1] = D(data, xdim, 1, y1 + 1, z1) - D(data, xdim, 1, y1 - 1, z1);
	nn[2] = D(data, xdim, 1, y1, z1 + 1) - D(data, xdim, 1, y1, z1 - 1);

	nn[6] = D(data, xdim, 2, y11, z1) - D(data, xdim, 0, y11, z1);
	nn[7] = D(data, xdim, 1, y11 + 1, z1) - D(data, xdim, 1, y11 - 1, z1);
	nn[8] = D(data, xdim, 1, y11, z1 + 1) - D(data, xdim, 1, y11, z1 - 1);

	nn[12] = D(data, xdim, 2, y1, z11) - D(data, xdim, 0, y1, z11);
	nn[13] = D(data, xdim, 1, y1 + 1, z11) - D(data, xdim, 1, y1 - 1, z11);
	nn[14] = D(data, xdim, 1, y1, z11 + 1) - D(data, xdim, 1, y1, z11 - 1);

	nn[18] = D(data, xdim, 2, y11, z11) - D(data, xdim, 0, y11, z11);
	nn[19] = D(data, xdim, 1, y11 + 1, z11) - D(data, xdim, 1, y11 - 1, z11);
	nn[20] = D(data, xdim, 1, y11, z11 + 1) - D(data, xdim, 1, y11, z11 - 1);

	for(i = 0; i < 8; i+=2) {
	    float d = (float) Math.sqrt(nn[3*i+0] * nn[3*i+0] +
			   nn[3*i+1] * nn[3*i+1] +
			   nn[3*i+2] * nn[3*i+2]);
	    if (d > EPSILON) {
		nn[3*i+0] /= d; nn[3*i+1] /= d; nn[3*i+2] /= d;
	    }
	    nn[3*i+3] = nn[3*i+0];
	    nn[3*i+4] = nn[3*i+1];
	    nn[3*i+5] = nn[3*i+2];
	}
    }

    for (x1 = 1; x1 < xdim-2; x1++) {
	if (index[x1]==0) {
                continue;
            }
	if (index[x1-1]==0) {
	    // no coherency 
	    norms[x1*24+0] = D(data, xdim, x1 + 1, y1, z1) - D(data, xdim, x1 - 1, y1, z1);
	    norms[x1*24+1] = D(data, xdim, x1, y1 + 1, z1) - D(data, xdim, x1, y1 - 1, z1);
	    norms[x1*24+2] = D(data, xdim, x1, y1, z1 + 1) - D(data, xdim, x1, y1, z1 - 1);

	    norms[x1*24+9] = D(data, xdim, x1 + 1, y11, z1) - D(data, xdim, x1 - 1, y11, z1);
	    norms[x1*24+10] = D(data, xdim, x1, y11 + 1, z1) - D(data, xdim, x1, y11 - 1, z1);
	    norms[x1*24+11] = D(data, xdim, x1, y11, z1 + 1) - D(data, xdim, x1, y11, z1 - 1);

	    norms[x1*24+12] = D(data, xdim, x1 + 1, y1, z11) - D(data, xdim, x1 - 1, y1, z11);
	    norms[x1*24+13] = D(data, xdim, x1, y1 + 1, z11) - D(data, xdim, x1, y1 - 1, z11);
	    norms[x1*24+14] = D(data, xdim, x1, y1, z11 + 1) - D(data, xdim, x1, y1, z11 - 1);

	    norms[x1*24+21] = D(data, xdim, x1 + 1, y11, z11) - D(data, xdim, x1 - 1, y11, z11);
	    norms[x1*24+22] = D(data, xdim, x1, y11 + 1, z11) - D(data, xdim, x1, y11 - 1, z11);
	    norms[x1*24+23] = D(data, xdim, x1, y11, z11 + 1) - D(data, xdim, x1, y11, z11 - 1);

	    norms = normalize(norms, x1, 0);	
	    norms = normalize(norms, x1, 3);
	    norms = normalize(norms, x1, 4);
	    norms = normalize(norms, x1, 7);
	}
	norms[x1*24+3] = D(data, xdim, x1 + 1 + 1, y1, z1) - D(data, xdim, x1 + 1 - 1, y1, z1);
	norms[x1*24+4] = D(data, xdim, x1 + 1, y1 + 1, z1) - D(data, xdim, x1 + 1, y1 - 1, z1);
	norms[x1*24+5] = D(data, xdim, x1 + 1, y1, z1 + 1) - D(data, xdim, x1 + 1, y1, z1 - 1);

	norms[x1*24+6] = D(data, xdim, x1 + 1 + 1, y11, z1) - D(data, xdim, x1 + 1 - 1, y11, z1);
	norms[x1*24+7] = D(data, xdim, x1 + 1, y11 + 1, z1) - D(data, xdim, x1 + 1, y11 - 1, z1);
	norms[x1*24+8] = D(data, xdim, x1 + 1, y11, z1 + 1) - D(data, xdim, x1 + 1, y11, z1 - 1);

	norms[x1*24+15] = D(data, xdim, x1 + 1 + 1, y1, z11) - D(data, xdim, x1 + 1 - 1, y1, z11);
	norms[x1*24+16] = D(data, xdim, x1 + 1, y1 + 1, z11) - D(data, xdim, x1 + 1, y1 - 1, z11);
	norms[x1*24+17] = D(data, xdim, x1 + 1, y1, z11 + 1) - D(data, xdim, x1 + 1, y1, z11 - 1);

	norms[x1*24+18] = D(data, xdim, x1 + 1 + 1, y11, z11) - D(data, xdim, x1 + 1 - 1, y11, z11);
	norms[x1*24+19] = D(data, xdim, x1 + 1, y11 + 1, z11) - D(data, xdim, x1 + 1, y11 - 1, z11);
	norms[x1*24+20] = D(data, xdim, x1 + 1, y11, z11 + 1) - D(data, xdim, x1 + 1, y11, z11 - 1);
	norms = normalize(norms, x1, 1); 
	norms = normalize(norms, x1, 2);
	norms = normalize(norms, x1, 5);
	norms = normalize(norms, x1, 6);
    }
    for (x1 = 1; x1 < xdim-2; x1++) {
	if (index[x1]==0) {
                continue;
            }
	if (index[x1-1]==0) {
                continue;
            }
	norms[x1*24+0] = norms[x1*24-24+3];
        norms[x1*24+1] = norms[x1*24-24+4];
        norms[x1*24+2] = norms[x1*24-24+5];
	norms[x1*24+9] = norms[x1*24-24+6];
        norms[x1*24+10] = norms[x1*24-24+7];
        norms[x1*24+11] = norms[x1*24-24+8];
	norms[x1*24+12] = norms[x1*24-24+15];
        norms[x1*24+13] = norms[x1*24-24+16];
        norms[x1*24+14] = norms[x1*24-24+17];
	norms[x1*24+21] = norms[x1*24-24+18];
        norms[x1*24+22] = norms[x1*24-24+19];
        norms[x1*24+23] = norms[x1*24-24+20];
    }

    if (index[x1]==1) {
	offset = x1*24;
	nn[offset + 0] = D(data, xdim, x1 + 1, y1, z1) - D(data, xdim, x1 - 1, y1, z1);
	nn[offset + 1] = D(data, xdim, x1, y1 + 1, z1) - D(data, xdim, x1, y1 - 1, z1);
	nn[offset + 2] = D(data, xdim, x1, y1, z1 + 1) - D(data, xdim, x1, y1, z1 - 1);


	nn[offset + 6] = D(data, xdim, x1 + 1, y11, z1) - D(data, xdim, x1 - 1, y11, z1);
	nn[offset + 7] = D(data, xdim, x1, y11 + 1, z1) - D(data, xdim, x1, y11 - 1, z1);
	nn[offset + 8] = D(data, xdim, x1, y11, z1 + 1) - D(data, xdim, x1, y11, z1 - 1);


	nn[offset + 12] = D(data, xdim, x1 + 1, y1, z11) - D(data, xdim, x1 - 1, y1, z11);
	nn[offset + 13] = D(data, xdim, x1, y1 + 1, z11) - D(data, xdim, x1, y1 - 1, z11);
	nn[offset + 14] = D(data, xdim, x1, y1, z11 + 1) - D(data, xdim, x1, y1, z11 - 1);


	nn[offset + 18] = D(data, xdim, x1 + 1, y11, z11) - D(data, xdim, x1 - 1, y11, z11);
	nn[offset + 19] = D(data, xdim, x1, y11 + 1, z11) - D(data, xdim, x1, y11 - 1, z11);
	nn[offset + 20] = D(data, xdim, x1, y11, z11 + 1) - D(data, xdim, x1, y11, z11 - 1);

	for(i = 0; i < 8; i+=2) {
	    float d = (float) Math.sqrt(nn[offset + 3*i+0] * nn[offset + 3*i+0] +
			   nn[offset + 3*i+1] * nn[offset + 3*i+1] +
			   nn[offset + 3*i+2] * nn[offset + 3*i+2]);
	    if (d > EPSILON) {
		nn[offset + 3*i+0] /= d; nn[offset + 3*i+1] /= d; nn[offset + 3*i+2] /= d;
	    }
	    nn[offset + 3*i+3] = nn[offset + 3*i+0];
	    nn[offset + 3*i+4] = nn[offset + 3*i+1];
	    nn[offset + 3*i+5] = nn[offset + 3*i+2];
	}
    }
    if (NORMAL_TYPE > 1) {
	for (offset = 0, x1 = 0; x1 < xdim-1; x1++, offset+=24){
	    if (index[x1]==1){
		for(i = 0; i < 24; i++) {
                    nn[offset + i] = -nn[offset + i];
                }
            }
        }
    }
}

//
// called by get_cell_verts
//
private float linterp(float a1, float a2, float a, int b1, int b2){
    float result = (((a-a1) * (float)(b2-b1) / (a2-a1)) + (float)b1);
    return result;
}

//
// called by get_cell_verts_and_norms
//
private float lerp(float a1, float a2, float a, float b1, float b2){
    float result = (a-a1)/(a2-a1)*(b2-b1) + b1;
    return result;
}

//
// called by iso_surface
//
// calls lerp
//
private void get_cell_verts_and_norms(int[] index, float[] data, int y1, int z1, int xdim, float xtrans, float ytrans, float ztrans, 
			float thresh, float[] crossings, float[] cnorm, float[] normals)
{

    int x1, y2, z2;
    float threshold = thresh;
    float[] cn;
    int offset = 0;
    
    cn = cnorm;

    y2 = y1 + 1;
    z2 = z1 + 1;
    for (offset = 0, x1 = 0; x1 < xdim-1; x1++, offset += 24) {
	float cx, cy, cz;
	float nx, ny, nz;
        nx = ny = nz = 0.0f;
	int nedges;
	int crnt_edge;
	int x2 = x1 + 1;
	int i;
	int v1, v4, v5, v8;

	if (index[x1]==0) {
                continue;
            }

        //data
	v1 = z1*XDIMYDIM + y1*xdim + x1;
	v4 = v1 + xdim;
	v5 = v1 + XDIMYDIM;
	v8 = v4 + XDIMYDIM;

	nedges = cell_table.getCellEntry(index[x1]).nedges;
	for (i = 0; i < nedges; i++) {
	    crnt_edge = cell_table.getCellEntry(index[x1]).edges[i];
	    cx = xtrans; cy = ytrans; cz = ztrans;
	    switch (crnt_edge) {
	    case 1:
	    cx += linterp(data[v1 + 0], data[v1 + 1], threshold, x1, x2);
	    cy += (float) y1;
	    cz += (float) z1;
	    nx = lerp(data[v1 + 0], data[v1 + 1], threshold, cn[offset + 0*3+0], cn[offset + 1*3+0]);
	    ny = lerp(data[v1 + 0], data[v1 + 1], threshold, cn[offset + 0*3+1], cn[offset + 1*3+1]);
	    nz = lerp(data[v1 + 0], data[v1 + 1], threshold, cn[offset + 0*3+2], cn[offset + 1*3+2]);            
	    break;

	    case 2:
	    cy += linterp(data[v1 + 1], data[v4 + 1], threshold, y1, y2);
	    cx += (float) x2;
	    cz += (float) z1;
	    nx = lerp(data[v1 + 1], data[v4 + 1], threshold, cn[offset + 1*3+0], cn[offset + 2*3+0]);
	    ny = lerp(data[v1 + 1], data[v4 + 1], threshold, cn[offset + 1*3+1], cn[offset + 2*3+1]);
	    nz = lerp(data[v1 + 1], data[v4 + 1], threshold, cn[offset + 1*3+2], cn[offset + 2*3+2]);
	    break;

	    case 3:
	    cx += linterp(data[v4 + 0], data[v4 + 1], threshold, x1, x2);
	    cy += (float) y2;
	    cz += (float) z1;
	    nx = lerp(data[v4 + 1], data[v4 + 0], threshold, cn[offset + 2*3+0], cn[offset + 3*3+0]);
	    ny = lerp(data[v4 + 1], data[v4 + 0], threshold, cn[offset + 2*3+1], cn[offset + 3*3+1]);
	    nz = lerp(data[v4 + 1], data[v4 + 0], threshold, cn[offset + 2*3+2], cn[offset + 3*3+2]);
	    break;

	    case 4:
	    cy += linterp(data[v1 + 0], data[v4 + 0], threshold, y1, y2);
	    cx += (float) x1;
	    cz += (float) z1;
	    nx = lerp(data[v4 + 0], data[v1 + 0], threshold, cn[offset + 3*3+0], cn[offset + 0*3+0]);
	    ny = lerp(data[v4 + 0], data[v1 + 0], threshold, cn[offset + 3*3+1], cn[offset + 0*3+1]);
	    nz = lerp(data[v4 + 0], data[v1 + 0], threshold, cn[offset + 3*3+2], cn[offset + 0*3+2]);
	    break;

	    case 5:
	    cx += linterp(data[v5 + 0], data[v5 + 1], threshold, x1, x2);
	    cy += (float) y1;
	    cz += (float) z2;
	    nx = lerp(data[v5 + 0], data[v5 + 1], threshold, cn[offset + 4*3+0], cn[offset + 5*3+0]);
	    ny = lerp(data[v5 + 0], data[v5 + 1], threshold, cn[offset + 4*3+1], cn[offset + 5*3+1]);
	    nz = lerp(data[v5 + 0], data[v5 + 1], threshold, cn[offset + 4*3+2], cn[offset + 5*3+2]);
	    break;

	    case 6:
	    cy += linterp(data[v5 + 1], data[v8 + 1], threshold, y1, y2);
	    cx += (float) x2;
	    cz += (float) z2;
	    nx = lerp(data[v5 + 1], data[v8 + 1], threshold, cn[offset + 5*3+0], cn[offset + 6*3+0]);
	    ny = lerp(data[v5 + 1], data[v8 + 1], threshold, cn[offset + 5*3+1], cn[offset + 6*3+1]);
	    nz = lerp(data[v5 + 1], data[v8 + 1], threshold, cn[offset + 5*3+2], cn[offset + 6*3+2]);
	    break;

	    case 7:
	    cx += linterp(data[v8 + 0], data[v8 + 1], threshold, x1, x2);
	    cy += (float) y2;
	    cz += (float) z2;
	    nx = lerp(data[v8 + 1], data[v8 + 0], threshold, cn[offset + 6*3+0], cn[offset + 7*3+0]);
	    ny = lerp(data[v8 + 1], data[v8 + 0], threshold, cn[offset + 6*3+1], cn[offset + 7*3+1]);
	    nz = lerp(data[v8 + 1], data[v8 + 0], threshold, cn[offset + 6*3+2], cn[offset + 7*3+2]);
	    break;

	    case 8:
	    cy += linterp(data[v5 + 0], data[v8 + 0], threshold, y1, y2);
	    cx += (float) x1;
	    cz += (float) z2;
	    nx = lerp(data[v8 + 0], data[v5 + 0], threshold, cn[offset + 7*3+0], cn[offset + 4*3+0]);
	    ny = lerp(data[v8 + 0], data[v5 + 0], threshold, cn[offset + 7*3+1], cn[offset + 4*3+1]);
	    nz = lerp(data[v8 + 0], data[v5 + 0], threshold, cn[offset + 7*3+2], cn[offset + 4*3+2]);
	    break;

	    case 9:
	    cz += linterp(data[v1 + 0], data[v5 + 0], threshold, z1, z2);
	    cy += (float) y1;
	    cx += (float) x1;
	    nx = lerp(data[v1 + 0], data[v5 + 0], threshold, cn[offset + 0*3+0], cn[offset + 4*3+0]);
	    ny = lerp(data[v1 + 0], data[v5 + 0], threshold, cn[offset + 0*3+1], cn[offset + 4*3+1]);
	    nz = lerp(data[v1 + 0], data[v5 + 0], threshold, cn[offset + 0*3+2], cn[offset + 4*3+2]);
            break;

	    case 10:
	    cz += linterp(data[v1 + 1], data[v5 + 1], threshold, z1, z2);
	    cy += (float) y1;
	    cx += (float) x2;
	    nx = lerp(data[v1 + 1], data[v5 + 1], threshold, cn[offset + 1*3+0], cn[offset + 5*3+0]);
	    ny = lerp(data[v1 + 1], data[v5 + 1], threshold, cn[offset + 1*3+1], cn[offset + 5*3+1]);
	    nz = lerp(data[v1 + 1], data[v5 + 1], threshold, cn[offset + 1*3+2], cn[offset + 5*3+2]);
	    break;

	    case 11:
	    cz += linterp(data[v4 + 0], data[v8 + 0], threshold, z1, z2);
	    cy += (float) y2;
	    cx += (float) x1;
	    nx = lerp(data[v4 + 0], data[v8 + 0], threshold, cn[offset + 3*3+0], cn[offset + 7*3+0]);
	    ny = lerp(data[v4 + 0], data[v8 + 0], threshold, cn[offset + 3*3+1], cn[offset + 7*3+1]);
	    nz = lerp(data[v4 + 0], data[v8 + 0], threshold, cn[offset + 3*3+2], cn[offset + 7*3+2]);
	    break;

	    case 12:
	    cz += linterp(data[v4 + 1], data[v8 + 1], threshold, z1, z2);
	    cy += (float) y2;
	    cx += (float) x2;
	    nx = lerp(data[v4 + 1], data[v8 + 1], threshold, cn[offset + 2*3+0], cn[offset + 6*3+0]);
	    ny = lerp(data[v4 + 1], data[v8 + 1], threshold, cn[offset + 2*3+1], cn[offset + 6*3+1]);
	    nz = lerp(data[v4 + 1], data[v8 + 1], threshold, cn[offset + 2*3+2], cn[offset + 6*3+2]);            
	    break;

	    } 
            crossings[x1*13*3+crnt_edge*3+0] = cx;
            crossings[x1*13*3+crnt_edge*3+1] = cy;
            crossings[x1*13*3+crnt_edge*3+2] = cz;
            normals[x1*13*3+crnt_edge*3+0] = nx;
            normals[x1*13*3+crnt_edge*3+1] = ny;
            normals[x1*13*3+crnt_edge*3+2] = nz;
	} 
    }
}

//
// This subroutine will calculate the polygons 
//
// called by iso_surface
//
// calls add_polygon, getCellEntry
//
private int get_cell_polys_and_norms(int[] index, int xdim, float[] crossings, float[] normals)
{
    int num_o_polys, polys = 0;
    int poly;
    float[] p1 = new float[3];
    float[] p2 = new float[3];
    float[] p3 = new float[3];
    float[] n1 = new float[3];
    float[] n2 = new float[3];
    float[] n3 = new float[3];
    int x1;
    int count;
    for (count=0;count<3;count++){
        n1[count] = 0.0f;
        n2[count] = 0.0f;
        n3[count] = 0.0f;
        p1[count] = 0.0f;
        p2[count] = 0.0f;
        p3[count] = 0.0f;
    }
    int offset = 0;

    for (x1 = 0; x1 < xdim-1; x1++) {
	if (index[x1]==0) {
                continue;
            }
	num_o_polys = cell_table.getCellEntry(index[x1]).npolys;
	for (poly = 0; poly < num_o_polys; poly++) {

	    offset = x1*13*3+(cell_table.getCellEntry(index[x1]).polys[poly*3])*3+0;
            for (count=0;count<3;count++){
                p1[count] = crossings[offset+count];
            }
	    offset = x1*13*3+(cell_table.getCellEntry(index[x1]).polys[poly*3 + 1])*3+0;
            for (count=0;count<3;count++){
                p2[count] = crossings[offset+count];
            }
	    offset = x1*13*3+(cell_table.getCellEntry(index[x1]).polys[poly*3 + 2])*3+0;
            for (count=0;count<3;count++){
                p3[count] = crossings[offset+count];
            }
            
	    if ((p1[0] == p2[0] && p1[1] == p2[1] && p1[2] == p2[2]) ||
		(p1[0] == p3[0] && p1[1] == p3[1] && p1[2] == p3[2]) ||
		(p2[0] == p3[0] && p2[1] == p3[1] && p2[2] == p3[2]))  {
//		printf("addpoly - degenerate triangle\n");
		polys--;
		continue;
	    }

	    offset = x1*13*3+(cell_table.getCellEntry(index[x1]).polys[poly*3])*3+0;
            for (count=0;count<3;count++){
                n1[count] = normals[offset+count];
            }
	    offset = x1*13*3+(cell_table.getCellEntry(index[x1]).polys[poly*3 + 1])*3+0;
            for (count=0;count<3;count++){
                n2[count] = normals[offset+count];
            }
	    offset = x1*13*3+(cell_table.getCellEntry(index[x1]).polys[poly*3 + 2])*3+0;
            for (count=0;count<3;count++){
                n3[count] = normals[offset+count];
            }
            
	    add_polygon(p1, p2, p3, n1, n2, n3);
	}

	polys += num_o_polys;
    }
    return polys;
}




//
// This subroutine finds the maximum & minimum data values 
//
// not used
//
private float[] get_max_min(short[] data, int xdim, int ydim, int zdim)
{
    int enddata;
    float max, min;
    int count;
    
    enddata = xdim * ydim * zdim;
    max = min = data[0];
    for (count=0; count < enddata; count++) {
	if (data[count] > max) {
                max = data[count];
            }
	if (data[count] < min) {
                min = data[count];
            }
    }
    
    float[] result = new float[2];
    result[0] = max;
    result[1] = min;
    
    return(result);
}

//
// This subroutine calculates a normal from three vertices 
//
// called by get_cell_polys
//
private float[] calc_normal(float[] p1,float[] p2,float[] p3,float[] n)
{
    float[] u = new float[3];
    float[] v = new float[3];
    float sum, mag;

    u[0] = p3[0] - p2[0];
    u[1] = p3[1] - p2[1];
    u[2] = p3[2] - p2[2];

    v[0] = p1[0] - p2[0];
    v[1] = p1[1] - p2[1];
    v[2] = p1[2] - p2[2];

    n[0] = u[1] * v[2] - u[2] * v[1];
    n[1] = u[2] * v[0] - u[0] * v[2];
    n[2] = u[0] * v[1] - u[1] * v[0];

    sum = n[0] * n[0] + n[1] * n[1] + n[2] * n[2];
    mag = (float) Math.sqrt((double) sum);

    if (mag == 0.0){
	mag = 1.0f;
    }

    n[0] = n[0] / mag;
    n[1] = n[1] / mag;
    n[2] = n[2] / mag;

    return(n);
}


//
// This subroutine stores a polygon (triangle) in a list of vertices 
// and connectivity.  This list can then be written out in different 
// file formats. 
//
// new space is allocated by vert_alloc if necessary
// NUM_VERTICES is incremented by 3
//
// pX[0] contains x coordiate of point X
// pX[1] contains y coordiate of point X
// pX[2] contains z coordiate of point X
//
// nX[0] contains x component of normal X
// nX[1] contains y component of normal X
// nX[2] contains z component of normal X
//
// called by get_cell_polys, get_cell_polys_and_norms
//
// calls vert_alloc
//
private void add_polygon(float[] p1,float[] p2,float[] p3, float[] n1,float[] n2,float[] n3)
{
	
//	System.out.println(Arrays.toString(p1));
//	System.out.println(Arrays.toString(p2));
//	System.out.println(Arrays.toString(p3));
//	System.out.println();
	
	
	for (TriangleListener triListener : listeners) {
		triListener.processTriangle(
				new float[][] { p1, p2, p3}, new float[][] { n1, n2, n3} );
	}
	if (true) return;
	
	
	
    int size;
    int ptr;
    int offset;
    
    // see if we have enough space to store the vertices 
    if (NUM_VERTICES >= (VERT_LIMIT - 3)) {
	// get more space 
	VERT_LIMIT += VERT_INCR;
	size = VERT_LIMIT;
	vert_alloc(size);
    }
    // store the vertices 
    ptr = NUM_VERTICES * 3;
    offset = 0;
    VERTICES[ptr++]  = p1[offset++];		// x of first vertex 
    VERTICES[ptr++]  = p1[offset++];		// y of first vertex 
    VERTICES[ptr++]  = p1[offset++];		// z of first vertex 
    offset = 0;
    VERTICES[ptr++]  = p2[offset++];		// x of second vertex
    VERTICES[ptr++]  = p2[offset++];		// y of second vertex 
    VERTICES[ptr++]  = p2[offset++];		// z of second vertex 
    offset = 0;
    VERTICES[ptr++]  = p3[offset++];		// x of third vertex 
    VERTICES[ptr++]  = p3[offset++];		// y of third vertex 
    VERTICES[ptr++]  = p3[offset++];		// z of third vertex

    ptr = NUM_VERTICES * 3;
    offset = 0;
    NORMALS[ptr++] = n1[offset++];
    NORMALS[ptr++] = n1[offset++];
    NORMALS[ptr++] = n1[offset++];
    offset = 0;
    NORMALS[ptr++] = n2[offset++];
    NORMALS[ptr++] = n2[offset++];
    NORMALS[ptr++] = n2[offset++];
    offset = 0;
    NORMALS[ptr++] = n3[offset++];
    NORMALS[ptr++] = n3[offset++];
    NORMALS[ptr++] = n3[offset++];

    NUM_VERTICES += 3;	
}

//
// This subroutine is for allocating memory for the vertex lists 
// NORMALS and VERTICES
// input is the size of the new arrays/3
// NORMALS and VERTICES are set to the input size * 3 and current values
// are preserved if possible cropped if not and new values are set to zero
//
// called by add_polygon
//
void vert_alloc(int size)
{
    float[] tempV = new float[size * 3];
    float[] tempN = new float[size * 3];
    int count;
    if (VERTICES!=null){
        for(count=0;count<VERTICES.length;count++){
            tempV[count] = VERTICES[count];
        }
        for(count=VERTICES.length;count<tempV.length;count++){
            tempV[count] = 0.0f;
        }
    }
    if (NORMALS!=null){
        for(count=0;count<NORMALS.length;count++){
            tempN[count] = NORMALS[count];
        }
        for(count=NORMALS.length;count<tempN.length;count++){
            tempN[count] = 0.0f;
        }
    }
    VERTICES = tempV;
    NORMALS = tempN;
}

//
// called by apply
//
void release_memory()
{
    if (VERTICES!=null) {
	VERTICES = null;
    }
    if (NORMALS!=null) {
	NORMALS = null;
    }
// htable is freed by freehash
}

/*#ifndef __POINT_H__
#define __POINT_H__

///////////////////////////////////////////////////////////////////////////
//
// File: Point.h
//
// Author: Rob Teichman
//
// Purpose: Point Class definition
//  A single point in R^3 and related operations.
// 
///////////////////////////////////////////////////////////////////////////
//
// RCS Id: $Id: SurfaceGen.java,v 1.3 2008/05/20 18:03:34 hennessey Exp $
//
// Revision History
//
// $Log: SurfaceGen.java,v $
// Revision 1.3  2008/05/20 18:03:34  hennessey
// fixes for first bug fixes found by static analysis
//
// Revision 1.2  2008/01/10 18:18:12  hennessey
// updates for netbeans 6
//
// Revision 1.1  2006/03/20 19:06:48  hennessey
// initial import
//
// Revision 1.3  2002/08/25 06:51:44  hennessey
// surface gen work
//
// Revision 1.2  2002/05/06 10:37:28  hennessey
// no message
//
// Revision 1.1  1999/10/01 15:27:37  kem
// Initial revision
//
// Revision 1.8  1999/07/09 17:47:07  RAZORDB
// Make Changes to compile under Linux and g++
//
// Revision 1.7  1998/05/18 19:13:01  rst
// Changing float to double for accuracy required by eigen shapes
//
// Revision 1.5  1998/03/26 17:37:01  rst
// operator/= and operator*=
//
// Revision 1.3  1997/10/20 19:28:49  rst
// bug in call to seeded invFieldTrans
//
// Revision 1.2  1997/08/26 15:43:19  kem
// Change invFieldTrans() hField argument to reference
//
// Revision 1.1  1997/08/22 20:35:36  rst
// Initial revision
//
// Revision 1.3  1997/08/22 20:24:13  rst
// Cleaning up files
//
// Revision 1.2  1997/08/20 19:28:15  rst
// Added invFieldTransformation routine
//
// Revision 1.1  1997/08/05 14:28:08  rst
// Initial revision
//
///////////////////////////////////////////////////////////////////////////
#include <OS.h>
#include <iostream.h>
#include <math.h>
#include <ADL/Array3D.h>


class Point {

public:

  // Default constructor.
  Point() :
    mX(0), mY(0), mZ(0) {}

  // Create point init'd to another Point.
  Point(Point const &P) :
    mX(P.mX), mY(P.mY), mZ(P.mZ) {}

  // Create point init'd to coords specified
  Point(double x, double y, double z) :
    mX(x), mY(y), mZ(z) {}
  
  // Default Destructor.
  ~Point() {}

  // KWD
  Point(double A) : mX(A), mY(A), mZ(A) {}
  Point(int A) : mX((double)A), mY((double)A), mZ((double)A) {}

  // Set a point to a location.
  void set(double x, double y, double z);


  // Get value of one dimension of a Point
  double x() const
  { return (mX); }
  double y() const
  { return (mY); }
  double z() const
  { return (mZ); }
  
  // Set a point from another point
  Point & operator= (Point const &P);
  Point & operator= (double P);
  Point & operator= (int P);

  // Equality/inequality of points.  Two points are equal
  // if they are equal in all dimensions.
  bool operator == (Point const &P) const;
  bool operator != (Point const &P) const;

  // KWD : Array1D<Point> compatibility
  bool operator > (Point const &P) const { return(this->norm() > P.norm()); }
  bool operator < (Point const &P) const { return(this->norm() < P.norm()); }
  
  // Returns the length of the vector
  double norm() const
  { return (sqrt(this->innerProd(*this))); }

  // Basic math functions
  //
  // Addition
  Point & operator+= (Point const &P);
  Point operator+ (Point const &P) const;

  // Subtraction
  Point & operator-= (Point const &P);
  Point operator- (Point const &P) const;

  // inner product
  double innerProd (Point const &P) const;

  // cross product
  Point cross (Point const &P) const;

  // division by a scalar
  Point & operator/= (double const &f);
  Point operator/ (double const &f) const;
  Point operator/ (Point const &f) const;

  // multiplication by a scalar
  Point & operator*= (double const &f);
  Point operator* (double const &f) const;

  // KWD : for Array1D compatibility
  Point operator* (const Point &P) const { return(*this); }

  // inverse field transformation on a point
  bool invFieldTrans (const Array3D<float> &H);
  bool invFieldTrans (const Array3D<float> &H, const Point seed);

  // Print the value of a point.
  void print(ostream &os) const;
  

private:

  // Data Elements
  double mX,
    mY,
    mZ;

};


//
// Definition of non-member print function for cout
//
ostream & operator<< (ostream &os, Point const &P);



#endif // __POINT_H__
*/

/*
#ifndef _Surface_h_
#define _Surface_h_

///////////////////////////////////////////////////////////////////////////
//
// File: Surface.h
//
// Author: Rob Teichman
//
// Purpose: Surface Class Definition
//  This class is a base class for representation of surfaces.  It includes
// basic operations performed on surfaces.
//
// All derrived classes must implement the read and write routines.
// 
///////////////////////////////////////////////////////////////////////////
//
// RCS Id: $Id: SurfaceGen.java,v 1.3 2008/05/20 18:03:34 hennessey Exp $
//
// Revision History
//
// $Log: SurfaceGen.java,v $
// Revision 1.3  2008/05/20 18:03:34  hennessey
// fixes for first bug fixes found by static analysis
//
// Revision 1.2  2008/01/10 18:18:12  hennessey
// updates for netbeans 6
//
// Revision 1.1  2006/03/20 19:06:48  hennessey
// initial import
//
// Revision 1.3  2002/08/25 06:51:44  hennessey
// surface gen work
//
// Revision 1.2  2002/05/06 10:37:28  hennessey
// no message
//
// Revision 1.2  1999/10/11 14:57:16  sarang
// revision
//
// Revision 1.1  1999/10/01 15:27:30  kem
// Initial revision
//
// Revision 1.19  1999/09/27 20:21:35  RAZORDB
// changes to contour generation (remove cache)
//
// Revision 1.18  1999/09/21 21:56:16  RAZORDB
// TDL update
//
// Revision 1.17  1999/09/21 17:10:48  RAZORDB
// debugging changes
//
// Revision 1.16  1999/07/16 20:03:27  rst
// Add Polygon fliping routines
//
// Revision 1.15  1999/07/09 17:47:15  RAZORDB
// Make Changes to compile under Linux and g++
//
// Revision 1.14  1999/05/12 17:25:36  RAZORDB
// update
//
// Revision 1.13  1999/01/19 17:22:28  RAZORDB
// kwd
//
// Revision 1.9  1998/05/18 19:14:48  rst
// Surface class changes
//
// Revision 1.8  1998/05/12 20:10:28  sarang
// Add functions needed for Schiz.
//
// Revision 1.7  1998/03/05 18:02:06  rst
// inverse apply, += for surfaces
//
// Revision 1.6  1997/11/26 22:42:18  rst
// updated and added new routines
//
// Revision 1.5  1997/10/31 16:05:05  rst
// Added friend directive for utilities
//
// Revision 1.4  1997/10/23 19:32:33  rst
// fixing and optimizing
//
// Revision 1.3  1997/10/10 19:46:38  rst
// Add neighborhood generation and optimize
//
// Revision 1.2  1997/08/26 15:43:45  kem
// Change invFieldTrans() hField argument to reference
//
// Revision 1.1  1997/08/22 20:35:32  rst
// Initial revision
//
///////////////////////////////////////////////////////////////////////////

#include <OS.h>
#include <iostream.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/param.h>
#include <ADL/Array1D.h>
#include <ADL/Array2D.h>
#include <ADL/Vector.h>
#include <ADL/Matrix.h>
#include <ADL/Lapack.h>
#include <TDL/Neighborhood.h>
#include <TDL/Point.h>
#include <TDL/Line.h>
#include <TDL/IDLdefines.h>
#include <TDL/ItXWorkClass.h>


#ifndef TOLERANCE
#define TOLERANCE 0.0001                   // for curvature calculations
#endif

//class SurfaceSet;

class Surface : public ItXWorkClass
{

friend class SurfaceUtils;
friend class SurfaceGrow;
friend class SurfaceReducer;
friend class Embed;
friend class SurfaceGen;
friend class SurfaceTracker;

public:
  // for genCurvature()
  enum CurvatureType {
        NotValid,MeanCurvature, MaxCurvature, MinCurvature, GaussCurvature,
        UserDefined};

  // default constructor
  Surface() :
  mNumVert(0), mNumPoly(0), mNormDirty(true), 
    mContDirty(false), mNbhdDepth(0), mVerbose(false)
    {
      mCurveType=NotValid;   
      mSagittalContours.setDim(512);
      mCoronalContours.setDim(512);
      mAxialContours.setDim(512);
      sprintf(filename,"<surface>");
    }

  // copy constructor
  Surface(Surface &S);

  // destructor
  virtual ~Surface() {}

 ////  load and save curvature to disk file
  ItXECode loadCurvature(char *);
  ItXECode saveCurvature(char *);

  //
  // virtual functions for file i/o
  //
  virtual bool read (istream &inFile = cin)
  { cerr << "Cannot call read on type Surface" << endl; return false; };

  virtual bool read (const char *fileName)
  { cerr << "Cannot call read on type Surface" << endl; return false; };

  virtual bool write (ostream &outFile = cout) const
  { cerr << "Cannot call write on type Surface" << endl; return false; };

  virtual bool write (const char *fileName) const
  { cerr << "Cannot call write on type Surface" << endl; return false; };

  bool Load(const char *fileName)
	{ SetFilename(fileName); return(read(fileName)); }

  bool Save(const char *fileName)
	{ SetFilename(fileName); return(write(fileName)); }

  void  SetFilename(const char *nm);
  inline const char *Filename() { return(filename); }

  //
  // direct surface modification routines
  //
  int addFacet(int a, int b, int c);
  int addVert(Point const &p);
  int addVert(double x, double y, double z)
  { Point p(x,y,z); return addVert(p); }

  int changeVert(int n, double x, double y, double z);
  int changeVert(int n, Point x);

  //
  // if surface topology or vertex values
  // change, this will invalidate neighborhoods,
  // curvature, normals, etc.
  //

  void verticesChanged();
  void geometryChanged();
  void flipPolyOrientation(int i); // Flip the Orientation of the ith polygon
  void flipOrientation(); // FLip Orientation of the entire surface

  // cleanup routine
  bool clean();

  // assigment operator
  Surface & operator= (Surface const &S);

  // addition operator, adds two surfaces vertex-wise
  Surface & operator+= (Surface const &S);

  // division by scaler
  Surface & operator/= (double const f)
  { this->scale(1.0/f); return *this; }

  // combine two surfaces into one
  Surface & combine (Surface const &S);

  // generate normals
  void genNormals();
  void genUnitNormals();

  // compute neighborhoods of given size at each vertex
  void genNeighborhoods(int Size = 1);
  void freeNeighborhoods()		{ mNbhd.setDim(0); mNbhdDepth = 0; }

  // compute curvature
  ItXECode genCurvature(CurvatureType, int depth = 2);
  void     freeCurvature()		{ mCurvature.setDim(0); }
  void     setCurvature(Array1D<double>&);

  // compute the Euler Characteristic number
  int euler();

  // compute volume enclosed by surface
  double volume();

  // check if surface is closed
  bool isClosed(Array1D<int> &vertlist);

  // compute the centroid of the surface
  int getCentroid(Point *cent);

  int getNeighborhoodDepth()	{ return(mNbhdDepth); }

  // compute the simple centroid of the surface
  // avg of vertices
  int getSimpleCentroid(Point *cent);

  inline int getNumVert()	{ return(mNumVert); }
  inline int getNumPoly()	{ return(mNumPoly); }

  inline Point getVert(int i)  { return mVert[i]; }
  inline int *getFacet(int i)  { return (&mFacet[i][0]); }

  inline const Array1D<Point>  &vertices()  const { return(mVert);      }
  inline const Array1D<Point>  &normals()   const { return(mNorm);      }
  inline const Array1D<Point>  &unormals()  const { return(mUNorm);     }
  inline const Array2D<int>    &facets()    const { return(mFacet);     }
  inline const Array1D<double> &curvature() const { return(mCurvature); }
  inline const Array1D<Neighborhood> &neighborhood() const { return(mNbhd); }

  inline Array1D<Point>  &vertices()  { return(mVert);      }
  inline Array2D<int>    &facets()    { return(mFacet);     }

  inline void setSize(int nvert, int nfacet) {
	mVert.setDim(nvert);
	mFacet.setDim(nfacet,3);
        mNumVert = nvert;
        mNumPoly = nfacet;
	geometryChanged();
	}

  //
  // Affine transformations
  //

  // generic affine transformation (3x3 matrix and 3x1 vector,
  // Ax+B applied to each vertex x in the surface)
  void affine(Matrix<double> const &A, Vector<double> const &B);
  void affine(Matrix<float> const &A, Vector<float> const &B);

  void scale(double scaleFactor)	// scale uniformly in all dimensions
    { this->scale(scaleFactor, scaleFactor, scaleFactor); }

  void scale(double scaleX, double scaleY, double scaleZ);
  void translate(double transX, double transY, double transZ);
  void rotate(double roll, double pitch, double yaw);

  // transformations around centriod
  void rotateCentroid(double roll, double pitch, double yaw);

  //
  // Functions to clean up surfaces with common problems
  //
  void removeDegenerateTriangles(double tolerance = 0);
  void fixNormals();

  // Functions to check if data is valid
  bool isInit() const		// returns true if facet and vert are loaded
  { return ( mNumVert != 0 && mNumPoly != 0); }
  bool hasNorms() const		// returns true if norm are valid
  { return (!mNorm.isEmpty() && !mNormDirty); }
  bool hasUNorms() const	// returns true if uNorm are valid
  { return (!mUNorm.isEmpty() && !mNormDirty); }
  bool hasNbhd(int need_depth = 1) const	// returns true if nbhd has been calculated
  { return ((!mNbhd.isEmpty())&&(mNbhdDepth == need_depth)); }	// at that particular depth
  bool hasCurvature() const	// returns true if curvature is valid
  { return (!mCurvature.isEmpty()); }

  //
  // Functions to get contours
  //
  int getSagittalContour (Line *cont, double x, double y, double z);
  int getCoronalContour (Line *cont, double x, double y, double z);
  int getAxialContour (Line *cont, double x, double y, double z);

  // temporary functions (for contour generation)
  double findXMin() const;
  double findXMax() const;
  double findYMin() const;
  double findYMax() const;
  double findZMin() const;
  double findZMax() const;

  // field transformation
  int applyFieldTrans(const Array3D<float> &hFields);

  // inverse field transformation routine
  bool invFieldTrans(const Array3D<float> &hFields);

  // split a facet into four and fix adjacent patches as needed
  int refineTriangle(int target);

  // split every facet into four facets
  int refineSurface();

  // Function to compute the innerproduct 
  double innerproduct(Surface &basesurf,Surface const &eigensurface);

//  Array1D<double> ComputeEigenSurf(SurfaceSet &inSurfs, SurfaceSet *EigenSurfaces);

  // set/get verbose flag
  inline void setVerbose(int verbose);
  inline bool getVerbose();
  void extract(int ind);
  int estimatePolyCount(double tolerance);
  void decimate(double tolerance);
  void isEdge(Array1D<int> &edge);
  void generateFlowers(char *file);
  void cutSurface(Array1D<int> &selected);	  
  int findClosestPoint(Point P);
  void readSettings(char *fname);
 void getSettings(Array1D<Point> &points, Array1D<char *> &names);
protected:

  // verbose flag for surface class
  bool mVerbose;

  //const double TOLERANCE=0.0001; // for curvature calculations - now a pound-define
  //const int MAX_PARTS=1;	// For now only handles simple surfaces,
				// not scenes (or groups) of surfaces

  char filename[MAXPATHLEN];	// Filename of surface (if any)

  // Fundamental surface data
  int mNumVert;			// total number of vertices
  int mNumPoly;			// total number of polygons
 CurvatureType mCurveType;
  Array2D<int> mFacet;		// array that describes the connectivity of
				// vertices to form triangles
  Array1D<Point> mVert;		// list of vertices for the surface

  // additional useful surface information, initialized as needed
  Array1D<Point> mNorm;		// list of normals at each vertex
  Array1D<Point> mUNorm;	// list of unit normals at each vertex
  Array1D<Neighborhood> mNbhd;	// contains neighborhood of each vertex
  int     mNbhdDepth;

  // contours
  Array1D<Line> mAxialContours;
  Array1D<Line> mSagittalContours;
  Array1D<Line> mCoronalContours;

  // curvature
  Array1D<double> mCurvature;

  // status bits
  bool mNormDirty;
  bool mContDirty;
  bool mCurveDirty;

  Array1D<Point> mProbes;
  Array1D<char *> mProbeNames;
  
private:

  void tagEdges(Array1D<int> &edges);

  // split facet function, called from refineTriangle
  inline void splitFacet(int face, int v1, int v2, int v3, int nv);

  // function to extract contour traces for each plane
  bool genContours (int xSlice, int ySlice, int zSlice);

  // other contour functions
  void findMinMax(double *xMin, double *xMax,
		  double *yMin, double *yMax,
		  double *zMin, double *zMax, int facet) const;
  void doFindMinMax (double *min, double *max,
		     double val0, double val1, double val2) const;
  bool findSagittalIntersect(Point *p1, Point *p2,
			      int facet, double xVal) const;
  bool findCoronalIntersect(Point *p1, Point *p2,
			    int facet, double yVal) const;
  bool findAxialIntersect(Point *p1, Point *p2,
			  int facet, double zVal) const;

  bool sagittalContourExists(int slice) const;
  bool coronalContourExists(int slice) const;
  bool axialContourExists(int slice) const;

  // curvature fcns
  static void getBasisVectors(Point &b1, Point &b2, Point &b3, const Point &n);
  static void getPlane(double &A, double &B, double &C, double &D,
                        const Point &p, const Point &n);

};


//
// inline bodies
//
inline void Surface::setVerbose(int verbose = 1)
{
  mVerbose = (verbose != 0);
}

inline bool Surface::getVerbose()
{
  return(mVerbose);
}

#endif // __SURFACE_H__

*/

/*private int hashit(float *v)
{
   unsigned long *p = (unsigned long *)v;
   return (((p[0]*283+p[1])*283)+p[2]) % TABLE_SIZE;
}


//
//  take a list of triangle vertices and normal vectors, produce
//  a connectivity array with redundant vertices eliminated.
//
float ***tricompactn(float *v, float *n, int nv)
{
    int i;
    float ***conn = (float ***)malloc((nv+1)*sizeof(float **));
    float **vert = (float **)malloc(nv*sizeof(float *));
    int nvert = 0;
    int dup = 0;

    if (!conn || !vert) {
	fprintf(stderr, "tricompact: out of memory\n");
	exit(1);
    }
    for(i = 0; i < nv; i++) {
	int h = hashit(v+3*i);
	hash *hh;

	for(hh = htable[h]; hh; hh=hh->next) {
	    if (hh->v1[0] == v[3*i+0] &&
		hh->v1[1] == v[3*i+1] &&
		hh->v1[2] == v[3*i+2] &&
		hh->n1[0] == n[3*i+0] &&
		hh->n1[1] == n[3*i+1] &&
		hh->n1[2] == n[3*i+2]) {
		    goto vdup;
	    }
	}
	vert[nvert] = v+3*i;
	hh = newhash();
	hh->next = htable[h]; htable[h] = hh;
	hh->v1 = v+3*i; hh->n1 = n+3*i; hh->v = vert+nvert;
	conn[i] = vert+nvert;
	nvert++;
	continue;
vdup:
	conn[i] = hh->v;
	dup++;
    }
    conn[i] = vert+nvert;
    if(VERBOSE)
	printf ("verts = %d dups = %d\n", nv, dup);

    { int vp = 0, np = 0;
    for(i = 0; i < TABLE_SIZE; i++) {
	hash *p;
	if (!htable[i]) continue;
	for(p = htable[i]; p; p = p->next) {
	    vp++;
	}
	np++;
    }
    printf("vp %d np %d  %f\n", vp, np, (float)vp/np);
    }
    freehash();
    return conn;
}



float ***tricompact(float *v,int nv)
{
    int i;
    float ***conn = (float ***)malloc((nv+1)*sizeof(float **));
    float **vert = (float **)malloc(nv*sizeof(float *));
    int nvert = 0;
    int dup = 0;

    for(i = 0; i < nv; i++) {
	int h = hashit(v+3*i);
	hash *hh;

	// if vertex already exists - ignore 
	for(hh = htable[h]; hh; hh=hh->next) {
	if (hh->v1) {
	    if (hh->v1[0] == v[3*i+0] &&
		hh->v1[1] == v[3*i+1] &&
		hh->v1[2] == v[3*i+2]) {
		    goto vdup;
	    }
	 }	
	}
	vert[nvert] = v+3*i;
	hh = newhash();
	hh->next = htable[h]; htable[h] = hh;
	hh->v1 = v+3*i; hh->v = vert+nvert;
	conn[i] = vert+nvert;
	nvert++;
	continue;
vdup:
	conn[i] = hh->v;
	dup++;
    }
    conn[i] = vert+nvert;
    if(VERBOSE) printf ("verts = %d dups = %d\n", nv, dup);

    { int vp = 0, np = 0;
    for(i = 0; i < TABLE_SIZE; i++) {
	hash *p;
	if (!htable[i]) continue;
	for(p = htable[i]; p; p = p->next) {
	    vp++;
	}
	np++;
    }
    }
    freehash();
    return conn;
}



hash *newhash() 
{
    int N = 1001

    if (!avail) {
	cur = (hash *) malloc(N*sizeof h[0]);
	cur->next = manage;
	manage = cur;
	avail = N-1;
	cur++;
    }
    avail--; h = cur++;
    return h;
}



private void freehash() {
    manage = null;
}
*/

//
// calls iso_surface, release_memory
//
//public boolean apply(short[] indat, Surface *S, int nx, int ny, int nz, float thresh, float xt,float yt,float zt)
public boolean apply(float[] indat, int nx, int ny, int nz, float thresh, float xt,float yt,float zt)
{
        if(indat==null) {
            return false;
        }

	int i;

	VERTICES = null;         // a pointer to the x coordinates
	NORMALS = null;          // a pointer to the x normals
 	NUM_VERTICES = 0;        // number of vertices currently stored
	VERT_LIMIT = 0;          // currently allocated space for vertices
	VERT_INCR = 20000;       // allocate space for this many vertices at a
	VERBOSE = 1;

	avail = 0;
	/*manage = (hash *) malloc(sizeof(hash));
        manage->v1   = NULL;
	manage->v1   = NULL;
        manage->n1   = NULL;
        manage->v    = NULL;
        manage->next = NULL;


        for (i=0;i<TABLE_SIZE;i++){
           if(!(htable[i] = (hash *)malloc(sizeof(hash)))) {
              cout << "out of memory!!" << endl;
              exit(1);
           }
           htable[i]->v1   = NULL;
           htable[i]->n1   = NULL;
           htable[i]->v    = NULL;
           htable[i]->next = NULL;
         
	float ***conn;
	float **vlist;
        }*/

	xtrans = xt;
	ytrans = yt;
	ztrans = zt;

	System.out.println("Generating Iso Surface");
	System.out.println("(nx,ny,nz) = " + nx + "," + ny + "," + nz); 
	System.out.println("Threshold = " + thresh);

	iso_surface(indat, nx, ny, nz, thresh);

	System.out.println("Done Generating iso_surface");
	System.out.println("NUM_VERTICES =  " + NUM_VERTICES);

	int j,a,b,c;
	int uniq;	// Unique triangels 

//	System.out.println("Coampacting Surface ");

	/*conn = tricompact(VERTICES, NUM_VERTICES);

	System.out.println("Done compacting Surface");

	uniq  = conn[NUM_VERTICES] - conn[0];
	vlist = conn[0];

	S->mNumVert = uniq;
	(S->mVert).setDim(uniq);

	S->mNumPoly = NUM_VERTICES/3;
	(S->mFacet).setDim(NUM_VERTICES/3,3);

	// Now set the vertices

	for(i = 0;i < uniq;i++) {
	   float *v = vlist[i];
	   S->mVert[i].set(v[0],v[1],v[2]);
	}

	for(j = 0;j < NUM_VERTICES/3;j++) {
	   i = j*3;
	   a = conn[i]-conn[0];
	   b = conn[i+1]-conn[0];
	   c = conn[i+2]-conn[0];
	   S->mFacet[j][0] = a;
	   S->mFacet[j][1] = b;
	   S->mFacet[j][2] = c;
	}

	System.out.println("Numvert = " + S->mNumVert);
	System.out.println("NumPoly = " + S->mNumPoly);
        */
	avail = 0;

//	release_memory();
	
	return(true);
}



	public float[] getVertices() {
		if (VERTICES.length == NUM_VERTICES) {
			return VERTICES;
		}
		else {		
			float[] vertArray = new float[NUM_VERTICES*3];
			System.arraycopy(VERTICES, 0, vertArray, 0, NUM_VERTICES*3);
			return vertArray;
		}		
	}
	
	public float[] getNormals() {
		if (NORMALS.length == NUM_VERTICES) {
			return NORMALS;
		}
		else {		
			float[] normArray = new float[NUM_VERTICES*3];
			System.arraycopy(NORMALS, 0, normArray, 0, NUM_VERTICES*3);
			return normArray;
		}		
	}

	
	
	
	
	
	public void addTriangleListener(TriangleListener l) {
		this.listeners.add(l);
	}
	
	public void removeTriangleListener(TriangleListener l) {
		this.listeners.remove(l);
	}
	
	public interface TriangleListener {
		/**
		 * Array will always be 3x3 elements representing x,y,z of 3 
		 * triangle vertices: <br>
		 * vertices[0][0] = x value for first vertex <br>
		 * vertices[0][1] = y value for first vertex <br>
		 * vertices[0][2] = z value for first vertex <br>
		 * vertices[1][0] = x value for second vertex <br>
		 * ...  
		 * @param coords
		 */
		public void processTriangle(float[][] vertices, float[][] normals);
	}
	
};
