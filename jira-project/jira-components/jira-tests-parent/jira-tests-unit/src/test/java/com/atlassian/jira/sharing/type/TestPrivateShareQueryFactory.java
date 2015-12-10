package com.atlassian.jira.sharing.type;

import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.sharing.SharedEntity;
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A test for PrivateShareQueryFactory
 *
 * @since v3.13
 */
public class TestPrivateShareQueryFactory
{
    private ApplicationUser user;

    @Before
    public void setUp() throws Exception
    {
        user = new MockApplicationUser("Admin");
    }

    @Test
    public void testGetTerms()
    {
        PrivateShareQueryFactory queryFactory = new PrivateShareQueryFactory();
        Term[] terms = queryFactory.getTerms((ApplicationUser) null);
        assertNotNull(terms);
        assertEquals(0, terms.length);

        terms = queryFactory.getTerms(user);
        assertEquals(1, terms.length);
        assertEquals("owner", terms[0].field());
        assertEquals("admin", terms[0].text());
    }

    @Test
    public void testGetQuery()
    {
        ShareTypeSearchParameter searchParameter = GlobalShareTypeSearchParameter.GLOBAL_PARAMETER;

        PrivateShareQueryFactory queryFactory = new PrivateShareQueryFactory();

        try
        {
            queryFactory.getQuery(searchParameter);
            fail("We need a user in order to search");
        }
        catch (UnsupportedOperationException ignored)
        {
        }

        Query query = queryFactory.getQuery(searchParameter, user);
        assertNotNull(query);
        assertEquals("owner:admin", query.toString());

        query = queryFactory.getQuery(searchParameter, (ApplicationUser) null);
        assertNull(query);
    }

    @Test
    public void testGetField()
    {
        PrivateShareQueryFactory queryFactory = new PrivateShareQueryFactory();
        final SharedEntity.Identifier entity = new SharedEntity.Identifier(123L, PortalPage.ENTITY_TYPE, user)
        {
            public SharePermissions getPermissions()
            {
                return SharePermissions.PRIVATE;
            }
        };
        Field field = queryFactory.getField(entity, null);
        assertNotNull(field);
        assertEquals("owner", field.name());
        assertTrue(field.isStored());
        assertTrue(field.isIndexed());
        assertEquals("admin",field.stringValue());
    }
}
