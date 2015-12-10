package com.atlassian.jira.webtests.ztests.issue.security.xsrf;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.apache.commons.lang.StringUtils;

import static com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation.NavigatorEditMode.ADVANCED;

/**
 * A test class that contains specific links for XSRF protection
 */
@WebTest ({ Category.FUNC_TEST, Category.SECURITY, Category.ISSUES })
public class TestXsrfSpecificLinks extends FuncTestCase
{

    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestXsrfSpecificLinks.xml");
    }

    public void testLinksThatShouldNOTHaveAtlTokens()
    {
        // issue linking should have no atlToken
        navigation.issue().gotoIssue("HSP-1");
        assertAnchorsDoNotHaveToken("//dl[contains(@class, 'links-list')]//dd//span//a/@href");

        // the issue tabs should not have tokens
        assertAnchorsDoNotHaveToken("//ul[@id='issue-tabs']//a/@href");

        // browse project tabs should not have tokens
        navigation.browseProject("HSP");
        assertAnchorsDoNotHaveToken("//section[@id='content']//ul/li/a/@href");

        // search history should not have tokens
        navigation.issueNavigator().createSearch("project=HSP");
        navigation.issueNavigator().createSearch("project=HSP and summary ~ fred");
        navigation.issueNavigator().createSearch("project=HSP and summary ~ bill");
    }

    private void assertAnchorsDoNotHaveToken(final String xPath)
    {
        String allHrefs = new XPathLocator(tester, xPath).getText();
        assertTrue(StringUtils.isNotBlank(allHrefs));
        text.assertTextNotPresent(allHrefs, "atl_token");
    }

}
