/*
 * Copyright (c) 2002-2011
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.field;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.project.Project;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * <b>NOTE:</b> This is referred to as Field Configuration in the UI.
 */
@PublicApi
public interface FieldLayout
{
    /**
     * The id of the field configuration.
     *
     * @return the id
     */
    public Long getId();

    /**
     * The name of the field configuration
     *
     * @return the name
     */
    public String getName();

    /**
     * The description of the field configuration.
     *
     * @return the description
     */
    public String getDescription();

    /**
     * A list of field layout item of this field configuration.
     *
     * @return a list of field layout items of this field configuration.
     */
    public List<FieldLayoutItem> getFieldLayoutItems();

    /**
     * The generic value which represents this field configuration.
     *
     * @return the generic value
     */
    public GenericValue getGenericValue();

    /**
     * Returns the field layout item for a given orderable field, if this orderable field is part of this field configuration.
     *
     * @param orderableField the orderable field to find in this field configuration.
     *
     * @return the field layout item if this field is part of this field configuration, otherwise null.
     */
    public FieldLayoutItem getFieldLayoutItem(OrderableField orderableField);

    /**
     * Returns the field layout item for a given field if, if this field is part of this field configuration.
     *
     * @param fieldId the id of the field.
     *
     * @return the field layout item if this field is part of this field configuration, otherwise null.
     */
    public FieldLayoutItem getFieldLayoutItem(String fieldId);

    /**
     * Returns all visible field layout items (system fields and custom fields) for a given project and issue type(s).
     *
     * @param remoteUser the user TODO: User is not used! We should remove it!
     * @param project the project
     * @param issueTypes a list of issue types to use when checking for VISIBLE CUSTOM FIELDS.
     * @return all visible field layout items for the given project and it's issue type(s).
     *
     * @deprecated Use {@link #getVisibleLayoutItems(com.atlassian.jira.project.Project, java.util.List)} instead. Since v6.2.
     */
    public List<FieldLayoutItem> getVisibleLayoutItems(User remoteUser, Project project, List<String> issueTypes);

    /**
     * Returns all visible field layout items (system fields and custom fields) for a given project and issue type(s).
     *
     * @param project the project
     * @param issueTypes a list of issue types to use when checking for VISIBLE CUSTOM FIELDS.
     * @return all visible field layout items for the given project and it's issue type(s).
     */
    public List<FieldLayoutItem> getVisibleLayoutItems(Project project, List<String> issueTypes);

    /**
     * Returns the list of Custom Fields in this Field Layout that are both visible and applicable to the given context (of project and Issue types).
     *
     * @param project The project context
     * @param issueTypes The Issue Types for context
     * @return the list of visible Custom Fields applicable to the given context (of project and Issue types).
     */
    public List<FieldLayoutItem> getVisibleCustomFieldLayoutItems(Project project, List<String> issueTypes);

    /**
     * Returns all hidden fields (system fields and custom fields) for a given project and issue type(s).
     *
     * @param project the project
     * @param issueTypeIds   issueTypes a list of issue types to use when checking for HIDDEN CUSTOM FIELDS.
     *
     * @return all hidden fields (system fields and custom fields).
     */
    public List<Field> getHiddenFields(Project project, List<String> issueTypeIds);

    /** @deprecated Use {@link #getHiddenFields(com.atlassian.jira.project.Project, java.util.List)}. Since v4.3 */
    public List<Field> getHiddenFields(User remoteUser, GenericValue project, List<String> issueTypeIds);

    /** @deprecated Use {@link #getHiddenFields(com.atlassian.jira.project.Project, java.util.List)}. Since v4.3 */
    public List<Field> getHiddenFields(User remoteUser, Project project, List<String> issueTypeIds);

    /**
     *  Returns the list of required fields for an issue type in a project.
     *
     * @param project the project
     * @param issueTypes the issue type.
     *
     * @return the list of required fields. System fields and custom fields.
     */
    public List<FieldLayoutItem> getRequiredFieldLayoutItems(Project project, List<String> issueTypes);

    /**
     * Checks if a field is hidden in this field configuration.
     *
     * @param fieldId the field id.
     *
     * @return true if the field is hidden, otherwise false.
     */
    public boolean isFieldHidden(String fieldId);

    /**
     * Returns the render type for a given field in this field configuration.
     *
     * @param fieldId the id of the field.
     *
     * @return the type of the renderer e.g. DefaultTextRenderer.RENDERER_TYPE ("jira-text-renderer")
     */
    public String getRendererTypeForField(String fieldId);

    /**
     * Returns true if this is the default FieldLayout.
     * This means that the "type" field holds "value".
     *
     * @return true if this is the default FieldLayout.
     */
    boolean isDefault();
}
