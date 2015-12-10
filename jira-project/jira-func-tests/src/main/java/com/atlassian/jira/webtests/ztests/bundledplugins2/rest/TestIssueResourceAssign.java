package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Errors;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.testkit.client.restclient.User;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@WebTest ( { Category.FUNC_TEST, Category.REST })
public class TestIssueResourceAssign extends RestFuncTest
{
    private static final String ADMIN = "admin";
    private static final String FRY = "fry";
    private static final String FARNSWORTH = "farnsworth";
    private IssueClient issueClient;

    public void testAssignPermission() throws Exception
    {
        administration.restoreData("TestAssignIssue.xml");

        // Joe does not have edit permission but he does have assign permission
        Response response = issueClient.loginAs("joe", "joe").assign("TST-1", new User().name(FRY));
        assertEquals(NO_CONTENT.getStatusCode(), response.statusCode);
        assertThat(issueClient.get("TST-1").fields.assignee.name, equalTo(FRY));

        // Bill does not have edit or assign permission
        response = issueClient.loginAs("bill", "bill").assign("TST-1", new User().name(FRY));
        assertEquals(FORBIDDEN.getStatusCode(), response.statusCode);
    }

    public void testAssign() throws Exception
    {
        administration.restoreData("TestAssignIssue.xml");

        // Assign in turn to admin, fry, farnsworth, unassigned and auto
        Response response = issueClient.assign("TST-1", new User().name(ADMIN));
        assertEquals(NO_CONTENT.getStatusCode(), response.statusCode);
        assertThat(issueClient.get("TST-1").fields.assignee.name, equalTo(ADMIN));

        issueClient.assign("TST-1", new User().name(FRY));
        assertThat(issueClient.get("TST-1").fields.assignee.name, equalTo(FRY));

        issueClient.assign("TST-1", new User().name(FARNSWORTH));
        assertThat(issueClient.get("TST-1").fields.assignee.name, equalTo(FARNSWORTH));

        issueClient.assign("TST-1", new User().name(null));
        assertThat(issueClient.get("TST-1").fields.assignee, equalTo(null));

        issueClient.assign("TST-1", new User().name("-1"));
        assertThat(issueClient.get("TST-1").fields.assignee.name, equalTo(ADMIN));
    }

    public void testAssignErrors() throws Exception
    {
        administration.restoreData("TestAssignIssue.xml");
        // Issue does not exist
        Response response = issueClient.assign("TST-19", new User().name(ADMIN));
        assertEquals(NOT_FOUND.getStatusCode(), response.statusCode);
        // user unassignable
        response = issueClient.assign("TST-1", new User().name("joe"));
        assertEquals(BAD_REQUEST.getStatusCode(), response.statusCode);
        assertEquals(new Errors().addError("assignee", "User 'joe' cannot be assigned issues."), response.entity);
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
    }
}
