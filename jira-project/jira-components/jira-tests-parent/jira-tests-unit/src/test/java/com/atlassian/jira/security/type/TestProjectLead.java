/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security.type;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelImpl;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.user.MockUser;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import junit.framework.TestCase;

public class TestProjectLead extends TestCase
{

    @Override
    protected void setUp() throws Exception
    {
        MockComponentWorker componentWorker = new MockComponentWorker();
        ComponentAccessor.initialiseWorker(componentWorker);
    }

    public void testGetQueryWithNullProject() throws Exception
    {
        ProjectLead projectLead = new ProjectLead(null);
        Query query = projectLead.getQuery(new MockUser("fred"), null, "developers");

        assertNull(query);
    }

    public void testGetQueryWithProjectOnly() throws Exception
    {
        ProjectLead projectLead = new ProjectLead(null);
        Query query = projectLead.getQuery(new MockUser("fred"), new MockProject(12), "developers");

        assertEquals("projid:12", query.toString());

        // expect a Term Query
        TermQuery actual = (TermQuery) query;
        assertEquals("projid", actual.getTerm().field());
        assertEquals("12", actual.getTerm().text());
    }

    public void testGetQueryWithSecurityLevelSearcherNotProjectLead() throws Exception
    {
        ProjectLead projectLead = new ProjectLead(null);
        final MockProject project = new MockProject(12, "ABC");
        project.setLead(new MockUser("wilma"));
        final MockUser userFred = new MockUser("fred");
        IssueSecurityLevel securityLevel = new IssueSecurityLevelImpl(10100L, "Blue", "", 20L);
        Query query = projectLead.getQuery(userFred, project, securityLevel, "developers");

        assertNull(query);
    }

    public void testGetQueryWithSecurityLevelSearcherIsProjectLead() throws Exception
    {
        ProjectLead projectLead = new ProjectLead(null);
        final MockProject project = new MockProject(12, "ABC");
        final MockUser userFred = new MockUser("fred");
        project.setLead(userFred);
        IssueSecurityLevel securityLevel = new IssueSecurityLevelImpl(10100L, "Blue", "", 20L);
        Query query = projectLead.getQuery(userFred, project, securityLevel, "developers");

        assertEquals("+projid:12 +issue_security_level:10100", query.toString());
    }
}
