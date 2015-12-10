package com.atlassian.jira.webtests.ztests.timetracking.modern;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Tests for the WorklogSystemField.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.TIME_TRACKING })
public class TestWorklogSystemField extends FuncTestCase
{
    private static final String COMMENT_PREVIEW_ICON = "comment-preview_link";

    public void testFieldConfiguration() throws Exception
    {
        administration.restoreBlankInstance();

        // by default Time Tracking is disabled therefore can't configure the field
        administration.fieldConfigurations().defaultFieldConfiguration();

        text.assertTextNotPresent("Log Work");

        // enable time tracking
        administration.timeTracking().enable(TimeTracking.Mode.MODERN);

        // assert that we can now configure the field
        administration.fieldConfigurations().defaultFieldConfiguration();
        text.assertTextPresent("Log Work");
    }

    public void testXssForLogWorkInputs() throws Exception
    {
        administration.restoreData("TestLogWorkAsField.xml");

        // goto edit screen
        navigation.issue().gotoEditIssue("HSP-1");

        // enter invalid inputs
        tester.setFormElement("worklog_timeLogged", "<em>TIMELOGGED</em>");
        tester.setFormElement("worklog_startDate", "<em>STARTDATE</em>");
        tester.checkCheckbox("worklog_adjustEstimate", "new");
        tester.setFormElement("worklog_newEstimate", "<em>NEWESTIMATE</em>");

        tester.submit();
        
        // assert values came back HTML escaped
        tester.assertTextPresent("&lt;em&gt;TIMELOGGED&lt;/em&gt;");
        tester.assertTextNotPresent("<em>TIMELOGGED</em>");
        tester.assertTextPresent("&lt;em&gt;STARTDATE&lt;/em&gt;");
        tester.assertTextNotPresent("<em>STARTDATE</em>");
        tester.assertTextPresent("&lt;em&gt;NEWESTIMATE&lt;/em&gt;");
        tester.assertTextNotPresent("<em>NEWESTIMATE</em>");
        
        // enter invalid input for adjustment amount
        tester.checkCheckbox("worklog_adjustEstimate", "manual");
        tester.setFormElement("worklog_adjustmentAmount", "<em>ADJUSTMENTAMOUNT</em>");

        tester.submit();

        // assert values came back HTML escaped
        tester.assertTextPresent("&lt;em&gt;ADJUSTMENTAMOUNT&lt;/em&gt;");
        tester.assertTextNotPresent("<em>ADJUSTMENTAMOUNT</em>");
    }

    public void testConfigureRendererForField() throws Exception
    {
        administration.restoreData("TestLogWorkAsField.xml");
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(40, "jira-developers");

        // Assert the default text renderer
        administration.fieldConfigurations().defaultFieldConfiguration();
        assertEquals("Default Text Renderer", administration.fieldConfigurations().defaultFieldConfiguration().getRenderer("Log Work"));

        // assert the preview icon is not present on Create Issue
        navigation.issue().goToCreateIssueForm("homosapien", "Bug");
        previewIconShouldNotBePresent();
        createIssueWithWorkLogged();

        // assert the preview icon is not present on the Edit Work Log form
        navigateToEditWorkLog();
        previewIconShouldNotBePresent();

        // assert the preview icon is not present on Log Work form
        navigateToLogWorkForm();
        previewIconShouldNotBePresent();
        logWorkOnCurrentIssue();

        // assert text rendering of Work Logs
        shouldRenderAsTextWithDefaultRenderer();

        // change the renderer to Wiki Style Renderer
        administration.fieldConfigurations().defaultFieldConfiguration().setRenderer("Log Work", "Wiki Style Renderer");
        assertEquals("Wiki Style Renderer", administration.fieldConfigurations().defaultFieldConfiguration().getRenderer("Log Work"));

        // assert preview icon is now present
        navigateToEditWorkLog();
        previewIconShouldBePresent();
        navigation.issue().goToCreateIssueForm("homosapien", "Bug");
        previewIconShouldBePresent();
        navigateToLogWorkForm();
        previewIconShouldBePresent();

        // assert wiki rendering of Work Logs
        shouldRenderAsWikiWithWikiRenderer();
    }

    private void navigateToEditWorkLog()
    {
        navigation.issue().gotoIssueWorkLog("HSP-2");
        tester.clickLink("edit_worklog_10000");
    }

    private void shouldRenderAsWikiWithWikiRenderer()
    {
        navigation.issue().gotoIssueWorkLog("HSP-2");
        tester.assertTextNotPresent("*Should be bold*");
        tester.assertTextPresent("<b>Should be bold</b>");
        tester.assertTextNotPresent("_Should be italics_");
        tester.assertTextPresent("<em>Should be italics</em>");
    }

    private void shouldRenderAsTextWithDefaultRenderer()
    {
        navigation.issue().gotoIssueWorkLog("HSP-2");
        tester.assertTextPresent("*Should be bold*");
        tester.assertTextNotPresent("<b>Should be bold</b>");
        tester.assertTextPresent("_Should be italics_");
        tester.assertTextNotPresent("<em>Should be italics</em>");
    }

    private void createIssueWithWorkLogged()
    {
        tester.setFormElement("summary", "issue summary");
        tester.setFormElement("worklog_timeLogged", "1h");
        tester.setFormElement("comment", "*Should be bold*");
        tester.submit();
    }

    private void logWorkOnCurrentIssue()
    {
        tester.setFormElement("timeLogged", "2h");
        tester.setFormElement("comment", "_Should be italics_");
        tester.submit();
    }

    private void navigateToLogWorkForm()
    {
        navigation.issue().gotoIssue("HSP-2");
        tester.clickLink("log-work");
    }

    private void previewIconShouldBePresent()
    {
        tester.assertElementPresent(COMMENT_PREVIEW_ICON);
    }

    private void previewIconShouldNotBePresent()
    {
        tester.assertElementNotPresent(COMMENT_PREVIEW_ICON);
    }
}
