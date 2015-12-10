package com.atlassian.jira.user.util;

import java.util.Date;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.ConstantClock;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 */
public class TestPasswordResetTokenBuilder
{

    private static final String PASSWORD_RESET_REQUEST_TOKEN = "password.reset.request.token";
    private static final String PASSWORD_RESET_REQUEST_EXPIRY = "password.reset.request.expiry";

    private User fred;
    private ConstantClock constantClock;
    private long constantTime;
    private MockCrowdService crowdService;

    @Before
    public void setUp() throws Exception
    {
        crowdService = new MockCrowdService();
        ComponentAccessor.initialiseWorker(new MockComponentWorker().addMock(CrowdService.class, crowdService));

        fred = new MockUser("fred");
        crowdService.addUser(fred, "-");
        constantClock = new ConstantClock(new Date());
        constantTime = constantClock.getCurrentDate().getTime();
    }

    @Test
    public void test_generateToken_nulluser()
    {
        try
        {
            new PasswordResetTokenBuilder(crowdService).generateToken(null);
            fail("Should have barfed above");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    @Test
    public void test_generateToken()
    {
        long expectedTime = constantTime + 24 * 60 * 60 * 1000;
        final UserUtil.PasswordResetToken token = new PasswordResetTokenBuilder(constantClock, crowdService).generateToken(fred);

        assertNotNull(token);
        assertNotNull(token.getToken());
        assertEquals(fred, token.getUser());
        assertEquals(expectedTime, token.getExpiryTime());
        assertEquals(24, token.getExpiryHours());
    }

    @Test
    public void test_validateToken_noTokenRecorded()
    {
        final UserUtil.PasswordResetTokenValidation.Status status = new PasswordResetTokenBuilder(constantClock, crowdService).validateToken(fred, "123456");
        assertEquals(UserUtil.PasswordResetTokenValidation.Status.EXPIRED, status);
    }

    @Test
    public void test_validateToken_expiredTokenPresented() throws OperationNotPermittedException
    {
        crowdService.setUserAttribute(fred, PASSWORD_RESET_REQUEST_TOKEN, "123456");
        crowdService.setUserAttribute(fred, PASSWORD_RESET_REQUEST_EXPIRY, "1");

        final UserUtil.PasswordResetTokenValidation.Status status = new PasswordResetTokenBuilder(constantClock, crowdService).validateToken(fred, "123456");
        assertEquals(UserUtil.PasswordResetTokenValidation.Status.EXPIRED, status);
    }

    @Test
    public void test_validateToken_invalidTokenPresented() throws OperationNotPermittedException
    {
        crowdService.setUserAttribute(fred, PASSWORD_RESET_REQUEST_TOKEN, "ABCDEF");
        crowdService.setUserAttribute(fred, PASSWORD_RESET_REQUEST_EXPIRY, String.valueOf(constantTime + 1000));

        final UserUtil.PasswordResetTokenValidation.Status status = new PasswordResetTokenBuilder(constantClock, crowdService).validateToken(fred, "123456");
        assertEquals(UserUtil.PasswordResetTokenValidation.Status.UNEQUAL, status);
    }

    @Test
    public void test_validateToken_OK() throws OperationNotPermittedException
    {
        crowdService.setUserAttribute(fred, PASSWORD_RESET_REQUEST_TOKEN, "123456");
        crowdService.setUserAttribute(fred, PASSWORD_RESET_REQUEST_EXPIRY, String.valueOf(constantTime + 1000));

        final UserUtil.PasswordResetTokenValidation.Status status = new PasswordResetTokenBuilder(constantClock, crowdService).validateToken(fred, "123456");
        assertEquals(UserUtil.PasswordResetTokenValidation.Status.OK, status);
    }

    @Test
    public void test_resetToken() throws OperationNotPermittedException
    {
        crowdService.setUserAttribute(fred, PASSWORD_RESET_REQUEST_TOKEN, "123456");
        crowdService.setUserAttribute(fred, PASSWORD_RESET_REQUEST_EXPIRY, String.valueOf(constantTime + 1000));

        new PasswordResetTokenBuilder(constantClock, crowdService).resetToken(fred);

        assertNull(crowdService.getUserWithAttributes(fred.getName()).getValue(PASSWORD_RESET_REQUEST_TOKEN));
        assertNull(crowdService.getUserWithAttributes(fred.getName()).getValue(PASSWORD_RESET_REQUEST_EXPIRY));

    }
}
