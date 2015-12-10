package com.atlassian.jira.sharing.search;

import com.atlassian.jira.sharing.SharePermissionImpl;
import com.atlassian.jira.sharing.SharedEntityColumn;
import com.atlassian.jira.sharing.type.GlobalShareType;
import com.atlassian.jira.sharing.type.GroupShareType;
import com.atlassian.jira.sharing.type.ProjectShareType;
import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.user.MockApplicationUser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A test for {@link com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder}
 *
 * @since v3.13
 */
public class TestSharedEntitySearchParametersBuilder
{
    private static final String DESCRIPTION = "description";
    private static final String NAME = "name";
    private static final String USER_NAME = "userName";
    private static final String SECOND_USER_NAME = "SecondUserName";
    private static final String SECOND_DESCRIPTION = "SecondDescription";

    @Test
    public void testInitialState()
    {
        final SharedEntitySearchParameters params = new SharedEntitySearchParametersBuilder().toSearchParameters();

        assertNull(params.getName());
        assertNull(params.getDescription());
        assertNull(params.getUserName());
        assertNull(params.getFavourite());
        assertNull(params.getShareTypeParameter());
        assertNotNull(params.getSortColumn());
        assertSame(SharedEntityColumn.NAME,params.getSortColumn());
        assertSame(SharedEntitySearchParameters.TextSearchMode.OR, params.getTextSearchMode());

        assertTrue(params.isAscendingSort());
    }

    @Test
    public void testSettersAndGetters()
    {
        final SharedEntitySearchParametersBuilder template = new SharedEntitySearchParametersBuilder();

        template.setDescription(TestSharedEntitySearchParametersBuilder.DESCRIPTION);
        assertEquals(TestSharedEntitySearchParametersBuilder.DESCRIPTION, template.toSearchParameters().getDescription());

        template.setFavourite(Boolean.TRUE);
        assertEquals(Boolean.TRUE, template.toSearchParameters().getFavourite());
        template.setFavourite(null);
        assertNull(template.toSearchParameters().getFavourite());

        template.setName(TestSharedEntitySearchParametersBuilder.NAME);
        assertEquals(TestSharedEntitySearchParametersBuilder.NAME, template.toSearchParameters().getName());

        template.setUserName(TestSharedEntitySearchParametersBuilder.USER_NAME);
        assertEquals(TestSharedEntitySearchParametersBuilder.USER_NAME, template.toSearchParameters().getUserName());

        template.setShareTypeParameter(PrivateShareTypeSearchParameter.PRIVATE_PARAMETER);
        assertEquals(PrivateShareTypeSearchParameter.PRIVATE_PARAMETER, template.toSearchParameters().getShareTypeParameter());

        template.setSortColumn(SharedEntityColumn.NAME, false);
        assertEquals(SharedEntityColumn.NAME, template.toSearchParameters().getSortColumn());
        assertFalse(template.toSearchParameters().isAscendingSort());

        template.setSortColumn(SharedEntityColumn.DESCRIPTION, true);
        assertEquals(SharedEntityColumn.DESCRIPTION, template.toSearchParameters().getSortColumn());
        assertTrue(template.toSearchParameters().isAscendingSort());

        template.setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.AND);
        assertSame(SharedEntitySearchParameters.TextSearchMode.AND, template.toSearchParameters().getTextSearchMode());
        template.setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.OR);
        assertSame(SharedEntitySearchParameters.TextSearchMode.OR, template.toSearchParameters().getTextSearchMode());

        //
        // test the various share types
        template.setSharePermission(new SharePermissionImpl(GlobalShareType.TYPE, null,null));
        assertSame(GlobalShareTypeSearchParameter.GLOBAL_PARAMETER, template.toSearchParameters().getShareTypeParameter());

        GroupShareTypeSearchParameter groupShareTypeSearchParameter = new GroupShareTypeSearchParameter("group1");
        template.setSharePermission(new SharePermissionImpl(GroupShareType.TYPE, "group1",null));
        assertEquals(groupShareTypeSearchParameter, template.toSearchParameters().getShareTypeParameter());

        ProjectShareTypeSearchParameter projectShareTypeSearchParameter = new ProjectShareTypeSearchParameter(new Long(123), new Long(456));
        template.setSharePermission(new SharePermissionImpl(ProjectShareType.TYPE, "123","456"));
        assertEquals(projectShareTypeSearchParameter, template.toSearchParameters().getShareTypeParameter());

        // assert it blows on bad types
        try
        {
            template.setSharePermission(new SharePermissionImpl(new ShareType.Name("unknown"), "123","456"));
            fail("Should have barfed ona  UnsupportedOperationException");
        }
        catch (UnsupportedOperationException ignored)
        {
        }
    }

    @Test
    public void testClone() throws Exception
    {
        final SharedEntitySearchParametersBuilder template = createStandardTemplate();
        final SharedEntitySearchParameters parameters1 = template.toSearchParameters();
        assertNotNull(parameters1);
        assertHasStandardValues(parameters1);

        final SharedEntitySearchParameters parameters2 = template.toSearchParameters();
        assertNotNull(parameters2);
        assertNotSame(parameters1, parameters2);

        template.setUserName(TestSharedEntitySearchParametersBuilder.SECOND_USER_NAME);
        template.setDescription(TestSharedEntitySearchParametersBuilder.SECOND_DESCRIPTION);
        final SharedEntitySearchParameters parameters3 = template.toSearchParameters();
        assertNotSame(parameters1, parameters3);
        assertNotSame(parameters2, parameters3);
        assertEquals(TestSharedEntitySearchParametersBuilder.SECOND_DESCRIPTION, parameters3.getDescription());
        assertEquals(TestSharedEntitySearchParametersBuilder.SECOND_USER_NAME, parameters3.getUserName());

    }

    @Test
    public void testContructorArgs() throws Exception
    {
        try
        {
            new SharedEntitySearchParametersBuilder(null);
            fail("Should have barfed");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testCopyConstructor()
    {
        final SharedEntitySearchParametersBuilder expectedTemplate = createStandardTemplate();
        final SharedEntitySearchParametersBuilder actualTemplate = new SharedEntitySearchParametersBuilder(expectedTemplate.toSearchParameters());

        assertHasStandardValues(actualTemplate);

        final SharedEntitySearchParametersBuilder actualTemplate2 = new SharedEntitySearchParametersBuilder(new SharedEntitySearchParameters()
        {
            public String getName()
            {
                return TestSharedEntitySearchParametersBuilder.NAME;
            }

            public String getDescription()
            {
                return TestSharedEntitySearchParametersBuilder.DESCRIPTION;
            }

            public MockApplicationUser getUser()
            {
                return new MockApplicationUser(TestSharedEntitySearchParametersBuilder.USER_NAME);
            }

            @Override
            public String getUserName()
            {
                return TestSharedEntitySearchParametersBuilder.USER_NAME;
            }

            public Boolean getFavourite()
            {
                return Boolean.TRUE;
            }

            public SharedEntityColumn getSortColumn()
            {
                return SharedEntityColumn.NAME;
            }

            public boolean isAscendingSort()
            {
                return true;
            }

            public ShareTypeSearchParameter getShareTypeParameter()
            {
                return PrivateShareTypeSearchParameter.PRIVATE_PARAMETER;
            }

            @Override
            public SharedEntitySearchContext getEntitySearchContext()
            {
                return SharedEntitySearchContext.USE;
            }

            public TextSearchMode getTextSearchMode()
            {
                return SharedEntitySearchParameters.TextSearchMode.AND;
            }
        });

        assertHasStandardValues(actualTemplate2);
    }

    private void assertHasStandardValues(final SharedEntitySearchParameters parameters)
    {
        assertEquals(TestSharedEntitySearchParametersBuilder.NAME, parameters.getName());
        assertEquals(TestSharedEntitySearchParametersBuilder.DESCRIPTION, parameters.getDescription());
        assertEquals(Boolean.TRUE, parameters.getFavourite());
        assertEquals(PrivateShareTypeSearchParameter.PRIVATE_PARAMETER, parameters.getShareTypeParameter());
        assertEquals(SharedEntityColumn.NAME, parameters.getSortColumn());
        assertEquals(true, parameters.isAscendingSort());
        assertEquals(TestSharedEntitySearchParametersBuilder.USER_NAME, parameters.getUserName());
        assertSame(SharedEntitySearchParameters.TextSearchMode.AND, parameters.getTextSearchMode());
    }

    private void assertHasStandardValues(final SharedEntitySearchParametersBuilder builder)
    {
        final SharedEntitySearchParameters parameters = builder.toSearchParameters();
        assertEquals(TestSharedEntitySearchParametersBuilder.NAME, parameters.getName());
        assertEquals(TestSharedEntitySearchParametersBuilder.DESCRIPTION, parameters.getDescription());
        assertEquals(Boolean.TRUE, parameters.getFavourite());
        assertEquals(PrivateShareTypeSearchParameter.PRIVATE_PARAMETER, parameters.getShareTypeParameter());
        assertEquals(SharedEntityColumn.NAME, parameters.getSortColumn());
        assertEquals(true, parameters.isAscendingSort());
        assertEquals(TestSharedEntitySearchParametersBuilder.USER_NAME, parameters.getUserName());
        assertSame(SharedEntitySearchParameters.TextSearchMode.AND, parameters.getTextSearchMode());
    }

    private SharedEntitySearchParametersBuilder createStandardTemplate()
    {
        final SharedEntitySearchParametersBuilder template = new SharedEntitySearchParametersBuilder();
        template.setName(TestSharedEntitySearchParametersBuilder.NAME);
        template.setDescription(TestSharedEntitySearchParametersBuilder.DESCRIPTION);
        template.setFavourite(Boolean.TRUE);
        template.setShareTypeParameter(PrivateShareTypeSearchParameter.PRIVATE_PARAMETER);
        template.setSortColumn(SharedEntityColumn.NAME, true);
        template.setUserName(TestSharedEntitySearchParametersBuilder.USER_NAME);
        template.setTextSearchMode(SharedEntitySearchParameters.TextSearchMode.AND);
        return template;
    }
}
