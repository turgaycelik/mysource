package com.atlassian.jira.jql.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlCascadingSelectLiteralUtil;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operator.Operator;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestCascadingSelectCustomFieldClauseQueryFactory extends MockControllerTestCase
{
    private String luceneField;
    private String fieldName;
    private JqlSelectOptionsUtil jqlSelectOptionsUtil;
    private JqlCascadingSelectLiteralUtil jqlCascadingSelectLiteralUtil;
    private JqlOperandResolver jqlOperandResolver;
    private String field = "field";
    private CustomField customField;
    private User theUser = null;
    private QueryCreationContext queryCreationContext;

    @Before
    public void setUp() throws Exception
    {
        luceneField = "cascade";
        fieldName = "myfield";
        jqlSelectOptionsUtil = mockController.getMock(JqlSelectOptionsUtil.class);
        jqlCascadingSelectLiteralUtil = mockController.getMock(JqlCascadingSelectLiteralUtil.class);
        jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
        customField = mockController.getMock(CustomField.class);
        queryCreationContext = new QueryCreationContextImpl(theUser);
    }

    @Test
    public void testGetQueryBadOperator() throws Exception
    {
        final CascadingSelectCustomFieldClauseQueryFactory factory = new CascadingSelectCustomFieldClauseQueryFactory(customField, luceneField, jqlSelectOptionsUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil);
        mockController.replay();

        final QueryFactoryResult result = factory.getQuery(queryCreationContext, new TerminalClauseImpl(fieldName, Operator.LESS_THAN, EmptyOperand.EMPTY));

        assertEquals(new BooleanQuery(), result.getLuceneQuery());
        assertFalse(result.mustNotOccur());

        mockController.verify();
    }

    @Test
    public void testGetQueryNoPositivesOrNegatives() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.IS, EmptyOperand.EMPTY);
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause);
        mockController.setReturnValue(Collections.emptyList());

        final CascadingSelectCustomFieldClauseQueryFactory factory = new CascadingSelectCustomFieldClauseQueryFactory(customField, luceneField, jqlSelectOptionsUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil)
        {
            @Override
            void processPositiveNegativeOptionLiterals(final List<QueryLiteral> literals, final List<QueryLiteral> positiveLiterals, final List<QueryLiteral> negativeLiterals)
            {
                // do nothing
            }
        };
        mockController.replay();

        final QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);

        assertEquals(new BooleanQuery(), result.getLuceneQuery());
        assertFalse(result.mustNotOccur());

        mockController.verify();
    }

    @Test
    public void testGetQueryOnlyPositives() throws Exception
    {
        final QueryLiteral positiveLiteral = createLiteral(500L);

        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.IS, EmptyOperand.EMPTY);
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause);
        mockController.setReturnValue(Collections.singletonList(positiveLiteral));

        final BooleanQuery expectedResult = new BooleanQuery();
        expectedResult.add(new TermQuery(new Term(luceneField, "10")), BooleanClause.Occur.SHOULD);

        final CascadingSelectCustomFieldClauseQueryFactory factory = new CascadingSelectCustomFieldClauseQueryFactory(customField, luceneField, jqlSelectOptionsUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil)
        {
            @Override
            void processPositiveNegativeOptionLiterals(final List<QueryLiteral> literals, final List<QueryLiteral> positiveLiterals, final List<QueryLiteral> negativeLiterals)
            {
                positiveLiterals.add(positiveLiteral);
            }

            @Override
            BooleanQuery getQueryFromLiterals(final boolean negationOperator, final List<QueryLiteral> literals)
            {
                if (!negationOperator)
                {
                    assertEquals(Collections.singletonList(positiveLiteral), literals);
                    return expectedResult;
                }
                else
                {
                    return null;
                }
            }
        };
        mockController.replay();

        final QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);

        assertEquals(expectedResult, result.getLuceneQuery());
        assertFalse(result.mustNotOccur());

        mockController.verify();
    }

    @Test
    public void testGetQueryOnlyNegatives() throws Exception
    {
        final QueryLiteral negativeLiteral = createLiteral(500L);

        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.IS, EmptyOperand.EMPTY);
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause);
        mockController.setReturnValue(Collections.singletonList(negativeLiteral));

        final BooleanQuery expectedResult = new BooleanQuery();
        expectedResult.add(new TermQuery(new Term(luceneField, "10")), BooleanClause.Occur.SHOULD);

        final CascadingSelectCustomFieldClauseQueryFactory factory = new CascadingSelectCustomFieldClauseQueryFactory(customField, luceneField, jqlSelectOptionsUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil)
        {
            @Override
            void processPositiveNegativeOptionLiterals(final List<QueryLiteral> literals, final List<QueryLiteral> positiveLiterals, final List<QueryLiteral> negativeLiterals)
            {
                negativeLiterals.add(negativeLiteral);
            }

            @Override
            BooleanQuery getQueryFromLiterals(final boolean negationOperator, final List<QueryLiteral> literals)
            {
                if (negationOperator)
                {
                    assertEquals(Collections.singletonList(negativeLiteral), literals);
                    return expectedResult;
                }
                else
                {
                    return null;
                }
            }
        };
        mockController.replay();

        final QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);

        assertEquals(expectedResult, result.getLuceneQuery());
        assertFalse(result.mustNotOccur());

        mockController.verify();
    }

    @Test
    public void testGetQueryPositivesAndNegativesPositiveOperator() throws Exception
    {
        final QueryLiteral negativeLiteral = createLiteral(500L);
        final QueryLiteral positiveLiteral = createLiteral(600L);

        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.IN, EmptyOperand.EMPTY);
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(positiveLiteral, negativeLiteral).asList());

        final BooleanQuery expectedNegativeResult = new BooleanQuery();
        expectedNegativeResult.add(new TermQuery(new Term(luceneField, "10")), BooleanClause.Occur.SHOULD);
        final BooleanQuery expectedPositiveResult = new BooleanQuery();
        expectedPositiveResult.add(new TermQuery(new Term(luceneField, "10")), BooleanClause.Occur.SHOULD);

        final BooleanQuery expectedResult = new BooleanQuery();
        expectedResult.add(expectedPositiveResult, BooleanClause.Occur.MUST);
        expectedResult.add(expectedNegativeResult, BooleanClause.Occur.MUST);

        final CascadingSelectCustomFieldClauseQueryFactory factory = new CascadingSelectCustomFieldClauseQueryFactory(customField, luceneField, jqlSelectOptionsUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil)
        {
            @Override
            void processPositiveNegativeOptionLiterals(final List<QueryLiteral> literals, final List<QueryLiteral> positiveLiterals, final List<QueryLiteral> negativeLiterals)
            {
                positiveLiterals.add(positiveLiteral);
                negativeLiterals.add(negativeLiteral);
            }

            @Override
            BooleanQuery getQueryFromLiterals(final boolean negationOperator, final List<QueryLiteral> literals)
            {
                if (negationOperator)
                {
                    assertEquals(Collections.singletonList(negativeLiteral), literals);
                    return expectedNegativeResult;
                }
                else
                {
                    assertEquals(Collections.singletonList(positiveLiteral), literals);
                    return expectedPositiveResult;
                }
            }
        };
        mockController.replay();

        final QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);

        assertEquals(expectedResult, result.getLuceneQuery());
        assertFalse(result.mustNotOccur());

        mockController.verify();
    }

    @Test
    public void testGetQueryPositivesAndNegativesNegativeOperator() throws Exception
    {
        final QueryLiteral negativeLiteral = createLiteral(500L);
        final QueryLiteral positiveLiteral = createLiteral(600L);

        final TerminalClauseImpl clause = new TerminalClauseImpl(fieldName, Operator.NOT_IN, EmptyOperand.EMPTY);
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.getValues(queryCreationContext, clause.getOperand(), clause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(positiveLiteral, negativeLiteral).asList());

        final BooleanQuery expectedNegativeResult = new BooleanQuery();
        expectedNegativeResult.add(new TermQuery(new Term(luceneField, "10")), BooleanClause.Occur.SHOULD);
        final BooleanQuery expectedPositiveResult = new BooleanQuery();
        expectedPositiveResult.add(new TermQuery(new Term(luceneField, "10")), BooleanClause.Occur.SHOULD);

        final BooleanQuery expectedResult = new BooleanQuery();
        expectedResult.add(expectedPositiveResult, BooleanClause.Occur.SHOULD);
        expectedResult.add(expectedNegativeResult, BooleanClause.Occur.SHOULD);

        final CascadingSelectCustomFieldClauseQueryFactory factory = new CascadingSelectCustomFieldClauseQueryFactory(customField, luceneField, jqlSelectOptionsUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil)
        {
            @Override
            void processPositiveNegativeOptionLiterals(final List<QueryLiteral> literals, final List<QueryLiteral> positiveLiterals, final List<QueryLiteral> negativeLiterals)
            {
                positiveLiterals.add(positiveLiteral);
                negativeLiterals.add(negativeLiteral);
            }

            @Override
            BooleanQuery getQueryFromLiterals(final boolean negationOperator, final List<QueryLiteral> literals)
            {
                if (!negationOperator)
                {
                    assertEquals(Collections.singletonList(negativeLiteral), literals);
                    return expectedNegativeResult;
                }
                else
                {
                    assertEquals(Collections.singletonList(positiveLiteral), literals);
                    return expectedPositiveResult;
                }
            }
        };
        mockController.replay();

        final QueryFactoryResult result = factory.getQuery(queryCreationContext, clause);

        assertEquals(expectedResult, result.getLuceneQuery());
        assertFalse(result.mustNotOccur());

        mockController.verify();
    }

    @Test
    public void testGetQueryFromLiteralsNoIdsAndNoEmptyPositive() throws Exception
    {
        final CascadingSelectCustomFieldClauseQueryFactory factory = new CascadingSelectCustomFieldClauseQueryFactory(customField, luceneField, jqlSelectOptionsUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil)
        {
            @Override
            boolean processParentChildOptionLiterals(final List<QueryLiteral> literals, final List<Long> parentIds, final List<Long> childIds)
            {
                return false;
            }
        };

        mockController.replay();

        final BooleanQuery result = factory.getQueryFromLiterals(false, null);

        assertNull(result);

        mockController.verify();
    }

    @Test
    public void testGetQueryFromLiteralsNoIdsAndNoEmptyNegative() throws Exception
    {
        final CascadingSelectCustomFieldClauseQueryFactory factory = new CascadingSelectCustomFieldClauseQueryFactory(customField, luceneField, jqlSelectOptionsUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil)
        {
            @Override
            boolean processParentChildOptionLiterals(final List<QueryLiteral> literals, final List<Long> parentIds, final List<Long> childIds)
            {
                return false;
            }
        };

        mockController.replay();

        final BooleanQuery result = factory.getQueryFromLiterals(true, null);

        assertNull(result);

        mockController.verify();
    }

    @Test
    public void testGetQueryFromLiteralsParentIdAndNoEmptyPositive() throws Exception
    {
        final CascadingSelectCustomFieldClauseQueryFactory factory = new CascadingSelectCustomFieldClauseQueryFactory(customField, luceneField, jqlSelectOptionsUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil)
        {
            @Override
            boolean processParentChildOptionLiterals(final List<QueryLiteral> literals, final List<Long> parentIds, final List<Long> childIds)
            {
                parentIds.add(10L);
                return false;
            }
        };

        mockController.replay();

        final BooleanQuery result = factory.getQueryFromLiterals(false, null);

        final BooleanQuery expectedResult = new BooleanQuery();
        expectedResult.add(new TermQuery(new Term(luceneField, "10")), BooleanClause.Occur.SHOULD);
        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetQueryFromLiteralsChildIdAndNoEmptyPositive() throws Exception
    {
        final CascadingSelectCustomFieldClauseQueryFactory factory = new CascadingSelectCustomFieldClauseQueryFactory(customField, luceneField, jqlSelectOptionsUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil)
        {
            @Override
            boolean processParentChildOptionLiterals(final List<QueryLiteral> literals, final List<Long> parentIds, final List<Long> childIds)
            {
                childIds.add(10L);
                return false;
            }
        };

        mockController.replay();

        final BooleanQuery result = factory.getQueryFromLiterals(false, null);

        final BooleanQuery expectedResult = new BooleanQuery();
        expectedResult.add(new TermQuery(new Term(luceneField + ":1", "10")), BooleanClause.Occur.SHOULD);
        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetQueryFromLiteralsChildIdAndNoEmptyNegative() throws Exception
    {
        final CascadingSelectCustomFieldClauseQueryFactory factory = new CascadingSelectCustomFieldClauseQueryFactory(customField, luceneField, jqlSelectOptionsUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil)
        {
            @Override
            boolean processParentChildOptionLiterals(final List<QueryLiteral> literals, final List<Long> parentIds, final List<Long> childIds)
            {
                childIds.add(10L);
                return false;
            }
        };

        mockController.replay();

        final BooleanQuery result = factory.getQueryFromLiterals(true, null);

        final BooleanQuery expectedResult = new BooleanQuery();
        expectedResult.add(new TermQuery(new Term(luceneField + ":1", "10")), BooleanClause.Occur.MUST_NOT);
        expectedResult.add(createNonEmptyQuery(), BooleanClause.Occur.MUST);
        expectedResult.add(new TermQuery(new Term(DocumentConstants.ISSUE_VISIBLE_FIELD_IDS, luceneField)), BooleanClause.Occur.MUST);
        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetQueryFromLiteralsChildIdAndEmptyPositive() throws Exception
    {
        final CascadingSelectCustomFieldClauseQueryFactory factory = new CascadingSelectCustomFieldClauseQueryFactory(customField, luceneField, jqlSelectOptionsUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil)
        {
            @Override
            boolean processParentChildOptionLiterals(final List<QueryLiteral> literals, final List<Long> parentIds, final List<Long> childIds)
            {
                childIds.add(10L);
                return true;
            }
        };

        mockController.replay();

        final BooleanQuery result = factory.getQueryFromLiterals(false, null);

        final BooleanQuery expectedResult = new BooleanQuery();
        expectedResult.add(new TermQuery(new Term(luceneField + ":1", "10")), BooleanClause.Occur.SHOULD);
        expectedResult.add(createEmptyQuery(), BooleanClause.Occur.SHOULD);
        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetQueryFromLiteralsChildIdAndEmptyNegative() throws Exception
    {
        final CascadingSelectCustomFieldClauseQueryFactory factory = new CascadingSelectCustomFieldClauseQueryFactory(customField, luceneField, jqlSelectOptionsUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil)
        {
            @Override
            boolean processParentChildOptionLiterals(final List<QueryLiteral> literals, final List<Long> parentIds, final List<Long> childIds)
            {
                childIds.add(10L);
                return true;
            }
        };

        mockController.replay();

        final BooleanQuery result = factory.getQueryFromLiterals(true, null);

        final BooleanQuery expectedResult = new BooleanQuery();
        expectedResult.add(new TermQuery(new Term(luceneField + ":1", "10")), BooleanClause.Occur.MUST_NOT);
        expectedResult.add(createNonEmptyQuery(), BooleanClause.Occur.MUST);
        expectedResult.add(new TermQuery(new Term(DocumentConstants.ISSUE_VISIBLE_FIELD_IDS, luceneField)), BooleanClause.Occur.MUST);
        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetQueryFromLiteralsOnlyEmptyPositive() throws Exception
    {
        final CascadingSelectCustomFieldClauseQueryFactory factory = new CascadingSelectCustomFieldClauseQueryFactory(customField, luceneField, jqlSelectOptionsUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil)
        {
            @Override
            boolean processParentChildOptionLiterals(final List<QueryLiteral> literals, final List<Long> parentIds, final List<Long> childIds)
            {
                return true;
            }
        };

        mockController.replay();

        final BooleanQuery result = factory.getQueryFromLiterals(false, null);

        final Query expectedResult = createEmptyQuery();
        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetQueryFromLiteralsOnlyEmptyNegative() throws Exception
    {
        final CascadingSelectCustomFieldClauseQueryFactory factory = new CascadingSelectCustomFieldClauseQueryFactory(customField, luceneField, jqlSelectOptionsUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil)
        {
            @Override
            boolean processParentChildOptionLiterals(final List<QueryLiteral> literals, final List<Long> parentIds, final List<Long> childIds)
            {
                return true;
            }
        };

        mockController.replay();

        final BooleanQuery result = factory.getQueryFromLiterals(true, null);

        final Query expectedResult = createNonEmptyQuery();
        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetQueryFromLiteralsParentAndChildPositive() throws Exception
    {
        final CascadingSelectCustomFieldClauseQueryFactory factory = new CascadingSelectCustomFieldClauseQueryFactory(customField, luceneField, jqlSelectOptionsUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil)
        {
            @Override
            boolean processParentChildOptionLiterals(final List<QueryLiteral> literals, final List<Long> parentIds, final List<Long> childIds)
            {
                parentIds.add(10L);
                childIds.add(30L);
                return false;
            }
        };

        mockController.replay();

        final BooleanQuery result = factory.getQueryFromLiterals(false, null);

        final BooleanQuery expectedResult = new BooleanQuery();
        expectedResult.add(new TermQuery(new Term(luceneField, "10")), BooleanClause.Occur.SHOULD);
        expectedResult.add(new TermQuery(new Term(luceneField + ":1", "30")), BooleanClause.Occur.SHOULD);
        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetQueryFromLiteralsParentAndChildNegative() throws Exception
    {
        final CascadingSelectCustomFieldClauseQueryFactory factory = new CascadingSelectCustomFieldClauseQueryFactory(customField, luceneField, jqlSelectOptionsUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil)
        {
            @Override
            boolean processParentChildOptionLiterals(final List<QueryLiteral> literals, final List<Long> parentIds, final List<Long> childIds)
            {
                parentIds.add(10L);
                childIds.add(30L);
                return false;
            }
        };

        mockController.replay();

        final BooleanQuery result = factory.getQueryFromLiterals(true, null);

        final BooleanQuery expectedResult = new BooleanQuery();
        expectedResult.add(new TermQuery(new Term(luceneField, "10")), BooleanClause.Occur.MUST_NOT);
        expectedResult.add(new TermQuery(new Term(luceneField + ":1", "30")), BooleanClause.Occur.MUST_NOT);
        expectedResult.add(createNonEmptyQuery(), BooleanClause.Occur.MUST);
        expectedResult.add(new TermQuery(new Term(DocumentConstants.ISSUE_VISIBLE_FIELD_IDS, luceneField)), BooleanClause.Occur.MUST);
        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testProcessOptionLiteralsOneChildOneParent() throws Exception
    {
        final QueryLiteral literal1 = createLiteral(10L);
        final QueryLiteral literal2 = createLiteral(20L);
        final List<QueryLiteral> literals = CollectionBuilder.newBuilder(literal1, literal2).asList();
        Operand operand = new MultiValueOperand(literal1, literal2);
        final TerminalClause clause = new TerminalClauseImpl(field, Operator.EQUALS, operand);

        List<Long> parents = new ArrayList<Long>();
        List<Long> children = new ArrayList<Long>();

        final MockOption parentOption = new MockOption(null, null, null, null, null, 10L);
        final MockOption childOption = new MockOption(parentOption, null, null, null, null, 20L);

        jqlSelectOptionsUtil.getOptions(customField, literal1, true);
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder(parentOption).asList());

        jqlSelectOptionsUtil.getOptions(customField, literal2, true);
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder(childOption).asList());

        mockController.replay();
        final CascadingSelectCustomFieldClauseQueryFactory factory = new CascadingSelectCustomFieldClauseQueryFactory(customField, luceneField, jqlSelectOptionsUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil);
        assertFalse(factory.processParentChildOptionLiterals(literals, parents, children));

        assertEquals(1, parents.size());
        assertTrue(parents.contains(10L));

        assertEquals(1, children.size());
        assertTrue(children.contains(20L));

        mockController.verify();

    }

    @Test
    public void testProcessOptionLiteralsOneChildOneParentAndEmpty() throws Exception
    {
        final QueryLiteral literal1 = createLiteral(10L);
        final QueryLiteral literal2 = createLiteral(20L);
        final List<QueryLiteral> literals = CollectionBuilder.newBuilder(literal1, literal2).asList();
        Operand operand = new MultiValueOperand(literal1, literal2);
        final TerminalClause clause = new TerminalClauseImpl(field, Operator.EQUALS, operand);

        List<Long> parents = new ArrayList<Long>();
        List<Long> children = new ArrayList<Long>();

        final MockOption parentOption = new MockOption(null, null, null, null, null, 10L);
        final MockOption childOption = new MockOption(parentOption, null, null, null, null, 20L);

        jqlSelectOptionsUtil.getOptions(customField, literal1, true);
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder(parentOption, null).asList());

        jqlSelectOptionsUtil.getOptions(customField, literal2, true);
        mockController.setReturnValue(CollectionBuilder.<Option>newBuilder(childOption).asList());

        mockController.replay();
        final CascadingSelectCustomFieldClauseQueryFactory factory = new CascadingSelectCustomFieldClauseQueryFactory(customField, luceneField, jqlSelectOptionsUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil);
        assertTrue(factory.processParentChildOptionLiterals(literals, parents, children));

        assertEquals(1, parents.size());
        assertTrue(parents.contains(10L));

        assertEquals(1, children.size());
        assertTrue(children.contains(20L));

        mockController.verify();
    }

    @Test
    public void testProcessOptionLiteralsNoLiterals() throws Exception
    {
        final List<QueryLiteral> literals = CollectionBuilder.<QueryLiteral>newBuilder().asList();

        List<Long> parents = new ArrayList<Long>();
        List<Long> children = new ArrayList<Long>();

        mockController.replay();

        final CascadingSelectCustomFieldClauseQueryFactory factory = new CascadingSelectCustomFieldClauseQueryFactory(customField, luceneField, jqlSelectOptionsUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil);

        assertFalse(factory.processParentChildOptionLiterals(literals, parents, children));

        assertEquals(0, parents.size());
        assertEquals(0, children.size());

        mockController.verify();
    }

    private BooleanQuery createNonEmptyQuery()
    {
        final BooleanQuery query = new BooleanQuery();
        query.add(new TermQuery(new Term(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, luceneField)), BooleanClause.Occur.SHOULD);
        query.add(new TermQuery(new Term(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, luceneField + ":1")), BooleanClause.Occur.SHOULD);
        return query;
    }

    private BooleanQuery createEmptyQuery()
    {
        final BooleanQuery query = new BooleanQuery();
        final TermQuery parentEmptyQuery = new TermQuery(new Term(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, luceneField));
        final TermQuery parentVisibiltyQuery = new TermQuery(new Term(DocumentConstants.ISSUE_VISIBLE_FIELD_IDS, luceneField));
        final BooleanQuery parentQuery = new BooleanQuery();
        parentQuery.add(parentEmptyQuery, BooleanClause.Occur.MUST_NOT);
        parentQuery.add(parentVisibiltyQuery, BooleanClause.Occur.MUST);
        query.add(parentQuery, BooleanClause.Occur.MUST);
        query.add(new TermQuery(new Term(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, luceneField + ":1")), BooleanClause.Occur.MUST_NOT);
        return query;
    }
}
