package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.search.MockJqlSearchRequest;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests that the {@link VersionStatisticsMapper} correctly modifies {@link SearchRequest}s to contain the
 * additional clauses required to link to the specific values given.
 *
 * Note: we are testing the modified clauses using Clause#toString() which may not be ideal.
 *
 * @since v4.0
 */
public class TestAbstractVersionStatisticsMapper extends MockControllerTestCase
{
    private static final String CLAUSE_NAME = "myVersion";
    private static final String DOCUMENT_CONSTANT = "myVersion";
    private static final long PROJECT_ID = 13L;
    private static final String PROJECT_KEY = "HSP";
    private static final long VERSION_ID = 555L;
    private static final String VERSION_NAME = "New Version 555";

    @Test
    public void testGetUrlSuffixForSomeVersion() throws Exception
    {
        VersionStatisticsMapper mapper = new MyVersionStatisticsMapper();

        final TerminalClauseImpl projectClause = new TerminalClauseImpl(IssueFieldConstants.PROJECT, Operator.EQUALS, PROJECT_ID);
        final TerminalClauseImpl issueTypeClause = new TerminalClauseImpl(IssueFieldConstants.ISSUE_TYPE, Operator.EQUALS, "Bug");
        final AndClause totalExistingClauses = new AndClause(projectClause, issueTypeClause);

        final TerminalClauseImpl myVersionClause = new TerminalClauseImpl(CLAUSE_NAME, Operator.EQUALS, VERSION_NAME);
        final TerminalClauseImpl myVersionProjectClause = new TerminalClauseImpl(IssueFieldConstants.PROJECT, Operator.EQUALS, PROJECT_ID);

        Query query = new QueryImpl(totalExistingClauses);
        SearchRequest sr = new MockJqlSearchRequest(10000L, query);

        Project project = new MockProject(PROJECT_ID, PROJECT_KEY);
        Version version = new MockVersion(VERSION_ID, VERSION_NAME, project);
        final SearchRequest urlSuffix = mapper.getSearchUrlSuffix(version, sr);
        final String modifiedClauses = urlSuffix.getQuery().getWhereClause().toString();

        assertTrue(modifiedClauses.contains(projectClause.toString()));
        assertTrue(modifiedClauses.contains(issueTypeClause.toString()));
        assertTrue(modifiedClauses.contains(myVersionClause.toString()));
        assertTrue(modifiedClauses.contains(myVersionProjectClause.toString()));
    }

    @Test
    public void testGetUrlSuffixForNullVersion() throws Exception
    {
        VersionStatisticsMapper mapper = new MyVersionStatisticsMapper();

        final TerminalClauseImpl projectClause = new TerminalClauseImpl(IssueFieldConstants.PROJECT, Operator.EQUALS, PROJECT_ID);
        final TerminalClauseImpl issueTypeClause = new TerminalClauseImpl(IssueFieldConstants.ISSUE_TYPE, Operator.EQUALS, "Bug");
        final AndClause totalExistingClauses = new AndClause(projectClause, issueTypeClause);

        final TerminalClauseImpl myVersionClause = new TerminalClauseImpl(CLAUSE_NAME, Operator.IS, EmptyOperand.EMPTY);

        Query query = new QueryImpl(totalExistingClauses);
        SearchRequest sr = new MockJqlSearchRequest(10000L, query);

        final SearchRequest urlSuffix = mapper.getSearchUrlSuffix(null, sr);
        final String modifiedClauses = urlSuffix.getQuery().getWhereClause().toString();

        assertTrue(modifiedClauses.contains(projectClause.toString()));
        assertTrue(modifiedClauses.contains(issueTypeClause.toString()));
        assertTrue(modifiedClauses.contains(myVersionClause.toString()));
    }

    @Test
    public void testGetUrlSuffixForNullSearchRequest() throws Exception
    {
        VersionStatisticsMapper mapper = new MyVersionStatisticsMapper();
        assertNull(mapper.getSearchUrlSuffix(null, null));
    }

    private static class MyVersionStatisticsMapper extends VersionStatisticsMapper
    {
        protected MyVersionStatisticsMapper()
        {
            super(CLAUSE_NAME, DOCUMENT_CONSTANT, null, false);
        }
    }
}
