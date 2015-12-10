/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mock;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.IssueTypeImpl;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.util.ErrorCollection;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockConstantsManager implements ConstantsManager
{
    Map<String, GenericValue> issueTypes;
    Map<String, GenericValue> priorities;
    Map<String, GenericValue> resolutions;
    Map<String, GenericValue> statuses;

    public MockConstantsManager()
    {
        issueTypes = new HashMap<String, GenericValue>();
        priorities = new HashMap<String, GenericValue>();
        resolutions = new HashMap<String, GenericValue>();
        statuses = new HashMap<String, GenericValue>();
    }

    public GenericValue getStatus(String id)
    {
        return statuses.get(id);
    }

    public Status getStatusObject(String id)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public Collection<GenericValue> getStatuses()
    {
        return statuses.values();
    }

    public Collection<Status> getStatusObjects()
    {
        return null;
    }

    public void refreshStatuses()
    {
        throw new UnsupportedOperationException();
    }

    public GenericValue getConstant(String constantType, String id)
    {
        throw new UnsupportedOperationException();
    }

    public List convertToConstantObjects(String constantType, Collection ids)
    {
        return null;
    }

    public boolean constantExists(String constantType, String name)
    {
        return false;
    }

    public GenericValue createIssueType(String name, Long sequence, String style, String description, String iconurl) throws CreateException
    {
        return null;
    }

    public IssueType insertIssueType(String name, Long sequence, String style, String description, String iconurl)
    {
        Long id = findNextIdFor(issueTypes);
        final MockGenericValue issueType = new MockGenericValue("IssueType",
                FieldMap.build("id", id)
                        .add("name", name)
                        .add("sequence", sequence)
                        .add("style", style)
                        .add("description", description)
                        .add("iconurl", iconurl)
        );
        addIssueType(issueType);
        return new IssueTypeImpl(issueType, null, null, null, null);
    }

    @Override
    public IssueType insertIssueType(final String name, final Long sequence, final String style, final String description, final Long avatarId)
            throws CreateException
    {
        Long id = findNextIdFor(issueTypes);
        final MockGenericValue issueType = new MockGenericValue("IssueType",
                FieldMap.build("id", id)
                        .add("name", name)
                        .add("sequence", sequence)
                        .add("style", style)
                        .add("description", description)
                        .add(IssueTypeImpl.AVATAR_FIELD, avatarId)
        );
        addIssueType(issueType);
        return new IssueTypeImpl(issueType, null, null, null, null);
    }

    private Long findNextIdFor(Map<String, GenericValue> issueTypes)
    {
        long maxId = 0L;
        for (GenericValue genericValue : issueTypes.values())
        {
            Long id = genericValue.getLong("id");
            if (id > maxId)
                maxId = id;
        }
        return maxId + 1;
    }

    public void validateCreateIssueType(String name, String style, String description, String iconurl, ErrorCollection errors, String nameFieldName)
    {

    }

    @Override
    public void validateCreateIssueTypeWithAvatar(final String name, final String style, final String description, final String avatarId, final ErrorCollection errors, final String nameFieldName)
    {

    }

    public void updateIssueType(String id, String name, Long sequence, String style, String description, String iconurl)
    {
        throw new UnsupportedOperationException();
    }

    public void updateIssueType(String id, String name, Long sequence, String style, String description, Long avatarId)
    {
        throw new UnsupportedOperationException();
    }

    public void removeIssueType(String id) throws RemoveException
    {

    }

    public IssueConstant getConstantByNameIgnoreCase(final String constantType, final String name)
    {
        return null;
    }

    public GenericValue getConstantByName(String constantType, String name)
    {
        return null;
    }

    public IssueConstant getIssueConstantByName(String constantType, String name)
    {
        return null;
    }

    public Collection<GenericValue> getSubTaskIssueTypes()
    {
        return null;
    }

    public List<GenericValue> getEditableSubTaskIssueTypes()
    {
        return null;
    }

    public List<GenericValue> getAllIssueTypes()
    {
        return null;
    }

    public List<String> getAllIssueTypeIds()
    {
        return null;
    }

    public IssueConstant getIssueConstant(GenericValue issueConstantGV)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public void storeIssueTypes(List issueTypes)
    {
        throw new UnsupportedOperationException();
    }

    public void refresh()
    {
        throw new UnsupportedOperationException();
    }

    public void invalidateAll()
    {
        throw new UnsupportedOperationException();
    }

    public void invalidate(final IssueConstant issueConstant)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<String> expandIssueTypeIds(Collection<String> issueTypeIds)
    {
        throw new UnsupportedOperationException();
    }

    public Collection<GenericValue> getPriorities()
    {
        List<GenericValue> priorityList = new ArrayList<GenericValue>(priorities.values());
        return EntityUtil.orderBy(priorityList, EasyList.build("sequence"));
    }

    public Collection<Priority> getPriorityObjects()
    {
        return null;
    }

    public String getPriorityName(String id)
    {
        return null;
    }

    public Priority getPriorityObject(String id)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public GenericValue getDefaultPriority() {
        return null;
    }

    public Priority getDefaultPriorityObject() {
        return null;
    }

    public void refreshPriorities()
    {
        throw new UnsupportedOperationException();
    }

    public Collection<GenericValue> getResolutions()
    {
        return resolutions.values();
    }

    public Collection<Resolution> getResolutionObjects()
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public GenericValue getResolution(String id)
    {
        return resolutions.get(id);
    }

    public Resolution getResolutionObject(String id)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public void refreshResolutions()
    {
        throw new UnsupportedOperationException();
    }

    public Collection<GenericValue> getIssueTypes()
    {
        return issueTypes.values();
    }

    public GenericValue getIssueType(String id)
    {
        return issueTypes.get(id);
    }

    public IssueType getIssueTypeObject(String id)
    {
        final GenericValue value = getIssueType(id);
        return value != null ?  new IssueTypeImpl(value, null, null, null, null) : null;
    }

    public void refreshIssueTypes()
    {
        throw new UnsupportedOperationException();
    }

    public IssueConstant getConstantObject(String constantType, String id)
    {
        throw new UnsupportedOperationException();
    }

    public Collection getConstantObjects(String constantType)
    {
        return null;
    }

    public void addIssueType(GenericValue type)
    {
        issueTypes.put(type.getString("id"), type);
    }

    public void addResolution(GenericValue resolution)
    {
        resolutions.put(resolution.getString("id"), resolution);
    }

    public void addPriority(GenericValue priority)
    {
        priorities.put(priority.getString("id"), priority);
    }

    public void addStatus(GenericValue status)
    {
        statuses.put(status.getString("id"), status);
    }

    public Collection<IssueType> getAllIssueTypeObjects()
    {
        return null;
    }

    public Collection<IssueType> getRegularIssueTypeObjects()
    {
        return null;
    }

    public Collection<IssueType> getSubTaskIssueTypeObjects()
    {
        return null;
    }

    public Status getStatusByName(String name)
    {
        return null;
    }

    public Status getStatusByNameIgnoreCase(String name)
    {
        return null;
    }

    public Status getStatusByTranslatedName(String name)
    {
        return null;
    }
}
