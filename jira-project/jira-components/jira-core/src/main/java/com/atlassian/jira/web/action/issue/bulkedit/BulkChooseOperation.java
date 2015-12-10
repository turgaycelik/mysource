package com.atlassian.jira.web.action.issue.bulkedit;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bulkedit.BulkOperationManager;
import com.atlassian.jira.bulkedit.operation.BulkDeleteOperation;
import com.atlassian.jira.bulkedit.operation.BulkEditOperation;
import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.bulkedit.operation.BulkUnwatchOperation;
import com.atlassian.jira.bulkedit.operation.BulkWatchOperation;
import com.atlassian.jira.bulkedit.operation.BulkWorkflowTransitionOperation;
import com.atlassian.jira.bulkedit.operation.ProgressAwareBulkOperation;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.bean.BulkEditBeanSessionHelper;
import com.opensymphony.util.TextUtils;

import java.util.Arrays;
import java.util.Collection;

/**
 * This action is used to present the user with a list of allowed bulk operations
 * on the selected issues
 */
public class BulkChooseOperation extends AbstractBulkOperationAction
{
    private Collection<ProgressAwareBulkOperation> bulkOperations;
    private String operation;
    final BulkOperationManager bulkOperationManager;

    public BulkChooseOperation(final SearchService searchService, final BulkOperationManager bulkOperationManager,
                               final BulkEditBeanSessionHelper bulkEditBeanSessionHelper)
    {
        super(searchService, bulkEditBeanSessionHelper);
        this.bulkOperationManager = bulkOperationManager;
        bulkOperations = bulkOperationManager.getProgressAwareBulkOperations();
    }

    public String doDefault() throws Exception
    {
        if (getBulkEditBean() == null)
        {
            return redirectToStart();
        }
        getBulkEditBean().addAvailablePreviousStep(1);
        getBulkEditBean().setCurrentStep(2);
        return getResult();
    }

    protected void doValidation()
    {
        if (!TextUtils.stringSet(getOperation()))
        {
            addErrorMessage(getText("bulk.chooseoperation.chooseoperation.error"));
        }
        else
        {
            // Check if the operation exists
            if (!bulkOperationManager.isValidOperation(getOperation()))
            {
                addErrorMessage(getText("bulk.chosseoperation.invalid.operation"));
            }

            // If a bulk move, make sure that more than just sub-tasks were selected
            // TODO: this doesn't seem to work and is apparently not correct anyway, as we can "move" subtask issuetypes.
            if (BulkMoveOperation.NAME.equals(getOperation()) && getBulkEditBean() != null &&
                    getBulkEditBean().isOnlyContainsSubTasks())
            {
                addErrorMessage(getText("admin.errors.issuebulkedit.cannot.move.sub.tasks"));
            }
        }

        super.doValidation();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        String operationName = bulkOperationManager.getProgressAwareOperation(this.getOperation()).getOperationName();
        if (getBulkEditBean() != null)
        {
            if (operationName != null &&
                    Arrays.asList(BulkMoveOperation.NAME, BulkWorkflowTransitionOperation.NAME, BulkEditOperation.NAME,
                            BulkDeleteOperation.NAME, BulkWatchOperation.NAME, BulkUnwatchOperation.NAME)
                            .contains(operationName))
            {
                getBulkEditBean().setOperationName(operationName);
            }
        }
        else
        {
            return redirectToStart();
        }

        return getRedirect(operationName + "Details.jspa");
    }

    public Collection<ProgressAwareBulkOperation> getBulkOperations()
    {
        return bulkOperations;
    }

    public boolean isCanPerform(ProgressAwareBulkOperation bulkOperation) throws Exception
    {
        return bulkOperation.canPerform(getBulkEditBean(), getLoggedInApplicationUser());
    }

    public boolean isHasAvailableOperations() throws Exception
    {
        for (ProgressAwareBulkOperation bulkOperation : bulkOperations)
        {
            if (isCanPerform(bulkOperation))
            {
                return true;
            }
        }
        return false;
    }

    public String getOperation()
    {
        return operation;
    }

    public void setOperation(String operation)
    {
        this.operation = operation;
    }
}
