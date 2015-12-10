package com.atlassian.jira.issue.views;

import com.atlassian.core.util.XMLUtils;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.views.util.RssViewUtils;
import com.atlassian.jira.issue.views.util.SearchRequestViewBodyWriterUtil;
import com.atlassian.jira.issue.views.util.SearchRequestViewUtils;
import com.atlassian.jira.issue.views.util.WordViewUtils;
import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.plugin.issueview.AbstractIssueView;
import com.atlassian.jira.plugin.searchrequestview.AbstractSearchRequestView;
import com.atlassian.jira.plugin.searchrequestview.RequestHeaders;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestParams;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
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
public class SearchRequestRSSView extends AbstractSearchRequestView
{
    public static final String DEFAULT_DESCRIPTION = "An XML representation of a search request";

    private static final Logger log = Logger.getLogger(SearchRequestRSSView.class);

    private final JiraAuthenticationContext authenticationContext;
    private final ApplicationProperties applicationProperties;
    private final BuildUtilsInfo buildUtilsInfo;

    private SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil;


    public SearchRequestRSSView(JiraAuthenticationContext authenticationContext, ApplicationProperties applicationProperties, SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil, final BuildUtilsInfo buildUtilsInfo)
    {
        this.authenticationContext = authenticationContext;
        this.applicationProperties = applicationProperties;
        this.searchRequestViewBodyWriterUtil = searchRequestViewBodyWriterUtil;
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
    }

    public void writeHeaders(final SearchRequest searchRequest, final RequestHeaders requestHeaders)
    {
        // JRA-17367 set cache control headers so that Outlook 2007's RSS readers works over SSL. This means
        // that you can't have no-cache or no-store or it will fail.
        WordViewUtils.writeGenericNoCacheHeaders(requestHeaders);
    }

    public void writeSearchResults(SearchRequest searchRequest, final SearchRequestParams searchRequestParams, Writer writer)
    {
        IssueHtmlView htmlView = getIssueHtmlView();

        try
        {
            if (htmlView == null)
            {
                writer.write("Could not find plugin of class 'IssueHtmlView'.  This is needed for this plugin to work");
                return;
            }

            writer.write(getHeader(searchRequest, searchRequestParams));
            final String styleSheet = getIssueHtmlView().getStyleSheetHtml();

            SingleIssueWriter singleIssueWriter = new SingleIssueWriter()
            {

                public void writeIssue(Issue issue, AbstractIssueView issueView, Writer writer) throws IOException
                {
                    if(log.isDebugEnabled())
                    {
                        log.debug("About to write RSS view for issue [" + issue.getKey() + "].");
                    }
                    Map bodyParams = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
                    String htmlBody = issueView.getBody(issue, searchRequestParams);

                    bodyParams.put("issue", issue);
                    bodyParams.put("issueDescription", XMLUtils.escape(styleSheet + " " + htmlBody));
                    String body = descriptor.getHtml("view", bodyParams);

                    writer.write(body);
                }
            };

            searchRequestViewBodyWriterUtil.writeBody(writer, getIssueHtmlView(), searchRequest, singleIssueWriter, searchRequestParams.getPagerFilter());
            writer.write(getFooter());
        }
        catch (SearchException e)
        {
            throw new DataAccessException(e);
        }
        catch (IOException e)
        {
            throw new DataAccessException(e);
        }

    }

    private IssueHtmlView getIssueHtmlView()
    {
        return SearchRequestViewUtils.getIssueView(IssueHtmlView.class);
    }

    private String getHeader(SearchRequest searchRequest, SearchRequestParams searchRequestParams)
    {
        return getHeader(searchRequest, searchRequestParams, applicationProperties, descriptor);
    }

    private String getHeader(SearchRequest searchRequest, SearchRequestParams searchRequestParams,
        ApplicationProperties applicationProperties, JiraResourcedModuleDescriptor descriptor)
    {
        long startIssue = searchRequestParams.getPagerFilter().getStart();
        long totalIssues = getSearchCount(searchRequest, searchRequestParams);
        long tempMax = searchRequestParams.getPagerFilter().getMax() < 0 ? 0 : searchRequestParams.getPagerFilter().getMax();
        long endIssue = Math.min(startIssue + tempMax, totalIssues);

        Map headerParams = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
        headerParams.put("title", SearchRequestViewUtils.getTitle(searchRequest, applicationProperties.getString(APKeys.JIRA_TITLE)));
        VelocityRequestContext velocityRequestContext = new DefaultVelocityRequestContextFactory(applicationProperties).getJiraVelocityRequestContext();
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
        return descriptor.getHtml("header", headerParams);
    }

    private String getDescription(SearchRequest searchRequest)
    {
        return searchRequest.getDescription() != null ? searchRequest.getDescription() : SearchRequestRSSView.DEFAULT_DESCRIPTION;
    }

    private String getFooter()
    {
        return descriptor.getHtml("footer", Collections.<String, String>emptyMap());
    }

    /*
     * Get the total search count. The search count would first be retrieved from the SearchRequestParams. If not found,
     * retrieve using the search provider instead.
     */
    private long getSearchCount(SearchRequest searchRequest, SearchRequestParams searchRequestParams)
    {
        String searchCount = (String) searchRequestParams.getSession().get("searchCount");
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
            catch(SearchException se)
            {
                return 0;
            }
        }
    }
}
