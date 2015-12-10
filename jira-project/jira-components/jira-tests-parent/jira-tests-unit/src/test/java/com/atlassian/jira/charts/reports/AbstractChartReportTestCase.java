package com.atlassian.jira.charts.reports;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.report.AbstractChartReport;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.web.action.ProjectActionSupport;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public abstract class AbstractChartReportTestCase
{
    @Mock @AvailableInContainer
    protected ProjectManager projectManager;
    @Mock @AvailableInContainer
    protected PermissionManager permissionManager;
    @Mock
    protected Project projectMock;
    protected boolean projectAvailable = true;
    protected Collection<Project> accessibleProjects;
    @Mock @AvailableInContainer
    protected SearchRequestService searchRequestService;
    @Mock @AvailableInContainer
    protected ApplicationProperties applicationProperties;
    @Mock @AvailableInContainer
    protected UserProjectHistoryManager userProjectHistoryManager;

    protected AbstractChartReport report;
    private ProjectActionSupport action;

    private static final String VALID_PROJECT_ID = "10000";
    private static final String MAX_DAYS_PREVIOUS = String.valueOf(Integer.MAX_VALUE);
    private static final String MAX_PROJECT_OR_FILTER_ID = String.valueOf(Long.MAX_VALUE);

    @AvailableInContainer
    @Mock
    private JiraAuthenticationContext authContext;
    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);

    protected void setUp() throws Exception
    {
        accessibleProjects = new ArrayList<Project>();
        accessibleProjects.add((Project) projectMock);
        when(projectManager.getProjectObj(anyLong())).thenReturn(projectMock);
        when(permissionManager.getProjectObjects(anyInt(), any(User.class))).thenReturn(accessibleProjects);
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_CHART_DAYS_PREVIOUS_LIMIT_PREFIX + ChartFactory.PeriodName.daily)).thenReturn("300");
        when(applicationProperties.getDefaultBackedString(APKeys.JIRA_CHART_DAYS_PREVIOUS_LIMIT_PREFIX + ChartFactory.PeriodName.monthly)).thenReturn("7500");

        action = new ProjectActionSupport(null, null)
        {
            public String getText(final String key)
            {
                return key;
            }

            public String getText(final String key, final String value1)
            {
                return key;
            }

            public String getText(final String key, final Object value1)
            {
                return key;
            }

            public Collection<Project> getBrowsableProjects()
            {
                return accessibleProjects;
            }

            public Project getSelectedProjectObject()
            {
                return (projectAvailable) ? (Project) projectMock : null;
            }

            @Override
            public User getLoggedInUser()
            {
                return null;
            }

            @Override
            public ApplicationUser getLoggedInApplicationUser()
            {
                return null;
            }
        };
        report = getChartReport();
    }

    /**
     * This is used to initialise {@link #report} to the instance of the extending {@link AbstractChartReport}
     * so that the tests defined in this test case can be re-used.
     * @return {@link AbstractChartReport} instance of the extending report.
     */
    public abstract AbstractChartReport getChartReport();

    public void _testDaysPreviousValidation()
    {
        //setup valid projectOrFilterId so its not included in the errors
        Map params = EasyMap.build("projectOrFilterId", "project-" + VALID_PROJECT_ID);
        params.put("periodName", "daily");

        //null
        params.put("daysprevious", null);
        validateAndAssertOnlyErrorForFieldIs(params, "daysprevious", "report.error.days.previous.not.a.number");

        //no input
        params.put("daysprevious", "");
        validateAndAssertOnlyErrorForFieldIs(params, "daysprevious", "report.error.days.previous.not.a.number");

        //negative
        params.put("daysprevious", "-1");
        validateAndAssertOnlyErrorForFieldIs(params, "daysprevious", "report.error.days.previous");

        //overflow
        params.put("daysprevious", MAX_DAYS_PREVIOUS + "0");
        validateAndAssertOnlyErrorForFieldIs(params, "daysprevious", "report.error.days.previous.not.a.number");

        //max valid value for daily period
        params.put("daysprevious", "300");
        validateAndAssertNoErrorForField(params);

        params.put("daysprevious", "301");
        validateAndAssertOnlyErrorForFieldIs(params, "daysprevious", "report.error.days.previous.period");

        //now try a nother period
        params.put("daysprevious", "301");
        params.put("periodName", "monthly");
        validateAndAssertNoErrorForField(params);

        //exceed the monthly limit
        params.put("daysprevious", "17000");
        params.put("periodName", "monthly");
        validateAndAssertOnlyErrorForFieldIs(params, "daysprevious", "report.error.days.previous.period");

        params.put("daysprevious", "30");
        validateAndAssertNoErrorForField(params);
    }

    public void _testProjectOrFilterIdValidation()
    {
        //setup valid projectOrFilterId so its not included in the errors
        Map params = EasyMap.build("daysprevious", "30", "periodName", "daily");

        params.put("projectOrFilterId", null);
        validateAndAssertOnlyErrorForFieldIs(params, "projectOrFilterId", "report.error.no.filter.or.project");

        params.put("projectOrFilterId", "");
        validateAndAssertOnlyErrorForFieldIs(params, "projectOrFilterId", "report.error.no.filter.or.project");

        params.put("projectOrFilterId", "-");
        validateAndAssertOnlyErrorForFieldIs(params, "projectOrFilterId", "report.error.no.filter.or.project");

        params.put("projectOrFilterId", "random");
        validateAndAssertOnlyErrorForFieldIs(params, "projectOrFilterId", "report.error.no.filter.or.project");

        params.put("projectOrFilterId", "random-");
        validateAndAssertOnlyErrorForFieldIs(params, "projectOrFilterId", "report.error.no.filter.or.project");

        params.put("projectOrFilterId", "random-1000");
        validateAndAssertOnlyErrorForFieldIs(params, "projectOrFilterId", "report.error.no.filter.or.project");
    }

    public void _testFilterIdValidation()
    {
        //setup valid projectOrFilterId so its not included in the errors
        Map params = EasyMap.build("daysprevious", "30", "periodName", "daily");

        params.put("projectOrFilterId", "filter");
        validateAndAssertOnlyErrorForFieldIs(params, "projectOrFilterId", "report.error.no.filter.or.project");

        params.put("projectOrFilterId", "filter-");
        validateAndAssertOnlyErrorForFieldIs(params, "projectOrFilterId", "report.error.no.filter.or.project");

        //invalid filter (ie. filter is null)
        String filterId = "10000";
        params.put("projectOrFilterId", "filter-" + filterId);
        when(searchRequestService.getFilter(any(JiraServiceContext.class), eq(new Long(filterId)))).thenReturn(null);
        validateAndAssertOnlyErrorForFieldIs(params, "projectOrFilterId", "report.error.no.filter");

        //filter id overflow
        filterId = MAX_PROJECT_OR_FILTER_ID + "0";
        params.put("projectOrFilterId", "filter-" + filterId);
        validateAndAssertOnlyErrorForFieldIs(params, "projectOrFilterId", "report.error.filter.id.not.a.number");
    }

    public void _testProjectIdValidation()
    {
        //setup valid projectOrFilterId so its not included in the errors
        Map params = EasyMap.build("daysprevious", "30", "periodName", "daily");

        params.put("projectOrFilterId", "project");
        validateAndAssertOnlyErrorForFieldIs(params, "projectOrFilterId", "report.error.no.filter.or.project");

        params.put("projectOrFilterId", "project-");
        validateAndAssertOnlyErrorForFieldIs(params, "projectOrFilterId", "report.error.no.filter.or.project");

        //invalid filter (ie. filter is null)
        String projectId = "10000";
        params.put("projectOrFilterId", "project-" + projectId);
        projectAvailable = false;
        validateAndAssertOnlyErrorForFieldIs(params, "projectOrFilterId", "report.error.project.id.not.found");

        //filter id overflow
        projectId = MAX_PROJECT_OR_FILTER_ID + "0";
        params.put("projectOrFilterId", "project-" + projectId);
        validateAndAssertOnlyErrorForFieldIs(params, "projectOrFilterId", "report.error.project.id.not.a.number");
        action.getErrors().clear();//clear the errors for the next test

        //valid project id
        projectId = "10000";
        params.put("projectOrFilterId", "project-" + projectId);
        projectAvailable = true;
        validateAndAssertNoErrorForField(params);
    }

    private void validateAndAssertOnlyErrorForFieldIs(Map params, String field, String expectedErrorMsg)
    {
        report.validate(action, params);
        Assert.assertEquals(1, action.getErrors().size());
        Assert.assertEquals(expectedErrorMsg, action.getErrors().get(field));
        action.getErrors().clear();//clear the errors for the next test
    }

    private void validateAndAssertNoErrorForField(Map params)
    {
        report.validate(action, params);
        Assert.assertTrue(action.getErrors().isEmpty());
        Assert.assertFalse(action.hasAnyErrors());
    }
}
