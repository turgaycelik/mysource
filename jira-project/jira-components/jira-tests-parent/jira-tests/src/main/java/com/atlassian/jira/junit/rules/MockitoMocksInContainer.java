package com.atlassian.jira.junit.rules;

import org.junit.rules.RuleChain;

/**
 * <p/>
 * Factory for a {@link MockitoContainer} rule
 *
 * @see MockitoContainer
 * @since v5.2
 */
public class MockitoMocksInContainer
{

    private MockitoMocksInContainer()
    {
        throw new AssertionError("Don't instantiate me");
    }

    /**
     * Returns a rule which initializes Mockito mocks and mocked component container.
     * <p/>
     * <b>Note:</b>This method returns pure RuleChain which does not allow to modify mock container behaviour directly. If you want
     * to do this, please use {@link #rule(Object)}
     * @param test test
     * @return initialized rule chain
     */
    public static RuleChain forTest(Object test)
    {
        return new MockitoContainer(test).getInnerChain();
    }

    /**
     * Returns a rule which initializes Mockito mocks and mocked component container.
     * @param test test
     * @return initialized rule
     * @see MockitoContainer
     */
    public static MockitoContainer rule(Object test){
        return new MockitoContainer(test);
    }

}
