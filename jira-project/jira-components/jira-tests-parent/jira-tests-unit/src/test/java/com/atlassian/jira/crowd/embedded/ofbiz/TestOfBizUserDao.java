package com.atlassian.jira.crowd.embedded.ofbiz;

import com.atlassian.beehive.ClusterLockService;
import com.atlassian.beehive.simple.SimpleClusterLockService;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.user.util.MockUserKeyStore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Clean unit tests for OfBizUserDao.
 *
 * In particular it tests the various rename user scenarios set out in https://extranet.atlassian.com/display/JIRADEV/Detect+rename+user+in+LDAP+Scenarios .
 */
public class TestOfBizUserDao extends AbstractTransactionalOfBizTestCase
{
    private CacheManager cacheManager;
    @Mock
    private ClusterLockService clusterLockService;

    @Before
    public void setup()
    {
        cacheManager = new MemoryCacheManager();
        clusterLockService = new SimpleClusterLockService();
    }

    @Test
    public void testRenameUserCaseOnly() throws Exception
    {
        final OfBizDelegator ofBizDelegator = new MockOfBizDelegator();
        final DirectoryDao directoryDao = new MockDirectoryDao(1L, 2L);
        final MockUserKeyStore userKeyStore = new MockUserKeyStore();
        userKeyStore.useDefaultMapping(false);
        final InternalMembershipDao internalMembershipDao = new OfBizInternalMembershipDao(ofBizDelegator, cacheManager);
        OfBizUserDao ofBizUserDao = new OfBizUserDao(ofBizDelegator, directoryDao, internalMembershipDao, userKeyStore, null, cacheManager, clusterLockService);
        final User oldUser = createUser(1, "aSmith");
        ofBizUserDao.add(oldUser, PasswordCredential.NONE);

        assertEquals("asmith", userKeyStore.getUsernameForKey("asmith"));
        assertEquals("aSmith", ofBizUserDao.findByName(1L, "asmith").getName());

        ofBizUserDao.rename(oldUser, "ASMITH");
        assertEquals("asmith", userKeyStore.getUsernameForKey("asmith"));
        assertEquals("asmith", userKeyStore.getKeyForUsername("asmith"));
        assertEquals("ASMITH", ofBizUserDao.findByName(1L, "asmith").getName());
    }

    @Test
    public void testRenameUserCaseOnlyHandlesGroupMemberships() throws Exception
    {
        final OfBizDelegator ofBizDelegator = new MockOfBizDelegator();
        final DirectoryDao directoryDao = new MockDirectoryDao(1L, 2L);
        final MockUserKeyStore userKeyStore = new MockUserKeyStore();
        userKeyStore.useDefaultMapping(false);
        final InternalMembershipDao internalMembershipDao = new OfBizInternalMembershipDao(ofBizDelegator, cacheManager);
        OfBizUserDao ofBizUserDao = new OfBizUserDao(ofBizDelegator, directoryDao, internalMembershipDao, userKeyStore, null, cacheManager, clusterLockService);
        final User oldUser = ofBizUserDao.add(createUser(1, "ASmith"), PasswordCredential.NONE);
        internalMembershipDao.addUserToGroup(1, (UserOrGroupStub) oldUser, new SimpleUserOrGroupStub(12, 1, "jira-devs"));

        // Preconditions
        assertEquals("asmith", userKeyStore.getUsernameForKey("asmith"));
        assertTrue(internalMembershipDao.isUserDirectMember(1, "asmith", "jira-devs"));

        ofBizUserDao.rename(oldUser, "ASMITH");
        assertEquals("asmith", userKeyStore.getUsernameForKey("asmith"));
        assertEquals("asmith", userKeyStore.getKeyForUsername("asmith"));
        assertNotNull(ofBizUserDao.findByName(1L, "ASMITH"));
        assertTrue(internalMembershipDao.isUserDirectMember(1, "asmith", "jira-devs"));
    }

    @Test
    public void testRenameUserSimple() throws Exception
    {
        final OfBizDelegator ofBizDelegator = new MockOfBizDelegator();
        final DirectoryDao directoryDao = new MockDirectoryDao(1L, 2L);
        final MockUserKeyStore userKeyStore = new MockUserKeyStore();
        userKeyStore.useDefaultMapping(false);
        final InternalMembershipDao internalMembershipDao = new OfBizInternalMembershipDao(ofBizDelegator, cacheManager);
        OfBizUserDao ofBizUserDao = new OfBizUserDao(ofBizDelegator, directoryDao, internalMembershipDao, userKeyStore, null, cacheManager, clusterLockService);
        final User oldUser = createUser(1, "asmith");
        ofBizUserDao.add(oldUser, PasswordCredential.NONE);

        assertEquals("asmith", userKeyStore.getUsernameForKey("asmith"));

        ofBizUserDao.rename(oldUser, "abrown");
        assertEquals("abrown", userKeyStore.getUsernameForKey("asmith"));
        assertEquals(null, userKeyStore.getUsernameForKey("abrown"));
        assertEquals(null, userKeyStore.getKeyForUsername("asmith"));
        assertEquals("asmith", userKeyStore.getKeyForUsername("abrown"));
        assertNotNull(ofBizUserDao.findByName(1L, "abrown"));
        // Assert asmith is gone from cwd_user
        try
        {
            ofBizUserDao.findByName(1L, "asmith");
            fail();
        }
        catch (UserNotFoundException expected) {}
    }

    @Test
    public void testRenameUserScenario1() throws Exception
    {
        // User B is renamed to A; A previously existed but has been deleted.

        final OfBizDelegator ofBizDelegator = new MockOfBizDelegator();
        final DirectoryDao directoryDao = new MockDirectoryDao(1L, 2L);
        final MockUserKeyStore userKeyStore = new MockUserKeyStore();
        userKeyStore.useDefaultMapping(false);
        final InternalMembershipDao internalMembershipDao = new OfBizInternalMembershipDao(ofBizDelegator, cacheManager);
        OfBizUserDao ofBizUserDao = new OfBizUserDao(ofBizDelegator, directoryDao, internalMembershipDao, userKeyStore, null, cacheManager, clusterLockService);
        final User oldUser = createUser(1, "asmith");
        ofBizUserDao.add(oldUser, PasswordCredential.NONE);
        userKeyStore.ensureUniqueKeyForNewUser("abrown");

        assertEquals("asmith", userKeyStore.getUsernameForKey("asmith"));
        assertEquals("abrown", userKeyStore.getUsernameForKey("abrown"));

        ofBizUserDao.rename(oldUser, "abrown");
        assertEquals("abrown", userKeyStore.getUsernameForKey("asmith"));
        assertEquals("abrown#1", userKeyStore.getUsernameForKey("abrown"));
        assertEquals(null, userKeyStore.getKeyForUsername("asmith"));
        assertEquals("asmith", userKeyStore.getKeyForUsername("abrown"));
        assertEquals("abrown", userKeyStore.getKeyForUsername("abrown#1"));
        assertNotNull(ofBizUserDao.findByName(1L, "abrown"));
        // Assert asmith is gone from cwd_user
        try
        {
            ofBizUserDao.findByName(1L, "asmith");
            fail();
        }
        catch (UserNotFoundException expected) {}
    }

    @Test
    public void testRenameUserScenario2() throws Exception
    {
        // User B is renamed to A, but A already exists in another directory, higher than this one.

        final OfBizDelegator ofBizDelegator = new MockOfBizDelegator();
        final DirectoryDao directoryDao = new MockDirectoryDao(1L, 2L);
        final MockUserKeyStore userKeyStore = new MockUserKeyStore();
        userKeyStore.useDefaultMapping(false);
        final InternalMembershipDao internalMembershipDao = new OfBizInternalMembershipDao(ofBizDelegator, cacheManager);
        OfBizUserDao ofBizUserDao = new OfBizUserDao(ofBizDelegator, directoryDao, internalMembershipDao, userKeyStore, null, cacheManager, clusterLockService);
        final User oldUser = createUser(2, "asmith");
        ofBizUserDao.add(oldUser, PasswordCredential.NONE);
        ofBizUserDao.add(createUser(1, "abrown"), PasswordCredential.NONE);

        assertEquals("asmith", userKeyStore.getUsernameForKey("asmith"));
        assertEquals("abrown", userKeyStore.getUsernameForKey("abrown"));

        ofBizUserDao.rename(oldUser, "abrown");
        assertEquals("asmith", userKeyStore.getUsernameForKey("asmith"));
        assertEquals("abrown", userKeyStore.getUsernameForKey("abrown"));
        assertEquals("asmith", userKeyStore.getKeyForUsername("asmith"));
        assertEquals("abrown", userKeyStore.getKeyForUsername("abrown"));
        assertNotNull(ofBizUserDao.findByName(1L, "abrown"));
        assertNotNull(ofBizUserDao.findByName(2L, "abrown"));
        // Assert asmith is gone from cwd_user
        try
        {
            ofBizUserDao.findByName(2L, "asmith");
            fail();
        }
        catch (UserNotFoundException expected) {}
    }

    @Test
    public void testRenameUserScenario3() throws Exception
    {
        // https://extranet.atlassian.com/display/JIRADEV/Detect+rename+user+in+LDAP+Scenarios
        // User B is renamed to A, but A already exists in another directory, lower than this one.

        final OfBizDelegator ofBizDelegator = new MockOfBizDelegator();
        final DirectoryDao directoryDao = new MockDirectoryDao(2L, 1L);
        final MockUserKeyStore userKeyStore = new MockUserKeyStore();
        userKeyStore.useDefaultMapping(false);
        final InternalMembershipDao internalMembershipDao = new OfBizInternalMembershipDao(ofBizDelegator, cacheManager);
        OfBizUserDao ofBizUserDao = new OfBizUserDao(ofBizDelegator, directoryDao, internalMembershipDao, userKeyStore, null, cacheManager, clusterLockService);
        final User oldUser = createUser(2, "asmith");
        ofBizUserDao.add(oldUser, PasswordCredential.NONE);
        ofBizUserDao.add(createUser(1, "abrown"), PasswordCredential.NONE);

        assertEquals("asmith", userKeyStore.getUsernameForKey("asmith"));
        assertEquals("abrown", userKeyStore.getUsernameForKey("abrown"));

        ofBizUserDao.rename(oldUser, "abrown");
        assertEquals("abrown", userKeyStore.getUsernameForKey("asmith"));
        assertEquals("abrown#1", userKeyStore.getUsernameForKey("abrown"));
        assertEquals(null, userKeyStore.getKeyForUsername("asmith"));
        assertEquals("asmith", userKeyStore.getKeyForUsername("abrown"));
        assertEquals("abrown", userKeyStore.getKeyForUsername("abrown#1"));
        assertNotNull(ofBizUserDao.findByName(1L, "abrown"));
        assertNotNull(ofBizUserDao.findByName(2L, "abrown"));
        // Assert asmith is gone from cwd_user
        try
        {
            ofBizUserDao.findByName(2L, "asmith");
            fail();
        }
        catch (UserNotFoundException expected) {}
        // Assert abrown#1 is not in cwd_user
        try
        {
            ofBizUserDao.findByName(2L, "abrown#1");
            fail();
        }
        catch (UserNotFoundException expected) {}
    }

    @Test
    public void testRenameUserScenario4() throws Exception
    {
        // https://extranet.atlassian.com/display/JIRADEV/Detect+rename+user+in+LDAP+Scenarios
        // User B is renamed to A, but A currently exists in this directory

        final OfBizDelegator ofBizDelegator = new MockOfBizDelegator();
        final DirectoryDao directoryDao = new MockDirectoryDao(1L);
        final MockUserKeyStore userKeyStore = new MockUserKeyStore();
        userKeyStore.useDefaultMapping(false);
        final InternalMembershipDao internalMembershipDao = new OfBizInternalMembershipDao(ofBizDelegator, cacheManager);
        OfBizUserDao ofBizUserDao = new OfBizUserDao(ofBizDelegator, directoryDao, internalMembershipDao, userKeyStore, null, cacheManager, clusterLockService);
        final User oldUser = createUser(1, "asmith");
        ofBizUserDao.add(oldUser, PasswordCredential.NONE);
        ofBizUserDao.add(createUser(1, "abrown"), PasswordCredential.NONE);

        assertEquals("asmith", userKeyStore.getUsernameForKey("asmith"));
        assertEquals("abrown", userKeyStore.getUsernameForKey("abrown"));

        ofBizUserDao.rename(oldUser, "abrown");
        assertEquals("abrown", userKeyStore.getUsernameForKey("asmith"));
        assertEquals("abrown#1", userKeyStore.getUsernameForKey("abrown"));
        assertEquals(null, userKeyStore.getKeyForUsername("asmith"));
        assertEquals("asmith", userKeyStore.getKeyForUsername("abrown"));
        assertEquals("abrown", userKeyStore.getKeyForUsername("abrown#1"));
        assertNotNull(ofBizUserDao.findByName(1L, "abrown"));
        assertNotNull(ofBizUserDao.findByName(1L, "abrown#1"));
        // Assert asmith is gone from cwd_user
        try
        {
            ofBizUserDao.findByName(2L, "asmith");
            fail();
        }
        catch (UserNotFoundException expected) {}
    }

    @Test
    public void testRenameUserScenario5() throws Exception
    {
        // User B is renamed to A, A already exists in this directory, but is shadowed by A in a higher directory

        final OfBizDelegator ofBizDelegator = new MockOfBizDelegator();
        final DirectoryDao directoryDao = new MockDirectoryDao(1L, 2L);
        final MockUserKeyStore userKeyStore = new MockUserKeyStore();
        userKeyStore.useDefaultMapping(false);
        final InternalMembershipDao internalMembershipDao = new OfBizInternalMembershipDao(ofBizDelegator, cacheManager);
        OfBizUserDao ofBizUserDao = new OfBizUserDao(ofBizDelegator, directoryDao, internalMembershipDao, userKeyStore, null, cacheManager, clusterLockService);
        final User oldUser = createUser(2, "asmith");
        ofBizUserDao.add(oldUser, PasswordCredential.NONE);
        ofBizUserDao.add(createUser(1, "abrown"), PasswordCredential.NONE);
        ofBizUserDao.add(createUser(2, "abrown"), PasswordCredential.NONE);

        assertEquals("asmith", userKeyStore.getUsernameForKey("asmith"));
        assertEquals("abrown", userKeyStore.getUsernameForKey("abrown"));

        ofBizUserDao.rename(oldUser, "abrown");
        assertEquals("asmith", userKeyStore.getUsernameForKey("asmith"));
        assertEquals("abrown", userKeyStore.getUsernameForKey("abrown"));
        assertEquals(null, userKeyStore.getUsernameForKey("asmith#1"));
        assertEquals(null, userKeyStore.getUsernameForKey("abrown#1"));
        assertEquals("asmith", userKeyStore.getKeyForUsername("asmith"));
        assertEquals("abrown", userKeyStore.getKeyForUsername("abrown"));
        assertNotNull(ofBizUserDao.findByName(1L, "abrown"));
        assertNotNull(ofBizUserDao.findByName(2L, "abrown"));
        // Assert asmith is gone from cwd_user
        try
        {
            ofBizUserDao.findByName(2L, "asmith");
            fail();
        }
        catch (UserNotFoundException expected) {}
    }

    @Test
    public void testRenameUserScenario6() throws Exception
    {
        // https://extranet.atlassian.com/display/JIRADEV/Detect+rename+user+in+LDAP+Scenarios
        // User B is renamed to A, A already exists in this directory, and is shadowing A in a lower directory

        final OfBizDelegator ofBizDelegator = new MockOfBizDelegator();
        final DirectoryDao directoryDao = new MockDirectoryDao(2L, 1L);
        final MockUserKeyStore userKeyStore = new MockUserKeyStore();
        userKeyStore.useDefaultMapping(false);
        final InternalMembershipDao internalMembershipDao = new OfBizInternalMembershipDao(ofBizDelegator, cacheManager);
        OfBizUserDao ofBizUserDao = new OfBizUserDao(ofBizDelegator, directoryDao, internalMembershipDao, userKeyStore, null, cacheManager, clusterLockService);
        final User oldUser = createUser(2, "asmith");
        ofBizUserDao.add(oldUser, PasswordCredential.NONE);
        ofBizUserDao.add(createUser(1, "abrown"), PasswordCredential.NONE);
        ofBizUserDao.add(createUser(2, "abrown"), PasswordCredential.NONE);

        assertEquals("asmith", userKeyStore.getUsernameForKey("asmith"));
        assertEquals("abrown", userKeyStore.getUsernameForKey("abrown"));

        ofBizUserDao.rename(oldUser, "abrown");
        assertEquals("abrown", userKeyStore.getUsernameForKey("asmith"));
        assertEquals("abrown#1", userKeyStore.getUsernameForKey("abrown"));
        assertEquals(null, userKeyStore.getKeyForUsername("asmith"));
        assertEquals("asmith", userKeyStore.getKeyForUsername("abrown"));
        assertEquals("abrown", userKeyStore.getKeyForUsername("abrown#1"));
        assertNotNull(ofBizUserDao.findByName(1L, "abrown"));
        assertNotNull(ofBizUserDao.findByName(2L, "abrown"));
        assertNotNull(ofBizUserDao.findByName(2L, "abrown#1"));
        // Assert asmith is gone from cwd_user
        try
        {
            ofBizUserDao.findByName(2L, "asmith");
            fail();
        }
        catch (UserNotFoundException expected) {}
    }

    @Test
    public void testRenameUserScenario7() throws Exception
    {
        // https://extranet.atlassian.com/display/JIRADEV/Detect+rename+user+in+LDAP+Scenarios
        // User B is renamed to A, B was shadowing another user
        final OfBizDelegator ofBizDelegator = new MockOfBizDelegator();
        final DirectoryDao directoryDao = new MockDirectoryDao(2L, 1L);
        final MockUserKeyStore userKeyStore = new MockUserKeyStore();
        userKeyStore.useDefaultMapping(false);
        final InternalMembershipDao internalMembershipDao = new OfBizInternalMembershipDao(ofBizDelegator, cacheManager);
        OfBizUserDao ofBizUserDao = new OfBizUserDao(ofBizDelegator, directoryDao, internalMembershipDao, userKeyStore, null, cacheManager, clusterLockService);
        final User oldUser = createUser(2, "asmith");
        ofBizUserDao.add(oldUser, PasswordCredential.NONE);
        ofBizUserDao.add(createUser(1, "asmith"), PasswordCredential.NONE);

        assertEquals("asmith", userKeyStore.getUsernameForKey("asmith"));
        assertEquals(null, userKeyStore.getUsernameForKey("abrown"));

        ofBizUserDao.rename(oldUser, "abrown");
        assertEquals("asmith", userKeyStore.getKeyForUsername("abrown"));
        assertEquals("XYZ10001", userKeyStore.getKeyForUsername("asmith"));
        assertEquals("abrown", userKeyStore.getUsernameForKey("asmith"));
        assertEquals("asmith", userKeyStore.getUsernameForKey("XYZ10001"));
        assertNotNull(ofBizUserDao.findByName(2L, "abrown"));
        assertNotNull(ofBizUserDao.findByName(1L, "asmith"));
        // Assert asmith is gone from cwd_user
        try
        {
            ofBizUserDao.findByName(2L, "asmith");
            fail();
        }
        catch (UserNotFoundException expected) {}
    }

    @Test
    public void testRenameUserScenario8() throws Exception
    {
        // https://extranet.atlassian.com/display/JIRADEV/Detect+rename+user+in+LDAP+Scenarios
        // User B is renamed to A, B is currently shadowed

        final OfBizDelegator ofBizDelegator = new MockOfBizDelegator();
        final DirectoryDao directoryDao = new MockDirectoryDao(1L, 2L);
        final MockUserKeyStore userKeyStore = new MockUserKeyStore();
        userKeyStore.useDefaultMapping(false);
        final InternalMembershipDao internalMembershipDao = new OfBizInternalMembershipDao(ofBizDelegator, cacheManager);
        OfBizUserDao ofBizUserDao = new OfBizUserDao(ofBizDelegator, directoryDao, internalMembershipDao, userKeyStore, null, cacheManager, clusterLockService);
        final User oldUser = createUser(2, "asmith");
        ofBizUserDao.add(oldUser, PasswordCredential.NONE);
        ofBizUserDao.add(createUser(1, "asmith"), PasswordCredential.NONE);

        assertEquals("asmith", userKeyStore.getUsernameForKey("asmith"));
        assertEquals(null, userKeyStore.getUsernameForKey("abrown"));

        ofBizUserDao.rename(oldUser, "abrown");
        assertEquals("abrown", userKeyStore.getKeyForUsername("abrown"));
        assertEquals("asmith", userKeyStore.getKeyForUsername("asmith"));
        assertEquals("asmith", userKeyStore.getUsernameForKey("asmith"));
        assertEquals("abrown", userKeyStore.getUsernameForKey("abrown"));
        assertNotNull(ofBizUserDao.findByName(2L, "abrown"));
        assertNotNull(ofBizUserDao.findByName(1L, "asmith"));
        // Assert asmith is gone from cwd_user
        try
        {
            ofBizUserDao.findByName(2L, "asmith");
            fail();
        }
        catch (UserNotFoundException expected) {}
    }

    private User createUser(final int directoryId, final String username)
    {
        return new UserTemplate(username, directoryId);
    }
}
