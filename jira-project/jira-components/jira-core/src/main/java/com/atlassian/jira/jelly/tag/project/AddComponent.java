/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.project;

import com.atlassian.jira.jelly.ProjectContextAccessor;
import com.atlassian.jira.jelly.ProjectContextAccessorImpl;
import com.atlassian.jira.jelly.tag.ProjectAwareActionTagSupport;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.ofbiz.core.entity.GenericValue;

public class AddComponent extends ProjectAwareActionTagSupport implements ProjectContextAccessor
{
    private final ProjectContextAccessor projectContextAccessor;
    private String KEY_PROJECT_ID = "pid";

    public AddComponent()
    {
        setActionName("AddComponent");
        ignoreErrors = true;
        projectContextAccessor = new ProjectContextAccessorImpl(this);
    }

    protected void preContextValidation()
    {
        final String PROJECT_KEY = "project-key";
        if (getProperties().containsKey(PROJECT_KEY))
        {
            setProject(getProperty(PROJECT_KEY));
        }
    }

    protected void prePropertyValidation(XMLOutput output) throws JellyTagException
    {
        if (hasProject())
        {
            setProperty(KEY_PROJECT_ID, getProjectId().toString());
        }
    }

    protected void endTagExecution(XMLOutput output)
    {
        loadPreviousProject();
    }

    protected void postTagExecution(XMLOutput output) throws JellyTagException
    {
        copyRedirectUrlParametersToTag(getResponse().getRedirectUrl());
        setProject(getProperty("key"));

    }

    public String[] getRequiredProperties()
    {
        return new String[] { KEY_PROJECT_ID };
    }

    public String[] getRequiredContextVariablesAfter()
    {
        return new String[0];
    }

    public void setProject(Long projectId)
    {
        projectContextAccessor.setProject(projectId);
    }

    public void setProject(String projectKey)
    {
        projectContextAccessor.setProject(projectKey);
    }

    public void setProject(GenericValue project)
    {
        projectContextAccessor.setProject(project);
    }

    public void loadPreviousProject()
    {
        projectContextAccessor.loadPreviousProject();
    }
}
