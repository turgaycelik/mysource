package com.atlassian.jira.portal;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.web.api.WebItem;
import com.atlassian.plugin.web.api.model.WebFragmentBuilder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.plugin.webfragment.EqWebItem.eqWebItem;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

/**
 * @since v4.0
 */
public class TestFavouriteDashboardLinkFactory extends MockControllerTestCase
{
    private VelocityRequestContext requestContext;
    private VelocityRequestContextFactory requestContextFactory;
    private PortalPageService portalPageService;
    private UserHistoryManager userHistoryManager;
    private I18nHelper.BeanFactory i18nFactory;
    private I18nHelper i18n;
    private User user;

    private FavouriteDashboardLinkFactory linkFactory;
    private Map<String, Object> context;


    @Before
    public void setUp() throws Exception
    {

        requestContext = mockController.getMock(VelocityRequestContext.class);
        requestContextFactory = mockController.getMock(VelocityRequestContextFactory.class);
        i18nFactory = mockController.getMock(I18nHelper.BeanFactory.class);
        i18n = mockController.getMock(I18nHelper.class);
        portalPageService = mockController.getMock(PortalPageService.class);
        userHistoryManager = mockController.getMock(UserHistoryManager.class);

        user = new MockUser("admin");
        context = Maps.newHashMap();

        linkFactory = new FavouriteDashboardLinkFactory(portalPageService, requestContextFactory, i18nFactory, userHistoryManager);
    }

    @After
    public void tearDown() throws Exception
    {
        requestContext = null;
        requestContextFactory = null;
        linkFactory = null;
        user = null;
        i18nFactory = null;
        i18n = null;
        portalPageService = null;
        userHistoryManager = null;
    }

    @Test
    public void testNullUserNullDashboards()
    {
        final WebItem link = new WebFragmentBuilder(20).
                id("dash_lnk_system").
                label("View System Dashboard").
                title("View System Dashboard Title").
                webItem("home_link/dashboard_link_main").
                url("/secure/Dashboard.jspa").build();


        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        portalPageService.getFavouritePortalPages((User) null);
        mockController.setReturnValue(null);

        requestContext.getBaseUrl();
        mockController.setReturnValue("");

        i18nFactory.getInstance((User) null);
        mockController.setReturnValue(i18n);

        i18n.getText("menu.dashboard.view.system");
        mockController.setReturnValue("View System Dashboard");

        i18n.getText("menu.dashboard.view.system.title");
        mockController.setReturnValue("View System Dashboard Title");

        mockController.replay();

        List<WebItem> returnList = Lists.newArrayList(linkFactory.getItems(context));

        assertThat(returnList, hasItems(eqWebItem(link)));

        mockController.verify();
    }

    @Test
    public void testNullUserEmptyDashboards()
    {
        final WebItem link = new WebFragmentBuilder(20).
                id("dash_lnk_system").
                label("View System Dashboard").
                title("View System Dashboard Title").
                webItem("home_link/dashboard_link_main").
                url("/secure/Dashboard.jspa").build();

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        portalPageService.getFavouritePortalPages((User) null);
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        requestContext.getBaseUrl();
        mockController.setReturnValue("");

        i18nFactory.getInstance((User) null);
        mockController.setReturnValue(i18n);

        i18n.getText("menu.dashboard.view.system");
        mockController.setReturnValue("View System Dashboard");

        i18n.getText("menu.dashboard.view.system.title");
        mockController.setReturnValue("View System Dashboard Title");

        mockController.replay();

        List<WebItem> returnList = Lists.newArrayList(linkFactory.getItems(context));

        assertThat(returnList, hasItems(eqWebItem(link)));

        mockController.verify();
    }

    @Test
    public void testNullDashboards()
    {
        final WebItem link = new WebFragmentBuilder(20).
                id("dash_lnk_system").
                label("View System Dashboard").
                title("View System Dashboard Title").
                webItem("home_link/dashboard_link_main").
                url("/jira/secure/Dashboard.jspa").build();

        context.put("user", user);

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        portalPageService.getFavouritePortalPages(user);
        mockController.setReturnValue(null);

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        i18n.getText("menu.dashboard.view.system");
        mockController.setReturnValue("View System Dashboard");

        i18n.getText("menu.dashboard.view.system.title");
        mockController.setReturnValue("View System Dashboard Title");

        mockController.replay();

        List<WebItem> returnList = Lists.newArrayList(linkFactory.getItems(context));

        assertThat(returnList, hasItems(eqWebItem(link)));

        mockController.verify();
    }

    @Test
    public void testEmptyDashboards()
    {
        final WebItem link = new WebFragmentBuilder(20).
                id("dash_lnk_system").
                label("View System Dashboard").
                title("View System Dashboard Title").
                webItem("home_link/dashboard_link_main").
                url("/jira/secure/Dashboard.jspa").build();

        context.put("user", user);
        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        portalPageService.getFavouritePortalPages(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        i18n.getText("menu.dashboard.view.system");
        mockController.setReturnValue("View System Dashboard");

        i18n.getText("menu.dashboard.view.system.title");
        mockController.setReturnValue("View System Dashboard Title");

        mockController.replay();

        List<WebItem> returnList = Lists.newArrayList(linkFactory.getItems(context));

        assertThat(returnList, hasItems(eqWebItem(link)));

        mockController.verify();
    }

    @Test
    public void testNoSessionNullUser()
    {
        final WebItem link = new WebFragmentBuilder(20).
                id("dash_lnk_1").
                label("Portal Page 1").
                title("Portal Page 1 - Portal Description 1").
                webItem("home_link/dashboard_link_main").
                url("/secure/Dashboard.jspa?selectPageId=1").build();

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        PortalPage page = PortalPage.id(1L).name("Portal Page 1").description("Portal Description 1").owner(new MockApplicationUser("admin")).favouriteCount(0L).version(0L).build();

        portalPageService.getFavouritePortalPages((User) null);
        mockController.setReturnValue(CollectionBuilder.newBuilder(page).asList());

        requestContext.getBaseUrl();
        mockController.setReturnValue("");

        i18nFactory.getInstance((User) null);
        mockController.setReturnValue(i18n);

        userHistoryManager.getHistory(UserHistoryItem.DASHBOARD, (User) null);
        mockController.setReturnValue(Collections.<String>emptyList());

        i18n.getText("menu.dashboard.title", "Portal Page 1", "Portal Description 1");
        mockController.setReturnValue("Portal Page 1 - Portal Description 1");

        mockController.replay();

        List<WebItem> returnList = Lists.newArrayList(linkFactory.getItems(context));
        List<WebItem> expectedList = Lists.newArrayList(link);

        assertThat(returnList, hasItems(eqWebItem(link)));

        mockController.verify();
    }

    @Test
    public void testDiffInSession()
    {
        final WebItem link = new WebFragmentBuilder(20).
                id("dash_lnk_1").
                label("Portal Page 1").
                title("Portal Page 1 - Portal Description 1").
                webItem("home_link/dashboard_link_main").
                url("/jira/secure/Dashboard.jspa?selectPageId=1").build();

        context.put("user", user);
        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        PortalPage page = PortalPage.id(1L).name("Portal Page 1").description("Portal Description 1").owner(new MockApplicationUser("admin")).favouriteCount(0L).version(0L).build();

        portalPageService.getFavouritePortalPages(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(page).asList());

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        userHistoryManager.getHistory(UserHistoryItem.DASHBOARD, user);
        mockController.setReturnValue(CollectionBuilder.list(new UserHistoryItem(UserHistoryItem.DASHBOARD, "2")));

        i18n.getText("menu.dashboard.title", "Portal Page 1", "Portal Description 1");
        mockController.setReturnValue("Portal Page 1 - Portal Description 1");

        mockController.replay();

        List<WebItem> returnList = Lists.newArrayList(linkFactory.getItems(context));
        List<WebItem> expectedList = Lists.newArrayList(link);

        assertThat(returnList, hasItems(eqWebItem(link)));

        mockController.verify();
    }


    @Test
    public void testSameInSession()
    {
        final WebItem link = new WebFragmentBuilder(20).
                id("dash_lnk_1").
                label("Portal Page 1").
                title("Portal Page 1 - Portal Description 1").
                webItem("home_link/dashboard_link_main").
                url("/jira/secure/Dashboard.jspa?selectPageId=1").build();

        context.put("user", user);
        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        PortalPage page = PortalPage.id(1L).name("Portal Page 1").description("Portal Description 1").owner(new MockApplicationUser("admin")).favouriteCount(0L).version(0L).build();

        portalPageService.getFavouritePortalPages(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(page).asList());

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        userHistoryManager.getHistory(UserHistoryItem.DASHBOARD, user);
        mockController.setReturnValue(CollectionBuilder.list(new UserHistoryItem(UserHistoryItem.DASHBOARD, "1")));

        i18n.getText("menu.dashboard.title", "Portal Page 1", "Portal Description 1");
        mockController.setReturnValue("Portal Page 1 - Portal Description 1");

        mockController.replay();

        List<WebItem> returnList = Lists.newArrayList(linkFactory.getItems(context));

        assertThat(returnList, hasItems(eqWebItem(link)));

        mockController.verify();
    }

    @Test
    public void testSameInSessionWithMulti()
    {
        final WebItem link = new WebFragmentBuilder(20).
                id("dash_lnk_1").
                styleClass("bolded").
                label("Portal Page 1").
                title("Portal Page 1 - Portal Description 1").
                webItem("home_link/dashboard_link_main").
                url("/jira/secure/Dashboard.jspa?selectPageId=1").build();
        final WebItem link2 = new WebFragmentBuilder(30).
                id("dash_lnk_2").
                label("Portal Page 2").
                title("Portal Page 2").
                webItem("home_link/dashboard_link_main").
                url("/jira/secure/Dashboard.jspa?selectPageId=2").build();

        context.put("user", user);
        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        PortalPage page = PortalPage.id(1L).name("Portal Page 1").description("Portal Description 1").owner(new MockApplicationUser("admin")).favouriteCount(0L).version(0L).build();
        PortalPage page2 = PortalPage.id(2L).name("Portal Page 2").owner(new MockApplicationUser("admin")).favouriteCount(0L).version(0L).build();

        portalPageService.getFavouritePortalPages(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(page, page2).asList());

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        userHistoryManager.getHistory(UserHistoryItem.DASHBOARD, user);
        mockController.setReturnValue(CollectionBuilder.list(new UserHistoryItem(UserHistoryItem.DASHBOARD, "1"), new UserHistoryItem(UserHistoryItem.DASHBOARD, "2")));

        i18n.getText("menu.dashboard.title", "Portal Page 1", "Portal Description 1");
        mockController.setReturnValue("Portal Page 1 - Portal Description 1");

        mockController.replay();

        List<WebItem> returnList = Lists.newArrayList(linkFactory.getItems(context));

        assertThat(returnList, hasItems(eqWebItem(link), eqWebItem(link2)));

        mockController.verify();
    }


    @Test
    public void testLongLabel()
    {
        final WebItem link = new WebFragmentBuilder(20).
                id("dash_lnk_1").
                label("123456789012345678901234567890").
                title("123456789012345678901234567890 - Portal Description 1").
                webItem("home_link/dashboard_link_main").
                url("/jira/secure/Dashboard.jspa?selectPageId=1").build();

        context.put("user", user);
        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        PortalPage page = PortalPage.id(1L).name("123456789012345678901234567890").description("Portal Description 1").owner(new MockApplicationUser("admin")).favouriteCount(0L).version(0L).build();

        portalPageService.getFavouritePortalPages(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(page).asList());

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        userHistoryManager.getHistory(UserHistoryItem.DASHBOARD, user);
        mockController.setReturnValue(CollectionBuilder.list(new UserHistoryItem(UserHistoryItem.DASHBOARD, "1")));

        i18n.getText("menu.dashboard.title", "123456789012345678901234567890", "Portal Description 1");
        mockController.setReturnValue("123456789012345678901234567890 - Portal Description 1");

        mockController.replay();

        List<WebItem> returnList = Lists.newArrayList(linkFactory.getItems(context));

        assertThat(returnList, hasItems(eqWebItem(link)));

        mockController.verify();
    }

    @Test
    public void testTooLongLabel()
    {
        final WebItem link = new WebFragmentBuilder(20).
                id("dash_lnk_1").
                label("123456789012345678901234567890...").
                title("12345678901234567890123456789012345678901234567890 - Portal Description 1").
                webItem("home_link/dashboard_link_main").
                url("/jira/secure/Dashboard.jspa?selectPageId=1").build();

        context.put("user", user);
        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        PortalPage page = PortalPage.id(1L).name("12345678901234567890123456789012345678901234567890").description("Portal Description 1").owner(new MockApplicationUser("admin")).favouriteCount(0L).version(0L).build();

        portalPageService.getFavouritePortalPages(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(page).asList());

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        userHistoryManager.getHistory(UserHistoryItem.DASHBOARD, user);
        mockController.setReturnValue(CollectionBuilder.list(new UserHistoryItem(UserHistoryItem.DASHBOARD, "1")));

        i18n.getText("menu.dashboard.title", "12345678901234567890123456789012345678901234567890", "Portal Description 1");
        mockController.setReturnValue("12345678901234567890123456789012345678901234567890 - Portal Description 1");

        mockController.replay();

        List<WebItem> returnList = Lists.newArrayList(linkFactory.getItems(context));

        assertThat(returnList, hasItems(eqWebItem(link)));

        mockController.verify();
    }


}
