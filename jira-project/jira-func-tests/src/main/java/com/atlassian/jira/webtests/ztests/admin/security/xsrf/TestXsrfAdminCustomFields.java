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
public class TestXsrfAdminCustomFields extends FuncTestCase
{
    private static final String FIELD_TYPE = "com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect";

    protected void setUpTest()
    {
        administration.restoreData("TestEditCustomFieldDescription.xml");
    }

    public void testCustomFieldOperations() throws Exception
    {
        new XsrfTestSuite(
                new XsrfCheck(
                        "Add Custom Field",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.gotoAdminSection("view_custom_fields");
                                tester.clickLink("add_custom_fields");
                                tester.setFormElement("fieldType", FIELD_TYPE);
                            }
                        },
                        new XsrfCheck.FormSubmission("nextBtn")),
                new XsrfCheck(
                        "Edit Custom Field",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                addCustomField();

                                tester.clickLink("edit_superAwesomeField");
                                tester.setFormElement("name", "superMoreAwesomeField");
                            }
                        },
                        new XsrfCheck.FormSubmission("Update")),
                new XsrfCheck(
                        "Delete Custom Field",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                String customFieldId = addCustomField();

                                tester.clickLink("del_" + customFieldId);
                            }
                        },
                        new XsrfCheck.FormSubmission("Delete"))

        ).run(funcTestHelperFactory);
    }

    public void testCustomFieldConfigurationOperations() throws Exception
    {
        final String customFieldId = addCustomField();
        final String numericCustomFieldId = customFieldId.split("_")[1];
        final String contextId = addCustomFieldContext(numericCustomFieldId);

        new XsrfTestSuite(
                new XsrfCheck(
                        "Configure Custom Field - Add",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.gotoAdminSection("view_custom_fields");
                                tester.clickLink("config_" + customFieldId);
                                tester.clickLink("add_new_context");
                                tester.setFormElement("name", "my context");
                                tester.selectOption("projects", "homosapien");
                            }
                        },
                        new XsrfCheck.FormSubmission("Add")),
                new XsrfCheck(
                        "Configure Custom Field - Edit",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.gotoAdminSection("view_custom_fields");
                                tester.clickLink("config_" + customFieldId);
                                tester.clickLink("edit_" + contextId);
                            }
                        },
                        new XsrfCheck.FormSubmission("Modify")),
                new XsrfCheck(
                        "Configure Custom Field - Set Default",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.gotoAdminSection("view_custom_fields");
                                tester.clickLink("config_" + customFieldId);
                                tester.clickLink(customFieldId + "-edit-default");
                            }
                        },
                        new XsrfCheck.FormSubmission("Set Default")),
                new XsrfCheck(
                        "Configure Custom Field - Delete",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.gotoAdminSection("view_custom_fields");
                                tester.clickLink("config_" + customFieldId);
                                //deleteIdSubmitter.setLinkId("delete_" + contextId);
                            }
                        },
                        new XsrfCheck.LinkWithIdSubmission("delete_" + contextId))

        ).run(funcTestHelperFactory);
    }

    private String addCustomField()
    {
        return administration.customFields().addCustomField(FIELD_TYPE, "superAwesomeField");
    }

    private String addCustomFieldContext(String numericCustomFieldId)
    {
        return administration.customFields().addConfigurationSchemeContext(numericCustomFieldId, "my context", new String[]{}, new String[]{"10001"});
    }

}
