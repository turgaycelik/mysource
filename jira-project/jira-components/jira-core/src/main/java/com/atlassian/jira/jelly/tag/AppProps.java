/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.google.common.collect.ImmutableList;
import com.opensymphony.util.TextUtils;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

import java.util.List;

public class AppProps extends TagSupport
{
    private String option;
    private String type;
    private String var;
    private static final List<String> ALLOWED_TYPES = ImmutableList.of("option", "string");

    public void doTag(XMLOutput xmlOutput) throws MissingAttributeException, JellyTagException
    {
        validateInput();

        ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();
        if (getType().equals(ALLOWED_TYPES.get(0)))
        {
            boolean value = applicationProperties.getOption(getOption());
            getContext().setVariable(getVar(), Boolean.valueOf(value));
        }
        else if (getType().equals(ALLOWED_TYPES.get(1)))
        {
            String value = applicationProperties.getString(getOption());
            getContext().setVariable(getVar(), value);
        }
    }

    protected void validateInput() throws JellyTagException
    {
        if (!TextUtils.stringSet(getOption()))
        {
            throw new JellyTagException("The attribute 'option' mustr be set.");
        }

        if (!TextUtils.stringSet(getType()))
        {
            throw new JellyTagException("The attribute 'option' mustr be set.");
        }
        else
        {
            if (!ALLOWED_TYPES.contains(getType()))
            {
                throw new JellyTagException("The attribute 'type' must have one of the following values: " + ALLOWED_TYPES);
            }
        }

        if (!TextUtils.stringSet(getVar()))
        {
            throw new JellyTagException("Attribute 'var' must be set");
        }
    }

    public String getOption()
    {
        return option;
    }

    public void setOption(String option)
    {
        this.option = option;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getVar()
    {
        return var;
    }

    public void setVar(String var)
    {
        this.var = var;
    }
}
