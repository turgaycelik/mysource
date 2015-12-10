package com.atlassian.jira.webtests.ztests.bundledplugins2;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@WebTest( { Category.FUNC_TEST, Category.ISSUES })
public class TestIssueTabPanels extends FuncTestCase
{

    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestIssueTabPanels.xml");
    }

    /**
     * Check that the Activity Stream tab is not sortable (JRA-17973)
     */
    public void testActivityStreamIsNotSortable()
    {
        tester.gotoPage("browse/MKY-1?page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel");
        // Assert that the Activity Stream is not Active
        tester.assertTextNotPresent("<li id=\"activity-stream-issue-tab\" class=\"active\"><strong>Activity</strong></li>");
        text.assertTextPresent(new WebPageLocator(tester), "Ascending order");

        tester.gotoPage("browse/MKY-1?page=com.atlassian.streams.streams-jira-plugin%3Aactivity-stream-issue-tab");
        // Assert that the Activity Stream is Active
        assertThat(locator.css("li#activity-stream-issue-tab.active").getText(), equalTo("Activity"));
        assertThat(locator.css("iframe#gadget-0").getNode(), not(equalTo(null)));
        text.assertTextNotPresent(new WebPageLocator(tester), "Ascending order");
    }
}
