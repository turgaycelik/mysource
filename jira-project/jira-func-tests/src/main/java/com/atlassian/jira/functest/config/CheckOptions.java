package com.atlassian.jira.functest.config;

import java.util.Set;

/**
 * The options passed to each {@link com.atlassian.jira.functest.unittests.config.ConfigurationCheck}.
 *
 * @since v4.0
 */
public interface CheckOptions
{
    /**
     * Returns true if check should be executed.
     *
     * @param checkId the check to validate.
     * @return true if the passed check should be executed.
     */
    boolean checkEnabled(String checkId);

    /**
     * Returns a representation of the options as a set of suppresscheck options.
     *
     * @return a representation of the options as a set of suppresscheck options.
     */
    Set<String> asSuppressChecks();
}
