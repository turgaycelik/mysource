/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.renderer.HackyFieldRendererRegistry;
import com.atlassian.jira.issue.fields.screen.AbstractGVBean;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.Function;

import com.google.common.base.Objects;

import org.ofbiz.core.entity.GenericValue;

public class FieldLayoutImpl extends AbstractGVBean implements FieldLayout
{
    private Long id;
    private String name;
    private String description;
    private List<FieldLayoutItem> fieldLayoutItems;
    protected Map<String, FieldLayoutItem> fieldLayoutItemByFieldId;
    private String type;

    public FieldLayoutImpl(GenericValue genericValue, List<FieldLayoutItem> fieldLayoutItems)
    {
        setFieldLayoutItems(fieldLayoutItems);
        setGenericValue(genericValue);
        init();
    }

    protected void init()
    {
        if (getGenericValue() != null)
        {
            this.id = getGenericValue().getLong("id");
            this.name = getGenericValue().getString("name");
            this.description = getGenericValue().getString("description");
            this.type = getGenericValue().getString("type");
        }
        // temporary revert until we fix JDEV-31414
//        else
//        {
//            // JRA-40658 JIRA will start life with no explicit rows in the FieldLayout table
//            // this path sets up the implicit values for the Default Field Configuration
//            // See also EditableDefaultFieldLayoutImpl.init()
//            this.name = EditableDefaultFieldLayout.NAME;
//            this.description = EditableDefaultFieldLayout.DESCRIPTION;
//            this.type = FieldLayoutManager.TYPE_DEFAULT;
//        }

        setModified(false);
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getType()
    {
        return type;
    }

    public boolean isDefault()
    {
        return FieldLayoutManager.TYPE_DEFAULT.equals(getType());
    }

    protected void setId(Long id)
    {
        this.id = id;
    }

    protected void setInternalName(String name)
    {
        this.name = name;
        updateGV("name", name);
    }

    protected void setInternalDescription(String description)
    {
        this.description = description;
        updateGV("description", description);
    }

    public List<FieldLayoutItem> getFieldLayoutItems()
    {
        return Collections.unmodifiableList(getInternalList());
    }

    public FieldLayoutItem getFieldLayoutItem(OrderableField orderableField)
    {
        if (orderableField == null)
        {
            throw new IllegalArgumentException("OrderableField cannot be null.");
        }

        return getFieldLayoutItem(orderableField.getId());
    }

    public FieldLayoutItem getFieldLayoutItem(String fieldId)
    {
        if (fieldId == null)
        {
            throw new IllegalArgumentException("FieldId cannot be null.");
        }

        return fieldLayoutItemByFieldId.get(fieldId);
    }

    @Override
    public List<FieldLayoutItem> getVisibleLayoutItems(final Project project, final List<String> issueTypes)
    {
        List<FieldLayoutItem> visibleList = findLayout(true, project, issueTypes, new Function<FieldLayoutItem, FieldLayoutItem>()
        {
            public FieldLayoutItem get(final FieldLayoutItem input)
            {
                return input;
            }
        });
        // Neeed to ensure the assignee field is processed after the component fields so that component specific assignees
        // can be properly processed
        orderLayoutItemsForProcessing(visibleList);
        return Collections.unmodifiableList(visibleList);
    }

    @Override
    public List<FieldLayoutItem> getVisibleLayoutItems(User remoteUser, Project project, List<String> issueTypes)
    {
        return getVisibleLayoutItems(project, issueTypes);
    }

    private void orderLayoutItemsForProcessing(List<FieldLayoutItem> fieldLayoutItems)
    {
        // Ensure that the Assignee is processed after the Component field. This can be done by processing the
        // assignee always last.
        for (Iterator iterator = fieldLayoutItems.iterator(); iterator.hasNext();)
        {
            FieldLayoutItem fieldLayoutItem = (FieldLayoutItem) iterator.next();
            if (IssueFieldConstants.ASSIGNEE.equals(fieldLayoutItem.getOrderableField().getId()))
            {
                // Remove the field layout item and place it at the end of the list
                iterator.remove();
                fieldLayoutItems.add(fieldLayoutItem);
                break;
            }
        }
    }

    public List<FieldLayoutItem> getVisibleCustomFieldLayoutItems(Project project, List<String> issueTypes)
    {
        List<FieldLayoutItem> visibleCustomFieldList = new ArrayList<FieldLayoutItem>();
        for (FieldLayoutItem fieldLayoutItem : getInternalList())
        {
            if (getFieldManager().isCustomField(fieldLayoutItem.getOrderableField()))
            {
                if (!fieldLayoutItem.isHidden())
                {
                    CustomField customField = (CustomField) fieldLayoutItem.getOrderableField();
                    if (customField.isInScope(project, issueTypes))
                    {
                        visibleCustomFieldList.add(fieldLayoutItem);
                    }
                }
            }
        }
        return visibleCustomFieldList;
    }

    public List<Field> getHiddenFields(User remoteUser, GenericValue projectGV, List<String> issueTypeIds)
    {
        // User is not used
        final Project project = ComponentAccessor.getProjectFactory().getProject(projectGV);
        return getHiddenFields(project, issueTypeIds);
    }

    public List<Field> getHiddenFields(User remoteUser, Project project, List<String> issueTypeIds)
    {
        // User is not used
        return getHiddenFields(project, issueTypeIds);
    }

    public List<Field> getHiddenFields(Project project, List<String> issueTypeIds)
    {
        List<Field> result = findLayout(false, project, issueTypeIds, new Function<FieldLayoutItem, Field>()
        {
            public Field get(final FieldLayoutItem input)
            {
                return input.getOrderableField();
            }
        });

        result.addAll(getFieldManager().getUnavailableFields());
        return Collections.unmodifiableList(result);
    }

    /**
     * If false is passed returns a list of non-custom hidden fields. If true is passed returns a list of non-custom
     * visible fields
     * <p/>
     * Set retriveFields to true if want to get actual field back and not field layout items
     */
    private <T> List<T> findLayout(boolean notHidden, Project project, List<String> issueTypeIds, Function<FieldLayoutItem, T> func)
    {
        FieldManager fieldManager = getFieldManager();
        List<T> result = new LinkedList<T>();
        for (FieldLayoutItem fieldLayoutItem : getInternalList())
        {
            if (!fieldManager.isCustomField(fieldLayoutItem.getOrderableField()))
            {
                // Note the use of XOR(^). We see if we are looking for non hidden fields (notHidden is set to true),
                // if we are and the field is not hidden, XOR will be true, so we add teh field to the result
                // Similarly, if we are looking for hidden fields (notHidden is set to false), and the field is hidden, XOR(^)
                // will be true, so we add it to the result.
                if (fieldLayoutItem.isHidden() ^ notHidden)
                {
                    result.add(func.get(fieldLayoutItem));
                }
            }
            else
            {
                CustomField customField = (CustomField) fieldLayoutItem.getOrderableField();
                if (notHidden)
                {
                    // Looking for not hidden fields
                    if (!fieldLayoutItem.isHidden() && customField.isInScope(project, issueTypeIds))
                    {
                        // Only add field if it not hidden ans is in scope
                        result.add(func.get(fieldLayoutItem));
                    }
                }
                else
                {
                    // Looking for hidden fields
                    if (fieldLayoutItem.isHidden() || !customField.isInScope(project, issueTypeIds))
                    {
                        // If the field is hidden or is not in scope, add it
                        result.add(func.get(fieldLayoutItem));
                    }
                }
            }
        }

        return result;
    }

    protected FieldManager getFieldManager()
    {
        return ComponentAccessor.getFieldManager();
    }

    protected HackyFieldRendererRegistry getHackyFieldRendererRegistry()
    {
        return ComponentAccessor.getComponentOfType(HackyFieldRendererRegistry.class);
    }

    public List<FieldLayoutItem> getRequiredFieldLayoutItems(Project project, List<String> issueTypes)
    {
        List<FieldLayoutItem> result = new ArrayList<FieldLayoutItem>();
        for (FieldLayoutItem fieldLayoutItem : getInternalList())
        {
            if (fieldLayoutItem.isRequired())
            {
                if (getFieldManager().isCustomField(fieldLayoutItem.getOrderableField()))
                {
                    CustomField customField = getFieldManager().getCustomField(fieldLayoutItem.getOrderableField().getId());
                    if (customField.isInScope(project, issueTypes))
                    {
                        // Ensure that the custom field is in scope if it is marked required
                        result.add(fieldLayoutItem);
                    }
                }
                else
                {
                    result.add(fieldLayoutItem);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    public boolean isFieldHidden(String fieldId)
    {
        // TODO Need to take out this hack
        if (getFieldManager().isOrderableField(fieldId))
        {
            final FieldLayoutItem fieldLayoutItem = getFieldLayoutItem(fieldId);
            return fieldLayoutItem == null || fieldLayoutItem.isHidden();
        }
        else
        {
            return false;
        }
    }

    public String getRendererTypeForField(String fieldId)
    {
        FieldLayoutItem fieldLayoutItem = getFieldLayoutItem(fieldId);
        if (fieldLayoutItem != null)
        {
            return fieldLayoutItem.getRendererType();
        }
        else
        {
            return null;
        }
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        return o instanceof FieldLayout && Objects.equal(id, ((FieldLayout)o).getId());
    }

    public int hashCode()
    {
        return (id != null) ? id.hashCode() : 0;
    }

    protected List<FieldLayoutItem> getInternalList()
    {
        return fieldLayoutItems;
    }

    /**
     * Set the field layout items on this layout
     *
     * @param fieldLayoutItems the items to set
     * @since v4.2
     */
    void setFieldLayoutItems(final List<FieldLayoutItem> fieldLayoutItems)
    {
        this.fieldLayoutItems = fieldLayoutItems;
        if (fieldLayoutItems == null)
        {
            // gets set temporarily to null eg in AbstractFieldLayoutManager.loadFieldLayout()
            fieldLayoutItemByFieldId = null;
            return;
        }
        fieldLayoutItemByFieldId = new HashMap<String, FieldLayoutItem>(fieldLayoutItems.size());
        for (FieldLayoutItem fieldLayoutItem : fieldLayoutItems)
        {
            fieldLayoutItemByFieldId.put(fieldLayoutItem.getOrderableField().getId(), fieldLayoutItem);
        }
    }
}
