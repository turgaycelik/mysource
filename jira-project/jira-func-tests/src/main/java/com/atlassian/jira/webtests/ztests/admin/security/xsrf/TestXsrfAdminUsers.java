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
public class TestXsrfAdminUsers extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreData("TestEditUserProjectRoles.xml");
    }    

    public void testUserOperations() throws Exception
    {
        new XsrfTestSuite(
                new XsrfCheck(
                        "Add Properties",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                addProperty();
                            }
                        },
                        new XsrfCheck.FormSubmission("Add")),
                new XsrfCheck(
                        "Delete Properties",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                addProperty();
                                tester.submit("Add");

                                tester.clickLink("delete_a");
                            }
                        },
                        new XsrfCheck.FormSubmission("Delete")),
                new XsrfCheck(
                        "Edit Properties",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                addProperty();
                                tester.submit("Add");

                                tester.clickLink("edit_a");
                                tester.setFormElement("value", "bc");
                            }
                        },
                        new XsrfCheck.FormSubmission("Update")),
                new XsrfCheck(
                        "Add Group",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                administration.usersAndGroups().gotoViewUser(FRED_USERNAME);
                                tester.clickLink("editgroups_link");
                                tester.selectOption("groupsToJoin", "jira-administrators");
                            }
                        },
                        new XsrfCheck.FormSubmission("join")),
                new XsrfCheck(
                        "Remove Group",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                administration.usersAndGroups().gotoViewUser(FRED_USERNAME);
                                tester.clickLink("editgroups_link");
                                tester.selectOption("groupsToLeave", "jira-users");
                            }
                        },
                        new XsrfCheck.FormSubmission("leave")),
                new XsrfCheck(
                        "Edit Project Roles",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                administration.usersAndGroups().gotoViewUser(ADMIN_USERNAME);
                                tester.clickLink("viewprojectroles_link");
                                tester.clickLinkWithText("Edit Project Roles");
                                tester.checkCheckbox("10020_10011", "on");
                            }
                        },
                        new XsrfCheck.FormSubmission("Save")),
                new XsrfCheck(
                        "Update details",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                administration.usersAndGroups().gotoViewUser(FRED_USERNAME);
                                tester.clickLinkWithText("Edit Details");
                                tester.setFormElement("fullName", "Fred's new name");
                            }
                        },
                        new XsrfCheck.FormSubmission("Update")),
                new XsrfCheck(
                        "Set Password",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                administration.usersAndGroups().gotoViewUser(FRED_USERNAME);
                                tester.clickLinkWithText("Set Password");
                                tester.setFormElement("password", "asdf");
                                tester.setFormElement("confirm", "asdf");
                            }
                        },
                        new XsrfCheck.FormSubmission("Update")),
                new XsrfCheck(
                        "Delete User",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                administration.usersAndGroups().gotoViewUser(FRED_USERNAME);
                                tester.clickLink("deleteuser_link");
                            }
                        },
                        new XsrfCheck.FormSubmission("Delete")),
                new XsrfCheck(
                        "Create User",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.gotoAdminSection("user_browser");
                                tester.clickLink("create_user");
                                tester.setFormElement("username","evil");
                                tester.setFormElement("password","evil");
                                tester.setFormElement("confirm","evil");
                                tester.setFormElement("fullname","Totally Evil");
                                tester.setFormElement("email","evil@example.com");
                                tester.uncheckCheckbox("sendEmail");
                            }
                        },
                        new XsrfCheck.FormSubmission("Create"))

        ).run(funcTestHelperFactory);
    }

    private void addProperty()
    {
        administration.usersAndGroups().gotoViewUser(FRED_USERNAME);
        tester.clickLinkWithText("Edit Properties");
        tester.setFormElement("key", "a");
        tester.setFormElement("value", "b");
    }
}
