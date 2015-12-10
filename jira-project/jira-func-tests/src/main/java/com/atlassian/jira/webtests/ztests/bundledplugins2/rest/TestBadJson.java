package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebResponse;

import static org.junit.Assert.assertThat;

/**
 * Testing for the a 400 response for bad Json string
 *
 * @since v5.0
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestBadJson extends RestFuncTest
{

    private String badJson = "{ fields: { \"project\" : { \"key\" : \"TST\" } }}";

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();

    }

    public void testBadJsonGives400() throws Exception
    {
        WebResponse response = POST("/rest/api/2/issue", badJson);
        assertEquals(400, response.getResponseCode());
        assertTrue(response.getText().contains("Unexpected character ('f'"));
    }
}
