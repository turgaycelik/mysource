/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity.integritycheck;

import com.atlassian.jira.appconsistency.integrity.check.Check;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class IntegrityCheckImpl extends AbstractIntegrityCheck
{
    private List<Check> checks = new ArrayList<Check>();

    public IntegrityCheckImpl(final int id, final String description, final Check... checks)
    {
        super(id, description);
        this.checks = Collections.unmodifiableList(Arrays.asList(checks));
        for (final Check check : this.checks)
        {
            check.setIntegrityCheck(this);
        }
    }

    @Override
    public List<Check> getChecks()
    {
        return checks;
    }
}
