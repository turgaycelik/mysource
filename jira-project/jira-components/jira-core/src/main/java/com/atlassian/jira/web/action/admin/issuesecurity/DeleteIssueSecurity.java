/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuesecurity;

import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.scheme.SchemeType;
import com.atlassian.jira.security.SecurityTypeManager;
import com.atlassian.jira.security.type.SecurityType;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

@SuppressWarnings ("UnusedDeclaration")
@WebSudoRequired
public class DeleteIssueSecurity extends SchemeAwareIssueSecurityAction
{
    private Long id;
    private boolean confirmed = false;

    private final PermissionTypeManager permTypeManager;
    private final IssueSecurityLevelManager issueSecurityLevelManager;

    public DeleteIssueSecurity(PermissionTypeManager permTypeManager, IssueSecuritySchemeManager issueSecuritySchemeManager, SecurityTypeManager issueSecurityTypeManager, IssueSecurityLevelManager issueSecurityLevelManager)
    {
        super(issueSecuritySchemeManager, issueSecurityTypeManager);
        this.permTypeManager = permTypeManager;
        this.issueSecurityLevelManager = issueSecurityLevelManager;
    }

    /**
     * Validates that a permission id has been passed and that the delete has been confirmed
     */
    protected void doValidation()
    {
        if (id == null)
            addErrorMessage(getText("admin.errors.issuesecurity.specify.permission.to.delete"));
        if (!confirmed)
            addErrorMessage(getText("admin.errors.issuesecurity.confirm.deletion"));
    }

    /**
     * Deletes the specified permission
     * @return String indicating result of action
     * @throws Exception
     * @see com.atlassian.jira.issue.security.IssueSecuritySchemeManagerImpl
     */
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        getSchemeManager().deleteEntity(getId());

        issueSecurityLevelManager.clearUsersLevels();

        if (getSchemeId() == null)
            return getRedirect("ViewIssueSecuritySchemes.jspa");
        else
            return getRedirect("EditIssueSecurities!default.jspa?schemeId=" + getSchemeId());
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    /**
     * Gets a issue security object based on the id
     * @return The issue security object
     * @throws GenericEntityException
     * @see com.atlassian.jira.issue.security.IssueSecuritySchemeManagerImpl
     */
    private GenericValue getIssueSecurity() throws GenericEntityException
    {
        return getSchemeManager().getEntity(id);
    }

    public String getIssueSecurityDisplayName() throws GenericEntityException
    {
        String type = getIssueSecurity().getString("type");
        SchemeType schemeType = getType(type);
        if (schemeType != null)
        {
            return schemeType.getDisplayName();
        }
        else
        {
            return type;
        }
    }

    /**
     * Get the permission parameter. This is a value such as the group that has the permission or the current reporter
     * @return The value of the parameter field of the permission object
     * @throws GenericEntityException
     */
    public String getIssueSecurityParameter() throws GenericEntityException
    {
        String param =getIssueSecurity().getString("parameter");
        String type = getIssueSecurity().getString("type");
        SecurityType securityType = permTypeManager.getSecurityType(type);
        if (securityType != null)
        {
            return securityType.getArgumentDisplay(param);
        }
        else
        {
            return "";
        }
    }

    /**
     * Get the name of the permission
     * @return The name of the permission
     */
    public String getIssueSecurityName() throws GenericEntityException
    {
        return issueSecurityLevelManager.getIssueSecurityName(getIssueSecurity().getLong("security"));
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed)
    {
        this.confirmed = confirmed;
    }
}
