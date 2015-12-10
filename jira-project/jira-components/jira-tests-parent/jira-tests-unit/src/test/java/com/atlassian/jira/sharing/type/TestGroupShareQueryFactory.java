package com.atlassian.jira.sharing.type;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.security.groups.MockGroupManager;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharedEntity;
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
 * A test for GroupShareQueryFactory
 *
 * @since v3.13
 */
public class TestGroupShareQueryFactory
{
    ApplicationUser user;

    @Before
    public void setUp() throws Exception
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());
        user = new MockApplicationUser("admin");
    }

    @Test
    public void testGetTerms()
    {
        // Set up the mock GroupManager
        final MockGroupManager mockGroupManager = new MockGroupManager();
        mockGroupManager.addMember("group1", "admin");
        mockGroupManager.addMember("group2", "admin");

        GroupShareQueryFactory queryFactory = new GroupShareQueryFactory(mockGroupManager);
        Term[] terms = queryFactory.getTerms((ApplicationUser) null);
        assertNotNull(terms);
        assertEquals(0, terms.length);

        terms = queryFactory.getTerms(user);
        assertEquals(2, terms.length);
        assertEquals("shareTypeGroup", terms[0].field());
        assertEquals("group1", terms[0].text());
        assertEquals("shareTypeGroup", terms[1].field());
        assertEquals("group2", terms[1].text());
    }

    @Test
    public void testGetQuery()
    {
        final GroupSharePermission groupPerm = getGroupSharePermission("group1");
        ShareTypeSearchParameter searchParameter = groupPerm.getSearchParameter();

        GroupShareQueryFactory queryFactory = new GroupShareQueryFactory(null);
        Query query = queryFactory.getQuery(searchParameter);
        assertNotNull(query);
        assertEquals("shareTypeGroup:group1",query.toString());

        // no sematic difference in these this call with a user
        query = queryFactory.getQuery(searchParameter, user);
        assertNotNull(query);
        assertEquals("shareTypeGroup:group1",query.toString());
    }

    @Test
    public void testGetField()
    {
        GroupShareQueryFactory queryFactory = new GroupShareQueryFactory(null);
        Field field = queryFactory.getField(new SharedEntity.Identifier(new Long(123), PortalPage.ENTITY_TYPE, user), getSharePermission("group1"));
        assertNotNull(field);
        assertEquals("shareTypeGroup", field.name());
        assertTrue(field.isStored());
        assertTrue(field.isIndexed());
        assertEquals("group1",field.stringValue());
    }



    GroupSharePermission getGroupSharePermission(final String groupName)
    {
        return new GroupSharePermission(getSharePermission(groupName));
    }

    private SharePermission getSharePermission(final String groupName)
    {
        return new SharePermission()
        {
            public Long getId()
            {
                return null;
            }

            public ShareType.Name getType()
            {
                return GroupShareType.TYPE;
            }

            public String getParam1()
            {
                return groupName;
            }

            public String getParam2()
            {
                return null;
            }
        };
    }
}
