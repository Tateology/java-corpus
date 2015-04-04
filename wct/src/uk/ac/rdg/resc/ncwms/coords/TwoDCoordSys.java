/*
 * Copyright (c) 2009 The University of Reading
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package uk.ac.rdg.resc.ncwms.coords;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ucar.nc2.dt.GridCoordSystem;
import uk.ac.rdg.resc.ncwms.coords.CurvilinearGrid.Cell;

/**
 * A HorizontalCoordSys that is created from a "curvilinear" coordinate system,
 * {@literal i.e.} one in which the latitude/longitude coordinates of each
 * grid point are specified using two-dimensional coordinate axes, which explicitly
 * give the lat/lon of each point in the horizontal plane.  In these coordinate
 * systems, finding the nearest grid point to a given lat/lon point is complex.
 * Therefore we pre-calculate a {@link LookUpTable "look-up table"} of
 * the nearest i-j indices to a set of lat-lon points. Coordinate
 * conversions using such a look-up table are not precise but may suffice for
 * many applications.
 * @todo reduce the number of objects: fold in LookUpTable and BufferedImageLutGenerator
 * @author Jon Blower
 */
final public class TwoDCoordSys extends HorizontalCoordSys
{
    private static final Logger logger = LoggerFactory.getLogger(TwoDCoordSys.class);

    /**
     * In-memory cache of TwoDCoordSys objects to save expensive re-generation of same object
     * @todo The CurvilinearGrid objects can be very big.  Really we only need to key
     * on the arrays of lon and lat: all other quantities can be calculated from
     * these.  This means that we could make other large objects available for
     * garbage collection.
     */
    private static final Map<CurvilinearGrid, TwoDCoordSys> CACHE = new HashMap<CurvilinearGrid, TwoDCoordSys>();

    private final CurvilinearGrid curvGrid;
    private final LookUpTable lut;

    /**
     * The passed-in coordSys must have 2D horizontal coordinate axes.
     */
    public static TwoDCoordSys generate(GridCoordSystem coordSys)
    {
        CurvilinearGrid curvGrid = new CurvilinearGrid(coordSys);

        // We calculate the required resolution of the look-up tables.  We
        // want this to be around 3 times the resolution of the grid.
        double minLutResolution = Math.sqrt(curvGrid.getMeanCellArea()) / 3.0;
//        double minLutResolution = Math.sqrt(curvGrid.getMeanCellArea()) / 2.0;
        logger.debug("minLutResolution = {}", minLutResolution);

        synchronized(CACHE)
        {
            TwoDCoordSys lutCoordSys = CACHE.get(curvGrid);
            if (lutCoordSys == null)
            {
            	// steve ansari - only keep one grid lut cached to save memory
            	CACHE.clear();
            	
            	
                logger.debug("Need to generate new look-up table");
                // Create a look-up table for this coord sys
                LookUpTable lut = new LookUpTable(curvGrid, minLutResolution);
                logger.debug("Generated new look-up table");
                // Create the TwoDCoordSys
                lutCoordSys = new TwoDCoordSys(curvGrid, lut);
                // Now put this in the cache
                CACHE.put(curvGrid, lutCoordSys);
            }
            else
            {
                logger.debug("Look-up table found in cache");
            }
            return lutCoordSys;
        }
    }

    /** Private constructor to prevent direct instantiation */
    private TwoDCoordSys(CurvilinearGrid curvGrid, LookUpTable lut)
    {
        this.curvGrid = curvGrid;
        this.lut = lut;
    }

    /**
     * @return the nearest grid point to the given lat-lon point, or null if the
     * lat-lon point is not contained within this layer's domain. The grid point
     * is given as a two-dimensional integer array: [i,j].
     */
    @Override
    public int[] lonLatToGrid(LonLatPosition lonLatPoint)
    {
        int[] lutCoords =
            this.lut.getGridCoordinates(lonLatPoint.getLongitude(), lonLatPoint.getLatitude());
        // Return null if the latLonPoint does not match a valid grid point
        if (lutCoords == null) return null;
        // Check that this cell really contains this point, if not, check
        // the neighbours
        Cell cell = this.curvGrid.getCell(lutCoords[0], lutCoords[1]);
        if (cell.contains(lonLatPoint)) return lutCoords;
        for (Cell neighbour : cell.getEdgeNeighbours())
        {
            if (neighbour.contains(lonLatPoint))
            {
                return new int[]{ neighbour.getI(), neighbour.getJ() };
            }
        }
        for (Cell neighbour : cell.getCornerNeighbours())
        {
            if (neighbour.contains(lonLatPoint))
            {
                return new int[]{ neighbour.getI(), neighbour.getJ() };
            }
        }
        // If we get this far something probably went wrong with the LUT
        //logger.debug("Point is not contained by cell or its neighbours");
        return lutCoords;
    }

    /**
     * @return the latitude and longitude of the given grid point, or null if
     * the given grid coordinates [i,j] are outside the extent of the grid
     */
    @Override
    public LonLatPosition gridToLonLat(int i, int j)
    {
        if (i >= 0 && i < this.curvGrid.getNi() &&
            j >= 0 && j < this.curvGrid.getNj())
        {
            return this.curvGrid.getMidpoint(i, j);
        }
        else
        {
            return null;
        }
    }
}
