package com.atlassian.jira.web.session.currentusers;

import com.atlassian.jira.util.http.HttpRequestType;

import java.util.Date;

/**
 * This interface represent the data captured about a users session with JIRA.
 *
 * @since v4.0
 */
public interface JiraUserSession
{
    /**
     * @return the raw id of the session
     */
    String getId();

    /**
     * @return the ASESSIONID of the session
     */
    String getASessionId();

    /**
     * @return The name of the user who owns this session.  NOTE : This can be null.
     */
    String getUserName();

    /**
     * @return The IP address this session in on.
     */
    String getIpAddress();

    /**
     * @return the number of requests that this user has made in this session
     */
    long getRequestCount();

    /**
     * @return The session creation time or null if its not known
     */
    Date getCreationTime();

    /**
     * @return the last time the session was active  or null if its not known
     */
    Date getLastAccessTime();

    /**
     * @return the type of session
     */
    HttpRequestType getType();

}
