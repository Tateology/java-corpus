/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api.porcelain;

import org.geogit.api.AbstractGeoGitOp;
import org.geogit.api.Node;
import org.geogit.api.NodeRef;
import org.geogit.api.ObjectId;
import org.geogit.api.RevFeature;
import org.geogit.api.RevFeatureType;
import org.geogit.api.RevObject;
import org.geogit.api.RevObject.TYPE;
import org.geogit.api.RevTree;
import org.geogit.api.plumbing.FindTreeChild;
import org.geogit.api.plumbing.ResolveTreeish;
import org.geogit.api.plumbing.RevObjectParse;
import org.geogit.di.CanRunDuringConflict;
import org.geogit.repository.SpatialOps;
import org.geogit.repository.WorkingTree;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Returns the NodeRef corresponding to a given refspec, if available.
 */
// The annotation is here to allow this being run from the 'conflicts' command.
// Other than that, there is no reason for this to be restricted to non-conflicting scenarios
@CanRunDuringConflict
public class FeatureNodeRefFromRefspec extends AbstractGeoGitOp<Optional<NodeRef>> {

    private WorkingTree workTree;

    private String ref;

    @Inject
    public FeatureNodeRefFromRefspec(WorkingTree workTree) {
        this.workTree = workTree;
    }

    public FeatureNodeRefFromRefspec setRefspec(String ref) {
        this.ref = ref;
        return this;
    }

    private RevFeatureType getFeatureTypeFromRefSpec() {

        String featureTypeRef = NodeRef.parentPath(ref);
        String fullRef;
        if (featureTypeRef.contains(":")) {
            fullRef = featureTypeRef;
        } else {
            fullRef = "WORK_HEAD:" + featureTypeRef;
        }

        String treeRef = fullRef.split(":")[0];
        String path = fullRef.split(":")[1];
        ObjectId revTreeId = command(ResolveTreeish.class).setTreeish(treeRef).call().get();
        RevTree revTree = command(RevObjectParse.class).setObjectId(revTreeId).call(RevTree.class)
                .get();

        Optional<NodeRef> nodeRef = command(FindTreeChild.class).setParent(revTree)
                .setChildPath(path).setIndex(true).call();
        Preconditions.checkArgument(nodeRef.isPresent(), "Invalid reference: %s", ref);

        RevFeatureType revFeatureType = command(RevObjectParse.class)
                .setObjectId(nodeRef.get().getMetadataId()).call(RevFeatureType.class).get();
        return revFeatureType;

    }

    private Optional<RevFeature> getFeatureFromRefSpec() {

        Optional<RevObject> revObject = command(RevObjectParse.class).setRefSpec(ref).call(
                RevObject.class);

        if (!revObject.isPresent()) { // let's try to see if it is a feature in the working tree
            NodeRef.checkValidPath(ref);
            Optional<NodeRef> elementRef = command(FindTreeChild.class)
                    .setParent(workTree.getTree()).setChildPath(ref).setIndex(true).call();
            Preconditions.checkArgument(elementRef.isPresent(), "Invalid reference: %s", ref);
            ObjectId id = elementRef.get().objectId();
            revObject = command(RevObjectParse.class).setObjectId(id).call(RevObject.class);
        }

        if (revObject.isPresent()) {
            Preconditions.checkArgument(TYPE.FEATURE.equals(revObject.get().getType()),
                    "%s does not resolve to a feature", ref);
            return Optional.of(RevFeature.class.cast(revObject.get()));
        } else {
            return Optional.absent();
        }
    }

    @Override
    public Optional<NodeRef> call() {

        Optional<RevFeature> feature = getFeatureFromRefSpec();

        if (feature.isPresent()) {
            RevFeatureType featureType = getFeatureTypeFromRefSpec();
            RevFeature feat = feature.get();
            Envelope bounds = SpatialOps.boundsOf(feat);
            Node node = Node.create(NodeRef.nodeFromPath(ref), feat.getId(), featureType.getId(),
                    TYPE.FEATURE, bounds);
            return Optional.of(new NodeRef(node, NodeRef.parentPath(ref), featureType.getId()));

        } else {
            return Optional.absent();
            /*
             * new NodeRef(Node.create("", ObjectId.NULL, ObjectId.NULL, TYPE.FEATURE), "",
             * ObjectId.NULL);
             */
        }

    }
}
