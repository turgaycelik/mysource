/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity.integritycheck;

import com.atlassian.jira.appconsistency.integrity.check.Check;

import java.util.List;

/**
 * unused
 */
public class IssueIntegrityCheck extends AbstractIntegrityCheck
{
    private List checks;

    public IssueIntegrityCheck(int id, List checks)
    {
        super(id, "Check Issue Integrity");
        this.checks = checks;
        for (Object check1 : this.checks)
        {
            Check check = (Check) check1;
            check.setIntegrityCheck(this);
        }
    }

    public List getChecks()
    {
        return checks;
    }
}
