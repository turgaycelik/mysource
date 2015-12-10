package com.atlassian.jira.webtests.ztests.admin.security.xsrf;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfTestSuite;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.1
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.SECURITY })
public class TestXsrfProject extends FuncTestCase
{
    private static final String SUBMIT_ASSOCIATE = "Associate";
    private static final String SUBMIT_SELECT = "Select";

    protected void setUpTest()
    {
        administration.restoreData("TestXsrfProject.xml");
    }

    public void testProjectAdministration() throws Exception
    {
        new XsrfTestSuite(
            new XsrfCheck("DeleteProject", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    tester.gotoPage("/secure/admin/DeleteProject!default.jspa?pid=10000");
                }
            }, new XsrfCheck.FormSubmission("Delete")),
            new XsrfCheck("AddProject", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    navigation.gotoAdminSection("view_projects");
                    tester.clickLink("add_project");
                    tester.setFormElement("name", "Test Project");
                    tester.setFormElement("key", "TST");
                    tester.setFormElement("lead", ADMIN_USERNAME);
                }
            }, new XsrfCheck.FormSubmission("Add")),
            new XsrfCheck("EditProject", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    tester.gotoPage("/secure/project/EditProject!default.jspa?pid=10001");
                    tester.setFormElement("description", "PROJECT FOR MONKEYS");
                }
            }, new XsrfCheck.FormSubmission("Update")),

            new XsrfCheck("EditProjectEmail", new XsrfCheck.Setup()
            {
                public void setup()
                {
                    tester.gotoPage("/secure/project/ProjectEmail!default.jspa?projectId=10001");
                    tester.setFormElement("fromAddress", "mailserver@mailserver");
                }
            }, new XsrfCheck.FormSubmission("update")),

            new ProjectAssociationXsrfCheck("SelectIssueTypeScheme",
                    "secure/admin/SelectIssueTypeSchemeForProject!default.jspa?projectId=10001",
                    "schemeId", "Copy of Default Issue Type Scheme",
                    null, " OK "),

            new ProjectAssociationXsrfCheck("SelectNotificationScheme",
                    "/secure/project/SelectProjectScheme!default.jspa?projectId=10001",
                    "schemeIds", "Default Notification Scheme",
                    null, SUBMIT_ASSOCIATE),

            new ProjectAssociationXsrfCheck("SelectPermissionScheme",
                    "/secure/project/SelectProjectPermissionScheme!default.jspa?projectId=10001",
                    "schemeIds", "Copy of Default Permission Scheme",
                    null, SUBMIT_ASSOCIATE),

            new ProjectAssociationXsrfCheck("SelectIssueSecurityScheme",
                    "/secure/project/SelectProjectIssueSecurityScheme!default.jspa?projectId=10001",
                    "newSchemeId", "Test Scheme",
                    "Next >>", SUBMIT_ASSOCIATE),

            new ProjectAssociationXsrfCheck("SelectFieldConfigurationScheme",
                    "/secure/admin/SelectFieldLayoutScheme!default.jspa?projectId=10001",
                    "schemeId", "New Field Config Scheme",
                    null, SUBMIT_ASSOCIATE),
            new ProjectAssociationXsrfCheck("SelectIssueTypeScreenScheme",
                    "/secure/project/SelectIssueTypeScreenScheme!default.jspa?projectId=10001",
                    "schemeId", "Copy of Default Issue Type Screen Scheme",
                    null, SUBMIT_ASSOCIATE),

            new ProjectAssociationXsrfCheck("EditWorkflowScheme",
                    "/secure/project/SelectProjectWorkflowScheme!default.jspa?projectId=10001",
                    "schemeId", "New Workflow Scheme",
                    SUBMIT_ASSOCIATE, SUBMIT_ASSOCIATE),

            new ProjectAssociationXsrfCheck("SelectProjectCategory",
                    "/secure/project/SelectProjectCategory!default.jspa?pid=10001",
                    "pcid", "Category One",
                    null, SUBMIT_SELECT)
        ).run(funcTestHelperFactory);
    }


    /**
     * A Project Association Xsrf Check involves setting up the client to get to the association mutative action with a new
     * value selected and then submitting the form.
     */
    class ProjectAssociationXsrfCheck extends XsrfCheck
    {
        public ProjectAssociationXsrfCheck(String description, String uri, String inputName,
                String inputValue, String optionalStepSubmit, String formSubmit)
        {
            super(description,
                    new ProjectAssociationSetup(uri, inputName, inputValue, optionalStepSubmit),
                    new FormSubmission(formSubmit));
        }
    }

    /**
     * All the Project Association Setups involve:
     * - Navigating to the provided uri
     * - Setting a new value
     * - Optionally submitting the form to the final step before action is done
     */
    class ProjectAssociationSetup implements XsrfCheck.Setup
    {
        private final String uri;
        private final String inputName;
        private final String inputValue;
        private final String optionalStepSubmit;

        ProjectAssociationSetup(final String uri, final String inputName, final String inputValue, final String optionalStepSubmit)
        {
            this.uri = uri;
            this.inputName = inputName;
            this.inputValue = inputValue;
            this.optionalStepSubmit = optionalStepSubmit;
        }

        public void setup()
        {
            tester.gotoPage(uri);
            tester.selectOption(inputName, inputValue);
            if (optionalStepSubmit != null)
            {
                tester.submit(optionalStepSubmit);
            }
        }
    }
}
