/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security.type;

import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelImpl;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestSingleUser
{
    @Test
    public void testGetQueryWithNullProject() throws Exception
    {
        final ApplicationUser fred = new MockApplicationUser("Fred");
        final SingleUser singleUser = new SingleUser(null, null);
        final Query query = singleUser.getQuery(fred.getDirectoryUser(), null, "developers");
        assertNull(query);
    }

    @Test
    public void testGetQueryWithProjectOnly() throws Exception
    {
        final ApplicationUser fred = new MockApplicationUser("Fred");
        final SingleUser singleUser = new SingleUser(null, null);
        final Query query = singleUser.getQuery(fred.getDirectoryUser(), new MockProject(12), "developers");

        assertEquals("projid:12", query.toString());

        // expect a Term Query
        final TermQuery actual = (TermQuery) query;
        assertEquals("projid", actual.getTerm().field());
        assertEquals("12", actual.getTerm().text());
    }

    @Test
    public void testGetQueryWithSecurityLevel() throws Exception
    {
        final ApplicationUser fred = new MockApplicationUser("Fred");
        final SingleUser singleUser = new SingleUser(null, null);
        final IssueSecurityLevel securityLevel = new IssueSecurityLevelImpl(10100L, "Blue", "", 20L);
        final Query query = singleUser.getQuery(fred.getDirectoryUser(), new MockProject(12, "ABC"), securityLevel, "developers");

        assertEquals("issue_security_level:10100", query.toString());

        // expect a Term Query
        final TermQuery actual = (TermQuery) query;
        assertEquals("issue_security_level", actual.getTerm().field());
        assertEquals("10100", actual.getTerm().text());
    }
}
