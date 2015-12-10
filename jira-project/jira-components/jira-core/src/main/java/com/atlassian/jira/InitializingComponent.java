package com.atlassian.jira;

/**
 * Similar to InitializingBean in Spring - this should be implemented by a component where you want to do some work
 * immediately after registration
 *
 * @since v6.0.2
 */
public interface InitializingComponent
{
    void afterInstantiation() throws Exception;
}
