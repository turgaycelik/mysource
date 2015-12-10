package com.atlassian.jira.jql.resolver;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.user.util.UserManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestUserResolver
{
    //Found user
    private static final String FOUND_USER_KEY = "1";
    private static final String FOUND_USER_NAME = "username";
    private static final String FOUND_FULL_NAME = "hello.world";
    private static final String FOUND_EMAIL_ADDRESS = "hello.world@example.com";
    private static final String FOUND_UPPER_CASE_NAME = "USERNAME";

    private static final String ID_USER_KEY = "12";
    private static final String ID_USER_NAME = "10";
    private static final Long ID_USER_ID = 10L;
    private static final String ID_FULL_NAME = "10";
    private static final String ID_EMAIL_ADDRESS = "11@example.com";
    private static final String ID_UPPER_CASE_NAME = "10";

    //Deleted user
    private static final String DELETED_USER_KEY = "2";
    private static final String DELETED_USER_NAME = "username2";

    //Not Found user
    private static final String NOT_FOUND_USER_NAME = "username3";
    private static final String NOT_FOUND_UPPER_CASE_NAME = "USERNAME3";

    @Mock private User foundUser;
    @Mock private User idUser;
    @Mock private UserKeyService userKeyService;
    @Mock private UserManager userManager;

    private UserResolver userResolver;

    @Before
    public void setUp()
    {
        mockFoundUser();
        mockIdUser();

        mockUserManager();
        mockUserKeyService();

        userResolver = new UserResolverImpl(userKeyService, userManager);
    }

    @After
    public void tearDown()
    {
        userManager = null;
        userResolver = null;
    }

    @Test
    public void testGetIdsFromUpperCaseNameUserExists() throws Exception
    {
        assertUserKeyFound(FOUND_UPPER_CASE_NAME, FOUND_USER_KEY);
    }

    @Test
    public void testGetIdsFromNameUserExists() throws Exception
    {
        assertUserKeyFound(FOUND_USER_NAME, FOUND_USER_KEY);
    }

    @Test
     public void testGetIdsFromNameUserDoesntExist() throws Exception
    {
        assertUserKeyNotFound(NOT_FOUND_USER_NAME);
    }

    @Test
    public void testGetIdsFromUpperCaseNameUserDoesntExist() throws Exception
    {
        assertUserKeyNotFound(NOT_FOUND_UPPER_CASE_NAME);
    }

    @Test
    public void testGetIdsFromNameUserDeleted() throws Exception
    {
        assertUserKeyFound(DELETED_USER_NAME, DELETED_USER_KEY);
    }

    @Test
    public void testNameExistsForExistingUser() throws Exception
    {
        assertTrue(userResolver.nameExists(FOUND_USER_NAME));
    }

    @Test
    public void testNameDoesNotExistForNonExistingUser() throws Exception
    {
        assertFalse(userResolver.nameExists(NOT_FOUND_USER_NAME));
    }

    @Test
    public void testNameExistsForDeletedUser() throws Exception
    {
        assertTrue(userResolver.nameExists(DELETED_USER_NAME));
    }

    @Test
    public void testNameExistsAsFullName() throws Exception
    {
        assertTrue(userResolver.nameExists(FOUND_FULL_NAME));
    }

    @Test
    public void testNameExistsAsEmailAddress() throws Exception
    {
        assertTrue(userResolver.nameExists(FOUND_EMAIL_ADDRESS));
    }

    @Test
    public void testIdExistsItDoes() throws Exception
    {
        assertTrue(userResolver.idExists(ID_USER_ID));
    }

    @Test
    public void testIdExistsItDoesnt() throws Exception
    {
        assertFalse(userResolver.idExists(12L));
    }

    @Test
    public void testIdExistsAsFullName() throws Exception
    {
        assertTrue(userResolver.idExists(10L));
    }

    @Test
    public void testIdExistsDoesNotMatchPortionOfEmailAddress() throws Exception
    {
        assertFalse(userResolver.idExists(11L));
    }

    @Test
    public void testPickEmailOrFullNameMatchesOnlyFullNames() throws Exception
    {
        assertUserKey(userResolver.getIdsFromName(FOUND_FULL_NAME), FOUND_USER_KEY);
    }

    @Test
    public void testPickEmailOrFullNameMatchesOnlyEmails() throws Exception
    {
        assertUserKey(userResolver.getIdsFromName(FOUND_EMAIL_ADDRESS), FOUND_USER_KEY);
    }

    @Test
    public void testPickEmailOrFullNameMatchesEmailsAndNamesButNameNotEmail() throws Exception
    {
        assertUserKey(userResolver.getIdsFromName(FOUND_FULL_NAME), FOUND_USER_KEY);
    }

    @Test
    public void testPickEmailOrFullNameMatchesEmailsAndNamesButNameEmail() throws Exception
    {
        assertUserKey(userResolver.getIdsFromName(FOUND_EMAIL_ADDRESS), FOUND_USER_KEY);
    }

    private void assertUserKey(List<String> result, String key)
    {
        assertEquals(asList(key), result);
    }

    private void assertUserKeyFound(final String searchingParameter, final String expectedKey)
    {
        assertUserKey(userResolver.getIdsFromName(searchingParameter), expectedKey);
    }

    private void assertUserKeyNotFound(final String searchingParameter)
    {
        assertEquals(Collections.EMPTY_LIST, userResolver.getIdsFromName(searchingParameter));
    }

    private void mockUserManager()
    {
        Set<User> userSet = new HashSet<User>();
        userSet.add(foundUser);
        userSet.add(idUser);

        when(userManager.getUsers()).thenReturn(userSet);
        when(userManager.getAllUsers()).thenReturn(userSet);
    }

    private void mockUserKeyService()
    {
        when(userKeyService.getKeyForUsername(FOUND_USER_NAME)).thenReturn(FOUND_USER_KEY);
        when(userKeyService.getKeyForUsername(FOUND_UPPER_CASE_NAME)).thenReturn(FOUND_USER_KEY);
        when(userKeyService.getKeyForUsername(ID_USER_NAME)).thenReturn(ID_USER_KEY);
        when(userKeyService.getKeyForUsername(ID_UPPER_CASE_NAME)).thenReturn(ID_USER_KEY);

        when(userKeyService.getKeyForUsername(DELETED_USER_NAME)).thenReturn(DELETED_USER_KEY);
    }

    private void mockFoundUser()
    {
        when(foundUser.getName()).thenReturn(FOUND_USER_NAME);
        when(foundUser.getDisplayName()).thenReturn(FOUND_FULL_NAME);
        when(foundUser.getEmailAddress()).thenReturn(FOUND_EMAIL_ADDRESS);
    }

    private void mockIdUser()
    {
        when(idUser.getName()).thenReturn(ID_USER_NAME);
        when(idUser.getDisplayName()).thenReturn(ID_FULL_NAME);
        when(idUser.getEmailAddress()).thenReturn(ID_EMAIL_ADDRESS);
    }
}
