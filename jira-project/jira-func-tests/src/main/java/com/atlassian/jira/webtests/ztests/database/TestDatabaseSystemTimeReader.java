package com.atlassian.jira.webtests.ztests.database;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.RestFuncTest;

import com.meterware.httpunit.WebResponse;

/**
 * Functional tests for reading database system time.
 *
 * @since 6.3
 */
@WebTest ({ Category.FUNC_TEST, Category.REST, Category.DATABASE })
public class TestDatabaseSystemTimeReader extends RestFuncTest
{
    private static final String REST_DBTIME_RESOURCE = "/rest/func-test/latest/databaseSystemTime";

    private static final int RESPONSE_OK = HttpURLConnection.HTTP_OK;

    /**
     * Can't make too many guarantees as to what different databases will return for the system time - it is not necessarily
     * number of millis since epoch.  But it should increase over time unless there are problems.  The test makes two
     * calls to read the database system time with one second delay between them and ensures that the system time is increasing.
     *
     * @throws Exception if an error occurs.
     */
    public void testThatDatabaseSystemTimeIncreasesOverTime() throws Exception
    {
        long time1 = readDatabaseTime();
        Thread.sleep(1000L); //one second should be enough for coarseness of any database's system clock
        long time2 = readDatabaseTime();

        assertTrue("Database system time is not increasing (" + time1 + ", " + time2 + ").", time1 < time2);
    }

    private long readDatabaseTime()
    throws Exception
    {
        WebResponse response = GET(REST_DBTIME_RESOURCE);
        assertResponseOk(response);

        return Long.parseLong(response.getText());
    }

    private void assertResponseOk(WebResponse response)
    {
        //Try to extract as much information about a failure as possible so test error mean something
        String failMessage = "REST error response: " + response.getResponseMessage();
        try
        {
            String responseMessage = response.getText();
            failMessage = failMessage + " - " + responseMessage;
        }
        catch (IOException e)
        {
            failMessage = failMessage + " (" + IOException.class.getSimpleName() + " getting response: " + e.toString() + ")";
        }
        assertEquals(failMessage, RESPONSE_OK, response.getResponseCode());
    }
}
