package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.testkit.client.restclient.Status;
import com.atlassian.jira.testkit.client.restclient.StatusClient;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.List;

/**
 * Func tests for StatusResource.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestStatusResource extends RestFuncTest
{
    /**
     * The id of a status that is visible by admin, but not by fred.
     */
    private static final String STATUS_ID = "10000";
    private static final String STATUS_NAME = "Insane";
    private static final String STATUS_NAME_TRANSLATED = "NotStarted";

    private StatusClient statusClient;

    /**
     * Verifies that the user is able to retrieve a visible status.
     *
     * @throws Exception if anything goes wrong
     */
    public void testStatusReturned() throws Exception
    {
        // this is what we expect:
        //
        // {
        //   "self": "http://localhost:8090/jira/rest/api/2/status/10000",
        //   "description": "Custom status",
        //   "iconUrl": "http://localhost:8090/jira/images/icons/statuses/generic.png",
        //   "name": "Insane"
        // }

        Status status = statusClient.get(STATUS_ID);
        assertEquals(getBaseUrlPlus("rest/api/2/status/" + STATUS_ID), status.self());
        assertEquals("Custom status", status.description());
        assertEquals(getBaseUrlPlus("images/icons/statuses/generic.png"), status.iconUrl());
        assertEquals("Insane", status.name());
        assertEquals(STATUS_ID, status.id());

        // And test also by name
        status = statusClient.get(STATUS_NAME);
        assertEquals(getBaseUrlPlus("rest/api/2/status/" + STATUS_ID), status.self());
        assertEquals("Custom status", status.description());
        assertEquals(getBaseUrlPlus("images/icons/statuses/generic.png"), status.iconUrl());
        assertEquals("Insane", status.name());
        assertEquals(STATUS_ID, status.id());

        // And test also by translated name
        status = statusClient.get(STATUS_NAME_TRANSLATED);
        assertEquals(getBaseUrlPlus("rest/api/2/status/" + "1"), status.self());
        assertEquals("Translated version of Open", status.description());
        assertEquals(getBaseUrlPlus("images/icons/statuses/open.png"), status.iconUrl());
        assertEquals("NotStarted", status.name());
        assertEquals("1", status.id());

        // And test also by untranslated name where there is a translation present
        status = statusClient.get("Open");
        assertEquals(getBaseUrlPlus("rest/api/2/status/" + "1"), status.self());
        assertEquals("Translated version of Open", status.description());
        assertEquals(getBaseUrlPlus("images/icons/statuses/open.png"), status.iconUrl());
        assertEquals("NotStarted", status.name());
        assertEquals("1", status.id());
    }

    /**
     * Get all statuses
     *
     * @throws Exception if anything goes wrong
     */
    public void testAllStatuses() throws Exception
    {
        List<Status> statuses = statusClient.get();
        assertEquals(6, statuses.size());
        assertStatusesContain(statuses, "1");
        assertStatusesContain(statuses, "3");
        assertStatusesContain(statuses, "4");
        assertStatusesContain(statuses, "5");
        assertStatusesContain(statuses, "6");
        assertStatusesContain(statuses, "10000");
    }

    private void assertStatusesContain(List<Status> statuses, String id)
    {
        for (Status status : statuses)
        {
            if (status.id().equals(id))
            {
                return;
            }
        }
        fail("Status " + id + " not in list");
    }


    /**
     * Verifies that the user is not able to see a status that is not active on any of his projects.
     *
     * @throws Exception if anything goes wrong
     */
    public void testStatusFilteredByPermissions() throws Exception
    {
        Response response = statusClient.loginAs(FRED_USERNAME).getResponse(STATUS_ID);
        assertEquals(404, response.statusCode);

        response = statusClient.loginAs(FRED_USERNAME).getResponse(STATUS_NAME);
        assertEquals(404, response.statusCode);

        List<Status> statuses = statusClient.get();
        assertEquals(5, statuses.size());
        assertStatusesContain(statuses, "1");
        assertStatusesContain(statuses, "3");
        assertStatusesContain(statuses, "4");
        assertStatusesContain(statuses, "5");
        assertStatusesContain(statuses, "6");
    }

    public void testStatusDoesntExist() throws Exception
    {
        // {"errorMessages":["The status with id '123' does not exist"],"errors":[]}
        Response resp123 = statusClient.getResponse("123");
        assertEquals(404, resp123.statusCode);
        assertEquals("The status with id '123' does not exist", resp123.entity.errorMessages.get(0));

        // {"errorMessages":["The status with id 'abc' does not exist"],"errors":[]}
        Response respAbc = statusClient.getResponse("abc");
        assertEquals(404, respAbc.statusCode);
        assertEquals("The status with id 'abc' does not exist", respAbc.entity.errorMessages.get(0));
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        statusClient = new StatusClient(getEnvironmentData());
        administration.restoreData("TestStatusResource.xml");
    }
}
