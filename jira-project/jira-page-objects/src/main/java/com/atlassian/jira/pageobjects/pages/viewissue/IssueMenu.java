package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.jira.pageobjects.components.JiraAjsDropdown;
import com.atlassian.jira.pageobjects.dialogs.quickedit.WorkflowTransitionDialog;
import com.atlassian.jira.pageobjects.model.AdminAction;
import com.atlassian.jira.pageobjects.model.IssueOperation;
import com.atlassian.jira.pageobjects.model.WorkflowIssueAction;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;

import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Issue menu on the View Issue page.
 *
 * @since v4.4
 */
public class IssueMenu
{
    private static final String MORE_ACTIONS_LOCATOR = "opsbar-operations_more";
    private static final String MORE_WORKFLOWS_LOCATOR = "opsbar-transitions_more";
    private static final String MORE_ADMIN_LOCATOR = "opsbar-admin_more";

    private JiraAjsDropdown moreActions;
    private JiraAjsDropdown moreWorkflows;
    private JiraAjsDropdown moreAdmin;

    @Inject
    private PageBinder pageBinder;

    @Inject
    private PageElementFinder locator;

    private final ViewIssuePage viewIssuePage;


    public IssueMenu(ViewIssuePage viewIssuePage)
    {
        this.viewIssuePage = notNull(viewIssuePage);
    }

    @Init
    public void init()
    {
        moreActions = pageBinder.bind(JiraAjsDropdown.class, MORE_ACTIONS_LOCATOR);
        moreAdmin = pageBinder.bind(JiraAjsDropdown.class, MORE_ADMIN_LOCATOR);
        moreWorkflows = pageBinder.bind(JiraAjsDropdown.class, MORE_WORKFLOWS_LOCATOR);
    }

    /**
     * Invoke given <tt>issueOperation</tt>.
     *
     * @param issueOperation issue operation to invoke
     * @return the view issue page
     */
    public ViewIssuePage invoke(IssueOperation issueOperation)
    {
        final PageElement topLevelIssueOperation = locator.find(By.cssSelector("li #" + issueOperation.id()));
        if (topLevelIssueOperation.isVisible())
        {
            topLevelIssueOperation.click();
        }
        else
        {
            invokeFromMores(issueOperation);
        }
        return viewIssuePage;
    }

    /**
     * Invoke given <tt>issueOperation</tt> and return the target page object.
     *
     * @param issueOperation issue operation to invoke
     * @param <T> target page objects type
     * @return instance of the target page object
     */
    public <T> T invoke(IssueOperation issueOperation, Class<T> targetClass, Object... args)
    {
        invoke(issueOperation);
        return pageBinder.bind(targetClass, args);
    }

    public WorkflowTransitionDialog invokeWorkflowAction(WorkflowIssueAction action)
    {
        return invoke(action, WorkflowTransitionDialog.class, Long.toString(action.workflowActionId()));
    }

    private void invokeFromMores(IssueOperation issueOp)
    {
        final By selector = By.id(issueOp.id());
        if (moreActions.hasItemBy(selector))
        {
            moreActions.openAndClick(selector);
        }
        else
        {
            moreWorkflows.openAndClick(selector);
        }
    }

    public <T> T invokeAdmin(AdminAction<T> item, Object...args)
    {
        final By selector = By.id(item.id());
        return moreAdmin.openAndClick(selector, item.getPageClass(), args);
    }

    public Boolean isMoreActionsOpened()
    {
        return moreActions.isOpen();
    }

    public void openMoreActions()
    {
        moreActions.open();
    }

    public boolean isItemPresentInMoreActionsMenu(final String name)
    {
        return moreActions.hasItemBy(By.linkText(name));
    }
}
