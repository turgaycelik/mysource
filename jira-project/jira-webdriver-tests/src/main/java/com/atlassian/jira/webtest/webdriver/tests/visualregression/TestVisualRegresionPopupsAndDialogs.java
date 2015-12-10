package com.atlassian.jira.webtest.webdriver.tests.visualregression;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.selenium.visualcomparison.utils.ScreenResolution;
import org.junit.Test;

/**
 * Visual regression tests for in-page dialogs and own-page popups. These screenshots are generally smaller than a normal page.
 *
 * @since v6.0
 */
@WebTest ( { Category.WEBDRIVER_TEST, Category.VISUAL_REGRESSION })
@Restore ("xml/TestVisualRegressionSmoke.zip")
public class TestVisualRegresionPopupsAndDialogs extends JiraVisualRegressionTest
{
    @Override
    public void resetVisualComparer()
    {
        super.resetVisualComparer();
        visualComparer.setScreenResolutions(new ScreenResolution[] { new ScreenResolution(600,900) });
    }

    @Test
    public void testUserPickerPopup()
    {
        goTo("/secure/popups/UserPickerBrowser.jspa?formName=jiraform&multiSelect=true&decorator=popup&element=customfield_10090");
        assertUIMatches("popup-user-picker");
    }

    @Test
    public void testFilterOrProjectPickerPopup()
    {
        goTo("/secure/FilterPickerPopup.jspa?showProjects=true&filterView=projects&field=filterId");
        assertUIMatches("popup-filter-project-picker");
    }

    @Test
    public void testFilterPickerPopupSearch()
    {
        goTo("/secure/FilterPickerPopup.jspa?filterView=search&field=filterId&showProjects=true&searchName=&searchOwnerUserName=&searchShareType=any&groupShare=jira-administrators&projectShare=10020&roleShare=&Search=Search");
        assertUIMatches("popup-filter-search");
    }

}
