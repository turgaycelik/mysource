package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.web.SessionKeys;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CreateSubTaskIssue extends CreateIssue
{
    private final ConstantsManager constantsManager;
    private final IssueManager issueManager;

    private Long parentIssueId;

    public CreateSubTaskIssue(ConstantsManager constantsManager, IssueManager issueManager, IssueCreationHelperBean issueCreationHelperBean, IssueFactory issueFactory)
    {
        super(issueFactory, issueCreationHelperBean);
        this.constantsManager = constantsManager;
        this.issueManager = issueManager;
        // Do not display issue level security field for sub-tasks as it is inherited from the parent issue
        getIgnoreFieldIds().add(IssueFieldConstants.SECURITY);
    }

    protected String getRedirectForCreateBypass()
    {
        return forceRedirect("CreateSubTaskIssue.jspa?parentIssueId=" + getParentIssueId() + "&pid=" + getPid() + "&issuetype=" + getIssuetype());
    }

    /**
     * Checks if there is only one sub-task issue type for the project of the parent issue.
     * If this is the case, the custom field values holder will be populated with those values.
     *
     * @return true if the field population occurred; false otherwise
     */
    boolean prepareFieldsIfOneOption()
    {
        final MutableIssue parent = issueManager.getIssueObject(parentIssueId);
        if (parent == null)
        {
            return false;
        }

        // need this here to check permissions which were previously only checked when running the JSP
        if (getAllowedProjects().isEmpty())
        {
            return false;
        }

        final Project project = parent.getProjectObject();
        Collection issueTypes = getIssueTypesForProject(project);
        if (issueTypes.size() == 1)
        {
            final IssueType issueType = (IssueType) issueTypes.iterator().next();
            setPid(project.getId());
            getFieldValuesHolder().put(IssueFieldConstants.PROJECT, project.getId());
            setIssuetype(issueType.getId());
            getFieldValuesHolder().put(IssueFieldConstants.ISSUE_TYPE, issueType.getId());
            return true;
        }
        return false;
    }

    protected void setHistoryIssuetype()
    {
        // Set the history sub-task issue type
        String historySubTaskIssueType = (String) ActionContext.getSession().get(SessionKeys.USER_HISTORY_SUBTASK_ISSUETYPE);
        setIssuetype(historySubTaskIssueType);
        getFieldValuesHolder().put(IssueFieldConstants.ISSUE_TYPE, historySubTaskIssueType);
    }

    protected void recordHistoryIssueType()
    {
        ActionContext.getSession().put(SessionKeys.USER_HISTORY_SUBTASK_ISSUETYPE, getIssuetype());
    }

    protected void validateIssueType()
    {
        getIssueObject().setParentId(getParentIssueId());
        issueCreationHelperBean.validateIssueType(getIssueObject(), this, ActionContext.getParameters(), this, this);
    }

    public Collection getIssueTypes()
    {
        return constantsManager.getSubTaskIssueTypes();
    }

    public Long getParentIssueId()
    {
        return parentIssueId;
    }

    public void setParentIssueId(Long parentIssueId)
    {
        this.parentIssueId = parentIssueId;
    }

    public MutableIssue getIssueObject()
    {
        if (getIssueObjectWithoutDatabaseRead() == null)
        {
            MutableIssue issue = super.getIssueObject();
            issue.setParentId(getParentIssueId());
            issue.setProjectId(getPid());
        }

        return getIssueObjectWithoutDatabaseRead();
    }

    public Long getPid()
    {
        final Long parentIssueId = getParentIssueId();
        final Issue issue = issueManager.getIssueObject(parentIssueId);
        if (issue != null)
        {
            return issue.getLong("project");
        }
        else
        {
            log.error("Issue with id '" + parentIssueId + "' does not exist or could not be retrieved.");
            return null;
        }
    }

    public String getParentIssueKey()
    {
        final Issue parentIssue = getParentIssue();
        if (parentIssue != null)
        {
            return parentIssue.getKey();
        }

        return null;
    }

    /**
     * Gets the relative path to the parent issue.
     * It does not include the {@link javax.servlet.http.HttpServletRequest#getContextPath() context path}.
     * @return The relative path to the parent issue.
     */
    public String getParentIssuePath()
    {
        return "/browse/" + getParentIssueKey();
    }

    private Issue getParentIssue()
    {
        return getIssueManager().getIssueObject(getParentIssueId());
    }

    /**
     * Returns a collection of sub-tasks issue types for the given project.
     *
     * @param project project to get the sub-tasks issue types for
     * @return a collection of sub-tasks issue types for the given project
     */
    protected Collection getIssueTypesForProject(final Project project)
    {
        IssueTypeSchemeManager issueTypeSchemeManager = ComponentAccessor.getIssueTypeSchemeManager();
        return issueTypeSchemeManager.getSubTaskIssueTypesForProject(project);
    }

    public Map<String, Object> getDisplayParams()
    {
        final Map<String, Object> displayParams = new HashMap<String, Object>();
        displayParams.put("theme", "aui");
        return displayParams;
    }
}
