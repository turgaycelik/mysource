/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.admin;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.lang.StringUtils;

public class AddCustomFieldSelectValue extends TagSupport
{

    private String value;
    private Option thisOption;
    private int numberAdded = 0;
    private final OptionsManager optionsManager = ManagerFactory.getOptionsManager();;

    public void doTag(XMLOutput xmlOutput) throws MissingAttributeException, JellyTagException
    {
        Tag parent = getParent();
        if (parent instanceof CreateCustomField)
        {
            CreateCustomField parentTag = (CreateCustomField) parent;

            // Retrieve the name of the value from the tag
            if (StringUtils.isNotEmpty(getValue()))
            {
                thisOption = parentTag.addSelectValue(getValue());
                getBody().run(getContext(), xmlOutput);
            }
            else
            {
                throw new MissingAttributeException("value");
            }
        }
        else if (parent instanceof AddCustomFieldSelectValue)
        {
            AddCustomFieldSelectValue parentTag = (AddCustomFieldSelectValue) parent;
            if (StringUtils.isNotEmpty(getValue()))
            {
                thisOption = parentTag.addChildOption(getValue());
                getBody().run(getContext(), xmlOutput);
            }
            else
            {
                throw new MissingAttributeException("value");
            }
        }
        else
        {
            throw new JellyTagException("AddCustomFieldSelectValue tag must be nested inside CreateCustomField.");
        }
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public Option addChildOption(String value)
    {
        final FieldConfig relatedField = thisOption.getRelatedCustomField();
        final Long sequence = new Long(numberAdded);
        Option newOption = optionsManager.createOption(relatedField, thisOption.getOptionId(), sequence, value);
        numberAdded++;

        return newOption;
    }
}
