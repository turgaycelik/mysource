/**
 * Copyright 2002-2007 Atlassian.
 */
package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.jira.util.dbc.Null;

import java.util.Date;

/**
 * A simple Name and Date pair for remembering who did something, when.
 *
 * @since v3.12
 */
public class AuditLog
{
    private final String who;
    private final Date when;

    public AuditLog(String who, Date when)
    {
        Null.not("who", who);
        Null.not("when", when);

        this.who = who;
        this.when = new Date(when.getTime());
    }

    public String getWho()
    {
        return who;
    }

    public Date getWhen()
    {
        return new Date(when.getTime());
    }
}
