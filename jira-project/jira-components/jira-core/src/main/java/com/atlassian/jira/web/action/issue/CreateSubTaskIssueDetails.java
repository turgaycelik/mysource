package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.TemporaryAttachmentsMonitorLocator;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.SessionKeys;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CreateSubTaskIssueDetails extends CreateIssueDetails
{
    public static final String SUB_TASK_LINK_TYPE_NAME = "jira_subtask_link";
    public static final String SUB_TASK_LINK_TYPE_STYLE = "jira_subtask";
    public static final String SUB_TASK_LINK_TYPE_INWARD_NAME = "jira_subtask_inward";
    public static final String SUB_TASK_LINK_TYPE_OUTWARD_NAME = "jira_subtask_outward";

    private final ConstantsManager constantsManager;
    private final SubTaskManager subTaskManager;
    private final IssueService issueService;

    private Long parentIssueId;
    private boolean quickCreateValidation;
    private boolean requiresLogin;
    
    public CreateSubTaskIssueDetails(ConstantsManager constantsManager, SubTaskManager subTaskManager, IssueCreationHelperBean issueCreationHelperBean,
                                     IssueFactory issueFactory, IssueService issueService, TemporaryAttachmentsMonitorLocator temporaryAttachmentsMonitorLocator)
    {
        super(issueFactory, issueCreationHelperBean, issueService, temporaryAttachmentsMonitorLocator);
        this.constantsManager = constantsManager;
        this.subTaskManager = subTaskManager;
        this.issueService = issueService;
        // Do not display issue level security field for sub-tasks as it is inherited from the parent issue
        getIgnoreFieldIds().add(IssueFieldConstants.SECURITY);
        this.quickCreateValidation = false;
    }

    protected void doValidation()
    {
        Long parentIssueId = getParentIssueId();

        // Check that we have a parent issue id
        if (parentIssueId == null)
        {
            addErrorMessage(getText("admin.errors.issues.parent.issue.id.not.set"));
            return;
        }

        final Issue parent = getIssueManager().getIssueObject(parentIssueId);
        if ((parent != null) && !parent.isEditable())
        {
            addErrorMessage(getText("admin.errors.issues.parent.issue.not.editable"));
        }

        getIssueObject().setParentId(getParentIssueId());
        getIssueObject().setProjectId(getPid());
        getIssueObject().setIssueTypeId(getIssuetype());

        final IssueInputParameters issueInputParameters = issueService.newIssueInputParameters(ActionContext.getParameters());

        // If not, then run the create issue code
        this.validationResult = issueService.validateSubTaskCreate(getLoggedInUser(), getParentIssueId(), issueInputParameters);
        if (!this.validationResult.isValid())
        {
             // This page has no fields to show field error messages against, make them error messages instead
            final ErrorCollection errorCollection = this.validationResult.getErrorCollection();
            transferErrorToMessages(errorCollection);
            addErrorCollection(errorCollection);
        }
        this.fieldValuesHolder = this.validationResult.getFieldValuesHolder();
        setIssueObject(validationResult.getIssue());

        if (getReasons() != null && getReasons().contains(Reason.NOT_LOGGED_IN))
        {
            requiresLogin = true;
        }
    }

    protected Collection getIssueTypes()
    {
        return constantsManager.getSubTaskIssueTypes();
    }

    protected String doPostCreationTasks() throws Exception
    {
        if (invalidInput())
        {
            return getResult();
        }

        // Create a link to the parent issue
        createSubTaskLink();

        // In case the sub-task was created a 'quick way' (directly from the view issue page of the parent issue)
        // record the sub-task issue type
        recordHistoryIssueType();

        if (TextUtils.stringSet(getViewIssueKey()))
        {
            Issue viewIssue = getIssueManager().getIssueObject(getViewIssueKey());
            if (ComponentAccessor.getPermissionManager().hasPermission(Permissions.BROWSE, viewIssue, getLoggedInUser()))
            {
                return getRedirect("/browse/" + getViewIssueKey() + "#summary");
            }
        }
        return super.doPostCreationTasks();
    }

    protected void recordHistoryIssueType()
    {
        ActionContext.getSession().put(SessionKeys.USER_HISTORY_SUBTASK_ISSUETYPE, getIssuetype());
    }

    private void createSubTaskLink() throws GenericEntityException, CreateException
    {
        final GenericValue parentIssue = getIssueManager().getIssue(getParentIssueId());
        subTaskManager.createSubTaskIssueLink(parentIssue, getIssue(), getLoggedInUser());
    }

    public String getParentIssueKey()
    {
        try
        {
            final GenericValue parentIssue = getParentIssue();
            if (parentIssue != null)
            {
                return parentIssue.getString("key");
            }
        }
        catch (GenericEntityException e)
        {
            log.error("Error occurred while retrieving parent issue.", e);
            log.error("Error occurred while retrieving parent issue. Please see log for more detail.");
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

    private GenericValue getParentIssue() throws GenericEntityException
    {
        return getIssueManager().getIssue(getParentIssueId());
    }

    public Long getParentIssueId()
    {
        return parentIssueId;
    }

    public void setParentIssueId(Long parentIssueId)
    {
        this.parentIssueId = parentIssueId;
    }

    public boolean isRequiresLogin()
    {
        return requiresLogin;
    }
}
