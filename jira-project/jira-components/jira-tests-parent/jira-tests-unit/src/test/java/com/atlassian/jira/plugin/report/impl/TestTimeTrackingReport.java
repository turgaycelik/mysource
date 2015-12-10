package com.atlassian.jira.plugin.report.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.plugin.report.ReportModuleDescriptorImpl;
import com.atlassian.jira.plugin.report.ReportSubTaskFetcher;
import com.atlassian.jira.plugin.report.SubTaskInclusionOption;
import com.atlassian.jira.portal.FilterValuesGenerator;
import com.atlassian.jira.portal.SortingValuesGenerator;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionImpl;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockUserLocaleStore;
import com.atlassian.jira.user.UserLocaleStore;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.query.Query;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestTimeTrackingReport
{
    private TimeTrackingReport ttr;
    private Project project;
    private Version version;
    private Issue issue1;
    private Issue issue2;
    private Issue issue3;
    private Issue issue4;
    private Issue issue5;
    private ApplicationUser bob;

    @Mock @AvailableInContainer private VersionManager mockVersionManager;
    @Mock @AvailableInContainer private FieldVisibilityManager mockFieldVisibilityManager;
    @Mock @AvailableInContainer private I18nHelper mockI18nHelper;
    @Mock @AvailableInContainer private I18nHelper.BeanFactory mockBeanFactory;
    @Mock @AvailableInContainer private PropertiesManager mockPropertiesManager;
    @Mock @AvailableInContainer private JiraAuthenticationContext mockAuthenticationContext;
    @Mock @AvailableInContainer private ProjectActionSupport mockProjectActionSupport;
    @Mock @AvailableInContainer private SubTaskManager mockSubTaskManager;
    @Mock @AvailableInContainer private PermissionManager mockPermissionManager;
    @Mock @AvailableInContainer private ApplicationProperties mockApplicationProperties;
    @Mock @AvailableInContainer private DurationFormatter mockDurationFormatter;
    @AvailableInContainer private UserLocaleStore userLocaleStore = new MockUserLocaleStore(Locale.ENGLISH);

    @Mock private ApplicationUser mockUser;
    @Mock private BuildUtilsInfo mockBuildUtilsInfo;
    @Mock private ReportSubTaskFetcher mockReportSubTaskFetcher;
    @Mock private SearchProvider mockSearchProvider;
    @Mock private SearchResults mockSearchResults;

    private OfBizDelegator mockOfBizDelegator = new MockOfBizDelegator();
    private JiraDurationUtils jiraDurationUtils;

    @Rule public RuleChain mockitoMocksIncontainer = MockitoMocksInContainer.forTest(this);

    @Before
    public void setUp() throws Exception
    {
        setupMockResourceBundle();
        when(mockFieldVisibilityManager.isFieldHidden(anyString(), isA(Issue.class))).thenReturn(false);

        GenericValue projectGv = mockOfBizDelegator.createValue("Project", FieldMap.build("id", (long) 1, "key", "HSP"));
        GenericValue version1 = mockOfBizDelegator.createValue("Version", FieldMap.build("id", (long) 10, "project", (long) 1));

        version = new VersionImpl(null, new MockGenericValue("Version", FieldMap.build("id", (long) 10, "project", (long) 1)));

        // 1: all completed.
        GenericValue issueGv1 = mockOfBizDelegator.createValue(
                "Issue", FieldMap.build("id", (long) 100, "number", 1L, "project", (long) 1, "timeoriginalestimate",
                (7 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS, "timeestimate", (long) 0)
                .add("timespent", (7 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS));

        // 2: 10 left
        GenericValue issueGv2 = mockOfBizDelegator.createValue(
                "Issue", FieldMap.build("id", (long) 200, "number", 2L, "project", (long) 1,
                "timeoriginalestimate", (7 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS,
                "timeestimate", (1 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS)
                .add("timespent", (6 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS));

        // 3: 20 left
        GenericValue issueGv3 = mockOfBizDelegator.createValue(
                "Issue", FieldMap.build("id", (long) 300, "number", 3L, "project", (long) 1,
                "timeoriginalestimate", (7 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS,
                "timeestimate", (2 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS)
                .add("timespent", (5 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS));

        // 4: 30 left
        GenericValue issueGv4 = mockOfBizDelegator.createValue(
                "Issue", FieldMap.build("id", (long) 400, "number", 4L, "project", (long) 1,
                "timeoriginalestimate", (7 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS,
                "timeestimate", (3 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS)
                .add("timespent", (5 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS));

        GenericValue issueGv5 = mockOfBizDelegator.createValue("Issue", FieldMap.build(
                "id", (long) 500, "number", 5L, "project", (long) 1, "timeoriginalestimate",
                (7 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS, "timeestimate",
                (3 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS)
                .add("timespent", (5 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS));

        CacheManager cacheManager = new MemoryCacheManager();

        project = new MockProject(projectGv);
        issue1 = convertToIssueObject(issueGv1);
        issue2 = convertToIssueObject(issueGv2);
        issue3 = convertToIssueObject(issueGv3);
        issue4 = convertToIssueObject(issueGv4);
        issue5 = convertToIssueObject(issueGv5);
        when(mockProjectActionSupport.getBrowsableProjects()).thenReturn(Collections.singletonList(project));
        when(mockProjectActionSupport.getSelectedProjectObject()).thenReturn(project);
        when(mockSubTaskManager.isSubTasksEnabled()).thenReturn(true);

        //JiraTestUtil.setupAndAssociateDefaultPermissionSchemeWithPermission(projectGv, Permissions.BROWSE);

        ConstantsManager constantsManager = ComponentAccessor.getConstantsManager();
        when(mockAuthenticationContext.getI18nHelper()).thenReturn(mockI18nHelper);
        when(mockBeanFactory.getInstance(isA(Locale.class))).thenReturn(mockI18nHelper);
        when(mockAuthenticationContext.getUser()).thenReturn(mockUser);
        when(mockI18nHelper.getLocale()).thenReturn(Locale.ENGLISH);
        when(mockApplicationProperties.getDefaultLocale()).thenReturn(Locale.ENGLISH);

        final TimeTrackingConfiguration trackingConfiguration = new TimeTrackingConfiguration.PropertiesAdaptor(mockApplicationProperties);
        jiraDurationUtils = new JiraDurationUtils(mockApplicationProperties, mockAuthenticationContext, trackingConfiguration, null, mockBeanFactory, cacheManager);

        setupFormat("core.durationutils.unit.day", "d");
        setupFormat("core.durationutils.unit.hour", "h");

        updateJDU("7", "24", JiraDurationUtils.FORMAT_PRETTY);
        when(mockBuildUtilsInfo.getBuildInformation()).thenReturn("Some build information");
        when(mockBuildUtilsInfo.getCurrentBuildNumber()).thenReturn("111");

        when(mockSearchProvider.search(isA(Query.class), any(ApplicationUser.class), isA(PagerFilter.class))).thenReturn(mockSearchResults);
        when(mockSearchResults.getIssues()).thenReturn(Lists.newArrayList(issue1, issue2, issue3, issue4));

        ttr = new TimeTrackingReport(mockVersionManager, mockApplicationProperties, constantsManager, jiraDurationUtils,
                mockSearchProvider, mockBuildUtilsInfo, mockReportSubTaskFetcher, mockSubTaskManager);

        ttr.init(new ReportModuleDescriptorImpl(null, ModuleFactory.LEGACY_MODULE_FACTORY)
        {
            @Override
            public I18nHelper getI18nBean()
            {
                return mockI18nHelper;
            }
        });
    }

    private void setupMockResourceBundle()
    {
        final ResourceBundle mockResourceBundle = new ResourceBundle()
        {
            final Map<String, Object> resources = buildResourceMap();

            private Map<String, Object> buildResourceMap()
            {
                return MapBuilder.<String, Object>newBuilder().add("core.dateutils.week", "week").
                                        add("core.dateutils.weeks", "weeks").
                                        add("core.dateutils.day", "day").
                                        add("core.dateutils.days", "days").
                                        add("core.dateutils.hour", "hour").
                                        add("core.dateutils.hours", "hours").
                                        add("pkey", "TST").toMap();
            }

            @Override
            protected Object handleGetObject(String key)
            {
                return resources.get(key);
            }

            @Override
            public Enumeration<String> getKeys()
            {
                return Collections.enumeration(resources.keySet());
            }
        };
        when(mockI18nHelper.getResourceBundle()).thenReturn(mockResourceBundle);
    }

    /*
     * Make sure right versionIds in a project are retrieved
     */
    @Test
    public void testGetProjectVersions() throws Exception
    {
        when(mockVersionManager.getVersions(project.getId())).thenReturn(Collections.singletonList(version));

        Collection versions = ttr.getProjectVersionIds(project);
        assertEquals(1, versions.size());
        assertTrue(versions.contains(version.getId().toString()));
    }

    /*
     * Make sure validate catches invalid selections
     * Shouldn't occur in the form, but just as a safeguard.
     */
    @Test
    public void testValidate() throws Exception
    {
        when(mockVersionManager.getVersions(project.getId())).thenReturn(Collections.EMPTY_LIST);

        Map params = FieldMap.build("completedFilter", "dud", "sortingOrder", "another dud", "versionId", "500");

        ttr.validate(mockProjectActionSupport, params);

        verify(mockProjectActionSupport).addError(eq("sortingOrder"), anyString());
        verify(mockProjectActionSupport).addError(eq("completedFilter"), anyString());
        verify(mockProjectActionSupport).addError(eq("versionId"), anyString());
    }

    /*
     * Make sure the right issues are retrieved.
     * <p/>
     * Case 1: uncompleted issues sorted by most completed first
     */
    @Test
    public void testGetIssues1() throws Exception
    {
        when(mockVersionManager.getVersions(project.getGenericValue())).thenReturn(Collections.singletonList(version));

        Collection issues = ttr.getReportIssues(bob, project.getId(), version.getId(), SortingValuesGenerator.SORT_BY_MOST_COMPLETED, FilterValuesGenerator.FILTER_INCOMPLETE_ISSUES, SubTaskInclusionOption.ONLY_SELECTED_VERSION);
        assertEquals(3, issues.size());

        Iterator issueIterator = issues.iterator();

        _assertEquals(issue2, issueIterator.next());
        _assertEquals(issue3, issueIterator.next());
        _assertEquals(issue4, issueIterator.next());
    }

    /*
     * Case 2: uncompleted issues sorted by least completed first
     */
    @Test
    public void testGetIssues2() throws Exception
    {
        when(mockVersionManager.getVersions(project.getGenericValue())).thenReturn(Collections.singletonList(version));

        Collection issues = ttr.getReportIssues(bob, project.getId(), version.getId(), SortingValuesGenerator.SORT_BY_LEAST_COMPLETED, FilterValuesGenerator.FILTER_INCOMPLETE_ISSUES, SubTaskInclusionOption.ONLY_SELECTED_VERSION);

        assertEquals(3, issues.size());

        Iterator issueIterator = issues.iterator();

        _assertEquals(issue4, issueIterator.next());
        _assertEquals(issue3, issueIterator.next());
        _assertEquals(issue2, issueIterator.next());
    }

    /*
     * Case 3: completed issues sorted by most completed first
     */
    @Test
    public void testGetIssues3() throws Exception
    {
        when(mockVersionManager.getVersions(project.getGenericValue())).thenReturn(Collections.singletonList(version));

        Collection issues = ttr.getReportIssues(bob, project.getId(), version.getId(), SortingValuesGenerator.SORT_BY_MOST_COMPLETED, FilterValuesGenerator.FILTER_ALL_ISSUES, SubTaskInclusionOption.ONLY_SELECTED_VERSION);

        assertEquals(4, issues.size());

        Iterator issueIterator = issues.iterator();

        _assertEquals(issue1, issueIterator.next());
        _assertEquals(issue2, issueIterator.next());
        _assertEquals(issue3, issueIterator.next());
        _assertEquals(issue4, issueIterator.next());
    }

    /*
     * Case 4: completed issues sorted by least completed first.
     */
    @Test
    public void testGetIssues4() throws Exception
    {
        when(mockVersionManager.getVersions(project.getGenericValue())).thenReturn(Collections.singletonList(version));

        Collection issues = ttr.getReportIssues(bob, project.getId(), version.getId(), SortingValuesGenerator.SORT_BY_LEAST_COMPLETED, FilterValuesGenerator.FILTER_ALL_ISSUES, SubTaskInclusionOption.ONLY_SELECTED_VERSION);

        assertEquals(4, issues.size());

        Iterator issueIterator = issues.iterator();

        _assertEquals(issue4, issueIterator.next());
        _assertEquals(issue3, issueIterator.next());
        _assertEquals(issue2, issueIterator.next());
        _assertEquals(issue1, issueIterator.next());
    }

    /*
     * Case 5: issue for no version.
     */
    @Test
    public void testGetIssues5() throws Exception
    {
        when(mockSearchResults.getIssues()).thenReturn(Collections.singletonList(issue5));
        Collection issues = ttr.getReportIssues(bob, project.getId(), (long) -1, SortingValuesGenerator.SORT_BY_LEAST_COMPLETED, FilterValuesGenerator.FILTER_ALL_ISSUES, SubTaskInclusionOption.ONLY_SELECTED_VERSION);

        assertEquals(1, issues.size());

        Iterator issueIterator = issues.iterator();

        _assertEquals(issue5, issueIterator.next());
    }

    @Test
    public void testGetNiceTimeDurationPretty()
    {
        DurationFormatter formatter = ttr.getDurationFormatter();
        assertNotNull(formatter);
        updateJDU("7", "24", JiraDurationUtils.FORMAT_PRETTY);
        assertEquals("1 week", formatter.format(issue3.getOriginalEstimate()));
        assertEquals("2 days", formatter.format(issue3.getEstimate()));
        assertEquals("5 days", formatter.format(issue3.getTimeSpent()));
    }

    @Test
    public void testGetNiceTimeDurationDays()
    {
        DurationFormatter formatter = ttr.getDurationFormatter();
        updateJDU("7", "24", JiraDurationUtils.FORMAT_DAYS);
        assertEquals("7d", formatter.format(issue3.getOriginalEstimate()));
        assertEquals("2d", formatter.format(issue3.getEstimate()));
        assertEquals("5d", formatter.format(issue3.getTimeSpent()));
    }

    @Test
    public void testGetNiceTimeDurationHours()
    {
        DurationFormatter formatter = ttr.getDurationFormatter();
        updateJDU("7", "24", JiraDurationUtils.FORMAT_HOURS);
        assertEquals("168h", formatter.format(issue3.getOriginalEstimate()));
        assertEquals("48h", formatter.format(issue3.getEstimate()));
        assertEquals("120h", formatter.format(issue3.getTimeSpent()));
    }

    @Test
    public void testGetNiceTimeDurationWithShorterWeeksAndDaysPretty()
    {
        DurationFormatter formatter = ttr.getDurationFormatter();
        updateJDU("5", "7", JiraDurationUtils.FORMAT_PRETTY);
        assertEquals("4 weeks, 4 days", formatter.format(issue3.getOriginalEstimate()));
        assertEquals("1 week, 1 day, 6 hours", formatter.format(issue3.getEstimate()));
        assertEquals("3 weeks, 2 days, 1 hour", formatter.format(issue3.getTimeSpent()));
    }

    @Test
    public void testGetNiceTimeDurationWithShorterWeeksAndDaysDays()
    {
        DurationFormatter formatter = ttr.getDurationFormatter();
        updateJDU("5", "7", JiraDurationUtils.FORMAT_DAYS);
        assertEquals("24d", formatter.format(issue3.getOriginalEstimate()));
        assertEquals("6d 6h", formatter.format(issue3.getEstimate()));
        assertEquals("17d 1h", formatter.format(issue3.getTimeSpent()));
    }

    @Test
    public void testGetNiceTimeDurationWithShorterWeeksAndDaysHours()
    {
        DurationFormatter formatter = ttr.getDurationFormatter();
        updateJDU("5", "7", JiraDurationUtils.FORMAT_HOURS);
        assertEquals("168h", formatter.format(issue3.getOriginalEstimate()));
        assertEquals("48h", formatter.format(issue3.getEstimate()));
        assertEquals("120h", formatter.format(issue3.getTimeSpent()));
    }

    @Test
    public void testGetOriginalEstTot() throws Exception
    {
        Map params = FieldMap.build("completedFilter", FilterValuesGenerator.FILTER_ALL_ISSUES, "sortingOrder", SortingValuesGenerator.SORT_BY_MOST_COMPLETED, "versionId", version.getId().toString(), "subtaskInclusion", SubTaskInclusionOption.ONLY_SELECTED_VERSION.getKey());

        ttr.getParams(mockProjectActionSupport, params);
        String originalTot = ttr.getTotals().getOriginalEstimate();
        assertEquals("4w", originalTot);

        updateJDU("7", "24", JiraDurationUtils.FORMAT_DAYS);
        originalTot = ttr.getTotals().getOriginalEstimate();
        assertEquals("28d", originalTot);

        updateJDU("7", "24", JiraDurationUtils.FORMAT_HOURS);
        originalTot = ttr.getTotals().getOriginalEstimate();
        assertEquals("672h", originalTot);
    }

    @Test
    public void testGetOriginalEstTotWithShorterWeeksAndDays()
            throws PermissionException, GenericEntityException, SearchException
    {
        updateJDU("5", "7", JiraDurationUtils.FORMAT_PRETTY);

        Map params = FieldMap.build("completedFilter", FilterValuesGenerator.FILTER_ALL_ISSUES, "sortingOrder", SortingValuesGenerator.SORT_BY_MOST_COMPLETED, "versionId", version.getId().toString(), "subtaskInclusion", SubTaskInclusionOption.ONLY_SELECTED_VERSION.getKey());

        ttr.getParams(mockProjectActionSupport, params);

        String originalTot = ttr.getTotals().getOriginalEstimate();
        assertEquals("19w 1d", originalTot);

        updateJDU("5", "7", JiraDurationUtils.FORMAT_DAYS);
        originalTot = ttr.getTotals().getOriginalEstimate();
        assertEquals("96d", originalTot);

        updateJDU("5", "7", JiraDurationUtils.FORMAT_HOURS);
        originalTot = ttr.getTotals().getOriginalEstimate();
        assertEquals("672h", originalTot);
    }

    @Test
    public void testGetTimeSpentTot() throws PermissionException, GenericEntityException, SearchException
    {
        Map params = FieldMap.build("completedFilter", FilterValuesGenerator.FILTER_ALL_ISSUES, "sortingOrder", SortingValuesGenerator.SORT_BY_MOST_COMPLETED, "versionId", version.getId().toString(), "subtaskInclusion", SubTaskInclusionOption.ONLY_SELECTED_VERSION.getKey());

        ttr.getParams(mockProjectActionSupport, params);

        String timeSpentTot = ttr.getTotals().getTimeSpent();
        assertEquals("3w 2d", timeSpentTot);

        updateJDU("7", "24", JiraDurationUtils.FORMAT_DAYS);
        timeSpentTot = ttr.getTotals().getTimeSpent();
        assertEquals("23d", timeSpentTot);

        updateJDU("7", "24", JiraDurationUtils.FORMAT_HOURS);
        timeSpentTot = ttr.getTotals().getTimeSpent();
        assertEquals("552h", timeSpentTot);
    }

    @Test
    public void testGetTimeSpentTotWithShorterWeeksAndDays()
            throws PermissionException, GenericEntityException, SearchException
    {
        updateJDU("5", "7", JiraDurationUtils.FORMAT_PRETTY);
        Map params = FieldMap.build("completedFilter", FilterValuesGenerator.FILTER_ALL_ISSUES, "sortingOrder", SortingValuesGenerator.SORT_BY_MOST_COMPLETED, "versionId", version.getId().toString(), "subtaskInclusion", SubTaskInclusionOption.ONLY_SELECTED_VERSION.getKey());

        ttr.getParams(mockProjectActionSupport, params);

        String timeSpentTot = ttr.getTotals().getTimeSpent();
        assertEquals("15w 3d 6h", timeSpentTot);

        updateJDU("5", "7", JiraDurationUtils.FORMAT_DAYS);
        timeSpentTot = ttr.getTotals().getTimeSpent();
        assertEquals("78d 6h", timeSpentTot);

        updateJDU("5", "7", JiraDurationUtils.FORMAT_HOURS);
        timeSpentTot = ttr.getTotals().getTimeSpent();
        assertEquals("552h", timeSpentTot);
    }

    @Test
    public void testGetTimeEstTot() throws PermissionException, GenericEntityException, SearchException
    {
        Map params = FieldMap.build("completedFilter", FilterValuesGenerator.FILTER_ALL_ISSUES, "sortingOrder", SortingValuesGenerator.SORT_BY_MOST_COMPLETED, "versionId", version.getId().toString(), "subtaskInclusion", SubTaskInclusionOption.ONLY_SELECTED_VERSION.getKey());

        ttr.getParams(mockProjectActionSupport, params);

        String timeEstTot = ttr.getTotals().getRemainingEstimate();
        assertEquals("6d", timeEstTot);

        updateJDU("7", "24", JiraDurationUtils.FORMAT_DAYS);
        timeEstTot = ttr.getTotals().getRemainingEstimate();
        assertEquals("6d", timeEstTot);

        updateJDU("7", "24", JiraDurationUtils.FORMAT_HOURS);
        timeEstTot = ttr.getTotals().getRemainingEstimate();
        assertEquals("144h", timeEstTot);
    }

    @Test
    public void testGetTimeEstTotWithShorterWeeksAndDays()
            throws PermissionException, GenericEntityException, SearchException
    {
        updateJDU("5", "7", JiraDurationUtils.FORMAT_PRETTY);

        Map params = FieldMap.build("completedFilter", FilterValuesGenerator.FILTER_ALL_ISSUES, "sortingOrder", SortingValuesGenerator.SORT_BY_MOST_COMPLETED, "versionId", version.getId().toString(), "subtaskInclusion", SubTaskInclusionOption.ONLY_SELECTED_VERSION.getKey());

        ttr.getParams(mockProjectActionSupport, params);

        String timeEstTot = ttr.getTotals().getRemainingEstimate();
        assertEquals("4w 4h", timeEstTot);

        updateJDU("5", "7", JiraDurationUtils.FORMAT_DAYS);
        timeEstTot = ttr.getTotals().getRemainingEstimate();
        assertEquals("20d 4h", timeEstTot);

        updateJDU("5", "7", JiraDurationUtils.FORMAT_HOURS);
        timeEstTot = ttr.getTotals().getRemainingEstimate();
        assertEquals("144h", timeEstTot);
    }

    @Test
    public void testGetAccuracyTot() throws PermissionException, GenericEntityException, SearchException
    {
        Map params = FieldMap.build("completedFilter", FilterValuesGenerator.FILTER_ALL_ISSUES, "sortingOrder", SortingValuesGenerator.SORT_BY_MOST_COMPLETED, "versionId", version.getId().toString(), "subtaskInclusion", SubTaskInclusionOption.ONLY_SELECTED_VERSION.getKey());

        ttr.getParams(mockProjectActionSupport, params);

        String accuracyTot = ttr.getTotals().getAccuracyNice();
        assertEquals("1d", accuracyTot);

        updateJDU("7", "24", JiraDurationUtils.FORMAT_DAYS);
        accuracyTot = ttr.getTotals().getAccuracyNice();
        assertEquals("1d", accuracyTot);

        updateJDU("7", "24", JiraDurationUtils.FORMAT_HOURS);
        accuracyTot = ttr.getTotals().getAccuracyNice();
        assertEquals("24h", accuracyTot);
    }

    @Test
    public void testGetAccuracyTotWithShorterWeeksAndDays()
            throws PermissionException, GenericEntityException, SearchException
    {
        updateJDU("5", "7", JiraDurationUtils.FORMAT_PRETTY);

        Map params = FieldMap.build("completedFilter", FilterValuesGenerator.FILTER_ALL_ISSUES, "sortingOrder", SortingValuesGenerator.SORT_BY_MOST_COMPLETED, "versionId", version.getId().toString(), "subtaskInclusion", SubTaskInclusionOption.ONLY_SELECTED_VERSION.getKey());

        ttr.getParams(mockProjectActionSupport, params);

        String accuracyTot = ttr.getTotals().getAccuracyNice();
        assertEquals("3d 3h", accuracyTot);

        updateJDU("5", "7", JiraDurationUtils.FORMAT_DAYS);
        accuracyTot = ttr.getTotals().getAccuracyNice();
        assertEquals("3d 3h", accuracyTot);

        updateJDU("5", "7", JiraDurationUtils.FORMAT_HOURS);
        accuracyTot = ttr.getTotals().getAccuracyNice();
        assertEquals("24h", accuracyTot);
    }

    @Test
    public void testGetCompletionPercentage() throws PermissionException, GenericEntityException, SearchException
    {
        Map params = FieldMap.build("completedFilter", FilterValuesGenerator.FILTER_ALL_ISSUES, "sortingOrder", SortingValuesGenerator.SORT_BY_MOST_COMPLETED, "versionId", version.getId().toString(), "subtaskInclusion", SubTaskInclusionOption.ONLY_SELECTED_VERSION.getKey());

        ttr.getParams(mockProjectActionSupport, params);

        int completionPercentage = ttr.getCompletionPercentage();
        assertEquals(79, completionPercentage);
    }

    @Test
    public void testGetCompletionPercentageWithShorterWeeksAndDays()
            throws PermissionException, GenericEntityException, SearchException
    {
        updateJDU("5", "7", JiraDurationUtils.FORMAT_PRETTY);

        Map params = FieldMap.build("completedFilter", FilterValuesGenerator.FILTER_ALL_ISSUES, "sortingOrder", SortingValuesGenerator.SORT_BY_MOST_COMPLETED, "versionId", version.getId().toString(), "subtaskInclusion", SubTaskInclusionOption.ONLY_SELECTED_VERSION.getKey());

        ttr.getParams(mockProjectActionSupport, params);

        int completionPercentage = ttr.getCompletionPercentage();
        assertEquals(79, completionPercentage);
    }

    @Test
    public void testGetAccuracyPercentage() throws PermissionException, GenericEntityException, SearchException
    {
        Map params = FieldMap.build("completedFilter", FilterValuesGenerator.FILTER_ALL_ISSUES, "sortingOrder", SortingValuesGenerator.SORT_BY_MOST_COMPLETED, "versionId", version.getId().toString(), "subtaskInclusion", SubTaskInclusionOption.ONLY_SELECTED_VERSION.getKey());

        ttr.getParams(mockProjectActionSupport, params);

        int completionPercentage = ttr.getAccuracyPercentage();
        assertEquals(-3, completionPercentage);
    }

    @Test
    public void testGetAccuracyPercentageWithShorterWeeksAndDays()
            throws PermissionException, GenericEntityException, SearchException
    {
        updateJDU("5", "7", JiraDurationUtils.FORMAT_PRETTY);

        Map params = FieldMap.build("completedFilter", FilterValuesGenerator.FILTER_ALL_ISSUES, "sortingOrder", SortingValuesGenerator.SORT_BY_MOST_COMPLETED, "versionId", version.getId().toString(), "subtaskInclusion", SubTaskInclusionOption.ONLY_SELECTED_VERSION.getKey());

        ttr.getParams(mockProjectActionSupport, params);

        int completionPercentage = ttr.getAccuracyPercentage();
        assertEquals(-3, completionPercentage);
    }

    private void updateJDU(String days, String hours, String format)
    {
        when(mockApplicationProperties.getDefaultBackedString(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY)).thenReturn(hours);
        when(mockApplicationProperties.getDefaultBackedString(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK)).thenReturn(days);
        when(mockApplicationProperties.getDefaultBackedString(APKeys.JIRA_TIMETRACKING_FORMAT)).thenReturn(format);
        jiraDurationUtils.updateFormatters(null, null);
    }

    private Issue convertToIssueObject(GenericValue issueGv)
    {
        return new MockIssue(issueGv);
    }

    void _assertEquals(Issue issue, Object object)
    {
        if (object instanceof ReportIssue)
        {
            assertEquals(((ReportIssue) object).getIssue(), issue);
        }
        else
        {
            assertEquals(issue, object);
        }
    }

    private void setupFormat(final String key, final String unit)
    {
        when(mockI18nHelper.getText(eq(key), anyString())).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                return String.format("%s%s", invocation.getArguments()[1], unit);
            }
        });
    }

}
