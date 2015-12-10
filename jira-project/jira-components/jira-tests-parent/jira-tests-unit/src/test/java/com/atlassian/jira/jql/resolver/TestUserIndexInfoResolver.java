package com.atlassian.jira.jql.resolver;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserKeyService;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestUserIndexInfoResolver
{
    private static final List<String> EMPTY = Collections.emptyList();

    private static final Long ID_LONG = 10L;
    private static final String ID_STRING = "10";
    private static final String USER_KEY = "ID12345";
    private static final String USER_NAME = "name";
    private static final String OTHER_NAME = "otherName";

    @Mock private UserResolver userResolver;
    @Mock private UserKeyService userKeyService;

    @After
    public void tearDown()
    {
        userResolver = null;
        userKeyService = null;
    }

    @Test
    public void testGetIdsFromNameStringUserResolverReturnsEmpty() throws Exception
    {
        when(userResolver.getIdsFromName(USER_NAME)).thenReturn(EMPTY);

        assertEquals(emptyList(), resolver().getIndexedValues(USER_NAME));
    }
                                      
    @Test
    public void testGetIdsFromNameLongUserResolverReturnsEmpty() throws Exception
    {
        when(userResolver.getIdsFromName(ID_STRING)).thenReturn(EMPTY);

        assertEquals(emptyList(), resolver().getIndexedValues(ID_LONG));
    }

    @Test
    public void testGetIdsFromNameStringUserResolverReturnsList() throws Exception
    {
        when(userResolver.getIdsFromName(USER_NAME)).thenReturn(asList(USER_KEY));

        assertEquals(asList(USER_KEY), resolver().getIndexedValues(USER_NAME));
    }
                                      
    @Test
    public void testGetIdsFromNameLongUserResolverReturnsList() throws Exception
    {
        when(userResolver.getIdsFromName(ID_STRING)).thenReturn(asList(USER_KEY));

        assertEquals(asList(USER_KEY), resolver().getIndexedValues(ID_LONG));
    }

    @Test
    public void testGetIndexedValueForUser() throws Exception
    {
        when(userKeyService.getKeyForUsername(USER_NAME)).thenReturn(USER_KEY);

        assertEquals(USER_KEY, resolver().getIndexedValue(new MockUser(USER_NAME)));
    }

    @Test
    public void testGetIndexedValueForUnknownUser() throws Exception
    {
        assertNull(resolver().getIndexedValue(new MockUser(OTHER_NAME)));
    }

    // So we won't have to init the ComponentAccessor...
    UserIndexInfoResolver resolver()
    {
        return new UserIndexInfoResolver(userResolver)
        {
            @Override
            UserKeyService getUserKeyService()
            {
                return userKeyService;
            }
        };
    }
}

