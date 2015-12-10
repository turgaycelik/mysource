/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity.integritycheck;

import com.atlassian.jira.appconsistency.integrity.check.Check;

import java.util.List;

public abstract class AbstractIntegrityCheck implements IntegrityCheck
{
    private final int id;
    private final String description;

    protected AbstractIntegrityCheck(final int id, final String description)
    {
        this.id = id;
        this.description = description;
    }

    public Long getId()
    {
        return Integer.valueOf(id).longValue();
    }

    public String getDescription()
    {
        return description;
    }

    public int compareTo(final IntegrityCheck ic)
    {
        if (ic == null)
        {
            return 1;
        }

        return (int) (getId().longValue() - ic.getId().longValue());
    }

    /**
     * Determines if AT LEAST ONE check is available
     */
    public boolean isAvailable()
    {
        for (final Check check : getChecks())
        {
            if (!check.isAvailable())
            {
                return false;
            }
        }

        return true;
    }

    public abstract List<? extends Check> getChecks();
}
