/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuesecurity;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelScheme;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.scheme.AbstractSchemeAwareAction;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.ParameterUtils;
import com.atlassian.jira.web.action.admin.notification.ProjectAware;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.collections.map.ListOrderedMap;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebSudoRequired
public class SelectProjectSecuritySchemeStep2 extends AbstractSchemeAwareAction implements ProjectAware
{
    //private List affectedIssues;
    private Long origSchemeId;
    private Long newSchemeId;
    private Long projectId;
    private GenericValue project;
    private Map levels = null;
    private static final String LEVEL_PREFIX = "level_";

    private final IssueSecuritySchemeManager issueSecuritySchemeManager;
    private final IssueSecurityLevelManager issueSecurityLevelManager;

    public SelectProjectSecuritySchemeStep2(IssueSecuritySchemeManager issueSecuritySchemeManager, IssueSecurityLevelManager issueSecurityLevelManager)
    {
        this.issueSecuritySchemeManager = issueSecuritySchemeManager;
        this.issueSecurityLevelManager = issueSecurityLevelManager;
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (origSchemeId != null && origSchemeId.equals(newSchemeId))
            addErrorMessage(getText("admin.errors.project.already.associated"));
        else
        {
            changeLevels();
            associateProjectToScheme();
        }

        //clear the project
        ManagerFactory.getIssueSecurityLevelManager().clearProjectLevels(project);
        return getRedirect(getRedirectURL());
    }

    /**
     * Sets the levels of all affected issues to the newly specified levels
     * @throws Exception
     */
    private void changeLevels() throws Exception
    {
        Set keys = getOriginalSecurityLevels().keySet();
        for (Object origLevelObj : keys)
        {
            Object newLevelObj = getLevels().get(origLevelObj);
            Long newLevel = null;
            if (newLevelObj != null)
            {
                newLevel = (Long) newLevelObj;
            }

            List affectedIssues = getAffectedIssues((Long) origLevelObj);

            for (Object affectedIssue : affectedIssues)
            {
                GenericValue issue = (GenericValue) affectedIssue;
                issue.set("security", newLevel);
                issue.store();
                ComponentAccessor.getIssueIndexManager().reIndex(issue);
            }
        }
    }

    private Map getLevels()
    {
        if (levels == null)
        {
            levels = new HashMap();

            // retrieve the list of levels from the parameters map
            final Map parameters = ActionContext.getParameters();
            final Set keys = parameters.keySet();
            for (final Object key1 : keys)
            {
                String key = (String) key1;
                if (key.startsWith(LEVEL_PREFIX))
                {
                    final Long longParam = ParameterUtils.getLongParam(parameters, key);
                    if (longParam.longValue() != -1)
                    {
                        levels.put(new Long(key.substring(LEVEL_PREFIX.length())), longParam);
                    }
                }
            }
        }
        return levels;
    }

    /**
     * Associates the project to the new issue security scheme
     * @throws Exception
     */
    private void associateProjectToScheme() throws Exception
    {
        //remove the schemes from the project
        getSchemeManager().removeSchemesFromProject(getProject());

        //if there is a new scheme then add it otherwise the scheme will not have a security scheme
        if (newSchemeId != null)
        {
            //get the new scheme
            GenericValue scheme = getSchemeManager().getScheme(newSchemeId);

            //add it to the project
            getSchemeManager().addSchemeToProject(getProject(), scheme);
        }
    }

    /**
     * Get all issues that are part of this project and have this security level
     * @param levelId The security level
     * @return A List containing all affected issues
     */
    public List getAffectedIssues(Long levelId)
    {
        Long realLevelId = null;
        if (levelId != null && levelId.longValue() != -1)
            realLevelId = levelId;

        List affectedIssues = new ArrayList();
        try
        {
            affectedIssues = ComponentAccessor.getOfBizDelegator().findByAnd("Issue", EasyMap.build("project", projectId, "security", realLevelId));
        }
        catch (Exception e)
        {
            addErrorMessage(getText("admin.errors.exception")+" " + e);
        }

        return affectedIssues;
    }

    /**
     * Get all issues that are part of this project and have security set on them
     * @return A List containing all affected issues
     */
    public List getTotalAffectedIssues()
    {
        List affectedIssues = new ArrayList();
        try
        {
            //get the list of original security levels
            Map levels = getOriginalSecurityLevels();
            if (levels != null)
            {
                Set levelSet = levels.entrySet();

                for (final Object aLevelSet : levelSet)
                {
                    Map.Entry mapEntry = (Map.Entry) aLevelSet;
                    Long levelId = (Long) mapEntry.getKey();

                    //add all affected issues for this level to the total list
                    affectedIssues.addAll(getAffectedIssues(levelId));
                }
            }
        }
        catch (Exception e)
        {
            addErrorMessage(getText("admin.errors.exception")+" " + e);
        }
        return affectedIssues;
    }

    /**
     * Get the list of Security Levels for the original scheme
     * @return A Map containing the levels
     */
    public Map getOriginalSecurityLevels()
    {
        Map<Long, String> map = new ListOrderedMap();
        map.put(new Long(-1), "None");
        if (origSchemeId != null)
        {
            for (IssueSecurityLevel issueSecurityLevel : issueSecurityLevelManager.getIssueSecurityLevels(origSchemeId))
            {
                map.put(issueSecurityLevel.getId(), issueSecurityLevel.getName());
            }
        }
        return map;
    }

    /**
     * Get the list of Security Levels for the new scheme
     * @return A Map containing the levels
     */
    public Map getNewSecurityLevels()
    {
        Map<Long, String> map = new ListOrderedMap();
        map.put(new Long(-1), "None");
        if (newSchemeId != null)
        {
            for (IssueSecurityLevel issueSecurityLevel : issueSecurityLevelManager.getIssueSecurityLevels(newSchemeId))
            {
                map.put(issueSecurityLevel.getId(), issueSecurityLevel.getName());
            }
        }
        return map;
    }

    public Long getOrigSchemeId()
    {
        return origSchemeId;
    }

    public void setOrigSchemeId(Long origSchemeId)
    {
        if (origSchemeId == null || origSchemeId.equals(new Long(-1)))
            this.origSchemeId = null;
        else
            this.origSchemeId = origSchemeId;
    }

    public Long getNewSchemeId()
    {
        return newSchemeId;
    }

    public void setNewSchemeId(Long newSchemeId)
    {
        if (newSchemeId == null || newSchemeId.equals(new Long(-1)))
            this.newSchemeId = null;
        else
            this.newSchemeId = newSchemeId;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(Long projectId)
    {
        this.projectId = projectId;
    }

    public GenericValue getProject() throws GenericEntityException
    {
        if (project == null)
        {
            project = ManagerFactory.getProjectManager().getProject(getProjectId());
        }
        return project;
    }

    public SchemeManager getSchemeManager()
    {
        return issueSecuritySchemeManager;
    }

    public String getRedirectURL() throws GenericEntityException
    {
        return "/plugins/servlet/project-config/" + getProject().getString("key") + "/issuesecurity";
    }

    public IssueSecurityLevelScheme getSecurityScheme(Long schemeId) throws GenericEntityException
    {
        return issueSecuritySchemeManager.getIssueSecurityLevelScheme(schemeId);
    }

    public static String getLevelPrefix()
    {
        return LEVEL_PREFIX;
    }
}
