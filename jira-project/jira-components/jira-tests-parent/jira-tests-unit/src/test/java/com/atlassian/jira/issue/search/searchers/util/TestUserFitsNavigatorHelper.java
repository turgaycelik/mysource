package com.atlassian.jira.issue.search.searchers.util;

import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.Strict;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @since v4.0
 */
public class TestUserFitsNavigatorHelper
{
    @Test
    public void testCheckUserFoundByUserName()
    {
        final AtomicBoolean calledFindUserName = new AtomicBoolean(false);
        final UserPickerSearchService service = mock(UserPickerSearchService.class, new Strict());

        UserFitsNavigatorHelper helper = new UserFitsNavigatorHelper(service)
        {
            @Override
            String findUserName(final String name)
            {
                calledFindUserName.set(true);
                return name.toLowerCase();
            }
        };
        
        assertEquals("monkey", helper.checkUser("monkey"));
        assertTrue("calledFindUserName", calledFindUserName.get());
    }
    
    @Test
    public void testCheckUserFoundByUserNameUpperCase()
    {
        final AtomicBoolean calledFindUserName = new AtomicBoolean(false);
        final UserPickerSearchService service = mock(UserPickerSearchService.class, new Strict());

        UserFitsNavigatorHelper helper = new UserFitsNavigatorHelper(service)
        {
            @Override
            String findUserName(final String name)
            {
                calledFindUserName.set(true);
                return name.toLowerCase();
            }
        };

        assertEquals("monkey", helper.checkUser("Monkey"));
        assertTrue("calledFindUserName", calledFindUserName.get());
    }

    @Test
    public void testCheckUserNotFoundFullNameEnabledDoesNotExist()
    {
        final AtomicBoolean calledFindUserName = new AtomicBoolean(false);
        final AtomicBoolean calledFullNameExists = new AtomicBoolean(false);
        final UserPickerSearchService service = mock(UserPickerSearchService.class, new Strict());

        UserFitsNavigatorHelper helper = new UserFitsNavigatorHelper(service)
        {
            @Override
            String findUserName(final String name)
            {
                calledFindUserName.set(true);
                return null;
            }

            @Override
            boolean userExistsByFullNameOrEmail(final String name)
            {
                calledFullNameExists.set(true);
                return false;
            }
        };
        
        assertEquals("monkey", helper.checkUser("monkey"));
        assertTrue("calledFindUserName", calledFindUserName.get());
        assertTrue("calledFullNameExists", calledFullNameExists.get());
    }

    @Test
    public void testCheckUserNotFoundFullNameEnabledDoesExist()
    {
        final AtomicBoolean calledFindUserName = new AtomicBoolean(false);
        final AtomicBoolean calledFullNameExists = new AtomicBoolean(false);
        final UserPickerSearchService service = mock(UserPickerSearchService.class, new Strict());

        UserFitsNavigatorHelper helper = new UserFitsNavigatorHelper(service)
        {
            @Override
            String findUserName(final String name)
            {
                calledFindUserName.set(true);
                return null;
            }

            @Override
            boolean userExistsByFullNameOrEmail(final String name)
            {
                calledFullNameExists.set(true);
                return true;
            }
        };
        
        assertNull(helper.checkUser("monkey"));
        assertTrue("calledFindUserName", calledFindUserName.get());
        assertTrue("calledFullNameExists", calledFullNameExists.get());
    }
}
