package com.atlassian.jira.web.action.issue.bulkedit;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bulkedit.BulkOperationManager;
import com.atlassian.jira.bulkedit.operation.BulkWatchOperation;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBeanSessionHelper;

public class BulkWatch extends AbstractBulkWatchOperationAction
{
    public BulkWatch(final TaskManager taskManager, SearchService searchService,
                     BulkOperationManager bulkOperationManager,
                     PermissionManager permissionManager, final I18nHelper i18nHelper,
                     final BulkEditBeanSessionHelper bulkEditBeanSessionHelper)
    {
        super(searchService, bulkOperationManager, permissionManager, bulkEditBeanSessionHelper, taskManager,
                i18nHelper);
    }

    @Override
    protected String getOperationNameKey()
    {
        return BulkWatchOperation.NAME_KEY;
    }

    @Override
    protected String getOperationName()
    {
        return BulkWatchOperation.NAME;
    }

    @Override
    protected String getCannotPerformErrorI18nKey()
    {
        return "bulk.watch.cannotperform.error";
    }

    @Override
    protected String getPerformErrorI18nKey()
    {
        return "bulk.watch.perform.error";
    }

    @Override
    protected String getWatchingDisabledErrorI18nKey()
    {
        return "bulk.watch.watching.disabled";
    }

    @Override
    protected String getProgressMessageI18nKey()
    {
        return "bulk.operation.progress.taskname.watch";
    }
}
