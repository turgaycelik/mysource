package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.subtask.conversion.SubTaskToIssueConversionService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.web.bean.ConvertIssueBean;
import com.atlassian.jira.workflow.WorkflowManager;

import java.util.Collection;

/**
 * Main action for converting Sub-tasks into Issues
 * All do* methods in {@link com.atlassian.jira.web.action.issue.AbstractConvertIssue}
 */
public class ConvertSubTaskToIssue extends AbstractConvertIssue
{
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    protected String parentIssueKey;
    private static final String SUBTASK_TO_ISSUE = "convert.subtask.to.issue";
    private static final String CONVERT_SUBTASK = "ConvertSubTask";


    public ConvertSubTaskToIssue(SubTaskToIssueConversionService service,
                                 IssueManager issueManager,
                                 IssueTypeSchemeManager issueTypeSchemeManager,
                                 FieldLayoutManager fieldLayoutManager,
                                 ConstantsManager constantsManager,
                                 WorkflowManager workflowManager,
                                 RendererManager rendererManager,
                                 IssueFactory issueFactory,
                                 PermissionManager permissionManager)
    {
        super(service, issueManager, fieldLayoutManager, constantsManager, workflowManager, rendererManager, issueFactory, permissionManager);
        this.issueTypeSchemeManager = issueTypeSchemeManager;
    }

    protected void initExtraFields(ConvertIssueBean bean, JiraServiceContext context)
    {
        // No extra fields needed
    }

    public Collection getAvailableIssueTypes()
    {
        return issueTypeSchemeManager.getNonSubTaskIssueTypesForProject(getIssue().getProjectObject());
    }

    protected MutableIssue getTargetIssueObjectWithSecurityLevel()
    {
        MutableIssue issue = super.getTargetIssueObjectWithSecurityLevel();
        issue.setParentId(null);

        return issue;
    }

    public String getActionPrefix()
    {
        return CONVERT_SUBTASK;
    }

    public String getPropertiesPrefix()
    {
        return SUBTASK_TO_ISSUE;
    }
}
