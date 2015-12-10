package com.atlassian.jira.webtests.ztests.dashboard;

import com.atlassian.jira.functest.framework.Dashboard;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.assertions.DashboardAssertions;
import com.atlassian.jira.functest.framework.dashboard.DashboardPageInfo;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.sharing.TestSharingPermissionUtils;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test to ensure that Manage Dashboard display correct information
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.DASHBOARDS })
public class TestManageDashboardPages extends FuncTestCase
{
    private static final DashboardPageInfo PAGE_FRED_PUBLIC = new DashboardPageInfo(10011, "PublicFredDashboard", "This is a dashboard page that can be seen by everyone.", false, TestSharingPermissionUtils.createPublicPermissions(), FRED_USERNAME, 1, DashboardPageInfo.Operation.COPY_ONLY);
    private static final DashboardPageInfo PAGE_EXISTS = new DashboardPageInfo(10012, "Exists", null, true, TestSharingPermissionUtils.createPrivatePermissions(), ADMIN_USERNAME, 1, DashboardPageInfo.Operation.ALL);
    private static final DashboardPageInfo PAGE_ADMINNOTFAVOURITE = new DashboardPageInfo(10013, "AdminNotFavourite", null, false, TestSharingPermissionUtils.createPublicPermissions(), ADMIN_USERNAME, 0, DashboardPageInfo.Operation.ALL);
    private static final DashboardPageInfo PAGE_ADMINFAVOURITE = new DashboardPageInfo(10014, "AdminFavourite", null, true, TestSharingPermissionUtils.createPublicPermissions(), ADMIN_USERNAME, 1, DashboardPageInfo.Operation.ALL);
    private static final DashboardPageInfo PAGE_SYSTEM_DASHBOARD = new DashboardPageInfo(10000, "System Dashboard", null, false, TestSharingPermissionUtils.createPublicPermissions(), null, 0, DashboardPageInfo.Operation.ALL);

    protected void setUpTest()
    {
        administration.restoreData("BaseProfessionalPortalPage.xml");
    }

    /**
     * Lets test the tab view as the admin user.
     */
    public void testCorrectDashboardsOnTabsAdmin()
    {
        final DashboardAssertions dashboardAssertions = assertions.getDashboardAssertions();

        navigation.dashboard().navigateToFavourites();
        dashboardAssertions.assertColumns(Arrays.asList("Name", "Owner", "Shared With", "", "" /* Operations */), new XPathLocator(tester, Dashboard.Table.FAVOURITE.toXPath()));
        dashboardAssertions.assertDashboardPages(makeFavourites(Arrays.asList(PAGE_EXISTS, PAGE_ADMINFAVOURITE)), Dashboard.Table.FAVOURITE);

        navigation.dashboard().navigateToMy();
        dashboardAssertions.assertColumns(Arrays.asList("Name", "Shared With", "" /* Operations */), new XPathLocator(tester, Dashboard.Table.OWNED.toXPath()));
        dashboardAssertions.assertDashboardPages(makeMy(Arrays.asList(PAGE_ADMINFAVOURITE, PAGE_ADMINNOTFAVOURITE, PAGE_EXISTS)), Dashboard.Table.OWNED);

        navigation.dashboard().navigateToPopular();
        dashboardAssertions.assertColumns(Arrays.asList("Name", "Owner", "Shared With", "Popularity"), new XPathLocator(tester, Dashboard.Table.POPULAR.toXPath()));
        dashboardAssertions.assertDashboardPages(makePopular(Arrays.asList(PAGE_ADMINFAVOURITE, PAGE_EXISTS, PAGE_FRED_PUBLIC, PAGE_ADMINNOTFAVOURITE, PAGE_SYSTEM_DASHBOARD)), Dashboard.Table.POPULAR);

        navigation.dashboard().favouriteDashboard(PAGE_ADMINNOTFAVOURITE.getId());
        PAGE_ADMINNOTFAVOURITE.setFavCount(1);
        PAGE_ADMINNOTFAVOURITE.setFavourite(true);

        navigation.dashboard().unFavouriteDashboard(PAGE_ADMINFAVOURITE.getId());
        PAGE_ADMINFAVOURITE.setFavourite(false);
        PAGE_ADMINFAVOURITE.setFavCount(0);

        navigation.dashboard().navigateToFavourites();
        dashboardAssertions.assertColumns(Arrays.asList("Name", "Owner", "Shared With", "", "") /* Operations */, new XPathLocator(tester, Dashboard.Table.FAVOURITE.toXPath()));
        dashboardAssertions.assertDashboardPages(makeFavourites(Arrays.asList(PAGE_EXISTS, PAGE_ADMINNOTFAVOURITE)), Dashboard.Table.FAVOURITE);

        navigation.dashboard().navigateToMy();
        dashboardAssertions.assertColumns(Arrays.asList("Name", "Shared With", "" /* Operations */), new XPathLocator(tester, Dashboard.Table.OWNED.toXPath()));
        dashboardAssertions.assertDashboardPages(makeMy(Arrays.asList(PAGE_ADMINFAVOURITE, PAGE_ADMINNOTFAVOURITE, PAGE_EXISTS)), Dashboard.Table.OWNED);

        navigation.dashboard().navigateToPopular();
        dashboardAssertions.assertColumns(Arrays.asList("Name", "Owner", "Shared With", "Popularity"), new XPathLocator(tester, Dashboard.Table.POPULAR.toXPath()));
        dashboardAssertions.assertDashboardPages(makePopular(Arrays.asList(PAGE_ADMINNOTFAVOURITE, PAGE_EXISTS, PAGE_FRED_PUBLIC, PAGE_ADMINFAVOURITE, PAGE_SYSTEM_DASHBOARD)), Dashboard.Table.POPULAR);

        navigation.dashboard().favouriteDashboard(PAGE_FRED_PUBLIC.getId());
        PAGE_FRED_PUBLIC.setFavourite(true);
        PAGE_FRED_PUBLIC.setFavCount(2);

        navigation.dashboard().navigateToFavourites();
        dashboardAssertions.assertColumns(Arrays.asList("Name", "Owner", "Shared With", "", "" /* Operations */), new XPathLocator(tester, Dashboard.Table.FAVOURITE.toXPath()));
        dashboardAssertions.assertDashboardPages(makeFavourites(Arrays.asList(PAGE_EXISTS, PAGE_ADMINNOTFAVOURITE, PAGE_FRED_PUBLIC)), Dashboard.Table.FAVOURITE);

        navigation.dashboard().navigateToMy();
        dashboardAssertions.assertColumns(Arrays.asList("Name", "Shared With", "" /* Operations */), new XPathLocator(tester, Dashboard.Table.OWNED.toXPath()));
        dashboardAssertions.assertDashboardPages(makeMy(Arrays.asList(PAGE_ADMINFAVOURITE, PAGE_ADMINNOTFAVOURITE, PAGE_EXISTS)), Dashboard.Table.OWNED);

        navigation.dashboard().navigateToPopular();
        dashboardAssertions.assertColumns(Arrays.asList("Name", "Owner", "Shared With", "Popularity"), new XPathLocator(tester, Dashboard.Table.POPULAR.toXPath()));
        dashboardAssertions.assertDashboardPages(makePopular(Arrays.asList(PAGE_FRED_PUBLIC, PAGE_ADMINNOTFAVOURITE, PAGE_EXISTS, PAGE_ADMINFAVOURITE, PAGE_SYSTEM_DASHBOARD)), Dashboard.Table.POPULAR);
    }

    private List<DashboardPageInfo> makeFavourites(final List<DashboardPageInfo> dashboards)
    {
        final List<DashboardPageInfo> returnInfo = new ArrayList<DashboardPageInfo>(dashboards.size());
        CollectionUtils.collect(dashboards, ChainedTransformer.getInstance(new DashboardCopy(), new RemoveFavouriteCount()), returnInfo);
        return returnInfo;
    }

    private List<DashboardPageInfo> makeMy(final List<DashboardPageInfo> dashboards)
    {
        final List<DashboardPageInfo> returnInfo = new ArrayList<DashboardPageInfo>(dashboards.size());
        CollectionUtils.collect(dashboards, ChainedTransformer.getInstance(new Transformer[]{new DashboardCopy(), new RemoveFavouriteCount(), new RemoveAuthor()}), returnInfo);
        return returnInfo;
    }

    private List<DashboardPageInfo> makePopular(final List<DashboardPageInfo> dashboards)
    {
        final List<DashboardPageInfo> returnInfo = new ArrayList<DashboardPageInfo>(dashboards.size());
        CollectionUtils.collect(dashboards, ChainedTransformer.getInstance(new DashboardCopy(), new RemoveOperations()), returnInfo);
        return returnInfo;
    }

    private static class DashboardCopy implements Transformer
    {
        public Object transform(final Object pageInfoObject)
        {
            final DashboardPageInfo pageInfo = (DashboardPageInfo) pageInfoObject;
            return pageInfo.copy();
        }
    }

    private static class RemoveFavouriteCount implements Transformer
    {
        public Object transform(final Object pageInfoObject)
        {
            final DashboardPageInfo pageInfo = (DashboardPageInfo) pageInfoObject;
            return pageInfo.setFavCount(null);
        }
    }

    private static class RemoveAuthor implements Transformer
    {
        public Object transform(final Object pageInfoObject)
        {
            final DashboardPageInfo pageInfo = (DashboardPageInfo) pageInfoObject;
            return pageInfo.setOwner(null);
        }
    }

    private static class RemoveOperations implements Transformer
    {
        public Object transform(final Object pageInfoObject)
        {
            final DashboardPageInfo pageInfo = (DashboardPageInfo) pageInfoObject;
            return pageInfo.setOperations(null);
        }
    }
}
