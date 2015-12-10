/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.scheme;

import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.AnswerWith;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.web.action.admin.notification.DeleteScheme;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import webwork.action.Action;

import java.util.Collections;

import static java.util.Collections.singletonList;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestDeleteNotificationSchemeAction
{

    private static final long SCHEME_ID = 10123L;
    private static final long DEFAULT_SCHEME_ID = 1L;

    @Rule
    public final RuleChain mockContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    @AvailableInContainer
    private NotificationSchemeManager notificationSchemeManager;

    @AvailableInContainer
    @SuppressWarnings ("unused")
    private final MockRedirectSanitiser mockRedirectSanitiser = new MockRedirectSanitiser();

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    private I18nHelper i18nHelper;

    @Mock
    private Scheme defaultScheme;

    @Mock
    private Scheme schemeToDelete;

    private DeleteScheme deleteScheme;

    @Before
    public void setUp() throws Exception
    {
        when(notificationSchemeManager.getDefaultSchemeObject()).thenReturn(defaultScheme);
        when(notificationSchemeManager.getSchemeObject(SCHEME_ID)).thenReturn(schemeToDelete);

        when(defaultScheme.getId()).thenReturn(1L);

        when(schemeToDelete.getId()).thenReturn(SCHEME_ID);

        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);
        when(i18nHelper.getText(anyString())).thenAnswer(AnswerWith.firstParameter());

        deleteScheme = new DeleteScheme(notificationSchemeManager);
    }

    @After
    public void tearDown() throws Exception
    {
        JiraTestUtil.resetRequestAndResponse();
    }

    @Test
    public void testDeletePermissionScheme() throws Exception
    {
        //use a mock servlet response
        final MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewNotificationSchemes.jspa");

        //delete the scheme
        deleteScheme.setSchemeId(SCHEME_ID);
        deleteScheme.setConfirmed(true);

        assertEquals(Action.NONE, deleteScheme.execute());
        assertEquals(Collections.<String>emptyList(), deleteScheme.getErrorMessages());

        verify(notificationSchemeManager).deleteScheme(SCHEME_ID);
        response.verify();
    }

    @Test
    public void deletingDefaultSchemeIsImpossible() throws Exception
    {
        deleteScheme.setSchemeId(DEFAULT_SCHEME_ID);
        deleteScheme.setConfirmed(true);

        assertEquals(Action.INPUT, deleteScheme.execute());
        assertEquals(singletonList("admin.errors.deletescheme.cannot.delete.default"), deleteScheme.getErrorMessages());

        verify(notificationSchemeManager, never()).deleteScheme(DEFAULT_SCHEME_ID);
    }

}
