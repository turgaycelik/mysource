package com.atlassian.jira.bulkedit.operation;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.BulkEditBean;

/**
 * This interface allows bulk operations to perform bulk operations and report back the progress.
 * It supersedes {@link com.atlassian.jira.bulkedit.operation.BulkOperation} which didn't report the operation progress.
 * <p/>
 * Bulk operations are added by plugins via {@link com.atlassian.jira.bulkedit.BulkOperationManager#addBulkOperation}.
 *
 * @since 6.3.6
 */
@PublicSpi
public interface ProgressAwareBulkOperation extends IssueOperation
{
    /**
     * Returns the &quot;operation name&quot;.
     * <p/>
     * <p>This is used to build up the name of the {@link com.atlassian.jira.action.JiraActionSupport action} used to render the details screen
     * in the bulk change UI.</p>
     * <p/>
     * <p>The action name that is generated as {@code operationName + "Details.jspa"}. See BulkChooseOperation
     * for details</p>
     *
     * @return The &quot;operation name&quot;
     */
    public String getOperationName();

    /**
     * An i18n key to be used to render the error message to be displayed when the user can not perform this bulk
     * operation.
     *
     * @return An i18n key to be used to render the error message to be displayed when the user can not perform this bulk
     * operation.
     */
    String getCannotPerformMessageKey();

    /**
     * Determines whether the operation can be performed with the given set of issues
     *
     * @see com.atlassian.jira.web.bean.BulkEditBean#getSelectedIssues()
     */
    boolean canPerform(BulkEditBean bulkEditBean, ApplicationUser remoteUser);

    /**
     * Performs the operation on the given set of issues and updates the progress.
     * <p/>
     * During execution, the implementation class will update the progress by typically calling
     * {@code Context.start(object);} on the context passed to get a task object, and subsequently call
     * {@code Context.Task.complete();} on the the task object to indicate task has finished.
     */
    void perform(BulkEditBean bulkEditBean, ApplicationUser remoteUser, Context taskContext)
            throws BulkOperationException;

    /**
     * Returns number of tasks/stages to be executed as part of this bulk operation. The number returned here must be
     * consistent with the number of updates performed as part of {@link
     * #perform(com.atlassian.jira.web.bean.BulkEditBean, com.atlassian.jira.user.ApplicationUser,
     * com.atlassian.jira.task.context.Context)}.
     *
     * @return Number of tasks/stages to be executed
     */
    int getNumberOfTasks(BulkEditBean bulkEditBean);
}
