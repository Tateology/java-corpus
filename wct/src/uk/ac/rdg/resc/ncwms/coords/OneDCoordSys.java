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

import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.ProjectionImpl;
import ucar.unidata.geoloc.ProjectionPoint;

/**
 * A {@link HorizontalCoordSys} that consists of two orthogonal 1D axes.
 * Instances of this class can only be created through
 * {@link HorizontalCoordSys#fromCoordSys(ucar.nc2.dt.GridCoordSystem)}.
 */
public class OneDCoordSys extends HorizontalCoordSys
{
    private final OneDCoordAxis xAxis;
    private final OneDCoordAxis yAxis;
    private final ProjectionImpl proj;

    OneDCoordSys(OneDCoordAxis xAxis, OneDCoordAxis yAxis, ProjectionImpl proj)
    {
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.proj = proj;
    }

    /**
     * @return the nearest grid point to the given lat-lon point, or null if the
     * lat-lon point is not contained within this layer's domain. The grid point
     * is given as a two-dimensional integer array: [i,j].
     */
    @Override
    public final int[] lonLatToGrid(LonLatPosition lonLatPoint)
    {
        if (this.proj == null)
        {
            return new int[] {
                this.getXIndex(lonLatPoint.getLongitude()),
                this.getYIndex(lonLatPoint.getLatitude())
            };
        }
        // ProjectionImpls are not thread-safe.  Thanks to Marcos
        // Hermida of Meteogalicia for pointing this out!
        ProjectionPoint point;
        synchronized(this.proj) {
            point = this.proj.latLonToProj(lonLatPoint.getLatitude(), lonLatPoint.getLongitude());
        }
        int iIndex = this.xAxis.getIndex(point.getX());
        int jIndex = this.yAxis.getIndex(point.getY());
        if (iIndex < 0 || jIndex < 0) return null;
        return new int[] { iIndex, jIndex };
    }

    @Override
    public final LonLatPosition gridToLonLat(int i, int j) {
        // Check that the indices are within range
        if (i < 0 || i >= this.xAxis.getSize() ||
            j < 0 || j >= this.yAxis.getSize())
        {
            return null;
        }
        if (this.proj == null)
        {
            return new LonLatPositionImpl(
                this.xAxis.getCoordValue(i),
                this.yAxis.getCoordValue(j)
            );
        }
        LatLonPoint llp = this.proj.projToLatLon(
            this.xAxis.getCoordValue(i),
            this.yAxis.getCoordValue(j)
        );
        return new LonLatPositionImpl(llp.getLongitude(), llp.getLatitude());
    }

    /**
     * @return the nearest point along the x axis to the given x coordinate, or
     * -1 if the value is out of range for this axis.
     */
    public final int getXIndex(double xCoord)
    {
        return this.xAxis.getIndex(xCoord);
    }

    /**
     * @return the nearest point along the y axis to the given y coordinate, or
     * -1 if the value is out of range for this axis.
     */
    public final int getYIndex(double yCoord)
    {
        return this.yAxis.getIndex(yCoord);
    }
}
