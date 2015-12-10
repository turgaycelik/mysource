package com.atlassian.jira.user;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.crowd.embedded.api.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestDefaultSecureUserTokenManager
{
    private User user;
    private DefaultSecureUserTokenManager manager;

    @Before
    public void setUp() throws Exception
    {
        user = new MockUser("admin");
        manager = new DefaultSecureUserTokenManager(new MemoryCacheManager());
    }

    @After
    public void tearDown() throws Exception
    {
        user = null;
        manager = null;
    }

    @Test
    public void testGenerateAndUseTokens()
    {
        assertNull(manager.generateToken(null, SecureUserTokenManager.TokenType.SCREENSHOT));
        String token = manager.generateToken(user, SecureUserTokenManager.TokenType.SCREENSHOT);

        assertNull(manager.useToken("someinvalidtoken", SecureUserTokenManager.TokenType.SCREENSHOT));
        User matchedUser = manager.useToken(token, SecureUserTokenManager.TokenType.SCREENSHOT);
        assertEquals(user, matchedUser);
        //tokens can only be used once.  Calling useToken again should return null.
        assertNull(manager.useToken(token, SecureUserTokenManager.TokenType.SCREENSHOT));
    }
}
