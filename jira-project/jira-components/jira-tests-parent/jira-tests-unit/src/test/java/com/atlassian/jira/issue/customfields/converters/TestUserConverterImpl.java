package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.DelegatingApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.MockUserManager;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestUserConverterImpl
{
    private static String USER_KEY = "mmm";
    private static String USER_NAME = "marysmith";

    private ApplicationUser user;
    private UserConverterImpl userConverter;

    private MockUserManager userManager;
    private MockI18nHelper i18nHelper;

    @Before
    public void setUp()
    {
        user = new DelegatingApplicationUser(USER_KEY, new MockUser(USER_NAME));
        userManager = new MockUserManager();
        i18nHelper = new MockI18nHelper();
        userConverter = new UserConverterImpl(userManager, i18nHelper);
    }

    @Test
    public void testGetDbString()
    {
        assertEquals(USER_KEY, userConverter.getDbString(user));
    }

    @Test
    public void testGetDbStringNullParam()
    {
        assertEquals("", userConverter.getDbString(null));
    }

    @Test
    public void testGetPresentationString()
    {
        assertEquals(USER_NAME, userConverter.getHttpParameterValue(user));
    }

    @Test
    public void testGetPresentationStringNullParam()
    {
        assertEquals("", userConverter.getHttpParameterValue(null));
    }

    @Test
    public void testGetUserFromDbString() throws Exception
    {
        userManager.addUser(user);

        final ApplicationUser userFromDbString = userConverter.getUserFromDbString(USER_KEY);
        assertEquals(USER_KEY, userFromDbString.getKey());
        assertEquals(USER_NAME, userFromDbString.getName());
    }

    @Test
    public void testGerUserFromDbStringNullParam()
    {
        assertNull(userConverter.getUserFromDbString(""));
        assertNull(userConverter.getUserFromDbString(" "));
        assertNull(userConverter.getUserFromDbString(null));
    }

    @Test
    public void testGetUserFromHttpParameterWithValidation() throws Exception
    {
        userManager.addUser(user);

        final ApplicationUser userFromHttpParameterWithValidation = userConverter.getUserFromHttpParameterWithValidation(USER_NAME);
        assertEquals(USER_KEY, userFromHttpParameterWithValidation.getKey());
        assertEquals(USER_NAME, userFromHttpParameterWithValidation.getName());
    }

    @Test
    public void testGetUserFromHttpParameterWithValidationNullParam()
    {
        assertNull(userConverter.getUserFromHttpParameterWithValidation(""));
        assertNull(userConverter.getUserFromHttpParameterWithValidation(" "));
        assertNull(userConverter.getUserFromHttpParameterWithValidation(null));
    }

    @Test
    public void testGetString() throws Exception
    {
        final UserConverterImpl userConverter = new UserConverterImpl(null, null);
        assertEquals("tom", userConverter.getString(new MockUser("tom")));
        assertEquals("", userConverter.getString(null));
    }

    @Test
    public void testGetUser() throws Exception
    {
        userManager.addUser(new MockUser("tom"));

        UserConverterImpl userConverter = new UserConverterImpl(userManager, i18nHelper);
        assertNull(userConverter.getUser("")); 
        assertNull(userConverter.getUser(" "));
        assertNull(userConverter.getUser(null));
        assertEquals("tom", userConverter.getUser("tom").getName());
    }
}
