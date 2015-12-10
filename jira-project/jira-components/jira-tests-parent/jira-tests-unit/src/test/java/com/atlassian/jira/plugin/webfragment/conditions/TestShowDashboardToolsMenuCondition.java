package com.atlassian.jira.plugin.webfragment.conditions;

import java.util.Map;

import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.gadgets.dashboard.spi.DashboardPermissionService;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.collect.MapBuilder;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestShowDashboardToolsMenuCondition
{
    @Test
    public void testShouldDisplayNonDefaultDashboard()
    {
        final IMocksControl mocksControl = EasyMock.createControl();

        final PortalPage mockPortalPage = PortalPage.name("Some page").id(10000L).build();

        final PortalPageService mockPortalPageService = mocksControl.createMock(PortalPageService.class);
        expect(mockPortalPageService.getSystemDefaultPortalPage()).andReturn(mockPortalPage);

        final UserUtil mockUserUtil = mocksControl.createMock(UserUtil.class);
        expect(mockUserUtil.getUser("admin")).andReturn(new MockUser("admin"));

        mocksControl.replay();

        ShowDashboardToolsMenuCondition condition = new ShowDashboardToolsMenuCondition()
        {
            @Override
            PortalPageService getPortalPageService()
            {
                return mockPortalPageService;
            }

            @Override
            UserUtil getUserUtil()
            {
                return mockUserUtil;
            }

            @Override
            DashboardPermissionService getPermissionService()
            {
                return null;
            }
        };

        final Map<String, Object> context = MapBuilder.<String, Object>newBuilder().
                add("username", "admin").
                add("dashboardId", DashboardId.valueOf(Long.toString(10020))).
                toMap();
        final boolean display = condition.shouldDisplay(context);
        assertTrue(display);
        mocksControl.verify();
    }

    @Test
    public void testShouldDisplayNonDefaultDashboardNotLoggedIn()
    {
        final IMocksControl mocksControl = EasyMock.createControl();

        final PortalPage mockPortalPage = PortalPage.name("Some page").id(10000L).build();

        final PortalPageService mockPortalPageService = mocksControl.createMock(PortalPageService.class);
        expect(mockPortalPageService.getSystemDefaultPortalPage()).andReturn(mockPortalPage);

        final UserUtil mockUserUtil = mocksControl.createMock(UserUtil.class);
        expect(mockUserUtil.getUser(null)).andReturn(null);

        mocksControl.replay();

        ShowDashboardToolsMenuCondition condition = new ShowDashboardToolsMenuCondition()
        {
            @Override
            PortalPageService getPortalPageService()
            {
                return mockPortalPageService;
            }

            @Override
            UserUtil getUserUtil()
            {
                return mockUserUtil;
            }

            @Override
            DashboardPermissionService getPermissionService()
            {
                return null;
            }
        };

        final Map<String, Object> context = MapBuilder.<String, Object>newBuilder().
                add("dashboardId", DashboardId.valueOf(Long.toString(10020))).
                toMap();
        final boolean display = condition.shouldDisplay(context);
        assertFalse(display);
        mocksControl.verify();
    }

    @Test
    public void testShouldDisplayDefaultDashboardInAdminSection()
    {
        final IMocksControl mocksControl = EasyMock.createControl();

        final PortalPage mockPortalPage = PortalPage.name("Some page").id(10000L).build();

        final PortalPageService mockPortalPageService = mocksControl.createMock(PortalPageService.class);
        expect(mockPortalPageService.getSystemDefaultPortalPage()).andReturn(mockPortalPage);

        final UserUtil mockUserUtil = mocksControl.createMock(UserUtil.class);

        final DashboardPermissionService mockPermissionService = mocksControl.createMock(DashboardPermissionService.class);
        final DashboardId dashboardId = DashboardId.valueOf(Long.toString(10000L));
        expect(mockPermissionService.isWritableBy(dashboardId, "admin")).andReturn(true);

        mocksControl.replay();

        ShowDashboardToolsMenuCondition condition = new ShowDashboardToolsMenuCondition()
        {
            @Override
            PortalPageService getPortalPageService()
            {
                return mockPortalPageService;
            }

            @Override
            UserUtil getUserUtil()
            {
                return mockUserUtil;
            }

            @Override
            DashboardPermissionService getPermissionService()
            {
                return mockPermissionService;
            }
        };

        final Map<String, Object> context = MapBuilder.<String, Object>newBuilder().
                add("username", "admin").
                add("dashboardId", dashboardId).
                toMap();
        final boolean display = condition.shouldDisplay(context);
        assertFalse(display);
        mocksControl.verify();
    }

    @Test
    public void testShouldDisplayDefaultDashboardOnHomeScreen()
    {
        final IMocksControl mocksControl = EasyMock.createControl();

        final PortalPage mockPortalPage = PortalPage.name("Some page").id(10000L).build();

        final PortalPageService mockPortalPageService = mocksControl.createMock(PortalPageService.class);
        expect(mockPortalPageService.getSystemDefaultPortalPage()).andReturn(mockPortalPage);

        final UserUtil mockUserUtil = mocksControl.createMock(UserUtil.class);
        expect(mockUserUtil.getUser("admin")).andReturn(new MockUser("admin"));

        final DashboardPermissionService mockPermissionService = mocksControl.createMock(DashboardPermissionService.class);
        final DashboardId dashboardId = DashboardId.valueOf(Long.toString(10000L));
        expect(mockPermissionService.isWritableBy(dashboardId, "admin")).andReturn(false);

        mocksControl.replay();

        ShowDashboardToolsMenuCondition condition = new ShowDashboardToolsMenuCondition()
        {
            @Override
            PortalPageService getPortalPageService()
            {
                return mockPortalPageService;
            }

            @Override
            UserUtil getUserUtil()
            {
                return mockUserUtil;
            }

            @Override
            DashboardPermissionService getPermissionService()
            {
                return mockPermissionService;
            }
        };

        final Map<String, Object> context = MapBuilder.<String, Object>newBuilder().
                add("username", "admin").
                add("dashboardId", dashboardId).
                toMap();
        final boolean display = condition.shouldDisplay(context);
        assertTrue(display);
        mocksControl.verify();
    }
}
