package gov.noaa.ncdc.wct.isosurface;
/*
 * CellEntry.java
 *
 */



/**
 *
 * @author  Administrator
 */
public class CellEntry {
  int nverts;
  int[] verts;
  int nedges;
  int[] edges;
  int npolys;
  int[] polys;
    
    /** Creates a new instance of CellEntry */
    public CellEntry() {
      nverts = 8;
      verts = new int[nverts];
      nedges = 12;
      edges = new int[nedges];
      npolys = 30;
      polys = new int[npolys];
    }
    
    public void init(int nv, int v1, int v2, int v3, int v4, int v5, int v6, int v7, int v8,
                int ne, int e1, int e2, int e3, int e4, int e5, int e6, int e7, int e8, int e9, int e10, int e11, int e12,
                int np, int p1, int p2, int p3, int p4, int p5, int p6, int p7, int p8, int p9, int p10,
                int p11, int p12, int p13, int p14, int p15, int p16, int p17, int p18, int p19, int p20,
                int p21, int p22, int p23, int p24, int p25, int p26, int p27, int p28, int p29, int p30){
        int count;
        
        nverts = nv;
        count = 0;
        verts[count++]=v1;
        verts[count++]=v2;
        verts[count++]=v3;
        verts[count++]=v4;
        verts[count++]=v5;
        verts[count++]=v6;
        verts[count++]=v7;
        verts[count++]=v8;
    
        nedges = ne;
        count = 0;
        edges[count++]=e1;
        edges[count++]=e2;
        edges[count++]=e3;
        edges[count++]=e4;
        edges[count++]=e5;
        edges[count++]=e6;
        edges[count++]=e7;
        edges[count++]=e8;
        edges[count++]=e9;
        edges[count++]=e10;
        edges[count++]=e11;
        edges[count++]=e12;
        
        npolys = np;
        count = 0;
        polys[count++]=p1;
        polys[count++]=p2;
        polys[count++]=p3;
        polys[count++]=p4;
        polys[count++]=p5;
        polys[count++]=p6;
        polys[count++]=p7;
        polys[count++]=p8;
        polys[count++]=p9;
        polys[count++]=p10;
        polys[count++]=p11;
        polys[count++]=p12;
        polys[count++]=p13;
        polys[count++]=p14;
        polys[count++]=p15;
        polys[count++]=p16;
        polys[count++]=p17;
        polys[count++]=p18;
        polys[count++]=p19;
        polys[count++]=p20;
        polys[count++]=p21;
        polys[count++]=p22;
        polys[count++]=p23;
        polys[count++]=p24;
        polys[count++]=p25;
        polys[count++]=p26;
        polys[count++]=p27;
        polys[count++]=p28;
        polys[count++]=p29;
        polys[count++]=p30;                        
    }
}


