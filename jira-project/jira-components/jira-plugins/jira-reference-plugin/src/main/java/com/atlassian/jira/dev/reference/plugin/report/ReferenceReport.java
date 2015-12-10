package com.atlassian.jira.dev.reference.plugin.report;


import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.report.impl.AbstractReport;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.query.Query;
import org.apache.commons.lang.StringUtils;


import java.util.Map;

/**
 * This is a simple report that reports all issues.
 *
 * @since v4.3
 */
public class ReferenceReport extends AbstractReport
{
    private final SearchService searchService;
    private final IssueService issueService;
    private JiraAuthenticationContext authenticationContext;

    public ReferenceReport(SearchService searchService, IssueService issueService, JiraAuthenticationContext authenticationContext)
    {
        this.searchService = searchService;
        this.issueService = issueService;
        this.authenticationContext = authenticationContext;
    }

    public String generateReportHtml(ProjectActionSupport action, Map reqParams) throws Exception
    {
        final String projectKey = (String) reqParams.get("projectKey");
        final Query query = JqlQueryBuilder.newBuilder().where().project(projectKey).buildQuery();
        final long issueCount = searchService.searchCount(authenticationContext.getLoggedInUser(), query);
        final Map<String, Object> params = MapBuilder.<String, Object>newBuilder()
                .add("report", this)
                .add("action", action)
                .add("user", authenticationContext.getLoggedInUser())
                .add("projectKey", reqParams.get("projectKey"))
                .add("issueCount",issueCount)
                .toMap();
        return descriptor.getHtml("view", params);
    }

    @Override
    public void validate(ProjectActionSupport action, Map params)
    {
        final String projectKey = (String) params.get("projectKey");
        if (StringUtils.isBlank(projectKey))
        {
            action.addError("projectKey", "Invalid project key");
        }
    }
}
