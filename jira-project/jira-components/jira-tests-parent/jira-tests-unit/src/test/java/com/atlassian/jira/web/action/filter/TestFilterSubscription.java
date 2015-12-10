package com.atlassian.jira.web.action.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockHttp;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.servlet.MockHttpServletResponse;
import com.atlassian.jira.util.UriValidator;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.action.RedirectSanitiser;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.jira.web.util.OutlookDateManager;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import junit.framework.Assert;
import webwork.action.ServletActionContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestFilterSubscription
{
    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @Rule
    public final MockHttp mockHttp = MockHttp.withMocks(new MockHttpServletRequest(), new MockHttpServletResponse());

    @Mock
    @AvailableInContainer
    private SubscriptionManager subscriptionManager;

    @Mock
    @AvailableInContainer
    private OutlookDateManager outlookDateManager;

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext authenticationContext;

    @Mock
    @AvailableInContainer
    private PermissionManager permissionManager;

    @AvailableInContainer
    private RedirectSanitiser redirectSanitiser = new MockRedirectSanitiser();

    @Mock
    @AvailableInContainer
    private GroupManager groupManager;

    @Mock
    @AvailableInContainer
    private UserUtil userUtil;

    @AvailableInContainer
    private UriValidator uriValidator = new UriValidator("UTF-8");

    @Mock
    private OutlookDate outlookDate;

    @Mock
    private HttpServletResponse servletResponse;

    private final long runAt = System.currentTimeMillis();
    private ApplicationUser loggedInUser;
    private User loggedInOldUser;
    private GenericValue srgv;
    private FilterSubscription filterSubscription;

    @Before
    public void setUp() throws Exception
    {
        filterSubscription = new FilterSubscription(null, null, null, null);
        ServletActionContext.setResponse(servletResponse);
        loggedInUser = new MockApplicationUser("owen");
        loggedInOldUser = new MockUser("owen2");

        when(outlookDate.formatDMYHMS(any(Date.class))).thenReturn("abc");
        when(outlookDateManager.getOutlookDate(Locale.getDefault())).thenReturn(outlookDate);
        when(authenticationContext.getI18nHelper()).thenReturn(new MockI18nHelper(Locale.getDefault()));
        when(authenticationContext.getUser()).thenReturn(loggedInUser);
        when(authenticationContext.getLoggedInUser()).thenReturn(loggedInOldUser);
    }

    private void setupWithSubscription(Long subscriptionId) throws Exception
    {
        setupWithSubscriptionAndFilter(subscriptionId, 0L);
    }

    private void setupWithSubscriptionAndFilter(Long subscriptionId, Long filterId) throws Exception
    {
        final GenericValue subscription = mock(GenericValue.class);
        when(subscriptionManager.getSubscription(loggedInUser, subscriptionId)).thenReturn(subscription);

        filterSubscription.setSubId(subscriptionId.toString());
        filterSubscription.setFilterId(filterId);
    }

    @Test
    public void testLastTimeAcceptsTimestamps() throws Exception
    {
        filterSubscription.setLastRun("123456");
        filterSubscription.setLastRun("1378460070");

        Assert.assertEquals(filterSubscription.getLastRun(), "1378460070");
    }

    @Test
    public void testNextRunAcceptsTimestamps() throws Exception
    {
        filterSubscription.setNextRun("123456");
        filterSubscription.setNextRun("1378460070");

        Assert.assertEquals(filterSubscription.getNextRun(), "1378460070");
    }

    @Test
    public void testGetLastRunStrFormatsDateWithOutlookFormatter() throws Exception
    {
        filterSubscription.setLastRun("123456");
        filterSubscription.getLastRunStr();

        verify(outlookDate).formatDMYHMS(new Date(123456));
    }

    @Test
    public void testGetNextRunStrFormatsDateWithOutlookFormatter() throws Exception
    {
        filterSubscription.setNextRun("998877");
        filterSubscription.getNextRunStr();

        verify(outlookDate).formatDMYHMS(new Date(998877));
    }

    @Test
    public void testDoDeleteRedirectsToViewSubscriptions() throws Exception
    {
        setupWithSubscriptionAndFilter(12345L, 775544L);

        filterSubscription.doDelete();

        verify(servletResponse).sendRedirect("ViewSubscriptions.jspa?filterId=775544");
    }

    @Test
    public void testDoDeleteDeletesSubscription() throws Exception
    {
        final Long subscriptionId = 111444L;
        setupWithSubscription(subscriptionId);

        filterSubscription.doDelete();

        verify(subscriptionManager).deleteSubscription(subscriptionId);
    }

    @Test
    public void testDoRunNowRunsSubscription() throws Exception
    {
        final Long subscriptionId = 987L;
        setupWithSubscription(subscriptionId);

        filterSubscription.doRunNow();

        verify(subscriptionManager).runSubscription(loggedInUser, subscriptionId);
    }

    @Test
    public void testDoRunNowSendsRedirectToViewSubscriptions() throws Exception
    {
        final Long filterId = 2345L;
        setupWithSubscriptionAndFilter(1L, filterId);

        filterSubscription.doRunNow();

        verify(servletResponse).sendRedirect("ViewSubscriptions.jspa?filterId=" + filterId);
    }

    @Test
    public void testHasPermissionChecksRightPermissionForLoggedInUser() throws Exception
    {
        when(permissionManager.hasPermission(Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS, loggedInUser)).thenReturn(true);
        assertTrue("hasPermissions should return true for logged in user", filterSubscription.hasGroupPermission());
    }

    @Test
    public void testGetSubmitNameUsesI18n() throws GenericEntityException
    {
        assertEquals("filtersubscription.subscribe", filterSubscription.getSubmitName());
        filterSubscription.setSubId("1");
        assertEquals("common.forms.update", filterSubscription.getSubmitName());
    }

    @Test
    public void testCancelStrReturnsProperLink() throws Exception
    {
        assertEquals("ManageFilters.jspa", filterSubscription.getCancelStr());

        filterSubscription.setSubId("2");
        filterSubscription.setFilterId(1L);
        assertEquals("ViewSubscriptions.jspa?filterId=1", filterSubscription.getCancelStr());

        filterSubscription.setReturnUrl("abc");
        assertEquals("abc", filterSubscription.getCancelStr());
    }

    @Test
    public void testGetGroupsForAdministratorReturnsAllGroups() throws Exception
    {
        final ArrayList<Group> allGroups = Lists.<Group>newArrayList(new MockGroup("G1"), new MockGroup("G2"), new MockGroup("G3"));
        when(permissionManager.hasPermission(Permissions.ADMINISTER, loggedInOldUser)).thenReturn(true);
        when(groupManager.getAllGroups()).thenReturn(allGroups);

        final Collection<String> groups = filterSubscription.getGroups();

        assertThat(groups, Matchers.contains("G1", "G2", "G3"));
    }

    @Test
    public void testGetGroupsForRegularUserReturnsOnlyHisGroups() throws Exception
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, loggedInOldUser)).thenReturn(false);
        when(userUtil.getGroupNamesForUser(loggedInOldUser.getName())).thenReturn(ImmutableSortedSet.of("G001", "G002"));

        final Collection<String> groups = filterSubscription.getGroups();

        assertThat(groups, Matchers.contains("G001", "G002"));
    }

    private static class MockHttpServletRequest extends com.atlassian.jira.mock.servlet.MockHttpServletRequest
    {
        @Override
        public String getScheme()
        {
            return "http";
        }
    };
}