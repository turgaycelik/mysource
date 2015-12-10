package com.atlassian.jira.pageobjects.config;

/**
 * Represents JIRA restore data operation.
 *
 * @author Dariusz Kordonski
 */
public interface RestoreJiraData
{
    /**
     * Execute restore from given resource.
     *
     * @param resourcePath path of the class path resource to restore.
     */
    void restore(String resourcePath);

    /**
     * Restore blank instance.
     *
     */
    void restoreBlank();
}
