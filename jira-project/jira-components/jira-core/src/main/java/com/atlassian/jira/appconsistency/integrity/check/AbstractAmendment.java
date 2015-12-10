/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity.check;

import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import org.apache.commons.lang.builder.ToStringBuilder;

public abstract class AbstractAmendment implements Amendment
{
    private int type;
    private String message;
    private String bugId;

    protected AbstractAmendment(int type, String bugId, String message)
    {
        if (!isValidType(type))
            throw new IllegalArgumentException("Unknown type '" + type + "'.");

        this.bugId = bugId;
        this.message = message;
        this.type = type;
    }

    public boolean isCorrection()
    {
        return getType() == Amendment.CORRECTION;
    }

    public boolean isWarning()
    {
        return getType() == Amendment.UNFIXABLE_ERROR;
    }

    public boolean isError()
    {
        return getType() == Amendment.ERROR;
    }

    protected int getType()
    {
        return type;
    }

    protected void setType(int type)
    {
        this.type = type;
    }

    protected boolean isValidType(int type)
    {
        return (type == Amendment.CORRECTION || type == Amendment.ERROR || type == Amendment.UNFIXABLE_ERROR);
    }

    public String getMessage()
    {
        return message;
    }

    protected void setMessage(String message)
    {
        this.message = message;
    }

    public String getBugId()
    {
        return bugId;
    }

    protected void setBugId(String bugId)
    {
        this.bugId = bugId;
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof AbstractAmendment))
            return false;

        final AbstractAmendment abstractAmendment = (AbstractAmendment) o;

        if (type != abstractAmendment.type)
            return false;
        if (bugId != null ? !bugId.equals(abstractAmendment.bugId) : abstractAmendment.bugId != null)
            return false;
        if (message != null ? !message.equals(abstractAmendment.message) : abstractAmendment.message != null)
            return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = type;
        result = 29 * result + (message != null ? message.hashCode() : 0);
        result = 29 * result + (bugId != null ? bugId.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
