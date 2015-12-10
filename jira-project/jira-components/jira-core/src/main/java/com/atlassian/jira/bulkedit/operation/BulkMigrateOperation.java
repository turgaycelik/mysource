package com.atlassian.jira.bulkedit.operation;

import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.MultiBulkMoveBean;
import com.atlassian.jira.workflow.WorkflowException;
import org.apache.log4j.Logger;

/**
 * Operation to Move issues from differing contexts to multiple target contexts.
 */
public class BulkMigrateOperation implements ProgressAwareBulkOperation
{
    public static final String OPERATION_NAME = "BulkMigrate";

    public static final String NAME_KEY = "bulk.move.operation.name";
    private static final String DESCRIPTION_KEY = "bulk.move.operation.description";

    private static final Logger log = Logger.getLogger(BulkMigrateOperation.class);

    private final BulkMoveOperation bulkMoveOperation;

    public BulkMigrateOperation(BulkMoveOperation bulkMoveOperation)
    {
        this.bulkMoveOperation = bulkMoveOperation;
    }

    @Override
    public boolean canPerform(final BulkEditBean bulkEditBean, final ApplicationUser remoteUser)
    {
        return bulkMoveOperation.canPerform(bulkEditBean, remoteUser);
    }

    @Override
    public void perform(final BulkEditBean rootBulkEditBean, final ApplicationUser applicationUser, Context taskContext)
            throws BulkOperationException
    {
        try
        {
            MultiBulkMoveBean multiBulkMoveBean = rootBulkEditBean.getRelatedMultiBulkMoveBean();
            for (final Object value : multiBulkMoveBean.getBulkEditBeans().values())
            {
                BulkEditBean bulkEditBean = (BulkEditBean) value;
                log.debug("Performing move for project " + bulkEditBean.getTargetProjectGV().getString("name") +
                        " issue type: " + bulkEditBean.getTargetIssueTypeGV().getString("name"));

                // This move changes the security level of the subtask, however the subtask move below, will overwrite this again
                // See  JRA-13937 - Bulk Move does not update the Security Level of subtasks for more details
                bulkMoveOperation.moveIssuesAndIndex(bulkEditBean, applicationUser, taskContext);

                MultiBulkMoveBean relatedMultiBulkMoveBean = bulkEditBean.getRelatedMultiBulkMoveBean();
                if (relatedMultiBulkMoveBean != null && relatedMultiBulkMoveBean.getBulkEditBeans() != null)
                {
                    for (final Object relatedValue : relatedMultiBulkMoveBean.getBulkEditBeans().values())
                    {
                        BulkEditBean subTaskBulkEditBean = (BulkEditBean) relatedValue;
                        log.info("subTaskBulkEditBean move for project " +
                                subTaskBulkEditBean.getTargetProjectGV().getString("name") + " issue type: " +
                                subTaskBulkEditBean.getTargetIssueTypeGV().getString("name"));
                        bulkMoveOperation.moveIssuesAndIndex(subTaskBulkEditBean, applicationUser, taskContext);
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new BulkOperationException(e);
        }
    }

    @Override
    public int getNumberOfTasks(final BulkEditBean rootBulkEditBean)
    {
        int count = 0;
        for (final Object o1 : rootBulkEditBean.getRelatedMultiBulkMoveBean().getBulkEditBeans().values())
        {
            BulkEditBean bulkEditBean = (BulkEditBean) o1;
            count += bulkEditBean.getSelectedIssues().size();
            MultiBulkMoveBean relatedMultiBulkMoveBean = bulkEditBean.getRelatedMultiBulkMoveBean();
            if (relatedMultiBulkMoveBean != null && relatedMultiBulkMoveBean.getBulkEditBeans() != null)
            {
                for (final Object o : relatedMultiBulkMoveBean.getBulkEditBeans().values())
                {
                    BulkEditBean subTaskBulkEditBean = (BulkEditBean) o;
                    count += subTaskBulkEditBean.getSelectedIssues().size();
                }
            }
        }
        return count;
    }

    public void chooseContext(BulkEditBean rootBulkEditBean, ApplicationUser applicationUser, I18nHelper i18nHelper,
                              ErrorCollection errors)
    {
        // Loop through the child BulkEditBeans and do a chooseContext() on each.
        for (final Object o : rootBulkEditBean.getRelatedMultiBulkMoveBean().getBulkEditBeans().values())
        {
            BulkEditBean bulkEditBean = (BulkEditBean) o;
            bulkMoveOperation.chooseContext(bulkEditBean, applicationUser, i18nHelper, errors);
        }
    }

    public void chooseContextNoValidate(BulkEditBean rootBulkEditBean, ApplicationUser applicationUser)
    {
        bulkMoveOperation
                .chooseContextNoValidate(rootBulkEditBean.getRelatedMultiBulkMoveBean().getCurrentBulkEditBean(),
                        applicationUser);
    }

    public boolean isStatusValid(BulkEditBean rootBulkEditBean)
    {
        return bulkMoveOperation.isStatusValid(rootBulkEditBean.getRelatedMultiBulkMoveBean().getCurrentBulkEditBean());
    }

    public void setStatusFields(BulkEditBean rootBulkEditBean) throws WorkflowException
    {
        bulkMoveOperation.setStatusFields(rootBulkEditBean.getRelatedMultiBulkMoveBean().getCurrentBulkEditBean());
    }

    public void validatePopulateFields(BulkEditBean rootBulkEditBean, I18nHelper i18nHelper, ErrorCollection errors)
    {
        bulkMoveOperation.validatePopulateFields(
                rootBulkEditBean.getRelatedMultiBulkMoveBean().getCurrentBulkEditBean(), errors, i18nHelper);
    }

    // -------------------------------------------------------------------------------------- Basic accessors & mutators
    @Override
    public String getNameKey()
    {
        return NAME_KEY;
    }

    @Override
    public String getDescriptionKey()
    {
        return DESCRIPTION_KEY;
    }

    @Override
    public String getOperationName()
    {
        return OPERATION_NAME;
    }

    @Override
    public String getCannotPerformMessageKey()
    {
        return BulkMoveOperation.CANNOT_PERFORM_MESSAGE_KEY;
    }

    public BulkMoveOperation getBulkMoveOperation()
    {
        return bulkMoveOperation;
    }
}
