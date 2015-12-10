package com.atlassian.jira.web.action;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.mock.web.action.MockAbstractBrowser;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.bean.PagerFilter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import webwork.action.ActionContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestAbstractBrowser
{
    @Before
    public void setUp()
    {
        new MockComponentWorker().init().addMock(OfBizDelegator.class, new MockOfBizDelegator());
    }

    @After
    public void tearDown() throws Exception
    {
        ComponentAccessor.initialiseWorker(null);
        ActionContext.getSession().remove(SessionKeys.SEARCH_PAGER);
        ActionContext.getSession().remove(SessionKeys.SEARCH_SORTER);
        ActionContext.getSession().remove(SessionKeys.SEARCH_REQUEST);
    }

    @Test
    public void testSetParameters()
    {
        HashMap<String, String[]> params = new HashMap<String, String[]>();
        params.put("id", new String[] { "1" });
        params.put("version", new String[] { "10" });

        MockAbstractBrowser ab = new MockAbstractBrowser();
        ab.setParameters(params);

        Map returnParams = ab.getParameters();
        assertNotNull(returnParams);
        assertTrue(!returnParams.isEmpty());
        assertEquals(2, params.size());
    }

    @Test
    public void testGetSingleParam()
    {
        HashMap<String, String[]> params = new HashMap<String, String[]>();
        params.put("id", new String[] { "1" });
        params.put("version", new String[] { "10" });

        MockAbstractBrowser ab = new MockAbstractBrowser();
        ab.setParameters(params);
        assertNull(ab.getSingleParam("ABC"));
        Assert.assertEquals("1", ab.getSingleParam("id"));
        Assert.assertEquals("10", ab.getSingleParam("version"));
    }

    @Test
    public void testSetStart1()
    {
        MockAbstractBrowser ab = new MockAbstractBrowser();

        ab.setStart("1");
        Assert.assertEquals(1, ab.getPager().getStart());
    }

    @Test
    public void testSetStart2()
    {
        MockAbstractBrowser ab = new MockAbstractBrowser();

        Assert.assertEquals(0, ab.getPager().getStart());
        ab.setStart("this is a string");
        Assert.assertEquals(0, ab.getPager().getStart());
    }

    @Test
    public void niceStartShouldBeZeroWhenGetBrowsableItemsReturnsNull()
    {
        // Set up
        final AbstractBrowser browser = mock(AbstractBrowser.class);
        when(browser.getBrowsableItems()).thenReturn(null);
        when(browser.getNiceStart()).thenCallRealMethod();

        // Invoke
        final int niceStart = browser.getNiceStart();

        // Check
        assertEquals(0, niceStart);
    }

    @Test
    public void niceStartShouldBeZeroWhenGetBrowsableItemsReturnsEmptyList()
    {
        // Set up
        final AbstractBrowser browser = mock(AbstractBrowser.class);
        when(browser.getBrowsableItems()).thenReturn(Collections.emptyList());
        when(browser.getNiceStart()).thenCallRealMethod();

        // Invoke
        final int niceStart = browser.getNiceStart();

        // Check
        assertEquals(0, niceStart);
    }

    @Test
    public void niceStartShouldBePagerStartPlusOneWhenGetBrowsableItemsReturnsNonEmptyList()
    {
        // Set up
        final AbstractBrowser browser = mock(AbstractBrowser.class);
        when(browser.getBrowsableItems()).thenReturn(Collections.singletonList(new Object()));
        when(browser.getNiceStart()).thenCallRealMethod();
        final PagerFilter mockPager = mock(PagerFilter.class);
        final int pagerStart = 666;
        when(mockPager.getStart()).thenReturn(pagerStart);
        when(browser.getPager()).thenReturn(mockPager);

        // Invoke
        final int niceStart = browser.getNiceStart();

        // Check
        assertEquals(pagerStart + 1, niceStart);
    }

    @Test
    public void niceEndShouldBePagerStartPlusCurrentPageSize()
    {
        // Set up
        final AbstractBrowser browser = mock(AbstractBrowser.class);
        when(browser.getNiceEnd()).thenCallRealMethod();
        final PagerFilter mockPager = mock(PagerFilter.class);
        final int pagerStart = 666;
        when(mockPager.getStart()).thenReturn(pagerStart);
        final List<Object> currentPage = Arrays.asList(new Object(), new Object());
        when(browser.getCurrentPage()).thenReturn(currentPage);
        when(browser.getPager()).thenReturn(mockPager);

        // Invoke
        final int niceEnd = browser.getNiceEnd();

        // Check
        assertEquals(pagerStart + currentPage.size(), niceEnd);
    }
}
