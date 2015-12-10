package com.atlassian.jira.web.session.currentusers;

import com.atlassian.jira.util.http.HttpRequestType;
import com.atlassian.jira.web.filters.accesslog.AccessLogIPAddressUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A base class for Mutable {@link JiraUserSession}s
 */
class MutableJiraUserSession implements JiraUserSession
{
    private final AtomicLong requestCount;
    private final AtomicReference<String> userName;
    private final AtomicReference<String> ipAddress;
    private final AtomicReference<Date> lastAccessTime;
    private final AtomicReference<Date> creationTime;
    private final HttpRequestType type;
    private final String asessionId;
    private final String sessionId;

    MutableJiraUserSession(HttpRequestType type, String sessionId, String asessionId)
    {
        this(type,sessionId,asessionId,null);
    }
    
    MutableJiraUserSession(HttpRequestType type, String sessionId, String asessionId, String userName)
    {
        this.type = type;
        this.sessionId = sessionId;
        this.asessionId = asessionId;
        
        this.requestCount = new AtomicLong(0);
        this.userName = new AtomicReference<String>(userName);
        this.ipAddress = new AtomicReference<String>();
        this.lastAccessTime = new AtomicReference<Date>(new Date());
        this.creationTime = new AtomicReference<Date>(new Date());
    }

    public String getId()
    {
        return sessionId;
    }

    public String getASessionId()
    {
        return asessionId;
    }

    public long getRequestCount()
    {
        return requestCount.get();
    }

    public String getUserName()
    {
        return userName.get();
    }

    public HttpRequestType getType()
    {
        return type;
    }

    public String getIpAddress()
    {
        return ipAddress.get();
    }

    public Date getCreationTime()
    {
        return creationTime.get();
    }

    public Date getLastAccessTime()
    {
        return lastAccessTime.get();
    }


    /**
     * The user name might not be set the first time the session is hit.  So we record it every time.  Also the number of
     * requests need to be increment as well.
     *
     * @param httpServletRequest the http request in play
     * @param userName the user name in play for this request
     */
    void recordInteraction(HttpServletRequest httpServletRequest, final String userName)
    {
        requestCount.incrementAndGet();
        this.lastAccessTime.set(new Date());
        this.ipAddress.set(AccessLogIPAddressUtil.getRemoteAddr(httpServletRequest));
        this.userName.set(userName);
    }
}
