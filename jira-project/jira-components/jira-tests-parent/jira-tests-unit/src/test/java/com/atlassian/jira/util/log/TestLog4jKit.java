package com.atlassian.jira.util.log;

import java.util.Hashtable;

import org.apache.log4j.MDC;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
/**
 */
public class TestLog4jKit
{
    @Test
    public void testPutStuffToMDC()
    {
        Log4jKit.clearMDC();
        assertEmptyMDC();

        Log4jKit.putToMDC("userName", "requestId", "asessionId", "requestURL", "ipAddress");


        Hashtable context = MDC.getContext();
        assertNotNull(context);
        assertEquals(5, context.size());
        assertEquals("userName", context.get(Log4jKit.MDC_JIRA_USERNAME));
        assertEquals("requestId", context.get(Log4jKit.MDC_JIRA_REQUEST_ID));
        assertEquals("asessionId", context.get(Log4jKit.MDC_JIRA_ASSESSION_ID));
        assertEquals("requestURL", context.get(Log4jKit.MDC_JIRA_REQUEST_URL));
        assertEquals("ipAddress", context.get(Log4jKit.MDC_JIRA_REQUEST_IPADDR));

        // its the same map underneath
        Log4jKit.putUserToMDC("rastus watermelon");
        context = MDC.getContext();
        assertNotNull(context);
        assertEquals(5, context.size());
        assertEquals("rastus watermelon", context.get(Log4jKit.MDC_JIRA_USERNAME));
        assertEquals("requestId", context.get(Log4jKit.MDC_JIRA_REQUEST_ID));
        assertEquals("asessionId", context.get(Log4jKit.MDC_JIRA_ASSESSION_ID));
        assertEquals("requestURL", context.get(Log4jKit.MDC_JIRA_REQUEST_URL));
        assertEquals("ipAddress", context.get(Log4jKit.MDC_JIRA_REQUEST_IPADDR));

        Log4jKit.clearMDC();
        assertEmptyMDC();
    }

    @Test
    public void testAnonymousUserPut()
    {
        Log4jKit.putUserToMDC(null);
        Hashtable context = MDC.getContext();
        assertNotNull(context);
        assertEquals("anonymous",context.get(Log4jKit.MDC_JIRA_USERNAME));

        Log4jKit.putUserToMDC("");
        context = MDC.getContext();
        assertNotNull(context);
        assertEquals("anonymous",context.get(Log4jKit.MDC_JIRA_USERNAME));
    }

    private void assertEmptyMDC()
    {
        final Hashtable context = MDC.getContext();
        assertTrue(context == null || context.size() == 0);
    }

}
