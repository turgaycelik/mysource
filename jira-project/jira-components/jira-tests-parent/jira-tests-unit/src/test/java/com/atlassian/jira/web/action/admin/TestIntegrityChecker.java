/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import java.util.List;
import java.util.Map;

import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.appconsistency.integrity.IntegrityCheckManager;
import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.appconsistency.integrity.check.Check;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.appconsistency.integrity.integritycheck.IntegrityCheck;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.RedirectSanitiser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mockobjects.servlet.MockHttpServletResponse;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import webwork.action.ActionContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestIntegrityChecker
{
    @Mock
    private IntegrityCheckManager integrityCheckManager;

    @Mock
    private com.atlassian.jira.appconsistency.integrity.IntegrityChecker checkerUtil;

    @AvailableInContainer (instantiateMe = true)
    private MockI18nHelper i18nHelper;

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext jiraAuthenticationContext;

    private RedirectSanitiser redirectSanitiser = new MockRedirectSanitiser();

    private IntegrityChecker integrityChecker;

    @Rule
    public MockitoContainer mockitoContainer = new MockitoContainer(this);


    @Before
    public void before()
    {
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);
        integrityChecker = new IntegrityChecker(integrityCheckManager, checkerUtil);
        mockitoContainer.getMockComponentContainer().addMock(RedirectSanitiser.class, redirectSanitiser);
    }

    @Test
    public void shouldShowAnErrorWhenNoActionIsSet()
    {
        integrityChecker.setCheck(null);
        integrityChecker.setFix(null);
        integrityChecker.setBack(null);

        integrityChecker.doValidation();

        assertThat(integrityChecker.getErrorMessages(), Matchers.contains("admin.integritychecker.error.no.function"));
    }

    @Test
    public void shouldShowAnErrorWhenNoValidChecksWereSelected()
    {
        integrityChecker.setCheck("true");
        ActionContext.setParameters(ImmutableMap.of());
        integrityChecker.doValidation();

        assertThat(integrityChecker.getErrorMessages(), Matchers.contains("admin.integritychecker.error.one.check"));
    }

    @Test
    public void shouldRunCheckOnIdsWhenCheckOptionIsSelected() throws Exception
    {
        integrityChecker.setCheck("true");
        ActionContext.setParameters(ImmutableMap.<String, String[]>of(
                integrityChecker.getCheckPrefix() + "_simple", new String[] { "123" },
                integrityChecker.getCheckPrefix() + "_second", new String[] { "213" }
        ));

        assertEquals("preview", integrityChecker.doExecute());

        verify(checkerUtil).previewWithIds(ImmutableList.of(123L, 213L));
    }

    @Test
    public void shouldRunCorrectionOnIdsWhenFixOptionIsSelected() throws Exception
    {
        integrityChecker.setFix("true");
        ActionContext.setParameters(ImmutableMap.<String, String[]>of(
                integrityChecker.getCheckPrefix() + "_simple", new String[] { "123" },
                integrityChecker.getCheckPrefix() + "_second", new String[] { "213" }
        ));

        assertEquals("correct", integrityChecker.doExecute());

        verify(checkerUtil).correctWithIds(ImmutableList.of(123L, 213L));
    }

    @Test
    public void shouldRedirectToDefaultWhenGoBackIsSelected() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("IntegrityChecker!default.jspa");
        integrityChecker.setBack("true");

        integrityChecker.doExecute();

        response.verify();
    }

    @Test
    public void shouldDisplayInputWhenNothingIsSelected() throws Exception
    {
        assertEquals("input", integrityChecker.doExecute());
    }

    @Test (expected = IllegalStateException.class)
    public void shouldThrowIllegalStateWhenCheckResultHasNotBeenPopulatedYet() throws Exception
    {
        integrityChecker.getResults();
    }

    @Test
    public void getTotalResultsShouldGatherCorrectionsFromAllAmmendments() throws Exception
    {
        final Amendment amendment = mock(Amendment.class);
        prepareIntegrityCheck(amendment);

        integrityChecker.doExecute();

        when(amendment.isCorrection()).thenReturn(false, true, false, true);
        assertEquals(2, integrityChecker.getTotalResults());

        reset(amendment);
        when(amendment.isCorrection()).thenReturn(true);
        assertEquals(4, integrityChecker.getTotalResults());

        reset(amendment);
        when(amendment.isCorrection()).thenReturn(false);
        assertEquals(0, integrityChecker.getTotalResults());
    }

    @Test
    public void hasCorrectableResultShouldCheckWhetherAmendmentsAreErrors() throws Exception
    {
        final Amendment amendment = mock(Amendment.class);

        final IntegrityCheck integrityCheck = prepareIntegrityCheck(amendment);

        integrityChecker.doExecute();

        when(amendment.isError()).thenReturn(false, false, true, false);
        assertTrue(integrityChecker.isHasCorrectableResults(integrityCheck));

        reset(amendment);
        when(amendment.isError()).thenReturn(true);
        assertTrue(integrityChecker.isHasCorrectableResults(integrityCheck));

        reset(amendment);
        when(amendment.isError()).thenReturn(false);
        assertFalse(integrityChecker.isHasCorrectableResults(integrityCheck));
    }

    @Test
    public void hasWarningShouldCheckWarningsAcrossAllAmendments() throws Exception
    {
        final Amendment amendment = mock(Amendment.class);
        final IntegrityCheck integrityCheck = prepareIntegrityCheck(amendment);

        integrityChecker.doExecute();
        Check oneOfTheChecks = integrityCheck.getChecks().get(2);

        reset(amendment);
        when(amendment.isWarning()).thenReturn(false);
        assertFalse(integrityChecker.isHasWarningResults(oneOfTheChecks));

        reset(amendment);
        when(amendment.isWarning()).thenReturn(false, true, false);
        assertTrue(integrityChecker.isHasWarningResults(oneOfTheChecks));

        reset(amendment);
        when(amendment.isWarning()).thenReturn(true);
        assertTrue(integrityChecker.isHasWarningResults(oneOfTheChecks));
    }

    @Test
    public void testIsCheckAvailable() throws Exception
    {
        Check check = mock(Check.class);
        when(check.isAvailable()).thenReturn(false);
        assertFalse(integrityChecker.isCheckAvailable(check));

        when(check.isAvailable()).thenReturn(true);
        integrityChecker.setBack("true");
        assertTrue(integrityChecker.isCheckAvailable(check));

        integrityChecker.setBack(null);
        integrityChecker.setFix("true");
        assertFalse(integrityChecker.isCheckAvailable(check));

    }

    @Test
    public void testIsCheckAvailableWhenCheckModeIsEnabled() throws Exception
    {
        final Amendment amendment = mock(Amendment.class);
        final IntegrityCheck integrityCheck = prepareIntegrityCheck(amendment);

        integrityChecker.doExecute();

        Check check = integrityCheck.getChecks().get(2);

        when(check.isAvailable()).thenReturn(true);
        when(amendment.isError()).thenReturn(false, true, false);
        assertTrue(integrityChecker.isCheckAvailable(check));

        when(amendment.isError()).thenReturn(false);
        assertFalse(integrityChecker.isCheckAvailable(check));
    }

    private IntegrityCheck prepareIntegrityCheck(final Amendment usedAmendment) throws IntegrityException
    {
        integrityChecker.setCheck("true");
        ActionContext.setParameters(ImmutableMap.<String, String[]>of(
                integrityChecker.getCheckPrefix() + "_simple", new String[] { "123" }
        ));

        final Check check0 = mock(Check.class);
        final Check check1 = mock(Check.class);
        final Check check2 = mock(Check.class);
        Map<Check, List<Amendment>> result = ImmutableMap.<Check, List<Amendment>>of(
                check0, ImmutableList.<Amendment>of(),
                check1, ImmutableList.<Amendment>of(usedAmendment),
                check2, ImmutableList.<Amendment>of(usedAmendment, usedAmendment, usedAmendment)
        );
        when(checkerUtil.previewWithIds(ImmutableList.of(123L))).thenReturn(result);

        final IntegrityCheck integrityCheck = mock(IntegrityCheck.class);
        final List of = ImmutableList.of(check0, check1, check2);
        when(integrityCheck.getChecks()).thenReturn(of);
        return integrityCheck;
    }


}
