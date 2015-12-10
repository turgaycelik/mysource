package com.atlassian.jira.plugin.issuelink;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.issue.link.RemoteIssueLink;

import java.util.Map;

/**
 * Defines an issue link renderer to customise how issue links appear.
 *
 * @since v5.0
 */
@PublicSpi
public interface IssueLinkRenderer
{
    /**
     * Returns the context used by the template to render the initial HTML. Implementers of this method
     * should not make remote calls, use {@link #getFinalContext(RemoteIssueLink, Map)} for that purpose.
     *
     * The resulting HTML will be injected as follows:
     * {@code
     * <dl class="links-list">
     *    <dt>Relationship Text</dt>
     *    <!-- ... Other Issue Links ... -->
     *    <dd id="uniqueHtmlElementId" class="remote-link">
     *        <div class="link-content">
     *            <!-- ISSUE LINK RENDERER CONTENT HERE -->
     *        </div>
     *
     *        <div class="delete-link" id="delete_uniqueHtmlElementId">
     *            <a class="icon icon-delete" title="Delete Link" id="delete-link_uniqueHtmlElementId" href="delete_remote_issue_link_url"><span>Delete Link</span></a>
     *        </div>
     *     </dd>
     *    <!-- ... Other Issue Links ... -->
     * </dl>
     * }
     *
     * @param remoteIssueLink remote issue link
     * @param context the contextual information that can be used during
     *  rendering.
     * @return context used to render the initial HTML.
     */
    Map<String, Object> getInitialContext(RemoteIssueLink remoteIssueLink, Map<String, Object> context);

    /**
     * Returns the context used to render the final HTML. This method will only be called if
     * {@link #requiresAsyncLoading(RemoteIssueLink)} returns <tt>true</tt>.
     *
     * The resulting HTML will be injected as follows:
     * {@code
     * <dl class="links-list">
     *    <dt>Relationship Text</dt>
     *    <!-- ... Other Issue Links ... -->
     *    <dd id="uniqueHtmlElementId" class="remote-link">
     *        <div class="link-content">
     *            <!-- ISSUE LINK RENDERER CONTENT HERE -->
     *        </div>
     *
     *        <div class="delete-link" id="delete_uniqueHtmlElementId">
     *            <a class="icon icon-delete" title="Delete Link" id="delete-link_uniqueHtmlElementId" href="delete_remote_issue_link_url"><span>Delete Link</span></a>
     *        </div>
     *     </dd>
     *    <!-- ... Other Issue Links ... -->
     * </dl>
     * }
     *
     * @param remoteIssueLink remote issue link
     * @param context the contextual information that can be used during rendering.
     * @return velocity context used to render the final HTML
     */
    Map<String, Object> getFinalContext(RemoteIssueLink remoteIssueLink, Map<String, Object> context);

    /**
     * Returns <tt>true</tt> if an AJAX call is required to retrieve the final HTML. If <tt>true</tt> is returned,
     * then {@link #getFinalContext(com.atlassian.jira.issue.link.RemoteIssueLink, java.util.Map)} will be
     * called to retrieve the final HTML for the issue link.
     *
     * @param remoteIssueLink remote issue link
     * @return <tt>true</tt> if an AJAX call is required to retrieve the final HTML
     */
    boolean requiresAsyncLoading(RemoteIssueLink remoteIssueLink);

    /**
     * Returns <tt>true</tt> if the remote issue link should be displayed.
     *
     * @param remoteIssueLink remote issue link
     * @return <tt>true</tt> if the remote issue link should be displayed
     */
    boolean shouldDisplay(RemoteIssueLink remoteIssueLink);
}
