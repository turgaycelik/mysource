package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.plugin.webresource.JiraWebResourceManager;

import java.util.Map;

/**
 * A helper that inserts {@link Issue} metadata into a
 * {@link com.atlassian.plugin.webresource.WebResourceManager}.
 *
 * @since v6.0
 */
public interface IssueMetadataHelper
{
    /**
     * Construct a map of a given issue's metadata.
     *
     * @param issue The issue.
     * @param searchRequest The user's session search.
     */
    public Map<String, String> getMetadata(
            final Issue issue,
            final SearchRequest searchRequest);

    /**
     * Insert a given issue's metadata into a
     * {@link com.atlassian.plugin.webresource.WebResourceManager}.
     *
     * @param issue The issue.
     * @param searchRequest The user's session search.
     * @param webResourceManager The web resource manager.
     */
    public void putMetadata(
            final Issue issue,
            final SearchRequest searchRequest,
            final JiraWebResourceManager webResourceManager);
}