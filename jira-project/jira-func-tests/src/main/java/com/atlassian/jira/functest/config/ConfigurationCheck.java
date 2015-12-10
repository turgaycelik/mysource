package com.atlassian.jira.functest.config;

import java.util.Collection;

/**
 * Interface for objects that wish to check the correctness of some JIRA xml.
 *
 * @since v4.0
 */
public interface ConfigurationCheck
{
    /**
     * Check the correctness of the passed JIRA XML. It must be possible to call this method multiple times with
     * different agruments (i.e. the object should be stateless).
     *
     * @param config the configuration to check.
     * @param options the context for the check. This is where errors can be reported.
     * @return the errors that were found in the passed configuration file.
     */
    Result checkConfiguration(JiraConfig config, CheckOptions options);

    /**
     * Fix the passed JIRA configuration and remove any errors.
     *
     * @param config the configuration to check.
     * @param options the context for the check. This can be used to see what checks are currently enabled for the
     *  passed configuration.
     */
    void fixConfiguration(JiraConfig config, CheckOptions options);

    public interface Result
    {
        public Collection<CheckMessage> getErrors();
        public Collection<CheckMessage> getWarnings();
        public boolean isGood();
    }
}
