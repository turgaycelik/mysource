package com.atlassian.jira.functest.framework.parser;

/**
 * A parser that builds the system info page information into a Map like structure
 *
 * @since v3.13
 */
public interface SystemInfoParser
{
    SystemInfo getSystemInfo();

    interface SystemInfo
    {

        String getAppServer();

        String getJavaVersion();

        String getDatabaseType();

        String getSystemEncoding();

        String getProperty(String displayedKey);
    }
}
