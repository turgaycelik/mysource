package com.atlassian.jira.webtest.webdriver.tests.admin.applicationproperties;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.config.LoginAs;
import com.atlassian.jira.pageobjects.pages.admin.EditApplicationPropertiesPage;
import com.atlassian.jira.pageobjects.pages.admin.configuration.ViewGeneralConfigurationPage;
import com.atlassian.jira.pageobjects.pages.admin.configuration.ViewGeneralConfigurationPage.OptionRow;
import com.google.common.base.Optional;
import org.junit.AfterClass;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@WebTest(com.atlassian.jira.functest.framework.suite.Category.WEBDRIVER_TEST)
@RestoreOnce("xml/TestAdvancedApplicationProperties.xml")
public class TestJiraAllowUnassignedIssues extends BaseJiraWebTest
{
    private static final String OFF = "OFF";
    private static final String ON = "ON";

    @AfterClass
    public static void disableAllowUnassigned()
    {
        // set back to default
        jira.quickLogin(jira.getAdminCredentials().getUsername(), jira.getAdminCredentials().getPassword());
        setAllowUnassigned(false, pageBinder.navigateToAndBind(EditApplicationPropertiesPage.class));
    }

    @Test
    @LoginAs(admin = true, targetPage = EditApplicationPropertiesPage.class)
    public void testAllowUnassignedIssuesDisplayedWhenOn()
    {
        verifyAllowUnassignedIssueValueIsDisplayed(
                setAllowUnassigned(true, pageBinder.bind(EditApplicationPropertiesPage.class)), true);
    }

    @Test
    @LoginAs(admin = true, targetPage = EditApplicationPropertiesPage.class)
    public void testAllowUnassignedIssuesDisplayedWhenOff()
    {
        verifyAllowUnassignedIssueValueIsDisplayed(
                setAllowUnassigned(false, pageBinder.bind(EditApplicationPropertiesPage.class)), false);
    }

    private static ViewGeneralConfigurationPage setAllowUnassigned(final boolean state,
            final EditApplicationPropertiesPage editPropsPage)
    {
        editPropsPage.setAllowUnassigned(state);
        return editPropsPage.submitAndBind();
    }

    private void verifyAllowUnassignedIssueValueIsDisplayed(final ViewGeneralConfigurationPage viewPropsPage,
            final boolean optionOn)
    {
        final Optional<OptionRow> option = viewPropsPage.getOption("Allow unassigned issues");

        assertTrue(option.isPresent());
        assertThat(option.get().getValue(), containsString(optionOn ? ON : OFF));
    }

}
