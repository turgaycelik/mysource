package com.atlassian.jira.rest.v2.issue.users;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.MockUser;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @since v5.2
 */
@RunWith (MockitoJUnitRunner.class)
public class UserPickerResourceHelperTest
{
    private static final User USER_A = new MockUser("a");
    private static final User USER_B = new MockUser("b");
    private static final User USER_C = new MockUser("c");

    private static final List<User> USER_LIST = Arrays.asList(USER_A, USER_B, USER_C);

    @Test
    public void testLimitUserSearch()
    {
        UserPickerResourceHelper userPickerResourceHelper = new UserPickerResourceHelperImpl(null, null, null, null, null);

        List<User> returnedList = userPickerResourceHelper.limitUserSearch(null, null, USER_LIST, null);
        assertEquals(USER_LIST, returnedList);

        returnedList = userPickerResourceHelper.limitUserSearch(-1, -1, USER_LIST, null);
        assertEquals(USER_LIST, returnedList);

        returnedList = userPickerResourceHelper.limitUserSearch(1, null, USER_LIST, null);
        assertTrue(Iterables.elementsEqual(Iterables.skip(USER_LIST, 1), returnedList));

        returnedList = userPickerResourceHelper.limitUserSearch(USER_LIST.size() + 1, null, USER_LIST, null);
        assertTrue(returnedList.isEmpty());

        returnedList = userPickerResourceHelper.limitUserSearch(null, 1, USER_LIST, null);
        assertTrue(Iterables.elementsEqual(Iterables.limit(USER_LIST, 1), returnedList));

        returnedList = userPickerResourceHelper.limitUserSearch(null, 0, USER_LIST, null);
        assertTrue(returnedList.isEmpty());

        returnedList = userPickerResourceHelper.limitUserSearch(null, null, USER_LIST, Collections.<String>emptyList());
        assertEquals(USER_LIST, returnedList);

        returnedList = userPickerResourceHelper.limitUserSearch(null, null, USER_LIST, Arrays.asList(USER_A.getName()));
        assertTrue(Iterables.elementsEqual(Collections2.filter(USER_LIST, new Predicate<User>()
        {
            @Override
            public boolean apply(User input)
            {
                return input != USER_A;
            }
        }), returnedList));

        returnedList = userPickerResourceHelper.limitUserSearch(1, 1, USER_LIST, null);
        assertTrue(Iterables.elementsEqual(Iterables.limit(Iterables.skip(USER_LIST, 1), 1), returnedList));
    }
}
