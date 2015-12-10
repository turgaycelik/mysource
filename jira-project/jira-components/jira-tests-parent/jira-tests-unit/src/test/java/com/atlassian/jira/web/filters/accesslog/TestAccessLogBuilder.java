package com.atlassian.jira.web.filters.accesslog;

import java.text.SimpleDateFormat;

import com.atlassian.jira.mock.servlet.MockHttpServletRequest;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 */
public class TestAccessLogBuilder
{
    private static final String LOG_DF = "[dd/MMM/yyyy:HH:mm:ss Z]";

    @Test
    public void testSymetry()
    {
        final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRequestURL("/brad/is/cool");

        final AccessLogBuilder builder = new AccessLogBuilder(httpServletRequest);
        assertEquals(httpServletRequest, builder.getHttpReq());

        final DateTime now = new DateTime();
        builder.setDateOfEvent(now);
        assertEquals(now, builder.getDateOfEvent());

        builder.setHttpStatusCode(666);
        assertEquals(666, builder.getHttpStatusCode());

        builder.setRequestId("requestId");
        assertEquals("requestId", builder.getRequestId());

        builder.setResponseContentLength(345);
        assertEquals(345, builder.getResponseContentLength());

        builder.setResponseTimeMS(789);
        assertEquals(789, builder.getResponseTimeMS());

        builder.setSessionId("sessionId");
        assertEquals("sessionId", builder.getSessionId());

        builder.setUrl("/what/the/hell/is/goging/on");
        assertEquals("/what/the/hell/is/goging/on", builder.getUrl());

        builder.setUserName("userName");
        assertEquals("userName", builder.getUserName());
    }

    @Test
    public void testMissingInfo()
    {
        final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();

        final DateTime now = new DateTime();
        final String dateStr = new SimpleDateFormat(LOG_DF).format(now.toDate());

        final AccessLogBuilder builder = new AccessLogBuilder(httpServletRequest);
        builder.setDateOfEvent(now);

        String msg = builder.toApacheCombinedLogFormat();
        assertEquals("- - - " + dateStr + " \"- - -\" - - - - - -", msg);

        builder.setHttpStatusCode(666);
        msg = builder.toApacheCombinedLogFormat();
        assertEquals("- - - " + dateStr + " \"- - -\" 666 - - - - -", msg);

        httpServletRequest.setMethod("GET");
        msg = builder.toApacheCombinedLogFormat();
        assertEquals("- - - " + dateStr + " \"GET - -\" 666 - - - - -", msg);

        builder.setUrl("/stuff");
        msg = builder.toApacheCombinedLogFormat();
        assertEquals("- - - " + dateStr + " \"GET /stuff -\" 666 - - - - -", msg);

        httpServletRequest.setProtocol("HTTP 1.9");
        msg = builder.toApacheCombinedLogFormat();
        assertEquals("- - - " + dateStr + " \"GET /stuff HTTP 1.9\" 666 - - - - -", msg);

        httpServletRequest.setRemoteAddr("198.162.127.1");
        msg = builder.toApacheCombinedLogFormat();
        assertEquals("198.162.127.1 - - " + dateStr + " \"GET /stuff HTTP 1.9\" 666 - - - - -", msg);

        builder.setUserName("fred");
        msg = builder.toApacheCombinedLogFormat();
        assertEquals("198.162.127.1 - fred " + dateStr + " \"GET /stuff HTTP 1.9\" 666 - - - - -", msg);

        builder.setRequestId("req123");
        msg = builder.toApacheCombinedLogFormat();
        assertEquals("198.162.127.1 req123 fred " + dateStr + " \"GET /stuff HTTP 1.9\" 666 - - - - -", msg);

        builder.setSessionId("ABC123");
        msg = builder.toApacheCombinedLogFormat();
        assertEquals("198.162.127.1 req123 fred " + dateStr + " \"GET /stuff HTTP 1.9\" 666 - - - - \"ABC123\"", msg);

        httpServletRequest.setHeader("User-Agent", "Brad Bot 1.7");
        msg = builder.toApacheCombinedLogFormat();
        assertEquals("198.162.127.1 req123 fred " + dateStr + " \"GET /stuff HTTP 1.9\" 666 - - - \"Brad Bot 1.7\" \"ABC123\"", msg);

        httpServletRequest.setHeader("Referer", "/from/here");
        msg = builder.toApacheCombinedLogFormat();
        assertEquals("198.162.127.1 req123 fred " + dateStr + " \"GET /stuff HTTP 1.9\" 666 - - \"/from/here\" \"Brad Bot 1.7\" \"ABC123\"", msg);

        builder.setResponseContentLength(345);
        msg = builder.toApacheCombinedLogFormat();
        assertEquals("198.162.127.1 req123 fred " + dateStr + " \"GET /stuff HTTP 1.9\" 666 345 - \"/from/here\" \"Brad Bot 1.7\" \"ABC123\"", msg);

        builder.setResponseTimeMS(6455);
        msg = builder.toApacheCombinedLogFormat();
        assertEquals("198.162.127.1 req123 fred " + dateStr + " \"GET /stuff HTTP 1.9\" 666 345 6.4550 \"/from/here\" \"Brad Bot 1.7\" \"ABC123\"",
            msg);
    }
}
