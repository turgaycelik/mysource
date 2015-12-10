package com.atlassian.jira.webtest.webdriver.tests.dialog;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.pages.admin.fields.configuration.schemes.ViewFieldConfigurationSchemesPage;
import com.atlassian.jira.pageobjects.pages.admin.fields.configuration.schemes.configure.AddIssueTypeToFieldConfigurationDialog;
import com.atlassian.jira.pageobjects.pages.admin.fields.configuration.schemes.configure.ConfigureFieldConfigurationSchemePage;
import com.atlassian.jira.pageobjects.pages.admin.ViewProjectsPage;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @since v5.0
 */
@WebTest ( { Category.WEBDRIVER_TEST, Category.ADMINISTRATION })
public class TestDialog extends BaseJiraWebTest
{
    //JRA-27502: Dialogs that redirect to a new page were not being recorded in the browsers history.
    @Test
    public void testBrowseBackButton()
    {
        final String fcsName = "New testBrowseBackButton FCS";
        final String issueTypeBug = "Bug";
        final String defaultFc = "Default Field Configuration";

        backdoor.restoreBlankInstance();

        //Add the admin summary page to be browsers history.
        jira.quickLoginAsSysadmin(ViewProjectsPage.class);

        final ViewFieldConfigurationSchemesPage viewPage = jira.visit(ViewFieldConfigurationSchemesPage.class);
        ConfigureFieldConfigurationSchemePage configPage = viewPage.openAddFieldConfigurationSchemeDialog()
                .setName(fcsName).submitSuccess();

        final AddIssueTypeToFieldConfigurationDialog associateDialog = configPage.openAddIssueTypeToFieldConfigurationDialog();
        configPage = associateDialog.setIssueType(issueTypeBug).setFieldConfiguration(defaultFc)
                .submitSuccess();
        assertThat(configPage.getName(), equalTo(fcsName));

        final WebDriver.Navigation navigation = jira.getTester().getDriver().navigate();

        navigation.back();
        //We should not be on the ViewFieldConfigurationSchemesPage. Binding checks that we are there.
        pageBinder.bind(ViewFieldConfigurationSchemesPage.class);

        navigation.back();
        //Expect to be at the summary page. Binding checks that we are there.
        pageBinder.bind(ViewProjectsPage.class);
    }

    private static ViewFieldConfigurationSchemesPage.FieldConfigurationSchemeItem fcs(final String name)
    {
        return new ViewFieldConfigurationSchemesPage.FieldConfigurationSchemeItem(name, null);
    }
}
