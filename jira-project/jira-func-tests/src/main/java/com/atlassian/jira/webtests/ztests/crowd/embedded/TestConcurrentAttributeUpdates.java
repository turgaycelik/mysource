package com.atlassian.jira.webtests.ztests.crowd.embedded;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.rules.RestRule;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;

import com.meterware.httpunit.WebResponse;

import org.xml.sax.SAXException;

/**
 * Testing the behaviour of the crowd user attribues when we login with multiple concurrent clients
 *
 * Under the earlier implementation this would cause rows to be duplicated in the table.
 *
 * @since v5.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST, Category.SECURITY })
public class TestConcurrentAttributeUpdates extends FuncTestCase
{
    public static final String REST_AUTH_RESOURCE = "/rest/auth/latest/session";

    private JSONObject fredGoodCredentials;
    private RestRule restRule;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        restRule = new RestRule(this);
        restRule.before();
        administration.restoreBlankInstance();

        try
        {
            fredGoodCredentials = new JSONObject();
            fredGoodCredentials.put("username", FunctTestConstants.FRED_USERNAME);
            fredGoodCredentials.put("password", FunctTestConstants.FRED_PASSWORD);
        }
        catch (JSONException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void tearDownTest()
    {
        restRule.after();
    }

    public void testConcurrentLogin() throws Exception
    {
        // Login once to get a clean set of attributes for Fred.
        navigation.login(FunctTestConstants.FRED_USERNAME, FunctTestConstants.FRED_PASSWORD);

        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++)
        {
            executor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    WebResponse response = null;
                    try
                    {
                        response = loginAs(fredGoodCredentials);
                        log.log("Logging on concurrently");
                    }
                    catch (IOException e)
                    {
                        fail(e.getMessage());
                    }
                    catch (SAXException e)
                    {
                        fail(e.getMessage());
                    }
                    assertEquals(200, response.getResponseCode());
                }
            });
        }

        // Now look at the attributes and see what we have.
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
        List rows = backdoor.entityEngine().findByAnd("UserAttribute", EasyMap.build("userId", Long.valueOf(10010), "name", "login.count"));
        assertEquals(1, rows.size());
    }

    protected WebResponse loginAs(JSONObject json) throws IOException, SAXException
    {
        return restRule.POST(REST_AUTH_RESOURCE, json);
    }

}
