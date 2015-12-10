package com.atlassian.jira.dashboard.permission;

import com.atlassian.gadgets.dashboard.DashboardId;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserUtil;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestJiraPermissionService extends MockControllerTestCase
{
    private ApplicationUser fred;

    @Before
    public void setUp() throws Exception
    {
        fred = new MockApplicationUser("fred");

    }

    @Test
    public void testIsReadableBy()
    {
        final UserUtil mockUserUtil = mockController.getMock(UserUtil.class);
        mockUserUtil.getUserByName("fred");
        mockController.setReturnValue(fred);

        final PortalPageService mockPortalPageService = mockController.getMock(PortalPageService.class);
        mockPortalPageService.validateForGetPortalPage(new JiraServiceContextImpl(fred), 10011L);
        mockController.setReturnValue(true);
        final PermissionManager mockPermissionManager = mockController.getMock(PermissionManager.class);

        JiraPermissionService jiraPermissionService = mockController.instantiateAndReplay(JiraPermissionService.class);

        final boolean readableBy = jiraPermissionService.isReadableBy(DashboardId.valueOf(Long.toString(10011L)), "fred");
        assertTrue(readableBy);
    }

    @Test
    public void testIsNotReadableBy()
    {
        final UserUtil mockUserUtil = mockController.getMock(UserUtil.class);
        mockUserUtil.getUserByName("fred");
        mockController.setReturnValue(fred);

        final PortalPageService mockPortalPageService = mockController.getMock(PortalPageService.class);
        mockPortalPageService.validateForGetPortalPage(new JiraServiceContextImpl(fred), 10011L);
        mockController.setReturnValue(false);
        final PermissionManager mockPermissionManager = mockController.getMock(PermissionManager.class);

        JiraPermissionService jiraPermissionService = mockController.instantiateAndReplay(JiraPermissionService.class);

        final boolean readableBy = jiraPermissionService.isReadableBy(DashboardId.valueOf(Long.toString(10011)), "fred");
        assertFalse(readableBy);
    }

    @Test
    public void testIsWritableBy()
    {
        final UserUtil mockUserUtil = mockController.getMock(UserUtil.class);
        mockUserUtil.getUserByName("fred");
        mockController.setReturnValue(fred);

        final PortalPage mockPortalPage = PortalPage.name("Non system page").build();

        final PortalPageService mockPortalPageService = mockController.getMock(PortalPageService.class);
        final JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(fred);
        mockPortalPageService.getPortalPage(serviceContext, 10011L);
        mockController.setReturnValue(mockPortalPage);
        mockPortalPageService.validateForUpdate(serviceContext, mockPortalPage);
        mockController.setReturnValue(true);
        final PermissionManager mockPermissionManager = mockController.getMock(PermissionManager.class);

        JiraPermissionService jiraPermissionService = mockController.instantiateAndReplay(JiraPermissionService.class);

        final boolean writableBy = jiraPermissionService.isWritableBy(DashboardId.valueOf(Long.toString(10011)), "fred");
        assertTrue(writableBy);
    }

    @Test
    public void testSystemDefaultDashboardNotWritable()
    {
        final UserUtil mockUserUtil = mockController.getMock(UserUtil.class);
        mockUserUtil.getUserByName("fred");
        mockController.setReturnValue(fred);

        final PortalPage mockPortalPage = PortalPage.name("System Default").systemDashboard().build();

        final PortalPageService mockPortalPageService = mockController.getMock(PortalPageService.class);
        final JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(fred);
        mockPortalPageService.getPortalPage(serviceContext, 10011L);
        mockController.setReturnValue(mockPortalPage);
        final PermissionManager mockPermissionManager = mockController.getMock(PermissionManager.class);

        JiraPermissionService jiraPermissionService = mockController.instantiateAndReplay(JiraPermissionService.class);

        JiraPermissionService.setAllowEditingOfDefaultDashboard(false);
        final boolean writableBy = jiraPermissionService.isWritableBy(DashboardId.valueOf(Long.toString(10011)), "fred");
        assertFalse(writableBy);
    }

    @Test
    public void testSystemDefaultDashboardIsWritable()
    {
        final UserUtil mockUserUtil = mockController.getMock(UserUtil.class);
        mockUserUtil.getUserByName("fred");
        mockController.setReturnValue(fred);

        final PortalPage mockPortalPage = PortalPage.name("System Default").systemDashboard().build();

        final PortalPageService mockPortalPageService = mockController.getMock(PortalPageService.class);
        final JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(fred);
        mockPortalPageService.getPortalPage(serviceContext, 10011L);
        mockController.setReturnValue(mockPortalPage);
        mockPortalPageService.validateForUpdate(serviceContext, mockPortalPage);
        mockController.setReturnValue(true);
        final PermissionManager mockPermissionManager = mockController.getMock(PermissionManager.class);

        JiraPermissionService jiraPermissionService = mockController.instantiateAndReplay(JiraPermissionService.class);

        JiraPermissionService.setAllowEditingOfDefaultDashboard(true);
        final boolean writableBy = jiraPermissionService.isWritableBy(DashboardId.valueOf(Long.toString(10011)), "fred");
        assertTrue(writableBy);
    }

    @Test
    public void testIsNotWritableBy()
    {
        final UserUtil mockUserUtil = mockController.getMock(UserUtil.class);
        mockUserUtil.getUserByName("fred");
        mockController.setReturnValue(fred);

        final PortalPage mockPortalPage = PortalPage.name("System Default").build();

        final PortalPageService mockPortalPageService = mockController.getMock(PortalPageService.class);
        final JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(fred);
        mockPortalPageService.getPortalPage(serviceContext, 10011L);
        mockController.setReturnValue(mockPortalPage);
        mockPortalPageService.validateForUpdate(serviceContext, mockPortalPage);
        mockController.setReturnValue(false);
        final PermissionManager mockPermissionManager = mockController.getMock(PermissionManager.class);

        JiraPermissionService jiraPermissionService = mockController.instantiateAndReplay(JiraPermissionService.class);

        final boolean writableBy = jiraPermissionService.isWritableBy(DashboardId.valueOf(Long.toString(10011)), "fred");
        assertFalse(writableBy);
    }
}
