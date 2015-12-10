/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.managers;

import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class  MockCustomFieldManager implements CustomFieldManager
{

    private List<CustomField> customFields = new ArrayList<CustomField>();

    public List getCustomFieldObjects(SearchContext searchContext)
    {
        return null;
    }

    public Class getCustomFieldSearcherClass(String key)
    {
        return null;
    }

    @Override
    public void refreshConfigurationSchemes(Long customFieldId)
    {
    }

    public void removeProjectAssociations(GenericValue project)
    {

    }

    public void removeProjectAssociations(Project project)
    {

    }

    public void removeProjectCategoryAssociations(ProjectCategory projectCategory)
    {

    }

    public void removeCustomField(CustomField customField)
    {
        customFields.remove(customField);
    }

    @Override
    public void updateCustomField(CustomField updatedField)
    {
    }

    public void removeCustomFieldValues(GenericValue genericValue) throws GenericEntityException
    {

    }

    public CustomField getCustomFieldObject(Long id)
    {
        if (id == null)
        {
            return null;
        }
        String idString = id.toString();

        for (final CustomField customField : customFields)
        {
            if (idString.equals(customField.getId()) || ("customfield_" + idString).equals(customField.getId()))
            {
                return customField;
            }
        }

        return null;
    }

    @Override
    public boolean exists(final String id)
    {
        return getCustomFieldObject(id) != null;
    }

    public CustomField getCustomFieldObject(String id)
    {
        return getCustomFieldObject(CustomFieldUtils.getCustomFieldId(id));
    }

    public List getCustomFieldObjects()
    {
        return customFields;
    }

    public List getGlobalCustomFieldObjects()
    {
        return null;
    }

    public void refresh()
    {

    }

    public void clear()
    {
    }

    public List getCustomFieldObjects(Long projectId, String issueType)
    {
        return null;
    }

    public List getCustomFieldObjects(Long projectId, List issueTypes)
    {
        return null;
    }

    public List getCustomFieldObjects(GenericValue issue)
    {
        return null;
    }

    public List getCustomFieldObjects(Issue issue)
    {
        return null;
    }

    @Nonnull
    public List getCustomFieldTypes()
    {
        return null;
    }

    Map customFieldTypeMap = new HashMap();
    public CustomFieldType getCustomFieldType(String key)
    {
        return (CustomFieldType) customFieldTypeMap.get(key);
    }

    public void addCustomFieldType(String key, CustomFieldType customFieldType)
    {
        customFieldTypeMap.put(key, customFieldType);
    }

    @Nonnull
    public List getCustomFieldSearchers(CustomFieldType customFieldType)
    {
        return null;
    }

    public CustomFieldSearcher getCustomFieldSearcher(String key)
    {
        return null;
    }

    @Nullable
    @Override
    public CustomFieldSearcher getDefaultSearcher(@Nonnull final CustomFieldType<?, ?> type)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void addCustomField(CustomField customField)
    {
        customFields.add(customField);
    }

    public CustomField getCustomFieldObjectByName(String customFieldName)
    {
        return null;
    }

    public Collection getCustomFieldObjectsByName(final String customFieldName)
    {
        Collection objectsByName = new ArrayList();
        for (final CustomField customField : customFields)
        {
            if (customField.getName().equals(customFieldName))
            {
                objectsByName.add(customField);
            }
        }
        return objectsByName;
    }

    public CustomField createCustomField(String fieldName, String description, CustomFieldType fieldType, CustomFieldSearcher customFieldSearcher, List contexts, List issueTypes) throws GenericEntityException
    {
        return null;
    }

    public void removeCustomFieldPossiblyLeavingOrphanedData(final Long id) throws RemoveException
    {
    }

    public CustomField getCustomFieldInstance(GenericValue customFieldGv)
    {
        return null;
    }
}

