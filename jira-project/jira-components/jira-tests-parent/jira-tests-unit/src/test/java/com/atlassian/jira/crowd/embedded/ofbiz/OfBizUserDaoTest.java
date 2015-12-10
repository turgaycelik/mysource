package com.atlassian.jira.crowd.embedded.ofbiz;

import java.util.List;

import com.atlassian.beehive.simple.SimpleClusterLockService;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.UserAlreadyExistsException;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.util.BatchResult;
import com.atlassian.jira.crowd.embedded.TestData;
import com.atlassian.jira.entity.EntityEngineImpl;
import com.atlassian.jira.event.MockEventPublisher;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.user.MockUserDeleteVeto;
import com.atlassian.jira.user.util.UserKeyStoreImpl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OfBizUserDaoTest extends AbstractTransactionalOfBizTestCase
{

    private static final long NOT_EXISTING_DIRECTORY_ID = -1;
    private OfBizUserDao userDao;
    private OfBizDirectoryDao directoryDao;
    private MockUserDeleteVeto userDeleteVeto;
    private UserKeyStoreImpl userKeyStore;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception
    {
        CacheManager cacheManager = new MemoryCacheManager();
        directoryDao = new OfBizDirectoryDao(getOfBizDelegator(), cacheManager);
        final OfBizInternalMembershipDao internalmembershipDao = new OfBizInternalMembershipDao(getOfBizDelegator(), cacheManager);
        final OfBizDelegator ofBizDelegator = getOfBizDelegator();
        userKeyStore = new UserKeyStoreImpl(new EntityEngineImpl(ofBizDelegator), ofBizDelegator, getGenericDelegator(), new MockEventPublisher(), cacheManager);
        userDeleteVeto = new MockUserDeleteVeto();
        userDao = new OfBizUserDao(getOfBizDelegator(), directoryDao, internalmembershipDao, userKeyStore, userDeleteVeto, cacheManager, new SimpleClusterLockService());
    }

    @After
    public void tearDown() throws Exception
    {
        userDao = null;
    }

    @Test
    public void testAddAndFindUserByName() throws Exception
    {
        final User createdUser = userDao.add(TestData.User.getTestData(), TestData.User.CREDENTIAL);

        TestData.User.assertEqualsTestUser(createdUser);

        final User retrievedUser = userDao.findByName(TestData.DIRECTORY_ID, TestData.User.NAME);

        TestData.User.assertEqualsTestUser(retrievedUser);
    }

    @Test
    public void testAddAndFindUserByExternalId() throws Exception
    {
        final User createdUser = userDao.add(TestData.User.getTestData("1"), TestData.User.CREDENTIAL);

        TestData.User.assertEqualsTestUser(createdUser);

        final User retrievedUser = userDao.findByExternalId(TestData.DIRECTORY_ID, "1");

        TestData.User.assertEqualsTestUser(retrievedUser);

    }

    @Test
    public void testGetAllExternalIds() throws Exception
    {
        // given
        Long dir1Id = directoryDao.add(new DirectoryImpl("dir1", DirectoryType.INTERNAL, "")).getId();
        Long dir2Id = directoryDao.add(new DirectoryImpl("dir2", DirectoryType.INTERNAL, "")).getId();

        userDao.add(TestData.User.getTestData(dir1Id, "9"), TestData.User.CREDENTIAL);
        // add user with null externalId
        userDao.add(TestData.User.getUser("user1", dir1Id, true, "Some", "User", "SomeUser", "SomeUser@somemail.com", null), TestData.User.CREDENTIAL);
        userDao.add(TestData.User.getTestData(dir2Id, "2"), TestData.User.CREDENTIAL);

        // make sure the null is not present in returned set
        assertThat(userDao.getAllExternalIds(dir1Id), containsInAnyOrder("9"));
        assertThat(userDao.getAllExternalIds(dir2Id), contains("2"));
    }

    @Test
    public void testGetAllExternalIdsNotExistingDirectory() throws Exception
    {
        exception.expect(DirectoryNotFoundException.class);
        userDao.getAllExternalIds(NOT_EXISTING_DIRECTORY_ID);
    }

    @Test
    public void testGetUserCount() throws Exception
    {
        Long dirId = directoryDao.add(new DirectoryImpl("dir1", DirectoryType.INTERNAL, "")).getId();

        assertEquals(0, userDao.getUserCount(dirId));
        userDao.add(TestData.User.getTestData(dirId, "9"), TestData.User.CREDENTIAL);
        assertEquals(1, userDao.getUserCount(dirId));
        userDao.add(TestData.User.getUser("user1", dirId, true, "Some", "User", "SomeUser", "SomeUser@somemail.com", null), TestData.User.CREDENTIAL);
        assertEquals(2, userDao.getUserCount(dirId));
    }

    @Test
    public void testGetUserCountNotExistingDirectory() throws Exception
    {
        exception.expect(DirectoryNotFoundException.class);
        userDao.getUserCount(NOT_EXISTING_DIRECTORY_ID);
    }

    @Test
    public void testTurkish() throws Exception
    {
        // EMBCWD-735
        // Add a dotted i turkish user
        User user = TestData.User.getUser("turkish", TestData.DIRECTORY_ID, TestData.User.ACTIVE, TestData.User.FIRST_NAME, TestData.User.LAST_NAME, TestData.User.DISPLAY_NAME, TestData.User.EMAIL);
        userDao.add(user, TestData.User.CREDENTIAL);

        // Now add a dotless turk?sh user
        user = TestData.User.getUser("turk\u0131sh", TestData.DIRECTORY_ID, TestData.User.ACTIVE, TestData.User.FIRST_NAME, TestData.User.LAST_NAME, TestData.User.DISPLAY_NAME, TestData.User.EMAIL);
        userDao.add(user, TestData.User.CREDENTIAL);

        List<User> allUsers = userDao.search(TestData.DIRECTORY_ID, QueryBuilder.queryFor(User.class, EntityDescriptor.user()).returningAtMost(EntityQuery.ALL_RESULTS));

        assertEquals(2, allUsers.size());
    }

    @Test
    public void testAddWithSameExternalIdShouldThrowUserAlreadyExistsException() throws Exception
    {
        exception.expect(UserAlreadyExistsException.class);
        exception.expectMessage("User already exists in directory [123] with name [userName]");

        User user = TestData.User.getUser("userName", TestData.DIRECTORY_ID, TestData.User.ACTIVE, TestData.User.FIRST_NAME, TestData.User.LAST_NAME, TestData.User.DISPLAY_NAME, TestData.User.EMAIL, "1");
        userDao.add(user, TestData.User.CREDENTIAL);

        // Now add a dotless turk?sh user
        user = TestData.User.getUser("differentUserName", TestData.DIRECTORY_ID, TestData.User.ACTIVE, TestData.User.FIRST_NAME, TestData.User.LAST_NAME, TestData.User.DISPLAY_NAME, TestData.User.EMAIL, "1");
        userDao.add(user, TestData.User.CREDENTIAL);
    }

    @Test
    public void testAddAndStoreAttributesAndFindUserWithAttributesByName() throws Exception
    {
        final User createdUser = userDao.add(TestData.User.getTestData(), TestData.User.CREDENTIAL);
        TestData.User.assertEqualsTestUser(createdUser);

        userDao.storeAttributes(createdUser, TestData.Attributes.getTestData());

        final UserWithAttributes retrievedUser = userDao.findByNameWithAttributes(TestData.DIRECTORY_ID, TestData.User.NAME);
        TestData.User.assertEqualsTestUser(retrievedUser);
        TestData.Attributes.assertEqualsTestData(retrievedUser);
    }

    @Test
    public void testUpdateUser() throws Exception
    {
        final User createdUser = userDao.add(TestData.User.getTestData(), TestData.User.CREDENTIAL);
        TestData.User.assertEqualsTestUser(createdUser);

        final boolean updatedIsActive = false;
        final String updatedEmail = "updatedEmail@example.com";
        final String updatedFirstName = "updatedFirstName";
        final String updatedLastName = "updatedLastName";
        final String updatedDisplayName = "updatedDisplayName";

        userDao.update(TestData.User.getUser(createdUser.getName(), createdUser.getDirectoryId(), updatedIsActive, updatedFirstName, updatedLastName,
                updatedDisplayName, updatedEmail));

        final User updatedUser = userDao.findByName(TestData.DIRECTORY_ID, TestData.User.NAME);

        assertEquals(TestData.User.NAME, updatedUser.getName());
        assertEquals(updatedIsActive, updatedUser.isActive());
        assertEquals(updatedFirstName, updatedUser.getFirstName());
        assertEquals(updatedLastName, updatedUser.getLastName());
        assertEquals(updatedDisplayName, updatedUser.getDisplayName());
        assertEquals(updatedEmail, updatedUser.getEmailAddress());
    }

    @Test
    public void testRenameUser() throws Exception
    {
        final User createdUser = userDao.add(TestData.User.getTestData("externalId"), TestData.User.CREDENTIAL);
        TestData.User.assertEqualsTestUser(createdUser);
        assertThat(userKeyStore.getKeyForUsername(createdUser.getName()), equalTo("ausername"));

        final String updatedName = "differentName";
        assertThat(TestData.User.NAME, is(not(equalTo(updatedName))));

        final User updatedUser = userDao.rename(createdUser, updatedName);

        for(final User user : ImmutableList.of(userDao.findByName(TestData.DIRECTORY_ID, updatedName),
                userDao.findByExternalId(TestData.DIRECTORY_ID, "externalId")))
        {
            assertEquals(updatedName, user.getName());
            assertEquals(createdUser.isActive(), user.isActive());
            assertEquals(createdUser.getFirstName(), user.getFirstName());
            assertEquals(createdUser.getLastName(), user.getLastName());
            assertEquals(createdUser.getDisplayName(), user.getDisplayName());
            assertEquals(createdUser.getEmailAddress(), user.getEmailAddress());
        }

        // user should be removed from cache
        try
        {
            userDao.findByName(createdUser.getDirectoryId(), TestData.User.NAME);
            fail("Should throw UserNotFoundException");
        }
        catch(UserNotFoundException e)
        {
            assertThat(e.getMessage(), equalTo("User <AUserName> does not exist"));
        }

        assertThat(userKeyStore.getKeyForUsername(createdUser.getName()), nullValue());
        assertThat(userKeyStore.getKeyForUsername(updatedUser.getName()), equalTo("ausername"));
    }

    @Test
    public void testUpdateCredentialAndGetCredential() throws Exception
    {
        final PasswordCredential updatedCredential = PasswordCredential.encrypted("I am a secret hash");
        final User user = userDao.add(TestData.User.getTestData(), TestData.User.CREDENTIAL);

        userDao.updateCredential(user, updatedCredential, 0);

        assertEquals(updatedCredential, userDao.getCredential(TestData.DIRECTORY_ID, TestData.User.NAME));
    }

    @Test
    public void testRemoveUser() throws Exception
    {
        userDao.add(TestData.User.getTestData(), TestData.User.CREDENTIAL);
        assertNotNull(userDao.findByName(TestData.DIRECTORY_ID, TestData.User.NAME));

        userDao.remove(TestData.User.getTestData());
        try
        {
            userDao.findByName(TestData.DIRECTORY_ID, TestData.User.NAME);
            fail("Should have thrown a user not found exception");
        }
        catch (final UserNotFoundException e)
        {
            assertEquals(TestData.User.NAME, e.getUserName());
        }
    }

    @Test
    public void testRemoveUserVeto() throws Exception
    {
        userDao.add(TestData.User.getTestData(), TestData.User.CREDENTIAL);
        OfBizUser user = userDao.findOfBizUser(TestData.DIRECTORY_ID, TestData.User.NAME);
        assertTrue(user.isActive());

        userDeleteVeto.setDefaultAllow(false);
        // try to remove
        userDao.remove(user);
        user = userDao.findOfBizUser(TestData.DIRECTORY_ID, TestData.User.NAME);
        // should be vetoed and disabled instead
        assertEquals(TestData.User.NAME, user.getName());
        // We append " [X]" to the end of the display name to indicate a delete
        assertEquals(TestData.User.DISPLAY_NAME + " [X]", user.getDisplayName());
        assertEquals(false, user.isActive());

        // Try again and make sure we don't add the [X] more than once
        userDao.remove(user);
        user = userDao.findOfBizUser(TestData.DIRECTORY_ID, TestData.User.NAME);
        // should be vetoed and disabled instead
        assertEquals(TestData.User.NAME, user.getName());
        assertEquals(TestData.User.DISPLAY_NAME + " [X]", user.getDisplayName());
        assertEquals(false, user.isActive());

        // Now add a shadowed user and the delete should work.
        Directory d = directoryDao.add(new DirectoryImpl("Blah", DirectoryType.INTERNAL, ""));
        UserTemplate otherUser = new UserTemplate(TestData.User.getTestData());
        otherUser.setDirectoryId(d.getId());
        userDao.add(otherUser, TestData.User.CREDENTIAL);

        userDao.remove(user);
        try
        {
            userDao.findByName(TestData.DIRECTORY_ID, TestData.User.NAME);
            fail("Should have thrown a user not found exception");
        }
        catch (final UserNotFoundException e)
        {
            assertEquals(TestData.User.NAME, e.getUserName());
        }
    }

    @Test
    public void testRemoveAttribute() throws Exception
    {
        final User createdUser = userDao.add(TestData.User.getTestData(), TestData.User.CREDENTIAL);
        userDao.storeAttributes(createdUser, TestData.Attributes.getTestData());

        TestData.Attributes.assertEqualsTestData(userDao.findByNameWithAttributes(TestData.DIRECTORY_ID, TestData.User.NAME));

        userDao.removeAttribute(createdUser, TestData.Attributes.ATTRIBUTE1);
        final UserWithAttributes userWithLessAttributes = userDao.findByNameWithAttributes(TestData.DIRECTORY_ID, TestData.User.NAME);

        assertNull(userWithLessAttributes.getValue(TestData.Attributes.ATTRIBUTE1));
    }

    @Test
    public void testRemoveAll() throws Exception
    {
        final User createdUser = userDao.add(TestData.User.getTestData(), TestData.User.CREDENTIAL);

        BatchResult<String> batchResult = userDao.removeAllUsers(TestData.DIRECTORY_ID,
                ImmutableSet.of(createdUser.getName(), "non-existent-user"));

        assertThat(batchResult.getSuccessfulEntities(), contains(createdUser.getName()));
        assertThat(batchResult.getFailedEntities(), contains("non-existent-user"));
    }
}
