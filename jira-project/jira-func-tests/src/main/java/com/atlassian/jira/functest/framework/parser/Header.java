package com.atlassian.jira.functest.framework.parser;

/**
 * @since v5.2
 */
public interface Header
{
    /**
     * Return the full user name from the header.
     *
     * @return the full user name from the header.
     */
    String getFullUserName();
}
