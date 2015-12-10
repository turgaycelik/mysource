package com.atlassian.jira.charts.piechart;

import java.util.Map;

import com.atlassian.jira.charts.PieSegment;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.StatisticMap;

import org.hamcrest.Matcher;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator.ISSUETYPE;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v6.0
 */
@RunWith (MockitoJUnitRunner.class)
public class TestPieChartDataSetFactory
{
    private static final MockIssueType BUG = new MockIssueType("1", "bug");
    private static final MockIssueType FEATURE = new MockIssueType("2", "feature");

    @Mock
    private CustomFieldManager customFieldManager;

    @Mock
    private ConstantsManager constantsManager;

    @Mock
    private I18nHelper i18nHelper;

    @Mock
    private StatisticMap statisticMap;

    private PieChartDataSetFactory dataSetFactory;

    @Before
    public void setUp() throws Exception
    {
        dataSetFactory = new PieChartDataSetFactory(constantsManager, customFieldManager, statisticMap, i18nHelper, ISSUETYPE);

        ComponentAccessor.initialiseWorker(new MockComponentWorker());

        when(i18nHelper.getText("common.concepts.issues")).thenReturn("issues");
    }

    @Test
    public void testRawPieDatasetGeneration()
    {
        Map<MockIssueType, Number> stats = newLinkedHashMap();
        stats.put(BUG, 1);
        stats.put(FEATURE, 2);

        when(statisticMap.entrySet()).thenReturn(stats.entrySet());
        when(statisticMap.getIrrelevantCount()).thenReturn(6);

        final PieDataset pieDataset = dataSetFactory.createRawPieDataset();

        assertThat(pieDataset, is(notNullValue()));
        assertThat(pieDataset.getKeys().size(), is(3));

        assertDatasetPieSegment(pieDataset, 0, is("bug"), is(BUG), 1);
        assertDatasetPieSegment(pieDataset, 1, is("feature"), is(FEATURE), 2);
        assertDatasetPieSegment(pieDataset, 2, is(nullValue()), is(nullValue()), 6);
    }

    @Test
    public void testCategoryDatasetGeneration()
    {
        PieDataset pieDataset = mock(PieDataset.class);
        when(pieDataset.getKeys()).thenReturn(asList("a", "b"));
        when(pieDataset.getValue("a")).thenReturn(10);
        when(pieDataset.getValue("b")).thenReturn(15);

        final CategoryDataset categoryDataset = dataSetFactory.createCategoryDataset(25, pieDataset);

        assertThat(categoryDataset.getValue("issues", "a"), is((Number) 10));
        assertThat(categoryDataset.getValue("issues", "b"), is((Number) 15));

        assertThat(categoryDataset.getValue("%", "a"), is((Number) 40.0));
        assertThat(categoryDataset.getValue("%", "b"), is((Number) 60.0));
    }

    private static void assertDatasetPieSegment(PieDataset dataset, int index, Matcher<? super String> nameMatcher, Matcher<?> keyMatcher, int expectedNumber)
    {
        final PieSegment pieSegment = (PieSegment) dataset.getKey(index);
        assertThat(pieSegment.getName(), nameMatcher);
        assertThat(pieSegment.getKey(), (Matcher<? super Object>) keyMatcher);
        assertThat(dataset.getValue(index), is((Number) expectedNumber));
    }
}