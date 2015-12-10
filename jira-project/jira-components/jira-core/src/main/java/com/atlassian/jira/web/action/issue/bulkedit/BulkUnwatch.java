package com.atlassian.jira.web.action.issue.bulkedit;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bulkedit.BulkOperationManager;
import com.atlassian.jira.bulkedit.operation.BulkUnwatchOperation;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBeanSessionHelper;

public class BulkUnwatch extends AbstractBulkWatchOperationAction
{
    public BulkUnwatch(final TaskManager taskManager, SearchService searchService,
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
        return BulkUnwatchOperation.NAME_KEY;
    }

    @Override
    protected String getOperationName()
    {
        return BulkUnwatchOperation.NAME;
    }

    @Override
    protected String getCannotPerformErrorI18nKey()
    {
        return "bulk.unwatch.cannotperform.error";
    }

    @Override
    protected String getPerformErrorI18nKey()
    {
        return "bulk.unwatch.perform.error";
    }

    @Override
    protected String getWatchingDisabledErrorI18nKey()
    {
        return "bulk.unwatch.watching.disabled";
    }

    @Override
    protected String getProgressMessageI18nKey()
    {
        return "bulk.operation.progress.taskname.unwatch";
    }
}
