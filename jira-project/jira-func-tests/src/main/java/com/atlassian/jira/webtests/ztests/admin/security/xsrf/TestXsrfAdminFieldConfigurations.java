package com.atlassian.jira.webtests.ztests.admin.security.xsrf;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfTestSuite;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Responsible for holding tests which verify that the User Administration actions are not susceptible to XSRF attacks.
 * @since v4.1
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.SECURITY })
public class TestXsrfAdminFieldConfigurations extends FuncTestCase
{
    private static final String FIELD_NAME = "config-name";

    protected void setUpTest()
    {
        administration.restoreData("TestEditCustomFieldDescription.xml");
    }    

    public void testFieldConfigurationOperations() throws Exception
    {
        new XsrfTestSuite(
                new XsrfCheck(
                        "Add Field Configuration",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.gotoAdminSection("field_configuration");
                                tester.clickLink("add-field-configuration");
                                tester.setFormElement("fieldLayoutName", FIELD_NAME);
                            }
                        },
                        new XsrfCheck.FormSubmission("Add")),
                new XsrfCheck(
                        "Copy Field Configuration",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                addFieldConfiguration();
                                tester.clickLink("copy-" + FIELD_NAME);
                            }
                        },
                        new XsrfCheck.FormSubmission("Copy")),
                new XsrfCheck(
                        "Edit Custom Field",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                addFieldConfiguration();
                                tester.clickLink("edit-" + FIELD_NAME);
                                tester.setFormElement("fieldLayoutName", FIELD_NAME + "-i-edit-you");
                            }
                        },
                        new XsrfCheck.FormSubmission("Update")),
                new XsrfCheck(
                        "Delete Custom Field",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                addFieldConfiguration();
                                tester.clickLink("delete-" + FIELD_NAME);
                            }
                        },
                        new XsrfCheck.FormSubmission("Delete"))

        ).run(funcTestHelperFactory);
    }

    public void testFieldConfigurationConfigureOperations() throws Exception
    {
        addFieldConfiguration();

        new XsrfTestSuite(
                new XsrfCheck(
                        "Configure Field Configuration Edit",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                viewConfiguration();
                                tester.clickLink("edit_0");
                                tester.setFormElement("description", "OMG description ^_^");
                            }
                        },
                        new XsrfCheck.FormSubmission("Update")),
                new XsrfCheck(
                        "Configure Field Configuration Hide",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                viewConfiguration();
                            }
                        },
                        new XsrfCheck.LinkWithIdSubmission("hide_0")),
                new XsrfCheck(
                        "Configure Field Configuration Show",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                viewConfiguration();
                            }
                        },
                        new XsrfCheck.LinkWithIdSubmission("show_0")),
                new XsrfCheck(
                        "Configure Field Configuration Required",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                viewConfiguration();
                            }
                        },
                        new XsrfCheck.LinkWithIdSubmission("require_0")),
                new XsrfCheck(
                        "Configure Field Configuration Optional",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                viewConfiguration();
                            }
                        },
                        new XsrfCheck.LinkWithIdSubmission("require_0")),
                new XsrfCheck(
                        "Configure Field Configuration Renderers",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                viewConfiguration();
                                tester.clickLink("renderer_versions");
                                tester.setWorkingForm("jiraform");
                                tester.clickButton("update_submit"); // takes us to confirm screen
                            }
                        },
                        new XsrfCheck.FormSubmission("Update"))

        ).run(funcTestHelperFactory);

    }

    private void addFieldConfiguration()
    {
        navigation.gotoAdminSection("field_configuration");
        tester.clickLink("add-field-configuration");
        tester.setFormElement("fieldLayoutName", FIELD_NAME);
        tester.clickButton("add-field-configuration-submit");
        // Hack: we go to the field configuration list so that if we already have created the field configuration we can
        // continue on with the test and do not get stuck in the add field configuration form.
        navigation.gotoAdminSection("field_configuration");
    }

    private void viewConfiguration()
    {
        navigation.gotoAdminSection("field_configuration");
        tester.clickLink("configure-" + FIELD_NAME);
    }
}
