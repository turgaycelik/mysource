package com.atlassian.jira.jql.query;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @since v4.0
 */
public class TestQueryFactoryResult
{
    @Test
    public void testWrapWithVisibilityQueryFalseResult()
    {
        final QueryFactoryResult input = QueryFactoryResult.createFalseResult();
        final QueryFactoryResult result = QueryFactoryResult.wrapWithVisibilityQuery("test", input);
        final Query expectedQuery = new BooleanQuery();

        assertEquals(expectedQuery, result.getLuceneQuery());
        assertFalse(result.mustNotOccur());
    }

    @Test
    public void testWrapWithVisibilityQueryTermQuery()
    {
        final TermQuery termQuery = new TermQuery(new Term("test", "123"));
        final QueryFactoryResult input = new QueryFactoryResult(termQuery);
        final QueryFactoryResult result = QueryFactoryResult.wrapWithVisibilityQuery("test", input);
        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(termQuery, BooleanClause.Occur.MUST);
        expectedQuery.add(vis(), BooleanClause.Occur.MUST);

        assertEquals(expectedQuery, result.getLuceneQuery());
        assertFalse(result.mustNotOccur());
    }

    @Test
    public void testWrapWithVisibilityQueryTermQueryMustNotOccur()
    {
        final TermQuery termQuery = new TermQuery(new Term("test", "123"));
        final QueryFactoryResult input = new QueryFactoryResult(termQuery, true);
        final QueryFactoryResult result = QueryFactoryResult.wrapWithVisibilityQuery("test", input);
        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(termQuery, BooleanClause.Occur.MUST_NOT);
        expectedQuery.add(vis(), BooleanClause.Occur.MUST);

        assertEquals(expectedQuery, result.getLuceneQuery());
        assertFalse(result.mustNotOccur());
    }

    @Test
    public void testWrapWithVisibilityQueryBooleanQuery()
    {
        final BooleanQuery booleanQuery = new BooleanQuery();
        final TermQuery termQuery = new TermQuery(new Term("test", "123"));
        booleanQuery.add(termQuery, BooleanClause.Occur.SHOULD);
        final QueryFactoryResult input = new QueryFactoryResult(booleanQuery);

        final QueryFactoryResult result = QueryFactoryResult.wrapWithVisibilityQuery("test", input);
        final BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(booleanQuery, BooleanClause.Occur.MUST);
        expectedQuery.add(vis(), BooleanClause.Occur.MUST);

        assertEquals(expectedQuery, result.getLuceneQuery());
        assertFalse(result.mustNotOccur());
    }

    @Test
    public void testMergeResultsWithShouldEmptyList() throws Exception
    {
        assertEquals(QueryFactoryResult.createFalseResult(), QueryFactoryResult.mergeResultsWithShould(Collections.<QueryFactoryResult>emptyList()));
    }

    @Test
    public void testMergeResultsWithShouldAllFalse() throws Exception
    {
        final List<QueryFactoryResult> list = CollectionBuilder.list(QueryFactoryResult.createFalseResult(), QueryFactoryResult.createFalseResult());
        assertEquals(QueryFactoryResult.createFalseResult(), QueryFactoryResult.mergeResultsWithShould(list));
    }

    @Test
    public void testMergeResultsWithShouldOneFalseTwoNot() throws Exception
    {
        final Query query1 = TermQueryFactory.nonEmptyQuery("field1");
        final Query query2 = TermQueryFactory.nonEmptyQuery("field2");
        final QueryFactoryResult result1 = new QueryFactoryResult(query1, false);
        final QueryFactoryResult result2 = new QueryFactoryResult(query2, true);
        final List<QueryFactoryResult> list = CollectionBuilder.list(QueryFactoryResult.createFalseResult(), result1, result2);

        BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(query1, BooleanClause.Occur.SHOULD);
        expectedQuery.add(query2, BooleanClause.Occur.MUST_NOT);
        QueryFactoryResult expectedResult = new QueryFactoryResult(expectedQuery);

        assertEquals(expectedResult, QueryFactoryResult.mergeResultsWithShould(list));
    }

    private static TermQuery vis()
    {
        return new TermQuery(new Term(DocumentConstants.ISSUE_VISIBLE_FIELD_IDS, "test"));
    }
}
