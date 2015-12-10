package com.atlassian.jira.issue.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.web.api.WebItem;
import com.atlassian.plugin.web.api.model.WebFragmentBuilder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static com.atlassian.jira.plugin.webfragment.EqWebItem.eqWebItem;
import static com.google.common.collect.Iterables.isEmpty;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith (ListeningMockitoRunner.class)
public class TestFavouriteFilterLinkFactory
{

    public static final String ISSUES_REPORTED_BY_ME = "issue.nav.filters.reported.by.me";
    public static final String MY_OPEN_ISSUES = "issue.nav.filters.my.open.issues";

    private final static WebItem MY_ISSUES = getWebItem(10, "filter_lnk_my", MY_OPEN_ISSUES, MY_OPEN_ISSUES, "/jira/issues/?filter=-1", "-1");
    private final static WebItem REPORTED_ISSUES = getWebItem(20, "filter_lnk_reported", ISSUES_REPORTED_BY_ME, ISSUES_REPORTED_BY_ME, "/jira/issues/?filter=-2", "-2");

    private final static WebItem MY_ISSUES_NO_BASE = getWebItem(10, "filter_lnk_my", MY_OPEN_ISSUES, MY_OPEN_ISSUES, "/issues/?filter=-1", "-1");
    private final static WebItem REPORTED_ISSUES_NO_BASE = getWebItem(20, "filter_lnk_reported", ISSUES_REPORTED_BY_ME, ISSUES_REPORTED_BY_ME, "/issues/?filter=-2", "-2");

    @Mock
    private SearchRequestService searchRequestService;
    @Mock
    private VelocityRequestContext requestContext;
    @Mock
    private VelocityRequestContextFactory requestContextFactory;
    @Mock
    private ApplicationProperties applicationProperties;
    @Mock
    private I18nHelper.BeanFactory i18nFactory;

    private ApplicationUser user;

    @Mock
    private FavouriteFilterLinkFactory linkFactory;

    @Before
    public void setUp() throws Exception
    {
        user = new MockApplicationUser("admin");
        linkFactory = new FavouriteFilterLinkFactory(searchRequestService, requestContextFactory, applicationProperties, i18nFactory);
    }

    @After
    public void tearDown() throws Exception
    {
        searchRequestService = null;
        requestContext = null;
        requestContextFactory = null;
        applicationProperties = null;
        linkFactory = null;
        user = null;
        i18nFactory = null;
    }

    @Test
    public void testNullFiltersNullUser()
    {
        when(searchRequestService.getFavouriteFilters((ApplicationUser) null)).thenReturn(null);
        when(requestContextFactory.getJiraVelocityRequestContext()).thenReturn(requestContext);
        when(i18nFactory.getInstance((User) null)).thenReturn(new MockI18nHelper());
        when(requestContext.getBaseUrl()).thenReturn("/jira");
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_FILTER_DROPDOWN_ITEMS)).thenReturn("10");

        assertTrue(isEmpty(linkFactory.getItems(Maps.<String, Object>newHashMap())));
    }


    @Test
    public void testEmptyFiltersNullUser()
    {
        when(searchRequestService.getFavouriteFilters((ApplicationUser) null)).thenReturn(Lists.<SearchRequest>newArrayList());
        when(requestContextFactory.getJiraVelocityRequestContext()).thenReturn(requestContext);
        when(i18nFactory.getInstance((User) null)).thenReturn(new MockI18nHelper());
        when(requestContext.getBaseUrl()).thenReturn("/jira");
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_FILTER_DROPDOWN_ITEMS)).thenReturn("10");

        assertTrue(isEmpty(linkFactory.getItems(Maps.<String, Object>newHashMap())));
    }

    @Test
    public void testOneFilterVariations()
    {
        testOneFilterNameVariations("Filter 1", "Filter Description", "/jira",
                "Filter 1", "menu.issues.filter.title [Filter 1] [Filter Description]");
        testOneFilterNameVariations("123456789012345678901234567890", "Filter Description", "/jira",
                "123456789012345678901234567890", "menu.issues.filter.title [123456789012345678901234567890] [Filter Description]");
        testOneFilterNameVariations("12345678901234567890123456789012345678901234567890", "Filter Description", "/jira",
                "123456789012345678901234567890...", "menu.issues.filter.title [12345678901234567890123456789012345678901234567890] [Filter Description]");

        testOneFilterNameVariations("Filter 1", "Filter Description", "",
                "Filter 1", "menu.issues.filter.title [Filter 1] [Filter Description]");
    }

    private void testOneFilterNameVariations(final String filterName, final String filterDesc, final String contextPath,
            final String expectedLabel, final String expectedDesc)
    {
        final SearchRequest sr = new SearchRequest(null, new MockApplicationUser("admin"), filterName, filterDesc, 1L, 1);

        when(searchRequestService.getFavouriteFilters(user)).thenReturn(Lists.newArrayList(sr));
        when(requestContextFactory.getJiraVelocityRequestContext()).thenReturn(requestContext);
        when(i18nFactory.getInstance(user.getDirectoryUser())).thenReturn(new MockI18nHelper());
        when(requestContext.getBaseUrl()).thenReturn(contextPath);
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_FILTER_DROPDOWN_ITEMS)).thenReturn("10");

        final Iterable<WebItem> items = linkFactory.getItems(MapBuilder.<String, Object>build("user", user.getDirectoryUser()));

        final WebItem link = getWebItem(30, "filter_lnk_1", expectedLabel, expectedDesc, contextPath + "/secure/IssueNavigator.jspa?mode=hide&requestId=1", "1");
        if (StringUtils.isNotBlank(contextPath))
        {
            assertThat(items, hasItems(eqWebItem(MY_ISSUES), eqWebItem(REPORTED_ISSUES), eqWebItem(link)));
        }
        else
        {
            assertThat(items, hasItems(eqWebItem(MY_ISSUES_NO_BASE), eqWebItem(REPORTED_ISSUES_NO_BASE), eqWebItem(link)));
        }
    }

    @Test
    public void testMultipleFiltersWithNoBaseUrl()
    {
        SearchRequest sr1 = new SearchRequest(null, new MockApplicationUser("admin"), "Filter 1", "Filter Description1", 1L, 1);
        SearchRequest sr2 = new SearchRequest(null, new MockApplicationUser("admin"), "Filter 2", null, 2L, 2);
        SearchRequest sr3 = new SearchRequest(null, new MockApplicationUser("admin"), "Filter 3", "Filter Description3", 3L, 3);
        SearchRequest sr4 = new SearchRequest(null, new MockApplicationUser("admin"), "Filter 4", null, 4L, 4);

        when(searchRequestService.getFavouriteFilters(user)).thenReturn(Lists.newArrayList(sr1, sr2, sr3, sr4));
        when(requestContextFactory.getJiraVelocityRequestContext()).thenReturn(requestContext);
        when(i18nFactory.getInstance(user.getDirectoryUser())).thenReturn(new MockI18nHelper());
        when(requestContext.getBaseUrl()).thenReturn("");
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_FILTER_DROPDOWN_ITEMS)).thenReturn("10");

        final Iterable<WebItem> items = linkFactory.getItems(MapBuilder.<String, Object>build("user", user.getDirectoryUser()));

        final WebItem link1 = getWebItem(30, "filter_lnk_1", "Filter 1", "menu.issues.filter.title [Filter 1] [Filter Description1]", "/secure/IssueNavigator.jspa?mode=hide&requestId=1", "1");
        final WebItem link2 = getWebItem(40, "filter_lnk_2", "Filter 2", "Filter 2", "/secure/IssueNavigator.jspa?mode=hide&requestId=2", "2");
        final WebItem link3 = getWebItem(50, "filter_lnk_3", "Filter 3", "menu.issues.filter.title [Filter 3] [Filter Description3]", "/secure/IssueNavigator.jspa?mode=hide&requestId=3", "3");
        final WebItem link4 = getWebItem(60, "filter_lnk_4", "Filter 4", "Filter 4", "/secure/IssueNavigator.jspa?mode=hide&requestId=4", "4");

        assertThat(items, hasItems(eqWebItem(MY_ISSUES_NO_BASE), eqWebItem(REPORTED_ISSUES_NO_BASE),
                eqWebItem(link1), eqWebItem(link2), eqWebItem(link3), eqWebItem(link4)));
    }

    @Test
    public void testMultipleFiltersWithNoBaseUrlInvalidMaxProperty()
    {
        SearchRequest sr1 = new SearchRequest(null, new MockApplicationUser("admin"), "Filter 1", "Filter Description1", 1L, 1);
        SearchRequest sr2 = new SearchRequest(null, new MockApplicationUser("admin"), "Filter 2", null, 2L, 2);
        SearchRequest sr3 = new SearchRequest(null, new MockApplicationUser("admin"), "Filter 3", "Filter Description3", 3L, 3);
        SearchRequest sr4 = new SearchRequest(null, new MockApplicationUser("admin"), "Filter 4", null, 4L, 4);

        when(searchRequestService.getFavouriteFilters(user)).thenReturn(Lists.newArrayList(sr1, sr2, sr3, sr4));
        when(requestContextFactory.getJiraVelocityRequestContext()).thenReturn(requestContext);
        when(i18nFactory.getInstance(user.getDirectoryUser())).thenReturn(new MockI18nHelper());
        when(requestContext.getBaseUrl()).thenReturn("");
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_FILTER_DROPDOWN_ITEMS)).thenReturn("Nick Rocks but he can't write unit tests.");

        final Iterable<WebItem> items = linkFactory.getItems(MapBuilder.<String, Object>build("user", user.getDirectoryUser()));

        final WebItem link1 = getWebItem(30, "filter_lnk_1", "Filter 1", "menu.issues.filter.title [Filter 1] [Filter Description1]", "/secure/IssueNavigator.jspa?mode=hide&requestId=1", "1");
        final WebItem link2 = getWebItem(40, "filter_lnk_2", "Filter 2", "Filter 2", "/secure/IssueNavigator.jspa?mode=hide&requestId=2", "2");
        final WebItem link3 = getWebItem(50, "filter_lnk_3", "Filter 3", "menu.issues.filter.title [Filter 3] [Filter Description3]", "/secure/IssueNavigator.jspa?mode=hide&requestId=3", "3");
        final WebItem link4 = getWebItem(60, "filter_lnk_4", "Filter 4", "Filter 4", "/secure/IssueNavigator.jspa?mode=hide&requestId=4", "4");

        assertThat(items, hasItems(eqWebItem(MY_ISSUES_NO_BASE), eqWebItem(REPORTED_ISSUES_NO_BASE),
                eqWebItem(link1), eqWebItem(link2), eqWebItem(link3), eqWebItem(link4)));
    }

    @Test
    public void testTooManyFiltersWithNoBaseUrl()
    {
        SearchRequest sr1 = new SearchRequest(null, new MockApplicationUser("admin"), "Filter 1", "Filter Description1", 1L, 1);
        SearchRequest sr2 = new SearchRequest(null, new MockApplicationUser("admin"), "Filter 2", null, 2L, 2);
        SearchRequest sr3 = new SearchRequest(null, new MockApplicationUser("admin"), "Filter 3", "Filter Description3", 3L, 3);
        SearchRequest sr4 = new SearchRequest(null, new MockApplicationUser("admin"), "Filter 4", null, 4L, 4);

        when(searchRequestService.getFavouriteFilters(user)).thenReturn(Lists.newArrayList(sr1, sr2, sr3, sr4));
        when(requestContextFactory.getJiraVelocityRequestContext()).thenReturn(requestContext);
        when(i18nFactory.getInstance(user.getDirectoryUser())).thenReturn(new MockI18nHelper());
        when(requestContext.getBaseUrl()).thenReturn("");
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_FILTER_DROPDOWN_ITEMS)).thenReturn("3");

        final Iterable<WebItem> items = linkFactory.getItems(MapBuilder.<String, Object>build("user", user.getDirectoryUser()));

        final WebItem link1 = getWebItem(30, "filter_lnk_1", "Filter 1", "menu.issues.filter.title [Filter 1] [Filter Description1]", "/secure/IssueNavigator.jspa?mode=hide&requestId=1", "1");
        final WebItem link2 = getWebItem(40, "filter_lnk_2", "Filter 2", "Filter 2", "/secure/IssueNavigator.jspa?mode=hide&requestId=2", "2");
        final WebItem link3 = getWebItem(50, "filter_lnk_3", "Filter 3", "menu.issues.filter.title [Filter 3] [Filter Description3]", "/secure/IssueNavigator.jspa?mode=hide&requestId=3", "3");
        final WebItem link4 = getWebItem(60, "filter_lnk_more", "menu.issues.filter.more", "menu.issues.filter.more.desc", "/secure/ManageFilters.jspa?filterView=favourites", null);

        assertThat(items, hasItems(eqWebItem(MY_ISSUES_NO_BASE), eqWebItem(REPORTED_ISSUES_NO_BASE),
                eqWebItem(link1), eqWebItem(link2), eqWebItem(link3), eqWebItem(link4)));
    }

    @Test
    public void testTooManyFiltersWithBaseUrl()
    {
        SearchRequest sr1 = new SearchRequest(null, new MockApplicationUser("admin"), "Filter 1", "Filter Description1", 1L, 1);
        SearchRequest sr2 = new SearchRequest(null, new MockApplicationUser("admin"), "Filter 2", null, 2L, 2);
        SearchRequest sr3 = new SearchRequest(null, new MockApplicationUser("admin"), "Filter 3", "Filter Description3", 3L, 3);
        SearchRequest sr4 = new SearchRequest(null, new MockApplicationUser("admin"), "Filter 4", null, 4L, 4);

        when(searchRequestService.getFavouriteFilters(user)).thenReturn(Lists.newArrayList(sr1, sr2, sr3, sr4));
        when(requestContextFactory.getJiraVelocityRequestContext()).thenReturn(requestContext);
        when(i18nFactory.getInstance(user.getDirectoryUser())).thenReturn(new MockI18nHelper());
        when(requestContext.getBaseUrl()).thenReturn("/jira");
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_FILTER_DROPDOWN_ITEMS)).thenReturn("3");

        final Iterable<WebItem> items = linkFactory.getItems(MapBuilder.<String, Object>build("user", user.getDirectoryUser()));

        final WebItem link1 = getWebItem(30, "filter_lnk_1", "Filter 1", "menu.issues.filter.title [Filter 1] [Filter Description1]", "/jira/secure/IssueNavigator.jspa?mode=hide&requestId=1", "1");
        final WebItem link2 = getWebItem(40, "filter_lnk_2", "Filter 2", "Filter 2", "/jira/secure/IssueNavigator.jspa?mode=hide&requestId=2", "2");
        final WebItem link3 = getWebItem(50, "filter_lnk_3", "Filter 3", "menu.issues.filter.title [Filter 3] [Filter Description3]", "/jira/secure/IssueNavigator.jspa?mode=hide&requestId=3", "3");
        final WebItem link4 = getWebItem(60, "filter_lnk_more", "menu.issues.filter.more", "menu.issues.filter.more.desc", "/jira/secure/ManageFilters.jspa?filterView=favourites", null);

        assertThat(items, hasItems(eqWebItem(MY_ISSUES), eqWebItem(REPORTED_ISSUES),
                eqWebItem(link1), eqWebItem(link2), eqWebItem(link3), eqWebItem(link4)));
    }

    private static WebItem getWebItem(int weight, final String id, final String label, final String title, final String url, final String filterId)
    {
        final WebFragmentBuilder fragmentBuilder = new WebFragmentBuilder(weight).
                id(id).
                label(label).
                title(title);
        if (filterId != null)
        {
            fragmentBuilder.
                    addParam("class", "filter-link").
                    addParam("data-filter-id", filterId);
        }

        return fragmentBuilder.
                webItem("find_link/issues_filter_main").
                url(url).
                build();
    }

}
