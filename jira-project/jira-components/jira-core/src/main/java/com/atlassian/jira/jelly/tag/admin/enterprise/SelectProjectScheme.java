/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.admin.enterprise;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.jelly.IssueSchemeAware;
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

public class SelectProjectScheme extends ProjectAwareActionTagSupport implements PermissionSchemeAware, IssueSchemeAware, ProjectContextAccessor
{
    private static final Logger log = Logger.getLogger(SelectProjectScheme.class);
    public static final String KEY_PROJECT_ID = "projectId";
    private static final String KEY_PROJECT_PERMISSION_SCHEME_IDS = "schemeIds";
    private static final String KEY_PROJECT_ISSUE_SCHEME_ID = "newSchemeId";
    private String[] requiredContextVariables;
    private boolean hasPreviousPermissionSchemeId = false;
    private Long previousPermissionSchemeId = null;
    private boolean hasPreviousIssueSchemeId = false;
    private Long previousIssueSchemeId = null;
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
        if (permissionSchemeName != null || issueSchemeName != null)
        {
            Collection schemes = null;
            try
            {
                if (permissionSchemeName != null)
                {
                    schemes = ManagerFactory.getPermissionSchemeManager().getSchemes();
                    setPreviousPermissionSchemeId(getPreviousPermissionSchemeId());
                    getContext().setVariable(JellyTagConstants.PERMISSION_SCHEME_ID, getSchemeId(schemes, permissionSchemeName));
                }
                else
                {
                    schemes = ManagerFactory.getIssueSecuritySchemeManager().getSchemes();
                    setPreviousIssueSchemeId(getIssueSchemeId());
                    getContext().setVariable(JellyTagConstants.ISSUE_SCHEME_ID, getSchemeId(schemes, issueSchemeName));
                }
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
        else if (hasIssueScheme())
        {
            requiredContextField = JellyTagConstants.ISSUE_SCHEME_ID;
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
        else if (hasIssueScheme())
        {
            setActionName("SelectProjectSecuritySchemeStep2");
            setProperty(KEY_PROJECT_ISSUE_SCHEME_ID, getIssueSchemeId().toString());
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
            getContext().setVariable(JellyTagConstants.PERMISSION_SCHEME_ID, getPreviousPermissionSchemeId());
        if (hasPreviousIssueSchemeId)
            getContext().setVariable(JellyTagConstants.ISSUE_SCHEME_ID, getPreviousIssueSchemeId());
    }

    public String[] getRequiredContextVariables()
    {
        return requiredContextVariables;
    }

    public String[] getRequiredProperties()
    {
        if (hasPermissionScheme())
        {
            return new String[] { KEY_PROJECT_ID, KEY_PROJECT_PERMISSION_SCHEME_IDS };
        }
        else if (hasIssueScheme())
        {
            return new String[] { KEY_PROJECT_ID, KEY_PROJECT_ISSUE_SCHEME_ID };
        }
        else
        {
            return new String[] { KEY_PROJECT_ID, "You must specify a permission or issue scheme." };
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

    public boolean hasIssueScheme()
    {
        return getContext().getVariables().containsKey(JellyTagConstants.ISSUE_SCHEME_ID);
    }

    public Long getIssueSchemeId()
    {
        return (Long) getContext().getVariable(JellyTagConstants.ISSUE_SCHEME_ID);
    }

    public GenericValue getIssueScheme()
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

    private Long getPreviousIssueSchemeId()
    {
        return previousIssueSchemeId;
    }

    private void setPreviousIssueSchemeId(Long previousIssueSchemeId)
    {
        this.hasPreviousIssueSchemeId = true;
        this.previousIssueSchemeId = previousIssueSchemeId;
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
