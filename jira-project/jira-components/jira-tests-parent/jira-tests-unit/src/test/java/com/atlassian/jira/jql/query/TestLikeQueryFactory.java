package com.atlassian.jira.jql.query;

import java.util.Collections;

import com.atlassian.jira.index.LuceneVersion;
import com.atlassian.jira.issue.index.analyzer.EnglishAnalyzer;
import com.atlassian.jira.issue.index.analyzer.TokenFilters;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.query.lucene.parsing.DefaultLuceneQueryParserFactory;
import com.atlassian.query.lucene.parsing.LuceneQueryParserFactory;
import com.atlassian.query.operator.Operator;

import com.google.common.collect.Lists;

import org.apache.lucene.queryParser.QueryParser;
import org.junit.Test;
import org.mockito.Mockito;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
public class TestLikeQueryFactory
{
    @Test
    public void testCreateQueryNullRawValues() throws Exception
    {
            final LuceneQueryParserFactory luceneQueryParserFactory = Mockito.mock(LuceneQueryParserFactory.class);
            when(luceneQueryParserFactory.createParserFor("testField")).thenReturn(new DefaultLuceneQueryParserFactory().createParserFor("testField"));

            final LikeQueryFactory likeQueryFactory = new LikeQueryFactory()
            {
                @Override
                QueryParser getQueryParser(final String fieldName)
                {
                    return luceneQueryParserFactory.createParserFor(fieldName);
                }
            };

            final QueryFactoryResult result = likeQueryFactory.createQueryForSingleValue("testField", Operator.LIKE, null);
            assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testCreateQueryEmptyRawValues() throws Exception
    {
        final LuceneQueryParserFactory luceneQueryParserFactory = Mockito.mock(LuceneQueryParserFactory.class);
        when(luceneQueryParserFactory.createParserFor("testField")).thenReturn(new DefaultLuceneQueryParserFactory().createParserFor("testField"));

        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory()
        {
            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return luceneQueryParserFactory.createParserFor(fieldName);
            }
        };

        final QueryFactoryResult result = likeQueryFactory.createQueryForSingleValue("testField", Operator.LIKE, Collections.<QueryLiteral>emptyList());
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testCreateQueryEmptyString() throws Exception
    {
        final LuceneQueryParserFactory luceneQueryParserFactory = Mockito.mock(LuceneQueryParserFactory.class);
        when(luceneQueryParserFactory.createParserFor("testField")).thenReturn(new DefaultLuceneQueryParserFactory().createParserFor("testField"));

        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory()
        {
            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return luceneQueryParserFactory.createParserFor(fieldName);
            }
        };

        final QueryFactoryResult result = likeQueryFactory.createQueryForSingleValue("testField", Operator.LIKE, Lists.newArrayList(createLiteral("")));
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testCreateQueryBlankString() throws Exception
    {
        final LuceneQueryParserFactory luceneQueryParserFactory = Mockito.mock(LuceneQueryParserFactory.class);
        when(luceneQueryParserFactory.createParserFor("testField")).thenReturn(new DefaultLuceneQueryParserFactory().createParserFor("testField"));

        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory()
        {
            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return luceneQueryParserFactory.createParserFor(fieldName);
            }
        };

        final QueryFactoryResult result = likeQueryFactory.createQueryForSingleValue("testField", Operator.LIKE, Lists.newArrayList(createLiteral("  ")));
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testCreateQueryHappyPathLong() throws Exception
    {
        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory()
        {
            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return new QueryParser(LuceneVersion.get(), fieldName, new EnglishAnalyzer(LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(), TokenFilters.English.StopWordRemoval.defaultSet()));
            }
        };

        final QueryFactoryResult result = likeQueryFactory.createQueryForSingleValue("test", Operator.LIKE, Lists.newArrayList(createLiteral(12L)));
        assertFalse(result.mustNotOccur());
        assertEquals("+test:12 +nonemptyfieldids:test +visiblefieldids:test", result.getLuceneQuery().toString());
    }

    @Test
    public void testCreateQueryForMultipleValuesPositive() throws Exception
    {
        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory(false)
        {
            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return new QueryParser(LuceneVersion.get(), fieldName, new EnglishAnalyzer(LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(), TokenFilters.English.StopWordRemoval.defaultSet()));
            }
        };

        final QueryFactoryResult result = likeQueryFactory.createQueryForSingleValue("test", Operator.LIKE, Lists.newArrayList(createLiteral(12L), createLiteral("blah")));
        assertFalse(result.mustNotOccur());
        assertEquals("+(test:12 test:blah)", result.getLuceneQuery().toString());
    }

    @Test
    public void testCreateQueryForMultipleValuesNegative() throws Exception
    {
        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory(false)
        {
            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return new QueryParser(LuceneVersion.get(), fieldName, new EnglishAnalyzer(LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(), TokenFilters.English.StopWordRemoval.defaultSet()));
            }
        };

        final QueryFactoryResult result = likeQueryFactory.createQueryForSingleValue("test", Operator.NOT_LIKE, Lists.newArrayList(createLiteral(12L), createLiteral("blah")));
        assertFalse(result.mustNotOccur());
        assertEquals("+(-test:12 -test:blah)", result.getLuceneQuery().toString());
    }

    @Test
    public void testCreateQueryForMultipleValuesPositiveWithEmpty() throws Exception
    {
        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory(true)
        {
            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return new QueryParser(LuceneVersion.get(), fieldName, new EnglishAnalyzer(LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(), TokenFilters.English.StopWordRemoval.defaultSet()));
            }
        };

        final QueryFactoryResult result = likeQueryFactory.createQueryForSingleValue("test", Operator.LIKE, Lists.newArrayList(createLiteral(12L), new QueryLiteral()));
        assertFalse(result.mustNotOccur());
        assertEquals("+(test:12 (-nonemptyfieldids:test +visiblefieldids:test))", result.getLuceneQuery().toString());
    }

    @Test
    public void testCreateQueryForMultipleValuesNegativeWithEmpty() throws Exception
    {
        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory(true)
        {
            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return new QueryParser(LuceneVersion.get(), fieldName, new EnglishAnalyzer(LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(), TokenFilters.English.StopWordRemoval.defaultSet()));
            }
        };

        final QueryFactoryResult result = likeQueryFactory.createQueryForSingleValue("test", Operator.NOT_LIKE, Lists.newArrayList(createLiteral(12L), new QueryLiteral()));
        assertFalse(result.mustNotOccur());
        assertEquals("+(-test:12 +nonemptyfieldids:test)", result.getLuceneQuery().toString());
    }

    @Test
    public void testCreateQueryForMultipleValuesPositiveWithEmptyButDoesntHandle() throws Exception
    {
        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory(false)
        {
            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return new QueryParser(LuceneVersion.get(), fieldName, new EnglishAnalyzer(LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(), TokenFilters.English.StopWordRemoval.defaultSet()));
            }
        };

        final QueryFactoryResult result = likeQueryFactory.createQueryForSingleValue("test", Operator.LIKE, Lists.newArrayList(createLiteral(12L), new QueryLiteral()));
        assertFalse(result.mustNotOccur());
        assertEquals("+(test:12)", result.getLuceneQuery().toString());
    }

    @Test
    public void testCreateQueryForMultipleValuesNegativeWithEmptyButDoesntHandle() throws Exception
    {
        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory(false)
        {
            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return new QueryParser(LuceneVersion.get(), fieldName, new EnglishAnalyzer(LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(), TokenFilters.English.StopWordRemoval.defaultSet()));
            }
        };

        final QueryFactoryResult result = likeQueryFactory.createQueryForSingleValue("test", Operator.NOT_LIKE, Lists.newArrayList(createLiteral(12L), new QueryLiteral()));
        assertFalse(result.mustNotOccur());
        assertEquals("+(-test:12)", result.getLuceneQuery().toString());
    }

    @Test
    public void testCreateQueryHappyPathDoNotExcludeEmpties() throws Exception
    {
        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory(false)
        {
            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return new QueryParser(LuceneVersion.get(), fieldName, new EnglishAnalyzer(LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(), TokenFilters.English.StopWordRemoval.defaultSet()));
            }
        };

        final QueryFactoryResult result = likeQueryFactory.createQueryForSingleValue("test", Operator.LIKE, Lists.newArrayList(createLiteral(12L)));
        assertFalse(result.mustNotOccur());
        assertEquals("+test:12", result.getLuceneQuery().toString());
    }

    @Test
    public void testCreateQueryHappyPathString() throws Exception
    {
        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory()
        {
            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return new QueryParser(LuceneVersion.get(), fieldName, new EnglishAnalyzer(LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(), TokenFilters.English.StopWordRemoval.defaultSet()));
            }
        };

        final QueryFactoryResult result = likeQueryFactory.createQueryForSingleValue("test", Operator.LIKE, Lists.newArrayList(createLiteral("dude")));
        assertFalse(result.mustNotOccur());
        assertEquals("+test:dude +nonemptyfieldids:test +visiblefieldids:test", result.getLuceneQuery().toString());
    }

    @Test
    public void testCreateQueryHappyPathComplexString() throws Exception
    {
        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory()
        {
            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return new QueryParser(LuceneVersion.get(), fieldName, new EnglishAnalyzer(LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(), TokenFilters.English.StopWordRemoval.defaultSet()));
            }
        };

        final QueryFactoryResult result = likeQueryFactory.createQueryForSingleValue("test", Operator.LIKE, Lists.newArrayList(createLiteral("-dude +cool stuff")));
        assertFalse(result.mustNotOccur());
        assertEquals("+(-test:dude +test:cool +test:stuff) +nonemptyfieldids:test +visiblefieldids:test", result.getLuceneQuery().toString());
    }

    @Test
    public void testCreateQueryHappyPathComplexStringWithEscaping() throws Exception
    {
        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory()
        {
            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return new QueryParser(LuceneVersion.get(), fieldName, new EnglishAnalyzer(LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(), TokenFilters.English.StopWordRemoval.defaultSet()));
            }
        };

        final QueryFactoryResult result = likeQueryFactory.createQueryForSingleValue("test", Operator.LIKE, Lists.newArrayList(createLiteral("-dude:1 +\"cool 2\" stuff\\3")));
        assertFalse(result.mustNotOccur());
        assertEquals("+(-test:\"dude 1\" +test:\"cool 2\" +test:\"stuff 3\") +nonemptyfieldids:test +visiblefieldids:test", result.getLuceneQuery().toString());
    }

    @Test
    public void testCreateQueryQueryWithColon() throws Exception
    {
        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory()
        {
            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return new QueryParser(LuceneVersion.get(), fieldName, new EnglishAnalyzer(LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(), TokenFilters.English.StopWordRemoval.defaultSet()));
            }
        };

        final QueryFactoryResult result = likeQueryFactory.createQueryForSingleValue("test", Operator.LIKE, Lists.newArrayList(createLiteral("-field:dude")));
        assertFalse(result.mustNotOccur());
        assertEquals("+(-test:\"field dude\") +nonemptyfieldids:test +visiblefieldids:test", result.getLuceneQuery().toString());
    }

    @Test
    public void testCreateNotQueryHappyPathLong() throws Exception
    {
        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory()
        {
            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return new QueryParser(LuceneVersion.get(), fieldName, new EnglishAnalyzer(LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(), TokenFilters.English.StopWordRemoval.defaultSet()));
            }
        };

        final QueryFactoryResult result = likeQueryFactory.createQueryForSingleValue("test", Operator.NOT_LIKE, Lists.newArrayList(createLiteral(12L)));
        assertFalse(result.mustNotOccur());
        assertEquals("-test:12 +nonemptyfieldids:test +visiblefieldids:test", result.getLuceneQuery().toString());
    }

    @Test
    public void testCreateNotQueryHappyPathString() throws Exception
    {
        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory()
        {
            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return new QueryParser(LuceneVersion.get(), fieldName, new EnglishAnalyzer(LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(), TokenFilters.English.StopWordRemoval.defaultSet()));
            }
        };

        final QueryFactoryResult result = likeQueryFactory.createQueryForSingleValue("test", Operator.NOT_LIKE, Lists.newArrayList(createLiteral("dude")));
        assertFalse(result.mustNotOccur());
        assertEquals("-test:dude +nonemptyfieldids:test +visiblefieldids:test", result.getLuceneQuery().toString());
    }

    @Test
    public void testCreateNotQueryHappyPathComplexString() throws Exception
    {
        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory()
        {
            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return new QueryParser(LuceneVersion.get(), fieldName, new EnglishAnalyzer(LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(), TokenFilters.English.StopWordRemoval.defaultSet()));
            }
        };

        final QueryFactoryResult result = likeQueryFactory.createQueryForSingleValue("test", Operator.NOT_LIKE, Lists.newArrayList(createLiteral("-dude +cool stuff")));
        assertFalse(result.mustNotOccur());
        assertEquals("-(-test:dude +test:cool +test:stuff) +nonemptyfieldids:test +visiblefieldids:test", result.getLuceneQuery().toString());
    }

    @Test
    public void testCreateNotQueryHappyPathComplexStringWithEscaping() throws Exception
    {
        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory()
        {
            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return new QueryParser(LuceneVersion.get(), fieldName, new EnglishAnalyzer(LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(), TokenFilters.English.StopWordRemoval.defaultSet()));
            }
        };

        final QueryFactoryResult result = likeQueryFactory.createQueryForSingleValue("test", Operator.NOT_LIKE, Lists.newArrayList(createLiteral("-dude:1 +\"cool 2\" stuff\\3")));
        assertFalse(result.mustNotOccur());
        assertEquals("-(-test:\"dude 1\" +test:\"cool 2\" +test:\"stuff 3\") +nonemptyfieldids:test +visiblefieldids:test", result.getLuceneQuery().toString());
    }

    @Test
    public void testCreateNotQueryQueryWithColon() throws Exception
    {
        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory()
        {
            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return new QueryParser(LuceneVersion.get(), fieldName, new EnglishAnalyzer(LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(), TokenFilters.English.StopWordRemoval.defaultSet()));
            }
        };

        final QueryFactoryResult result = likeQueryFactory.createQueryForSingleValue("test", Operator.NOT_LIKE, Lists.newArrayList(createLiteral("-field:dude")));
        assertFalse(result.mustNotOccur());
        assertEquals("-(-test:\"field dude\") +nonemptyfieldids:test +visiblefieldids:test", result.getLuceneQuery().toString());
    }

    @Test
    public void testCreateQueryQueryInvalidQuery() throws Exception
    {
        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory()
        {
            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return new QueryParser(LuceneVersion.get(), fieldName, new EnglishAnalyzer(LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(), TokenFilters.English.StopWordRemoval.defaultSet()));
            }
        };

        final QueryFactoryResult result = likeQueryFactory.createQueryForSingleValue("test", Operator.LIKE, Lists.newArrayList(createLiteral("^field:-dude")));
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testCreateQueryQueryInvalidQueryBadFuzzy() throws Exception
    {
        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory()
        {
            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return new QueryParser(LuceneVersion.get(), fieldName, new EnglishAnalyzer(LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(), TokenFilters.English.StopWordRemoval.defaultSet()));
            }
        };

        final QueryFactoryResult result = likeQueryFactory.createQueryForSingleValue("test", Operator.LIKE, Lists.newArrayList(createLiteral("dude~1")));
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testCreateQueryForSingleValueReturnsFalseResultForOperator() throws Exception
    {
        createQuerySingleValueReturnsFalseResultForOperator(Operator.GREATER_THAN_EQUALS);
        createQuerySingleValueReturnsFalseResultForOperator(Operator.GREATER_THAN);
        createQuerySingleValueReturnsFalseResultForOperator(Operator.LESS_THAN);
        createQuerySingleValueReturnsFalseResultForOperator(Operator.LESS_THAN_EQUALS);
        createQuerySingleValueReturnsFalseResultForOperator(Operator.IN);
        createQuerySingleValueReturnsFalseResultForOperator(Operator.NOT_IN);
        createQuerySingleValueReturnsFalseResultForOperator(Operator.IS);
        createQuerySingleValueReturnsFalseResultForOperator(Operator.IS_NOT);
        createQuerySingleValueReturnsFalseResultForOperator(Operator.EQUALS);
        createQuerySingleValueReturnsFalseResultForOperator(Operator.NOT_EQUALS);
    }

    @Test
    public void testCreateQueryForMultipleValuesReturnsFalseResultForOperator() throws Exception
    {
        createQueryMultiplValuesReturnsFalseResultForOperator(Operator.LESS_THAN);
        createQueryMultiplValuesReturnsFalseResultForOperator(Operator.LESS_THAN_EQUALS);
        createQueryMultiplValuesReturnsFalseResultForOperator(Operator.GREATER_THAN);
        createQueryMultiplValuesReturnsFalseResultForOperator(Operator.GREATER_THAN_EQUALS);
        createQueryMultiplValuesReturnsFalseResultForOperator(Operator.EQUALS);
        createQueryMultiplValuesReturnsFalseResultForOperator(Operator.NOT_EQUALS);
        createQueryMultiplValuesReturnsFalseResultForOperator(Operator.IS);
        createQueryMultiplValuesReturnsFalseResultForOperator(Operator.IS_NOT);
        createQueryMultiplValuesReturnsFalseResultForOperator(Operator.IN);
        createQueryMultiplValuesReturnsFalseResultForOperator(Operator.NOT_IN);

        createQueryMultiplValuesReturnsFalseResultForOperator(Operator.LIKE);
        createQueryMultiplValuesReturnsFalseResultForOperator(Operator.NOT_LIKE);
    }

    @Test
    public void testHandlesOperator() throws Exception
    {
        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory();
        assertFalse(likeQueryFactory.handlesOperator(Operator.GREATER_THAN));
        assertFalse(likeQueryFactory.handlesOperator(Operator.GREATER_THAN_EQUALS));
        assertFalse(likeQueryFactory.handlesOperator(Operator.LESS_THAN));
        assertFalse(likeQueryFactory.handlesOperator(Operator.LESS_THAN_EQUALS));
        assertFalse(likeQueryFactory.handlesOperator(Operator.EQUALS));
        assertFalse(likeQueryFactory.handlesOperator(Operator.NOT_EQUALS));
        assertFalse(likeQueryFactory.handlesOperator(Operator.IN));
        assertFalse(likeQueryFactory.handlesOperator(Operator.NOT_IN));
        assertTrue(likeQueryFactory.handlesOperator(Operator.LIKE));
        assertTrue(likeQueryFactory.handlesOperator(Operator.NOT_LIKE));
        assertTrue(likeQueryFactory.handlesOperator(Operator.IS));
        assertTrue(likeQueryFactory.handlesOperator(Operator.IS_NOT));
    }

    @Test
    public void testCreateQueryForMultipleValuesForLike() throws Exception
    {
        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory();
        final QueryFactoryResult result = likeQueryFactory.createQueryForMultipleValues("test", Operator.LIKE, Collections.<QueryLiteral>emptyList());
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    @Test
    public void testCreateQueryForSingleValueEmptyLiteralSameAsEmptyQuery() throws Exception
    {
        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory()
        {
            @Override
            QueryParser getQueryParser(final String fieldName)
            {
                return new QueryParser(LuceneVersion.get(), fieldName, new EnglishAnalyzer(LuceneVersion.get(), false, TokenFilters.English.Stemming.aggressive(), TokenFilters.English.StopWordRemoval.defaultSet()));
            }
        };

        final QueryFactoryResult forSingleValue = likeQueryFactory.createQueryForSingleValue("test", Operator.LIKE, Lists.newArrayList(new QueryLiteral()));
        final QueryFactoryResult forEmptyOperand = likeQueryFactory.createQueryForEmptyOperand("test", Operator.LIKE);

        assertEquals(forEmptyOperand, forSingleValue);
    }

    @Test
    public void testCreateQueryForEmptyOperand() throws Exception
    {
        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory();
        QueryFactoryResult emptyQuery = likeQueryFactory.createQueryForEmptyOperand("test", Operator.LIKE);
        assertFalse(emptyQuery.mustNotOccur());
        assertEquals("-nonemptyfieldids:test +visiblefieldids:test", emptyQuery.getLuceneQuery().toString());
        emptyQuery = likeQueryFactory.createQueryForEmptyOperand("test", Operator.IS);
        assertFalse(emptyQuery.mustNotOccur());
        assertEquals("-nonemptyfieldids:test +visiblefieldids:test", emptyQuery.getLuceneQuery().toString());
    }

    @Test
    public void testCreateQueryForEmptyOperandNotEmptyOperators() throws Exception
    {
        final LikeQueryFactory likeQueryFactory = new LikeQueryFactory();
        QueryFactoryResult emptyQuery = likeQueryFactory.createQueryForEmptyOperand("test", Operator.NOT_LIKE);
        assertFalse(emptyQuery.mustNotOccur());
        assertEquals("nonemptyfieldids:test", emptyQuery.getLuceneQuery().toString());
        emptyQuery = likeQueryFactory.createQueryForEmptyOperand("test", Operator.IS_NOT);
        assertFalse(emptyQuery.mustNotOccur());
        assertEquals("nonemptyfieldids:test", emptyQuery.getLuceneQuery().toString());
    }

    @Test
    public void testCreateQueryForEmptyOperandBadOperator() throws Exception
    {
        _testCreateQueryForEmptyOperandBadOperator(Operator.EQUALS);
        _testCreateQueryForEmptyOperandBadOperator(Operator.NOT_EQUALS);
        _testCreateQueryForEmptyOperandBadOperator(Operator.IN);
        _testCreateQueryForEmptyOperandBadOperator(Operator.NOT_IN);
        _testCreateQueryForEmptyOperandBadOperator(Operator.GREATER_THAN);
        _testCreateQueryForEmptyOperandBadOperator(Operator.GREATER_THAN_EQUALS);
        _testCreateQueryForEmptyOperandBadOperator(Operator.LESS_THAN);
        _testCreateQueryForEmptyOperandBadOperator(Operator.LESS_THAN_EQUALS);
    }

    public void _testCreateQueryForEmptyOperandBadOperator(Operator operator) throws Exception
    {
        final QueryFactoryResult result = new LikeQueryFactory().createQueryForEmptyOperand("test", operator);
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    private void createQuerySingleValueReturnsFalseResultForOperator(final Operator operator)
    {
        final QueryFactoryResult result = new LikeQueryFactory().createQueryForSingleValue("test", operator, Collections.<QueryLiteral>emptyList());
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }

    private void createQueryMultiplValuesReturnsFalseResultForOperator(final Operator operator)
    {
        final QueryFactoryResult result = new LikeQueryFactory().createQueryForMultipleValues("test", operator, Collections.<QueryLiteral>emptyList());
        assertEquals(QueryFactoryResult.createFalseResult(), result);
    }
}
