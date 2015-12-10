/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.appconsistency.integrity.check.Check;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.junit.rules.InitMockitoMocks;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class TestIntegrityChecker
{

    @Rule
    public InitMockitoMocks initMockitoMocks = new InitMockitoMocks(this);

    @Mock
    private IntegrityCheckManager integrityCheckManager;

    @Mock
    private Check check;

    private IntegrityChecker integrityChecker;

    @Before
    public void setUp()
    {
        integrityChecker = new IntegrityChecker(integrityCheckManager);
    }


    @Test
    public void testMultiplePreviewAmendments() throws IntegrityException
    {
        //given:
        final ArrayList testList1 = new ArrayList();
        final ArrayList testList2 = new ArrayList();
        final Check check1 = mock(Check.class);
        when(check1.preview()).thenReturn(testList1);
        final Check check2 = mock(Check.class);
        when(check2.preview()).thenReturn(testList2);

        // when:
        final Map amendments = integrityChecker.preview(ImmutableList.of(check1, check2));

        // then:
        assertEquals(2, amendments.size());
        assertEquals(Collections.EMPTY_LIST, amendments.get(check1));
        assertEquals(Collections.EMPTY_LIST, amendments.get(check2));

        verify(check1).preview();
        verifyNoMoreInteractions(check1);
        verify(check2).preview();
        verifyNoMoreInteractions(check1);
    }

    @Test
    public void testMultipleCorrectAmendments() throws IntegrityException
    {
        // given:
        final ArrayList testList1 = new ArrayList();
        final ArrayList testList2 = new ArrayList();

        final Check check1 = mock(Check.class);
        when(check1.correct()).thenReturn(testList1);
        final Check check2 = mock(Check.class);
        when(check2.correct()).thenReturn(testList2);

        // when:
        final Map amendments = integrityChecker.correct(ImmutableList.of(check1, check2));

        // then:
        assertEquals(2, amendments.size());
        assertSame(testList1, amendments.get(check1));
        assertSame(testList2, amendments.get(check2));

        verify(check1).correct();
        verifyNoMoreInteractions(check1);
        verify(check2).correct();
        verifyNoMoreInteractions(check1);
    }

    @Test
    public void testPreviewAmendments() throws IntegrityException
    {
        // given:
        final ArrayList testList = new ArrayList();
        when(check.preview()).thenReturn(testList);

        // when:
        final List amendments = integrityChecker.preview(check);

        // then:
        assertSame(testList, amendments);
        verify(check).preview();
        verifyNoMoreInteractions(check);
        verifyZeroInteractions(integrityCheckManager);
    }

    @Test
    public void testCorrectAmendments() throws IntegrityException
    {
        // given:
        final ArrayList testList = new ArrayList();
        when(check.correct()).thenReturn(testList);

        // when:
        final List amendments = integrityChecker.correct(check);

        // then:
        assertSame(testList, amendments);

        verify(check).correct();
        verifyNoMoreInteractions(check);
        verifyZeroInteractions(integrityCheckManager);
    }
}
