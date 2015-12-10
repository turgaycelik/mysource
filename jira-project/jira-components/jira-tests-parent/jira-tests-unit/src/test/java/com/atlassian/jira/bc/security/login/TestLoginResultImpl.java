package com.atlassian.jira.bc.security.login;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 */
public class TestLoginResultImpl
{
    @Test
    public void testConstruction()
    {
        LoginInfo loginInfo = new LoginInfoImpl(1L,2L,3L,4L,5L,5L,6L,true);

        final LoginResultImpl loginResult = new LoginResultImpl(LoginReason.OK, loginInfo, "userName");
        assertEquals(LoginReason.OK, loginResult.getReason());
        assertEquals("userName", loginResult.getUserName());
        assertSame(loginInfo, loginResult.getLoginInfo());
    }
}
