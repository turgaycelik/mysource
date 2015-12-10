package com.atlassian.jira.jql.query;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestLastViewedDateClauseQueryFactory extends MockControllerTestCase
{
    private JqlOperandResolver jqlOperandResolver;
    private JqlDateSupport jqlDateSupport;
    private User theUser = null;
    private QueryCreationContext queryCreationContext;
    private UserIssueHistoryManager userHistoryManager;

    @Before
    public void setUp() throws Exception
    {
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlDateSupport = mockController.getMock(JqlDateSupport.class);
        userHistoryManager = mockController.getMock(UserIssueHistoryManager.class);

        queryCreationContext = new QueryCreationContextImpl(theUser);
    }

    @Test
    public void testInvalidOperand() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, null);

        final Operand blah = new SingleValueOperand("blah");

        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(false);
        replay();

        assertEquals(QueryFactoryResult.createFalseResult(), factory.getQuery(queryCreationContext, new TerminalClauseImpl("lastViewed", Operator.EQUALS, blah)));

        verify();

    }

    @Test
    public void testGetQueryBadOperators() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, null);

        final Operand blah = new SingleValueOperand("blah");

        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).times(5);
        replay();

        Set<Operator> changeHistoryOperators = OperatorClasses.CHANGE_HISTORY_OPERATORS;
        for (Operator operator : changeHistoryOperators)
        {
            assertEquals(QueryFactoryResult.createFalseResult(), factory.getQuery(queryCreationContext, new TerminalClauseImpl("lastViewed", operator, blah)));
        }

        verify();
    }

    @Test
    public void testGetQueryEmptyHistory() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Operand blah = new SingleValueOperand("blah");

        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(Collections.<UserHistoryItem>emptyList());
        replay();

        assertEquals(QueryFactoryResult.createFalseResult(), factory.getQuery(queryCreationContext, new TerminalClauseImpl("lastViewed", Operator.EQUALS, blah)));

        verify();
    }

    //  is//= Empty

    @Test
    public void testGetQueryEqualsEmpty() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Operand blah = new EmptyOperand();

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", 100l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(true).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, new TerminalClauseImpl("lastViewed", Operator.EQUALS, blah));
        assertEquals("issue_id:00001", result.getLuceneQuery().toString());
        assertTrue(result.mustNotOccur());

        verify();
    }

    @Test
    public void testGetQueryIsEmpty() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Operand blah = new EmptyOperand();

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", 100l);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(true).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, new TerminalClauseImpl("lastViewed", Operator.IS, blah));
        assertEquals("issue_id:00001 issue_id:00002", result.getLuceneQuery().toString());
        assertTrue(result.mustNotOccur());

        verify();
    }

    @Test
    public void testGetQueryInEmptyOnly() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Operand blah = new EmptyOperand();

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", 100l);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.IN, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral())).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(true).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("issue_id:00001 issue_id:00002", result.getLuceneQuery().toString());
        assertTrue(result.mustNotOccur());

        verify();
    }


    // is not/!= empty

    @Test
    public void testGetQueryIsNotEmpty() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Operand blah = new EmptyOperand();

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", 100l);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(true).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, new TerminalClauseImpl("lastViewed", Operator.IS_NOT, blah));
        assertEquals("issue_id:00001 issue_id:00002", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());

        verify();
    }

    @Test
    public void testGetQueryNotEqualsEmpty() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Operand blah = new EmptyOperand();

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", 100l);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(true).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, new TerminalClauseImpl("lastViewed", Operator.NOT_EQUALS, blah));
        assertEquals("issue_id:00001 issue_id:00002", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());

        verify();
    }
    @Test
    public void testGetQueryNotInEmptyOnly() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Operand blah = new EmptyOperand();

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", 100l);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.NOT_IN, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral())).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(true).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("issue_id:00001 issue_id:00002", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());

        verify();
    }

    // in/= single

    @Test
    public void testGetQueryEquals() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Date now = new Date();

        final Operand blah = new SingleValueOperand(now.getTime());

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", now.getTime());
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.EQUALS, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), now.getTime()))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(false).once();
        expect(jqlDateSupport.convertToDate(now.getTime())).andReturn(now).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("issue_id:00001", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());

        verify();
    }
   @Test
    public void testGetQueryEqualsNoneMatch() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Date now = new Date();

        final Operand blah = new SingleValueOperand(now.getTime());

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", 100l);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.EQUALS, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), now.getTime()))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(false).once();
        expect(jqlDateSupport.convertToDate(now.getTime())).andReturn(now).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());

        verify();
    }

    @Test
    public void testGetQueryIn() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Date now = new Date();

        final Operand blah = new SingleValueOperand(now.getTime());

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", now.getTime());
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.IN, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), now.getTime()))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(true).once();
        expect(jqlDateSupport.convertToDate(now.getTime())).andReturn(now).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("issue_id:00001", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());

        verify();
    }

    @Test
    public void testGetQueryInNoneMatch() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Date now = new Date();

        final Operand blah = new SingleValueOperand(now.getTime());

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", 100l);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.IN, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), now.getTime()))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(true).once();
        expect(jqlDateSupport.convertToDate(now.getTime())).andReturn(now).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());

        verify();
    }

    @Test
    public void testGetQueryEqualsMatchesMultiple() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Date now = new Date();

        final Operand blah = new SingleValueOperand(now.getTime());

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", now.getTime());
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "00003", now.getTime());

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2, item3);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.EQUALS, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), now.getTime()))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(false).once();
        expect(jqlDateSupport.convertToDate(now.getTime())).andReturn(now).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("issue_id:00001 issue_id:00003", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());

        verify();
    }
    @Test
    public void testGetQueryInMatchesMultiple() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Date now = new Date();

        final Operand blah = new SingleValueOperand(now.getTime());

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", now.getTime());
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "00003", now.getTime());

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2, item3);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.IN, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), now.getTime()))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(true).once();
        expect(jqlDateSupport.convertToDate(now.getTime())).andReturn(now).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("issue_id:00001 issue_id:00003", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());

        verify();
    }
    @Test
    public void testGetQueryInMultipleNoMatch() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Date now = new Date();

        long otherTime = now.getTime() - 100l;
        final Operand blah = new MultiValueOperand(now.getTime(), otherTime);

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", 100l);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "00003", 300l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2, item3);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.IN, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), now.getTime()), new QueryLiteral(clause.getOperand(), otherTime))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(true).once();
        expect(jqlDateSupport.convertToDate(now.getTime())).andReturn(now).once();
        expect(jqlDateSupport.convertToDate(otherTime)).andReturn(new Date(otherTime)).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());

        verify();
    }
    @Test
    public void testGetQueryInMultipleSingleMatch() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Date now = new Date();

        long otherTime = now.getTime() - 100l;
        final Operand blah = new MultiValueOperand(now.getTime(), otherTime);

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", now.getTime());
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "00003", 300l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2, item3);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.IN, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), now.getTime()), new QueryLiteral(clause.getOperand(), otherTime))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(true).once();
        expect(jqlDateSupport.convertToDate(now.getTime())).andReturn(now).once();
        expect(jqlDateSupport.convertToDate(otherTime)).andReturn(new Date(otherTime)).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("issue_id:00001", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());

        verify();
    }

    @Test
    public void testGetQueryInMultipleMultipleMatch() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Date now = new Date();

        long otherTime = now.getTime() - 100l;
        final Operand blah = new MultiValueOperand(now.getTime(), otherTime);

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", now.getTime());
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "00003", otherTime);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2, item3);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.IN, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), now.getTime()), new QueryLiteral(clause.getOperand(), otherTime))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(true).once();
        expect(jqlDateSupport.convertToDate(now.getTime())).andReturn(now).once();
        expect(jqlDateSupport.convertToDate(otherTime)).andReturn(new Date(otherTime)).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("issue_id:00003 issue_id:00001", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());

        verify();
    }

    @Test
    public void testGetQueryInMultipleWithEmptyNoneMatch() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Date now = new Date();

        final Operand blah = new MultiValueOperand(new SingleValueOperand(now.getTime()), new EmptyOperand());

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", 100l);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "00003", 300l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2, item3);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.IN, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), now.getTime()), new QueryLiteral(clause.getOperand(), (Long)null))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(true).once();
        expect(jqlDateSupport.convertToDate(now.getTime())).andReturn(now).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("-issue_id:00001 -issue_id:00002 -issue_id:00003", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());

        verify();
    }

    @Test
    public void testGetQueryInMultipleWithEmptyWithMatch() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Date now = new Date();

        final Operand blah = new MultiValueOperand(new SingleValueOperand(now.getTime()), new EmptyOperand());

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", now.getTime());
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "00003", 300l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2, item3);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.IN, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), now.getTime()), new QueryLiteral(clause.getOperand(), (Long)null))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(true).once();
        expect(jqlDateSupport.convertToDate(now.getTime())).andReturn(now).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("-issue_id:00002 -issue_id:00003", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());

        verify();
    }

    @Test
    public void testGetQueryInMultipleWithEmptyWithMultipleMatch() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Date now = new Date();
        long otherTime = now.getTime() - 100l;

        final Operand blah = new MultiValueOperand(new SingleValueOperand(now.getTime()), new EmptyOperand(), new SingleValueOperand(otherTime));

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", now.getTime());
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "00003", otherTime);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2, item3);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.IN, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), now.getTime()), new QueryLiteral(clause.getOperand(), (Long)null), new QueryLiteral(clause.getOperand(), otherTime))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(true).once();
        expect(jqlDateSupport.convertToDate(now.getTime())).andReturn(now).once();
        expect(jqlDateSupport.convertToDate(otherTime)).andReturn(new Date(otherTime)).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("-issue_id:00002", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());

        verify();
    }

    @Test
    public void testGetQueryNotEquals() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Date now = new Date();

        final Operand blah = new SingleValueOperand(now.getTime());

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", now.getTime());
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.NOT_EQUALS, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), now.getTime()))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(false).once();
        expect(jqlDateSupport.convertToDate(now.getTime())).andReturn(now).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("issue_id:00001", result.getLuceneQuery().toString());
        assertTrue(result.mustNotOccur());

        verify();
    }
   @Test
    public void testGetQueryNotEqualsNoneMatch() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Date now = new Date();

        final Operand blah = new SingleValueOperand(now.getTime());

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", 100l);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.NOT_EQUALS, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), now.getTime()))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(false).once();
        expect(jqlDateSupport.convertToDate(now.getTime())).andReturn(now).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("", result.getLuceneQuery().toString());
        assertTrue(result.mustNotOccur());

        verify();
    }

    @Test
    public void testGetQueryNotIn() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Date now = new Date();

        final Operand blah = new SingleValueOperand(now.getTime());

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", now.getTime());
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.NOT_IN, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), now.getTime()))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(true).once();
        expect(jqlDateSupport.convertToDate(now.getTime())).andReturn(now).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("issue_id:00001", result.getLuceneQuery().toString());
        assertTrue(result.mustNotOccur());

        verify();
    }

    @Test
    public void testGetQueryNotInNoneMatch() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Date now = new Date();

        final Operand blah = new SingleValueOperand(now.getTime());

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", 100l);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.NOT_IN, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), now.getTime()))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(true).once();
        expect(jqlDateSupport.convertToDate(now.getTime())).andReturn(now).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("", result.getLuceneQuery().toString());
        assertTrue(result.mustNotOccur());

        verify();
    }

    @Test
    public void testGetQueryNotEqualsMatchesMultiple() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Date now = new Date();

        final Operand blah = new SingleValueOperand(now.getTime());

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", now.getTime());
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "00003", now.getTime());

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2, item3);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.NOT_EQUALS, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), now.getTime()))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(false).once();
        expect(jqlDateSupport.convertToDate(now.getTime())).andReturn(now).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("issue_id:00001 issue_id:00003", result.getLuceneQuery().toString());
        assertTrue(result.mustNotOccur());

        verify();
    }
    @Test
    public void testGetQueryNotInMatchesMultiple() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Date now = new Date();

        final Operand blah = new SingleValueOperand(now.getTime());

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", now.getTime());
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "00003", now.getTime());

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2, item3);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.NOT_IN, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), now.getTime()))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(true).once();
        expect(jqlDateSupport.convertToDate(now.getTime())).andReturn(now).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("issue_id:00001 issue_id:00003", result.getLuceneQuery().toString());
        assertTrue(result.mustNotOccur());

        verify();
    }
    @Test
    public void testGetQueryNotInMultipleNoMatch() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Date now = new Date();

        long otherTime = now.getTime() - 100l;
        final Operand blah = new MultiValueOperand(now.getTime(), otherTime);

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", 100l);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "00003", 300l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2, item3);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.NOT_IN, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), now.getTime()), new QueryLiteral(clause.getOperand(), otherTime))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(true).once();
        expect(jqlDateSupport.convertToDate(now.getTime())).andReturn(now).once();
        expect(jqlDateSupport.convertToDate(otherTime)).andReturn(new Date(otherTime)).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("", result.getLuceneQuery().toString());
        assertTrue(result.mustNotOccur());

        verify();
    }
    @Test
    public void testGetQueryNotInMultipleSingleMatch() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Date now = new Date();

        long otherTime = now.getTime() - 100l;
        final Operand blah = new MultiValueOperand(now.getTime(), otherTime);

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", now.getTime());
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "00003", 300l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2, item3);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.NOT_IN, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), now.getTime()), new QueryLiteral(clause.getOperand(), otherTime))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(true).once();
        expect(jqlDateSupport.convertToDate(now.getTime())).andReturn(now).once();
        expect(jqlDateSupport.convertToDate(otherTime)).andReturn(new Date(otherTime)).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("issue_id:00001", result.getLuceneQuery().toString());
        assertTrue(result.mustNotOccur());

        verify();
    }

    @Test
    public void testGetQueryNotInMultipleMultipleMatch() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Date now = new Date();

        long otherTime = now.getTime() - 100l;
        final Operand blah = new MultiValueOperand(now.getTime(), otherTime);

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", now.getTime());
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "00003", otherTime);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2, item3);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.NOT_IN, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), now.getTime()), new QueryLiteral(clause.getOperand(), otherTime))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(true).once();
        expect(jqlDateSupport.convertToDate(now.getTime())).andReturn(now).once();
        expect(jqlDateSupport.convertToDate(otherTime)).andReturn(new Date(otherTime)).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("issue_id:00003 issue_id:00001", result.getLuceneQuery().toString());
        assertTrue(result.mustNotOccur());

        verify();
    }

    @Test
    public void testGetQueryNotInMultipleWithEmptyNoneMatch() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Date now = new Date();

        final Operand blah = new MultiValueOperand(new SingleValueOperand(now.getTime()), new EmptyOperand());

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", 100l);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "00003", 300l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2, item3);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.NOT_IN, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), now.getTime()), new QueryLiteral(clause.getOperand(), (Long)null))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(true).once();
        expect(jqlDateSupport.convertToDate(now.getTime())).andReturn(now).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("-issue_id:00001 -issue_id:00002 -issue_id:00003", result.getLuceneQuery().toString());
        assertTrue(result.mustNotOccur());

        verify();
    }

    @Test
    public void testGetQueryNotInMultipleWithEmptyWithMatch() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Date now = new Date();

        final Operand blah = new MultiValueOperand(new SingleValueOperand(now.getTime()), new EmptyOperand());

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", now.getTime());
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "00003", 300l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2, item3);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.NOT_IN, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), now.getTime()), new QueryLiteral(clause.getOperand(), (Long)null))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(true).once();
        expect(jqlDateSupport.convertToDate(now.getTime())).andReturn(now).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("-issue_id:00002 -issue_id:00003", result.getLuceneQuery().toString());
        assertTrue(result.mustNotOccur());

        verify();
    }

    @Test
    public void testGetQueryNotInMultipleWithEmptyWithMultipleMatch() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final Date now = new Date();
        long otherTime = now.getTime() - 100l;

        final Operand blah = new MultiValueOperand(new SingleValueOperand(now.getTime()), new EmptyOperand(), new SingleValueOperand(otherTime));

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", now.getTime());
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "00003", otherTime);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2, item3);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.NOT_IN, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), now.getTime()), new QueryLiteral(clause.getOperand(), (Long)null), new QueryLiteral(clause.getOperand(), otherTime))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(true).once();
        expect(jqlDateSupport.convertToDate(now.getTime())).andReturn(now).once();
        expect(jqlDateSupport.convertToDate(otherTime)).andReturn(new Date(otherTime)).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("-issue_id:00002", result.getLuceneQuery().toString());
        assertTrue(result.mustNotOccur());

        verify();
    }

    // >
    @Test
    public void testGetQueryGreaterThanNoMatches() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final long compareTime = 300l;
        final Operand blah = new SingleValueOperand(compareTime);

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", 100l);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "00003", 300l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2, item3);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.GREATER_THAN, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), compareTime))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(false).once();
        expect(jqlDateSupport.convertToDate(compareTime)).andReturn(new Date(compareTime)).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());

        verify();
    }
   @Test
    public void testGetQueryGreaterThanMatches() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final long compareTime = 200l;
        final Operand blah = new SingleValueOperand(compareTime);

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", 100l);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "00003", 300l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2, item3);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.GREATER_THAN, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), compareTime))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(false).once();
        expect(jqlDateSupport.convertToDate(compareTime)).andReturn(new Date(compareTime)).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("issue_id:00003", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());

        verify();
    }
    // >=
    @Test
    public void testGetQueryGreaterEqualsThanNoMatches() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final long compareTime = 400l;
        final Operand blah = new SingleValueOperand(compareTime);

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", 100l);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "00003", 300l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2, item3);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.GREATER_THAN_EQUALS, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), compareTime))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(false).once();
        expect(jqlDateSupport.convertToDate(compareTime)).andReturn(new Date(compareTime)).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());

        verify();
    }
   @Test
    public void testGetQueryGreaterEqualsThanMatches() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final long compareTime = 200l;
        final Operand blah = new SingleValueOperand(compareTime);

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", 100l);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "00003", 300l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2, item3);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.GREATER_THAN_EQUALS, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), compareTime))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(false).once();
        expect(jqlDateSupport.convertToDate(compareTime)).andReturn(new Date(compareTime)).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("issue_id:00002 issue_id:00003", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());

        verify();
    }

    // >
    @Test
    public void testGetQueryLessThanNoMatches() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final long compareTime = 100l;
        final Operand blah = new SingleValueOperand(compareTime);

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", 100l);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "00003", 300l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2, item3);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.LESS_THAN, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), compareTime))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(false).once();
        expect(jqlDateSupport.convertToDate(compareTime)).andReturn(new Date(compareTime)).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());

        verify();
    }
   @Test
    public void testGetQueryLessThanMatches() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final long compareTime = 200l;
        final Operand blah = new SingleValueOperand(compareTime);

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", 100l);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "00003", 300l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2, item3);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.LESS_THAN, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), compareTime))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(false).once();
        expect(jqlDateSupport.convertToDate(compareTime)).andReturn(new Date(compareTime)).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("issue_id:00001", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());

        verify();
    }
    // >=
    @Test
    public void testGetQueryLessThanEqualsNoMatches() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final long compareTime = 50l;
        final Operand blah = new SingleValueOperand(compareTime);

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", 100l);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "00003", 300l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2, item3);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.LESS_THAN_EQUALS, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), compareTime))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(false).once();
        expect(jqlDateSupport.convertToDate(compareTime)).andReturn(new Date(compareTime)).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());

        verify();
    }
   @Test
    public void testGetQueryLessThanEqualsMatches() throws Exception
    {
        final LastViewedDateClauseQueryFactory factory = new LastViewedDateClauseQueryFactory(jqlDateSupport, jqlOperandResolver, userHistoryManager);

        final long compareTime = 200l;
        final Operand blah = new SingleValueOperand(compareTime);

        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "00001", 100l);
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "00002", 200l);
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "00003", 300l);

        final List<UserHistoryItem> history = CollectionBuilder.list(item1, item2, item3);
        expect(jqlOperandResolver.isValidOperand(blah)).andReturn(true).once();
        expect(jqlOperandResolver.isEmptyOperand(blah)).andReturn(false).once();
        expect(userHistoryManager.getFullIssueHistoryWithoutPermissionChecks(theUser)).andReturn(history);
        TerminalClause clause = new TerminalClauseImpl("lastViewed", Operator.LESS_THAN_EQUALS, blah);
        expect(jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause)).andReturn(CollectionBuilder.list(new QueryLiteral(clause.getOperand(), compareTime))).once();
        expect(jqlOperandResolver.isListOperand(blah)).andReturn(false).once();
        expect(jqlDateSupport.convertToDate(compareTime)).andReturn(new Date(compareTime)).once();
        replay();

        QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);
        assertEquals("issue_id:00001 issue_id:00002", result.getLuceneQuery().toString());
        assertFalse(result.mustNotOccur());

        verify();
    }



}
