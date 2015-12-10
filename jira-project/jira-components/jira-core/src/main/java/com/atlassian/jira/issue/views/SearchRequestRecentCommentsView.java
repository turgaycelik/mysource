package com.atlassian.jira.issue.views;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.RecentCommentManager;
import com.atlassian.jira.issue.comments.util.CommentIterator;
import com.atlassian.jira.issue.renderers.CommentFieldRenderContext;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.views.util.RssViewUtils;
import com.atlassian.jira.issue.views.util.SearchRequestViewUtils;
import com.atlassian.jira.issue.views.util.WordViewUtils;
import com.atlassian.jira.plugin.searchrequestview.AbstractSearchRequestView;
import com.atlassian.jira.plugin.searchrequestview.RequestHeaders;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestParams;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.web.bean.PagerFilter;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class SearchRequestRecentCommentsView extends AbstractSearchRequestView
{
    public static final String DEFAULT_DESCRIPTION = "An XML representation of a search request";

    private final JiraAuthenticationContext authenticationContext;
    private final ApplicationProperties applicationProperties;
    private final RendererManager rendererManager;
    private final RecentCommentManager recentCommentManager;
    private final BuildUtilsInfo buildUtilsInfo;

    public SearchRequestRecentCommentsView(JiraAuthenticationContext authenticationContext, ApplicationProperties applicationProperties,
            RendererManager rendererManager, RecentCommentManager recentCommentManager, final BuildUtilsInfo buildUtilsInfo)
    {
        this.authenticationContext = authenticationContext;
        this.applicationProperties = applicationProperties;
        this.rendererManager = rendererManager;
        this.recentCommentManager = recentCommentManager;
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
    }

    public void writeHeaders(final SearchRequest searchRequest, final RequestHeaders requestHeaders)
    {
        // JRA-17367 set cache control headers so that Outlook 2007's RSS readers works over SSL. This means
        // that you can't have no-cache or no-store or it will fail.
        WordViewUtils.writeGenericNoCacheHeaders(requestHeaders);
    }

    public void writeSearchResults(SearchRequest searchRequest, SearchRequestParams searchRequestParams, Writer writer) throws SearchException
    {
        SearchRequestRecentCommentsView commentView = getRecentCommentsView();
        CommentIterator recentComments = null;
        try
        {
            if (commentView == null)
            {
                writer.write("Could not find plugin of class 'SearchRequestRecentCommentsView'.  This is needed for this plugin to work");
                return;
            }

            recentComments = recentCommentManager.getRecentComments(searchRequest, authenticationContext.getUser());
            final PagerFilter pagerFilter = searchRequestParams.getPagerFilter();
            writer.write(getHeader(searchRequest, pagerFilter, recentComments.size()));
            writeBody(recentComments, writer, pagerFilter);
            writer.write(getFooter());
        }
        catch (IOException e)
        {
            throw new DataAccessException(e);
        }
        finally
        {
            if (recentComments != null)
            {
                recentComments.close();
            }
        }
    }

    private SearchRequestRecentCommentsView getRecentCommentsView()
    {
        return SearchRequestViewUtils.getSearchRequestView(SearchRequestRecentCommentsView.class);
    }

    private void writeBody(CommentIterator recentComments, Writer writer, PagerFilter pagerFilter)
    {
        int traversedComments = 0;
        while (recentComments.hasNext() && traversedComments < pagerFilter.getEnd())
        {
            Comment comment = recentComments.next();
            writeAction(writer, comment);
            traversedComments++;
        }
    }

    private void writeAction(Writer writer, Comment comment)
    {

        Map actionParams = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
        actionParams.put("issue", comment.getIssue());
        actionParams.put("description", getActionHtml(comment)); // need to use the renderer!
        actionParams.put("comment", comment);

        try
        {
            writer.write(descriptor.getHtml("view", actionParams));
        }
        catch (IOException e)
        {
            throw new DataAccessException(e);
        }
    }

    private String getActionHtml(Comment comment)
    {
        return rendererManager.getRenderedContent(new CommentFieldRenderContext(comment));
    }

    private String getHeader(SearchRequest searchRequest, PagerFilter pagerFilter, int totalComments)
    {

        long startComment = pagerFilter.getStart();
        long tempMax = pagerFilter.getMax() < 0 ? 0 : pagerFilter.getMax();
        long endComment = Math.min(startComment + tempMax, totalComments);

        Map headerParams = JiraVelocityUtils.getDefaultVelocityParams(new HashMap(), authenticationContext);
        headerParams.put("title", SearchRequestViewUtils.getTitle(searchRequest, applicationProperties.getString(APKeys.JIRA_TITLE)));
        VelocityRequestContext velocityRequestContext = new DefaultVelocityRequestContextFactory(applicationProperties).getJiraVelocityRequestContext();
        headerParams.put("link", SearchRequestViewUtils.getLink(searchRequest, velocityRequestContext.getBaseUrl(), authenticationContext.getLoggedInUser()));
        headerParams.put("buildInfo", buildUtilsInfo.getBuildInformation());
        headerParams.put("currentDate", new Date());
        headerParams.put("description", getDescription(searchRequest));
        headerParams.put("rssLocale", RssViewUtils.getRssLocale(authenticationContext.getLocale()));
        headerParams.put("startcomment", startComment);
        headerParams.put("endcomment", endComment);
        headerParams.put("totalcomment", (long) totalComments);

        headerParams.put("version", buildUtilsInfo.getVersion());
        headerParams.put("buildNumber", buildUtilsInfo.getCurrentBuildNumber());
        headerParams.put("buildDate", new SimpleDateFormat("dd-MM-yyyy").format(buildUtilsInfo.getCurrentBuildDate()));
        return descriptor.getHtml("header", headerParams);
    }

    private String getDescription(SearchRequest searchRequest)
    {
        return searchRequest.getDescription() != null ? searchRequest.getDescription() : DEFAULT_DESCRIPTION;
    }

    public String getFooter()
    {
        return descriptor.getHtml("footer", Collections.<String,String>emptyMap());
    }
}
