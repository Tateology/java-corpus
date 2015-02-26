/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api;

import java.util.ArrayList;

import org.geogit.api.plumbing.HashObject;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * A binary representation of the state of a Feature Type.
 */
public class RevFeatureType extends AbstractRevObject {

    private final FeatureType featureType;

    private ImmutableList<PropertyDescriptor> sortedDescriptors;

    public static RevFeatureType build(FeatureType type) {
        RevFeatureType unnamed = new RevFeatureType(type);
        ObjectId id = new HashObject().setObject(unnamed).call();
        return new RevFeatureType(id, type);
    }

    /**
     * Constructs a new {@code RevFeatureType} from the given {@link FeatureType}.
     * 
     * @param featureType the feature type to use
     */
    private RevFeatureType(FeatureType featureType) {
        this(ObjectId.NULL, featureType);
    }

    /**
     * Constructs a new {@code RevFeatureType} from the given {@link ObjectId} and
     * {@link FeatureType}.
     * 
     * @param id the object id to use for this feature type
     * @param featureType the feature type to use
     */
    public RevFeatureType(ObjectId id, FeatureType featureType) {
        super(id);
        this.featureType = featureType;
        ArrayList<PropertyDescriptor> descriptors = Lists.newArrayList(this.featureType
                .getDescriptors());
        sortedDescriptors = ImmutableList.copyOf(descriptors);

    }

    @Override
    public TYPE getType() {
        return TYPE.FEATURETYPE;
    }

    public FeatureType type() {
        return featureType;
    }

    /**
     * @return the sorted {@link PropertyDescriptor}s of the feature type
     */
    public ImmutableList<PropertyDescriptor> sortedDescriptors() {
        return sortedDescriptors;
    }

    /**
     * @return the name of the feature type
     */
    public Name getName() {
        Name name = type().getName();
        return name;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FeatureType[");
        builder.append(getId().toString());
        builder.append("; ");
        boolean first = true;
        for (PropertyDescriptor desc : sortedDescriptors()) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append(desc.getName().getLocalPart());
            builder.append(": ");
            builder.append(desc.getType().getBinding().getSimpleName());
        }
        builder.append(']');
        return builder.toString();
    }
}
