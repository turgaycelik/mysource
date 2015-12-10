package com.atlassian.jira.admin;

import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.plugin.webfragment.DefaultSimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserAdminHistoryManager;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.config.properties.APKeys.JIRA_MAX_ADMIN_HISTORY_DROPDOWN_ITEMS;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.1
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAdminHistoryLinkFactory
{
    private AdminHistoryLinkFactory linkFactory;
    private User user;

    @Mock private ApplicationProperties applicationProperties;
    @Mock private DefaultSimpleLinkManager simpleLinkManager;
    @Mock private PluginAccessor pluginAccessor;
    @Mock private UserAdminHistoryManager historyManager;

    @Before
    public void setUp() throws Exception
    {
        user = new MockUser("admin");
        linkFactory = new AdminHistoryLinkFactory(historyManager, simpleLinkManager, pluginAccessor, applicationProperties);
    }

    @Test
    public void testNullUSerNullHistory()
    {
        // Set up
        when(historyManager.getAdminPageHistoryWithoutPermissionChecks(null)).thenReturn(null);
        when(applicationProperties.getDefaultBackedString(JIRA_MAX_ADMIN_HISTORY_DROPDOWN_ITEMS)).thenReturn("5");

        // Invoke and check
        assertTrue(linkFactory.getLinks(null, null).isEmpty());
    }

    @Test
    public void testNullHistory()
    {
        when(historyManager.getAdminPageHistoryWithoutPermissionChecks(user)).thenReturn(null);
        when(applicationProperties.getDefaultBackedString(JIRA_MAX_ADMIN_HISTORY_DROPDOWN_ITEMS)).thenReturn("5");

        assertTrue(linkFactory.getLinks(user, null).isEmpty());
    }

    @Test
    public void testEmptyHistory()
    {
        final List<UserHistoryItem> objects = emptyList();
        when(historyManager.getAdminPageHistoryWithoutPermissionChecks(user)).thenReturn(objects);
        when(applicationProperties.getDefaultBackedString(JIRA_MAX_ADMIN_HISTORY_DROPDOWN_ITEMS)).thenReturn("5");

        assertTrue(linkFactory.getLinks(user, null).isEmpty());
    }

    @Test
    public void testOneHistory()
    {
        final UserHistoryItem history = mock(UserHistoryItem.class);
        final SimpleLink link = new SimpleLinkImpl("admin_link_1", "Admin Link", "Admin Link title", null, "admin-item-link", "/admin/url", null);
        final SimpleLinkSection mockLinkSection = mock(SimpleLinkSection.class);

        when(historyManager.getAdminPageHistoryWithoutPermissionChecks(user))
                .thenReturn(singletonList(history));


        Plugin plugin = mock(Plugin.class);

        when(simpleLinkManager.getSectionsForLocation(eq("system.admin"), eq(user), isA(JiraHelper.class)))
                .thenReturn(singletonList(mockLinkSection));
        when(mockLinkSection.getId())
                .thenReturn("admin_section");

        when(simpleLinkManager.getLinksForSection(eq("system.admin/admin_section"), eq(user), isA(JiraHelper.class)))
                .thenReturn(singletonList(link));
        when(history.getEntityId())
                .thenReturn("admin_link_1");
        when(history.getData())
                .thenReturn("/admin/url");

        when(plugin.getKey())
                .thenReturn("");
        when(pluginAccessor.getPlugin("jira.top.navigation.bar"))
                .thenReturn(plugin);
        when(pluginAccessor.isPluginEnabled(isA(String.class)))
                .thenReturn(false);

        when(applicationProperties.getDefaultBackedString(JIRA_MAX_ADMIN_HISTORY_DROPDOWN_ITEMS)).thenReturn("5");

        final List<SimpleLink> returnList = linkFactory.getLinks(user, null);
        final List<SimpleLink> expectedList = singletonList(link);

        assertEquals(expectedList, returnList);
    }

    @Test
    public void testMultipleHistory()
    {
        final UserHistoryItem history = mock(UserHistoryItem.class);
        when(history.getEntityId()).thenReturn("admin_link_1");
        when(history.getData()).thenReturn("/admin/url");

        final UserHistoryItem history2 = mock(UserHistoryItem.class);
        when(history2.getEntityId()).thenReturn("admin_link_2");
        when(history2.getData()).thenReturn("/admin/url2");

        final SimpleLink link = new SimpleLinkImpl(
                "admin_link_1", "Admin Link", "Admin Link title", null, "admin-item-link", "/admin/url", null);
        final SimpleLink link2 =new SimpleLinkImpl(
                "admin_link_2", "Admin Link 2", "Admin Link title 2", null, "admin-item-link", "/admin/url2", null);

        final SimpleLinkSection mockLinkSection = mock(SimpleLinkSection.class);
        when(mockLinkSection.getId()).thenReturn("admin_section");

        final Plugin plugin = mock(Plugin.class);
        when(plugin.getKey()).thenReturn("");

        when(applicationProperties.getDefaultBackedString(JIRA_MAX_ADMIN_HISTORY_DROPDOWN_ITEMS)).thenReturn("5");
        when(historyManager.getAdminPageHistoryWithoutPermissionChecks(user)).thenReturn(asList(history, history2));
        when(pluginAccessor.getPlugin("jira.top.navigation.bar")).thenReturn(plugin);
        when(pluginAccessor.isPluginEnabled(isA(String.class))).thenReturn(false);
        //noinspection unchecked
        when(simpleLinkManager.getLinksForSection(eq("system.admin/admin_section"), eq(user), isA(JiraHelper.class)))
                .thenReturn(asList(link, link2), singletonList(link2));
        when(simpleLinkManager.getSectionsForLocation(eq("system.admin"), eq(user), isA(JiraHelper.class)))
                .thenReturn(singletonList(mockLinkSection));

        // Invoke
        final List<SimpleLink> actualLinks = linkFactory.getLinks(user, null);

        // Check
        assertEquals(asList(link, link2), actualLinks);
    }
}
