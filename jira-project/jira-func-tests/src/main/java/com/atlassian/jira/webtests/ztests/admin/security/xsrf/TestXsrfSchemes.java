package com.atlassian.jira.webtests.ztests.admin.security.xsrf;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfTestSuite;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.permission.ProjectPermissions;

import static com.atlassian.jira.permission.ProjectPermissions.ADMINISTER_PROJECTS;

/**
 * @since v4.1
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.SECURITY })
public class TestXsrfSchemes extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testPermissionSchemeAdministration() throws Exception
    {
        new XsrfTestSuite(
                new XsrfCheck("AddPermissionScheme", new PermissionSchemeSetup()
                {
                    public void setup()
                    {
                        super.setup();
                        tester.clickLinkWithText("Add Permission Scheme");
                        tester.setFormElement("name", "New Permission Scheme 1");
                    }
                }, new XsrfCheck.FormSubmission("Add")),
                new XsrfCheck("EditPermissionSchemeDetails", new PermissionSchemeSetup()
                {
                    public void setup()
                    {
                        super.setup();
                        tester.clickLink("10000_edit_details");
                        tester.setFormElement("description", "This is New Permission Scheme 1!");
                    }
                }, new XsrfCheck.FormSubmission("Update")),
                new XsrfCheck("CopyPermissionScheme", new PermissionSchemeSetup(), new XsrfCheck.LinkWithIdSubmission("10000_copy")),
                new XsrfCheck("DeletePermissionScheme", new PermissionSchemeSetup()
                {
                    public void setup()
                    {
                        super.setup();
                        tester.clickLink("del_Copy of New Permission Scheme 1");
                    }
                }, new XsrfCheck.FormSubmission("Delete")),
                new XsrfCheck("AddPermission", new PermissionSchemeSetup()
                {
                    public void setup()
                    {
                        super.setup();
                        tester.clickLinkWithText("New Permission Scheme 1");
                        tester.clickLink("add_perm_" + ADMINISTER_PROJECTS.permissionKey());
                        tester.checkCheckbox("type", "reporter");
                    }
                }, new XsrfCheck.FormSubmission(" Add ")),
                new XsrfCheck("DeletePermission", new PermissionSchemeSetup()
                {
                    public void setup()
                    {
                        super.setup();
                        tester.clickLinkWithText("New Permission Scheme 1");
                        tester.clickLink("del_perm_" + ADMINISTER_PROJECTS.permissionKey() + "_");
                    }
                }, new XsrfCheck.FormSubmission("Delete"))
        ).run(funcTestHelperFactory);
    }

    public void testWorkflowSchemeAdministration() throws Exception
    {
        new XsrfTestSuite(
                new XsrfCheck("AddWorkflowScheme", new WorkflowSchemeSetup()
                {
                    public void setup()
                    {
                        super.setup();
                        tester.clickLink("add_workflowscheme");
                        tester.setFormElement("name", "name");
                        tester.setFormElement("description", "desc");
                    }
                }, new XsrfCheck.FormSubmission("Add"))
                ,
                new XsrfCheck("CopyWorkflowScheme", new WorkflowSchemeSetup()
                {
                    public void setup()
                    {
                        super.setup();
                    }
                }, new XsrfCheck.LinkWithTextSubmission("Copy"))
                ,
                new XsrfCheck("DeleteWorkflowScheme", new WorkflowSchemeSetup()
                {
                    public void setup()
                    {
                        super.setup();
                        tester.clickLinkWithText("Delete");
                    }
                }, new XsrfCheck.FormSubmission("Delete"))

        ).run(funcTestHelperFactory);
    }

    public void testNotificationSchemeAdministration() throws Exception
    {
        new XsrfTestSuite(
                new XsrfCheck("AddNotificationScheme", new NotificationSchemeSetup()
                {
                    public void setup()
                    {
                        super.setup();
                        tester.clickLinkWithText("Add Notification Scheme");
                        tester.setFormElement("name", "name");
                        tester.setFormElement("description", "desc");
                    }
                }, new XsrfCheck.FormSubmission("Add"))
                ,
                new XsrfCheck("EditNotificationScheme", new NotificationSchemeSetup()
                {
                    public void setup()
                    {
                        super.setup();
                        tester.clickLink("10010_rename");
                    }
                }, new XsrfCheck.FormSubmission("Update"))
                ,
                new XsrfCheck("CopyNotificationScheme", new NotificationSchemeSetup()
                {
                    public void setup()
                    {
                        super.setup();
                    }
                }, new XsrfCheck.LinkWithTextSubmission("Copy"))
                ,
                new XsrfCheck("DeleteNotificationScheme", new NotificationSchemeSetup()
                {
                    public void setup()
                    {
                        super.setup();
                        tester.clickLinkWithText("Delete");
                    }
                }, new XsrfCheck.FormSubmission("Delete"))

        ).run(funcTestHelperFactory);
    }

    public void testIssueSecuritySchemeAdministration() throws Exception
    {
        new XsrfTestSuite(
                new XsrfCheck("AddIssueSecurityScheme", new IssueSecuritySchemeSetup()
                {
                    public void setup()
                    {
                        super.setup();
                        tester.clickLink("add_securityscheme");
                        tester.setFormElement("name", "name");
                        tester.setFormElement("description", "desc");
                    }
                }, new XsrfCheck.FormSubmission("Add"))
                ,
                new XsrfCheck("EditIssueSecurityScheme", new IssueSecuritySchemeSetup()
                {
                    public void setup()
                    {
                        super.setup();
                        tester.clickLink("edit_10000");
                    }
                }, new XsrfCheck.FormSubmission("Update"))
                ,
                new XsrfCheck("CopyIssueSecurityScheme", new IssueSecuritySchemeSetup()
                {
                    public void setup()
                    {
                        super.setup();
                    }
                }, new XsrfCheck.LinkWithTextSubmission("Copy"))
                ,
                new XsrfCheck("DeleteIssueSecurityScheme", new IssueSecuritySchemeSetup()
                {
                    public void setup()
                    {
                        super.setup();
                        tester.clickLinkWithText("Delete");
                    }
                }, new XsrfCheck.FormSubmission("Delete"))

        ).run(funcTestHelperFactory);
    }

    private class PermissionSchemeSetup implements XsrfCheck.Setup
    {
        public void setup()
        {
            gotoDashboard();
            navigation.gotoAdminSection("permission_schemes");
        }
    }

    private class WorkflowSchemeSetup implements XsrfCheck.Setup
    {
        public void setup()
        {
            gotoDashboard();
            navigation.gotoAdminSection("workflow_schemes");
        }
    }

    private class NotificationSchemeSetup implements XsrfCheck.Setup
    {
        public void setup()
        {
            gotoDashboard();
            navigation.gotoAdminSection("notification_schemes");
        }
    }

    private class IssueSecuritySchemeSetup implements XsrfCheck.Setup
    {
        public void setup()
        {
            gotoDashboard();
            navigation.gotoAdminSection("security_schemes");
        }
    }

    private void gotoDashboard()
    {
        // after "Copy" check, the response no longer has the Admin bar. We need to start again from the Dashboard.
        if (!page.isLinkPresentWithExactText("Administration"))
        {
            navigation.gotoDashboard();
        }
    }
}
