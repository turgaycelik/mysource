package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueClient;

@WebTest ( { Category.FUNC_TEST, Category.REST })
public class TestIssueResourceNoPrioritySet extends RestFuncTest
{
    private IssueClient issueClient;

    public void testUnassignedIssueHasNoValue() throws Exception
    {
        final Issue issue = issueClient.get("TST-2");
        assertNull(issue.fields.priority);
        // testing it here too - to save time of func tests (every time restoring JIRA already takes too long)
        assertNull(issue.fields.reporter);

        final Issue issue2 = issueClient.get("TST-1");
        assertEquals(PRIORITY_BLOCKER, issue2.fields.priority.name());
        assertEquals(ADMIN_USERNAME, issue2.fields.reporter.name);

        administration.fieldConfigurations().defaultFieldConfiguration().hideFields(PRIORITY_FIELD_ID);
        assertNull(issueClient.get("TST-2").fields.priority);
        assertNull(issueClient.get("TST-1").fields.priority);

        // now testing something similar - Due Date built-in field (again to save time on restoring JIRA)

        assertNull(issueClient.get("TST-1").fields.duedate);

        administration.fieldConfigurations().defaultFieldConfiguration().hideFields(DUE_DATE_FIELD_ID);
        assertNull(issueClient.get("TST-1").fields.duedate);

        administration.fieldConfigurations().defaultFieldConfiguration().showFields(DUE_DATE_FIELD_ID);
        navigation.issue().setDueDate("TST-1", "10/May/11");
        assertTrue(issueClient.get("TST-1").fields.duedate.startsWith("2011-05-")); // not going into further details due to possible timezone issues (irrelevant here)

    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestIssueResourceNoPrioritySet.xml");
        issueClient = new IssueClient(getEnvironmentData());
    }
}
