package com.atlassian.jira.jql.query;

import com.atlassian.jira.config.DefaultStatusCategoryManager;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.issue.status.MockStatus;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.category.StatusCategoryImpl;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.StatusCategoryResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collection;
import java.util.Set;

/**
 * @since v6.2
 */
public class TestStatusCategoryClauseQueryFactory
{

    @Mock
    private StatusManager statusManager;

    private StatusCategoryClauseQueryFactory factory;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        JqlOperandResolver operandResolver = MockJqlOperandResolver.createSimpleSupport();

        final Collection<Status> statuses = ImmutableList.<Status>of(
                new MockStatus("1", "undefined 1", StatusCategoryImpl.findById(1L)),
                new MockStatus("2", "todo 1", StatusCategoryImpl.findById(2L)),
                new MockStatus("3", "in progress 1", StatusCategoryImpl.findById(4L)),
                new MockStatus("4", "in progress 2", StatusCategoryImpl.findById(4L))
        );

        Mockito.when(statusManager.getStatuses()).thenReturn(statuses);

        StatusCategoryResolver statusCategoryResolver = new StatusCategoryResolver(new DefaultStatusCategoryManager(Mockito.mock(FeatureManager.class)));
        factory = new StatusCategoryClauseQueryFactory(statusManager, operandResolver, statusCategoryResolver);
    }

    @Test
    public void testSingleNumeric() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("statusCategory", 1L);
        final QueryFactoryResult query = factory.getQuery(null, clause);
        Assert.assertEquals("1", query.getLuceneQuery().toString("status"));
        Assert.assertFalse(query.mustNotOccur());
    }

    @Test
    public void testSingleKey() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("statusCategory", "new");
        final QueryFactoryResult query = factory.getQuery(null, clause);
        Assert.assertEquals("2", query.getLuceneQuery().toString("status"));
        Assert.assertFalse(query.mustNotOccur());
    }

    @Test
    public void testSingleName() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("statusCategory", "In Progress");
        final QueryFactoryResult query = factory.getQuery(null, clause);
        Assert.assertEquals(normalize("3 4"), normalize(query.getLuceneQuery().toString("status")));
        Assert.assertFalse(query.mustNotOccur());
    }

    @Test
    public void testSingleUnknownName() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("statusCategory", "Invalid Name");
        final QueryFactoryResult query = factory.getQuery(null, clause);
        Assert.assertEquals(QueryFactoryResult.createFalseResult(), query);
    }

    @Test
    public void testMultiNumeric() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("statusCategory", 2L, 4L);
        final QueryFactoryResult query = factory.getQuery(null, clause);
        Assert.assertEquals(normalize("2 3 4"), normalize(query.getLuceneQuery().toString("status")));
        Assert.assertFalse(query.mustNotOccur());
    }

    @Test
    public void testMultiKeyOrName() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("statusCategory", "indeterminate", "New");
        final QueryFactoryResult query = factory.getQuery(null, clause);
        Assert.assertEquals(normalize("2 3 4"), normalize(query.getLuceneQuery().toString("status")));
        Assert.assertFalse(query.mustNotOccur());
    }

    @Test
    public void testEmptyIsUnderstood() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("statusCategory", Operator.IN, new MultiValueOperand(EmptyOperand.EMPTY, new SingleValueOperand("New")));
        final QueryFactoryResult query = factory.getQuery(null, clause);
        Assert.assertEquals(normalize("1 2"), normalize(query.getLuceneQuery().toString("status")));
        Assert.assertFalse(query.mustNotOccur());
    }

    @Test
    public void testEmpty() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("statusCategory", Operator.IS, EmptyOperand.EMPTY);
        final QueryFactoryResult query = factory.getQuery(null, clause);
        Assert.assertEquals("1", query.getLuceneQuery().toString("status"));
        Assert.assertFalse(query.mustNotOccur());
    }

    @Test
    public void testIsNotEmpty() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("statusCategory", Operator.IS_NOT, EmptyOperand.EMPTY);
        final QueryFactoryResult query = factory.getQuery(null, clause);
        Assert.assertEquals("1", query.getLuceneQuery().toString("status"));
        Assert.assertTrue(query.mustNotOccur());
    }

    @Test
    public void testNotInExcludesEmpty() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("statusCategory", Operator.NOT_IN, new MultiValueOperand("new"));
        final QueryFactoryResult query = factory.getQuery(null, clause);
        Assert.assertEquals(normalize("1 2"), normalize(query.getLuceneQuery().toString("status")));
        Assert.assertTrue(query.mustNotOccur());
    }

    @Test
    public void testNotEqualsExcludesEmpty() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("statusCategory", Operator.NOT_EQUALS, new MultiValueOperand("new"));
        final QueryFactoryResult query = factory.getQuery(null, clause);
        Assert.assertEquals(normalize("1 2"), normalize(query.getLuceneQuery().toString("status")));
        Assert.assertTrue(query.mustNotOccur());
    }


    private Set<String> normalize(String string)
    {
        return ImmutableSet.copyOf(StringUtils.split(string));
    }
}
