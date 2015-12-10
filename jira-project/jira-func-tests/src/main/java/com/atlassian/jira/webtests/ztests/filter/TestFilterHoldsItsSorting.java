package com.atlassian.jira.webtests.ztests.filter;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.apache.commons.lang.StringUtils;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * A test for JRA-15570.  The sort order of searches with a free text parameter in it where being lost when it passed
 * through the cache.
 *
 * @since v4.0
 */
@WebTest ( { Category.FUNC_TEST, Category.FILTERS })
public class TestFilterHoldsItsSorting extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreData("TestFilterHoldsItsSorting.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testThatItHoldsItSorting()
    {
        // This filter has a free text search in it for the word "bug"
        navigation.issueNavigator().loadFilter(10000, null);
        assertNavigatorKeyOrder(new String[] { "HSP-2", "HSP-4", "HSP-1", "HSP-3" });

        // ok change the sorting to be by key
        tester.gotoPage("secure/IssueNavigator.jspa?sorter/field=issuekey&sorter/order=ASC'");
        assertNavigatorKeyOrder(new String[] { "HSP-1", "HSP-2", "HSP-3", "HSP-4" });

        // now go back to the saved version
        navigation.issueNavigator().loadFilter(10000, null);
        assertNavigatorKeyOrder(new String[] { "HSP-2", "HSP-4", "HSP-1", "HSP-3" });
    }

    private void assertNavigatorKeyOrder(final String[] issueKeys)
    {
        TableLocator tableLocator = new TableLocator(tester, "issuetable");

        for (int i = 0; i < issueKeys.length; i++)
        {
            String key = issueKeys[i];
            assertTrue(contains(tableLocator.getTable().getCellAsText(i + 1, 1), key));
        }
    }

    private boolean contains(final String cellAsText, final String s)
    {
        return !StringUtils.isBlank(cellAsText) && cellAsText.contains(s);
    }
}
