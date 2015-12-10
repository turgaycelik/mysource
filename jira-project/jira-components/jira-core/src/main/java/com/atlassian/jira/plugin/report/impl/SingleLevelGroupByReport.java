package com.atlassian.jira.plugin.report.impl;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.search.ReaderCache;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.issue.statistics.StatsGroup;
import com.atlassian.jira.issue.statistics.util.OneDimensionalDocIssueHitCollector;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.util.profiling.UtilTimerStack;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.search.Collector;

import java.util.Map;

public class SingleLevelGroupByReport extends AbstractReport
{
    private static final Logger log = Logger.getLogger(SingleLevelGroupByReport.class);

    private final SearchProvider searchProvider;
    private final JiraAuthenticationContext authenticationContext;
    private final SearchRequestService searchRequestService;
    private final IssueFactory issueFactory;
    private final CustomFieldManager customFieldManager;
    private final IssueIndexManager issueIndexManager;
    private final SearchService searchService;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final ReaderCache readerCache;
    private final FieldManager fieldManager;
    private final ProjectManager projectManager;

    public SingleLevelGroupByReport(final SearchProvider searchProvider, final JiraAuthenticationContext authenticationContext,
            final SearchRequestService searchRequestService, final IssueFactory issueFactory,
            final CustomFieldManager customFieldManager, final IssueIndexManager issueIndexManager,
            final SearchService searchService, final FieldVisibilityManager fieldVisibilityManager,
            final ReaderCache readerCache, FieldManager fieldManager, ProjectManager projectManager)
    {
        this.searchProvider = searchProvider;
        this.authenticationContext = authenticationContext;
        this.searchRequestService = searchRequestService;
        this.issueFactory = issueFactory;
        this.customFieldManager = customFieldManager;
        this.issueIndexManager = issueIndexManager;
        this.searchService = searchService;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.readerCache = readerCache;
        this.fieldManager = fieldManager;
        this.projectManager = projectManager;
    }

    public StatsGroup getOptions(SearchRequest sr, User user, StatisticsMapper mapper) throws PermissionException
    {

        try
        {
            return searchMapIssueKeys(sr, user, mapper);
        }
        catch (SearchException e)
        {
            log.error("Exception rendering " + this.getClass().getName() + ".  Exception " + e.getMessage(), e);
            return null;
        }
    }

    public StatsGroup searchMapIssueKeys(SearchRequest request, User searcher, StatisticsMapper mapper)
            throws SearchException
    {
        try
        {
            UtilTimerStack.push("Search Count Map");
            StatsGroup statsGroup = new StatsGroup(mapper);
            Collector hitCollector = new OneDimensionalDocIssueHitCollector(mapper.getDocumentConstant(), statsGroup,
                    issueIndexManager.getIssueSearcher().getIndexReader(), issueFactory,
                    fieldVisibilityManager, readerCache, fieldManager, projectManager);
            searchProvider.searchAndSort((request != null) ? request.getQuery() : null, searcher, hitCollector, PagerFilter.getUnlimitedFilter());
            return statsGroup;
        }
        finally
        {
            UtilTimerStack.pop("Search Count Map");
        }
    }

    public String generateReportHtml(ProjectActionSupport action, Map params) throws Exception
    {
        String filterId = (String) params.get("filterid");
        if (filterId == null)
        {
            log.error("Single Level Group By Report run without a project selected (JRA-5042): params=" + params);
            return "<span class='errMsg'>No search filter has been selected. Please "
                   + "<a href=\"IssueNavigator.jspa?reset=Update&amp;pid="
                   + TextUtils.htmlEncode((String) params.get("selectedProjectId"))
                   + "\">create one</a>, and re-run this report. See also "
                   + "<a href=\"http://jira.atlassian.com/browse/JRA-5042\">JRA-5042</a></span>";
        }
        String mapperName = (String) params.get("mapper");
        final StatisticsMapper mapper = new FilterStatisticsValuesGenerator().getStatsMapper(mapperName);
        final JiraServiceContext ctx = new JiraServiceContextImpl(authenticationContext.getLoggedInUser());
        final SearchRequest request = searchRequestService.getFilter(ctx, new Long(filterId));

        final Map startingParams;
        try
        {
            startingParams = EasyMap.build(
                    "action", action,
                    "statsGroup", getOptions(request, authenticationContext.getLoggedInUser(), mapper),
                    "searchRequest", request,
                    "mapperType", mapperName,
                    "customFieldManager", customFieldManager,
                    "fieldVisibility", fieldVisibilityManager,
                    "searchService", searchService,
                    "portlet", this);

            return descriptor.getHtml("view", startingParams);
        }
        catch (PermissionException e)
        {
            log.error(e, e);
            return null;
        }

    }

    public void validate(ProjectActionSupport action, Map params)
    {
        super.validate(action, params);
        String filterId = (String) params.get("filterid");
        if (StringUtils.isEmpty(filterId))
        {
            action.addError("filterid", action.getText("report.singlelevelgroupby.filter.is.required"));
        }
        else
        {
            validateFilterId(action,filterId);
        }
    }

    private void validateFilterId(ProjectActionSupport action, String filterId)
    {
        try
        {
            JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(
                    action.getLoggedInUser(), new SimpleErrorCollection());
            SearchRequest searchRequest = searchRequestService.getFilter(serviceContext, new Long(filterId));
            if (searchRequest == null)
            {
                action.addErrorMessage(action.getText("report.error.no.filter"));
            }
        }
        catch (NumberFormatException nfe)
        {
            action.addError("filterId", action.getText("report.error.filter.id.not.a.number", filterId));
        }
    }
}
