package com.atlassian.jira.web.filters.accesslog;

import java.util.Hashtable;

import com.atlassian.instrumentation.operations.OpTimerFactory;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.servlet.MockHttpServletRequest;
import com.atlassian.jira.mock.servlet.MockHttpServletResponse;
import com.atlassian.jira.util.log.Log4jKit;

import com.google.common.collect.Iterables;

import org.apache.log4j.MDC;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 */
public class TestAccessLogRequestInfo
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @Mock
    private ClusterManager clusterManager;

    @Before
    public void setUp() throws Exception
    {
        initMocks(this);
        ComponentAccessor.initialiseWorker(new MockComponentWorker()
                .addMock(ClusterManager.class, clusterManager)
        );

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        AccessLogRequestInfo.concurrentRequests.set(0);
    }

    @Test
    public void nodeIdShouldBeSetWhenClustered()
    {
        when(clusterManager.isClustered()).thenReturn(true);
        when(clusterManager.getNodeId()).thenReturn("node");

        final AccessLogRequestInfo accessLogRequestInfo1 = new AccessLogRequestInfo();
        accessLogRequestInfo1.enterRequest(request, response);

        assertThat(response.getHeader(AccessLogRequestInfo.X_NODEID_HEADER).get(0), is(equalTo("node")));
    }


    @Test
    public void testCanBeCalledTwice()
    {
        assertEquals(0L, AccessLogRequestInfo.concurrentRequests.get());
        assertNull(request.getAttribute(AccessLogRequestInfo.JIRA_REQUEST_START_MILLIS));
        assertNull(request.getAttribute(AccessLogRequestInfo.JIRA_REQUEST_ID));

        final AccessLogRequestInfo accessLogRequestInfo1 = new AccessLogRequestInfo();
        accessLogRequestInfo1.enterRequest(request, response);

        final Object originalStartTime = request.getAttribute(AccessLogRequestInfo.JIRA_REQUEST_START_MILLIS);
        assertNotNull(originalStartTime);
        assertNotNull(request.getAttribute(AccessLogRequestInfo.JIRA_REQUEST_ID));
        assertEquals(1L, AccessLogRequestInfo.concurrentRequests.get());

        assertThat(response.getHeader(AccessLogRequestInfo.X_NODEID_HEADER), IsCollectionWithSize.hasSize(0));

        final AccessLogRequestInfo accessLogRequestInfo2 = new AccessLogRequestInfo();
        accessLogRequestInfo2.enterRequest(request, response);

        final Object secondStartTime = request.getAttribute(AccessLogRequestInfo.JIRA_REQUEST_START_MILLIS);
        assertSame(originalStartTime, secondStartTime);
        assertNotNull(request.getAttribute(AccessLogRequestInfo.JIRA_REQUEST_ID));
        assertEquals(1L, AccessLogRequestInfo.concurrentRequests.get());

        accessLogRequestInfo1.exitRequest(request);
        assertEquals(0L, AccessLogRequestInfo.concurrentRequests.get());

        accessLogRequestInfo1.exitRequest(request);
        assertEquals(0L, AccessLogRequestInfo.concurrentRequests.get());
    }

    @Test
    public void testLog4JMDCInteraction()
    {
        // https interaction

        request = new MockHttpServletRequest();
        request.setRemoteAddr("172.45.53.1");
        request.setContextPath("/cntx");
        request.setRequestURL("https://somehostname/cntx/url/path?p=1");

        AccessLogRequestInfo requestInfo = new AccessLogRequestInfo();
        requestInfo.enterRequest(request, response);

        Hashtable context = MDC.getContext();
        assertNotNull(context);
        assertEquals("/url/path?p=1", context.get(Log4jKit.MDC_JIRA_REQUEST_URL));
        assertEquals("172.45.53.1", context.get(Log4jKit.MDC_JIRA_REQUEST_IPADDR));
        assertEquals("anonymous", context.get(Log4jKit.MDC_JIRA_USERNAME));

        // http interaction

        request = new MockHttpServletRequest();
        request.setRemoteAddr("172.45.53.1");
        request.setContextPath("/cntx");
        request.setRequestURL("http://somehostname/cntx/url/path?p=1");

        requestInfo = new AccessLogRequestInfo();
        requestInfo.enterRequest(request, response);


        context = MDC.getContext();
        assertNotNull(context);
        assertEquals("/url/path?p=1", context.get(Log4jKit.MDC_JIRA_REQUEST_URL));
        assertEquals("172.45.53.1", context.get(Log4jKit.MDC_JIRA_REQUEST_IPADDR));
        assertEquals("anonymous", context.get(Log4jKit.MDC_JIRA_USERNAME));

        // bad input interaction

        request = new MockHttpServletRequest();
        request.setContextPath("/Xcntx");
        request.setRequestURL("httpX://somehostname/cntx/url/path?p=1");

        requestInfo = new AccessLogRequestInfo();
        requestInfo.enterRequest(request, response);


        context = MDC.getContext();
        assertNotNull(context);
        assertEquals("httpX://somehostname/cntx/url/path?p=1", context.get(Log4jKit.MDC_JIRA_REQUEST_URL));
        assertEquals("anonymous", context.get(Log4jKit.MDC_JIRA_USERNAME));

    }
}
