package com.atlassian.jira.pageobjects.navigator;

import javax.inject.Inject;

import com.atlassian.jira.pageobjects.dialogs.ShifterDialog;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.webdriver.AtlassianWebDriver;

import org.openqa.selenium.By;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Results on issue navigator. Can be used for either simple or advanced mode.
 *
 * @since v4.4
 */
public class IssueNavigatorResults
{

    protected PageElement totalCount;
    protected PageElement issuetable;

    @Inject
    protected PageBinder binder;

    @Inject
    protected PageElementFinder finder;

    @Inject
    protected AtlassianWebDriver webDriver;

    @WaitUntil
    public void loaded()
    {
        waitUntilTrue(finder.find(By.className("navigator-content")).timed().isPresent());
        waitUntilFalse(finder.find(By.cssSelector("navigator-content > div")).timed().hasClass("pending"));
    }

    @Init
    public void getElements()
    {
        totalCount = finder.find(By.className("results-count-total"));
        issuetable = finder.find(By.id("issuetable"));
    }

    public int getTotalCount()
    {
        if (!totalCount.isPresent())
        {
            return 0;
        }
        else
        {
            return Integer.parseInt(totalCount.getText());
        }

    }

    public SelectedIssue getSelectedIssue()
    {
        return binder.bind(SelectedIssue.class);
    }

    public ShifterDialog openActionsDialog()
    {
        issuetable.type(".");
        final ShifterDialog theDialog = binder.bind(ShifterDialog.class);
        waitUntilTrue("Issue Actions Dialog Dialog did not open successfully", theDialog.isOpenTimed());
        return theDialog;
    }

    public ShifterDialog openActionsDialog(long issueId)
    {
        selectIssue(Long.toString(issueId));
        return openActionsDialog();
    }

    public IssueNavigatorResults focus()
    {
        issuetable.click();
        return this;
    }

    public IssueNavigatorResults nextIssue()
    {
        issuetable.type("j");
        return this;
    }

    public SelectedIssue selectIssue(String issueKey)
    {
        issuetable.find(By.cssSelector("tr[data-issuekey='" + issueKey + "']")).click();
        return getSelectedIssue();
    }
}
