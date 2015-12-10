/**
 * Copyright 2008 Atlassian Pty Ltd 
 */
package com.atlassian.jira.security.roles;

/**
 * @since v3.13
 */
public class Main
{
    public static void main(final String[] args) throws Exception
    {
        for (int i = 0; i < 1000; i++)
        {
            new TestCachingProjectRoleAndActorStoreForConcurrency().testConcurrentProjectRoleActorUpdateDoesntInvalidateCache();
        }
    }

}
