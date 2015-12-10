/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity.integritycheck;

import com.atlassian.jira.appconsistency.integrity.check.Check;

import com.google.common.collect.ImmutableList;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TestIntegrityCheckImpl
{
    private static final String DESCRIPTION = "Check Desc";
    private static final int id = 123;

    @Test
    public void shouldSetIntegrityChecksOnAllGivenChecks()
    {
        Check[] checks = { mock(Check.class), Mockito.mock(Check.class), Mockito.mock(Check.class) };

        IntegrityCheckImpl integrityCheck = new IntegrityCheckImpl(id, DESCRIPTION, checks);

        for (Check check : checks)
        {
            verify(check).setIntegrityCheck(integrityCheck);
        }

        assertEquals(ImmutableList.copyOf(checks), integrityCheck.getChecks());
    }

}
