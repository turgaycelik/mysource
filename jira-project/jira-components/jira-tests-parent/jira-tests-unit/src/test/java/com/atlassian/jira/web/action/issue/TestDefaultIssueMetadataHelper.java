package com.atlassian.jira.web.action.issue;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.plugin.webresource.JiraWebResourceManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultIssueMetadataHelper}.
 *
 * @since v6.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultIssueMetadataHelper
{
    @Mock private JiraAuthenticationContext authenticationContext;
    @Mock private AvatarService avatarService;
    private URI avatarURI;
    @Mock private Issue issue;
    private IssueMetadataHelper issueMetadataHelper;
    @Mock private PermissionManager permissionManager;
    private User user;
    @Mock private UserPickerSearchService userPickerSearchService;
    @Mock private JiraWebResourceManager webResourceManager;

    @Before
    public void setUp() throws Exception
    {
        user = new MockUser("fred");
        when(authenticationContext.getLoggedInUser()).thenReturn(user);

        avatarURI = new URI("/jira/secure/useravatar?avatarId=12345");
        when(avatarService.getAvatarURL(user, null, Avatar.Size.SMALL))
                .thenReturn(avatarURI);

        when(issue.getKey()).thenReturn("JRA-123");
        when(permissionManager.hasPermission(Permissions.MANAGE_WATCHER_LIST,
                issue, user)).thenReturn(true);

        when(userPickerSearchService.canPerformAjaxSearch(user))
                .thenReturn(true);

        issueMetadataHelper = new DefaultIssueMetadataHelper(
                authenticationContext, avatarService,
                permissionManager, userPickerSearchService);
    }

    @Test
    public void testGetMetadata() throws Exception
    {
        Map<String, String> expectedMetadata = new HashMap<String, String>();
        expectedMetadata.put("can-edit-watchers", "true");
        expectedMetadata.put("can-search-users", "true");
        expectedMetadata.put("default-avatar-url", avatarURI.toString());
        expectedMetadata.put("issue-key", "JRA-123");

        assertEquals(expectedMetadata, getMetadata());

        // The metadata should change if different values are returned.
        when(permissionManager.hasPermission(Permissions.MANAGE_WATCHER_LIST,
                issue, user)).thenReturn(false);
        expectedMetadata.put("can-edit-watchers", "false");
        assertEquals(expectedMetadata, getMetadata());

        when(userPickerSearchService.canPerformAjaxSearch(user))
                .thenReturn(false);
        expectedMetadata.put("can-search-users", "false");
        assertEquals(expectedMetadata, getMetadata());

        avatarURI = new URI("/jira/secure/useravatar?avatarId=54321");
        when(avatarService.getAvatarURL(user, null, Avatar.Size.SMALL))
                .thenReturn(avatarURI);
        expectedMetadata.put("default-avatar-url", avatarURI.toString());
        assertEquals(expectedMetadata, getMetadata());

        when(issue.getKey()).thenReturn("CONF-123");
        expectedMetadata.put("issue-key", "CONF-123");
        assertEquals(expectedMetadata, getMetadata());
    }

    @Test
    public void testPutMetadata()
    {
        issueMetadataHelper.putMetadata(issue, mock(SearchRequest.class),
                webResourceManager);

        Map<String, String> metadata = getMetadata();
        for (Map.Entry<String, String> entry : metadata.entrySet())
        {
            verify(webResourceManager, times(1)).putMetadata(entry.getKey(), entry.getValue());
        }
    }

    private Map<String, String> getMetadata()
    {
        return issueMetadataHelper.getMetadata(issue, mock(SearchRequest.class));
    }
}