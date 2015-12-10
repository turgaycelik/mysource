package com.atlassian.jira.webtests.ztests.favourite;

import com.atlassian.jira.functest.framework.Dashboard.Table;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.parser.filter.FilterItem;
import com.atlassian.jira.functest.framework.parser.filter.FilterList;
import com.atlassian.jira.functest.framework.parser.filter.FilterParser;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;

/**
 * Test the favourite/unfavourite functionality exposed by the AdjustFavourite action.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestAdjustFavourite extends FuncTestCase
{
    private static final String TYPE_DASHBOARD = "PortalPage";
    private static final String TYPE_FILTER = "SearchRequest";

    private static final String USER_FRED = FRED_USERNAME;

    private static final long ID_FILTER_PUBLIC = 10000L;
    private static final long ID_FILTER_PRIVATE = 10001L;

    private static final long ID_DASHBOARD_PUBLIC = 10011L;
    private static final long ID_DASHBOARD_PRIVATE = 10012L;

    protected void setUpTest()
    {
        administration.restoreData("TestAdjustFavourite.xml");
    }

    /**
     * Make sure that adding a favourite works.
     */
    public void testAddFavourite()
    {
        navigation.logout();
        navigation.login(USER_FRED);

        //make sure favouriting a filter works.
        addFavourite(TYPE_FILTER, ID_FILTER_PUBLIC);
        assertions.getJiraFormAssertions().assertNoErrorsPresent();
        assertFilterIsFavourite(ID_FILTER_PUBLIC);

        //make sure favouriting a dashboard works.
        addFavourite(TYPE_DASHBOARD, ID_DASHBOARD_PUBLIC);
        assertions.getJiraFormAssertions().assertNoErrorsPresent();
        assertDashboardIsFavourite(ID_DASHBOARD_PUBLIC);
    }

    /**
     * Make sure that redirect works as expected.
     */
    public void testTestRedirect()
    {
        //we should follow the URL passed to the request.
        String expectedUrl = "ManageFilters.jspa?filterView=popular";

        addFavourite(TYPE_FILTER, ID_FILTER_PUBLIC, expectedUrl);
        String actualUrl = tester.getDialog().getResponse().getURL().toExternalForm();
        assertTrue("Expected to get redirected to '" + actualUrl + "' but we ended up at '" + actualUrl + "'.", actualUrl.endsWith(expectedUrl));


        //if we don't specify a URL, we should be redirected back to the dashboard.
        expectedUrl = "Dashboard.jspa";

        addFavourite(TYPE_DASHBOARD, ID_DASHBOARD_PUBLIC);
        actualUrl = tester.getDialog().getResponse().getURL().toExternalForm();
        assertTrue("Expected to get redirected to '" + actualUrl + "' ", actualUrl.endsWith(expectedUrl));
    }

    /**
     * Make sure we don't add a favourite for an entity that the user cannot see.
     */
    public void testCantAddFavouriteUserCantSee()
    {
        navigation.logout();
        navigation.login(USER_FRED);

        addFavourite(TYPE_DASHBOARD, ID_DASHBOARD_PRIVATE);
        assertions.getJiraFormAssertions().assertFormErrMsg("Unable to find the requested entity, it does not exist or you don't have permission to see it.");

        addFavourite(TYPE_FILTER, ID_FILTER_PRIVATE);
        assertions.getJiraFormAssertions().assertFormErrMsg("Unable to find the requested entity, it does not exist or you don't have permission to see it.");
    }

    /**
     * Make sure we don't add a favourite for an entity that does not exist.
     */
    public void testCantAddFavouriteThatDoesNotExist()
    {
        navigation.logout();
        navigation.login(USER_FRED);

        addFavourite(TYPE_DASHBOARD, 9839054L);
        assertions.getJiraFormAssertions().assertFormErrMsg("Unable to find the requested entity, it does not exist or you don't have permission to see it.");

        addFavourite(TYPE_FILTER, 547485934L);
        assertions.getJiraFormAssertions().assertFormErrMsg("Unable to find the requested entity, it does not exist or you don't have permission to see it.");
    }

    /**
     * Test that the action ignores bad parameters.
     */
    public void testBadParameters()
    {
        navigation.logout();
        navigation.login(USER_FRED);

        addFavourite(TYPE_DASHBOARD, -1);
        assertions.getJiraFormAssertions().assertFormErrMsg("No entity was specified.");

        addFavourite(null, ID_DASHBOARD_PRIVATE);
        assertions.getJiraFormAssertions().assertFormErrMsg("No entity type was specified.");

        addFavourite("FHDJS", ID_DASHBOARD_PRIVATE);
        assertions.getJiraFormAssertions().assertFormErrMsg("The entity type 'FHDJS' is not known to the system.");
    }

    private void addFavourite(final String entityType, final long entityId)
    {
        addFavourite(entityType, entityId, null);
    }

    private void addFavourite(final String entityType, final long entityId, final String returnUrl)
    {
        final StringBuilder buffer = new StringBuilder(page.addXsrfToken("/secure/AddFavourite.jspa?ignore=me"));
        if (StringUtils.isNotBlank(entityType))
        {
            buffer.append("&entityType=").append(entityType);
        }
        if (entityId >= 0)
        {
            buffer.append("&entityId=").append(entityId);
        }
        if (StringUtils.isNotBlank(returnUrl))
        {
            buffer.append("&returnUrl=").append(encode(returnUrl));
        }

        tester.gotoPage(buffer.toString());
    }

    private void assertDashboardIsFavourite(final long pageId)
    {
        navigation.dashboard().navigateToFavourites();
        final SharedEntityInfo info = new SharedEntityInfo(pageId, null, null, true, null);
        assertions.getDashboardAssertions().assertDashboardPages(Collections.singletonList(info), Table.FAVOURITE);
    }

    private void assertFilterIsFavourite(final long filterId)
    {
        navigation.manageFilters().favouriteFilters();
        final FilterList list = parse.filter().parseFilterList(FilterParser.TableId.FAVOURITE_TABLE);
        for (FilterItem filterItem : list.getFilterItems())
        {
            if (filterItem.getId() == filterId)
            {
                return;
            }
        }
        fail("Expected filter '" + filterId + "' to be a favourite but it does not appear to be.");
    }

    private static String encode(final String data)
    {
        try
        {
            return URLEncoder.encode(data, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }
}
