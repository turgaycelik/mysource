package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.api.dashboard.DashboardBean;
import com.atlassian.jira.rest.api.dashboard.DashboardsBean;
import com.atlassian.jira.testkit.client.restclient.DashboardClient;
import com.atlassian.jira.testkit.client.restclient.Response;

import java.net.URI;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;
import static com.atlassian.jira.functest.matcher.URIMatcher.*;

/**
 * Func tests for the <code>/dashboard</code> resource.
 *
 * @since v5.0
 */
@WebTest ({ Category.FUNC_TEST, Category.DASHBOARDS })
public class TestDashboardResource extends RestFuncTest
{
    private static final int PAGE_SIZE = 2;

    private DashboardClient dashboardClient;

    public void testDashboardResourceShouldAllowGettingFavouriteDashboards() throws Exception
    {
        // ensure there is a useful validation on the value of the ?filter= query param
        Response badFilterResponse = dashboardClient.getListResponse("zzz", 0, -1);
        assertThat(badFilterResponse.statusCode, equalTo(400));
        assertThat(badFilterResponse.entity.errorMessages, hasItem("Bad value 'zzz' for query parameter 'filter'. Valid values are: 'favourite', 'my'"));

        final DashboardBean[] favouriteDashboards = new DashboardBean[] {
                createDashboardBean("10011", "Private Dashboard owned by admin"),
                createDashboardBean("10010", "Shared Dashboard with group jira-administrators owned by admin"),
                createDashboardBean("10019", "Shared Dashboard with role Users on homosapien owned by fred"),
        };

        // pass maxResults=0 to get dashboard count
        DashboardsBean noResultsBean = dashboardClient.getList("favourite", 0, 0);
        assertThat("clients should be able to pass maxResults=0 to get dashboard count and no results", noResultsBean.total(), equalTo(favouriteDashboards.length));
        assertThat("clients should be able to pass maxResults=0 to get dashboard count and no results", noResultsBean.dashboards().size(), equalTo(0));

        // get favourite dashboards on 1 page
        DashboardsBean favourites = dashboardClient.getList("favourite", 0, -1);
        assertThat(favourites.startAt(), equalTo(0));
        assertThat(favourites.total(), equalTo(favouriteDashboards.length));
        assertThat("clients should be able to pass ?filter=favourite to get only favourite dashboards", favourites.dashboards(), hasItems(favouriteDashboards));
        assertThat(favourites.dashboards().size(), equalTo(favouriteDashboards.length));

        // get favourite dashboards over 3 pages
        DashboardsBean page0 = dashboardClient.getList("favourite", 0, PAGE_SIZE);
        assertThat("page 0 should have 2 results", page0.dashboards().size(), equalTo(2));
        assertThat(page0.prev(), equalTo(null));
        assertThat(URI.create(page0.next()), isSameURI(getRestApiUri("/dashboard?startAt=2&filter=favourite&maxResults=" + PAGE_SIZE)));

        DashboardsBean page1 = dashboardClient.getList("favourite", 2, PAGE_SIZE);
        assertThat("page 1 should have 1 results", page1.dashboards().size(), equalTo(1));
        assertThat(URI.create(page1.prev()), isSameURI(getRestApiUri("/dashboard?startAt=0&filter=favourite&maxResults=" + PAGE_SIZE)));
        assertThat(page1.next(), equalTo(null));

        assertThat("page 2 should have 0 results", dashboardClient.getList("favourite", 4, PAGE_SIZE).dashboards().size(), equalTo(0));

        // make sure it doesn't fall over for anonymous users
        DashboardsBean anonymousFavourites = dashboardClient.anonymous().getList("favourite", 0, -1);
        assertThat(anonymousFavourites.total(), equalTo(0));
        assertThat(anonymousFavourites.dashboards(), equalTo(Collections.<DashboardBean>emptyList()));
    }

    public void testDashboardResourceShouldAllowGettingAllDashboards() throws Exception
    {
        final DashboardBean[] allDashboards = new DashboardBean[] {
                createDashboardBean("10011", "Private Dashboard owned by admin"),
                createDashboardBean("10013", "Shared Dashboard with all roles on Monkey owned by developer"),
                createDashboardBean("10014", "Shared Dashboard with Anyone owned by developer"),
                createDashboardBean("10010", "Shared Dashboard with group jira-administrators owned by admin"),
                createDashboardBean("10018", "Shared Dashboard with group jira-users owned by fred"),
                createDashboardBean("10019", "Shared Dashboard with role Users on homosapien owned by fred"),
                createDashboardBean("10000", "System Dashboard"),
        };

        // pass maxResults=0 to get dashboard count
        DashboardsBean noResultsBean = dashboardClient.getList("", 0, 0);
        assertThat(noResultsBean.total(), equalTo(allDashboards.length));
        assertThat(noResultsBean.dashboards().size(), equalTo(0));

        // get all dashboards on 1 page
        DashboardsBean all = dashboardClient.getList(null, 0, -1);
        assertThat(all.startAt(), equalTo(0));
        assertThat(all.total(), equalTo(allDashboards.length));
        assertThat(all.dashboards(), hasItems(allDashboards));
        assertThat(all.dashboards().size(), equalTo(allDashboards.length));

        // get all dashboards over 4 pages
        assertThat("page 0 should have 2 results", dashboardClient.getList("", 0, PAGE_SIZE).dashboards().size(), equalTo(2));
        assertThat("page 1 should have 2 results", dashboardClient.getList("", 2, PAGE_SIZE).dashboards().size(), equalTo(2));
        assertThat("page 2 should have 2 results", dashboardClient.getList("", 4, PAGE_SIZE).dashboards().size(), equalTo(2));
        assertThat("page 3 should have 1 results", dashboardClient.getList("", 6, PAGE_SIZE).dashboards().size(), equalTo(1));
        assertThat("page 4 should have 0 results", dashboardClient.getList("", 8, PAGE_SIZE).dashboards().size(), equalTo(0));

        // make sure it doesn't fall over for anonymous users
        DashboardsBean anonymousMy = dashboardClient.anonymous().getList("", 0, -1);
        assertThat(anonymousMy.total(), equalTo(2));
    }

    public void testDashboardResourceShouldAllowGettingMyDashboards() throws Exception
    {
        final DashboardBean[] myDashboards = new DashboardBean[] {
                createDashboardBean("10011", "Private Dashboard owned by admin"),
                createDashboardBean("10010", "Shared Dashboard with group jira-administrators owned by admin"),
        };

        // pass maxResults=0 to get dashboard count
        DashboardsBean noResultsBean = dashboardClient.getList("my", 0, 0);
        assertThat(noResultsBean.total(), equalTo(myDashboards.length));
        assertThat(noResultsBean.dashboards().size(), equalTo(0));

        // get my dashboards on 1 page
        DashboardsBean my = dashboardClient.getList("my", 0, -1);
        assertThat(my.startAt(), equalTo(0));
        assertThat(my.total(), equalTo(myDashboards.length));
        assertThat(my.dashboards(), hasItems(myDashboards));
        assertThat(my.dashboards().size(), equalTo(myDashboards.length));

        // get my dashboards over 4 pages
        assertThat("page 0 should have 2 results", dashboardClient.getList("my", 0, PAGE_SIZE).dashboards().size(), equalTo(2));
        assertThat("page 1 should have 0 results", dashboardClient.getList("my", 2, PAGE_SIZE).dashboards().size(), equalTo(0));

        // make sure it doesn't fall over for anonymous users
        DashboardsBean anonymousMy = dashboardClient.anonymous().getList("my", 0, -1);
        assertThat(anonymousMy.total(), equalTo(0));
        assertThat(anonymousMy.dashboards(), equalTo(Collections.<DashboardBean>emptyList()));
    }

    public void testDashboardResourceShouldAllowGettingASingleDashboard() throws Exception
    {
        Response fooResponse = dashboardClient.getSingleResponse("foo");
        assertThat(fooResponse.statusCode, equalTo(404));
        assertThat(fooResponse.entity.errorMessages, hasItem("The dashboard with id 'foo' does not exist."));

        Response minusOneResponse = dashboardClient.getSingleResponse("-1");
        assertThat(minusOneResponse.statusCode, equalTo(404));
        assertThat(minusOneResponse.entity.errorMessages, hasItem("The dashboard with id '-1' does not exist."));

        assertThat(dashboardClient.getSingle("10000"), equalTo(createDashboardBean("10000", "System Dashboard")));

        // make sure it doesn't fall over for anonymous users
        DashboardBean anonSystemDashboard = dashboardClient.anonymous().getSingle("10000");
        assertThat(anonSystemDashboard.name(), equalTo("System Dashboard"));

        Response anonPrivateDashboardResp = dashboardClient.anonymous().getSingleResponse("10010");
        assertThat(anonPrivateDashboardResp.statusCode, equalTo(404));
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestDashboardResource.xml");
        dashboardClient = new DashboardClient(getEnvironmentData());
    }

    private DashboardBean createDashboardBean(String id, String name)
    {
        return new DashboardBean().id(id).name(name).self(getRestApiUrl("/dashboard/" + id)).view(getBaseUrlPlus("/secure/Dashboard.jspa?selectPageId=" + id));
    }
}
