package com.atlassian.jira.issue.search.optimizers;

import java.util.Set;
import org.junit.Test;
import com.google.common.collect.ImmutableSet;

import com.atlassian.jira.jql.parser.DefaultJqlQueryParser;
import com.atlassian.jira.jql.parser.JqlParseException;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.query.Query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestDeterminedProjectsInQueryVisitor
{
    private final JqlQueryParser parser = new DefaultJqlQueryParser();

    @Test
    public void testLongQueryWithDeterminedProjects() throws JqlParseException
    {
        final Set<String> expectedDeterminedProjects = ImmutableSet.of("A", "B", "C", "D", "E", "F", "G");
        final Query q = parser.parseQuery("project = A AND (status != closed OR status = closed AND updated >= 2013-08-14) " +
                "OR project = B AND (status != closed OR status = closed AND updated >= 2013-10-18) OR " +
                "project = C AND (status != closed OR status = closed AND updated >= 2013-11-27) OR " +
                "project = D AND (status != closed OR status = closed AND updated >= 2013-02-20) OR " +
                "project = E AND (status != closed OR status = closed AND updated >= 2013-03-06) OR " +
                "project = F AND (status != closed OR status = closed AND updated >= 2013-03-27) OR " +
                "project = G AND (status != closed OR status = closed AND updated >= 2013-10-30)");
        final DeterminedProjectsInQueryVisitor determinedProjectsInQueryVisitor = new DeterminedProjectsInQueryVisitor();
        final boolean result = q.getWhereClause().accept(determinedProjectsInQueryVisitor);
        final Set<String> determinedProjects = determinedProjectsInQueryVisitor.getDeterminedProjects();

        assertTrue(result);
        assertEquals(determinedProjects.size(), expectedDeterminedProjects.size());
        assertTrue(determinedProjects.containsAll(expectedDeterminedProjects));
    }

    @Test
    public void testLongQueryWithDeterminedProjectsAndCamelCasedKeyWords() throws JqlParseException
    {
        final Set<String> expectedDeterminedProjects = ImmutableSet.of("UPPER", "lower", "camelCase", "test", "testx", "testxx", "with space");
        final Query q = parser.parseQuery("project = UPPER AND (status != closed OR status = closed AND updated >= 2013-08-14) " +
                "OR project = lower AND (status != closed OR status = closed AND updated >= 2013-10-18) OR " +
                "PROJECT = camelCase AND (status != closed OR status = closed AND updated >= 2013-11-27) OR " +
                "Project = test AND (status != closed OR status = closed AND updated >= 2013-02-20) OR " +
                "proJect = testx AND (status != closed OR status = closed AND updated >= 2013-03-06) OR " +
                "projeCT = testxx AND (status != closed OR status = closed AND updated >= 2013-03-27) OR " +
                "PROject = \"with space\" AND (status != closed OR status = closed AND updated >= 2013-10-30)");
        final DeterminedProjectsInQueryVisitor determinedProjectsInQueryVisitor = new DeterminedProjectsInQueryVisitor();
        final boolean result = q.getWhereClause().accept(determinedProjectsInQueryVisitor);
        final Set<String> determinedProjects = determinedProjectsInQueryVisitor.getDeterminedProjects();

        assertTrue(result);
        assertEquals(expectedDeterminedProjects.size(), determinedProjects.size());
        assertTrue(determinedProjects.containsAll(expectedDeterminedProjects));
    }

    @Test
    public void testSimpleQueryNoDeterminedProjects() throws JqlParseException
    {
        final Query q = parser.parseQuery("project = APS OR (status != closed OR status = closed AND updated >= 2013-08-14)");
        final DeterminedProjectsInQueryVisitor determinedProjectsInQueryVisitor = new DeterminedProjectsInQueryVisitor();

        assertFalse(q.getWhereClause().accept(determinedProjectsInQueryVisitor));
    }

    @Test
    public void testNotEqualsProjectClauseNoDeterminedProjects() throws JqlParseException
    {
        final Query q = parser.parseQuery("project != APS");
        final DeterminedProjectsInQueryVisitor determinedProjectsInQueryVisitor = new DeterminedProjectsInQueryVisitor();

        assertFalse(q.getWhereClause().accept(determinedProjectsInQueryVisitor));
    }

    @Test
    public void testNotEqualsAndEqualsProjectClauseOneDeterminedProject() throws JqlParseException
    {
        final Set<String> expectedDeterminedProjects = ImmutableSet.of("ABC");
        final Query q = parser.parseQuery("project != APS AND project = ABC");
        final DeterminedProjectsInQueryVisitor determinedProjectsInQueryVisitor = new DeterminedProjectsInQueryVisitor();

        assertTrue(q.getWhereClause().accept(determinedProjectsInQueryVisitor));
        final Set<String> determinedProjects = determinedProjectsInQueryVisitor.getDeterminedProjects();

        assertEquals(expectedDeterminedProjects.size(), determinedProjects.size());
        assertTrue(determinedProjects.containsAll(expectedDeterminedProjects));
    }

    @Test
    public void testNegatedProjectClauseNoDeterminedProjects() throws JqlParseException
    {
        final Query q = parser.parseQuery("NOT (project = APS)");
        final DeterminedProjectsInQueryVisitor determinedProjectsInQueryVisitor = new DeterminedProjectsInQueryVisitor();

        assertFalse(q.getWhereClause().accept(determinedProjectsInQueryVisitor));
    }

    @Test
    public void testNegatedProjectInClauseNoDeterminedProjects() throws JqlParseException
    {
        final Query q = parser.parseQuery("NOT (project in (APS, ABC) )");
        final DeterminedProjectsInQueryVisitor determinedProjectsInQueryVisitor = new DeterminedProjectsInQueryVisitor();

        assertFalse(q.getWhereClause().accept(determinedProjectsInQueryVisitor));
    }

    @Test
    public void testProjectNotInClauseNoDeterminedProjects() throws JqlParseException
    {
        final Query q = parser.parseQuery("project NOT IN (APS, ABC)");
        final DeterminedProjectsInQueryVisitor determinedProjectsInQueryVisitor = new DeterminedProjectsInQueryVisitor();

        assertFalse(q.getWhereClause().accept(determinedProjectsInQueryVisitor));
    }

    @Test
    public void testNegatedProjectClauseWithOtherClausesNoDeterminedProjects() throws JqlParseException
    {
        final Query q = parser.parseQuery("NOT (project = APS and status != open)");
        final DeterminedProjectsInQueryVisitor determinedProjectsInQueryVisitor = new DeterminedProjectsInQueryVisitor();

        assertFalse(q.getWhereClause().accept(determinedProjectsInQueryVisitor));
    }

    @Test
    public void testSimpleQueryCamelCaseOperandDeterminedProjects() throws JqlParseException
    {
        final Query q = parser.parseQuery("PROject = APS");
        final DeterminedProjectsInQueryVisitor determinedProjectsInQueryVisitor = new DeterminedProjectsInQueryVisitor();

        assertTrue(q.getWhereClause().accept(determinedProjectsInQueryVisitor));
    }

    @Test
    public void testSimpleQueryNoProjects() throws JqlParseException
    {
        final Query q = parser.parseQuery("status != closed OR status = closed AND updated >= 2013-08-14");
        final DeterminedProjectsInQueryVisitor determinedProjectsInQueryVisitor = new DeterminedProjectsInQueryVisitor();

        assertFalse(q.getWhereClause().accept(determinedProjectsInQueryVisitor));
    }

    @Test
    public void testProjectInClause() throws JqlParseException
    {
        final Query q = parser.parseQuery("project in (aaa, bbb)");
        final DeterminedProjectsInQueryVisitor determinedProjectsInQueryVisitor = new DeterminedProjectsInQueryVisitor();

        assertTrue(q.getWhereClause().accept(determinedProjectsInQueryVisitor));
        final Set<String> determinedProjects = determinedProjectsInQueryVisitor.getDeterminedProjects();
        assertEquals(2, determinedProjects.size());
        assertTrue(determinedProjects.contains("aaa"));
        assertTrue(determinedProjects.contains("bbb"));
    }

    @Test
    public void testProjectInSubClause() throws JqlParseException
    {
        final Query q = parser.parseQuery("project in (aaa, bbb) and status != closed");
        final DeterminedProjectsInQueryVisitor determinedProjectsInQueryVisitor = new DeterminedProjectsInQueryVisitor();

        assertTrue(q.getWhereClause().accept(determinedProjectsInQueryVisitor));
        final Set<String> determinedProjects = determinedProjectsInQueryVisitor.getDeterminedProjects();
        assertEquals(2, determinedProjects.size());
        assertTrue(determinedProjects.contains("aaa"));
        assertTrue(determinedProjects.contains("bbb"));
    }

    @Test
    public void testNegatedProjectInSubClause() throws JqlParseException
    {
        final Query q = parser.parseQuery("NOT (project in (aaa, bbb) and status != closed)");
        final DeterminedProjectsInQueryVisitor determinedProjectsInQueryVisitor = new DeterminedProjectsInQueryVisitor();

        assertFalse(q.getWhereClause().accept(determinedProjectsInQueryVisitor));
    }
}