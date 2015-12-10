/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security.type;

import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelImpl;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.user.MockUser;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import junit.framework.TestCase;

public class TestGroupDropdown extends TestCase
{
    public void testGetQueryWithNullProject() throws Exception
    {
        GroupDropdown groupDropdown = new GroupDropdown(null);
        Query query = groupDropdown.getQuery(new MockUser("fred"), null, "developers");

        assertNull(query);
    }

    public void testGetQueryWithProjectOnly() throws Exception
    {
        GroupDropdown groupDropdown = new GroupDropdown(null);
        Query query = groupDropdown.getQuery(new MockUser("fred"), new MockProject(12), "developers");

        assertEquals("projid:12", query.toString());

        // expect a Term Query
        TermQuery actual = (TermQuery) query;
        assertEquals("projid", actual.getTerm().field());
        assertEquals("12", actual.getTerm().text());
    }

    public void testGetQueryWithSecurityLevel() throws Exception
    {
        GroupDropdown groupDropdown = new GroupDropdown(null);
        IssueSecurityLevel securityLevel = new IssueSecurityLevelImpl(10100L, "Blue", "", 20L);
        Query query = groupDropdown.getQuery(new MockUser("fred"), new MockProject(12, "ABC"), securityLevel, "developers");

        assertEquals("issue_security_level:10100", query.toString());

        // expect a Term Query
        TermQuery actual = (TermQuery) query;
        assertEquals("issue_security_level", actual.getTerm().field());
        assertEquals("10100", actual.getTerm().text());
    }
}
