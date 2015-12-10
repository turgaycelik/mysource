package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.io.IOException;

/**
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.API })
public class TestOpenSearchProvider extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    public void testOpenSearch() throws IOException
    {
        navigation.gotoPage("/osd.jsp");
        String responseText = tester.getDialog().getResponse().getText();
        assertTrue(responseText.contains("<ShortName>jWebTest JIRA installation</ShortName>"));
        assertTrue(responseText.contains("<Description>Atlassian JIRA Search Provider</Description>"));
        assertTrue(responseText.contains("/secure/QuickSearch.jspa?searchString={searchTerms}"));
    }
}
