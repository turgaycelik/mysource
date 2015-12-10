package com.atlassian.jira.webtests.ztests.jelly;

import com.atlassian.jira.testkit.client.restclient.Version;
import com.atlassian.jira.testkit.client.restclient.VersionClient;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.JELLY })
public class TestJellyCreateIssue extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testCreateIssue()
    {
        VersionClient versionClient = new VersionClient(environmentData);
        Version version = new Version();
        version.project("HSP").name("v1");
        Version newVersion = versionClient.create(version);
        version.project("HSP").name("v2");
        newVersion = versionClient.create(version);
        version.project("HSP").name("v3");
        newVersion = versionClient.create(version);
        version.project("HSP").name("v4");
        newVersion = versionClient.create(version);
        version.project("HSP").name("v5");
        newVersion = versionClient.create(version);

        String jellyScript = "<JiraJelly xmlns:jira=\"jelly:com.atlassian.jira.jelly.enterprise.JiraTagLib\">\n" +
                "\n" +
                "<jira:CreateIssue\n" +
                "project-key=\"HSP\"\n" +
                "summary=\"All versions\"\n" +
                "priority=\"Minor\"\n" +
                "versions=\"v1,v2,v3,v4,v5\"\n" +
                "fixVersions=\"v1,v2,v3,v4,v5\"\n" +
                "assignee=\"admin\"\n" +
                "reporter=\"admin\"\n" +
                "description=\"My test Description\"\n" +
                "duedate=\"04/Jul/07 12:16 PM\"\n" +
                "created=\"2008-05-12 13:03:33\"\n" +
                "updated=\"2008-07-11 22:40:06\"\n" +
                "duplicateSummary=\"ignore\"\n" +
                "/>\n" +
                "\n" +
                "</JiraJelly>";
        administration.runJellyScript(jellyScript);
        tester.assertTextPresent("Jelly script completed successfully.");
               

        navigation.issue().viewIssue("HSP-1");
        WebPageLocator locator = new WebPageLocator(tester);
        text.assertTextSequence(locator, "Affects Version/s:", "v1", "v2", "v3", "v4", "v5");
        text.assertTextSequence(locator, "Fix Version/s:", "v1", "v2", "v3", "v4", "v5");
        navigation.gotoAdmin();

        jellyScript = "<JiraJelly xmlns:jira=\"jelly:com.atlassian.jira.jelly.enterprise.JiraTagLib\">\n" +
                "\n" +
                "<jira:CreateIssue\n" +
                "project-key=\"HSP\"\n" +
                "summary=\"All versions\"\n" +
                "priority=\"Minor\"\n" +
                "versions=\"v1,v2,v4,v5\"\n" +
                "fixVersions=\"v1,v2,v3,v5\"\n" +
                "assignee=\"admin\"\n" +
                "reporter=\"admin\"\n" +
                "description=\"My test Description\"\n" +
                "duedate=\"04/Jul/07 12:16 PM\"\n" +
                "created=\"2008-05-12 13:03:33\"\n" +
                "updated=\"2008-07-11 22:40:06\"\n" +
                "duplicateSummary=\"ignore\"\n" +
                "/>\n" +
                "\n" +
                "</JiraJelly>";
        administration.runJellyScript(jellyScript);
        tester.assertTextPresent("Jelly script completed successfully.");

        navigation.issue().viewIssue("HSP-2");
        locator = new WebPageLocator(tester);
        text.assertTextSequence(locator, "Affects Version/s:", "v1", "v2", "v4", "v5");
        text.assertTextSequence(locator, "Fix Version/s:", "v1", "v2", "v3", "v5");
    }
}
