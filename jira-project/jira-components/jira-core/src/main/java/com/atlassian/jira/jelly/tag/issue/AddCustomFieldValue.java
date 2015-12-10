/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.issue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.CustomField;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.lang.StringUtils;

public class AddCustomFieldValue extends TagSupport
{

    private String id;
    private String name;
    private String value;
    private String key;

    public void doTag(XMLOutput xmlOutput) throws MissingAttributeException, JellyTagException
    {
        validate();

        // Check to see if this tag is inside a CreateIssue Tag.
        CustomFieldValuesAwareTag parent = findCustomFieldValuesAwareTag(this);
        if (parent != null)
        {
            CustomFieldValuesAwareTag customFieldValuesAwareTag = (CustomFieldValuesAwareTag) parent;

            // Check to see if there is an id
            // Retrieve the custom field value from this tag

            CustomField customField = null;
            if (StringUtils.isNotEmpty(getId()))
            {
                customField = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(getId());
            }
            else if (StringUtils.isNotEmpty(getName()))
            {
                // Retrieve the custom field from its name
                customField = ComponentAccessor.getCustomFieldManager().getCustomFieldObjectByName(getName());
            }

            if (customField != null)
            {
                customFieldValuesAwareTag.addCustomFieldValue(customField, getValue(), getKey());
            }
            else
            {
                throw new JellyTagException("Custom Field id/name is not valid");
            }
        }
        else
        {
            throw new JellyTagException("AddCustomFieldValue must be inside a CreateIssue or a TransitionWorkflow tag");
        }
    }

    private void validate() throws MissingAttributeException
    {
        if (StringUtils.isEmpty(getValue()))
        {
            throw new MissingAttributeException("value");
        }

        CustomField customField;
        if (StringUtils.isEmpty(getId()) && StringUtils.isEmpty(getName()))
        {
            throw new MissingAttributeException("id or name");
        }

    }

    /**
     * Looks for a parent tag that is a CustomFieldValuesAwareTag, and returns
     * it, if it doesn't find one then null is returned.
     */
    private CustomFieldValuesAwareTag findCustomFieldValuesAwareTag(Tag tag)
    {
        if (tag == null || tag instanceof CustomFieldValuesAwareTag)
        {
            return (CustomFieldValuesAwareTag) tag;
        }
        else
        {
            return findCustomFieldValuesAwareTag(tag.getParent());
        }
    }


    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }
}
