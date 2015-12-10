package com.atlassian.jira.jql.query;

import java.util.Collections;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.IdentityIndexInfoResolver;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.query.operator.Operator;

import org.apache.lucene.search.BooleanQuery;
import org.easymock.classextension.EasyMock;
import org.junit.Test;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * @since v4.0
 */
public class TestRelationalOperatorMutatedIndexValueQueryFactory extends MockControllerTestCase
{
    private static final String FIELD_NAME = "integerField";

    @Test
    public void testDoesNotSupportOperators() throws Exception
    {
        final RelationalOperatorMutatedIndexValueQueryFactory factory = mockController.instantiate(RelationalOperatorMutatedIndexValueQueryFactory.class);

        _testDoesNotSupportOperator(factory, Operator.EQUALS);
        _testDoesNotSupportOperator(factory, Operator.NOT_EQUALS);
        _testDoesNotSupportOperator(factory, Operator.LIKE);
        _testDoesNotSupportOperator(factory, Operator.IN);
        _testDoesNotSupportOperator(factory, Operator.IS);
    }

    private void _testDoesNotSupportOperator(final RelationalOperatorMutatedIndexValueQueryFactory factory, final Operator operator) throws Exception
    {
        final QueryFactoryResult result = factory.createQueryForSingleValue("testField", operator, Collections.singletonList(createLiteral(1000L)));
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testCreateForMultipleValues() throws Exception
    {
        final RelationalOperatorMutatedIndexValueQueryFactory factory = mockController.instantiate(RelationalOperatorMutatedIndexValueQueryFactory.class);

        final QueryFactoryResult result = factory.createQueryForMultipleValues("testField", Operator.IN, Collections.singletonList(createLiteral(1000L)));
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testCreateForEmptyOperand() throws Exception
    {
        final RelationalOperatorMutatedIndexValueQueryFactory factory = mockController.instantiate(RelationalOperatorMutatedIndexValueQueryFactory.class);

        final QueryFactoryResult result = factory.createQueryForEmptyOperand("testField", Operator.IN);
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testCreateForSingleValueEmptyLiterals() throws Exception
    {
        assertQueryHappy(Operator.LESS_THAN, "", new QueryLiteral());
    }

    @Test
    public void testCreateForSingleValueNoLiterals() throws Exception
    {
        final IndexInfoResolver<?> indexInfoResolver = new IdentityIndexInfoResolver();
        final RelationalOperatorMutatedIndexValueQueryFactory factory = new RelationalOperatorMutatedIndexValueQueryFactory(indexInfoResolver);

        final QueryFactoryResult query = factory.createQueryForSingleValue(FIELD_NAME, Operator.LESS_THAN, Collections.<QueryLiteral>emptyList());
        assertFalse(query.mustNotOccur());
        assertNotNull(query.getLuceneQuery());
        assertEquals(new BooleanQuery(), query.getLuceneQuery());
    }

    @Test
    public void testCreateForSingleValueDoesntResolve() throws Exception
    {
        final IndexInfoResolver<?> indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        EasyMock.expect(indexInfoResolver.getIndexedValues("doesntresolve")).andReturn(Collections.<String>emptyList());

        replay();

        final RelationalOperatorMutatedIndexValueQueryFactory factory = new RelationalOperatorMutatedIndexValueQueryFactory(indexInfoResolver);

        final QueryFactoryResult query = factory.createQueryForSingleValue(FIELD_NAME, Operator.LESS_THAN, Collections.singletonList(createLiteral("doesntresolve")));

        assertFalse(query.mustNotOccur());
        assertNotNull(query.getLuceneQuery());
        assertEquals(new BooleanQuery(), query.getLuceneQuery());

        verify();
    }

    @Test
    public void testLessThan() throws Exception
    {
        assertQueryHappy(Operator.LESS_THAN, "integerField:[* TO 999}", 999L);
        assertQueryHappy(Operator.LESS_THAN, "integerField:[* TO 999}", "999");
        assertQueryHappy(Operator.LESS_THAN, "integerField:[* TO -999}", -999L);
        assertQueryHappy(Operator.LESS_THAN, "integerField:[* TO -999}", "-999");
    }

    @Test
    public void testLessThanEquals() throws Exception
    {
        assertQueryHappy(Operator.LESS_THAN_EQUALS, "integerField:[* TO 999]", 999L);
        assertQueryHappy(Operator.LESS_THAN_EQUALS, "integerField:[* TO 999]", "999");
        assertQueryHappy(Operator.LESS_THAN_EQUALS, "integerField:[* TO -999]", -999L);
        assertQueryHappy(Operator.LESS_THAN_EQUALS, "integerField:[* TO -999]", "-999");
    }

    @Test
    public void testGreaterThan() throws Exception
    {
        assertQueryHappy(Operator.GREATER_THAN, "integerField:{999 TO *]", 999L);
        assertQueryHappy(Operator.GREATER_THAN, "integerField:{999 TO *]", "999");
        assertQueryHappy(Operator.GREATER_THAN, "integerField:{-999 TO *]", -999L);
        assertQueryHappy(Operator.GREATER_THAN, "integerField:{-999 TO *]", "-999");
    }

    @Test
    public void testGreaterThanEquals() throws Exception
    {
        assertQueryHappy(Operator.GREATER_THAN_EQUALS, "integerField:[999 TO *]", 999L);
        assertQueryHappy(Operator.GREATER_THAN_EQUALS, "integerField:[999 TO *]", "999");
        assertQueryHappy(Operator.GREATER_THAN_EQUALS, "integerField:[-999 TO *]", -999L);
        assertQueryHappy(Operator.GREATER_THAN_EQUALS, "integerField:[-999 TO *]", "-999");
    }

    private void assertQueryHappy(final Operator op, final String luceneQuery, final Object value)
    {
        final IndexInfoResolver<?> indexInfoResolver = new IdentityIndexInfoResolver();
        final RelationalOperatorMutatedIndexValueQueryFactory factory = new RelationalOperatorMutatedIndexValueQueryFactory(indexInfoResolver);

        final QueryFactoryResult query = factory.createQueryForSingleValue(FIELD_NAME, op, Collections.singletonList(createQL(value)));
        assertFalse(query.mustNotOccur());
        assertNotNull(query.getLuceneQuery());
        assertEquals(luceneQuery, query.getLuceneQuery().toString(""));
    }

    private QueryLiteral createQL(final Object value)
    {
        if (value instanceof String)
        {
            return createLiteral((String) value);
        }
        else if (value instanceof Long)
        {
            return createLiteral((Long) value);
        }
        else if (value instanceof QueryLiteral)
        {
            return (QueryLiteral) value;
        }
        throw new IllegalArgumentException();
    }
}
