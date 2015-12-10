/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuesecurity;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.SecurityTypeManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.List;
import java.util.Map;

@WebSudoRequired
public class AddIssueSecurity extends SchemeAwareIssueSecurityAction
{
    private Long securityLevel;
    private String type;

    private final IssueSecurityLevelManager issueSecurityLevelManager;
    private final UserKeyService userKeyService;


    public AddIssueSecurity(IssueSecuritySchemeManager issueSecuritySchemeManager, SecurityTypeManager issueSecurityTypeManager, IssueSecurityLevelManager issueSecurityLevelManager, final UserKeyService userKeyService)
    {
        super(issueSecuritySchemeManager, issueSecurityTypeManager);
        this.issueSecurityLevelManager = issueSecurityLevelManager;
        this.userKeyService = userKeyService;
    }

    protected void doValidation()
    {
        try
        {
            if (schemeNotSelected())
            {
                addErrorMessage(getText("admin.errors.must.select.a.scheme.for.the.issue.security", "\"Permission Schemes\""));
                return;
            }

            if (securityNotSelected())
            {
                addErrorMessage(getText("admin.errors.must.select.a.issue.security.for.this.issue.security"));
                return;
            }
            if (typeNotSelected())
            {
                addErrorMessage(getText("admin.errors.must.select.a.type.for.the.issue.security"));
                return;
            }

            if (!validateIssueSecurityType())
            {
                return;
            }
            validateUniqueIssueSecurity();

        }
        catch (GenericEntityException e)
        {
            addErrorMessage(getText("admin.errors.an.error.occured.adding.the.issue.security") + ":\n" + e.getMessage());
        }
    }

    private boolean schemeNotSelected() throws GenericEntityException
    {
        return getSchemeId() == null || getScheme() == null;
    }

    private boolean securityNotSelected()
    {
        return getSecurity() == null;
    }

    private boolean typeNotSelected()
    {
         return !TextUtils.stringSet(getType());
    }

    private boolean validateIssueSecurityType()
    {
        JiraServiceContext jiraServiceContext = getJiraServiceContext();
        issueSecurityTypeManager.getSchemeType(getType()).doValidation(getType(), getParameters(), jiraServiceContext);
        return !jiraServiceContext.getErrorCollection().hasAnyErrors();
    }

    /**
     * Newly added issue securities must be unique, i.e. not yet exist
     * within given security level.
     *
     * @throws GenericEntityException if retrieval of existing securities for issue level
     * fails
     */
    private void validateUniqueIssueSecurity() throws GenericEntityException
    {
        for (GenericValue securityToCheck : getExistingSecurities())
        {
            if (typesMatch(securityToCheck) && parametersMatch(newSecurityParameter(), securityToCheck))
            {
                addErrorMessage(getText("admin.errors.this.issue.security.already.exists"));
                break;
            }
        }
    }

    private List<GenericValue> getExistingSecurities() throws GenericEntityException
    {
        return getSchemeManager().getEntities(getScheme(), securityLevel);
    }

    private boolean typesMatch(GenericValue securityToMatch)
    {
        return type.equals(securityToMatch.getString("type"));
    }

    private boolean parametersMatch(String newSecurityParam, GenericValue securityToMatch)
    {
        return haveNoParam(newSecurityParam, securityToMatch) || haveMatchingParams(newSecurityParam, securityToMatch);
    }

    private boolean haveNoParam(String newSecurityParameter, GenericValue securityToMatch)
    {
        return newSecurityParameter == null && securityToMatch.getString("parameter") == null;
    }

    private boolean haveMatchingParams(String newSecurityParameter, GenericValue securityToMatch)
    {
        return newSecurityParameter != null && newSecurityParameter.equals(securityToMatch.getString("parameter"));
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        SchemeEntity entity = new SchemeEntity(getType(), getParameter(getType()), getSecurity());
        issueSecuritySchemeManager.createSchemeEntity(getScheme(), entity);

        issueSecurityLevelManager.clearUsersLevels();

        return getRedirect("EditIssueSecurities!default.jspa?schemeId=" + getSchemeId());
    }

    public Map getTypes()
    {
        return issueSecurityTypeManager.getTypes();
    }

    public Long getSecurity()
    {
        return securityLevel;
    }

    public void setSecurity(Long security)
    {
        this.securityLevel = security;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public Map getParameters()
    {
        return ActionContext.getSingleValueParameters();
    }

    public String getParameter(String key)
    {
        String param = (String) getParameters().get(key);
        if (key.equals("user"))
        {
            // Map the username to a userkey
            param = userKeyService.getKeyForUsername(param);
        }
        return TextUtils.stringSet(param) ? param : null;
    }

    private String newSecurityParameter()
    {
        return getParameter(getType());
    }

    public GenericValue getSecurityLevel(Long id) throws Exception
    {
        return issueSecurityLevelManager.getIssueSecurityLevel(id);
    }
}
