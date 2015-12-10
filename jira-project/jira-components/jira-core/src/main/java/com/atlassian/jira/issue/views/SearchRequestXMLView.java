package com.atlassian.jira.issue.views;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.views.util.RssViewUtils;
import com.atlassian.jira.issue.views.util.SearchRequestViewBodyWriterUtil;
import com.atlassian.jira.issue.views.util.SearchRequestViewUtils;
import com.atlassian.jira.plugin.issueview.AbstractIssueView;
import com.atlassian.jira.plugin.searchrequestview.AbstractSearchRequestView;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestParams;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.RequestContextParameterHolder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 *
 */
public class SearchRequestXMLView extends AbstractSearchRequestView
{
    public static final String DEFAULT_DESCRIPTION = "An XML representation of a search request";
    private static final Logger log = Logger.getLogger(SearchRequestXMLView.class);

    private final JiraAuthenticationContext authenticationContext;
    private final ApplicationProperties applicationProperties;
    private final SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil;
    private final BuildUtilsInfo buildUtilsInfo;

    public SearchRequestXMLView(final JiraAuthenticationContext authenticationContext, final ApplicationProperties applicationProperties, final SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil, final BuildUtilsInfo buildUtilsInfo)
    {
        this.authenticationContext = authenticationContext;
        this.applicationProperties = applicationProperties;
        this.searchRequestViewBodyWriterUtil = searchRequestViewBodyWriterUtil;
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
    }

    @Override
    public void writeSearchResults(final SearchRequest searchRequest, final SearchRequestParams searchRequestParams, final Writer writer)
    {
        final IssueXMLView xmlView = getIssueXMLView();

        try
        {
            if (xmlView == null)
            {
                writer.write("Could not find plugin of class 'IssueXMLView'.  This is needed for this plugin to work");
                return;
            }

            writer.write(getHeader(searchRequest, searchRequestParams));

            final SingleIssueWriter singleIssueWriter = new SingleIssueWriter()
            {
                public void writeIssue(final Issue issue, final AbstractIssueView issueView, final Writer writer)
                        throws IOException
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("About to write XML view for issue [" + issue.getKey() + "].");
                    }
                    writer.write(issueView.getBody(issue, searchRequestParams));
                }
            };

            searchRequestViewBodyWriterUtil.writeBody(writer, getIssueXMLView(), searchRequest, singleIssueWriter,
                    searchRequestParams.getPagerFilter());
            writer.write(getFooter());
        }
        catch (final SearchException e)
        {
            throw new DataAccessException(e);
        }
        catch (final IOException e)
        {
            throw new DataAccessException(e);
        }

    }

    private IssueXMLView getIssueXMLView()
    {
        return SearchRequestViewUtils.getIssueView(IssueXMLView.class);
    }

    public String getHeader(final SearchRequest searchRequest, final SearchRequestParams searchRequestParams)
    {
        final long startIssue = searchRequestParams.getPagerFilter().getStart();
        final long totalIssues = getSearchCount(searchRequest, searchRequestParams);
        final long tempMax = searchRequestParams.getPagerFilter().getMax() < 0 ? 0 : searchRequestParams.getPagerFilter().getMax();
        final long endIssue = Math.min(startIssue + tempMax, totalIssues);

        final Map<String, Object> headerParams = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
        headerParams.put("title", SearchRequestViewUtils.getTitle(searchRequest, applicationProperties.getString(APKeys.JIRA_TITLE)));
        final VelocityRequestContext velocityRequestContext = new DefaultVelocityRequestContextFactory(applicationProperties).getJiraVelocityRequestContext();
        headerParams.put("link", SearchRequestViewUtils.getLink(searchRequest, velocityRequestContext.getBaseUrl(), authenticationContext.getLoggedInUser()));
        headerParams.put("buildInfo", buildUtilsInfo.getBuildInformation());
        headerParams.put("currentDate", new Date());
        headerParams.put("description", getDescription(searchRequest));
        headerParams.put("rssLocale", RssViewUtils.getRssLocale(authenticationContext.getLocale()));
        headerParams.put("startissue", startIssue);
        headerParams.put("endissue", endIssue);
        headerParams.put("totalissue", totalIssues);
        headerParams.put("version", buildUtilsInfo.getVersion());
        headerParams.put("buildNumber", buildUtilsInfo.getCurrentBuildNumber());
        headerParams.put("buildDate", new SimpleDateFormat("dd-MM-yyyy").format(buildUtilsInfo.getCurrentBuildDate()));

        headerParams.put("customViewRequested", searchRequestParams.getIssueViewFieldParams().isCustomViewRequested());

        final RequestContextParameterHolder requestParameters = velocityRequestContext.getRequestParameters();
        if (requestParameters != null)
        {
            final String requestURL = requestParameters.getRequestURL();
            if (requestURL != null)
            {
                final String queryString = StringEscapeUtils.escapeXml(requestParameters.getQueryString());
                // Prepare the URL such that in the velocity template we can easily append parameters to it
                if (queryString != null)
                {
                    headerParams.put("exampleURLPrefix", requestURL + "?" + queryString + (queryString.endsWith("&amp;") ? "" : "&amp;"));
                }
                else
                {
                    headerParams.put("exampleURLPrefix", requestURL + "?");
                }
            }
        }

        return descriptor.getHtml("header", headerParams);
    }

    private String getDescription(final SearchRequest searchRequest)
    {
        return searchRequest.getDescription() != null ? searchRequest.getDescription() : DEFAULT_DESCRIPTION;
    }

    public String getFooter()
    {
        return descriptor.getHtml("footer", Collections.<String, String>emptyMap());
    }

    /*
     * Get the total search count. The search count would first be retrieved from the SearchRequestParams. If not found,
     * retrieve using the search provider instead.
     */
    private long getSearchCount(final SearchRequest searchRequest, final SearchRequestParams searchRequestParams)
    {
        final String searchCount = (String) searchRequestParams.getSession().get("searchCount");
        if (StringUtils.isNumeric(searchCount))
        {
            return Long.parseLong(searchCount);
        }
        else
        {
            try
            {
                return searchRequestViewBodyWriterUtil.searchCount(searchRequest);
            }
            catch (final SearchException se)
            {
                return 0;
            }
        }
    }
}
