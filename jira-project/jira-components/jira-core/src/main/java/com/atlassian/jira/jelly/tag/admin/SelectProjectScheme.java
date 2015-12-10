/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.admin;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.jelly.PermissionSchemeAware;
import com.atlassian.jira.jelly.ProjectContextAccessor;
import com.atlassian.jira.jelly.ProjectContextAccessorImpl;
import com.atlassian.jira.jelly.tag.JellyTagConstants;
import com.atlassian.jira.jelly.tag.ProjectAwareActionTagSupport;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

public class SelectProjectScheme extends ProjectAwareActionTagSupport implements PermissionSchemeAware, ProjectContextAccessor
{
    private static final Logger log = Logger.getLogger(SelectProjectScheme.class);
    public static final String KEY_PROJECT_ID = "projectId";
    private static final String KEY_PROJECT_PERMISSION_SCHEME_IDS = "schemeIds";
    private String[] requiredContextVariables;
    private boolean hasPreviousPermissionSchemeId = false;
    private Long previousPermissionSchemeId = null;
    private final ProjectContextAccessor projectContextAccessor;

    public SelectProjectScheme()
    {
        ignoreErrors = false;
        projectContextAccessor = new ProjectContextAccessorImpl(this);
    }

    protected void preContextValidation()
    {
        final String projectKey = getProperty("projectKey");
        if (projectKey != null)
        {
            setProject(projectKey);
        }

        final String permissionSchemeName = getProperty("permission-scheme");
        final String issueSchemeName = getProperty("issue-scheme");
        if (issueSchemeName != null)
        {
            throw new RuntimeException("You can not set an issue scheme in the Professional Edition of JIRA. If you are using JIRA Enterprise, check that the namespace of this script is correct ('jelly:com.atlassian.jira.jelly.enterprise.JiraTagLib')");
        }

        if (permissionSchemeName != null)
        {
            try
            {
                Collection schemes = ManagerFactory.getPermissionSchemeManager().getSchemes();
                setPreviousPermissionSchemeId(getPreviousPermissionSchemeId());
                getContext().setVariable(JellyTagConstants.PERMISSION_SCHEME_ID, getSchemeId(schemes, permissionSchemeName));
            }
            catch (GenericEntityException e)
            {
                log.error("Could not retreive scheme");
            }
        }

        // depending on which variables is in the context change the required context variables
        String requiredContextField;
        if (hasPermissionScheme())
        {
            requiredContextField = JellyTagConstants.PERMISSION_SCHEME_ID;
        }
        else
        {
            requiredContextField = JellyTagConstants.MISSING_VARIABLE;
        }

        String[] temp = new String[super.getRequiredContextVariables().length + 1];
        System.arraycopy(super.getRequiredContextVariables(), 0, temp, 0, super.getRequiredContextVariables().length);
        temp[temp.length - 1] = requiredContextField;
        requiredContextVariables = temp;
    }

    protected void prePropertyValidation(XMLOutput output) throws JellyTagException
    {
        setProperty(KEY_PROJECT_ID, getProjectId().toString());
        if (hasPermissionScheme())
        {
            setActionName("SelectProjectPermissionScheme");
            setProperty(KEY_PROJECT_PERMISSION_SCHEME_IDS, getPermissionSchemeId().toString());
        }
        else
        {
            throw new IllegalStateException("Validation should have caught no scheme before here.");
        }
    }

    protected void endTagExecution(XMLOutput output)
    {
        loadPreviousProject();
        if (hasPreviousPermissionSchemeId)
        {
            getContext().setVariable(JellyTagConstants.PERMISSION_SCHEME_ID, getPreviousPermissionSchemeId());
        }
    }

    public String[] getRequiredContextVariables()
    {
        return requiredContextVariables;
    }

    public String[] getRequiredProperties()
    {
        if (hasPermissionScheme())
        {
            return new String[]{KEY_PROJECT_ID, KEY_PROJECT_PERMISSION_SCHEME_IDS};
        }
        else
        {
            return new String[]{KEY_PROJECT_ID, "You must specify a permission or issue scheme."};
        }
    }

    public String[] getRequiredContextVariablesAfter()
    {
        return new String[0];
    }

    public boolean hasPermissionScheme()
    {
        return getContext().getVariables().containsKey(JellyTagConstants.PERMISSION_SCHEME_ID);
    }

    public Long getPermissionSchemeId()
    {
        return (Long) getContext().getVariable(JellyTagConstants.PERMISSION_SCHEME_ID);
    }

    public GenericValue getPermissionScheme()
    {
        throw new UnsupportedOperationException();
    }

    private Long getSchemeId(Collection schemes, String schemeName)
    {
        for (final Object scheme1 : schemes)
        {
            GenericValue scheme = (GenericValue) scheme1;
            if (schemeName.equals(scheme.getString("name")))
            {
                return scheme.getLong("id");
            }
        }
        return null;
    }

    private Long getPreviousPermissionSchemeId()
    {
        return previousPermissionSchemeId;
    }

    private void setPreviousPermissionSchemeId(Long previousPermissionSchemeId)
    {
        this.hasPreviousPermissionSchemeId = true;
        this.previousPermissionSchemeId = previousPermissionSchemeId;
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
