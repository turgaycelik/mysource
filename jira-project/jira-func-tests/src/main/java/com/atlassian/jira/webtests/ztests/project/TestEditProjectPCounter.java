package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Test for JRA-20038.
 * Creating an Issue needs to refresh the Project cache because the "pcounter" is updated.
 */
@WebTest ({ Category.FUNC_TEST, Category.PROJECTS })
public class TestEditProjectPCounter extends FuncTestCase
{
    public void testEditProjectPCounter() throws Exception
    {
        // We load JIRA data with 2 projects but no issues.
        administration.restoreBlankInstance();
        administration.project().addProject("Bovine", "COW", ADMIN_USERNAME);
        administration.project().addProject("Canine", "DOG", ADMIN_USERNAME);

        // Create an issue in the wrong Project
        navigation.issue().createIssue("Bovine", "Bug", "Three legged dog");
        // Create another issue in the COW Project
        navigation.issue().createIssue("Bovine", "Bug", "Where's the cheese?");

        // Move COW-1 to DOG project
        navigation.issue().viewIssue("COW-1");
        tester.clickLink("move-issue");
        tester.selectOption("pid", "Canine");
        tester.submit("Next >>");
        tester.submit("Next >>");
        tester.submit("Move");
        text.assertTextPresent("DOG-1");

        // COW-1 will redirect to the new URL
        navigation.gotoPage("browse/COW-1");
        text.assertTextPresent("DOG-1");
        text.assertTextPresent("Three legged dog");

        // Now edit the COW project
        Long projectId = backdoor.project().getProjectId("COW");
        tester.gotoPage("/secure/project/EditProject!default.jspa?pid=" + projectId);
        tester.setFormElement("description", "Some description");
        tester.submit("Update");

        // The bug was that the pcounter got reset to 0.
        // add a new issue
        navigation.issue().createIssue("Bovine", "Bug", "I like cheese");
        // This should be created as COW-3
        text.assertTextNotPresent("COW-1");
        text.assertTextPresent("COW-3");
        text.assertTextPresent("I like cheese");

        // COW-1 should still redirect to DOG-1
        navigation.gotoPage("browse/COW-1");
        text.assertTextPresent("DOG-1");
        text.assertTextPresent("Three legged dog");
    }
}
