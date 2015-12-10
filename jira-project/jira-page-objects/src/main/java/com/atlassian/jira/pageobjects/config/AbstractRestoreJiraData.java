package com.atlassian.jira.pageobjects.config;

/**
 * Implements {@link #restoreBlank()} in terms of abstract {@link #restore(String)}.
 *
 * @since 5.2
 */
public abstract class AbstractRestoreJiraData implements RestoreJiraData
{

    @Override
    public void restoreBlank()
    {
        restore("blankprojects.xml");
    }
}
