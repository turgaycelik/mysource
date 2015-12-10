package com.atlassian.jira.config;

import java.util.Date;

/**
 * Reindex message bean.
 *
 * @since v5.2
 */
public class ReindexMessage
{
    private final String userName;
    private final Date time;
    private final String i18nTaskKey;

    public ReindexMessage(String userName, Date time, String i18nTaskKey)
    {
        this.userName = userName;
        this.time = time;
        this.i18nTaskKey = i18nTaskKey;
    }

    public String getUserName()
    {
        return userName;
    }

    public Date getTime()
    {
        return time;
    }

    public String getI18nTaskKey()
    {
        return i18nTaskKey;
    }

}
