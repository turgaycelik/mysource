package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.workflow.WorkflowManager;
import webwork.action.ActionContext;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Step during the move issue wizard to migrate the issue types of sub-tasks whose issue type is not valid in the
 * destination project.
 *
 * @since v4.0
 */
public class MoveIssueSubtasks extends MoveIssue
{
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    Collection<IssueType> subtaskIssueTypes;
    Collection<IssueType> projectIssueTypes;

    public MoveIssueSubtasks(SubTaskManager subTaskManager, ConstantsManager constantsManager, WorkflowManager workflowManager, FieldManager fieldManager, FieldLayoutManager fieldLayoutManager,
            IssueFactory issueFactory, FieldScreenRendererFactory fieldScreenRendererFactory, CommentService commentService, final IssueTypeSchemeManager issueTypeSchemeManager, UserUtil userUtil)
    {
        super(subTaskManager, constantsManager, workflowManager, fieldManager, fieldLayoutManager, issueFactory, fieldScreenRendererFactory, commentService, userUtil);
        this.issueTypeSchemeManager = issueTypeSchemeManager;
    }

    /**
     * Handles an initial request to determine whether the issue type of any of the sub-tasks of the issue being moved
     * need to be migrated.
     * @return It does not return anything actually; it redirects you to
     * {@link com.atlassian.jira.web.action.issue.enterprise.MoveIssueUpdateWorkflow#doDefault()}}
     */
    public String doDefault()
    {
        if (getMoveIssueBean() == null)
        {
            return redirectToSessionTimeoutPage();
        }

        getMoveIssueBean().addAvailablePreviousStep(1);
        getMoveIssueBean().setCurrentStep(1);
        if (isNeedsSubtaskIssueTypeMigration())
        {
            return INPUT;
        }
        else
        {
            try
            {
                return forceRedirect("MoveIssueUpdateWorkflow!default.jspa?id=" + id + "&assignee=" + URLEncoder.encode("" + getAssignee(), "UTF8"));
            }
            catch (UnsupportedEncodingException e)
            {
                // UTF8 doesn't exist. Should not happen. I hate you Java and your darned checked exceptions.
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Handles the request to process the new issue types that have been assigned to the issue's sub-tasks.
     * @return It does not return anything actually; it redirects you to
     * {@link com.atlassian.jira.web.action.issue.enterprise.MoveIssueUpdateWorkflow#doDefault()} if your session has not
     * timed out.
     */
    protected String doExecute()
    {
        if (getMoveIssueBean() == null)
        {
            return redirectToSessionTimeoutPage();
        }
        return forceRedirect("MoveIssueUpdateWorkflow!default.jspa?id=" + getIssue().getString("id"));
    }

    protected void doValidation()
    {
        if (getMoveIssueBean() != null)
        {
            if (!hasIssuePermission(Permissions.MOVE_ISSUE, getIssueObject()))
            {
                // Add error message and do not continue
                addErrorMessage(getText("move.issue.nopermissions"));
            }
            else
            {
                Map actionParameters = ActionContext.getParameters();
                for (IssueType issueType : getMigrateIssueTypes())
                {
                    final String key = getPrefixIssueTypeId(issueType.getId());
                    final Object o = actionParameters.get(key);
                    if (o instanceof String[])
                    {
                        String[] strings = (String[]) o;
                        // There should only be one target issue type
                        final String targetIssueTypeId = strings[0];
                        final IssueType targetIssueType = constantsManager.getIssueTypeObject(targetIssueTypeId);
                        if (getProjectSubtaskIssueTypes().contains(targetIssueType))
                        {
                            getMoveIssueBean().getFieldValuesHolder().put(key, targetIssueTypeId);
                        }
                        else
                        {
                            addErrorMessage(getText("createissue.invalidissuetype"));
                        }
                    }
                    else
                    {
                        addErrorMessage(getText("createissue.invalidissuetype"));
                    }
                }
            }
        }
    }

    public Collection<IssueType> getProjectSubtaskIssueTypes()
    {
        if (projectIssueTypes == null)
        {
            final Long projectId = getMoveIssueBean().getTargetPid();
            Project project = projectManager.getProjectObj(projectId);
            projectIssueTypes = issueTypeSchemeManager.getSubTaskIssueTypesForProject(project);
        }
        return projectIssueTypes;
    }

    private boolean isNeedsSubtaskIssueTypeMigration()
    {
        return (!getProjectSubtaskIssueTypes().containsAll(getSubtaskIssueTypes()));

    }

    public Collection<IssueType> getMigrateIssueTypes()
    {
        Collection<IssueType> subtaskIssueTypes = new HashSet<IssueType>(getSubtaskIssueTypes());
        subtaskIssueTypes.removeAll(getProjectSubtaskIssueTypes());
        return subtaskIssueTypes;
    }

    Collection<Issue> getSubtaskObjects()
    {
        return getSubTaskManager().getSubTaskObjects(getIssueObject());
    }

    Collection<IssueType> getSubtaskIssueTypes()
    {
        Set<IssueType> types = new HashSet<IssueType>();

        Collection<? extends Issue> subtasks = getSubtaskObjects();
        for (Issue subTask : subtasks)
        {
            types.add(subTask.getIssueTypeObject());
        }

        return types;
    }
}
