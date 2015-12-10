package com.atlassian.jira.webtests.ztests.jelly;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JELLY })
public class TestJellyAddComment extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestJellyAddComment.xml");
    }

    public void testAddCommentNoDate()
    {
        navigation.issue().viewIssue("HSP-1");
        text.assertTextNotPresent(new WebPageLocator(tester), "This is a first comment.");
        text.assertTextSequence(new WebPageLocator(tester), "Created", "2:21 PM", "Updated", "2:21 PM");

        String script = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<JiraJelly xmlns:jira=\"jelly:com.atlassian.jira.jelly.enterprise.JiraTagLib\" xmlns:core=\"jelly:core\">\n"
                + "  <jira:AddComment issue-key=\"HSP-1\" commenter=\"admin\" comment=\"This is a first comment.\"/>\n"
                + "</JiraJelly>";
        administration.runJellyScript(script);

        navigation.issue().viewIssue("HSP-1");
        text.assertTextPresent(new WebPageLocator(tester), "This is a first comment.");
    }

    public void testAddCommentWithCreatedDate()
    {
        navigation.issue().viewIssue("HSP-1");
        text.assertTextNotPresent(new WebPageLocator(tester), "This is a second comment.");
        text.assertTextSequence(new WebPageLocator(tester), "Created", "2:21 PM", "Updated", "2:21 PM");

        final String script = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<JiraJelly xmlns:jira=\"jelly:com.atlassian.jira.jelly.enterprise.JiraTagLib\" xmlns:core=\"jelly:core\">\n"
                + "  <jira:AddComment issue-key=\"HSP-1\" commenter=\"admin\" comment=\"This is a second comment.\" created=\"2009-10-02 11:54:12.0\"/>\n"
                + "</JiraJelly>";

        administration.runJellyScript(script);

        navigation.issue().viewIssue("HSP-1");
        text.assertTextPresent(new WebPageLocator(tester), "This is a second comment.");
        //updated date was provided, (via created) therefore the issues updated date should have been set to the updated date.
        text.assertTextSequence(new WebPageLocator(tester), "Created", "2:21 PM", "Updated", "02/Oct/09 11:54 AM");
        text.assertTextSequence(new WebPageLocator(tester), ADMIN_FULLNAME, "added a comment", "02/Oct/09 11:54 AM");
    }

    public void testAddCommentWithUpdatedDate()
    {
        navigation.issue().viewIssue("HSP-1");
        text.assertTextNotPresent(new WebPageLocator(tester), "This is a second comment.");
        text.assertTextSequence(new WebPageLocator(tester), "Created", "2:21 PM", "Updated", "2:21 PM");

        final String script = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<JiraJelly xmlns:jira=\"jelly:com.atlassian.jira.jelly.enterprise.JiraTagLib\" xmlns:core=\"jelly:core\">\n"
                + "  <jira:AddComment issue-key=\"HSP-1\" commenter=\"admin\" comment=\"This is a second comment.\" created=\"2009-10-02 11:54:12.0\" updated=\"2009-12-02 11:54:12.0\"/>\n"
                + "</JiraJelly>";

        administration.runJellyScript(script);

        navigation.issue().viewIssue("HSP-1");
        text.assertTextPresent(new WebPageLocator(tester), "This is a second comment.");
        //updated date was provided, (via created) therefore the issues updated date should have been set to the updated date.
        text.assertTextSequence(new WebPageLocator(tester), "Created", "2:21 PM", "Updated", "02/Dec/09 11:54 AM");
        text.assertTextSequence(new WebPageLocator(tester), ADMIN_FULLNAME, "added a comment", "02/Oct/09 11:54 AM", "edited");
    }

    public void testAddCommentWithUpdatedDateInPast()
    {
        navigation.issue().viewIssue("HSP-1");
        text.assertTextNotPresent(new WebPageLocator(tester), "This is a second comment.");
        text.assertTextSequence(new WebPageLocator(tester), "Created", "2:21 PM", "Updated", "2:21 PM");

        final String script = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<JiraJelly xmlns:jira=\"jelly:com.atlassian.jira.jelly.enterprise.JiraTagLib\" xmlns:core=\"jelly:core\">\n"
                + "  <jira:AddComment issue-key=\"HSP-1\" commenter=\"admin\" comment=\"This is a second comment.\" created=\"2008-10-02 11:54:12.0\" updated=\"2008-12-02 11:54:12.0\"/>\n"
                + "</JiraJelly>";

        administration.runJellyScript(script);

        navigation.issue().viewIssue("HSP-1");
        text.assertTextPresent(new WebPageLocator(tester), "This is a second comment.");
        //updated date was provided, (via created) therefore the issues updated date should have been set to the updated date.
        text.assertTextSequence(new WebPageLocator(tester), "Created", "2:21 PM", "Updated", "27/Apr/09 2:21 PM");
        text.assertTextSequence(new WebPageLocator(tester), ADMIN_FULLNAME, "added a comment", "02/Oct/08 11:54 AM", "edited");
    }

    public void testAddCommentWithDateDontTweakIssueUpdateDate()
    {
        navigation.issue().viewIssue("HSP-1");
        text.assertTextNotPresent(new WebPageLocator(tester), "This is a third comment.");
        text.assertTextSequence(new WebPageLocator(tester), "Created", "2:21 PM", "Updated", "2:21 PM");

        final String script = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<JiraJelly xmlns:jira=\"jelly:com.atlassian.jira.jelly.enterprise.JiraTagLib\" xmlns:core=\"jelly:core\">\n"
                + "  <jira:AddComment issue-key=\"HSP-1\" commenter=\"admin\" comment=\"This is a third comment.\" created=\"2008-10-02 11:54:12.0\" "
                + "tweakIssueUpdateDate=\"false\"/>\n"
                + "</JiraJelly>";

        administration.runJellyScript(script);

        navigation.issue().viewIssue("HSP-1");
        text.assertTextPresent(new WebPageLocator(tester), "This is a third comment.");
        //updated date should not have been affected!
        text.assertTextSequence(new WebPageLocator(tester), "Created", "2:21 PM", "Updated", "2:21 PM");
        text.assertTextSequence(new WebPageLocator(tester), ADMIN_FULLNAME, "added a comment", "02/Oct/08 11:54 AM");
    }
}
