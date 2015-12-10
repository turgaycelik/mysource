package com.atlassian.jira.webtests.ztests.upgrade.tasks;


import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.LicenseKeys;

/**
 * @since v4.4
 */
@WebTest({ Category.FUNC_TEST, Category.UPGRADE_TASKS })
public class TestUpgradeTask641 extends FuncTestCase {

    public static final String ROWF_PROJECT_NAME = "readOnlyWorkflowTest";
    public static final String ROWF_PROJECT_KEY = "ROWFT";
    public static final String ROWF_ISSUE_KEY = "ROWFT-1";

    public void testUpgrade()
    {
        /*
            to truly test the upgrade, we have to start with a new instance.
            Unlike the other upgradetask funcTestHelperFactory tests, resotring data will void this test
         */
        setupJIRAFromScratch();

        createProjectWithOneIssue();

        navigation.issue().viewIssue(ROWF_ISSUE_KEY);
        tester.assertLinkPresentWithText("View Workflow");

        //now remove the permission and check we don't get that linke any longer
        administration.permissionSchemes().defaultScheme().removePermission(45, "10000");
        navigation.issue().viewIssue(ROWF_ISSUE_KEY);
        tester.assertLinkNotPresentWithText("View Workflow");
    }


    private void setupJIRAFromScratch() {
        administration.restoreNotSetupInstance();

        //step 2
        tester.gotoPage("secure/SetupApplicationProperties.jspa");
        tester.assertTextPresent("Set Up Application Properties");

        // Fill in mandatory fields
        tester.setWorkingForm("jira-setupwizard");
        tester.setFormElement("title", "My JIRA");
        // Submit Step 2 with Default paths.
        tester.submit();

        //skip choosing bundle
        tester.gotoPage("/secure/SetupLicense!default.jspa");

        //step 3
        tester.setWorkingForm("setupLicenseForm");
        tester.setFormElement("setupLicenseKey", LicenseKeys.V2_COMMERCIAL.getLicenseString());
        tester.submit();

        //step 4
        tester.assertTextPresent("Set Up Administrator Account");
        tester.setFormElement("username", ADMIN_USERNAME);
        tester.setFormElement("password", ADMIN_USERNAME);
        tester.setFormElement("confirm", ADMIN_USERNAME);
        tester.setFormElement("fullname", "Mary Magdelene");
        tester.setFormElement("email", "admin@example.com");
        tester.submit();
        tester.assertTextPresent("Set Up Email Notifications");

        //step 5
        log("Noemail");
        tester.submit("finish");
        log("Noemail");
        // During SetupComplete, the user is automatically logged in
        // Assert that the user is logged in by checking if the profile link is present
        tester.assertLinkPresent("header-details-user-fullname");
        navigation.disableWebSudo();
    }

    private void createProjectWithOneIssue() {
        administration.project().addProject(ROWF_PROJECT_NAME, ROWF_PROJECT_KEY, ADMIN_USERNAME);
        navigation.browseProject(ROWF_PROJECT_KEY);

        //create the issue
        navigation.issue().goToCreateIssueForm(ROWF_PROJECT_NAME,"Bug");

        tester.assertTextPresent("CreateIssueDetails.jspa");
        tester.setWorkingForm("issue-create");
        tester.setFormElement("summary", "Test Issue for ReadOnly Workflow");
        tester.setFormElement("reporter", ADMIN_USERNAME);

        tester.submit();
    }

}
