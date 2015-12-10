package com.atlassian.jira.issue.search.util;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TermQuery;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestDefaultLuceneQueryModifier
{

    @Test
    public void testNotChangedIfNoBooleanQuery() throws Exception
    {
        final TermQuery blahQuery = getTermQuery("blah", "blee");

        assertEquals(blahQuery, new DefaultLuceneQueryModifier().getModifiedQuery(blahQuery));
    }

    @Test
    public void testPassedEmptyBooleanQuery() throws Exception
    {
        assertEquals(new BooleanQuery(), new DefaultLuceneQueryModifier().getModifiedQuery(new BooleanQuery()));
    }
    
    @Test
    public void testContainsOnlyNotQueries() throws Exception
    {
        BooleanQuery queryToTransform = new BooleanQuery();
        final TermQuery blahQuery = getTermQuery("blah", "blee");
        blahQuery.setBoost(9);
        queryToTransform.add(blahQuery, BooleanClause.Occur.MUST_NOT);
        final TermQuery clahQuery = getTermQuery("clah", "clee");
        queryToTransform.add(clahQuery, BooleanClause.Occur.MUST_NOT);
        queryToTransform.setBoost(3);
        queryToTransform.setMinimumNumberShouldMatch(4);

        BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.setBoost(3);
        expectedQuery.setMinimumNumberShouldMatch(4);
        expectedQuery.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
        expectedQuery.add(blahQuery, BooleanClause.Occur.MUST_NOT);
        expectedQuery.add(clahQuery, BooleanClause.Occur.MUST_NOT);

        assertEquals(expectedQuery, new DefaultLuceneQueryModifier().getModifiedQuery(queryToTransform));
    }

    @Test
    public void testContainsOnlyNotWithNestedNotQueries() throws Exception
    {
        BooleanQuery queryToTransform = new BooleanQuery();

        final TermQuery blahQuery = getTermQuery("blah", "blee");
        queryToTransform.add(blahQuery, BooleanClause.Occur.MUST_NOT);

        BooleanQuery nestedNotQuery = new BooleanQuery();
        nestedNotQuery.setMinimumNumberShouldMatch(3);
        nestedNotQuery.setBoost(4);
        final TermQuery clahQuery = getTermQuery("clah", "clee");
        nestedNotQuery.add(clahQuery, BooleanClause.Occur.MUST_NOT);
        final TermQuery dlahQuery = getTermQuery("dlah", "dlee");
        nestedNotQuery.add(dlahQuery, BooleanClause.Occur.MUST_NOT);

        queryToTransform.add(nestedNotQuery, BooleanClause.Occur.MUST_NOT);

        BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
        BooleanQuery expectedNestedNotQuery = new BooleanQuery();
        expectedNestedNotQuery.setMinimumNumberShouldMatch(3);
        expectedNestedNotQuery.setBoost(4);
        expectedNestedNotQuery.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
        expectedNestedNotQuery.add(clahQuery, BooleanClause.Occur.MUST_NOT);
        expectedNestedNotQuery.add(dlahQuery, BooleanClause.Occur.MUST_NOT);
        expectedQuery.add(blahQuery, BooleanClause.Occur.MUST_NOT);
        expectedQuery.add(expectedNestedNotQuery, BooleanClause.Occur.MUST_NOT);

        assertEquals(expectedQuery, new DefaultLuceneQueryModifier().getModifiedQuery(queryToTransform));
    }

    @Test
    public void testContainsMustWithNotQueries() throws Exception
    {
        BooleanQuery queryToTransform = new BooleanQuery();
        final TermQuery blahQuery = getTermQuery("blah", "blee");
        queryToTransform.add(blahQuery, BooleanClause.Occur.MUST_NOT);
        final TermQuery clahQuery = getTermQuery("clah", "clee");
        queryToTransform.add(clahQuery, BooleanClause.Occur.MUST_NOT);
        final TermQuery dlahQuery = getTermQuery("dlah", "dlee");
        queryToTransform.add(dlahQuery, BooleanClause.Occur.MUST);

        BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(blahQuery, BooleanClause.Occur.MUST_NOT);
        expectedQuery.add(clahQuery, BooleanClause.Occur.MUST_NOT);
        expectedQuery.add(dlahQuery, BooleanClause.Occur.MUST);

        assertEquals(expectedQuery, new DefaultLuceneQueryModifier().getModifiedQuery(queryToTransform));
    }
    
    @Test
    public void testContainsMustWithNotAndShouldQueries() throws Exception
    {
        BooleanQuery queryToTransform = new BooleanQuery();
        final TermQuery blahQuery = getTermQuery("blah", "blee");
        queryToTransform.add(blahQuery, BooleanClause.Occur.MUST_NOT);
        final TermQuery clahQuery = getTermQuery("clah", "clee");
        queryToTransform.add(clahQuery, BooleanClause.Occur.MUST_NOT);
        final TermQuery dlahQuery = getTermQuery("dlah", "dlee");
        queryToTransform.add(dlahQuery, BooleanClause.Occur.MUST);
        final TermQuery elahQuery = getTermQuery("elah", "elee");
        queryToTransform.add(elahQuery, BooleanClause.Occur.SHOULD);

        BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(blahQuery, BooleanClause.Occur.MUST_NOT);
        expectedQuery.add(clahQuery, BooleanClause.Occur.MUST_NOT);
        expectedQuery.add(dlahQuery, BooleanClause.Occur.MUST);
        expectedQuery.add(elahQuery, BooleanClause.Occur.SHOULD);

        assertEquals(expectedQuery, new DefaultLuceneQueryModifier().getModifiedQuery(queryToTransform));
    }

    @Test
    public void testContainsMustWithNestedNotQueries() throws Exception
    {
        BooleanQuery queryToTransform = new BooleanQuery();
        BooleanQuery nestedNotQuery = new BooleanQuery();
        final TermQuery blahQuery = getTermQuery("blah", "blee");
        nestedNotQuery.add(blahQuery, BooleanClause.Occur.MUST_NOT);
        final TermQuery clahQuery = getTermQuery("clah", "clee");
        nestedNotQuery.add(clahQuery, BooleanClause.Occur.MUST_NOT);
        final TermQuery dlahQuery = getTermQuery("dlah", "dlee");
        queryToTransform.add(nestedNotQuery, BooleanClause.Occur.MUST);
        queryToTransform.add(dlahQuery, BooleanClause.Occur.MUST);

        BooleanQuery expectedQuery = new BooleanQuery();
        BooleanQuery expectedNestedNotQuery = new BooleanQuery();
        expectedNestedNotQuery.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
        expectedNestedNotQuery.add(blahQuery, BooleanClause.Occur.MUST_NOT);
        expectedNestedNotQuery.add(clahQuery, BooleanClause.Occur.MUST_NOT);
        expectedQuery.add(expectedNestedNotQuery, BooleanClause.Occur.MUST);
        expectedQuery.add(dlahQuery, BooleanClause.Occur.MUST);

        assertEquals(expectedQuery, new DefaultLuceneQueryModifier().getModifiedQuery(queryToTransform));
    }

    @Test
    public void testContainsMustWithBooleanNestedNotQueries() throws Exception
    {
        BooleanQuery queryToTransform = new BooleanQuery();
        BooleanQuery nestedNotQuery = new BooleanQuery();
        final TermQuery blahQuery = getTermQuery("blah", "blee");
        nestedNotQuery.add(blahQuery, BooleanClause.Occur.MUST);
        final TermQuery clahQuery = getTermQuery("clah", "clee");
        nestedNotQuery.add(clahQuery, BooleanClause.Occur.MUST);
        final TermQuery dlahQuery = getTermQuery("dlah", "dlee");
        queryToTransform.add(nestedNotQuery, BooleanClause.Occur.MUST_NOT);
        queryToTransform.add(dlahQuery, BooleanClause.Occur.MUST);

        BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(nestedNotQuery, BooleanClause.Occur.MUST_NOT);
        expectedQuery.add(dlahQuery, BooleanClause.Occur.MUST);

        assertEquals(queryToTransform, new DefaultLuceneQueryModifier().getModifiedQuery(queryToTransform));
    }

    @Test
    public void testContainsShouldWithNotQueries() throws Exception
    {
        BooleanQuery queryToTransform = new BooleanQuery();
        final TermQuery blahQuery = getTermQuery("blah", "blee");
        queryToTransform.add(blahQuery, BooleanClause.Occur.MUST_NOT);
        final TermQuery clahQuery = getTermQuery("clah", "clee");
        clahQuery.setBoost(8);
        queryToTransform.add(clahQuery, BooleanClause.Occur.MUST_NOT);
        final TermQuery dlahQuery = getTermQuery("dlah", "dlee");
        queryToTransform.add(dlahQuery, BooleanClause.Occur.SHOULD);

        BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(dlahQuery, BooleanClause.Occur.SHOULD);
        BooleanQuery subNotQuery1 = new BooleanQuery();
        subNotQuery1.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
        subNotQuery1.add(blahQuery, BooleanClause.Occur.MUST_NOT);
        expectedQuery.add(subNotQuery1, BooleanClause.Occur.SHOULD);
        BooleanQuery subNotQuery2 = new BooleanQuery();
        subNotQuery2.setBoost(8);
        subNotQuery2.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
        final TermQuery clahQueryWithNoBoost = getTermQuery("clah", "clee");
        subNotQuery2.add(clahQueryWithNoBoost, BooleanClause.Occur.MUST_NOT);
        expectedQuery.add(subNotQuery2, BooleanClause.Occur.SHOULD);

        assertEquals(expectedQuery, new DefaultLuceneQueryModifier().getModifiedQuery(queryToTransform));
    }

    @Test
    public void testContainsShouldWithNestedNotQueries() throws Exception
    {
        BooleanQuery queryToTransform = new BooleanQuery();
        BooleanQuery nestedNotQuery = new BooleanQuery();
        final TermQuery blahQuery = getTermQuery("blah", "blee");
        nestedNotQuery.add(blahQuery, BooleanClause.Occur.MUST_NOT);
        final TermQuery clahQuery = getTermQuery("clah", "clee");
        nestedNotQuery.add(clahQuery, BooleanClause.Occur.MUST_NOT);
        final TermQuery dlahQuery = getTermQuery("dlah", "dlee");
        queryToTransform.add(nestedNotQuery, BooleanClause.Occur.SHOULD);
        queryToTransform.add(dlahQuery, BooleanClause.Occur.SHOULD);

        BooleanQuery expectedQuery = new BooleanQuery();
        BooleanQuery expectedNestedNotQuery = new BooleanQuery();
        expectedNestedNotQuery.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
        expectedNestedNotQuery.add(blahQuery, BooleanClause.Occur.MUST_NOT);
        expectedNestedNotQuery.add(clahQuery, BooleanClause.Occur.MUST_NOT);
        expectedQuery.add(expectedNestedNotQuery, BooleanClause.Occur.SHOULD);
        expectedQuery.add(dlahQuery, BooleanClause.Occur.SHOULD);

        assertEquals(expectedQuery, new DefaultLuceneQueryModifier().getModifiedQuery(queryToTransform));
    }

    @Test
    public void testContainsShouldWithBooleanNestedNotQueries() throws Exception
    {
        BooleanQuery queryToTransform = new BooleanQuery();
        BooleanQuery nestedNotQuery = new BooleanQuery();
        final TermQuery blahQuery = getTermQuery("blah", "blee");
        nestedNotQuery.add(blahQuery, BooleanClause.Occur.MUST);
        final TermQuery clahQuery = getTermQuery("clah", "clee");
        nestedNotQuery.add(clahQuery, BooleanClause.Occur.MUST);
        final TermQuery dlahQuery = getTermQuery("dlah", "dlee");
        queryToTransform.add(nestedNotQuery, BooleanClause.Occur.MUST_NOT);
        queryToTransform.add(dlahQuery, BooleanClause.Occur.SHOULD);

        BooleanQuery expectedQuery = new BooleanQuery();
        expectedQuery.add(dlahQuery, BooleanClause.Occur.SHOULD);

        BooleanQuery subExpectedNestedNotQuery = new BooleanQuery();
        subExpectedNestedNotQuery.add(blahQuery, BooleanClause.Occur.MUST);
        subExpectedNestedNotQuery.add(clahQuery, BooleanClause.Occur.MUST);

        BooleanQuery expectedNestedNotQuery = new BooleanQuery();
        expectedNestedNotQuery.add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST);
        expectedNestedNotQuery.add(subExpectedNestedNotQuery, BooleanClause.Occur.MUST_NOT);

        expectedQuery.add(expectedNestedNotQuery, BooleanClause.Occur.SHOULD);

        assertEquals(expectedQuery, new DefaultLuceneQueryModifier().getModifiedQuery(queryToTransform));
    }

    protected TermQuery getTermQuery(String fieldName, String indexValue)
    {
        return new TermQuery(new Term(fieldName, indexValue));
    }

}
