package com.atlassian.jira.webtests.ztests.imports.properties;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.SearchRequest;
import com.atlassian.jira.testkit.client.restclient.SearchResult;

/**
 * Test that import with entity properties work. See https://jira.atlassian.com/browse/JRA-39902 for more details.
 *
 * @since v7.0
 */
@WebTest ({ Category.FUNC_TEST, Category.ADMINISTRATION, Category.IMPORT_EXPORT })
public class TestImportWithEntityProperties extends FuncTestCase
{
    public void testImportWithIssuePropertiesInside() throws Exception
    {
        administration.restoreDataWithPluginsReload("TestImportWithIssueProperties.xml");
        final SearchRequest searchRequest = new SearchRequest();
        searchRequest.jql("issue.property['foo.bar'].foo1.bar2='baz3'");
        final SearchResult search = backdoor.search().getSearch(searchRequest);
        assertEquals("there should be exactly one issue in result", 1, (int) search.total);
    }

    public void tearDownTest()
    {
        //just in case there is a problem so other tests do not fail hopefully
        backdoor.plugins().enablePlugin("com.atlassian.jira.dev.reference-plugin");
    }
}
