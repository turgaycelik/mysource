/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.issue.enterprise;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.jelly.IssueContextAccessor;
import com.atlassian.jira.jelly.ProjectContextAccessor;
import com.atlassian.jira.jelly.enterprise.IssueSchemeLevelAware;
import com.atlassian.jira.jelly.tag.JellyTagConstants;
import com.atlassian.jira.jelly.tag.issue.AbstractCreateIssue;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.user.util.UserManager;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

public class CreateIssue extends AbstractCreateIssue implements IssueSchemeLevelAware, ProjectContextAccessor, IssueContextAccessor
{
    private static final Logger log = Logger.getLogger(CreateIssue.class);

    public CreateIssue(VersionManager versionManager, UserManager userManager)
    {
        super(versionManager, userManager);
    }

    protected void preContextValidation()
    {
        super.preContextValidation();

        try
        {
            // Check to see if there is a security level and if there is retieve its id and set it in the properties
            String securityLevel = getProperty("security-level");
            if (securityLevel != null)
            {
                List schemes = ManagerFactory.getIssueSecuritySchemeManager().getSchemes(getProject());
                for (int i = 0; i < schemes.size(); i++)
                {
                    GenericValue scheme = (GenericValue) schemes.get(i);
                    List schemeIssueSecurityLevels = ManagerFactory.getIssueSecurityLevelManager().getSchemeIssueSecurityLevels(scheme.getLong("id"));
                    for (int j = 0; j < schemeIssueSecurityLevels.size(); j++)
                    {
                        GenericValue securityLevelGV = (GenericValue) schemeIssueSecurityLevels.get(j);
                        if (securityLevel.equals(securityLevelGV.getString("name")))
                        {
                            setPreviousIssueSchemeLevelId(getIssueSchemeLevelId());
                            getContext().setVariable(JellyTagConstants.ISSUE_SCHEME_LEVEL_ID, securityLevelGV.getLong("id"));

                            // Jump out of loop.
                            j = schemeIssueSecurityLevels.size();
                            i = schemes.size();
                        }
                    }
                }
            }
        }
        catch (GenericEntityException e)
        {
            log.error(e, e);
        }
    }

    protected void prePropertyValidation(XMLOutput output) throws JellyTagException
    {
        if (hasIssueSchemeLevel())
            setProperty(KEY_ISSUE_SECURITY, getIssueSchemeLevelId().toString());
        else
            this.getProperties().remove(KEY_ISSUE_SECURITY);

        super.prePropertyValidation(output);
    }

    public boolean hasIssueSchemeLevel()
    {
        return getContext().getVariables().containsKey(JellyTagConstants.ISSUE_SCHEME_LEVEL_ID);
    }

    public Long getIssueSchemeLevelId()
    {
        if (hasIssueSchemeLevel())
            return (Long) getContext().getVariable(JellyTagConstants.ISSUE_SCHEME_LEVEL_ID);
        else
            return null;
    }

    public GenericValue getIssueSchemeLevel()
    {
        try
        {
            return ManagerFactory.getIssueSecurityLevelManager().getIssueSecurityLevel(getIssueSchemeLevelId());
        }
        catch (GenericEntityException e)
        {
            return null;
        }
    }
}
