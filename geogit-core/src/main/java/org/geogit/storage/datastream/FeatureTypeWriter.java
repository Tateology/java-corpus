/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.storage.datastream;

import static org.geogit.storage.datastream.FormatCommon.writeHeader;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.geogit.api.RevFeatureType;
import org.geogit.storage.FieldType;
import org.geogit.storage.ObjectWriter;
import org.geotools.referencing.CRS;
import org.geotools.referencing.CRS.AxisOrder;
import org.geotools.referencing.wkt.Formattable;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class FeatureTypeWriter implements ObjectWriter<RevFeatureType> {
    @Override
    public void write(RevFeatureType object, OutputStream out) throws IOException {
        DataOutput data = new DataOutputStream(out);
        writeHeader(data, "featuretype");
        writeName(object.getName(), data);
        data.writeInt(object.sortedDescriptors().size());
        for (PropertyDescriptor desc : object.type().getDescriptors()) {
            writeProperty(desc, data);
        }
    }

    private void writeName(Name name, DataOutput data) throws IOException {
        final String ns = name.getNamespaceURI();
        final String lp = name.getLocalPart();
        data.writeUTF(ns == null ? "" : ns);
        data.writeUTF(lp == null ? "" : lp);
    }

    private void writePropertyType(PropertyType type, DataOutput data) throws IOException {
        writeName(type.getName(), data);
        data.writeByte(FieldType.forBinding(type.getBinding()).getTag());
        if (type instanceof GeometryType) {
            GeometryType gType = (GeometryType) type;
            CoordinateReferenceSystem crs = gType.getCoordinateReferenceSystem();
            String srsName;
            if (crs == null) {
                srsName = "urn:ogc:def:crs:EPSG::0";
            } else {
                final boolean longitudeFirst = CRS.getAxisOrder(crs, false) == AxisOrder.EAST_NORTH;
                final boolean codeOnly = true;
                String crsCode = CRS.toSRS(crs, codeOnly);
                if (crsCode != null) {
                    srsName = (longitudeFirst ? "EPSG:" : "urn:ogc:def:crs:EPSG::") + crsCode;
                    // check that what we are writing is actually a valid EPSG code and we will be
                    // able to decode it later. If not, we will use WKT instead
                    try {
                        CRS.decode(srsName, longitudeFirst);
                    } catch (NoSuchAuthorityCodeException e) {
                        srsName = null;
                    } catch (FactoryException e) {
                        srsName = null;
                    }
                } else {
                    srsName = null;
                }
            }
            if (srsName != null) {
                data.writeBoolean(true);
                data.writeUTF(srsName);
            } else {
                final String wkt;
                if (crs instanceof Formattable) {
                    wkt = ((Formattable) crs).toWKT(Formattable.SINGLE_LINE);
                } else {
                    wkt = crs.toWKT();
                }
                data.writeBoolean(false);
                data.writeUTF(wkt);
            }
        }
    }

    private void writeProperty(PropertyDescriptor attr, DataOutput data) throws IOException {
        writeName(attr.getName(), data);
        data.writeBoolean(attr.isNillable());
        data.writeInt(attr.getMinOccurs());
        data.writeInt(attr.getMaxOccurs());
        writePropertyType(attr.getType(), data);
    }
}
