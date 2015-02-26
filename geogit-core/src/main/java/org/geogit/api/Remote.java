/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.geogit.api;

import java.io.File;
import java.net.MalformedURLException;

import javax.annotation.Nullable;

import com.google.common.base.Optional;

/**
 * Internal representation of a GeoGit remote repository.
 * 
 */
public class Remote {
    private String name;

    private String fetchurl;

    private String pushurl;

    private String fetch;

    private String mappedBranch;

    private boolean mapped;

    /**
     * Constructs a new remote with the given parameters.
     * 
     * @param name the name of the remote
     * @param fetchurl the fetch URL of the remote
     * @param pushurl the push URL of the remote
     * @param fetch the fetch string of the remote
     * @param mapped whether or not this remote is mapped
     * @param mappedBranch the branch the remote is mapped to
     */
    public Remote(String name, String fetchurl, String pushurl, String fetch, boolean mapped,
            @Nullable String mappedBranch) {
        this.name = name;
        this.fetchurl = checkURL(fetchurl);
        this.pushurl = checkURL(pushurl);
        this.fetch = fetch;
        this.mapped = mapped;
        this.mappedBranch = Optional.fromNullable(mappedBranch).or("*");
    }

    private String checkURL(String url) {
        url = url.replace("\\", "/");
        if (url.startsWith("file:")) {
            return url;
        }
        File file = new File(url);
        if (file.exists()) {
            try {
                return file.toURI().toURL().toExternalForm();
            } catch (MalformedURLException e) {
                // shouldn't reach here, since the file exists and the path should then be correct
                return url;
            }
        }
        return url;
    }

    /**
     * @return the name of the remote
     */
    public String getName() {
        return name;
    }

    /**
     * @return the fetch URL of the remote
     */
    public String getFetchURL() {
        return fetchurl;
    }

    /**
     * @return the push URL of the remote
     */
    public String getPushURL() {
        return pushurl;
    }

    /**
     * @return the fetch string of the remote
     */
    public String getFetch() {
        return fetch;
    }

    /**
     * @return whether or not this remote is mapped
     */
    public boolean getMapped() {
        return mapped;
    }

    /**
     * @return the branch the remote is mapped to
     */
    public String getMappedBranch() {
        return mappedBranch;
    }

    /**
     * Determines if this Remote is the same as the given Remote.
     * 
     * @param o the remote to compare against
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Remote)) {
            return false;
        }
        Remote r = (Remote) o;
        return fetch.equals(r.fetch) && fetchurl.equals(r.fetchurl) && pushurl.equals(r.pushurl)
                && name.equals(r.name) && (mapped == r.mapped)
                && mappedBranch.equals(r.mappedBranch);
    }
}
