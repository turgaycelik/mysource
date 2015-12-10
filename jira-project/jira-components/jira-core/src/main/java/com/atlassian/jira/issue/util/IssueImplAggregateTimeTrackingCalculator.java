package com.atlassian.jira.issue.util;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

import java.util.Collection;

/**
 * An implementation of {@link com.atlassian.jira.issue.util.AggregateTimeTrackingCalculator} that is meant for
 * {@link com.atlassian.jira.issue.IssueImpl} usage.  This will work for {@link com.atlassian.jira.issue.DocumentIssueImpl}
 * though the one retreived from the Factory will be more efficient.
 *
 * @since v3.11
 */
public class IssueImplAggregateTimeTrackingCalculator implements AggregateTimeTrackingCalculator
{
    private final PermissionChecker permissionChecker;

    public IssueImplAggregateTimeTrackingCalculator(final JiraAuthenticationContext context, final PermissionManager permissionManager)
    {
        this(new PermissionChecker()
        {
            public boolean hasPermission(Issue subTask)
            {
                return permissionManager.hasPermission(Permissions.BROWSE, subTask, context.getUser());
            }
        });
    }

    public IssueImplAggregateTimeTrackingCalculator(PermissionChecker permissionChecker)
    {
        this.permissionChecker = permissionChecker;
    }

    /**
     * Creates and returns a bean that contains all aggregate time tracking information for given issue(not
     * a sub-task). This information is gathered from all issue's sub-tasks that a user in this context has
     * permission to see.
     *
     * @param issue issue to calculate aggregates for.
     * @return The bean containing all aggregate values.
     */
    public AggregateTimeTrackingBean getAggregates(Issue issue)
    {
        if (issue == null)
        {
            throw new IllegalArgumentException("The issue must not be null");
        }

        final AggregateTimeTrackingBean bean = new AggregateTimeTrackingBean(issue.getOriginalEstimate(), issue.getEstimate(), issue.getTimeSpent(), 0);
        if (issue.isSubTask())
        {
            return bean;
        }

        final Collection<Issue> subTasks = issue.getSubTaskObjects();
        if (subTasks != null && !subTasks.isEmpty())
        {
            int subTaskCount = 0;
            for (final Issue subTask : subTasks)
            {
                // only include sub-tasks that user has permission to see
                if (permissionChecker.hasPermission(subTask))
                {
                    bean.setRemainingEstimate(AggregateTimeTrackingBean.addAndPreserveNull(subTask.getEstimate(), bean.getRemainingEstimate()));
                    bean.setOriginalEstimate(AggregateTimeTrackingBean.addAndPreserveNull(subTask.getOriginalEstimate(), bean.getOriginalEstimate()));
                    bean.setTimeSpent(AggregateTimeTrackingBean.addAndPreserveNull(subTask.getTimeSpent(), bean.getTimeSpent()));
                    bean.bumpGreatestSubTaskEstimate(subTask.getOriginalEstimate(), subTask.getEstimate(), subTask.getTimeSpent());
                    subTaskCount++;
                }
            }
            bean.setSubTaskCount(subTaskCount);
        }
        return bean;
    }

    /**
     * Responsible for doing permission checks for an issue.
     *
     * @since v3.11
     */
    public interface PermissionChecker
    {
        /**
         * Returns true if a user in this context has a browse permission
         * {@link Permissions#BROWSE} for given issue (sub-task), false otherwise.
         *
         * @param subTask sub-task to check the permission for
         * @return true if a user in this context has a browse permission for given issue (sub-task), false otherwise
         */
        boolean hasPermission(Issue subTask);
    }
}
