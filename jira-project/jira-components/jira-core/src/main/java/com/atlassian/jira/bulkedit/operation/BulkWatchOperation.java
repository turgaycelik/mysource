package com.atlassian.jira.bulkedit.operation;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.watcher.WatcherService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.BulkEditBean;

import java.util.List;

/**
 * Represents an operation able to make an {@link User user} the watcher on the list of selected issues in a
 * {@link BulkEditBean}.
 *
 * @since v6.0
 */
public class BulkWatchOperation implements ProgressAwareBulkOperation
{
    public static final String NAME = "BulkWatch";
    private final WatcherService watcherService;
    public static final String NAME_KEY = "bulk.watch.operation.name";

    public BulkWatchOperation(final WatcherService watcherService)
    {
        this.watcherService = watcherService;
    }

    @Override
    public boolean canPerform(final BulkEditBean bulkEditBean, final ApplicationUser applicationUser)
    {
        final List<Issue> selectedIssues = bulkEditBean.getSelectedIssues();

        return watcherService.canWatchAll(selectedIssues, applicationUser);
    }

    @Override
    public void perform(final BulkEditBean bulkEditBean, final ApplicationUser remoteUser, final Context taskContext)
            throws BulkOperationException
    {
        final List<Issue> selectedIssues = bulkEditBean.getSelectedIssues();

        // Looping over all issues and watching one at a time causes really slow indexing.
        // Make sure we use this faster bulk add method.
        watcherService.addWatcherToAll(selectedIssues, remoteUser, remoteUser, taskContext);
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
        return "bulk.watch.cannotperform";
    }

    @Override
    public String getNameKey()
    {
        return NAME_KEY;
    }

    @Override
    public String getDescriptionKey()
    {
        return "bulk.watch.operation.description";
    }
}
