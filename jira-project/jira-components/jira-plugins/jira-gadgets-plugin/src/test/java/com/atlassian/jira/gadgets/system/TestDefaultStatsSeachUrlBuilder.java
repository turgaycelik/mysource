package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.ProjectStatisticsMapper;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.issue.statistics.TwoDimensionalStatsMap;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;

import static org.easymock.classextension.EasyMock.expect;

/**
 *
 */
public class TestDefaultStatsSeachUrlBuilder extends TestCase
{
    public void testHeaders()
    {
        SearchService ss = EasyMock.createMock(SearchService.class);
        JiraAuthenticationContext authenticationContext = EasyMock.createMock(JiraAuthenticationContext.class);
        User user = new MockUser("foo");
        expect(authenticationContext.getLoggedInUser()).andReturn(user);
        DefaultStatsSearchUrlBuilder builder = new DefaultStatsSearchUrlBuilder(ss, authenticationContext);

        StatisticsMapper axisMapper = EasyMock.createMock(StatisticsMapper.class);
        SearchRequest request = new SearchRequest();

        SearchRequest result = new SearchRequest();
        EasyMock.expect(axisMapper.getSearchUrlSuffix("foo", request)).andReturn(result);

        expect(ss.getQueryString(user, result.getQuery())).andReturn("theUrl");

        EasyMock.replay(authenticationContext, axisMapper, ss);

        String urlForHeaderCell = builder.getSearchUrlForHeaderCell("foo", axisMapper, request);
        assertEquals("theUrl", urlForHeaderCell);
    }

    public void testCells()
    {
        SearchService ss = EasyMock.createMock(SearchService.class);
        JiraAuthenticationContext authenticationContext = EasyMock.createMock(JiraAuthenticationContext.class);
        User user = new MockUser("foo");
        expect(authenticationContext.getLoggedInUser()).andReturn(user);
        DefaultStatsSearchUrlBuilder builder = new DefaultStatsSearchUrlBuilder(ss, authenticationContext);

        TwoDimensionalStatsMap twodmap = EasyMock.createMock(TwoDimensionalStatsMap.class);
        SearchRequest request = new SearchRequest();

        SearchRequest result = new SearchRequest();
        SearchRequest result2 = new SearchRequest();

        StatisticsMapper xaxisMapper = EasyMock.createMock(StatisticsMapper.class);
        StatisticsMapper yaxisMapper = EasyMock.createMock(StatisticsMapper.class);

        expect(twodmap.getxAxisMapper()).andReturn(xaxisMapper);
        expect(twodmap.getyAxisMapper()).andReturn(yaxisMapper);
        EasyMock.expect(yaxisMapper.getSearchUrlSuffix("bar", request)).andReturn(result);
        EasyMock.expect(xaxisMapper.getSearchUrlSuffix("foo", request)).andReturn(result2);

        expect(ss.getQueryString(user, result2.getQuery())).andReturn("theUrl");

        EasyMock.replay(authenticationContext, twodmap, ss, yaxisMapper, xaxisMapper);

        String urlForHeaderCell = builder.getSearchUrlForCell("foo", "bar", twodmap, request);
        assertEquals("theUrl", urlForHeaderCell);
    }

    public void testCellsNotFirst()
    {
        SearchService ss = EasyMock.createMock(SearchService.class);
        JiraAuthenticationContext authenticationContext = EasyMock.createMock(JiraAuthenticationContext.class);
        User user = new MockUser("foo");
        expect(authenticationContext.getLoggedInUser()).andReturn(user);
        DefaultStatsSearchUrlBuilder builder = new DefaultStatsSearchUrlBuilder(ss, authenticationContext);

        TwoDimensionalStatsMap twodmap = EasyMock.createMock(TwoDimensionalStatsMap.class);
        SearchRequest request = new SearchRequest();

        SearchRequest result = new SearchRequest();
        SearchRequest result2 = new SearchRequest();

        StatisticsMapper xaxisMapper = EasyMock.createMock(ProjectStatisticsMapper.class);
        StatisticsMapper yaxisMapper = EasyMock.createMock(StatisticsMapper.class);

        expect(twodmap.getxAxisMapper()).andReturn(xaxisMapper);
        expect(twodmap.getyAxisMapper()).andReturn(yaxisMapper);
        EasyMock.expect(yaxisMapper.getSearchUrlSuffix("bar", request)).andReturn(result);
        EasyMock.expect(xaxisMapper.getSearchUrlSuffix("foo", request)).andReturn(result2);

        expect(ss.getQueryString(user, result2.getQuery())).andReturn("theUrl");

        EasyMock.replay(authenticationContext, twodmap, ss, yaxisMapper, xaxisMapper);

        String urlForHeaderCell = builder.getSearchUrlForCell("foo", "bar", twodmap, request);
        assertEquals("theUrl", urlForHeaderCell);
    }
}
