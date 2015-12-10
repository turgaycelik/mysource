package com.atlassian.jira.util.system.check;

/**
 * Implement this interface to add a new check for a particular system environment settings that might cause problems
 * when running JIRA. Add new SystemEnvironmentChecks to the {@link com.atlassian.jira.util.system.check.SystemEnvironmentChecklist}
 *
 * @since v4.0
 */
public interface SystemEnvironmentCheck
{
    /**
     * This method returns the warning message, if no warning is neccessary it returns null.
     *
     * @return an I18nMessage or null
     */
    I18nMessage getWarningMessage();

}
