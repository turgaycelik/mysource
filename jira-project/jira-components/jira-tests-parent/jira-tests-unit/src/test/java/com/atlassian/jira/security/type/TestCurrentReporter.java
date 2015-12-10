/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security.type;

import java.util.Arrays;

import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelImpl;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockApplicationUser;

import org.apache.lucene.search.Query;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestCurrentReporter
{
    private MockApplicationUser britney = new MockApplicationUser("britneySpears");

    @Mock
    private PermissionSchemeManager mockPermissionSchemeManager;

    @Before
    public void setUp()
    {
        new MockComponentWorker()
                .addMock(PermissionSchemeManager.class, mockPermissionSchemeManager)
                .init();
    }

    @After
    public void tearDown()
    {
        britney = null;
        mockPermissionSchemeManager = null;
    }

    @Test
    public void testGetQueryWithNullProject() throws Exception
    {
        CurrentReporter currentReporter = new CurrentReporter(null);
        Query query = currentReporter.getQuery(britney.getDirectoryUser(), null, "developers");
        assertNull(query);
    }

    @Test
    public void testGetQueryWithProjectOnly() throws Exception
    {
        final MockGenericValue projectGV = new MockGenericValue("Project", FieldMap.build("id", 12L));
        MockProject mockProject = new MockProject(12, "ABC", "Blah", projectGV);
        CurrentReporter currentReporter = new CurrentReporter(null);
        final MockGenericValue schemeGV = new MockGenericValue("Scheme");

        when(mockPermissionSchemeManager.getSchemes(projectGV)).thenReturn(Arrays.<GenericValue>asList(schemeGV));
        when(mockPermissionSchemeManager.getEntities(schemeGV, "reporter", new Long(Permissions.BROWSE))).thenReturn(Arrays.<GenericValue>asList(new MockGenericValue("rubbish")));

        Query query = currentReporter.getQuery(britney.getDirectoryUser(), mockProject, "developers");
        assertEquals("(+projid:12 +issue_author:" + britney.getKey() + ')', query.toString());
    }

    @Test
    public void testGetQueryWithSecurityLevel() throws Exception
    {
        CurrentReporter currentReporter = new CurrentReporter(null);
        IssueSecurityLevel securityLevel = new IssueSecurityLevelImpl(10100L, "Blue", "", 20L);
        Query query = currentReporter.getQuery(britney.getDirectoryUser(), new MockProject(12, "ABC"), securityLevel, "developers");

        assertEquals("+(+issue_security_level:10100 +issue_author:" + britney.getKey() + ')', query.toString());
    }
}
