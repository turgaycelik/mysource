package com.atlassian.jira.web.session.currentusers;

import com.atlassian.jira.util.http.HttpRequestType;

import java.util.Date;

/**
 * An implementation of JiraUserSession that snapshots the value of another {@link JiraUserSession}
 */
class SnapshotJiraUserUserSession implements JiraUserSession
{
    private final String sessionId;
    private final String userName;
    private final String ipAddress;
    private final long requestCount;
    private final Date creationTime;
    private final Date lastAccessTime;
    private final HttpRequestType type;
    private final String asessionId;


    SnapshotJiraUserUserSession(final JiraUserSession jiraUserSession)
    {
        this.sessionId = jiraUserSession.getId();
        this.asessionId = jiraUserSession.getASessionId();
        this.userName = jiraUserSession.getUserName();
        this.ipAddress = jiraUserSession.getIpAddress();
        this.requestCount = jiraUserSession.getRequestCount();
        this.creationTime = jiraUserSession.getCreationTime();
        this.lastAccessTime = jiraUserSession.getLastAccessTime();
        this.type = jiraUserSession.getType();
    }

    public String getId()
    {
        return sessionId;
    }

    public String getASessionId()
    {
        return asessionId;
    }

    public String getUserName()
    {
        return userName;
    }

    public String getIpAddress()
    {
        return ipAddress;
    }


    public long getRequestCount()
    {
        return requestCount;
    }

    public Date getCreationTime()
    {
        return creationTime;
    }

    public Date getLastAccessTime()
    {
        return lastAccessTime;
    }

    public HttpRequestType getType()
    {
        return type;
    }

    @Override
    public String toString()
    {
        return new StringBuilder(sessionId).append(" AS:").append(asessionId).append(" lat:").append(lastAccessTime == null ? null : lastAccessTime.getTime()).toString();
    }
}
