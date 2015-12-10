package com.atlassian.jira.user;

import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for {@link com.atlassian.jira.user.DefaultUserAdminHistoryManager}
 *
 * @since v4.1
 */
public class TestDefaultUserAdminHistoryManager extends MockControllerTestCase
{
    private UserHistoryManager historyManager;

    private UserAdminHistoryManager adminHistoryManager;
    private User user;

    @Before
    public void setUp() throws Exception
    {
        user = new MockUser("admin");
        historyManager = mockController.getMock(UserHistoryManager.class);

        adminHistoryManager = new DefaultUserAdminHistoryManager(historyManager);
    }

    @After
    public void tearDown() throws Exception
    {
        user = null;
        historyManager = null;
        adminHistoryManager = null;

    }

    @Test
    public void testAddAdminPageNull()
    {
        mockController.replay();

        try
        {
            adminHistoryManager.addAdminPageToHistory(user, null, null);
            fail("adminPage can not be bull");
        }
        catch (IllegalArgumentException e)
        {
            // pass
        }

        mockController.verify();
    }

    @Test
    public void testAddAdminPageNullUser()
    {
        final String key = "123";

        historyManager.addItemToHistory(UserHistoryItem.ADMIN_PAGE, (com.atlassian.crowd.embedded.api.User) null, "123", null);

        mockController.replay();

        adminHistoryManager.addAdminPageToHistory(null, key, null);

        mockController.verify();
    }

    @Test
    public void testAddProject()
    {
        final String key = "123";

        historyManager.addItemToHistory(UserHistoryItem.ADMIN_PAGE, user, "123", null);

        mockController.replay();

        adminHistoryManager.addAdminPageToHistory(user, key, null);

        mockController.verify();
    }


    @Test
    public void testGetAdminPageHistoryWithOutChecks()
    {
        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ADMIN_PAGE, "123");
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ADMIN_PAGE, "1234");
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ADMIN_PAGE, "1235");
        final UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ADMIN_PAGE, "1236");

        final List<UserHistoryItem> list = CollectionBuilder.newBuilder(item1, item2, item3, item4).asList();
        historyManager.getHistory(UserHistoryItem.ADMIN_PAGE, user);
        mockController.setReturnValue(list);

        mockController.replay();


        final List<UserHistoryItem> returnedList = adminHistoryManager.getAdminPageHistoryWithoutPermissionChecks(user);

        assertEquals(list, returnedList);

        mockController.verify();
    }

    @Test
    public void testGetAdminPageHistoryWithOutChecksNullUser()
    {
        final UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ADMIN_PAGE, "123");
        final UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ADMIN_PAGE, "1234");
        final UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ADMIN_PAGE, "1235");
        final UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ADMIN_PAGE, "1236");

        final List<UserHistoryItem> list = CollectionBuilder.newBuilder(item1, item2, item3, item4).asList();
        historyManager.getHistory(UserHistoryItem.ADMIN_PAGE, (com.atlassian.crowd.embedded.api.User) null);
        mockController.setReturnValue(list);

        mockController.replay();


        final List<UserHistoryItem> returnedList = adminHistoryManager.getAdminPageHistoryWithoutPermissionChecks(null);

        assertEquals(list, returnedList);

        mockController.verify();
    }

    @Test
    public void testGetAdminPageHistoryWithOutChecksNullHistory()
    {
        historyManager.getHistory(UserHistoryItem.ADMIN_PAGE, user);
        mockController.setReturnValue(null);

        mockController.replay();


        assertTrue(adminHistoryManager.getAdminPageHistoryWithoutPermissionChecks(user).isEmpty());

        mockController.verify();
    }


}
