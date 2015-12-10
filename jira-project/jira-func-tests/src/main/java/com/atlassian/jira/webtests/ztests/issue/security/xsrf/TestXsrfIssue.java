package com.atlassian.jira.webtests.ztests.issue.security.xsrf;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfCheck;
import com.atlassian.jira.functest.framework.security.xsrf.XsrfTestSuite;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Permissions;
import com.meterware.httpunit.WebForm;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

import java.io.IOException;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v4.1
 */
@WebTest ({ Category.FUNC_TEST, Category.SECURITY, Category.ISSUES })
public class TestXsrfIssue extends FuncTestCase
{
    private static final String MY_SUMMARY = "My Summary";
    public static final String ATL_TOKEN = "atl_token";

    protected void setUpTest()
    {
        administration.restoreBlankInstance();
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");
    }

    /**
     * This tests that JIRA can recover from a xsrf token invalidation and allow the user to retry the operation
     */
    public void testXsrfPageCanBeRetried()
    {
        standardCreateIssueSetup();
        removeClientSideToken();
        tester.submit("Create");

        // we should be on the XSRF expired Error page
        assertions.getTextAssertions().assertTextSequence(new WebPageLocator(tester),
                "XSRF Security Token Missing",
                "JIRA could not complete this action due to a missing form token",
                "The original input has been captured and you can retry the operation");

        // now retry the operation
        tester.submit("atl_token_retry_button");

        // we should be on the create issue page and it should exist!
        assertions.getTextAssertions().assertTextSequence(new WebPageLocator(tester), "MKY-1", MY_SUMMARY, "Type:", "Bug", "Status:", "Open");
    }

    /**
     * This tests that JIRA can recover from a session invalidation and allow the user to retry the operation
     */
    public void testXsrfPageSessionCanBeRetried()
    {
        standardCreateIssueSetup();
        invalidateJSessionID();
        tester.submit("Create");

        // we should be on the Session expired Error page
        assertions.getTextAssertions().assertTextSequence(new WebPageLocator(tester),
                "Session Expired",
                "You have been logged out because you have not used JIRA for approximately 5 hours",
                "We have captured the submitted values");
    }

    /**
     * Simulate a session timeout by changing the JSESSIONID.
     */
    private void invalidateJSessionID()
    {
        tester.getDialog().getWebClient().removeCookie("JSESSIONID");
        tester.getDialog().getWebClient().addCookie("JSESSIONID", "invalid");
    }

    private void standardCreateIssueSetup()
    {
        // create an issue
        navigation.issue().goToCreateIssueForm("monkey","Bug");
        tester.setFormElement("summary", MY_SUMMARY);
        tester.setFormElement("description", "My description");
    }

    private void removeClientSideToken()
    {
        for (WebForm webForm : form.getForms())
        {
            if (webForm.hasParameterNamed(ATL_TOKEN))
            {
                webForm.getScriptableObject().setParameterValue(ATL_TOKEN, "invalidToken");
            }
            webForm.getScriptableObject().setAction(XsrfCheck.invalidTokenInUrl(webForm.getAction()));
        }
    }


    public void testCreateIssue() throws Exception
    {
        new XsrfTestSuite(
                new XsrfCheck("CreateIssue", new XsrfCheck.Setup()
                {
                    public void setup()
                    {
                        standardCreateIssueSetup();
                    }
                }, new XsrfCheck.FormSubmission("Create"))
        ).run(funcTestHelperFactory);
    }

    class CreateAndViewIssue implements XsrfCheck.Setup
    {
        String assignee;

        public CreateAndViewIssue() {}

        public CreateAndViewIssue(String assignee)
        {
            this.assignee = assignee;
        }

        public void setup()
        {
            String issueKey = navigation.issue().createIssue("monkey", "Bug", "My Bug");
            if (assignee != null)
            {
                navigation.issue().assignIssue(issueKey, "", assignee);
            }

            navigation.issue().viewIssue(issueKey);
        }
    }

    class CreateViewIssueAndStartProgress implements XsrfCheck.Setup
    {
        public void setup()
        {
            String issueKey = navigation.issue().createIssue("monkey", "Bug", "My Bug");
            navigation.issue().viewIssue(issueKey);
            tester.clickLinkWithText("Start Progress");
        }
    }

    class CreateViewIssueAndAssignComment implements XsrfCheck.Setup
    {
        private String linkText;
        private String assignee;

        public CreateViewIssueAndAssignComment(String linkText, String assignee)
        {
            this.linkText = linkText;
            this.assignee = assignee;
        }


        public CreateViewIssueAndAssignComment(String linkText)
        {
            this.linkText = linkText;
        }

        public void setup()
        {
            String issueKey = navigation.issue().createIssue("monkey", "Bug", "My Bug");
            navigation.issue().viewIssue(issueKey);
            tester.clickLinkWithText(linkText);
            if (assignee != null)
            {
                tester.selectOption("assignee", assignee);
            }
        }
    }

    public void testWorkFlowActions() throws Exception
    {
        new XsrfTestSuite(
                new XsrfCheck(
                        "StartProgress",
                        new CreateAndViewIssue(),
                        new XsrfCheck.LinkWithTextSubmission("Start Progress")),
                new XsrfCheck(
                        "StopProgress",
                        new CreateViewIssueAndStartProgress(),
                        new XsrfCheck.LinkWithTextSubmission("Stop Progress")),
                new XsrfCheck(
                        "Resolve Issue",
                        new CreateViewIssueAndAssignComment("Resolve Issue"),
                        new XsrfCheck.FormSubmission("Transition")),
                new XsrfCheck(
                        "Close Issue",
                        new CreateViewIssueAndAssignComment("Close Issue"),
                        new XsrfCheck.FormSubmission("Transition")),
                new XsrfCheck(
                        "Reopen Issue",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                String issueKey = navigation.issue().createIssue("monkey", "Bug", "My Bug");
                                navigation.issue().viewIssue(issueKey);
                                tester.clickLinkWithText("Resolve Issue");
                                tester.submit("Transition");
                                tester.clickLinkWithText("Reopen Issue");
                            }
                        },
                        new XsrfCheck.FormSubmission("Transition"))
        ).run(funcTestHelperFactory);
    }

    public void testIssueLinking()
    {
        administration.issueLinking().enable();
        administration.issueLinking().addIssueLink("link", "inward", "outward");

        String issueKey = navigation.issue().createIssue("monkey", "Bug", "My Bug");
        String issueKey2 = navigation.issue().createIssue("monkey", "Bug", "My Bug2");
        navigation.issue().viewIssue(issueKey);

        //test creating an issue link with invalid token
        StringBuilder url = new StringBuilder().append("/secure/LinkJiraIssue.jspa?atl_token=").append("invalidtoken").
                append("&id=").append(getIssueIdWithIssueKey(issueKey)).append("&linkDesc=").append("inward").append("&currentIssueKey=").
                append(issueKey).append("&issueKeys=").append(issueKey2);
        navigation.gotoPage(url.toString());
        text.assertTextPresent(new WebPageLocator(tester), "JIRA could not complete this action due to a missing form token.");

        url = new StringBuilder().append("/secure/LinkJiraIssue.jspa?atl_token=").append(page.getXsrfToken()).
                append("&id=").append(getIssueIdWithIssueKey(issueKey)).append("&linkDesc=").append("inward").append("&currentIssueKey=").
                append(issueKey).append("&issueKeys=").append(issueKey2);
        navigation.gotoPage(url.toString());

        text.assertTextNotPresent(new WebPageLocator(tester), "JIRA could not complete this action due to a missing form token.");
    }

    private String getIssueIdWithIssueKey(String issueKey)
    {
        navigation.issue().gotoIssue(issueKey);

        String text;
        String issueId;

        try
        {
            text = tester.getDialog().getResponse().getText();
            String paramName = "ViewVoters!default.jspa?id=";
            int issueIdLocation = text.indexOf(paramName) + paramName.length();
            issueId = text.substring(issueIdLocation, issueIdLocation + 5);
            log("issueId = " + issueId);
        }
        catch (IOException e)
        {
            fail("Unable to retrieve issue id" + e.getMessage());
            return "fail";
        }

        return issueId;
    }

    public void testIssueOperations() throws Exception
    {
        backdoor.usersAndGroups().addUser("my-user");
        backdoor.usersAndGroups().addUserToGroup("my-user", "jira-developers");
        backdoor.darkFeatures().enableForSite("no.frother.assignee.field");

        administration.attachments().enable();
        administration.activateSubTasks();
        administration.timeTracking().enable(TimeTracking.Mode.LEGACY);
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(Permissions.COMMENT_EDIT_OWN, "jira-users");
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(Permissions.WORKLOG_DELETE_ALL, "jira-users");
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(Permissions.WORKLOG_EDIT_ALL, "jira-users");

        new XsrfTestSuite(
                new XsrfCheck(
                        "Assign - another user",
                        new CreateViewIssueAndAssignComment("Assign", "my-user"),
                        new XsrfCheck.FormSubmission("Assign")
                )).run(funcTestHelperFactory);
        new XsrfTestSuite(
                new XsrfCheck(
                        "Assign - to me",
                        new CreateAndViewIssue("my-user"),
                        new XsrfCheck.LinkWithTextSubmission("Assign To Me")
                )).run(funcTestHelperFactory);
        new XsrfTestSuite(
                new XsrfCheck(
                        "Attach file",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                String issueKey = navigation.issue().createIssue("monkey", "Bug", "My Bug");
                                navigation.issue().viewIssue(issueKey);
                                tester.clickLinkWithText("Attach file");
                            }
                        },
                        new XsrfCheck.FormSubmission("Attach")
                )).run(funcTestHelperFactory);
        new XsrfTestSuite(
                new XsrfCheck(
                        "Delete Attachment",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                administration.restoreData("TestCloneIssueAttachments.xml");
                                navigation.gotoPage("/secure/DeleteAttachment!default.jspa?id=10032&deleteAttachmentId=10020");
                            }
                        },
                        new XsrfCheck.FormSubmission("Delete")
                )).run(funcTestHelperFactory);
        new XsrfTestSuite(
                new XsrfCheck(
                        "Clone",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                administration.restoreBlankInstance();
                                backdoor.usersAndGroups().addUser("my-user");
                                backdoor.usersAndGroups().addUserToGroup("my-user", "jira-developers");

                                administration.attachments().enable();
                                administration.activateSubTasks();
                                administration.timeTracking().enable(TimeTracking.Mode.LEGACY);
                                administration.permissionSchemes().defaultScheme().grantPermissionToGroup(Permissions.COMMENT_EDIT_OWN, "jira-users");
                                administration.permissionSchemes().defaultScheme().grantPermissionToGroup(Permissions.WORKLOG_DELETE_ALL, "jira-users");
                                administration.permissionSchemes().defaultScheme().grantPermissionToGroup(Permissions.WORKLOG_EDIT_ALL, "jira-users");
                                new CreateViewIssueAndAssignComment("Clone").setup();
                            }
                        },
                        new XsrfCheck.FormSubmission("Create")
                )).run(funcTestHelperFactory);
        new XsrfTestSuite(
                new XsrfCheck(
                        "Add Comment",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                String issueKey = navigation.issue().createIssue("monkey", "Bug", "My Bug");
                                navigation.issue().viewIssue(issueKey);
                                tester.clickLinkWithText("Comment");
                                tester.setFormElement("comment", "This is a comment");
                            }
                        },
                        new XsrfCheck.FormSubmission("Add")
                )).run(funcTestHelperFactory);
        new XsrfTestSuite(
                new XsrfCheck(
                        "Delete Comment",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                String issueKey = navigation.issue().createIssue("monkey", "Bug", "My Bug");
                                navigation.issue().viewIssue(issueKey);
                                tester.clickLinkWithText("Comment");
                                tester.setWorkingForm("comment-add");
                                tester.setFormElement("comment", "This is a comment");
                                tester.submit("Add");

                                XPathLocator locator = new XPathLocator(tester, "//div[@id='issue_actions_container']//span[text()='Delete']");
                                Node node = locator.getNode();
                                notNull("node", node);
                                String linkId = ((Attr) node.getParentNode().getAttributes().getNamedItem("id")).getValue();
                                tester.clickLink(linkId);
                            }
                        },
                        new XsrfCheck.FormSubmission("Delete")
                )).run(funcTestHelperFactory);
        new XsrfTestSuite(
                new XsrfCheck(
                        "Edit Comment",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                String issueKey = navigation.issue().createIssue("monkey", "Bug", "My Bug");
                                navigation.issue().viewIssue(issueKey);
                                tester.clickLinkWithText("Comment");
                                tester.setFormElement("comment", "This is a comment");
                                tester.setWorkingForm("comment-add");
                                tester.submit("Add");

                                XPathLocator locator = new XPathLocator(tester, "//div[@id='issue_actions_container']//span[text()='Edit']");
                                Node node = locator.getNode();
                                notNull("node", node);
                                String linkId = ((Attr) node.getParentNode().getAttributes().getNamedItem("id")).getValue();
                                tester.clickLink(linkId);
                            }
                        },
                        new XsrfCheck.FormSubmission("Save")
                )).run(funcTestHelperFactory);
        new XsrfTestSuite(
                new XsrfCheck(
                        "Create sub-task",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                String issueKey = navigation.issue().createIssue("monkey", "Bug", "My Bug");
                                navigation.issue().viewIssue(issueKey);
                                tester.clickLink("create-subtask");
                                tester.setFormElement("summary", "This is my subtask");
                            }
                        },
                        new XsrfCheck.FormSubmission("Create")
                )).run(funcTestHelperFactory);
        new XsrfTestSuite(
                new XsrfCheck(
                        "Delete",
                        new CreateViewIssueAndAssignComment("Delete"),
                        new XsrfCheck.FormSubmission("Delete")
                )).run(funcTestHelperFactory);
        new XsrfTestSuite(
                new XsrfCheck(
                        "Edit",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                String issueKey = navigation.issue().createIssue("monkey", "Bug", "My Bug");
                                navigation.issue().viewIssue(issueKey);
                                tester.clickLink("edit-issue");
                                tester.setFormElement("description", "My edit description");
                            }
                        },
                        new XsrfCheck.FormSubmission("Update")
                )).run(funcTestHelperFactory);
        new XsrfTestSuite(
                new XsrfCheck(
                        "Move",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                String issueKey = navigation.issue().createIssue("monkey", "Bug", "My Bug");
                                navigation.issue().viewIssue(issueKey);
                                tester.clickLinkWithText("Move");
                                tester.selectOption("pid", "homosapien");
                                tester.submit("Next >>");
                                tester.submit("Next >>");
                            }
                        },
                        new XsrfCheck.FormSubmission("Move")
                )).run(funcTestHelperFactory);
        new XsrfTestSuite(
                new XsrfCheck(
                        "Convert to sub-task",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                String issueKey = navigation.issue().createIssue("monkey", "Bug", "My Bug");
                                String issueKey2 = navigation.issue().createIssue("monkey", "Bug", "My Bug2");
                                navigation.issue().viewIssue(issueKey);

                                tester.clickLinkWithText("Convert");
                                tester.setFormElement("parentIssueKey", issueKey2);
                                tester.submit("Next >>");
                                tester.submit("Next >>");
                            }
                        },
                        new XsrfCheck.FormSubmission("Finish")
                )).run(funcTestHelperFactory);
        new XsrfTestSuite(
                new XsrfCheck(
                        "Move this subtask",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                String issueKey = navigation.issue().createIssue("monkey", "Bug", "My Bug");
                                String issueKey2 = navigation.issue().createIssue("monkey", "Bug", "My Bug2");
                                String issueKey3 = navigation.issue().createIssue("monkey", "Bug", "My Bug3");
                                navigation.issue().viewIssue(issueKey);

                                tester.clickLinkWithText("Convert");
                                tester.setFormElement("parentIssueKey", issueKey2);
                                tester.submit("Next >>");
                                tester.submit("Next >>");
                                tester.submit("Finish");

                                tester.clickLinkWithText("Move");

                                tester.checkCheckbox("operation", "move.subtask.parent.operation.name");
                                tester.submit("Next >>");

                                tester.setFormElement("parentIssue", issueKey3);
                            }
                        },
                        new XsrfCheck.FormSubmission("Change Parent")
                )).run(funcTestHelperFactory);
        new XsrfTestSuite(
                new XsrfCheck(
                        "Convert to Issue",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                String issueKey = navigation.issue().createIssue("monkey", "Bug", "My Bug");
                                String issueKey2 = navigation.issue().createIssue("monkey", "Bug", "My Bug2");
                                navigation.issue().viewIssue(issueKey);

                                tester.clickLinkWithText("Convert");
                                tester.setFormElement("parentIssueKey", issueKey2);
                                tester.submit("Next >>");
                                tester.submit("Next >>");
                                tester.submit("Finish");

                                tester.clickLinkWithText("Convert");
                                tester.submit("Next >>");
                                tester.submit("Next >>");
                            }
                        },
                        new XsrfCheck.FormSubmission("Finish")
                )).run(funcTestHelperFactory);
        new XsrfTestSuite(
                new XsrfCheck(
                        "Add subtask",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                String issueKey = navigation.issue().createIssue("monkey", "Bug", "My Bug");
                                String issueKey2 = navigation.issue().createIssue("monkey", "Bug", "My Bug2");
                                navigation.issue().viewIssue(issueKey);

                                tester.clickLinkWithText("Convert");
                                tester.setFormElement("parentIssueKey", issueKey2);
                                tester.submit("Next >>");
                                tester.submit("Next >>");
                                tester.submit("Finish");

                                navigation.issue().viewIssue(issueKey2);
                                tester.clickLink("stqc_show");
                                tester.setFormElement("summary", "my subtask");
                            }
                        },
                        new XsrfCheck.FormSubmission("Create")
                )).run(funcTestHelperFactory);

        //Currently broken, will be fixed by http://jdog.atlassian.com/browse/JRADEV-186

        //TODO: Voting
/*
                new XsrfCheck(
                    "Watch it",
                    new CreateAndViewIssue(),
                    new LinkWithTextSubmission("Watch it"))
*/
        //TODO: Stop Watching
        new XsrfTestSuite(
                new XsrfCheck(
                        "Log Work",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                String issueKey = navigation.issue().createIssue("monkey", "Bug", "My Bug");
                                navigation.issue().viewIssue(issueKey);

                                tester.clickLinkWithText("Log Work");
                                tester.setFormElement("timeLogged", "1h");
                            }
                        },
                        new XsrfCheck.FormSubmission("Log")
                )).run(funcTestHelperFactory);
        new XsrfTestSuite(
                new XsrfCheck(
                        "Delete Work Log",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                String issueKey = navigation.issue().createIssue("monkey", "Bug", "My Bug");
                                navigation.issue().viewIssue(issueKey);

                                tester.clickLinkWithText("Log Work");
                                tester.setFormElement("timeLogged", "1h");
                                tester.submit("Log");
                                tester.clickLink("all-tabpanel");
                                tester.clickLinkWithText("Work Log");

                                XPathLocator locator = new XPathLocator(tester, "//div[@id='issue_actions_container']//span[text()='Delete']");
                                Node node = locator.getNode();
                                notNull("node", node);
                                String linkId = ((Attr) node.getParentNode().getAttributes().getNamedItem("id")).getValue();
                                tester.clickLink(linkId);
                            }
                        },
                        new XsrfCheck.FormSubmission("Delete")
                )).run(funcTestHelperFactory);
        new XsrfTestSuite(
                new XsrfCheck(
                        "Edit Work Log",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                String issueKey = navigation.issue().createIssue("monkey", "Bug", "My Bug");
                                navigation.issue().viewIssue(issueKey);

                                tester.clickLinkWithText("Log Work");
                                tester.setFormElement("timeLogged", "1h");
                                tester.submit("Log");
                                tester.clickLink("all-tabpanel");
                                tester.clickLinkWithText("Work Log");

                                XPathLocator locator = new XPathLocator(tester, "//div[@id='issue_actions_container']//span[text()='Edit']");
                                Node node = locator.getNode();
                                notNull("node", node);
                                String linkId = ((Attr) node.getParentNode().getAttributes().getNamedItem("id")).getValue();
                                tester.clickLink(linkId);
                            }
                        },
                        new XsrfCheck.FormSubmission("Log")
                )).run(funcTestHelperFactory);

        backdoor.darkFeatures().disableForSite("no.frother.assignee.field");
    }

    public void testVotingOrWatching() throws Exception
    {
        administration.restoreBlankInstance();
        navigation.logout();
        navigation.login(FRED_USERNAME);
        final String issueKey = navigation.issue().createIssue("monkey", "Bug", "My Bug");
        navigation.login(ADMIN_USERNAME);
        new XsrfTestSuite(
                new XsrfCheck("Vote for an issue",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.issue().viewIssue(issueKey);
                            }
                        },
                        new XsrfCheck.LinkWithIdSubmission("vote-toggle")),
                new XsrfCheck("Watch an issue",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.issue().viewIssue(issueKey);
                            }
                        },
                        new XsrfCheck.LinkWithIdSubmission("watching-toggle")),
                new XsrfCheck("Unvote an issue",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.issue().viewIssue(issueKey);
                                tester.clickLink("vote-toggle");
                            }
                        },
                        new XsrfCheck.LinkWithIdSubmission("vote-toggle")),
                new XsrfCheck("Unwatch an issue",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                navigation.issue().viewIssue(issueKey);
                                tester.clickLink("watching-toggle");
                            }
                        },
                        new XsrfCheck.LinkWithIdSubmission("watching-toggle")),

                new XsrfCheck("View Voters - Un Vote",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                tester.gotoPage("secure/ViewVoters!default.jspa?id=10000");
                            }
                        },
                        new XsrfCheck.LinkWithIdSubmission("unvote")),

                new XsrfCheck("View Voters - Vote",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                tester.gotoPage("secure/ViewVoters!default.jspa?id=10000");
                            }
                        },
                        new XsrfCheck.LinkWithIdSubmission("vote")),

                new XsrfCheck("Manager Watches - Un Watch",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                tester.gotoPage("secure/ManageWatchers!default.jspa?id=10000");
                            }
                        },
                        new XsrfCheck.LinkWithIdSubmission("unwatch")),

                new XsrfCheck("Manager Watches - Watch",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                tester.gotoPage("secure/ManageWatchers!default.jspa?id=10000");
                            }
                        },
                        new XsrfCheck.LinkWithIdSubmission("watch")),

                new XsrfCheck("Manager Watches - Add Watcher",
                        new XsrfCheck.Setup()
                        {
                            public void setup()
                            {
                                tester.gotoPage("secure/ManageWatchers!default.jspa?id=10000");
                                tester.setFormElement("userNames", FRED_USERNAME);
                            }
                        },
                        new XsrfCheck.FormSubmission("add"))


        ).run(funcTestHelperFactory);
    }
}
