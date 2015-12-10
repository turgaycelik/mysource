package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.issue.statistics.util.FieldValueToDisplayTransformer;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import junit.framework.TestCase;
import org.easymock.Capture;
import org.easymock.classextension.EasyMock;

import static org.easymock.classextension.EasyMock.createMock;

/**
 * Unit test for {@link com.atlassian.jira.gadgets.system.HeatMapResource}
 *
 * @since v4.1
 */
public class TestHeatMapResource extends TestCase {


    public void testCreateSearchUrl() {


        final User mockUser = new MockUser("test");
        final SearchRequest mockSearchRequest = createMock(SearchRequest.class);

        final SearchService mockSearchService = createMock(SearchService.class);
        final VelocityRequestContext mockVelocityRequestContext = createMock(VelocityRequestContext.class);
        final StatisticsMapper mockStatMapper = createMock(StatisticsMapper.class);
        final Query mockSearchQuery = createMock(Query.class);
        final Object obj = new Object();

        EasyMock.expect(mockStatMapper.getSearchUrlSuffix(EasyMock.eq(obj), EasyMock.eq(mockSearchRequest)))
                .andReturn(mockSearchRequest);
        EasyMock.expect(mockVelocityRequestContext.getCanonicalBaseUrl())
                .andReturn("http://a");
        EasyMock.expect(mockSearchRequest.getQuery())
                .andReturn(mockSearchQuery);
        EasyMock.expect(mockSearchService.getQueryString(EasyMock.eq(mockUser), EasyMock.eq(mockSearchQuery)))
                .andReturn("&queryString");

        // Based on our mock inputs, does the right url get generated?
        EasyMock.replay(mockStatMapper, mockVelocityRequestContext, mockSearchRequest, mockSearchService);
        final HeatMapResource hmr = new HeatMapResource(null, null, null, null, mockSearchService, null, null);
        final String url = hmr.createSearchUrl(mockUser, mockSearchRequest, mockStatMapper, mockVelocityRequestContext, obj);
        EasyMock.verify(mockStatMapper, mockVelocityRequestContext, mockSearchRequest, mockSearchService);

        final String expectedUrl = "http://a/secure/IssueNavigator.jspa?reset=true&queryString";
        assertEquals(url, expectedUrl);

        // It's possible no url suffix was returned, we test that too
        EasyMock.reset(mockStatMapper, mockVelocityRequestContext, mockSearchRequest, mockSearchService);

        EasyMock.expect(mockStatMapper.getSearchUrlSuffix(EasyMock.eq(obj), EasyMock.eq(mockSearchRequest)))
                .andReturn(null);
        EasyMock.expect(mockVelocityRequestContext.getCanonicalBaseUrl())
                .andReturn("http://a");
        Capture<Query> queryCapture = new Capture<Query>();
        EasyMock.expect(mockSearchService.getQueryString(EasyMock.eq(mockUser), EasyMock.capture(queryCapture)))
                .andReturn("");

        EasyMock.replay(mockStatMapper, mockVelocityRequestContext, mockSearchRequest, mockSearchService);
        final String noSuffixUrl = hmr.createSearchUrl(mockUser, mockSearchRequest, mockStatMapper, mockVelocityRequestContext, obj);
        EasyMock.verify(mockStatMapper, mockVelocityRequestContext, mockSearchRequest, mockSearchService);

        assertEquals(queryCapture.getValue(), new QueryImpl());

        final String expectedNoSuffixUrl = "http://a/secure/IssueNavigator.jspa?reset=true";
        assertEquals(noSuffixUrl, expectedNoSuffixUrl);

    }

    public void testCreateDataRow() {

        // Just testing that the data row produced actually contains the values we passed in. Only real
        // calculation is figuring out the percentage.
        @SuppressWarnings("unchecked")
        final FieldValueToDisplayTransformer<String> mockFieldValueToDisplayTransformer = createMock(FieldValueToDisplayTransformer.class);
        final Object fieldValue = new Object();
        final String rowKey = "rowKey";
        final String statType = "statType";
        final int totalIssues = 200;
        final String expectedUrl = "http://a";
        final int expectedIssues = 20;
        // This should be 100 * issues / totalIssues
        final int expectedPercentage = 10;
        final int expectedFontSize = 30;
        final HeatMapResource.DataRow expectedDataRow = new HeatMapResource.DataRow(rowKey, expectedUrl, expectedIssues, expectedPercentage, expectedFontSize);


        HeatMapResource hmr = new HeatMapResource(null, null, null, null, null, null, null) {
            @Override
            double calculateFontSize(int numberOfData, double percentage) {
                return expectedFontSize;
            }
        };
        EasyMock.expect(mockFieldValueToDisplayTransformer.transformFromCustomField(EasyMock.eq(statType), EasyMock.eq(fieldValue),
                EasyMock.eq(expectedUrl)))
                .andReturn(rowKey);

        EasyMock.replay(mockFieldValueToDisplayTransformer);
        HeatMapResource.DataRow result = hmr.createDataRow(fieldValue, mockFieldValueToDisplayTransformer, statType, totalIssues, expectedUrl, 10, expectedIssues);
        EasyMock.verify(mockFieldValueToDisplayTransformer);
        assertEquals(result, expectedDataRow);

    }

    public void testCalculateFontSize() {
        HeatMapResource hmr = new HeatMapResource(null, null, null, null, null, null, null);

        // font size based on number of issues and percentage of these issues
        assertEquals(hmr.calculateFontSize(0, 0), HeatMapResource.MIN_FONT);
        assertEquals(hmr.calculateFontSize(100, 100), HeatMapResource.MIN_FONT + HeatMapResource.MAX_FONT);
        assertEquals(hmr.calculateFontSize(10, 100), 62.0);
    }
}
