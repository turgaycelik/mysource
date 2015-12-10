package com.atlassian.jira.pageobjects.framework.util;

import com.atlassian.pageobjects.elements.PageElementFinder;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;

import static com.atlassian.jira.pageobjects.framework.util.JiraLocators.body;

/**
 * Removes the dirty warning. Dirty warning is difficult to test using WebDriver and generally contributes to a high rate
 * of broken builds. This class attempts to remove any kind of dirty form warning that we know of.
 *
 *
 * @since 5.1
 */
public class DirtyWarningTerminator
{

    @Inject
    private PageElementFinder finder;

    @Inject
    private WebDriver webDriver;

    /**
     * Eat that, dirty warning!
     *
     */
    public void htfuDirtyWarnings()
    {
        removeExistingEvil();
        preventTheEvil();
    }

    private void removeExistingEvil()
    {
        try
        {
            webDriver.switchTo().alert().dismiss();
        }
        catch (NoAlertPresentException iDontReallyCare)
        {
        }
    }

    private void preventTheEvil()
    {
        // get out of any IFrame
        webDriver.switchTo().defaultContent();
        // just make it WOOOORK
        finder.find(body()).javascript().execute("window.onbeforeunload=null;");
    }


}
