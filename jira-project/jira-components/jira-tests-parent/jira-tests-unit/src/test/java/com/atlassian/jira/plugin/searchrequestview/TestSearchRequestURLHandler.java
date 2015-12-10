package com.atlassian.jira.plugin.searchrequestview;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.web.RequestParameterKeys;
import com.atlassian.jira.web.bean.PagerFilter;
import com.mockobjects.servlet.MockHttpServletRequest;

@RunWith(MockitoJUnitRunner.class)
public class TestSearchRequestURLHandler
{
    @Mock
    private BuildUtilsInfo buildUtilsInfo;

    DefaultSearchRequestURLHandler urlHandler;

    @Before
    public void setUp()
    {
       urlHandler = new DefaultSearchRequestURLHandler(null, null, null, null, null, null, null, null, null, buildUtilsInfo, null);
    }

    @Test
    public void testGetPagerFilter()
    {

        //test default values (URL params are not specified)
        PagerFilter pager = urlHandler.getPagerFilter(getPageFilterMockRequest(null, null));
        assertEquals(Integer.MAX_VALUE, pager.getMax());
        assertEquals(0, pager.getStart());

        //test default values (URL params are corrupt)
        pager = urlHandler.getPagerFilter(getPageFilterMockRequest("13zz", "e23d"));
        assertEquals(Integer.MAX_VALUE, pager.getMax());
        assertEquals(0, pager.getStart());

        //test values specified by URL param
        pager = urlHandler.getPagerFilter(getPageFilterMockRequest("10", "20"));
        assertEquals(10, pager.getMax());
        assertEquals(20, pager.getStart());
    }

    private MockHttpServletRequest getPageFilterMockRequest(final String tempMax, final String start)
    {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setupAddParameter(RequestParameterKeys.JIRA_SEARCH_REQUEST_VIEW_TEMP_MAX, tempMax);
        request.setupAddParameter(RequestParameterKeys.JIRA_SEARCH_REQUEST_VIEW_PAGER_START, start);
        return request;
    }


}
