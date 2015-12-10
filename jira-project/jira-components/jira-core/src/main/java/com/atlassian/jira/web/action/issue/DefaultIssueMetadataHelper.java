package com.atlassian.jira.web.action.issue;

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

import java.util.HashMap;
import java.util.Map;

/**
 * The default implementation of {@link IssueMetadataHelper}.
 *
 * @since v6.0
 */
public class DefaultIssueMetadataHelper implements IssueMetadataHelper
{
    private final JiraAuthenticationContext authenticationContext;
    private final AvatarService avatarService;
    private final PermissionManager permissionManager;
    private final UserPickerSearchService userPickerSearchService;

    public DefaultIssueMetadataHelper(
            final JiraAuthenticationContext authenticationContext,
            final AvatarService avatarService,
            final PermissionManager permissionManager,
            final UserPickerSearchService userPickerSearchService)
    {
        this.authenticationContext = authenticationContext;
        this.avatarService = avatarService;
        this.permissionManager = permissionManager;
        this.userPickerSearchService = userPickerSearchService;
    }

    @Override
    public Map<String, String> getMetadata(Issue issue, SearchRequest searchRequest)
    {
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("can-edit-watchers", canEditWatchers(issue));
        metadata.put("can-search-users", canSearchUsers());
        metadata.put("default-avatar-url", getDefaultAvatarURL());
        metadata.put("issue-key", issue.getKey());
        return metadata;
    }

    @Override
    public void putMetadata(
            final Issue issue,
            final SearchRequest searchRequest,
            final JiraWebResourceManager webResourceManager)
    {
        Map<String, String> metadata = getMetadata(issue, searchRequest);
        for (Map.Entry<String, String> entry : metadata.entrySet())
        {
            webResourceManager.putMetadata(entry.getKey(), entry.getValue());
        }
    }

    /**
     * @param issue An issue.
     * @return whether the current user can manage {@code issue}'s watchers.
     */
    private String canEditWatchers(final Issue issue)
    {
        return Boolean.toString(permissionManager.hasPermission(
                Permissions.MANAGE_WATCHER_LIST, issue, getLoggedInUser()));
    }

    /**
     * @return whether the current user can search for users via AJAX.
     */
    private String canSearchUsers()
    {
        return Boolean.toString(userPickerSearchService.canPerformAjaxSearch(
                getLoggedInUser()));
    }

    /**
     * @return the URL of the default avatar.
     */
    private String getDefaultAvatarURL()
    {
        return avatarService.getAvatarURL(getLoggedInUser(), null,
                Avatar.Size.SMALL).toString();
    }

    private User getLoggedInUser()
    {
        return authenticationContext.getLoggedInUser();
    }
}