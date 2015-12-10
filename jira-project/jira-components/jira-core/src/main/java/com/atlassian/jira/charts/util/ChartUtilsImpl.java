package com.atlassian.jira.charts.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.Base64InputStreamConsumer;
import com.atlassian.jira.util.SimpleErrorCollection;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jfree.chart.encoders.EncoderUtil;
import org.jfree.chart.encoders.ImageFormat;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Map;

/**
 * Utility class for charting
 *
 * @since v4.0
 */
public class ChartUtilsImpl implements ChartUtils
{
    private static final Logger log = Logger.getLogger(ChartUtilsImpl.class);

    private final JiraAuthenticationContext authenticationContext;
    private final ProjectManager projectManager;
    private final SearchService searchService;
    private final SearchRequestService searchRequestService;
    private final JiraHome jiraHome;

    public ChartUtilsImpl(SearchRequestService searchRequestService, JiraAuthenticationContext authenticationContext,
            ProjectManager projectManager, final SearchService searchService, final JiraHome jiraHome)
    {
        this.searchRequestService = searchRequestService;
        this.authenticationContext = authenticationContext;
        this.projectManager = projectManager;
        this.searchService = searchService;
        this.jiraHome = jiraHome;
    }


    public SearchRequest retrieveOrMakeSearchRequest(final String projectOrFilterId, final Map<String, Object> params)
    {
        SearchRequest sr = null;

        final User user = authenticationContext.getLoggedInUser();
        if (projectOrFilterId.startsWith("filter-"))
        {
            Long filterId = new Long(projectOrFilterId.substring(7));
            sr = searchRequestService.getFilter(
                    new JiraServiceContextImpl(user, new SimpleErrorCollection()), filterId);
            if (sr != null)
            {
                params.put("searchRequest", sr);
            }
        }
        else if (projectOrFilterId.startsWith("project-"))
        {
            Long projectId = new Long(projectOrFilterId.substring(8));
            final Project project = projectManager.getProjectObj(projectId);
            if (project != null)
            {
                sr = makeProjectSearchRequest(project.getKey());
                params.put("project", project);
            }
        }
        else if(projectOrFilterId.startsWith("jql-"))
        {
            final String jql = projectOrFilterId.substring(4);

            sr = new SearchRequest();
            if (StringUtils.isNotBlank(jql))
            {
                final SearchService.ParseResult parseResult = searchService.parseQuery(user, jql);
                if (parseResult.isValid())
                {
                    sr = new SearchRequest(parseResult.getQuery());
                }
                else
                {
                    throw new IllegalArgumentException("Invalid JQL query specified for chart '" + jql + "'.");
                }
            }
            params.put("searchRequest", sr);
        }

        return sr;
    }

    private SearchRequest makeProjectSearchRequest(String projectKey)
    {
        return new SearchRequest(JqlQueryBuilder.newBuilder().where().project(projectKey).buildQuery());
    }

    @Override
    public File getChartDirectory()
    {
        File tempDirectory = new File(jiraHome.getSharedCachesDirectory(), "charts");
        if (!tempDirectory.exists())
        {
            tempDirectory.mkdir();
        }
        return tempDirectory;
    }

    public String renderBase64Chart(BufferedImage image, String chartName)
    {
        try
        {
            InputStream inputStream = new ByteArrayInputStream(EncoderUtil.encode(image, ImageFormat.PNG));
            Base64InputStreamConsumer base64Consumer = new Base64InputStreamConsumer(false);
            base64Consumer.consume(inputStream);
            return "data:image/png;base64," + base64Consumer.getEncoded();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to base 64 chart image with name " + chartName, e);
        }
    }

}
