/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api.plumbing.diff;

/**
 * A class with the counts of changed elements between two commits, divided in trees and features
 * 
 */
public class DiffObjectCount {

    private long featuresCount;

    private long treesCount;

    public DiffObjectCount() {
        this(0, 0);
    }

    public DiffObjectCount(long treesCount, long featuresCount) {
        this.treesCount = treesCount;
        this.featuresCount = featuresCount;
    }

    /**
     * Returns the number of features modified
     * 
     * @return
     */
    public long getFeaturesCount() {
        return featuresCount;
    }

    /**
     * Returns the number of trees modified
     * 
     * @return
     */
    public long getTreesCount() {
        return treesCount;
    }

    /**
     * Adds the counts from another DiffObject count object to this one
     * 
     * @param toAdd
     */
    void add(DiffObjectCount toAdd) {
        treesCount += toAdd.getTreesCount();
        featuresCount += toAdd.getFeaturesCount();
    }

    /**
     * Increases the number of modified features in this object by a given number
     * 
     * @param count the number to add to the current count
     */
    void addFeatures(long count) {
        featuresCount += count;
    }

    /**
     * Increases the number of modified features in this object by a given number
     * 
     * @param count the number to add to the current count
     */
    void addTrees(long count) {
        treesCount += count;
    }

    /**
     * Returns the total count of modified elements
     */
    public long getCount() {
        return treesCount + featuresCount;
    }

}
