/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.scheme.issuesecurity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericValue;

import webwork.action.Action;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.SecurityTypeManager;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.web.action.RedirectSanitiser;
import com.atlassian.jira.web.action.admin.issuesecurity.DeleteIssueSecurityLevel;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mockobjects.servlet.MockHttpServletResponse;

public class TestDeleteIssueSecurityLevel
{

    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    @AvailableInContainer
    private final OfBizDelegator ofBizDelegator = new MockOfBizDelegator();

    @Mock
    private FieldVisibilityManager fieldVisibilityManager;
    
    @Mock
    @AvailableInContainer
    private SecurityTypeManager securityTypeManager;
    
    @Mock
    @AvailableInContainer
    private IssueSecurityLevelManager issueSecurityLevelManager;
    
    @Mock
    private IssueSecuritySchemeManager issueSecuritySchemeManager;
    
    @Mock
    @AvailableInContainer
    private IssueIndexManager issueIndexManager;
    
    @AvailableInContainer
    private final RedirectSanitiser redirectSanitiser = new MockRedirectSanitiser();

    private DeleteIssueSecurityLevelFixture testedObject;

    private GenericValue issueScheme;
    
    // security levels
    private GenericValue securityLevelA;
    private GenericValue securityLevelB;
    private GenericValue securityLevelC;
    private GenericValue securityLevelD;
    
    private GenericValue issueA;
    private GenericValue issueB;

    // deprecation is suppressed - it tests existing implementation, those depreciations should disappeared, when original implementation
    // stops using deprecated methods
    @SuppressWarnings("deprecation")
    @Before
    public void setUp() throws Exception
    {
        when(fieldVisibilityManager.isFieldHidden(Mockito.anyString(), Mockito.<Issue> any())).thenReturn(Boolean.FALSE);
        when(fieldVisibilityManager.isFieldVisible(Mockito.anyString(), Mockito.<Issue> any())).thenReturn(Boolean.TRUE);

        issueScheme = UtilsForTests.getTestEntity("Scheme", ImmutableMap.of("id", 1L, " name", "IScheme", "description", "Test Desc"));

        //create security levels
        securityLevelA = UtilsForTests.getTestEntity("SchemeIssueSecurityLevels", ImmutableMap.of("id", new Long(1), "scheme", issueScheme.getLong("id"), "name", "Test Level", "description", "Test Desc"));
        securityLevelB = UtilsForTests.getTestEntity("SchemeIssueSecurityLevels", ImmutableMap.of("id", new Long(2), "scheme", issueScheme.getLong("id"), "name", "Test Level2", "description", "Test Desc2"));
        securityLevelC = UtilsForTests.getTestEntity("SchemeIssueSecurityLevels", ImmutableMap.of("id", new Long(3), "scheme", issueScheme.getLong("id"), "name", "Test Level3", "description", "Test Desc3"));
        securityLevelD = UtilsForTests.getTestEntity("SchemeIssueSecurityLevels", ImmutableMap.of("id", new Long(4), "scheme", issueScheme.getLong("id"), "name", "Test Level4", "description", "Test Desc4"));
        
        // returns all security levels, which were assigned to mock issue scheme
        when(issueSecurityLevelManager.getSchemeIssueSecurityLevels(issueScheme.getLong("id"))).thenReturn(
                Arrays.asList(securityLevelA, securityLevelB, securityLevelC, securityLevelD));

        //Set D as the default
        issueScheme.set("defaultlevel", securityLevelD.getLong("id"));
        issueScheme.store();

        //create issues with security level
        issueA = UtilsForTests.getTestEntity("Issue", ImmutableMap.of("key", "ABC-1", "project", new Long(2), "reporter", "bob", "security", securityLevelA.getLong("id")));
        issueB = UtilsForTests.getTestEntity("Issue", ImmutableMap.of("key", "ABC-2", "project", new Long(2), "reporter", "bob", "security", securityLevelA.getLong("id")));

        testedObject = new DeleteIssueSecurityLevelFixture(issueSecuritySchemeManager, securityTypeManager, issueSecurityLevelManager);
        
        // associate with scheme and set the current level
        testedObject.setSchemeId(issueScheme.getLong("id"));
        testedObject.setLevelId(securityLevelA.getLong("id"));
    }

    @Test
    public void testAffectedIssues() throws Exception
    {
        assertEquals(Arrays.asList(issueA, issueB), testedObject.getAffectedIssues());
    }

    @Test
    public void testOtherLevels() throws Exception
    {
        @SuppressWarnings("unchecked")
        final Map<Long, String> otherLevels = testedObject.getOtherLevels();

        //The current level will not be returned but the other 3 will
        assertEquals(
                ImmutableSet.<Long> of(securityLevelB.getLong("id"), securityLevelC.getLong("id"), securityLevelD.getLong("id")),
                otherLevels.keySet());
    }

    @Test
    public void testExecuteNewLevel() throws Exception
    {
        final MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect(testedObject.getRedirectURL());

        testedObject.setSwapLevel(securityLevelB.getLong("id"));

        //check the security levels on the issues are set
        assertEquals(issueA.getLong("security"), securityLevelA.getLong("id"));
        assertEquals(issueB.getLong("security"), securityLevelA.getLong("id"));

        //Check that the statistics are updated
        final GenericValue expectedIssueA = ofBizDelegator.findByPrimaryKey("Issue", ImmutableMap.of("id", issueA.getLong("id")));
        final GenericValue expectedIssueB = ofBizDelegator.findByPrimaryKey("Issue", ImmutableMap.of("id", issueB.getLong("id")));
        expectedIssueA.set("security", securityLevelB.getLong("id"));
        expectedIssueB.set("security", securityLevelB.getLong("id"));

        final String result = testedObject.doExecute();

        //there should be no errors
        assertEquals(Collections.emptyMap(), testedObject.getErrors());

        assertEquals(Action.NONE, result);

        //the scheme level should be gone and there should be 3 left (2, 3, 4)
        Mockito.verify(issueSecurityLevelManager).deleteSecurityLevel(securityLevelA.getLong("id"));

        //The security levels on the issues should be changed
        issueA = ofBizDelegator.findById("Issue", issueA.getLong("id"));
        issueB = ofBizDelegator.findById("Issue", issueB.getLong("id"));
        assertEquals(issueA.getLong("security"), securityLevelB.getLong("id"));
        assertEquals(issueB.getLong("security"), securityLevelB.getLong("id"));
        response.verify();
    }

    @Test
    public void testExecuteNoLevel() throws Exception
    {
        final MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect(testedObject.getRedirectURL());

        testedObject.setSwapLevel(null);

        //check the security levels on the issues are set
        assertEquals(issueA.getLong("security"), securityLevelA.getLong("id"));
        assertEquals(issueB.getLong("security"), securityLevelA.getLong("id"));

        final String result = testedObject.doExecute();

        //there should be no errors
        assertEquals(Collections.emptyMap(), testedObject.getErrors());

        assertEquals(Action.NONE, result);

        //the scheme level should be gone and there should be 3 left
        Mockito.verify(issueSecurityLevelManager).deleteSecurityLevel(securityLevelA.getLong("id"));

        //The security levels on the issues should be changed
        issueA = ofBizDelegator.findById("Issue", issueA.getLong("id"));
        issueB = ofBizDelegator.findById("Issue", issueB.getLong("id"));
        assertNull(issueA.getLong("security"));
        assertNull(issueB.getLong("security"));
        response.verify();
    }

    @Test
    public void testExecuteNoAffectedIssues() throws Exception
    {
        testedObject.setLevelId(securityLevelC.getLong("id"));

        assertEquals(Collections.emptyList(), testedObject.getAffectedIssues());

        final MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect(testedObject.getRedirectURL());

        testedObject.setSwapLevel(securityLevelB.getLong("id"));

        //check the security levels on the issues are set
        assertEquals(issueA.getLong("security"), securityLevelA.getLong("id"));
        assertEquals(issueB.getLong("security"), securityLevelA.getLong("id"));

        final String result = testedObject.doExecute();

        //there should be no errors
        assertEquals(Collections.emptyMap(), testedObject.getErrors());

        assertEquals(Action.NONE, result);

        //the scheme level should be gone and there should be 3 left
        Mockito.verify(issueSecurityLevelManager).deleteSecurityLevel(securityLevelC.getLong("id"));

        assertEquals(issueA.getLong("security"), securityLevelA.getLong("id"));
        assertEquals(issueB.getLong("security"), securityLevelA.getLong("id"));
        response.verify();
    }

    @Test
    public void testDeleteDefaultLevel() throws Exception
    {
        final MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect(testedObject.getRedirectURL());

        testedObject.setLevelId(securityLevelD.getLong("id"));

        final String result = testedObject.execute();
        assertEquals(Action.NONE, result);

        response.verify();

        assertNull(issueScheme.getLong("defaultvalue"));
    }
}

class DeleteIssueSecurityLevelFixture extends DeleteIssueSecurityLevel
{

    private static final long serialVersionUID = 1L;

    public DeleteIssueSecurityLevelFixture(final IssueSecuritySchemeManager issueSecuritySchemeManager, final SecurityTypeManager issueSecurityTypeManager, final IssueSecurityLevelManager issueSecurityLevelManager)
    {
        super(issueSecuritySchemeManager, issueSecurityTypeManager, issueSecurityLevelManager);
    }

    @Override
    public String doExecute() throws Exception
    {
        return super.doExecute();
    }
}
