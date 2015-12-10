package com.atlassian.jira.appconsistency.integrity.integritycheck;

import com.atlassian.jira.appconsistency.integrity.check.Check;

import java.util.List;

public class FilterSubscriptionIntegrityCheck extends IntegrityCheckImpl
{
    public FilterSubscriptionIntegrityCheck(final int id, final String description, final Check... checks)
    {
        super(id, description, checks);
    }

    public FilterSubscriptionIntegrityCheck(final int id, final String description, final List<Check> checks)
    {
        super(id, description, checks.toArray(new Check[checks.size()]));
    }
}
