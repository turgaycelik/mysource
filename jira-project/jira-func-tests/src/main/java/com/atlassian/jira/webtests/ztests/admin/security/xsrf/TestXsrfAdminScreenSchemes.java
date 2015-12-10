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
public class TestXsrfAdminScreenSchemes extends FuncTestCase
{
    private static final String SCREEN_SCHEME_NAME = "villainous scheme";

    protected void setUpTest()
    {
        administration.restoreData("TestEditCustomFieldDescription.xml");
    }    

    public void testScreenSchemeOperations() throws Exception
    {
        new XsrfTestSuite(
                new XsrfCheck(
                        "Add Screen Scheme",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.gotoAdminSection("field_screen_scheme");
                                tester.clickLink("add-field-screen-scheme");
                                tester.setFormElement("fieldScreenSchemeName", SCREEN_SCHEME_NAME);
                            }
                        },
                        new XsrfCheck.FormSubmission("Add")),
                new XsrfCheck(
                        "Edit Screen Scheme",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.gotoAdminSection("field_screen_scheme");
                                tester.clickLink("edit_fieldscreenscheme_" + SCREEN_SCHEME_NAME);
                            }
                        },
                        new XsrfCheck.FormSubmission("Update")),
                new XsrfCheck(
                        "Copy Screen Scheme",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.gotoAdminSection("field_screen_scheme");
                                tester.clickLink("copy_fieldscreenscheme_" + SCREEN_SCHEME_NAME);
                            }
                        },
                        new XsrfCheck.FormSubmission("Copy")),
                new XsrfCheck(
                        "Delete Screen Scheme",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.gotoAdminSection("field_screen_scheme");
                                tester.clickLink("delete_fieldscreenscheme_" + SCREEN_SCHEME_NAME);
                            }
                        },
                        new XsrfCheck.FormSubmission("Delete"))

        ).run(funcTestHelperFactory);
    }

    public void testScreenSchemeConfigOperations() throws Exception
    {
        addScreen();

        new XsrfTestSuite(
                new XsrfCheck(
                        "Configure Screen Scheme Add",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                gotoConfigure();
                                tester.clickLink("add-screen-scheme-item");
                            }
                        },
                        new XsrfCheck.FormSubmission("Add")),
                new XsrfCheck(
                        "Configure Screen Scheme Edit",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                gotoConfigure();
                                tester.clickLink("edit_fieldscreenscheme_Create Issue");
                            }
                        },
                        new XsrfCheck.FormSubmission("Update")),
                new XsrfCheck(
                        "Configure Screen Scheme Delete",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                gotoConfigure();
                            }
                        },
                        new XsrfCheck.LinkWithIdSubmission("delete_fieldscreenscheme_Create Issue"))

        ).run(funcTestHelperFactory);
    }

    private void addScreen()
    {
        navigation.gotoAdminSection("field_screen_scheme");
        tester.clickLink("add-field-screen-scheme");
        tester.setFormElement("fieldScreenSchemeName", SCREEN_SCHEME_NAME);
        tester.submit("Add");
    }

    private void gotoConfigure()
    {
        navigation.gotoAdminSection("field_screen_scheme");
        tester.clickLink("configure_fieldscreenscheme_" + SCREEN_SCHEME_NAME);
    }
}
