package com.atlassian.jira.appconsistency.integrity.integritycheck;

import com.atlassian.jira.appconsistency.integrity.check.Check;

public class FieldLayoutIntegrityCheck extends IntegrityCheckImpl
{
    public FieldLayoutIntegrityCheck(final int id, final String description, final Check... checks)
    {
        super(id, description, checks);
    }
}
