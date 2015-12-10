/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuesecurity;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.security.SecurityTypeManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.JiraEntityUtils;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.collections.map.ListOrderedMap;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;

@SuppressWarnings ("UnusedDeclaration")
@WebSudoRequired
public class DeleteIssueSecurityLevel extends SchemeAwareIssueSecurityAction
{
    private List<GenericValue> affectedIssues;
    private Long levelId;
    private Long swapLevel;

    private final IssueSecurityLevelManager issueSecurityLevelManager;

    public DeleteIssueSecurityLevel(IssueSecuritySchemeManager issueSecuritySchemeManager, SecurityTypeManager issueSecurityTypeManager, IssueSecurityLevelManager issueSecurityLevelManager)
    {
        super(issueSecuritySchemeManager, issueSecurityTypeManager);
        this.issueSecurityLevelManager = issueSecurityLevelManager;
    }

    /**
     * Swap the security level on all issues to the chosen level. If no level is chosen then the security will
     * be set to null. The issue cache is flushed and the security level record is removed
     *
     * @throws Exception
     */
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        for (final GenericValue issue : getAffectedIssues())
        {
            issue.set("security", swapLevel);
            issue.store();
        }

        //if it is the default level for the scheme then set the value to null
        if (isDefault(levelId))
        {
            GenericValue scheme = getScheme();
            if (scheme != null)
            {
                scheme.set("defaultlevel", null);
                scheme.store();
            }
        }

        //Also need to reindex the because Issue level security is done with lucene for searching
        ComponentAccessor.getIssueIndexManager().reIndexIssues(getAffectedIssues());

        //remove the security level
        issueSecurityLevelManager.deleteSecurityLevel(levelId);

        issueSecurityLevelManager.clearUsersLevels();

        return getRedirect(getRedirectURL());
    }

    public List<GenericValue> getAffectedIssues()
    {
        if (affectedIssues == null)
        {
            try
            {
                affectedIssues = ComponentAccessor.getOfBizDelegator().findByAnd("Issue", EasyMap.build("security", levelId));
            }
            catch (Exception e)
            {
                addErrorMessage(getText("admin.errors.exception") + " " + e);
                return null;
            }
        }

        return affectedIssues;
    }

    public Long getLevelId()
    {
        return levelId;
    }

    public void setLevelId(Long levelId)
    {
        this.levelId = levelId;
    }

    public String getIssueSecurityName() throws GenericEntityException
    {
        return issueSecurityLevelManager.getIssueSecurityName(levelId);
    }

    public String getRedirectURL()
    {
        return "EditIssueSecurities!default.jspa?schemeId=" + getSchemeId();
    }

    /**
     * Return all other security levels in this scheme
     */
    public Map getOtherLevels()
    {
        Map levels = new ListOrderedMap();

        try
        {
            List schemeIssueSecurities = issueSecurityLevelManager.getSchemeIssueSecurityLevels(getSchemeId());
            levels = JiraEntityUtils.createEntityMap(schemeIssueSecurities, "id", "name");

            //remove the current level
            levels.remove(levelId);
        }
        catch (Exception e)
        {
            addErrorMessage(getText("admin.errors.issuesecurity.exception.getting.versions"));
        }

        return levels;
    }

    public Long getSwapLevel()
    {
        return swapLevel;
    }

    public void setSwapLevel(Long swapLevel)
    {
        if (swapLevel == null || swapLevel.equals(new Long(-1)))
        {
            this.swapLevel = null;
        }
        else
        {
            this.swapLevel = swapLevel;
        }
    }
}
