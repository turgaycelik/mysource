package com.atlassian.jira.charts.report;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.util.ChartReportUtils;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.charts.util.DataUtils;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.plugin.report.impl.AbstractReport;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.action.ProjectActionSupport;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Map;

/**
 * @since v4.0
 */
@PublicSpi
public abstract class AbstractChartReport extends AbstractReport
{
    private static final String PROJECT_OR_FILTER_ID = "projectOrFilterId";

    private final ProjectManager projectManager;
    private final SearchRequestService searchRequestService;

    protected final JiraAuthenticationContext authenticationContext;
    protected final ApplicationProperties applicationProperties;
    protected final ChartUtils chartUtils;
    private static final String DAYSPREVIOUS = "daysprevious";
    private static final String PERIOD = "periodName";

    protected AbstractChartReport(JiraAuthenticationContext authenticationContext, ApplicationProperties applicationProperties,
            ProjectManager projectManager, SearchRequestService searchRequestService, ChartUtils chartUtils)
    {
        this.authenticationContext = authenticationContext;
        this.applicationProperties = applicationProperties;
        this.projectManager = projectManager;
        this.searchRequestService = searchRequestService;
        this.chartUtils = chartUtils;
    }

    public void validate(ProjectActionSupport action, Map params)
    {
        validateDaysPrevious(action, params);
        validateProjectOrFilterId(action, params);
    }

    protected void validateProjectOrFilterId(ProjectActionSupport action, Map params)
    {
        String projectOrFilterId = (String) params.get(PROJECT_OR_FILTER_ID);
        if (ChartReportUtils.isValidProjectParamFormat(projectOrFilterId))
        {
            validateProjectId(action, ChartReportUtils.extractProjectOrFilterId(projectOrFilterId));
        }
        else if (ChartReportUtils.isValidFilterParamFormat(projectOrFilterId))
        {
            validateFilterId(action, ChartReportUtils.extractProjectOrFilterId(projectOrFilterId));
        }
        else
        {
            action.addError(PROJECT_OR_FILTER_ID, action.getText("report.error.no.filter.or.project"));
        }
    }

    private void validateProjectId(ProjectActionSupport action, String projectId)
    {
        try
        {
            if (StringUtils.isNotEmpty(projectId))
            {
                action.setSelectedProjectId(new Long(projectId));
            }
            Project project = action.getSelectedProjectObject();
            Collection<Project> browseable = action.getBrowsableProjects();
            if (project == null || browseable == null || !browseable.contains(project))
            {
                action.addError(PROJECT_OR_FILTER_ID, action.getText("report.error.project.id.not.found"));
            }
        }
        catch (NumberFormatException nfe)
        {
            action.addError(PROJECT_OR_FILTER_ID, action.getText("report.error.project.id.not.a.number", projectId));
        }
    }

    private void validateFilterId(ProjectActionSupport action, String filterId)
    {
        try
        {
            JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(action.getLoggedInUser(), new SimpleErrorCollection());
            SearchRequest searchRequest = searchRequestService.getFilter(serviceContext, new Long(filterId));
            if (searchRequest == null)
            {
                action.addError(PROJECT_OR_FILTER_ID, action.getText("report.error.no.filter"));
            }
        }
        catch (NumberFormatException nfe)
        {
            action.addError(PROJECT_OR_FILTER_ID, action.getText("report.error.filter.id.not.a.number", filterId));
        }
    }

    protected void validateDaysPrevious(ProjectActionSupport action, Map params)
    {
        final String daysprevious = (String) params.get(DAYSPREVIOUS);
        //if there's daysprevious there must be a period
        final String period = (String) params.get(PERIOD);

        try
        {
            final int days = Integer.parseInt(daysprevious);
            if (days < 1)
            {
                action.addError(DAYSPREVIOUS, action.getText("report.error.days.previous"));
                return;
            }
            final int normalizedDays = DataUtils.normalizeDaysValue(days, ChartFactory.PeriodName.valueOf(period));
            if(normalizedDays != days)
            {
                action.addError(DAYSPREVIOUS, action.getText("report.error.days.previous.period", normalizedDays));
            }
        }
        catch (NumberFormatException nfe)
        {
            action.addError(DAYSPREVIOUS, action.getText("report.error.days.previous.not.a.number"));
        }
    }
}
