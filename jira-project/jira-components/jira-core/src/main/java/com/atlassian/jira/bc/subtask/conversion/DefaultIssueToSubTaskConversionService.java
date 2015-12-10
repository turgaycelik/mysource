package com.atlassian.jira.bc.subtask.conversion;

import java.util.Collection;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.workflow.WorkflowManager;

import org.apache.log4j.Logger;

import static com.atlassian.jira.user.util.Users.isAnonymous;

/**
 * Default implementation of {@link IssueToSubTaskConversionService}.
 */
public class DefaultIssueToSubTaskConversionService extends DefaultIssueConversionService implements IssueToSubTaskConversionService
{

    private static final Logger log = Logger.getLogger(DefaultIssueToSubTaskConversionService.class);

    private final PermissionManager permissionManager;
    private final SubTaskManager subtaskManager;

    public DefaultIssueToSubTaskConversionService(PermissionManager permissionManager, SubTaskManager subtaskManager,
                                                  IssueTypeSchemeManager issueTypeSchemeManager,
                                                  JiraAuthenticationContext jiraAuthenticationContext,
                                                  WorkflowManager workflowManager, FieldLayoutManager fieldLayoutManager,
                                                  FieldManager fieldManager, IssueEventManager issueEventManager, IssueEventBundleFactory issueEventBundleFactory)
    {
        super(permissionManager, workflowManager, fieldLayoutManager, issueTypeSchemeManager, jiraAuthenticationContext, fieldManager, issueEventManager, issueEventBundleFactory);
        this.permissionManager = permissionManager;
        this.subtaskManager = subtaskManager;
    }

    /**
     * As per the {@link IssueToSubTaskConversionService} interface.
     * <p/>
     * NOTE: We also considered a check for number of issues in the project and
     * returning false if project has only one issue. This was not implemented
     * as it would impair the performance of this method.
     *
     * @param context JIRA Service Context
     * @param issue   issue to convert
     * @return true if user can convert issue to a sub-task
     * @throws IllegalArgumentException if context or issue is null
     */
    public boolean canConvertIssue(JiraServiceContext context, Issue issue)
    {
        if (context == null)
        {
            throw new IllegalArgumentException("Operation is out of context");
        }
        if (issue == null)
        {
            throw new IllegalArgumentException("Issue not specified");
        }
        final User user = context.getLoggedInUser();
        final ErrorCollection errorCollection = new SimpleErrorCollection();

        // check system config
        if (!areSubTasksEnabled())
        {
            errorCollection.addErrorMessage(getText("convert.issue.to.subtask.errormessage.subtasksdisabled"));
        }

        // check permission
        if (!hasPermission(context, issue))
        {
            if (isAnonymous(user))
            {
                errorCollection.addErrorMessage(getText("convert.issue.to.subtask.errormessage.nopermissionanon"));
            }
            else
            {
                errorCollection.addErrorMessage(getText("convert.issue.to.subtask.errormessage.nopermisionuser"));
            }
        }

        // check issue
        if (issue.isSubTask())
        {
            errorCollection.addErrorMessage(getText("convert.issue.to.subtask.errormessage.subtaskalready", issue.getKey()));
        }
        if (hasIssueSubTasks(issue))
        {
            errorCollection.addErrorMessage(getText("convert.issue.to.subtask.errormessage.issuehassubtasks", issue.getKey()));
        }
        if (!projectHasSubTasks(issue.getProjectObject()))
        {
            errorCollection.addErrorMessage(getText("convert.issue.to.subtask.errormessage.nosubtaskissuetypesforproject", issue.getProjectObject().getKey()));
        }

        context.getErrorCollection().addErrorCollection(errorCollection);
        return !errorCollection.hasAnyErrors();
    }

    public void validateTargetIssueType(JiraServiceContext context, Issue issue, IssueType issueType, final String fieldNameIssueTypeId)
    {
        final ErrorCollection errorCollection = context.getErrorCollection();

        if (!issueType.isSubTask())
        {
            // Issue Type must be a sub-task
            errorCollection.addError(fieldNameIssueTypeId,
                    getText("convert.issue.to.subtask.error.issuetypenotsubtask", issueType.getName()));
        }

        final Collection<IssueType> issueTypes = issueTypeSchemeManager.getIssueTypesForProject(issue.getProjectObject());
        if (!containsIssueTypeWithSameId(issueTypes, issueType.getId()))
        {
            // Issue type not applicable for project
            errorCollection.addError(fieldNameIssueTypeId,
                    getText("convert.issue.to.subtask.error.issuetypenotforproject", issueType.getName()));
        }

    }

    /**
     * Loops through the collection and compares the given issue type ids.
     * Returns true if matching id is found, otherwise returns false.
     *
     * @param issueTypes  collection of IssueType objects
     * @param issueTypeId issue type id to search for
     * @return true if collection contains issue type with matching id
     */
    private boolean containsIssueTypeWithSameId(Collection<IssueType> issueTypes, final String issueTypeId)
    {
        for (final IssueType type : issueTypes)
        {
            if (type.getId().equals(issueTypeId))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * For an issue to subtask conversion, we can safely ignore the issue security field since the subtask
     * will always take the security level of the parent issue.
     */
    protected boolean canIssueSecurityFieldIgnore()
    {
        return true;
    }


    public void validateParentIssue(JiraServiceContext context, Issue issue, Issue parentIssue, final String fieldNameParentIssueKey)
    {
        final ErrorCollection errorCollection = context.getErrorCollection();

        // must be non-sub-task
        if (parentIssue.isSubTask())
        {
            errorCollection.addError(fieldNameParentIssueKey, getText("convert.issue.to.subtask.error.parentissubtask", parentIssue.getKey())); // Parent can not be sub-task
        }
        // not self
        if (parentIssue.getId().equals(issue.getId()))
        {
            errorCollection.addError(fieldNameParentIssueKey, getText("convert.issue.to.subtask.error.parentsameissue", parentIssue.getKey())); // Must not be same issue
        }
        // same project
        if (!parentIssue.getProjectObject().getId().equals(issue.getProjectObject().getId()))
        {
            errorCollection.addError(fieldNameParentIssueKey, getText("convert.issue.to.subtask.error.differentproject", parentIssue.getKey(), issue.getKey())); // Must be in same project
        }
        // can see parent
        if (!permissionManager.hasPermission(Permissions.BROWSE, parentIssue, context.getLoggedInApplicationUser()))
        {
            errorCollection.addError(fieldNameParentIssueKey, getText("convert.issue.to.subtask.error.invalidparentissuekey", parentIssue.getKey())); // Must be able to see parent
        }
        if(!parentIssue.isEditable())
        {
            errorCollection.addError(fieldNameParentIssueKey, getText("convert.issue.to.subtask.error.parentnoteditable", parentIssue.getKey()));
        }
    }

    public void preStoreUpdates(JiraServiceContext context, IssueChangeHolder changeHolder, Issue currentIssue, MutableIssue targetIssue)
    {
        final Issue parentIssue = targetIssue.getParentObject();
        changeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Parent", null, null, parentIssue.getId().toString(), parentIssue.getKey()));

        updateTargetIssueSecurityLevel(changeHolder, currentIssue, targetIssue, parentIssue);

        try
        {
            subtaskManager.createSubTaskIssueLink(parentIssue, targetIssue, context.getLoggedInUser());
        }
        catch (CreateException e)
        {
            String msg = "Could not create sub-task issue link for '" + targetIssue.getKey() + "' (issue) and '"
                    + parentIssue.getKey() + "' (parent)";
            log.error(msg, e);
            throw new DataAccessException(msg, e);
        }
    }

    /**
     * Sets the parent's security level on the target issue and add a new
     * change item to the change holder if security level of the target or
     * currnet issue is different from the parent issue's.
     *
     * @param changeHolder change holder
     * @param currentIssue current issue - original issue
     * @param targetIssue  target issue - sub-task
     * @param parentIssue  parent issue
     */
    private void updateTargetIssueSecurityLevel(IssueChangeHolder changeHolder, Issue currentIssue, MutableIssue targetIssue, Issue parentIssue)
    {
        targetIssue.setSecurityLevel(parentIssue.getSecurityLevel());

        final Long targetSecurityLevelId = targetIssue.getSecurityLevelId();
        final Long currentSecurityLevelId = currentIssue.getSecurityLevelId();
        final Long parentSecurityLevelId = parentIssue.getSecurityLevelId();
        if (isNotNullAndNotEqualTo(targetSecurityLevelId, parentSecurityLevelId)
                || isNotNullAndNotEqualTo(currentSecurityLevelId, parentSecurityLevelId))
        {
            changeHolder.addChangeItem(new ChangeItemBean(
                    ChangeItemBean.STATIC_FIELD,
                    IssueFieldConstants.SECURITY,
                    getLongToStringNullSafe(currentSecurityLevelId),
                    getIssueNameNullSafe(currentIssue),
                    getLongToStringNullSafe(targetSecurityLevelId),
                    getIssueNameNullSafe(targetIssue)));
        }
    }

    /**
     * Returns true if id1 is not null and not equal to id2, false otherwise.
     *
     * @param id1 id1
     * @param id2 id2
     * @return true if id1 is not null and not equal to id2, false otherwise
     */
    protected static boolean isNotNullAndNotEqualTo(Long id1, Long id2)
    {
        return id1 != null && !id1.equals(id2);
    }

    private static String getIssueNameNullSafe(Issue issue)
    {
        return issue.getSecurityLevel() == null ? null : issue.getSecurityLevel().getString("name");
    }

    /**
     * Returns a String as a result of toString() call or null if aLong was null.
     *
     * @param aLong long
     * @return String as a result of toString() call or null if aLong was null
     */
    protected static String getLongToStringNullSafe(Long aLong)
    {
        return (aLong == null ? null : aLong.toString());
    }

    /**
     * Is JIRA configured to use sub-tasks
     *
     * @return true if sub-tasks are enabled
     */
    private boolean areSubTasksEnabled()
    {
        return subtaskManager.isSubTasksEnabled();
    }

    /**
     * Does the given issue have sub-tasks
     *
     * @param issue issue to test
     * @return true if the given issue has sub-tasks
     */
    private boolean hasIssueSubTasks(Issue issue)
    {
        return !issue.getSubTaskObjects().isEmpty();
    }

    /**
     * Returns true if given project has sub-task Issue Types in its Issue Type scheme
     *
     * @param project to to test
     * @return true if project has sub-task Issue Types in its Issue Type Scheme
     */
    protected boolean projectHasSubTasks(Project project)
    {
        return !issueTypeSchemeManager.getSubTaskIssueTypesForProject(project).isEmpty();
    }

}
