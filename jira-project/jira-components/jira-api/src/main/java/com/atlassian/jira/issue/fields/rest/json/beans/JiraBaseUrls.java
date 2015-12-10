package com.atlassian.jira.issue.fields.rest.json.beans;

/**
 * A simple component for getting the base url of the app
 *
 * @since v5.0
 */
public interface JiraBaseUrls
{
    /**
     * @return The canonical base URL for this instance. It will return an absolute URL (eg. "http://example.com/jira/").
     */
    String baseUrl();
    /**
     * @return The canonical base URL for the /rest/api/2/ endpoint. It will return an absolute URL (eg. "http://example.com/jira/rest/api/2/").
     */
    String restApi2BaseUrl();
}
