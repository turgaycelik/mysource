package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Priority;
import com.atlassian.jira.testkit.client.restclient.PriorityClient;
import com.atlassian.jira.testkit.client.restclient.Response;

import java.util.List;

/**
 * Func tests for PriorityResource.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestPriorityResource extends RestFuncTest
{
    private static final String PRIORITY_ID = "1";

    private PriorityClient priorityClient;

    public void testAllPriorities() throws Exception
    {
        List<Priority> priorities = priorityClient.get();

        assertprioritiesContain(priorities, "1");
        assertprioritiesContain(priorities, "2");
        assertprioritiesContain(priorities, "3");
        assertprioritiesContain(priorities, "4");
        assertprioritiesContain(priorities, "5");
    }

    private void assertprioritiesContain(List<Priority> priorities, String id)
    {
        for (Priority priority : priorities)
        {
            if (priority.id().equals(id))
            {
                return;
            }
        }
        fail("Priority " + id + " not in list");
    }


    public void testViewPriority() throws Exception
    {
        Priority priority = priorityClient.get(PRIORITY_ID);

        assertEquals(getBaseUrlPlus("rest/api/2/priority/" + PRIORITY_ID), priority.self());
        assertEquals("#cc0000", priority.statusColor());
        assertEquals("Blocks development and/or testing work, production could not run.", priority.description());
        assertEquals(getBaseUrlPlus("images/icons/priorities/blocker.png"), priority.iconUrl());
        assertEquals("Blocker", priority.name());
        assertEquals(PRIORITY_ID, priority.id());
    }

    public void testViewPriorityNotFound() throws Exception
    {
        // {"errorMessages":["The priority with id '123' does not exist"],"errors":[]}
        Response resp123 = priorityClient.getResponse("123");
        assertEquals(404, resp123.statusCode);
        assertEquals(1, resp123.entity.errorMessages.size());
        assertTrue(resp123.entity.errorMessages.contains("The priority with id '123' does not exist"));

        // {"errorMessages":["The priority with id 'foo' does not exist"],"errors":[]}
        Response respFoo = priorityClient.getResponse("foo");
        assertEquals(404, respFoo.statusCode);
        assertTrue(respFoo.entity.errorMessages.contains("The priority with id 'foo' does not exist"));
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        priorityClient = new PriorityClient(getEnvironmentData());
        administration.restoreBlankInstance();
    }
}
