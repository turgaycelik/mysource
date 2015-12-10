package com.atlassian.jira.sharing.index;

import com.atlassian.jira.sharing.SharedEntityColumn;
import com.atlassian.jira.sharing.search.SearchParseException;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * A test for QueryBuilder
 *
 * @since v3.13
 */
public class TestQueryBuilder
{
    /**
     * Test the QueryBuilder ability to ignore bad params
     */
    @Test
    public void testEmptyParms()
    {
        final QueryBuilder queryBuilder = new QueryBuilder();

        queryBuilder.addParsedQuery(SharedEntityColumn.NAME, "", BooleanClause.Occur.SHOULD);
        assertFalse(queryBuilder.hasClauses());

        queryBuilder.add((Query) null, BooleanClause.Occur.SHOULD);
        assertFalse(queryBuilder.hasClauses());

        queryBuilder.add((Term) null, BooleanClause.Occur.SHOULD);
        assertFalse(queryBuilder.hasClauses());

        queryBuilder.add(new Term[0], BooleanClause.Occur.SHOULD);
        assertFalse(queryBuilder.hasClauses());

        queryBuilder.add(new Term[] { null }, BooleanClause.Occur.SHOULD);
        assertFalse(queryBuilder.hasClauses());

        queryBuilder.add((QueryBuilder) null, BooleanClause.Occur.SHOULD);
        assertFalse(queryBuilder.hasClauses());

        queryBuilder.add(new QueryBuilder(), BooleanClause.Occur.SHOULD);
        assertFalse(queryBuilder.hasClauses());
    }

    @Test(expected = SearchParseException.class)
    public void testIllegalLeadingWildcardParam()
    {
        new QueryBuilder().addParsedQuery(SharedEntityColumn.NAME, "*notallowed", null);
    }

    @Test(expected = SearchParseException.class)
    public void testIllegalLeadingWildcardParamSecondWord()
    {
        new QueryBuilder().addParsedQuery(SharedEntityColumn.NAME, "allowed *notallowed", null);
    }

    @Test(expected = SearchParseException.class)
    public void testIllegalFuzzyQuery()
    {
        new QueryBuilder().addParsedQuery(SharedEntityColumn.NAME, "notallowed~1", null);
    }

    @Test
    public void testAddColumnShould()
    {
        final QueryBuilder queryBuilder = new QueryBuilder();

        queryBuilder.addParsedQuery(SharedEntityColumn.NAME, "value", BooleanClause.Occur.SHOULD);
        assertTrue(queryBuilder.hasClauses());
        final Query query = queryBuilder.toQuery();
        assertNotNull(query);
        assertEquals("name:value", query.toString());
    }

    @Test
    public void testAddColumnMust()
    {
        final QueryBuilder queryBuilder = new QueryBuilder();

        queryBuilder.addParsedQuery(SharedEntityColumn.NAME, "value", BooleanClause.Occur.MUST);
        assertTrue(queryBuilder.hasClauses());
        final Query query = queryBuilder.toQuery();
        assertNotNull(query);
        assertEquals("+name:value", query.toString());
    }

    @Test
    public void testAddColumnMustNot()
    {
        final QueryBuilder queryBuilder = new QueryBuilder();

        queryBuilder.addParsedQuery(SharedEntityColumn.NAME, "value", BooleanClause.Occur.MUST_NOT);
        assertTrue(queryBuilder.hasClauses());
        final Query query = queryBuilder.toQuery();
        assertNotNull(query);
        assertEquals("-name:value", query.toString());
    }

    @Test
    public void testAddTerm()
    {
        final QueryBuilder queryBuilder = new QueryBuilder();

        queryBuilder.add(new Term("term", "value"), BooleanClause.Occur.SHOULD);
        assertTrue(queryBuilder.hasClauses());
        final Query query = queryBuilder.toQuery();
        assertNotNull(query);
        assertEquals("term:value", query.toString());
    }

    @Test
    public void testAddTerms()
    {
        final QueryBuilder queryBuilder = new QueryBuilder();

        final Term[] terms = new Term[] { new Term("term1", "value1"), new Term("term2", "value2") };
        queryBuilder.add(terms, BooleanClause.Occur.SHOULD);
        assertTrue(queryBuilder.hasClauses());
        final Query query = queryBuilder.toQuery();
        assertNotNull(query);
        assertEquals("term1:value1 term2:value2", query.toString());
    }

    @Test
    public void testAddQuery()
    {
        final TermQuery termQuery1 = new TermQuery(new Term("term1", "value1"));
        final TermQuery termQuery2 = new TermQuery(new Term("term2", "value2"));

        final QueryBuilder queryBuilder = new QueryBuilder();

        queryBuilder.add(termQuery1, BooleanClause.Occur.SHOULD);
        queryBuilder.add(termQuery2, BooleanClause.Occur.MUST);

        assertTrue(queryBuilder.hasClauses());
        final Query query = queryBuilder.toQuery();
        assertNotNull(query);
        assertEquals("term1:value1 +term2:value2", query.toString());
    }

    @Test
    public void testAddQueryBuilder()
    {
        final TermQuery termQuery1 = new TermQuery(new Term("term1", "value1"));
        final TermQuery termQuery2 = new TermQuery(new Term("term2", "value2"));

        final QueryBuilder queryBuilder1 = new QueryBuilder();
        queryBuilder1.add(termQuery1, BooleanClause.Occur.MUST);

        final QueryBuilder queryBuilder = new QueryBuilder();

        queryBuilder.add(queryBuilder1, BooleanClause.Occur.MUST);
        queryBuilder.add(termQuery2, BooleanClause.Occur.SHOULD);

        assertTrue(queryBuilder.hasClauses());
        final Query query = queryBuilder.toQuery();
        assertNotNull(query);
        assertEquals("+(+term1:value1) term2:value2", query.toString());
    }

    @Test
    public void testAddQueryFuzzy()
    {
        final QueryBuilder queryBuilder = new QueryBuilder();

        queryBuilder.addParsedQuery(SharedEntityColumn.NAME, "value~0.8", BooleanClause.Occur.MUST);
        assertTrue(queryBuilder.hasClauses());
        final Query query = queryBuilder.toQuery();
        assertNotNull(query);
        assertEquals("+name:value~0.8", query.toString());
    }

    @Test
    public void testAddQueryWildcard()
    {
        final QueryBuilder queryBuilder = new QueryBuilder();

        queryBuilder.addParsedQuery(SharedEntityColumn.NAME, "value*", BooleanClause.Occur.MUST);
        assertTrue(queryBuilder.hasClauses());
        final Query query = queryBuilder.toQuery();
        assertNotNull(query);
        assertEquals("+name:value*", query.toString());
    }

    @Test
    public void testToQueryNoClauses()
    {
        final QueryBuilder queryBuilder = new QueryBuilder();
        final Query query = queryBuilder.toQuery();
        assertNotNull(query);
        assertTrue(query instanceof MatchAllDocsQuery);
    }

    @Test
    public void testBuildTemplateMethod()
    {
        final QueryBuilder queryBuilder = new QueryBuilder();

        assertSame(queryBuilder, queryBuilder.build());
        assertSame(queryBuilder, queryBuilder.build().build().build().build());
    }

    @Test(expected = SearchParseException.class)
    public void testAddParseProblems()
    {
        final QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.addParsedQuery(SharedEntityColumn.NAME, "+(89=34299835\u0000426788787=4*&%(%$%^$4", BooleanClause.Occur.SHOULD);
    }
}
