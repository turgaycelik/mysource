package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.subtask.conversion.IssueToSubTaskConversionService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.web.bean.ConvertIssueBean;
import com.atlassian.jira.web.bean.ConvertIssueToSubTaskBean;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.util.TextUtils;

import java.util.Collection;
import java.util.Locale;

/**
 * Main action for converting Issue to sub-task
 * All do* methods in {@link com.atlassian.jira.web.action.issue.AbstractConvertIssue}
 */
public class ConvertIssueToSubTask extends AbstractConvertIssue
{
    private final IssueToSubTaskConversionService service;
    protected String parentIssueKey;
    private final IssueTypeSchemeManager issueTypeSchemeManager;

    private static final String FIELDNAME_PARENT_ISSUE_KEY = "parentIssueKey";
    private static final String ISSUE_TO_SUBTASK = "convert.issue.to.subtask";
    private static final String CONVERT_ISSUE = "ConvertIssue";


    public ConvertIssueToSubTask(IssueToSubTaskConversionService service, IssueManager issueManager, ConstantsManager constantsManager,
                                 WorkflowManager workflowManager, FieldLayoutManager fieldLayoutManager,
                                 RendererManager rendererManager, PermissionManager permissionManager,
                                 IssueTypeSchemeManager issueTypeSchemeManager, IssueFactory issueFactory)
    {
        super(service, issueManager, fieldLayoutManager, constantsManager, workflowManager, rendererManager, issueFactory, permissionManager);
        this.service = service;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
    }


    protected void validateStep1(JiraServiceContextImpl context)
    {
        super.validateStep1(context);
        validateParentIssue(context);
    }

    protected MutableIssue getTargetIssueObjectWithSecurityLevel()
    {
        MutableIssue issue = super.getTargetIssueObjectWithSecurityLevel();
        issue.setParentId(issueManager.getIssueObject(((ConvertIssueToSubTaskBean) getBean()).getParentIssueKey()).getId());
        return issue;
    }

    /**
     * Ensure that the given parent issue is a valid issue and can be set as parent
     *
     * @param context jira service context
     */
    private void validateParentIssue(JiraServiceContext context)
    {
        final String key = getParentIssueKey();
        if (!TextUtils.stringSet(key))
        {
            addI18nError(context, FIELDNAME_PARENT_ISSUE_KEY, "convert.issue.to.subtask.error.noparentissuekey");
        }
        else
        {
            final Issue parentIssue = issueManager.getIssueObject(key);
            if (parentIssue == null)
            {
                addI18nError(context, FIELDNAME_PARENT_ISSUE_KEY, "convert.issue.to.subtask.error.invalidparentissuekey", key);
            }
            else
            {
                setParentIssueKey(parentIssue.getKey());
                ((ConvertIssueToSubTaskBean) getBean()).setParentIssueKey(parentIssue.getKey());
                service.validateParentIssue(context, getIssue(), parentIssue, FIELDNAME_PARENT_ISSUE_KEY);
            }
        }
    }


    protected ConvertIssueBean getBean()
    {
        return ConvertIssueToSubTaskBean.getConvertIssueToSubTaskBean(request.getSession(), getId());
    }

    public void setParentIssueKey(String issueKey)
    {
        this.parentIssueKey = issueKey;
    }


    /**
     * Set the parent issue on the bean.
     */
    protected void initExtraFields(ConvertIssueBean bean, JiraServiceContext context)
    {
        if (wasPassed("parentIssueKey"))
        {
            ((ConvertIssueToSubTaskBean) bean).setParentIssueKey(parentIssueKey.toUpperCase(Locale.ENGLISH));
        }
    }


    public Collection getAvailableIssueTypes()
    {
        return issueTypeSchemeManager.getSubTaskIssueTypesForProject(getIssue().getProjectObject());
    }

    public String getActionPrefix()
    {
        return CONVERT_ISSUE;
    }

    public String getPropertiesPrefix()
    {
        return ISSUE_TO_SUBTASK;
    }

}
