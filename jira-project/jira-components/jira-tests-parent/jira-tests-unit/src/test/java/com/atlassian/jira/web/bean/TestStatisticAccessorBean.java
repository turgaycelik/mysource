package com.atlassian.jira.web.bean;

import java.util.Map;

import com.atlassian.jira.issue.search.ReaderCache;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.FieldVisibilityManager;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.Mockito;

import junit.framework.Assert;

import static org.mockito.Mockito.when;

public class TestStatisticAccessorBean
{
    @Rule public final TestRule mockContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    private StatisticsMapper<String> mapper;

    @Mock
    private StatisticAccessorBean.SearchStatisticsResult result;

    // We need to mock these because the StatisticAccessBean statically grabs them from the ComponentAccessor
    // during instantiation :/ They aren't actually used by this test, however.
    @Mock
    @AvailableInContainer
    private SearchProvider searchProvider;
    @Mock
    @AvailableInContainer
    private FieldVisibilityManager fieldVisibilityManager;
    @Mock
    @AvailableInContainer
    private ReaderCache readerCache;

    @Test
    public void testGetWrapperAccumulates() throws Exception
    {
        final Map<String, Integer> hits = MapBuilder.<String, Integer>newBuilder()
                .add("10000", 2)
                .add("10001", 5)
                .add("10002", 1)
                .toMap();
        when(result.getStatistics()).thenReturn(hits);

        // We want two different ids to map to the same value.
        when(mapper.getValueFromLuceneField("10000")).thenReturn("Apple");
        when(mapper.getValueFromLuceneField("10001")).thenReturn("Cookie");
        when(mapper.getValueFromLuceneField("10002")).thenReturn("Apple");

        // every value is valid for unit testing :)
        when(mapper.isValidValue(Mockito.any(String.class))).thenReturn(true);

        final StatisticAccessorBean accessor = new StatisticAccessorBean() {
            @Override
            protected StatisticAccessorBean.SearchStatisticsResult searchCountMap(String groupField)
                    throws SearchException
            {
                return result;
            }
        };

        // The two different "Apples" should have been summed up.
        final StatisticMapWrapper wrapper = accessor.getWrapper(mapper);
        Assert.assertEquals(3, wrapper.getStatistics().get("Apple"));
    }
}