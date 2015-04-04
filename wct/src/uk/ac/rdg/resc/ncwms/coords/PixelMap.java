/*
 * Copyright (c) 2007 The University of Reading
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *<p>Maps real-world points to i and j indices of corresponding
 * points within the source data.  This is a very important class in ncWMS.  A
 * PixelMap is constructed using the following general algorithm:</p>
 *
 * <pre>
 * For each point in the given {@link PointList}:
 *    1. Find the x-y coordinates of this point in the CRS of the PointList
 *    2. Transform these x-y coordinates into latitude and longitude
 *    3. Use the given {@link HorizontalCoordSys} to transform lat-lon into the
 *       index values (i and j) of the nearest cell in the source grid
 *    4. Add the mapping (point -> i,j) to the pixel map
 * </pre>
 *
 * <p>(A more efficient algorithm is used for the special case in which both the
 * requested CRS and the CRS of the data are lat-lon.)</p>
 *
 * <p>The resulting PixelMap is then used by {@link DataReadingStrategy}s to work out what
 * data to read from the source data files.  A variety of strategies are possible
 * for reading these data points, each of which may be optimal in a certain
 * situation.</p>
 *
 * @author Jon Blower
 * @todo Perhaps we can think of a more appropriate name for this class?
 * @todo equals() and hashCode(), particularly if we're going to cache instances
 * of this class
 * @see DataReadingStrategy
 */
public final class PixelMap
{
    private static final Logger logger = LoggerFactory.getLogger(PixelMap.class);

    // These define the bounding box (in terms of axis indices) of the data
    // to extract from the source files
    private int minIIndex = Integer.MAX_VALUE;
    private int minJIndex = Integer.MAX_VALUE;
    private int maxIIndex = -1;
    private int maxJIndex = -1;

    // Maps j indices to row information
    private final Map<Integer, Row> pixelMap = new HashMap<Integer, Row>();

    // Number of unique i-j pairs
    private int numUniqueIJPairs = 0;

    public PixelMap(HorizontalCoordSys horizCoordSys, PointList pointList) throws TransformException
    {
        long start = System.currentTimeMillis();
        if (pointList instanceof HorizontalGrid)
        {
            this.initFromGrid(horizCoordSys, (HorizontalGrid)pointList);
        }
        else
        {
            this.initFromPointList(horizCoordSys, pointList);
        }
        logger.debug("Built pixel map in {} ms", System.currentTimeMillis() - start);
    }

    private void initFromPointList(HorizontalCoordSys horizCoordSys, PointList pointList) throws TransformException
    {
        logger.debug("Using generic method based on iterating over the PointList");
        int pixelIndex = 0;
        for (HorizontalPosition point : pointList.asList())
        {
            // Translate this point in the target grid to lat-lon
            LonLatPosition lonLat = new LonLatPositionImpl(point.getX(), point.getY());
            // Now find the nearest index in the grid: gridCoords will be
            // null if latLon is outside the grid's domain
            int[] gridCoords = horizCoordSys.lonLatToGrid(lonLat);
            if (gridCoords != null)
            {
                this.put(gridCoords[0], gridCoords[1], pixelIndex);
            }
            pixelIndex++;
        }
    }

    /**
     * Generates a PixelMap for the given Layer.  Data read from the Layer will
     * be projected onto the given HorizontalGrid
     *
     * @throws Exception if the necessary transformations could not be performed
     */
    private void initFromGrid(HorizontalCoordSys horizCoordSys, HorizontalGrid grid) throws TransformException
    {
        // Cycle through each pixel in the picture and work out which
        // i and j index in the source data it corresponds to

        // We can gain efficiency if the target grid is a lat-lon grid and
        // the data exist on a lat-long grid by minimizing the number of
        // calls to axis.getIndex().
        if (grid.isLatLon() && horizCoordSys instanceof LatLonCoordSys)
        {
            logger.debug("Using optimized method for lat-lon coordinates with 1D axes");
            LatLonCoordSys latLonGrid = (LatLonCoordSys)horizCoordSys;
            int pixelIndex = 0;
            // Calculate the indices along the x axis.
            int[] xIndices = new int[grid.getXAxisValues().length];
            for (int i = 0; i < grid.getXAxisValues().length; i++)
            {
                xIndices[i] = latLonGrid.getLonIndex(grid.getXAxisValues()[i]);
            }
            for (double lat : grid.getYAxisValues())
            {
                if (lat >= -90.0 && lat <= 90.0)
                {
                    int yIndex = latLonGrid.getLatIndex(lat);
                    for (int xIndex : xIndices)
                    {
                        this.put(xIndex, yIndex, pixelIndex);
                        pixelIndex++;
                    }
                }
                else
                {
                    // We still need to increment the pixel index
                    pixelIndex += xIndices.length;
                }
            }
        }
        else
        {
            // We can't do better than the generic initialization method
            // based upon iterating through each point in the grid.
            this.initFromPointList(horizCoordSys, (PointList)grid);
        }
    }

    /**
     * Adds a new pixel index to this map.  Does nothing if either x or y is
     * negative.
     * @param i The i index of the point in the source data
     * @param j The j index of the point in the source data
     * @param pixel The index of the corresponding point in the picture
     */
    private void put(int i, int j, int pixel)
    {
        // If either of the indices are negative there is no data for this
        // pixel index
        if (i < 0 || j < 0) return;

        // Modify the bounding box if necessary
        if (i < this.minIIndex) this.minIIndex = i;
        if (i > this.maxIIndex) this.maxIIndex = i;
        if (j < this.minJIndex) this.minJIndex = j;
        if (j > this.maxJIndex) this.maxJIndex = j;

        // Get the information for this row (i.e. this y index),
        // creating a new row if necessary
        Row row = this.pixelMap.get(j);
        if (row == null)
        {
            row = new Row();
            this.pixelMap.put(j, row);
        }

        // Add the pixel to this row
        row.put(i, pixel);
    }

    /**
     * Returns true if this PixelMap does not contain any data: this will happen
     * if there is no intersection between the requested data and the data on disk.
     * @return true if this PixelMap does not contain any data: this will happen
     * if there is no intersection between the requested data and the data on disk
     */
    public boolean isEmpty()
    {
        return this.pixelMap.size() == 0;
    }

    /**
     * Gets the j indices of all rows in this pixel map
     * @return the Set of all j indices in this pixel map
     */
    public Set<Integer> getJIndices()
    {
        return this.pixelMap.keySet();
    }

    /**
     * Gets the i indices of all the data points in the given row that
     * are needed to make the final image.
     * @return the Set of all I indices in the given row
     * @throws IllegalArgumentException if there is no row with the given y index
     */
    public Set<Integer> getIIndices(int j)
    {
        return this.getRow(j).getIIndices().keySet();
    }

    /**
     * Gets the set of all pixel indices, representing individual elements in the
     * final data array, that correspond with the given grid point in the source data.  A single
     * value from the source data might map to several elements in the final data array,
     * especially if we are "zoomed in".
     * @return a Set of all data array indices that correspond with the given i and
     * j index
     * @throws IllegalArgumentException if there is no row with the given j index
     * or if the given i index is not found in the row
     */
    public Set<Integer> getPixelIndices(int i, int j)
    {
        Map<Integer, Set<Integer>> row = this.getRow(j).getIIndices();
        if (!row.containsKey(i))
        {
            throw new IllegalArgumentException("The i index " + i +
                " was not found in the row with j index " + j);
        }
        return row.get(i);
    }

    /**
     * Gets the minimum i index in the row with the given j index
     * @return the minimum i index in the row with the given j index
     * @throws IllegalArgumentException if there is no row with the given y index
     */
    public int getMinIIndexInRow(int j)
    {
        return this.getRow(j).getMinIIndex();
    }

    /**
     * Gets the maximum i index in the row with the given j index
     * @return the maximum i index in the row with the given j index
     * @throws IllegalArgumentException if there is no row with the given y index
     */
    public int getMaxIIndexInRow(int j)
    {
        return this.getRow(j).getMaxIIndex();
    }

    /**
     * @return the row with the given j index
     * @throws IllegalArgumentException if there is no row with the given y index
     */
    private Row getRow(int j)
    {
        if (!this.pixelMap.containsKey(j))
        {
            throw new IllegalArgumentException("There is no row with j index " + j);
        }
        return this.pixelMap.get(j);
    }

    /**
     * Gets the minimum i index in the whole pixel map
     * @return the minimum i index in the whole pixel map
     */
    public int getMinIIndex()
    {
        return minIIndex;
    }

    /**
     * Gets the minimum j index in the whole pixel map
     * @return the minimum j index in the whole pixel map
     */
    public int getMinJIndex()
    {
        return minJIndex;
    }

    /**
     * Gets the maximum i index in the whole pixel map
     * @return the maximum i index in the whole pixel map
     */
    public int getMaxIIndex()
    {
        return maxIIndex;
    }

    /**
     * Gets the maximum j index in the whole pixel map
     * @return the maximum j index in the whole pixel map
     */
    public int getMaxJIndex()
    {
        return maxJIndex;
    }

    /**
     * Contains information about a particular row in the data
     */
    private class Row
    {
        // Maps i Indices to a set of pixel indices
        //             i        pixels
        private Map<Integer, Set<Integer>> iIndices = new HashMap<Integer, Set<Integer>>();
        // Min and max x Indices in this row
        private int minIIndex = Integer.MAX_VALUE;
        private int maxIIndex = -1;

        /**
         * Adds a mapping of an i index to a pixel index
         */
        public void put(int i, int pixel)
        {
            if (i < this.minIIndex) this.minIIndex = i;
            if (i > this.maxIIndex) this.maxIIndex = i;

            Set<Integer> pixelIndices = this.iIndices.get(i);
            if (pixelIndices == null)
            {
                pixelIndices = new HashSet<Integer>();
                this.iIndices.put(i, pixelIndices);
                // We have a new unique x-y pair
                PixelMap.this.numUniqueIJPairs++;
            }
            // Add the pixel index to the set
            pixelIndices.add(pixel);
        }

        public Map<Integer, Set<Integer>> getIIndices()
        {
            return this.iIndices;
        }

        public int getMinIIndex()
        {
            return this.minIIndex;
        }

        public int getMaxIIndex()
        {
            return this.maxIIndex;
        }
    }

    /**
     * Gets the number of unique i-j pairs in this pixel map. When combined
     * with the size of the resulting image we can quantify the under- or
     * oversampling.  This is the number of data points that will be extracted
     * by the {@link PixelByPixelDataReader}.
     * @return the number of unique i-j pairs in this pixel map.
     */
    public int getNumUniqueIJPairs()
    {
        return numUniqueIJPairs;
    }

    /**
     * Gets the sum of the lengths of each row of data points,
     * {@literal i.e.} sum(imax - imin + 1).  This is the number of data points that will
     * be extracted by the data reader.
     * @return the sum of the lengths of each row of data points
     */
    public int getSumRowLengths()
    {
        int sumRowLengths = 0;
        for (Row row : this.pixelMap.values())
        {
            sumRowLengths += (row.getMaxIIndex() - row.getMinIIndex() + 1);
        }
        return sumRowLengths;
    }

    /**
     * Gets the size of the i-j bounding box that encompasses all data.  This is
     * the number of data points that will be extracted by the
     * {@link BoundingBoxDataReader}.
     * @return the size of the i-j bounding box that encompasses all data.
     */
    public int getBoundingBoxSize()
    {
        return (this.maxIIndex - this.minIIndex + 1) *
               (this.maxJIndex - this.minJIndex + 1);
    }

}
