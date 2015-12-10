package com.atlassian.jira.security.login;

import java.util.Date;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.UserWithAttributes;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.jira.bc.security.login.LoginInfo;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.ConstantClock;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

/**
 */
public class TestLoginStoreImpl
{
    private User fred;
    private Date when;
    private static final String LAST_LOGIN_TIME = "login.lastLoginMillis";
    private static final String PREV_LOGIN_TIME = "login.previousLoginMillis";
    private static final String LAST_FAILED_TIME = "login.lastFailedLoginMillis";
    private static final String LOGIN_COUNT = "login.count";
    private static final String CURRENT_FAILED_COUNT = "login.currentFailedCount";
    private static final String TOTAL_FAILED_COUNT = "login.totalFailedCount";

    private static final String JIRA_MAXIMUM_AUTHENTICATION_ATTEMPTS_ALLOWED = "jira.maximum.authentication.attempts.allowed";

    @Mock
    @AvailableInContainer
    private ApplicationProperties applicationProperties;

    @AvailableInContainer(instantiateMe = true)
    private MockCrowdService crowdService;

    private LoginStoreImpl loginStore;

    @Rule
    public MockitoContainer container = new MockitoContainer(this);

    @Before
    public void setUp() throws Exception
    {
        fred = new MockUser("fred");
        fred = crowdService.addUser(fred, "");

        when = new Date();
        ConstantClock clock = new ConstantClock(when);

        loginStore = new LoginStoreImpl(clock, applicationProperties, crowdService);
    }

    @Test
    public void testGetLoginInfo_NeverLoggedIn()
    {
        expect_MaxAttempts("3");

        final LoginInfo loginInfo = loginStore.getLoginInfo(fred);
        assertNotNull(loginInfo);
        assertEquals(new Long(3), loginInfo.getMaxAuthenticationAttemptsAllowed());

        assertNull(loginInfo.getLastLoginTime());
        assertNull(loginInfo.getPreviousLoginTime());
        assertNull(loginInfo.getCurrentFailedLoginCount());
        assertNull(loginInfo.getTotalFailedLoginCount());
        assertNull(loginInfo.getLoginCount());
    }

    @Test
    public void testGetLoginInfo_HasLoggedIn() throws OperationNotPermittedException
    {
        crowdService.setUserAttribute(fred, LAST_LOGIN_TIME, "1234");
        crowdService.setUserAttribute(fred, LOGIN_COUNT, "1");

        expect_MaxAttempts("3");

        final LoginInfo loginInfo = loginStore.getLoginInfo(fred);
        assertNotNull(loginInfo);
        assertEquals(new Long(1234), loginInfo.getLastLoginTime());
        assertEquals(new Long(1), loginInfo.getLoginCount());
        assertEquals(new Long(3), loginInfo.getMaxAuthenticationAttemptsAllowed());

        assertNull(loginInfo.getPreviousLoginTime());
        assertNull(loginInfo.getCurrentFailedLoginCount());
        assertNull(loginInfo.getTotalFailedLoginCount());
    }

    @Test
    public void testRecordSuccessfulLogin_WithNothingPreviouslyRecorded()
    {
        expect_MaxAttempts("3");

        final LoginInfo loginInfo = loginStore.recordLoginAttempt(fred, true);

        // did the value get stored
        UserWithAttributes fredWithAttributes = crowdService.getUserWithAttributes(fred.getName());
        assertEquals(String.valueOf(when.getTime()), fredWithAttributes.getValue(LAST_LOGIN_TIME));
        assertEquals("1", fredWithAttributes.getValue(LOGIN_COUNT));
        assertEquals(null, fredWithAttributes.getValue(PREV_LOGIN_TIME));
        assertEquals("0", fredWithAttributes.getValue(CURRENT_FAILED_COUNT));
        assertEquals(null, fredWithAttributes.getValue(TOTAL_FAILED_COUNT));
        assertEquals(null, fredWithAttributes.getValue(LAST_FAILED_TIME));


        assertEquals(new Long(when.getTime()), loginInfo.getLastLoginTime());
        assertEquals(new Long(1), loginInfo.getLoginCount());
        assertEquals(new Long(3), loginInfo.getMaxAuthenticationAttemptsAllowed());
        assertEquals(new Long(0), loginInfo.getCurrentFailedLoginCount());
        assertNull(loginInfo.getPreviousLoginTime());
        assertNull(loginInfo.getTotalFailedLoginCount());
    }

    @Test
    public void testRecordFailedLogin_WithNothingPreviouslyRecorded()
    {

        expect_MaxAttempts("3");

        final LoginInfo loginInfo = loginStore.recordLoginAttempt(fred, false);

        // did the value get stored
        UserWithAttributes fredWithAttributes = crowdService.getUserWithAttributes(fred.getName());
        final String whenStr = String.valueOf(when.getTime());

        assertEquals(null, fredWithAttributes.getValue(LAST_LOGIN_TIME));
        assertEquals(null, fredWithAttributes.getValue(LOGIN_COUNT));
        assertEquals(null, fredWithAttributes.getValue(PREV_LOGIN_TIME));
        assertEquals("1", fredWithAttributes.getValue(CURRENT_FAILED_COUNT));
        assertEquals("1", fredWithAttributes.getValue(TOTAL_FAILED_COUNT));
        assertEquals(whenStr, fredWithAttributes.getValue(LAST_FAILED_TIME));


        final Long whenLong = when.getTime();
        assertEquals(null, loginInfo.getLastLoginTime());
        assertEquals(null, loginInfo.getPreviousLoginTime());
        assertEquals(null, loginInfo.getLoginCount());
        assertEquals(new Long(3), loginInfo.getMaxAuthenticationAttemptsAllowed());
        assertEquals(new Long(1), loginInfo.getCurrentFailedLoginCount());
        assertEquals(new Long(1), loginInfo.getTotalFailedLoginCount());
        assertEquals(whenLong, loginInfo.getLastFailedLoginTime());
    }

    @Test
    public void testRecordFailedLogin_WithPreviousFailures() throws OperationNotPermittedException
    {
        crowdService.setUserAttribute(fred, CURRENT_FAILED_COUNT, "5");
        crowdService.setUserAttribute(fred, TOTAL_FAILED_COUNT, "10");

        expect_MaxAttempts("3");

        final LoginInfo loginInfo = loginStore.recordLoginAttempt(fred, false);

        // did the value get stored
        UserWithAttributes fredWithAttributes = crowdService.getUserWithAttributes(fred.getName());
        final String whenStr = String.valueOf(when.getTime());

        assertEquals(null, fredWithAttributes.getValue(LAST_LOGIN_TIME));
        assertEquals(null, fredWithAttributes.getValue(LOGIN_COUNT));
        assertEquals(null, fredWithAttributes.getValue(PREV_LOGIN_TIME));
        assertEquals("6", fredWithAttributes.getValue(CURRENT_FAILED_COUNT));
        assertEquals("11", fredWithAttributes.getValue(TOTAL_FAILED_COUNT));
        assertEquals(whenStr, fredWithAttributes.getValue(LAST_FAILED_TIME));


        final Long whenLong = when.getTime();
        assertEquals(null, loginInfo.getLastLoginTime());
        assertEquals(null, loginInfo.getPreviousLoginTime());
        assertEquals(null, loginInfo.getLoginCount());
        assertEquals(new Long(3), loginInfo.getMaxAuthenticationAttemptsAllowed());
        assertEquals(new Long(6), loginInfo.getCurrentFailedLoginCount());
        assertEquals(new Long(11), loginInfo.getTotalFailedLoginCount());
        assertEquals(whenLong, loginInfo.getLastFailedLoginTime());
    }


    @Test
    public void testRecordSuccessfulLogin_AndBumpsPreviousLoginTime() throws OperationNotPermittedException
    {
        crowdService.setUserAttribute(fred, LAST_LOGIN_TIME, "1234");
        crowdService.setUserAttribute(fred, LOGIN_COUNT, "5");


        expect_MaxAttempts("3");

        loginStore.recordLoginAttempt(fred, true);

        // did the value get stored
        UserWithAttributes fredWithAttributes = crowdService.getUserWithAttributes(fred.getName());
        final String whenStr = String.valueOf(when.getTime());

        assertEquals(String.valueOf(when.getTime()), fredWithAttributes.getValue(LAST_LOGIN_TIME));
        assertEquals("6", fredWithAttributes.getValue(LOGIN_COUNT));
        assertEquals("1234", fredWithAttributes.getValue(PREV_LOGIN_TIME));
    }

    @Test
    public void testRecordSucessfulLogin_AndResetsCurrentCount() throws OperationNotPermittedException
    {
        crowdService.setUserAttribute(fred, CURRENT_FAILED_COUNT, "999");
        crowdService.setUserAttribute(fred, TOTAL_FAILED_COUNT, "6666");


        expect_MaxAttempts("3");

        final LoginInfo loginInfo = loginStore.recordLoginAttempt(fred, true);

        // did the value get stored
        UserWithAttributes fredWithAttributes = crowdService.getUserWithAttributes(fred.getName());
        final String whenStr = String.valueOf(when.getTime());

        assertEquals(whenStr, fredWithAttributes.getValue(LAST_LOGIN_TIME));
        assertEquals("1", fredWithAttributes.getValue(LOGIN_COUNT));
        assertEquals(null, fredWithAttributes.getValue(PREV_LOGIN_TIME));
        assertEquals("0", fredWithAttributes.getValue(CURRENT_FAILED_COUNT));
        assertEquals("6666", fredWithAttributes.getValue(TOTAL_FAILED_COUNT));

        final Long whenLong = when.getTime();
        assertEquals(whenLong, loginInfo.getLastLoginTime());
        assertEquals(null, loginInfo.getPreviousLoginTime());
        assertEquals(new Long(1L), loginInfo.getLoginCount());
        assertEquals(new Long(3), loginInfo.getMaxAuthenticationAttemptsAllowed());
        assertEquals(new Long(0), loginInfo.getCurrentFailedLoginCount());
        assertEquals(new Long(6666), loginInfo.getTotalFailedLoginCount());
    }

    @Test
    public void test_ResetFailedLoginCount() throws OperationNotPermittedException
    {
        crowdService.setUserAttribute(fred, CURRENT_FAILED_COUNT, "999");

        loginStore.resetFailedLoginCount(fred);

        UserWithAttributes fredWithAttributes = crowdService.getUserWithAttributes(fred.getName());

        assertEquals("0", fredWithAttributes.getValue(CURRENT_FAILED_COUNT));
    }

    @Test
    public void test_getMaxAuthenticationAttemptsAllowed_WhenItsNotSet()
    {
        expect_MaxAttempts(null);
        
        final long attemptsAllowed = loginStore.getMaxAuthenticationAttemptsAllowed();
        assertEquals(Long.MAX_VALUE, attemptsAllowed);
    }

    @Test
    public void test_getMaxAuthenticationAttemptsAllowed_WhenItsSet()
    {
        expect_MaxAttempts("5");

        final long attemptsAllowed = loginStore.getMaxAuthenticationAttemptsAllowed();
        assertEquals(5, attemptsAllowed);
    }

    @Test
    public void test_getMaxAuthenticationAttemptsAllowed_WhenItsRubbisg()
    {
        expect_MaxAttempts("a5x");

        final long attemptsAllowed = loginStore.getMaxAuthenticationAttemptsAllowed();
        assertEquals(Long.MAX_VALUE, attemptsAllowed);
    }

    private void expect_MaxAttempts(final String howMany)
    {
        when(applicationProperties.getDefaultBackedString(JIRA_MAXIMUM_AUTHENTICATION_ATTEMPTS_ALLOWED)).thenReturn(howMany);
    }

}
