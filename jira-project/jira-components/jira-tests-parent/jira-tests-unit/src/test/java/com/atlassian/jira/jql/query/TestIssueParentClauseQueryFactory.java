package com.atlassian.jira.jql.query;

import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlIssueSupport;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link IssueParentClauseQueryFactory}.
 *
 * @since v4.0
 */
public class TestIssueParentClauseQueryFactory extends MockControllerTestCase
{
    private JqlOperandResolver jqlOperandResolver;
    private JqlIssueSupport issueSupport;
    private SubTaskManager subTaskManager;
    private User theUser = null;
    private QueryCreationContext queryCreationContext;
    private boolean overrideSecurity = false;

    @Before
    public void setUp() throws Exception
    {
        jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
        issueSupport = mockController.getMock(JqlIssueSupport.class);
        subTaskManager = mockController.getMock(SubTaskManager.class);
        expect(subTaskManager.isSubTasksEnabled()).andStubReturn(true);
        queryCreationContext = new QueryCreationContextImpl(theUser, overrideSecurity);
    }

    @Test
    public void testSubTasksDisabled() throws Exception
    {
        final String fieldName = "field";
        final EmptyOperand emptyOperand = new EmptyOperand();

        expect(subTaskManager.isSubTasksEnabled()).andReturn(false);

        replay();

        final IssueParentClauseQueryFactory factory = new IssueParentClauseQueryFactory(jqlOperandResolver, issueSupport, subTaskManager);
        final QueryFactoryResult query1 = factory.getQuery(queryCreationContext, new TerminalClauseImpl(fieldName, Operator.IS, emptyOperand));

        assertFalse(query1.mustNotOccur());
        assertEquals(new BooleanQuery(), query1.getLuceneQuery());

        final QueryFactoryResult query2 = factory.getQuery(queryCreationContext, new TerminalClauseImpl(fieldName, Operator.EQUALS, emptyOperand));

        assertFalse(query2.mustNotOccur());
        assertEquals(new BooleanQuery(), query2.getLuceneQuery());

        verify();
    }

    @Test
    public void testEmptyClause() throws Exception
    {
        final String fieldName = "field";
        final EmptyOperand emptyOperand = new EmptyOperand();

        replay();

        final IssueParentClauseQueryFactory factory = new IssueParentClauseQueryFactory(jqlOperandResolver, issueSupport, subTaskManager);
        final QueryFactoryResult query1 = factory.getQuery(queryCreationContext, new TerminalClauseImpl(fieldName, Operator.IS, emptyOperand));

        assertFalse(query1.mustNotOccur());
        assertEquals(new BooleanQuery(), query1.getLuceneQuery());

        final QueryFactoryResult query2 = factory.getQuery(queryCreationContext, new TerminalClauseImpl(fieldName, Operator.EQUALS, emptyOperand));

        assertFalse(query2.mustNotOccur());
        assertEquals(new BooleanQuery(), query2.getLuceneQuery());

        verify();
    }

    @Test
    public void testNotEmptyClause() throws Exception
    {
        final String fieldName = "field";
        final EmptyOperand emptyOperand = new EmptyOperand();

        replay();

        final IssueParentClauseQueryFactory factory = new IssueParentClauseQueryFactory(jqlOperandResolver, issueSupport, subTaskManager);
        final QueryFactoryResult query1 = factory.getQuery(queryCreationContext, new TerminalClauseImpl(fieldName, Operator.IS_NOT, emptyOperand));

        assertFalse(query1.mustNotOccur());
        assertEquals(new BooleanQuery(), query1.getLuceneQuery());

        final QueryFactoryResult query2 = factory.getQuery(queryCreationContext, new TerminalClauseImpl(fieldName, Operator.NOT_EQUALS, emptyOperand));

        assertTrue(query2.mustNotOccur());
        assertEquals(new BooleanQuery(), query2.getLuceneQuery());

        verify();
    }

    @Test
    public void testEqualsNoValues() throws Exception
    {
        final String fieldName = "equals";
        final SingleValueOperand singleValueOperand = new SingleValueOperand("value");
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, singleValueOperand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        expect(jqlOperandResolver.getValues(queryCreationContext, singleValueOperand, clause)).andReturn(null);

        replay();

        final IssueParentClauseQueryFactory factory = new IssueParentClauseQueryFactory(jqlOperandResolver, issueSupport, subTaskManager);
        final QueryFactoryResult query = factory.getQuery(queryCreationContext, clause);

        assertEquals(new BooleanQuery(), query.getLuceneQuery());
        assertFalse(query.mustNotOccur());

        verify();
    }

    @Test
    public void testEqualsSingleValueId() throws Exception
    {
        final String fieldName = "equals";
        final SingleValueOperand singleValueOperand = new SingleValueOperand(1L);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, singleValueOperand);

        replay();

        final IssueParentClauseQueryFactory factory = new IssueParentClauseQueryFactory(jqlOperandResolver, issueSupport, subTaskManager);
        final QueryFactoryResult query = factory.getQuery(queryCreationContext, clause);

        final Query expectedQuery = new TermQuery(new Term(SystemSearchConstants.forIssueParent().getIndexField(), "1"));

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());

        verify();
    }

    @Test
    public void testEqualsSingleValueKeyOverrideSecurity() throws Exception
    {
        overrideSecurity = true;
        queryCreationContext = new QueryCreationContextImpl(theUser, overrideSecurity);

        final String fieldName = "equals";
        final SingleValueOperand singleValueOperand = new SingleValueOperand("TST-1");
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, singleValueOperand);

        EasyMock.expect(issueSupport.getIssue("TST-1"))
                .andReturn(new MockIssue(5L));

        replay();

        final IssueParentClauseQueryFactory factory = new IssueParentClauseQueryFactory(jqlOperandResolver, issueSupport, subTaskManager);
        final QueryFactoryResult query = factory.getQuery(queryCreationContext, clause);

        final Query expectedQuery = new TermQuery(new Term(SystemSearchConstants.forIssueParent().getIndexField(), "5"));

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());
    }

    @Test
    public void testEqualsMultipleValueId() throws Exception
    {
        final String fieldName = "equals";
        final Operand operand = new MultiValueOperand(createLiteral(1L), createLiteral(2L), new QueryLiteral());
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        replay();

        final IssueParentClauseQueryFactory factory = new IssueParentClauseQueryFactory(jqlOperandResolver, issueSupport, subTaskManager);
        final QueryFactoryResult query = factory.getQuery(queryCreationContext, clause);

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermQuery(new Term(SystemSearchConstants.forIssueParent().getIndexField(), "1")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term(SystemSearchConstants.forIssueParent().getIndexField(), "2")), BooleanClause.Occur.SHOULD);

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertFalse(query.mustNotOccur());

        verify();
    }

    @Test
    public void testNotEqualsSingleValueId() throws Exception
    {
        final String fieldName = "equals";
        final SingleValueOperand singleValueOperand = new SingleValueOperand(1L);
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.NOT_EQUALS, singleValueOperand);

        replay();

        final IssueParentClauseQueryFactory factory = new IssueParentClauseQueryFactory(jqlOperandResolver, issueSupport, subTaskManager);
        final QueryFactoryResult query = factory.getQuery(queryCreationContext, clause);

        final Query expectedQuery = new TermQuery(new Term(SystemSearchConstants.forIssueParent().getIndexField(), "1"));

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertTrue(query.mustNotOccur());

        verify();
    }

    @Test
    public void testNotEqualsMultipleValueId() throws Exception
    {
        final String fieldName = "equals";
        final MultiValueOperand operand = new MultiValueOperand(createLiteral(1L), createLiteral(2L), new QueryLiteral());
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.NOT_IN, operand);

        replay();

        final IssueParentClauseQueryFactory factory = new IssueParentClauseQueryFactory(jqlOperandResolver, issueSupport, subTaskManager);
        final QueryFactoryResult query = factory.getQuery(queryCreationContext, clause);

        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new TermQuery(new Term(SystemSearchConstants.forIssueParent().getIndexField(), "1")), BooleanClause.Occur.SHOULD);
        expectedQuery.add(new TermQuery(new Term(SystemSearchConstants.forIssueParent().getIndexField(), "2")), BooleanClause.Occur.SHOULD);

        assertEquals(expectedQuery, query.getLuceneQuery());
        assertTrue(query.mustNotOccur());

        verify();
    }

    @Test
    public void testGetIndexValuesForStringLiterals() throws Exception
    {
        final String stringLiteral = "KEY";

        expect(issueSupport.getIssue(stringLiteral, (ApplicationUser) theUser)).andReturn(new MockIssue(555L));

        replay();

        final IssueParentClauseQueryFactory factory = new IssueParentClauseQueryFactory(jqlOperandResolver, issueSupport, subTaskManager);
        final List<String> stringIds = factory.getIndexValues(theUser, overrideSecurity, Collections.singletonList(createLiteral(stringLiteral)));
        
        assertEquals(1, stringIds.size());
        assertEquals("555", stringIds.get(0));

        verify();
    }

    @Test
    public void testGetIndexValuesForStringLiteralsOverrideSecurity() throws Exception
    {
        overrideSecurity = true;

        final String stringLiteral = "KEY";

        expect(issueSupport.getIssue(stringLiteral)).andReturn(new MockIssue(555L));

        replay();

        final IssueParentClauseQueryFactory factory = new IssueParentClauseQueryFactory(jqlOperandResolver, issueSupport, subTaskManager);
        final List<String> stringIds = factory.getIndexValues(theUser, overrideSecurity, Collections.singletonList(createLiteral(stringLiteral)));

        assertEquals(1, stringIds.size());
        assertEquals("555", stringIds.get(0));

        verify();
    }

    @Test
    public void testGetIndexValuesForStringLiteralsNoIssues() throws Exception
    {
        final String stringLiteral = "KEY";

        expect(issueSupport.getIssue(stringLiteral, (ApplicationUser) theUser)).andReturn(null);

        replay();

        final IssueParentClauseQueryFactory factory = new IssueParentClauseQueryFactory(jqlOperandResolver, issueSupport, subTaskManager);
        final List<String> stringIds = factory.getIndexValues(theUser, overrideSecurity, Collections.singletonList(createLiteral(stringLiteral)));

        assertTrue(stringIds.isEmpty());

        verify();
    }

    @Test
    public void testNotEqualsNoValues() throws Exception
    {
        final String fieldName = "notEquals";
        final SingleValueOperand singleValueOperand = new SingleValueOperand("value");
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.NOT_IN, singleValueOperand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        expect(jqlOperandResolver.getValues(queryCreationContext, singleValueOperand, clause)).andReturn(null);

        replay();

        final IssueParentClauseQueryFactory factory = new IssueParentClauseQueryFactory(jqlOperandResolver, issueSupport, subTaskManager);
        final QueryFactoryResult query = factory.getQuery(queryCreationContext, clause);

        assertEquals(new BooleanQuery(), query.getLuceneQuery());
        assertFalse(query.mustNotOccur());

        verify();
    }

    @Test
    public void testBadOperator() throws Exception
    {
        final SingleValueOperand singleValueOperand = new SingleValueOperand("value");
        final TerminalClauseImpl clause = new TerminalClauseImpl("dontCare", Operator.LIKE, singleValueOperand);

        replay();

        final IssueParentClauseQueryFactory factory = new IssueParentClauseQueryFactory(jqlOperandResolver, issueSupport, subTaskManager);
        final QueryFactoryResult query = factory.getQuery(queryCreationContext, clause);

        assertEquals(new BooleanQuery(), query.getLuceneQuery());
        assertFalse(query.mustNotOccur());

        verify();
    }
}
