package com.atlassian.jira.webtests.ztests.remote;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.backdoor.TestRunnerControl;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * This is the "client" of a back end "integration" test. It sets up JIRA using Func Test framework helpers, and
 * then invokes a server-side backdoor to run a test based on that data.
 *
 * The test on the back end should have a corresponding name to this test i.e. TestIssueServiceBackEnd.
 *
 * @since v5.0.1
 * @author mtokar
 */
@WebTest ({ Category.FUNC_TEST, Category.API })
public class TestIssueService extends FuncTestCase
{
    private static final String BACK_END_TEST_NAME = "com.atlassian.jira.dev.functest.api.bc.issue." + TestIssueService.class.getSimpleName() + "BackEnd";

    @Override
    protected void setUpTest()
    {
        super.setUpTest();

        // set up the data the way we need it
        administration.restoreData("EmptyJira.xml");

        // we need one project with the Environment field hidden on the Edit Screen, and one with it shown, and one project with a required field.
        backdoor.project().addProject("WithoutField", "WITHOUT", "admin");
        backdoor.project().addProject("WithField", "WITH", "admin");
        backdoor.project().addProject("WithRequiredField", "WITHREQ", "admin");
        backdoor.project().addProject("Transitions metadata", "METATRANS", "admin");
        backdoor.project().addProject("Update metadata", "METAUPDATE", "admin");

        // create one issue in each project
        backdoor.issues().createIssue("WITHOUT", "The first issue in WITHOUT");
        backdoor.issues().createIssue("WITH", "The first issue in WITH");
        backdoor.issues().createIssue("WITHREQ", "The first issue in WITHREQ");
        backdoor.issues().createIssue("METATRANS", "Transition metadata test issue");
        backdoor.issues().createIssue("METAUPDATE", "Update metadata test issue");

        // copy the Default Field Configuration for hiding environment
        backdoor.fieldConfiguration().copyFieldConfiguration("Default Field Configuration", "");
        // hide the Environment field in that copy
        administration.fieldConfigurations().fieldConfiguration("Copy of Default Field Configuration").hideFields("Environment");
        // create a Field Configuration Scheme for the new Field Config
        administration.fieldConfigurationSchemes().addFieldConfigurationScheme("Without Environment Scheme", "For test");
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("Without Environment Scheme").editAssociation(null, "Copy of Default Field Configuration");
        // associate new scheme to Without project
        administration.project().associateFieldConfigurationScheme("WithoutField", "Without Environment Scheme");

        // copy the Default Field Configuration for hiding environment
        backdoor.fieldConfiguration().copyFieldConfiguration("Default Field Configuration", "Required Field Config");
        // make Component required in this config
        administration.fieldConfigurations().fieldConfiguration("Required Field Config").requireField("Component/s");
        // create a Field Configuration Scheme for the new Field Config
        administration.fieldConfigurationSchemes().addFieldConfigurationScheme("With Required Scheme", "For test");
        administration.fieldConfigurationSchemes().fieldConfigurationScheme("With Required Scheme").editAssociation(null, "Required Field Config");
        // associate new scheme to With Required project
        administration.project().associateFieldConfigurationScheme("WithRequiredField", "With Required Scheme");
    }

    public void testBackEnd() throws Exception
    {
        TestRunnerControl.TestResult response = backdoor.testRunner().getRunTests(BACK_END_TEST_NAME);
        if (!response.passed)
        {
            fail(response.message);
        }
    }
}
