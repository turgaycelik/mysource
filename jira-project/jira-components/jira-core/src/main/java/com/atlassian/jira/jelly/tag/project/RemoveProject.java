/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.project;

import com.atlassian.jira.jelly.tag.ProjectAwareActionTagSupport;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.log4j.Logger;

public class RemoveProject extends ProjectAwareActionTagSupport
{
    private static final transient Logger log = Logger.getLogger(RemoveProject.class);
    private static final String PROJECT_ID = "pId";

    public RemoveProject()
    {
        setActionName("DeleteProject");
    }

    protected void prePropertyValidation(XMLOutput output) throws JellyTagException
    {
        setProperty(PROJECT_ID, getProjectId().toString());
    }

    public String[] getRequiredProperties()
    {
        return new String[] { PROJECT_ID };
    }

    public String[] getRequiredContextVariablesAfter()
    {
        return new String[0];
    }
}
