package com.atlassian.jira.appconsistency.integrity.integritycheck;

import com.atlassian.jira.appconsistency.integrity.check.Check;

public class WorkflowStateIntegrityCheck extends IntegrityCheckImpl
{
    public WorkflowStateIntegrityCheck(final int id, final String description, final Check... checks)
    {
        super(id, description, checks);
    }
}
