package com.atlassian.jira.bulkedit;

import com.atlassian.jira.bulkedit.operation.BulkOperation;
import com.atlassian.jira.bulkedit.operation.BulkOperationException;
import com.atlassian.jira.bulkedit.operation.ProgressAwareBulkOperation;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.BulkEditBean;

import javax.annotation.Nonnull;

/**
 * Wraps a {@link com.atlassian.jira.bulkedit.operation.BulkOperation} into a
 * {@link com.atlassian.jira.bulkedit.operation.ProgressAwareBulkOperation}, ignoring task progress aspects.
 */
class ProgressAwareBulkOperationWrapper implements ProgressAwareBulkOperation
{
    private final BulkOperation legacyBulkOperation;

    ProgressAwareBulkOperationWrapper(@Nonnull BulkOperation legacyBulkOperation)
    {
        this.legacyBulkOperation = legacyBulkOperation;
    }

    BulkOperation getLegacyBulkOperation()
    {
        return legacyBulkOperation;
    }

    @Override
    public String getOperationName()
    {
        return legacyBulkOperation.getOperationName();
    }

    @Override
    public String getCannotPerformMessageKey()
    {
        return legacyBulkOperation.getCannotPerformMessageKey();
    }

    @Override
    public boolean canPerform(BulkEditBean bulkEditBean, ApplicationUser remoteUser)
    {
        return legacyBulkOperation.canPerform(bulkEditBean, remoteUser.getDirectoryUser());
    }

    @Override
    public void perform(BulkEditBean bulkEditBean, ApplicationUser remoteUser, Context taskContext)
            throws BulkOperationException
    {
        try
        {
            legacyBulkOperation.perform(bulkEditBean, remoteUser.getDirectoryUser());
        }
        catch (Exception e)
        {
            throw new BulkOperationException(e);
        }
    }

    @Override
    public int getNumberOfTasks(BulkEditBean bulkEditBean)
    {
        return 0;
    }

    @Override
    public String getNameKey()
    {
        return legacyBulkOperation.getNameKey();
    }

    @Override
    public String getDescriptionKey()
    {
        return legacyBulkOperation.getDescriptionKey();
    }
}
