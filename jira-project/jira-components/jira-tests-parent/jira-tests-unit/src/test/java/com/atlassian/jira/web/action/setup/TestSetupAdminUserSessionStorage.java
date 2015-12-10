package com.atlassian.jira.web.action.setup;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestSetupAdminUserSessionStorage
{
    @Test
    public void testGetAndSet()
    {
        final String username = "user123";
        final SetupAdminUserSessionStorage sessionStorage = new SetupAdminUserSessionStorage(username);
        assertEquals(username, sessionStorage.getUsername());
    }
}
