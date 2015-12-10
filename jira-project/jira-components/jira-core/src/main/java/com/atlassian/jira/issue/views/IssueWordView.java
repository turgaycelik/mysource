package com.atlassian.jira.issue.views;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.views.util.IssueViewUtil;
import com.atlassian.jira.issue.views.util.WordViewUtils;
import com.atlassian.jira.plugin.issueview.IssueViewRequestParams;
import com.atlassian.jira.plugin.searchrequestview.RequestHeaders;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.util.FileIconBean;

/**
 * A view of an issue that produces a full XML view of an issue.  It is also valid RSS.
 */
public class IssueWordView extends AbstractIssueHtmlView
{
    public IssueWordView(final JiraAuthenticationContext authenticationContext,
            final ApplicationProperties applicationProperties, final CommentManager commentManager,
            final FileIconBean fileIconBean, final FieldScreenRendererFactory fieldScreenRendererFactory,
            final IssueViewUtil issueViewUtil, final FieldVisibilityManager fieldVisibilityManager)
    {
        super(authenticationContext, applicationProperties, commentManager, fileIconBean, fieldScreenRendererFactory,
                issueViewUtil, fieldVisibilityManager);
    }

    protected String getLinkToPrevious(final Issue issue)
    {
        return null; // we don't want a link in the 'word' view.
    }

    protected boolean printCssLinks()
    {
        return false;
    }

    public void writeHeaders(final Issue issue, final RequestHeaders requestHeaders,
            final IssueViewRequestParams issueViewRequestParams)
    {
        WordViewUtils.writeGenericNoCacheHeaders(requestHeaders);
        requestHeaders.addHeader("content-disposition", "attachment;filename=\"" + issue.getKey() + ".doc\";");
    }
}
