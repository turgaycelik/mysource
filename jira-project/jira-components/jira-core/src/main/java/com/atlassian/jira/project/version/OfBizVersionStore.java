/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project.version;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class OfBizVersionStore implements VersionStore
{
    private final OfBizDelegator delegator;

    public OfBizVersionStore(OfBizDelegator delegator)
    {
        this.delegator = delegator;
    }

    @Override
    public List<GenericValue> getAllVersions()
    {
        return new ArrayList<GenericValue>(delegator.findAll("Version", ImmutableList.of("sequence")));
    }

    @Override
    public List<GenericValue> getVersionsByName(final String name)
    {
        final List<GenericValue> versionGvs = getAllVersions();
        final List<GenericValue> filteredVersions = new ArrayList<GenericValue>();

        for (GenericValue versionGv : versionGvs)
        {
            if (name.equalsIgnoreCase(versionGv.getString("name")))
            {
                filteredVersions.add(versionGv);
            }
        }
        return filteredVersions;
    }

    @Override
    public List<GenericValue> getVersionsByProject(Long projectId)
    {
        return new ArrayList<GenericValue>(delegator.findByAnd("Version", EasyMap.build("project", projectId), ImmutableList.of("sequence")));
    }

    @Override
    public GenericValue createVersion(Map<String, Object> versionParams)
    {
        return delegator.createValue(OfBizDelegator.VERSION, versionParams);
    }

    public void storeVersion(Version version)
    {
        delegator.store(version.getGenericValue());
    }

    public void storeVersions(final Collection<Version> versions)
    {
        for (Version version : versions)
        {
            if (version != null)
            {
                storeVersion(version);
            }
        }
    }

    public GenericValue getVersion(Long id)
    {
        return delegator.findById("Version", id);
    }

    public void deleteVersion(GenericValue versionGV)
    {
        delegator.removeValue(versionGV);
    }

}
