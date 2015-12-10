package com.atlassian.jira.plugin.issuelink;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.issue.link.RemoteIssueLink;

import java.util.Map;

/**
 * Base class for an issue link renderer that does not perform any asynchronous rendering of an issue link and will
 * always display the issue link.
 *
 * @since v5.0
 */
@PublicSpi
public abstract class AbstractIssueLinkRenderer implements IssueLinkRenderer
{
    /**
     * Refer to documentation in {@link IssueLinkRenderer#getFinalContext(com.atlassian.jira.issue.link.RemoteIssueLink, java.util.Map)}.
     *
     * Subclasses requiring asynchronous loading should override this method, otherwise this method will always throw an
     * {@link UnsupportedOperationException}.
     */
    @Override
    public Map<String, Object> getFinalContext(RemoteIssueLink remoteIssueLink, Map<String, Object> context)
    {
        throw new UnsupportedOperationException("Asynchronous rendering of remote issue link is not supported");
    }

    /**
     * Refer to documentation in {@link IssueLinkRenderer#requiresAsyncLoading(com.atlassian.jira.issue.link.RemoteIssueLink)}.
     *
     * Subclasses requiring asynchronous loading should override this method and return <tt>true</tt>, otherwise this
     * method will always return <tt>false</tt>.
     *
     * @param remoteIssueLink remote issue link
     * @return <tt>false</tt> by default
     */
    @Override
    public boolean requiresAsyncLoading(RemoteIssueLink remoteIssueLink)
    {
        return false;
    }

    /**
     * Refer to documentation in {@link IssueLinkRenderer#shouldDisplay(com.atlassian.jira.issue.link.RemoteIssueLink)}.
     *
     * @param remoteIssueLink remote issue link
     * @return <tt>true</tt> by default
     */
    @Override
    public boolean shouldDisplay(RemoteIssueLink remoteIssueLink)
    {
        return true;
    }
}
