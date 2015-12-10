package com.atlassian.jira.issue.search.optimizers;

import com.atlassian.jira.jql.parser.DefaultJqlQueryParser;
import com.atlassian.jira.jql.parser.JqlParseException;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.jql.util.JqlStringSupport;
import com.atlassian.jira.jql.util.JqlStringSupportImpl;
import com.atlassian.query.Query;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestUnreleasedVersionsFunctionOptimizer
{

    private final JqlQueryParser parser = new DefaultJqlQueryParser();

    private final JqlStringSupport jqlStringSupport = new JqlStringSupportImpl(parser);


    @Test
    public void testSimpleQueryShouldOptimize() throws JqlParseException
    {
        final Query query = parser.parseQuery("project = ABAE and (fixVersion is EMPTY or fixVersion in unreleasedVersions())");

        final UnreleasedVersionsFunctionOptimizer optimizer = new UnreleasedVersionsFunctionOptimizer();
        final Query optimized = optimizer.createOptimizedQuery(query);

        assertEquals("project = ABAE AND (fixVersion is EMPTY OR fixVersion in unreleasedVersions(ABAE))", jqlStringSupport.generateJqlString(optimized));
    }

    @Test
    public void testSimpleQueryLowerCaseShouldOptimize() throws JqlParseException
    {
        final Query query = parser.parseQuery("project = ABAE and (fixVersion is EMPTY or fixVersion in unreleasedversions())");

        final UnreleasedVersionsFunctionOptimizer optimizer = new UnreleasedVersionsFunctionOptimizer();
        final Query optimized = optimizer.createOptimizedQuery(query);

        assertEquals("project = ABAE AND (fixVersion is EMPTY OR fixVersion in unreleasedVersions(ABAE))", jqlStringSupport.generateJqlString(optimized));
    }

    @Test
    public void testSimpleQueryWithSortShouldOptimize() throws JqlParseException
    {
        final Query query = parser.parseQuery("project = ABAE and (fixVersion is EMPTY or fixVersion in unreleasedVersions()) ORDER BY Rank ASC");

        final UnreleasedVersionsFunctionOptimizer optimizer = new UnreleasedVersionsFunctionOptimizer();
        final Query optimized = optimizer.createOptimizedQuery(query);

        assertEquals("project = ABAE AND (fixVersion is EMPTY OR fixVersion in unreleasedVersions(ABAE)) ORDER BY Rank ASC", jqlStringSupport.generateJqlString(optimized));
    }

    @Test
    public void testSimpleQueryWithUnreleasedVersionsSpecifiedShouldNotOptimize() throws JqlParseException
    {
        final String queryString = "project = ABAE AND (fixVersion is EMPTY OR fixVersion in unreleasedVersions(ABAE, ACD)) ORDER BY Rank ASC";
        final Query query = parser.parseQuery(queryString);

        final UnreleasedVersionsFunctionOptimizer optimizer = new UnreleasedVersionsFunctionOptimizer();
        final Query optimized = optimizer.createOptimizedQuery(query);

        assertEquals(queryString, jqlStringSupport.generateJqlString(optimized));
    }

    @Test
    public void testQueryWithMoreProjects() throws JqlParseException
    {
        final Query query = parser.parseQuery("(project = ABAE OR project = abbreviatus OR project = abrae OR " +
                "project = abyssinica OR project = acrodonta OR project = adamsi) AND " +
                "(fixVersion is EMPTY OR fixVersion in unreleasedVersions())");

        final UnreleasedVersionsFunctionOptimizer optimizer = new UnreleasedVersionsFunctionOptimizer();
        final Query optimized = optimizer.createOptimizedQuery(query);

        assertEquals("(project = ABAE OR project = abbreviatus OR project = abrae OR project = abyssinica OR " +
                        "project = acrodonta OR project = adamsi) AND (fixVersion is EMPTY OR fixVersion in " +
                        "unreleasedVersions(ABAE, abbreviatus, abrae, abyssinica, acrodonta, adamsi))",
                jqlStringSupport.generateJqlString(optimized));
    }
}