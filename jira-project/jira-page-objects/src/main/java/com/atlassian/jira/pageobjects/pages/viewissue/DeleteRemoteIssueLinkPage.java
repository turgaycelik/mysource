package com.atlassian.jira.pageobjects.pages.viewissue;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Represents the delete remote issue link page.
 *
 * @since v5.0
 */
public class DeleteRemoteIssueLinkPage extends AbstractDeleteLinkPage
{
    private static final String URI_TEMPLATE = "/secure/DeleteRemoteIssueLink.jspa?id=%d&remoteIssueLinkId=%d&atl_token=%s";

    private final Long remoteIssueLinkId;
    private final String uri;

    public DeleteRemoteIssueLinkPage(String issueKey, Long issueId, Long remoteIssueLinkId, String xsrfToken)
    {
        super(issueKey, issueId);
        this.remoteIssueLinkId = notNull(remoteIssueLinkId);
        this.uri = String.format(URI_TEMPLATE, issueId, remoteIssueLinkId, xsrfToken);
    }

    @Override
    public String getUrl()
    {
        return uri;
    }

    public Long getRemoteIssueLinkId()
    {
        return remoteIssueLinkId;
    }
}
