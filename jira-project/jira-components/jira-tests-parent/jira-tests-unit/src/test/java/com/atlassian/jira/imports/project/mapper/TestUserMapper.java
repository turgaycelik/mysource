package com.atlassian.jira.imports.project.mapper;

import com.atlassian.jira.external.beans.ExternalUser;
import com.atlassian.jira.user.util.UserUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
@RunWith(MockitoJUnitRunner.class)
public class TestUserMapper
{
    @Test
    public void testRegisterExternalUser() throws Exception
    {
        ExternalUser externalUser1 = new ExternalUser("dudeKey", "dude", "Dude McMan", "dude@test.com", "dude");
        ExternalUser externalUser2 = new ExternalUser("duderKey", "duder", "Duder McMan", "duder@test.com", "duder");

        final UserUtil userUtil = Mockito.mock(UserUtil.class);
        UserMapper userMapper = new UserMapper(userUtil)
        {
            public boolean userExists(final String userName)
            {
                return true;
            }
        };
        userMapper.registerOldValue(externalUser1);
        userMapper.registerOldValue(externalUser2);

        assertEquals(2, userMapper.getRegisteredOldIds().size());
        assertEquals(externalUser1, userMapper.getExternalUser("dudeKey"));
        assertEquals(externalUser2, userMapper.getExternalUser("duderKey"));
    }

    @Test
    public void testRegisterNullExternalUser() throws Exception
    {
        final UserUtil userUtil = Mockito.mock(UserUtil.class);
        UserMapper userMapper = new UserMapper(userUtil);

        try
        {
            userMapper.registerOldValue(null);
            fail("should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }

        assertEquals(0, userMapper.getRegisteredOldIds().size());
    }

    @Test
    public void testFlagAsMandatorySimple() throws Exception
    {
        final UserUtil userUtil = Mockito.mock(UserUtil.class);
        UserMapper userMapper = new UserMapper(userUtil);

        userMapper.flagUserAsMandatory("dude");
        userMapper.flagUserAsMandatory("duder");
        assertEquals(2, userMapper.getRequiredOldIds().size());
        assertTrue(userMapper.getRequiredOldIds().contains("dude"));
        assertTrue(userMapper.getRequiredOldIds().contains("duder"));
    }

    @Test
    public void testFlagAsMandatoryOverridesInUse() throws Exception
    {
        final UserUtil userUtil = Mockito.mock(UserUtil.class);
        UserMapper userMapper = new UserMapper(userUtil)
        {
            public boolean userExists(final String userName)
            {
                return false;
            }
        };

        userMapper.flagUserAsInUse("dude");
        assertEquals(1, userMapper.getUnmappedUsersInUse().size());
        userMapper.flagUserAsMandatory("dude");
        // This should remove "dude" from In Use
        assertEquals(0, userMapper.getUnmappedUsersInUse().size());
        assertEquals(1, userMapper.getRequiredOldIds().size());
        assertTrue(userMapper.getRequiredOldIds().contains("dude"));
    }

    @Test
    public void testGetUnmappedMandatoryUsers() throws Exception
    {
        ExternalUser externalUser1 = new ExternalUser("dudeKey", "dude", "Dude McMan", "dude@test.com", "dude");
        ExternalUser externalUser2 = new ExternalUser("duderKey", "duder", "Duder McMan", "duder@test.com", "duder");

        final UserUtil userUtil = Mockito.mock(UserUtil.class);
        UserMapper userMapper = new UserMapper(userUtil)
        {
            public boolean userExists(final String userKey)
            {
                return userKey.equals("dudeKey");
            }
        };
                
        userMapper.registerOldValue(externalUser1);
        userMapper.registerOldValue(externalUser2);

        userMapper.flagUserAsMandatory("dudeKey");
        userMapper.flagUserAsMandatory("duderKey");

        assertEquals(1, userMapper.getUnmappedMandatoryUsers().size());
        assertEquals(externalUser2, userMapper.getUnmappedMandatoryUsers().iterator().next());
    }

    @Test
    public void testFlagAsInUseSimple()
    {
        final UserUtil userUtil = Mockito.mock(UserUtil.class);
        UserMapper userMapper = new UserMapper(userUtil)
        {
            public boolean userExists(final String userName)
            {
                return "fred".equals(userName);
            }
        };

        userMapper.flagUserAsInUse("dude");
        userMapper.flagUserAsInUse("duder");
        userMapper.flagUserAsInUse("fred");
        userMapper.flagUserAsInUse("barney");
        userMapper.registerOldValue(new ExternalUser("barney", "", ""));
        assertEquals(3, userMapper.getUnmappedUsersInUse().size());
        assertTrue(userMapper.getUnmappedUsersInUse().contains(new ExternalUser("dude", "dude", "", "", "")));
        assertTrue(userMapper.getUnmappedUsersInUse().contains(new ExternalUser("duder", "duder", "", "", "")));
        assertTrue(userMapper.getUnmappedUsersInUse().contains(new ExternalUser("barney", "barney", "", "", "")));
    }

    @Test
    public void testFlagAsInUseButAlsoMandatory()
    {
        final UserUtil userUtil = Mockito.mock(UserUtil.class);
        UserMapper userMapper = new UserMapper(userUtil)
        {
            public boolean userExists(final String userName)
            {
                return false;
            }
        };

        // Dude is required
        userMapper.flagUserAsMandatory("dude");
        // Now if dude is flagged as in use, this should be ignored.
        userMapper.flagUserAsInUse("dude");
        userMapper.flagUserAsInUse("duder");
        assertEquals(1, userMapper.getUnmappedUsersInUse().size());
        assertTrue(userMapper.getUnmappedUsersInUse().contains(new ExternalUser("duder", "duder", "", "", "")));
    }

    @Test
    public void testGetUnmappedRequiredUsersNoExternalUser() throws Exception
    {
        ExternalUser externalUser1 = new ExternalUser("dudeKey", "dude", "Dude McMan", "dude@test.com", "dude");
        ExternalUser externalUser2 = new ExternalUser("duderKey", "duder", "Duder McMan", "duder@test.com", "duder");

        final UserUtil userUtil = Mockito.mock(UserUtil.class);
        UserMapper userMapper = new UserMapper(userUtil)
        {
            public boolean userExists(final String userKey)
            {
                return "dudeKey".equals(userKey) || "duderKey".equals(userKey);
            }
        };

        userMapper.registerOldValue(externalUser1);
        userMapper.registerOldValue(externalUser2);

        userMapper.flagUserAsMandatory("dudeKey");
        userMapper.flagUserAsMandatory("duderKey");
        userMapper.flagUserAsMandatory("duderest");

        assertEquals(1, userMapper.getUnmappedMandatoryUsers().size());
        assertEquals(new ExternalUser("duderest", "duderest", "", "", ""), userMapper.getUnmappedMandatoryUsers().iterator().next());
    }

    @Test
    public void testGetUnmappedMandatoryUsersWithNoRegisteredOldValue() throws Exception
    {
        ExternalUser externalUser1 = new ExternalUser("dudeKey", "dude", "Dude McMan", "dude@test.com", "dude");
        ExternalUser externalUser2 = new ExternalUser("duderKey", "duder", "Duder McMan", "duder@test.com", "duder");

        final UserUtil userUtil = Mockito.mock(UserUtil.class);
        UserMapper userMapper = new UserMapper(userUtil)
        {
            public boolean userExists(final String userKey)
            {
                return "dudeKey".equals(userKey) || "barneyKey".equals(userKey);
            }
        };

        userMapper.registerOldValue(externalUser1);
        userMapper.registerOldValue(externalUser2);

        userMapper.flagUserAsMandatory("dudeKey");
        userMapper.flagUserAsMandatory("duderKey");
        userMapper.flagUserAsMandatory("fredKey");
        userMapper.flagUserAsMandatory("barneyKey");

        assertEquals(1, userMapper.getUnmappedMandatoryUsersWithNoRegisteredOldValue().size());
        assertEquals(new ExternalUser("fredKey", "fredKey", "", "", ""), userMapper.getUnmappedMandatoryUsersWithNoRegisteredOldValue().iterator().next());
    }

    @Test
    public void testGetUnmappedUsersInUseWithNoRegisteredOldValue() throws Exception
    {
        ExternalUser externalUser1 = new ExternalUser("dudeKey", "dude", "Dude McMan", "dude@test.com", "dude");
        ExternalUser externalUser2 = new ExternalUser("duderKey", "duder", "Duder McMan", "duder@test.com", "duder");

        final UserUtil userUtil = Mockito.mock(UserUtil.class);
        UserMapper userMapper = new UserMapper(userUtil)
        {
            public boolean userExists(final String userKey)
            {
                return "dudeKey".equals(userKey) || "barneyKey".equals(userKey);
            }
        };

        userMapper.registerOldValue(externalUser1);
        userMapper.registerOldValue(externalUser2);

        userMapper.flagUserAsInUse("dudeKey");
        userMapper.flagUserAsInUse("duderKey");
        userMapper.flagUserAsInUse("fredKey");
        userMapper.flagUserAsInUse("barneyKey");

        assertEquals(1, userMapper.getUnmappedUsersInUseWithNoRegisteredOldValue().size());
        assertEquals(new ExternalUser("fredKey", "fredKey", "", "", ""), userMapper.getUnmappedUsersInUseWithNoRegisteredOldValue().iterator().next());
    }

    @Test
    public void testGetUsersToAutoCreate() throws Exception
    {
        ExternalUser externalUser1 = new ExternalUser("dudeKey", "dude", "Dude McMan", "dude@test.com", "dude");
        ExternalUser externalUser2 = new ExternalUser("duderKey", "duder", "Duder McMan", "duder@test.com", "duder");

        final UserUtil userUtil = Mockito.mock(UserUtil.class);
        UserMapper userMapper = new UserMapper(userUtil)
        {
            public boolean userExists(final String userName)
            {
                return "bettyKey".equals(userName) || "wilmaKey".equals(userName);
            }
        };

        userMapper.registerOldValue(externalUser1);
        userMapper.registerOldValue(externalUser2);

        userMapper.flagUserAsInUse("dudeKey");
        userMapper.flagUserAsMandatory("duderKey");
        userMapper.flagUserAsInUse("fredKey");
        userMapper.flagUserAsMandatory("barneyKey");
        userMapper.flagUserAsInUse("wilmaKey");
        userMapper.flagUserAsMandatory("bettyKey");

        assertEquals(2, userMapper.getUsersToAutoCreate().size());
        assertTrue(userMapper.getUsersToAutoCreate().contains(externalUser1));
        assertTrue(userMapper.getUsersToAutoCreate().contains(externalUser2));
    }
}
