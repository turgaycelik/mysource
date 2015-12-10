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
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.SECURITY, Category.SCHEMES, Category.ISSUE_TYPES, Category.SCREENS })
public class TestXsrfAdminIssueTypeScreenSchemes extends FuncTestCase
{
    private static final String ISSUE_TYPE_SCREEN_SCHEME_NAME = "a frivolous name";

    protected void setUpTest()
    {
        administration.restoreData("TestEditCustomFieldDescription.xml");
    }    

    public void testIssueTypeScreenSchemeOperations() throws Exception
    {
        new XsrfTestSuite(
                new XsrfCheck(
                        "Add Issue Type Screen Scheme",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigate();
                                tester.clickLink("add-issue-type-screen-scheme");
                                tester.setFormElement("schemeName", ISSUE_TYPE_SCREEN_SCHEME_NAME);
                            }
                        },
                        new XsrfCheck.FormSubmission("Add")),
                new XsrfCheck(
                        "Edit Issue Type Screen Scheme",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigate();
                                tester.clickLink("edit_issuetypescreenscheme_10000");
                            }
                        },
                        new XsrfCheck.FormSubmission("Update")),
                new XsrfCheck(
                        "Copy Issue Type Screen Scheme",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigate();
                                tester.clickLink("copy_issuetypescreenscheme_10000");
                            }
                        },
                        new XsrfCheck.FormSubmission("Copy")),
                new XsrfCheck(
                        "Delete Issue Type Screen Scheme",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigate();
                                tester.clickLink("delete_issuetypescreenscheme_10000");
                            }
                        },
                        new XsrfCheck.FormSubmission("Delete"))

        ).run(funcTestHelperFactory);
    }

    public void testIssueTypeScreenSchemeConfigOperations() throws Exception
    {
        addScreen();

        new XsrfTestSuite(
                new XsrfCheck(
                        "Configure Issue Type Screen Scheme Edit Default",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                gotoConfigure();
                                tester.clickLink("edit_issuetypescreenschemeentity_default");
                            }
                        },
                        new XsrfCheck.FormSubmission("Update")),
                new XsrfCheck(
                        "Configure Issue Type Screen Scheme Add",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                gotoConfigure();
                                tester.clickLink("add-issue-type-screen-scheme-configuration-association");
                            }
                        },
                        new XsrfCheck.FormSubmission("Add")),
                new XsrfCheck(
                        "Configure Issue Type Screen Scheme Edit",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                gotoConfigure();
                                tester.clickLink("edit_issuetypescreenschemeentity_Bug");
                            }
                        },
                        new XsrfCheck.FormSubmission("Update")),
                new XsrfCheck(
                        "Configure Issue Type Screen Scheme Delete",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                gotoConfigure();
                            }
                        },
                        new XsrfCheck.LinkWithIdSubmission("delete_issuetypescreenschemeentity_Bug"))

        ).run(funcTestHelperFactory);
    }

    private void addScreen()
    {
        navigate();
        tester.clickLink("add-issue-type-screen-scheme");
        tester.setFormElement("schemeName", ISSUE_TYPE_SCREEN_SCHEME_NAME);
        tester.clickButton("add-issue-type-screen-scheme-form-submit");
    }

    private void gotoConfigure()
    {
        navigate();
        tester.clickLink("configure_issuetypescreenscheme_10000");
    }

    private void navigate()
    {
        navigation.gotoAdminSection("issue_type_screen_scheme");
    }
}
