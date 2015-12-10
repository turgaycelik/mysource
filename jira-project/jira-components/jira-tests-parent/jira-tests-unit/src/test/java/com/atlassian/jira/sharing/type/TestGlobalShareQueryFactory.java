package com.atlassian.jira.sharing.type;

import com.atlassian.jira.sharing.search.GlobalShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.ShareTypeSearchParameter;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;

import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * A test for GlobalShareQueryFactory
 *
 * @since v3.13
 */
public class TestGlobalShareQueryFactory
{
    ApplicationUser user;

    @Before
    public void setUp() throws Exception
    {
        user = new MockApplicationUser("admin");
    }

    @Test
    public void testGetTerms()
    {
        GlobalShareQueryFactory queryFactory = new GlobalShareQueryFactory();
        Term[] terms = queryFactory.getTerms((ApplicationUser) null);
        assertNotNull(terms);
        assertEquals(1, terms.length);
        assertEquals("shareTypeGlobal", terms[0].field());
        assertEquals("true", terms[0].text());

        terms = queryFactory.getTerms(user);
        assertEquals(1, terms.length);
        assertEquals("shareTypeGlobal", terms[0].field());
        assertEquals("true", terms[0].text());
    }

    @Test
    public void testGetQuery()
    {
        ShareTypeSearchParameter searchParameter = GlobalShareTypeSearchParameter.GLOBAL_PARAMETER;

        GlobalShareQueryFactory queryFactory = new GlobalShareQueryFactory();
        Query query = queryFactory.getQuery(searchParameter);
        assertNotNull(query);
        assertEquals("shareTypeGlobal:true", query.toString());

        // no sematic difference in these this call with a user
        query = queryFactory.getQuery(searchParameter, user);
        assertNotNull(query);
        assertEquals("shareTypeGlobal:true", query.toString());
    }

    @Test
    public void testGetField()
    {
        GlobalShareQueryFactory queryFactory = new GlobalShareQueryFactory();
        // none of the parameters matter for global
        Field field = queryFactory.getField(null, null);
        assertNotNull(field);
        assertEquals("shareTypeGlobal", field.name());
        assertTrue(field.isStored());
        assertTrue(field.isIndexed());
        assertEquals("true",field.stringValue());
    }
}
