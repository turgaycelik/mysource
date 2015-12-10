package com.atlassian.jira.sharing.index;

import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;

import org.apache.lucene.search.Query;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.sharing.search.SharedEntitySearchParameters.TextSearchMode.EXACT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * A test case for DefaultQueryFactory
 *
 * @since v3.13
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultQueryFactory
{
    private DefaultQueryFactory queryFactory;

    @Mock private PermissionManager mockPermissionManager;
    @Mock private ProjectFactory mockProjectFactory;
    @Mock private ProjectManager mockProjectManager;
    @Mock private ProjectRoleManager mockProjectRoleManager;
    @Mock private SharedEntitySearchContextToQueryFactoryMap mockSharedEntitySearchContextToQueryFactoryMap;
    @Mock private ShareTypeFactory mockShareTypeFactory;
    @Mock private UserManager mockUserManager;

    @Before
    public void setUp()
    {
        queryFactory = new DefaultQueryFactory(
                mockShareTypeFactory, mockSharedEntitySearchContextToQueryFactoryMap, mockUserManager);
    }

    @Test
    public void testSimpleCreate_WithUserFred()
    {
        // Set up
        setUpUser("fred");
        final SharedEntitySearchParameters searchParameters = new SharedEntitySearchParametersBuilder()
                .setUserName("fred")
                .toSearchParameters();

        // Invoke
        final Query query = queryFactory.create(searchParameters);

        // Check
        assertNotNull(query);
        assertEquals("+owner:fred", query.toString());
    }

    private void setUpUser(final String username)
    {
        when(mockUserManager.getUserByName(username)).thenReturn(new MockApplicationUser(username));
    }

    @Test
    public void testSimpleCreate_WithName()
    {
        // Set up
        setUpUser("input");
        final SharedEntitySearchParameters searchParameters = new SharedEntitySearchParametersBuilder()
                .setName("input")
                .toSearchParameters();

        // Invoke
        final Query query = queryFactory.create(searchParameters);

        // Check
        assertNotNull(query);
        assertEquals("+(name:input)", query.toString());
    }

    @Test
    public void testSimpleCreate_WithNameCaseInsensitive()
    {
        // Set up
        setUpUser("INput");
        final SharedEntitySearchParameters searchParameters = new SharedEntitySearchParametersBuilder()
                .setName("INput")
                .setTextSearchMode(EXACT)
                .toSearchParameters();

        // Invoke
        final Query query = queryFactory.create(searchParameters);

        // Check
        assertNotNull(query);
        assertEquals("+(+nameCaseless:input)", query.toString());
    }

    @Test
    public void testSimpleCreate_WithDesc()
    {
        // Set up
        final SharedEntitySearchParameters searchParameters = new SharedEntitySearchParametersBuilder()
                .setDescription("input")
                .toSearchParameters();

        // Invoke
        final Query query = queryFactory.create(searchParameters);

        // Check
        assertNotNull(query);
        assertEquals("+(description:input)", query.toString());
    }

    @Test
    public void testSimpleCreate_WithDescInsensitive()
    {
        // Set up
        final SharedEntitySearchParameters searchParameters = new SharedEntitySearchParametersBuilder()
                .setDescription("INput")
                .setTextSearchMode(EXACT)
                .toSearchParameters();

        // Invoke
        final Query query = queryFactory.create(searchParameters);

        // Check
        assertNotNull(query);
        assertEquals("+(+descriptionSort:input)", query.toString());
    }

    @Test
    public void testSimpleCreate_WithUserAndNameAndDesc()
    {
        // Set up
        setUpUser("userName");
        final SharedEntitySearchParameters searchParameters = new SharedEntitySearchParametersBuilder()
                .setDescription("inputDesc")
                .setName("inputName")
                .setUserName("userName")
                .toSearchParameters();

        // Invoke
        final Query query = queryFactory.create(searchParameters);

        // Check
        assertNotNull(query);
        // name should no longer be stemmed, owner/userName should not!
        assertEquals("+(name:inputname description:inputdesc) +owner:username", query.toString());
    }

    @Test
    public void testSimpleCreate_WithUserCaseInsensitive()
    {
        // Set up
        setUpUser("USERNAME");
        final SharedEntitySearchParameters searchParameters = new SharedEntitySearchParametersBuilder()
                .setUserName("USERNAME")
                .toSearchParameters();

        // Invoke
        final Query query = queryFactory.create(searchParameters);

        // Check
        assertNotNull(query);
        // name should no longer be stemmed, owner/userName should not!
        assertEquals("+owner:username", query.toString());
    }
}
