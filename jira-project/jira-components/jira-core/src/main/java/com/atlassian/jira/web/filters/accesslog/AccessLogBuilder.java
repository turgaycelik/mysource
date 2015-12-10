package com.atlassian.jira.web.filters.accesslog;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.servlet.http.HttpServletRequest;
import java.text.DecimalFormat;

/**
 * A builder class that can create access log entries
 *
 * @since v3.13.2
 */
public class AccessLogBuilder
{
    private ReadableInstant dateOfEvent;
    private String requestId;
    private final HttpServletRequest httpReq;
    private String url;
    private String userName;
    private String sessionId;
    private long responseTimeMS = -1;
    private int httpStatusCode = -1;
    private long responseContentLength = -1;

    /**
     * Constructs a Apache CLF builder in the context of the given HttpServletRequest
     *
     * @param httpReq must be non null
     */
    public AccessLogBuilder(final HttpServletRequest httpReq)
    {
        this.httpReq = httpReq;
    }

    public ReadableInstant getDateOfEvent()
    {
        return dateOfEvent;
    }

    public String getRequestId()
    {
        return requestId;
    }

    public HttpServletRequest getHttpReq()
    {
        return httpReq;
    }

    public String getUrl()
    {
        return url;
    }

    public String getUserName()
    {
        return userName;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public long getResponseTimeMS()
    {
        return responseTimeMS;
    }

    public int getHttpStatusCode()
    {
        return httpStatusCode;
    }

    public long getResponseContentLength()
    {
        return responseContentLength;
    }

    /**
     * Sets the date of the log event.  if this is not provide it defaults to now
     *
     * @param dateOfEvent the date of the log event
     * @return this ApacheLogBuilder to allow a fluent style
     */
    public AccessLogBuilder setDateOfEvent(final ReadableInstant dateOfEvent)
    {
        this.dateOfEvent = dateOfEvent;
        return this;
    }

    /**
     * Sets a request id into the builder
     *
     * @param requestId the id of the request
     * @return this ApacheLogBuilder to allow a fluent style
     */
    public AccessLogBuilder setRequestId(final String requestId)
    {
        this.requestId = requestId;
        return this;
    }

    /**
     * Sets the URL of the builder
     *
     * @param url the ULR in play
     * @return this ApacheLogBuilder to allow a fluent style
     */
    public AccessLogBuilder setUrl(final String url)
    {
        this.url = url;
        return this;
    }

    /**
     * Sets the user name to use in the log
     *
     * @param userName the name of the user
     * @return this ApacheLogBuilder to allow a fluent style
     */
    public AccessLogBuilder setUserName(final String userName)
    {
        this.userName = userName;
        return this;
    }

    /**
     * Sets the sessionId to use in this builder
     *
     * @param sessionId the session id in play
     * @return this ApacheLogBuilder to allow a fluent style
     */
    public AccessLogBuilder setSessionId(final String sessionId)
    {
        this.sessionId = sessionId;
        return this;
    }

    /**
     * Sets the response time in milliseconds
     *
     * @param responseTimeMS the resposne time in ms
     * @return this ApacheLogBuilder to allow a fluent style
     */
    public AccessLogBuilder setResponseTimeMS(final long responseTimeMS)
    {
        this.responseTimeMS = responseTimeMS;
        return this;
    }

    /**
     * Sets the HTTP status code
     *
     * @param httpStatusCode the HTTP status code
     * @return this ApacheLogBuilder to allow a fluent style
     */
    public AccessLogBuilder setHttpStatusCode(final int httpStatusCode)
    {
        this.httpStatusCode = httpStatusCode;
        return this;
    }

    /**
     * Sets the response content length
     *
     * @param responseContentLength the response content length
     * @return this ApacheLogBuilder to allow a fluent style
     */
    public AccessLogBuilder setResponseContentLength(final long responseContentLength)
    {
        this.responseContentLength = responseContentLength;
        return this;
    }

    /**
     * This turns the builder into a Apache Combined Log Format line with a JIRA twist
     * <p/>
     * http://httpd.apache.org/docs/1.3/logs.html
     *
     * @return a string representing the Apache CLF log entry
     */
    public String toApacheCombinedLogFormat()
    {
        final String transportProtocol = httpReq.getProtocol();
        final String httpMethod = httpReq.getMethod();
        final String remoteAddress = httpReq.getRemoteAddr();
        final String userAgent = httpReq.getHeader("User-Agent");
        final String referer = httpReq.getHeader("Referer");
        final String dateStr = dateOfEvent == null ? getDateString(new DateTime()) : getDateString(dateOfEvent);
        final Long contentLen = responseContentLength < 0 ? null : responseContentLength;
        final Long responseTimeMillis = responseTimeMS < 0 ? null : responseTimeMS;
        final Integer httpStatus = httpStatusCode < 0 ? null : httpStatusCode;

        final StringBuilder urlStr = new StringBuilder().append(enc(httpMethod)).append(" ").append(enc(url)).append(" ").append(
            enc(transportProtocol));

        //
        // According to http://httpd.apache.org/docs/1.3/logs.html - Apache Combined Log Format looks like this :
        //
        //      127.0.0.1 - frank [10/Oct/2000:13:55:36 -0700] "GET /apache_pb.gif HTTP/1.0" 200 2326 "http://www.example.com/start.html" "Mozilla/4.08 [en] (Win98; I ;Nav)"
        //
        // JIRA flavour - We insert the request id into the "identd" field, insert a time taken in field 7 and put the session id at the end.
        //
        return new StringBuilder(enc(remoteAddress)) // remote address
        .append(" ").append(enc(requestId)) // <<-- normally the '-' character in Apache CLF
        .append(" ").append(enc(userName)) // user
        .append(" ").append(enc(dateStr)) //date
        .append(" ").append(quote(urlStr)) // url
        .append(" ").append(enc(httpStatus)) // http return
        .append(" ").append(enc(contentLen)) // content length
        .append(" ").append(enc(formatMStoDecimalSecs(responseTimeMillis))) // <<-- not specified in Apache CLF
        .append(" ").append(quote(referer)) // referer [sic]
        .append(" ").append(quote(userAgent)) // user agent
        .append(" ").append(quote(sessionId)) // <<-- not specified in Apache CLF
        .toString();
    }

    /**
     * Puts quotes around the Object.toString() value, unless its null or blank in which case it becomes the - character
     * meaning empty.
     *
     * @param o the object to quote
     * @return a quoted String or - if the object has no value
     */
    static String quote(final Object o)
    {
        if ((o == null) || StringUtils.isBlank(o.toString()))
        {
            return "-";
        }
        return new StringBuilder("\"").append(o.toString().trim()).append("\"").toString();
    }

    /**
     * This will return the .toString() value of object or - if the value is blank
     *
     * @param o the object in play
     * @return the .toString() value of object or - if the value is blank
     */
    static String enc(final Object o)
    {
        if ((o == null) || StringUtils.isBlank(o.toString()))
        {
            return "-";
        }
        return o.toString().trim();
    }

    private static final String LOG_DF = "[dd/MMM/yyyy:HH:mm:ss Z]";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern(LOG_DF);

    static String getDateString(final ReadableInstant date)
    {
        if (date == null)
        {
            return "-";
        }
        return DATE_FORMAT.print(date);
    }

    static String formatMStoDecimalSecs(final Long responseMS)
    {
        if (responseMS == null)
        {
            return null;
        }
        final float secs = (responseMS.longValue()) / 1000f;
        return new DecimalFormat("#0.0000").format(secs);
    }
}
