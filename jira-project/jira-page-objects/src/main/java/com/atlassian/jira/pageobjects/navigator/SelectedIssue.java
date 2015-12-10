package com.atlassian.jira.pageobjects.navigator;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.atlassian.jira.pageobjects.components.menu.IssueActionsMenu;
import com.atlassian.jira.pageobjects.dialogs.IssueActionsUtil;
import com.atlassian.jira.pageobjects.dialogs.quickedit.EditIssueDialog;
import com.atlassian.jira.pageobjects.model.DefaultIssueActions;
import com.atlassian.jira.pageobjects.pages.viewissue.ActionTrigger;
import com.atlassian.jira.pageobjects.pages.viewissue.AssignIssueDialog;
import com.atlassian.jira.pageobjects.pages.viewissue.CommentType;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;

import org.openqa.selenium.By;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Selected Issue on the Issue Navigator
 *
 * @since v5.0
 */
public class SelectedIssue
{

    IssueActionsMenu actionsMenu;

    @ElementBy (cssSelector = ".issuerow.focused")
    private PageElement row;

    @Inject
    PageBinder binder;

    @Inject
    PageElementFinder pageElementFinder;

    @Inject
    IssueActionsUtil issueActionsUtil;

    @WaitUntil
    public void ready()
    {
        waitUntilTrue(row.timed().isPresent());
    }

    @Init
    public void init()
    {
        if (isAccessible())
        {
            actionsMenu = binder.bind(IssueActionsMenu.class, By.id("actions_" + getIssueId()), By.id("actions_" + getIssueId() + "_drop"));
        }
    }

    public String getIssueId()
    {
        return row.getAttribute("rel");
    }

    public String getAssignee()
    {
        return row.find(By.className("assignee")).getText().trim();
    }

    public String getStatus()
    {
        return row.find(By.className("status")).getText().trim();
    }

    public String getResolution()
    {
        return row.find(By.className("resolution")).getText().trim();
    }


    public void assignIssue(String user, ActionTrigger trigger)
    {
        prepareForAction(trigger);
        issueActionsUtil.assignIssue(user, trigger);
    }

    public void addLabels(List<String> labels, ActionTrigger trigger)
    {
        prepareForAction(trigger);
        issueActionsUtil.addLabels(labels, trigger);
    }

    public void addComment(String comment, ActionTrigger trigger)
    {
        prepareForAction(trigger);
        issueActionsUtil.addComment(comment, trigger, CommentType.DIALOG);
    }

    public void closeIssue(ActionTrigger trigger)
    {
        prepareForAction(trigger);
        issueActionsUtil.closeIssue(trigger);
    }

    public void resolveIssue(ActionTrigger trigger)
    {
        prepareForAction(trigger);
        issueActionsUtil.resolveIssue(trigger);
    }

    public void stopWatching(ActionTrigger trigger)
    {
        prepareForAction(trigger);
        issueActionsUtil.stopWatching(trigger);
    }

    public void startWatching(ActionTrigger trigger)
    {
        prepareForAction(trigger);
        issueActionsUtil.startWatching(trigger);
    }


    public void deleteIssue(ActionTrigger trigger)
    {
        prepareForAction(trigger);
        issueActionsUtil.delete(trigger);
    }

    public void prepareForAction(ActionTrigger trigger) {
        if (trigger == ActionTrigger.MENU)
        {
            getActionsMenu().open();
        }
    }

    public void stopProgress(ActionTrigger trigger)
    {
        prepareForAction(trigger);
        issueActionsUtil.stopProgress(trigger);
    }

    public void startProgress(ActionTrigger trigger)
    {
        prepareForAction(trigger);
        issueActionsUtil.startProgress(trigger);
    }

    public void editIssue(Map<String, String> values, ActionTrigger trigger)
    {
        prepareForAction(trigger);
        issueActionsUtil.editIssue(values, trigger);
    }


    public AssignIssueDialog assignViaShortcut()
    {
        row.type(DefaultIssueActions.ASSIGN_ISSUE.shortcut());
        return binder.bind(AssignIssueDialog.class);
    }

    public EditIssueDialog editViaShortcut()
    {
        row.type(DefaultIssueActions.EDIT_ISSUE.shortcut());
        return binder.bind(EditIssueDialog.class);
    }

    public IssueActionsMenu getActionsMenu()
    {
        return actionsMenu;
    }


    public String getSelectedIssueKey()
    {
        return pageElementFinder.find(By.id("issuerow" + getIssueId())).find(By.cssSelector(".issuekey")).getText();
    }

    public boolean isStaleIssue()
    {
        return row.hasClass("stale-issue");
    }

    public boolean isInView()
    {
        return (Boolean)row.javascript().execute("return jQuery(arguments[0]).isInView()", row);
    }

    public boolean isAccessible()
    {
        return !row.hasClass("inaccessible-issue");
    }

}
