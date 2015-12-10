/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuetypes;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestDeleteIssueType
{
    @Rule
    public final RuleChain mockito = MockitoMocksInContainer.forTest(this);

    private DeleteIssueType deleteIssueType;

    @Mock
    private IssueTypeManager issueTypeManager;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    @AvailableInContainer
    private ConstantsManager constantsManager;

    @AvailableInContainer (instantiateMe = true)
    private MockRedirectSanitiser redirectSanitiser;

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    private IssueType issueType;

    private final MockI18nHelper mockI18nHelper = new MockI18nHelper();

    @Before
    public void setUp() throws Exception
    {
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(mockI18nHelper);
        deleteIssueType = new DeleteIssueType(issueTypeManager, eventPublisher);
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        JiraTestUtil.resetRequestAndResponse();
    }


    @Test
    public void testFindingSurrogateTypesForwardsToIssueTypeManager() throws Exception
    {
        final String id = "10000";
        when(constantsManager.getIssueTypeObject(id)).thenReturn(issueType);

        deleteIssueType.setId(id);
        deleteIssueType.getAvailableIssueTypes();

        verify(issueTypeManager).getAvailableIssueTypes(issueType);
    }

    @Test
    public void testDeletingForwardsToIssueTypeManager() throws Exception
    {
        final MockHttpServletResponse mockHttpServletResponse = JiraTestUtil.setupExpectedRedirect("ViewIssueTypes.jspa");
        final String id = "100";
        final String newId = "101";

        deleteIssueType.setId(id);
        deleteIssueType.setNewId(newId);
        deleteIssueType.doExecute();

        verify(issueTypeManager).removeIssueType(id, newId);
        mockHttpServletResponse.verify();
    }

    @Test
    public void testValidationFailsOnNonExistentSurrogates() throws Exception
    {
        final String id = "10000";
        final String newId = "10012";
        when(constantsManager.getIssueTypeObject(id)).thenReturn(issueType);

        final GenericValue issueTypeGv = mock(GenericValue.class);
        when(issueTypeGv.getRelated("ChildIssue")).thenReturn(Collections.<GenericValue>singletonList(new MockGenericValue("mock")));

        when(constantsManager.getIssueType(id)).thenReturn(issueTypeGv);
        when(constantsManager.getIssueType(newId)).thenReturn(new MockGenericValue("somethingExisting"));

        deleteIssueType.setId(id);
        deleteIssueType.setNewId(newId);
        deleteIssueType.doValidation();

        assertEquals(singletonList("admin.errors.issuetypes.no.alternative"), deleteIssueType.getErrorMessages());
    }
}
