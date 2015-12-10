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
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.SECURITY, Category.SCHEMES, Category.FIELDS })
public class TestXsrfAdminFieldConfigurationSchemes extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreData("TestEditCustomFieldDescription.xml");
    }    

    public void testFieldConfigurationSchemeOperations() throws Exception
    {
        new XsrfTestSuite(
                new XsrfCheck(
                        "Add Field Configuration Schema",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.gotoAdminSection("issue_fields");
                                tester.clickLink("add-field-configuration-scheme");
                                tester.setFormElement("fieldLayoutSchemeName", "name-add");
                            }
                        },
                        new XsrfCheck.FormSubmission("Add")),
                new XsrfCheck(
                        "Copy Field Configuration Schema",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.gotoAdminSection("issue_fields");
                                tester.clickLink("copy_10010");
                                tester.setFormElement("fieldLayoutSchemeName", "name-copied");
                            }
                        },
                        new XsrfCheck.FormSubmission("Copy")),
                new XsrfCheck(
                        "Edit Field Configuration Schema",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.gotoAdminSection("issue_fields");
                                tester.clickLink("edit_10010");
                                tester.setFormElement("fieldLayoutSchemeName", "name-edited");
                            }
                        },
                        new XsrfCheck.FormSubmission("Update")),
                new XsrfCheck(
                        "Delete Field Configuration Schema",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.gotoAdminSection("issue_fields");
                                tester.clickLink("del_name-edited");
                            }
                        },
                        new XsrfCheck.FormSubmission("Delete"))

        ).run(funcTestHelperFactory);
    }

    public void testFieldConfigurationConfigureOperations() throws Exception
    {
        addFieldConfigurationSchema("configure-add");

        new XsrfTestSuite(
                new XsrfCheck(
                        "Configure Field Configuration Edit Default",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                administration.fieldConfigurationSchemes().
                                        fieldConfigurationScheme("configure-add").goTo();

                                tester.clickLink("edit_fieldlayoutschemeentity");
                            }
                        },
                        new XsrfCheck.FormSubmission("Update")),
                new XsrfCheck(
                        "Configure Field Configuration Schema Add",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                administration.fieldConfigurationSchemes().
                                        fieldConfigurationScheme("configure-add").goTo();

                                tester.clickLink("add-issue-type-field-configuration-association");
                            }
                        },
                        new XsrfCheck.FormSubmission("Add")),
                new XsrfCheck(
                        "Configure Field Configuration Edit",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                administration.fieldConfigurationSchemes().
                                        fieldConfigurationScheme("configure-add").goTo();

                                tester.clickLink("edit_fieldlayoutschemeentity_1");
                            }
                        },
                        new XsrfCheck.FormSubmission("Update")),
                new XsrfCheck(
                        "Configure Field Configuration Delete",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                administration.fieldConfigurationSchemes().
                                        fieldConfigurationScheme("configure-add").goTo();
                            }
                        },
                        new XsrfCheck.LinkWithIdSubmission("delete_fieldlayoutschemeentity_1"))

        ).run(funcTestHelperFactory);

    }

    private void addFieldConfigurationSchema(String name)
    {
        navigation.gotoAdminSection("issue_fields");
        tester.clickLink("add-field-configuration-scheme");
        tester.setFormElement("fieldLayoutSchemeName", name);
        tester.submit("Add");
    }
}
