/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.project.version;

import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Interface for the VersionStore.
 * <p>
 * This is used by the VersionManager to store and retrieve Project Versions.
 * </p>
 * <p>
 * Most of the methods still work with <code>GenericValue</code>s, because this happens to be useful to the DefaultVersionManager.
 * (Specifically, it uses EntityUtil.filterByAnd() to filter the List of GenericValues returned by the getAllVersions() method).
 * On the other hand, storeVersions() takes a List of Version objects because this is what the DefaultVersionManager wants.
 * </p>
 */
public interface VersionStore
{

    GenericValue getVersion(Long id);

    /**
     * Returns a list of GenericValues representing all Versions in JIRA, ordered by sequence.
     * @return a list of GenericValues representing all Versions in JIRA, ordered by sequence.
     */
    List<GenericValue> getAllVersions();

    /**
     * Returns a list of GenericValues representing all Versions in JIRA with a given name, case insensitively.
     * @param name Version name
     * @return a list of GenericValues representing all Versions in JIRA with a given name, case insensitively.
     */
    List<GenericValue> getVersionsByName(String name);

    /**
     * Returns a list of GenericValues representing all Versions in a project.
     * @param projectId Project Id
     * @return a list of GenericValues representing all Versions in a project
     */
    List<GenericValue> getVersionsByProject(Long projectId);

    GenericValue createVersion(Map<String, Object> versionParams);

    /**
     * Stores an individual Version in the DB.
     * If you want to store a collection of Versions, consider using {@link #storeVersions}, it can be more efficient.
     *
     * @param version The Version to store.
     * @see #storeVersions
     */
    void storeVersion(final Version version);

    /**
     * Stores a collection of Version objects.
     * This method can have performance benefits over calling <code>storeVersion()<code> multiple times.
     * eg the {@link CachingVersionStore} will reload the cache from DB with every call to <code>storeVersion()<code>.
     *
     * @param versions Collection of Version objects.
     * @see #storeVersion(Version)
     */
    void storeVersions(final Collection<Version> versions);

    void deleteVersion(GenericValue versionGV);
}
