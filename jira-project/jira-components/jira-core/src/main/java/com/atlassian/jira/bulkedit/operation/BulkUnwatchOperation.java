package com.atlassian.jira.bulkedit.operation;

import com.atlassian.jira.bc.issue.watcher.WatcherService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.BulkEditBean;

import java.util.List;

/**
 * Represents an operation able to make an {@link com.atlassian.crowd.embedded.api.User user} no longer the watcher on
 * the list of selected issues in a {@link com.atlassian.jira.web.bean.BulkEditBean}.
 *
 * @since v6.0
 */
public class BulkUnwatchOperation implements ProgressAwareBulkOperation
{
    public static final String NAME = "BulkUnwatch";
    private final WatcherService watcherService;
    public static final String NAME_KEY = "bulk.unwatch.operation.name";

    public BulkUnwatchOperation(final WatcherService watcherService)
    {
        this.watcherService = watcherService;
    }

    @Override
    public boolean canPerform(final BulkEditBean bulkEditBean, final ApplicationUser applicationUser)
    {
        final List<Issue> selectedIssues = bulkEditBean.getSelectedIssues();

        return watcherService.canUnwatchAll(selectedIssues, applicationUser);
    }

    @Override
    public void perform(final BulkEditBean bulkEditBean, final ApplicationUser remoteUser, final Context taskContext)
            throws BulkOperationException
    {
        final List<Issue> selectedIssues = bulkEditBean.getSelectedIssues();

        // Looping over all issues and unwatching one at a time causes really slow indexing.
        // Make sure we use this faster bulk remove method.
        watcherService.removeWatcherFromAll(selectedIssues, remoteUser, remoteUser, taskContext);
    }

    @Override
    public int getNumberOfTasks(final BulkEditBean bulkEditBean)
    {
        return bulkEditBean.getSelectedIssues().size();
    }

    @Override
    public String getOperationName()
    {
        return NAME;
    }

    @Override
    public String getCannotPerformMessageKey()
    {
        return "bulk.unwatch.cannotperform";
    }

    @Override
    public String getNameKey()
    {
        return NAME_KEY;
    }

    @Override
    public String getDescriptionKey()
    {
        return "bulk.unwatch.operation.description";
    }
}
