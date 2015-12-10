package com.atlassian.jira.charts.piechart;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.PieSegment;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestAppender;
import com.atlassian.jira.junit.rules.ClearStatics;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.query.Query;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @since v6.0
 */
@RunWith (MockitoJUnitRunner.class)
public class TestPieChartUrlGeneratorFactory
{
    @Mock
    private CustomFieldManager customFieldManager;

    @Mock
    private ConstantsManager constantsManager;

    @Mock
    private SearchService searchService;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private I18nHelper i18nHelper;

    @Mock
    private SearchRequest baseSearchRequest;

    @Mock
    private User user;

    @Mock
    private SearchProvider searchProvider;

    @Mock
    private SearchRequestAppender searchRequestAppender;

    @Mock
    private Query query;

    @Rule
    public ClearStatics clearStatics = new ClearStatics();

    private PieChartUrlGeneratorFactory urlGeneratorFactory;

    @Before
    public void setUp() throws Exception
    {
        when(applicationProperties.getString(APKeys.JIRA_BASEURL)).thenReturn("http://localhost:8080");
        when(searchService.getQueryString(user, query)).thenReturn("SOMEQUERYSTRING");

        urlGeneratorFactory = new PieChartUrlGeneratorFactory(searchService, applicationProperties, searchRequestAppender, user, baseSearchRequest);

        ComponentAccessor.initialiseWorker(new MockComponentWorker());
    }

    @Test
    public void categoryUrlGeneratorReturnsSensibleUrlForPieSegment()
    {
        CategoryDataset dataset = mock(CategoryDataset.class);

        PieSegment pieSegment = mock(PieSegment.class);
        when(dataset.getColumnKey(3)).thenReturn(pieSegment);
        when(pieSegment.getKey()).thenReturn("MyValue");

        SearchRequest segmentSearchRequest = mock(SearchRequest.class);
        when(segmentSearchRequest.getQuery()).thenReturn(query);

        when(searchRequestAppender.appendInclusiveSingleValueClause("MyValue", baseSearchRequest)).thenReturn(segmentSearchRequest);

        final String url = urlGeneratorFactory.getCategoryUrlGenerator().generateURL(dataset, 2, 3);

        assertThat(url, is("http://localhost:8080/secure/IssueNavigator.jspa?reset=trueSOMEQUERYSTRING"));
    }

    @Test
    public void categoryUrlGeneratorReturnsNullForNonPieSegment()
    {
        CategoryDataset dataset = mock(CategoryDataset.class);

        when(dataset.getColumnKey(3)).thenReturn("a key");

        final String url = urlGeneratorFactory.getCategoryUrlGenerator().generateURL(dataset, 2, 3);
        verifyZeroInteractions(searchRequestAppender);

        assertThat(url, is(nullValue()));
    }

    @Test
    public void pieUrlGeneratorReturnsSensibleUrlForSingleValueSegment()
    {
        PieDataset dataset = mock(PieDataset.class);

        PieSegment pieSegment = mock(PieSegment.class);
        when(pieSegment.isGenerateUrl()).thenReturn(true);
        when(pieSegment.getKey()).thenReturn("MyValue");

        SearchRequest segmentSearchRequest = mock(SearchRequest.class);
        when(segmentSearchRequest.getQuery()).thenReturn(query);

        when(searchRequestAppender.appendInclusiveSingleValueClause("MyValue", baseSearchRequest)).thenReturn(segmentSearchRequest);

        final String url = urlGeneratorFactory.getPieUrlGenerator("Other").generateURL(dataset, pieSegment, 0);

        assertThat(url, is("http://localhost:8080/secure/IssueNavigator.jspa?reset=trueSOMEQUERYSTRING"));

    }

    @Test
    public void pieUrlGeneratorReturnsSensibleUrlForOtherSegment()
    {
        PieDataset dataset = mock(PieDataset.class);

        PieSegment pieSegment1 = mock(PieSegment.class);
        when(pieSegment1.getKey()).thenReturn("Value1");

        PieSegment pieSegment2 = mock(PieSegment.class);
        when(pieSegment2.getKey()).thenReturn("Value2");

        when(dataset.getKeys()).thenReturn(asList(pieSegment1, pieSegment2));

        SearchRequest segmentSearchRequest = mock(SearchRequest.class);
        when(segmentSearchRequest.getQuery()).thenReturn(query);

        when(searchRequestAppender.appendExclusiveMultiValueClause(asList("Value1", "Value2"), baseSearchRequest)).thenReturn(segmentSearchRequest);

        final String url = urlGeneratorFactory.getPieUrlGenerator("Other").generateURL(dataset, "Other", 0);

        assertThat(url, is("http://localhost:8080/secure/IssueNavigator.jspa?reset=trueSOMEQUERYSTRING"));
    }


    @Test
    public void pieUrlGeneratorReturnsNoUrlWhenOtherSegmentIsNotSupported()
    {
        PieDataset dataset = mock(PieDataset.class);

        PieSegment pieSegment1 = mock(PieSegment.class);
        when(pieSegment1.getKey()).thenReturn("Value1");

        PieSegment pieSegment2 = mock(PieSegment.class);
        when(pieSegment2.getKey()).thenReturn("Value2");

        when(dataset.getKeys()).thenReturn(asList(pieSegment1, pieSegment2));

        SearchRequest segmentSearchRequest = mock(SearchRequest.class);
        when(segmentSearchRequest.getQuery()).thenReturn(query);

        when(searchRequestAppender.appendExclusiveMultiValueClause(isA(Iterable.class), eq(baseSearchRequest))).thenReturn(null);

        final String url = urlGeneratorFactory.getPieUrlGenerator("Other").generateURL(dataset, "Other", 0);

        assertThat(url, is(nullValue()));
    }
}