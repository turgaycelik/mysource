package com.atlassian.jira.web.filters.accesslog;

import com.atlassian.instrumentation.operations.OpTimerFactory;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.servlet.MockHttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_REQUEST_ASESSIONID;
import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_REQUEST_ID;
import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_REQUEST_START_MILLIS;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit test case for {@link com.atlassian.jira.web.filters.accesslog.AccessLogImprinter}.
 */
public class TestAccessLogImprinter
{
    @Mock
    private OpTimerFactory opTimerFactory;

    @Mock
    private ClusterManager clusterManager;

    @Before
    public void setUp() throws Exception
    {
        initMocks(this);
        ComponentAccessor.initialiseWorker(new MockComponentWorker()
                .addMock(OpTimerFactory.class, opTimerFactory)
                .addMock(ClusterManager.class, clusterManager)
        );
    }

    @Test
    public void testNoAttributes()
    {
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();

        AccessLogImprinter imprinter = new AccessLogImprinter(httpServletRequest);

        String value = imprinter.imprintHTMLComment();
        assertNotNull(value);
        assertTrue(value, value.startsWith("\n<!--"));
        assertTrue(value, value.endsWith("\n-->"));
        assertTrue(value, value.contains("REQUEST ID : -"));
        assertTrue(value, value.contains("REQUEST TIMESTAMP : -"));
        assertTrue(value, value.contains("REQUEST TIME : -"));
        assertTrue(value, value.contains("ASESSIONID : -"));
        assertThat(value, is(not(containsString("NODE ID"))));
    }

    @Test
    public void testValuesSet()
    {
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();

        AccessLogImprinter imprinter = new AccessLogImprinter(httpServletRequest);

        httpServletRequest.setAttribute(JIRA_REQUEST_START_MILLIS, System.currentTimeMillis());
        String value = imprinter.imprintHTMLComment();
        assertNotNull(value);
        assertTrue(value, !value.contains("REQUEST TIMESTAMP : -"));

        httpServletRequest.setAttribute(JIRA_REQUEST_ID, "requestId");
        value = imprinter.imprintHTMLComment();
        assertNotNull(value);
        assertTrue(value, value.contains("REQUEST ID : requestId"));

        httpServletRequest.setAttribute(JIRA_REQUEST_ASESSIONID, "ABCDEF1234");
        value = imprinter.imprintHTMLComment();
        assertNotNull(value);
        assertTrue(value, value.contains("ASESSIONID : ABCDEF1234"));

        assertThat(value, is(not(containsString("NODE ID"))));
    }

    @Test
    public void nodeIdShouldBeSetWhenClustered()
    {
        when(clusterManager.isClustered()).thenReturn(true);
        when(clusterManager.getNodeId()).thenReturn("node");

        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();

        AccessLogImprinter imprinter = new AccessLogImprinter(httpServletRequest);

        final String value = imprinter.imprintHTMLComment();

        assertNotNull(value);
        assertThat(value, containsString("NODE ID : node"));
    }

    @Test
    public void testEscaping()
    {
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();

        AccessLogImprinter imprinter = new AccessLogImprinter(httpServletRequest);

        httpServletRequest.setAttribute(JIRA_REQUEST_ID, "a man smoking a pipe <!-- in it");
        String value = imprinter.imprintHTMLComment();
        assertNotNull(value);
        assertTrue(value, !value.contains("REQUEST ID : \"a man smoking a pipe  <!-: comment in it\""));
    }
}
