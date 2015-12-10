/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.project;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.jelly.ProjectContextAccessor;
import com.atlassian.jira.jelly.ProjectContextAccessorImpl;
import com.atlassian.jira.jelly.tag.ProjectAwareActionTagSupport;
import com.atlassian.jira.project.version.VersionManager;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

public class AddVersion extends ProjectAwareActionTagSupport implements ProjectContextAccessor
{
    private static final transient Logger log = Logger.getLogger(AddVersion.class);

    private static final String KEY_PROJECT_ID = "pid";
    private static final String KEY_VERSION_ID_VAR = "versionIdVar";

    private final ProjectContextAccessor projectContextAccessor = new ProjectContextAccessorImpl(this);

    private String versionIdVar;

    public AddVersion()
    {
        setActionName("AddVersion");
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

    protected void postTagExecution(XMLOutput output) throws JellyTagException
    {
        if (StringUtils.isNotEmpty(getProperty(KEY_VERSION_ID_VAR)))
        {
            VersionManager vm = ComponentAccessor.getVersionManager();
            Long createdVersionId = vm.getVersion(getProjectId(), getProperty("name")).getId();
            getContext().setVariable(getProperty(KEY_VERSION_ID_VAR), createdVersionId);
        }

    }

    protected void endTagExecution(XMLOutput output)
    {
        loadPreviousProject();
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

    public void setProject(GenericValue projectKey)
    {
        projectContextAccessor.setProject(projectKey);
    }

    public void loadPreviousProject()
    {
        projectContextAccessor.loadPreviousProject();
    }

    public String getVersionIdVar()
    {
        return versionIdVar;
    }

    public void setVersionIdVar(String versionIdVar)
    {
        this.versionIdVar = versionIdVar;
    }

}
