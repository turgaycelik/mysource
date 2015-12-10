package com.atlassian.jira.web.action.issue.bulkedit;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bulkedit.operation.BulkMigrateOperation;
import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.BulkEditBeanImpl;
import com.atlassian.jira.web.bean.BulkEditBeanSessionHelper;
import com.atlassian.jira.web.bean.MultiBulkMoveBean;
import com.atlassian.jira.workflow.WorkflowManager;

import java.util.List;
import java.util.Map;

public class BulkMigrate extends BulkMove
{
    private static final String SINGLE_ISSUE_MOVE_ERROR = "singleerror";
    private String sameAsBulkEditBean;
    private Long singleIssueId;

    protected final BulkMigrateOperation bulkMigrateOperation;

    public BulkMigrate(final SearchService searchService, final BulkMoveOperation bulkMoveOperation,
                       final FieldManager fieldManager, final WorkflowManager workflowManager,
                       final ConstantsManager constantsManager, final IssueFactory issueFactory,
                       final BulkMigrateOperation bulkMigrateOperation, final PermissionManager permissionManager,
                       final BulkEditBeanSessionHelper bulkEditBeanSessionHelper, final TaskManager taskManager,
                       final I18nHelper i18nHelper)
    {
        super(searchService, bulkMoveOperation, fieldManager, workflowManager, constantsManager, issueFactory,
                permissionManager, bulkEditBeanSessionHelper, taskManager, i18nHelper);
        this.bulkMigrateOperation = bulkMigrateOperation;
    }

    public String doDetails()
    {
        // check if this action is used to move just single issue
        // see SingleIssueModeEditBean interface
        if (singleIssueId != null)
        {
            clearBulkEditBean();
            Issue singleIssueObject = getIssueManager().getIssueObject(singleIssueId);
            if (singleIssueObject != null)
            {
                if (!isAbleToMoveSingleIssue(singleIssueObject, getLoggedInApplicationUser()))
                {
                    addErrorMessage(getText("move.issue.nopermissions"));
                    return SINGLE_ISSUE_MOVE_ERROR;
                }
                // now we need to properly set information because we've just jump into the
                // middle of bulk process (because we don't need to choose issues and operation)
                initSingleIssueBulkEditBean(singleIssueObject);
            }
            else
            {
                addErrorMessage(getText("admin.errors.issues.issue.does.not.exist"));
                return SINGLE_ISSUE_MOVE_ERROR;
            }
        }

        BulkEditBean rootBulkEditBean = getRootBulkEditBean();

        if (rootBulkEditBean == null)
        {
            return redirectToStart();
        }

        // Initialise
        rootBulkEditBean.setOperationName(getBulkMigrateOperation().getOperationName());
        rootBulkEditBean.resetMoveData();
        rootBulkEditBean.initMultiBulkBean();

        rootBulkEditBean.clearAvailablePreviousSteps();
        rootBulkEditBean.addAvailablePreviousStep(1);
        rootBulkEditBean.addAvailablePreviousStep(2);

        // Ensure that bulk notification can be disabled
        if (isCanDisableMailNotifications())
            rootBulkEditBean.setSendBulkNotification(false);
        else
            rootBulkEditBean.setSendBulkNotification(true);

        rootBulkEditBean.setCurrentStep(3);

        return SUCCESS;
    }

    private boolean isAbleToMoveSingleIssue(Issue singleIssueObject, ApplicationUser user)
    {
        if (!permissionManager.hasPermission(ProjectPermissions.MOVE_ISSUES, singleIssueObject, user))
        {
            return false;
        }
        return true;
    }

    private void initSingleIssueBulkEditBean(Issue singleIssueObject)
    {
        BulkEditBean bulkEditBean = new BulkEditBeanImpl(getIssueManager());
        SingleIssueModeInitializer.initialize(bulkEditBean, singleIssueObject);
        storeBulkEditBean(bulkEditBean);
    }

    public String doStart() throws Exception
    {
        return SUCCESS;
    }

    public String doChooseContext() throws Exception
    {
        final BulkEditBean currentRootBulkEditBean = getCurrentRootBulkEditBean();
        if (currentRootBulkEditBean == null) return redirectToStart();

        // Resets all bulk edit beans
        final MultiBulkMoveBean rootMultiBulkMoveBean = currentRootBulkEditBean.getRelatedMultiBulkMoveBean();
        final Map bulkEditBeans = rootMultiBulkMoveBean.getBulkEditBeans();
        for (final Object o1 : bulkEditBeans.values())
        {
            final BulkEditBean bulkEditBean = (BulkEditBean) o1;
            bulkEditBean.resetMoveData();
        }

        // Validate & commit context
        getBulkMigrateOperation().chooseContext(currentRootBulkEditBean, getLoggedInApplicationUser(), this, this);

        if (invalidInput())
        {
            return INPUT;
        }

        // Re-organise the MultiBulkEditBean into being keyed by destination contexts
        rootMultiBulkMoveBean.remapBulkEditBeansByTargetContext();
        // Find fields to manually edit, fields to remove, and find subtasks that need to be moved with their parent.
        getBulkMigrateOperation().getBulkMoveOperation()
                .finishChooseContext(rootMultiBulkMoveBean, getLoggedInApplicationUser());
        // Check if any subtasks need to be moved and initialise accordingly.
        boolean needsSubTaskChooseContext = false;
        for (final Object o : rootMultiBulkMoveBean.getBulkEditBeans().values())
        {
            final BulkEditBean targetContextBean = (BulkEditBean) o;
            // Set up all the sub task beans
            if (targetContextBean.getSubTaskBulkEditBean() != null)
            {
                needsSubTaskChooseContext = true;
                targetContextBean.initMultiBulkBeanWithSubTasks();
            }
        }

        // Choose sub task status
        if (needsSubTaskChooseContext)
        {
            return getRedirect(decorateRedirectUrl("BulkMigrateChooseSubTaskContext!default.jspa"));
        }
        else
        {
            return getNextRedirect();
        }
    }

    public String doChooseSubTaskContext() throws Exception
    {
        final BulkEditBean rootBulkEditBean = getRootBulkEditBean();
        if (rootBulkEditBean == null) return redirectToStart();

        // Resets all bulk edit beans
        for (final Object o1 : rootBulkEditBean.getRelatedMultiBulkMoveBean().getBulkEditBeans().values())
        {
            final BulkEditBean currentRootBulkEditBean = (BulkEditBean) o1;

            if (currentRootBulkEditBean.getRelatedMultiBulkMoveBean() != null)
            {
                final Map subTaskBeans = currentRootBulkEditBean.getRelatedMultiBulkMoveBean().getBulkEditBeans();
                for (final Object o : subTaskBeans.values())
                {
                    final BulkEditBean subTaskBean = (BulkEditBean) o;
                    subTaskBean.resetMoveData();
                }

                // Validate & commit context
                getBulkMigrateOperation()
                        .chooseContext(currentRootBulkEditBean, getLoggedInApplicationUser(), this, this);
            }
        }

        if (invalidInput())
        {
            return INPUT;
        }

        // Re-organise the MultiBulkEditBean into being keyed by destination contexts
        for (final Object o : rootBulkEditBean.getRelatedMultiBulkMoveBean().getBulkEditBeans().values())
        {
            final BulkEditBean currentRootBulkEditBean = (BulkEditBean) o;
            final MultiBulkMoveBean multiBulkMoveBean = currentRootBulkEditBean.getRelatedMultiBulkMoveBean();
            if (multiBulkMoveBean != null)
            {
                // Re-organise the MultiBulkEditBean into being keyed by destination contexts
                multiBulkMoveBean.remapBulkEditBeansByTargetContext();
                // Find fields to manually edit, and fields to remove.
                getBulkMigrateOperation().getBulkMoveOperation()
                        .finishChooseContext(multiBulkMoveBean, getLoggedInApplicationUser());
            }
        }

        // Check if status change is required for any issues
        return getNextRedirect();
    }


    public String doChooseStatus() throws Exception
    {
        if (getBulkEditBean() == null) return redirectToStart();

        getBulkMigrateOperation().setStatusFields(getCurrentRootBulkEditBean());
        return getRedirect(decorateRedirectUrl("BulkMigrateSetFields!default.jspa"));
    }

    public String doSetFields() throws Exception
    {
        if (getBulkEditBean() == null) return redirectToStart();

        getBulkMigrateOperation().validatePopulateFields(getCurrentRootBulkEditBean(), this, this);

        if (invalidInput())
        {
            return ERROR;
        }

        // If there's another layer of sub-tasking
        if (getBulkEditBean().getRelatedMultiBulkMoveBean() != null)
        {
            setSubTaskPhase(true);
            return getNextRedirect();
        }

        // If there's another bulk edit bean to migrate
        if (!getCurrentRootBulkEditBean().getRelatedMultiBulkMoveBean().isLastBulkEditBean())
        {
            getCurrentRootBulkEditBean().getRelatedMultiBulkMoveBean().progressToNextBulkEditBean();
            return getNextRedirect();

        }
        else
        {
            // It's the end of the road
            if (isSubTaskPhase())
            {
                setSubTaskPhase(false);
            }

            // Do it again for the parent
            if (!getCurrentRootBulkEditBean().getRelatedMultiBulkMoveBean().isLastBulkEditBean())
            {
                getCurrentRootBulkEditBean().getRelatedMultiBulkMoveBean().progressToNextBulkEditBean();

                return getNextRedirect();
            }
            else
            {
                // Progress to the final level
                progressToLastStep();
                return "confirm";
            }
        }
    }

    // Verify and perform the move operation
    public String doPerform() throws Exception
    {
        if (getBulkEditBean() == null)
        {
            return redirectToStart();
        }

        // Ensure the user has the global BULK CHANGE permission
        if (!permissionManager.hasPermission(Permissions.BULK_CHANGE, getLoggedInUser()) &&
                !getBulkEditBean().isSingleMode())
        {
            addErrorMessage(
                    getText("bulk.change.no.permission", String.valueOf(getBulkEditBean().getSelectedIssues().size())));
            return ERROR;
        }

        // Ensure the user can perform the operation
        if (!getBulkMigrateOperation().canPerform(getRootBulkEditBean(), getLoggedInApplicationUser()))
        {
            addErrorMessage(getText(BulkMoveOperation.CANNOT_PERFORM_MESSAGE_KEY));
            return ERROR;
        }

        // If this is a Bulk Move, then check that the issues have not been already moved in the meantime:
        final String movedIssueKey = findFirstMovedIssueKey();
        if (movedIssueKey != null)
        {
            addErrorMessage(getText("bulk.move.error.issue.already.moved", movedIssueKey));
            return ERROR;
        }

        final String taskName = getText("bulk.operation.progress.taskname.migrate",
                getRootBulkEditBean().getSelectedIssuesIncludingSubTasks().size());
        return submitBulkOperationTask(getRootBulkEditBean(), getBulkMigrateOperation(), taskName);
    }

    /**
     * Looks at the issues we are about to move/migrate, and if any have been moved in the meantime, then
     * we will return the key of the fist issue which has been moved.
     *
     * @return issue key of the first issue that has been moved.
     * or null if none have been moved.
     */
    private String findFirstMovedIssueKey()
    {
        //  Check whether the user has the move permission on all original selected issues
        final List<Issue> selectedIssues = getBulkEditBean().getSelectedIssues();
        for (final Issue selectedIssue : selectedIssues)
        {
            // Now get the latest value in the DB for this Issue.
            final MutableIssue latestIssue = getIssueManager().getIssueObject(selectedIssue.getId());
            if (!latestIssue.getKey().equals(selectedIssue.getKey()))
            {
                return selectedIssue.getKey();
            }
        }
        return null;
    }

    public BulkEditBean getBulkEditBean()
    {
        if (getCurrentRootBulkEditBean() != null)
        {
            return getCurrentRootBulkEditBean().getRelatedMultiBulkMoveBean().getCurrentBulkEditBean();
        }
        else
        {
            return null;
        }
    }

    public MultiBulkMoveBean getMultiBulkMoveBean()
    {
        return getCurrentRootBulkEditBean().getRelatedMultiBulkMoveBean();
    }

    public IssueContext getCurrentIssueContext()
    {
        return getCurrentRootBulkEditBean().getRelatedMultiBulkMoveBean().getCurrentIssueContext();
    }

    public BulkEditBean getCurrentRootBulkEditBean()
    {
        BulkEditBean currentBulkEditBean = null;
        if (!isSubTaskPhase())
        {
            currentBulkEditBean = getRootBulkEditBean();
        }
        else
        {
            if (getRootBulkEditBean() != null)
            {
                currentBulkEditBean = getRootBulkEditBean().getRelatedMultiBulkMoveBean().getCurrentBulkEditBean();
            }
        }
        return currentBulkEditBean;
    }

    public String getOperationDetailsActionName()
    {
        return getBulkMigrateOperation().getOperationName() + "Details.jspa";
    }

    public String getprojectFieldName(final BulkEditBean bulkEditBean)
    {
        return bulkEditBean.getKey() + "pid";
    }

    public String getSameAsBulkEditBean()
    {
        return sameAsBulkEditBean;
    }

    public void setSameAsBulkEditBean(final String sameAsBulkEditBean)
    {
        this.sameAsBulkEditBean = sameAsBulkEditBean;
    }

    protected BulkMigrateOperation getBulkMigrateOperation()
    {
        return bulkMigrateOperation;
    }

    public String getRedirectUrl() throws Exception
    {
        return decorateRedirectUrl(super.getRedirectUrl());
    }

    private String decorateRedirectUrl(String url)
    {
        return isSubTaskPhase() ? (url + "?subTaskPhase=" + isSubTaskPhase()) : url;
    }

    private String getNextRedirect()
    {
        if (!getBulkMigrateOperation().isStatusValid(getCurrentRootBulkEditBean()))
        {
            return getRedirect(decorateRedirectUrl("BulkMigrateChooseStatus!default.jspa"));
        }
        else
        {
            return getRedirect(decorateRedirectUrl("BulkMigrateSetFields!default.jspa"));
        }
    }

    /**
     * @see BulkEditBean#setSingleIssueKey(String)
     */
    public Long getSingleIssueId()
    {
        return singleIssueId;
    }

    /**
     * @see BulkEditBean#setSingleIssueKey(String)
     */
    public void setSingleIssueId(final Long singleIssueId)
    {
        this.singleIssueId = singleIssueId;
    }
}


