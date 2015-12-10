package com.atlassian.jira.crowd.embedded.ofbiz;

import java.util.Collection;
import java.util.List;

import com.atlassian.cache.Cache;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Visitor;

import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class UserOrGroupCacheTest
{
    private static final long DIRECTORY_ID1 = 1;
    private static final long DIRECTORY_ID2 = 2;
    private static final long NOT_EXISTING_DIRECTORY_ID = -1;

    private static final DirectoryEntityKey USER1_KEY = DirectoryEntityKey.getKeyLowerCase(DIRECTORY_ID1, "user1");
    private static final DirectoryEntityKey USER2_KEY = DirectoryEntityKey.getKeyLowerCase(DIRECTORY_ID1, "user2");
    private static final DirectoryEntityKey USER3_KEY = DirectoryEntityKey.getKeyLowerCase(DIRECTORY_ID2, "user3");

    private OfBizUser user1;
    private OfBizUser user2;
    private OfBizUser user3;

    private Cache<DirectoryEntityKey, OfBizUser> innerCache;

    /**
     * This abstract class is mocked. In order to call its methods use an Answer with CALLS_REAL_METHODS.
     */
    private UserOrGroupCache<OfBizUser> cache;

    @SuppressWarnings ("unchecked")
    @Before
    public void setUp()
    {
        cache = mock(UserOrGroupCache.class);
        innerCache = mock(Cache.class);

        when(cache.getCache()).thenReturn(innerCache);
        final Collection<DirectoryEntityKey> keys = ImmutableSet.of(USER1_KEY, USER2_KEY, USER3_KEY);
        when(innerCache.getKeys()).thenReturn(keys);

        user1 = mock(OfBizUser.class);
        when(innerCache.get(USER1_KEY)).thenReturn(user1);
        user2 = mock(OfBizUser.class);
        when(innerCache.get(USER2_KEY)).thenReturn(user2);
        user3 = mock(OfBizUser.class);
        when(innerCache.get(USER3_KEY)).thenReturn(user3);
    }

    @Test
    public void testVisitAllInDirectory1() throws Exception
    {
        testVisitAllInDirectory(DIRECTORY_ID1, user1, user2);
    }

    @Test
    public void testVisitAllInDirectory2() throws Exception
    {
        testVisitAllInDirectory(DIRECTORY_ID2, user3);
    }

    @Test
    public void testVisitAllInNonExistingDirectory() throws Exception
    {
        testVisitAllInDirectory(NOT_EXISTING_DIRECTORY_ID);
    }

    void testVisitAllInDirectory(long directoryId, OfBizUser... expectedUsers)
    {
        //given
        Visitor<OfBizUser> visitor = mock(Visitor.class);
        doAnswer(CALLS_REAL_METHODS.get()).when(cache).visitAllInDirectory(directoryId, visitor);

        // when
        cache.visitAllInDirectory(directoryId, visitor);

        // then
        for (OfBizUser expectedUser : expectedUsers)
        {
            verify(visitor).visit(expectedUser);
        }
        verifyNoMoreInteractions(visitor);
    }

    @Test
    public void getGetAllInDirectory1() throws Exception
    {
        testGetAllInDirectory(DIRECTORY_ID1, user1, user2);
    }

    @Test
    public void getGetAllInDirectory2() throws Exception
    {
        testGetAllInDirectory(DIRECTORY_ID2, user3);
    }

    @Test
    public void getGetAllInNonExistingDirectory() throws Exception
    {
        testGetAllInDirectory(NOT_EXISTING_DIRECTORY_ID);
    }

    public void testGetAllInDirectory(long directoryId, OfBizUser... expectedUsers) throws Exception
    {
        // given
        doAnswer(CALLS_REAL_METHODS.get()).when(cache).visitAllInDirectory(eq(directoryId), any(Visitor.class));
        doAnswer(CALLS_REAL_METHODS.get()).when(cache).getAllInDirectory(eq(directoryId), any(Function.class));
        Function<OfBizUser, OfBizUser> mockIdentityFunction = mock(Function.class);
        // returns the argument it was called with
        when(mockIdentityFunction.get(any(OfBizUser.class))).thenAnswer(new Answer<OfBizUser>()
        {
            @Override
            public OfBizUser answer(final InvocationOnMock invocation) throws Throwable
            {
                return (OfBizUser) invocation.getArguments()[0];
            }
        });

        // when
        List<OfBizUser> allInDirectory = cache.getAllInDirectory(directoryId, mockIdentityFunction);

        // then
        assertThat(allInDirectory, containsInAnyOrder(expectedUsers));
        verify(cache).visitAllInDirectory(eq(directoryId), any(Visitor.class));
        for (OfBizUser expectedUser : expectedUsers)
        {
            verify(mockIdentityFunction).get(expectedUser);
        }
        verifyNoMoreInteractions(mockIdentityFunction);
    }

}