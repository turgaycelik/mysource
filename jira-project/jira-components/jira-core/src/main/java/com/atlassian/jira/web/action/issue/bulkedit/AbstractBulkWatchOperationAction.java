package com.atlassian.jira.web.action.issue.bulkedit;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bulkedit.BulkOperationManager;
import com.atlassian.jira.bulkedit.operation.ProgressAwareBulkOperation;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.BulkEditBeanSessionHelper;

public abstract class AbstractBulkWatchOperationAction extends AbstractBulkOperationDetailsAction
{
    private final ProgressAwareBulkOperation genericBulkWatchOperation;
    private final PermissionManager permissionManager;

    protected abstract String getOperationNameKey();

    protected abstract String getOperationName();

    protected abstract String getCannotPerformErrorI18nKey();

    protected abstract String getPerformErrorI18nKey();

    protected abstract String getWatchingDisabledErrorI18nKey();

    protected abstract String getProgressMessageI18nKey();

    public AbstractBulkWatchOperationAction(final SearchService searchService,
                                            final BulkOperationManager bulkOperationManager,
                                            final PermissionManager permissionManager,
                                            final BulkEditBeanSessionHelper bulkEditBeanSessionHelper,
                                            final TaskManager taskManager,
                                            final I18nHelper i18nHelper)
    {
        super(searchService, bulkEditBeanSessionHelper, taskManager, i18nHelper);
        this.permissionManager = permissionManager;
        this.genericBulkWatchOperation = bulkOperationManager.getProgressAwareOperation(getOperationNameKey());
    }

    public ProgressAwareBulkOperation getGenericBulkWatchOperation()
    {
        return genericBulkWatchOperation;
    }

    public boolean isHasAvailableActions() throws Exception
    {
        return getGenericBulkWatchOperation().canPerform(getBulkEditBean(), getLoggedInApplicationUser());
    }

    public String getOperationDetailsActionName()
    {
        return getGenericBulkWatchOperation().getOperationName() + "Details.jspa";
    }

    public void doPerformValidation()
    {
        try
        {
            // Ensure the user has the global BULK CHANGE permission
            if (!permissionManager.hasPermission(Permissions.BULK_CHANGE, getLoggedInApplicationUser()))
            {
                addErrorMessage(getText("bulk.change.no.permission",
                        String.valueOf(getBulkEditBean().getSelectedIssues().size())));
            }

            // If bulk watching has been disabled, genericBulkWatchOperation will be null, as the lookup from bulkOperationManager
            // won't find it as an available operation.
            if (getGenericBulkWatchOperation() == null)
            {
                addErrorMessage(getText(getWatchingDisabledErrorI18nKey()));
                return;
            }

            // Ensure the bulk watch/unwatch operation can be performed
            if (!getGenericBulkWatchOperation().canPerform(getBulkEditBean(), getLoggedInApplicationUser()))
            {
                addErrorMessage(getText(getCannotPerformErrorI18nKey(),
                        String.valueOf(getBulkEditBean().getSelectedIssues().size())));
            }
        }
        catch (Exception e)
        {
            log.error("Error occured while testing operation.", e);
            addErrorMessage(getText("bulk.canperform.error"));
        }
    }

    public String doDetails() throws Exception
    {
        if (getBulkEditBean() == null)
        {
            return redirectToStart();
        }

        return getRedirect(getOperationName() + "DetailsValidation.jspa");
    }

    public String doDetailsValidation() throws Exception
    {
        // Check that we have a BulkEditBean - i.e. the user got here by following the wizard - not by
        // clicking the "back" button of the browser (or something like that)

        // Note: if the user is accessing JIRA from a URL not identical to baseURL, the redirect will cause them to lose their session,
        // and getBulkEditBean() will return null here (JT)
        if (getBulkEditBean() == null)
        {
            // If we do not have BulkEditBean, send the user to the first step of the wizard
            return redirectToStart();
        }

        BulkEditBean bulkEditBean = getBulkEditBean();
        bulkEditBean.clearAvailablePreviousSteps();
        bulkEditBean.addAvailablePreviousStep(1);
        bulkEditBean.addAvailablePreviousStep(2);
        bulkEditBean.addAvailablePreviousStep(3);
        bulkEditBean.setCurrentStep(4);
        return INPUT;
    }

    @RequiresXsrfCheck
    public String doPerform() throws Exception
    {
        if (getBulkEditBean() == null)
        {
            return redirectToStart();
        }

        // Validate input
        doPerformValidation();
        if (invalidInput())
        {
            return ERROR;
        }

        final String taskName = getText(getProgressMessageI18nKey(),
                getRootBulkEditBean().getSelectedIssuesIncludingSubTasks().size());
        return submitBulkOperationTask(getBulkEditBean(), getGenericBulkWatchOperation(), taskName);
    }
}
