package com.atlassian.jira.studio.startup;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Properties;

/**
 * Callbacks that will be called during JIRA's startup process.
 *
 * @since v4.4.1
 */
public interface StudioStartupHooks
{
    /**
     * Called by JIRA during startup to get its log4j configuration. JIRA will re-initialise its log4j configuration with
     * the configuration returned from this function (see PropertyConfigurator).
     *
     * JIRA's current log4j configuration is passed as an argument to this method.
     *
     * The returned configuration will replace JIRA's current log4j configuration, and as such you will need to copy
     * any of the current configuration that you wish to keep into into the returned configuration. A null return
     * value means that JIRA should just keep its current configuration.
     *
     * @param initialConfiguration JIRA's current log4j configuration. This properties object is mutable and safe to
     * return from this method.
     *
     * @return the new log4j configuration that JIRA will use. If you wish to keep JIRA's current
     * log4j configuration, then you must ensure the passed configuraton is contained within the returned configuration.
     * A null return means that JIRA's current log4j configuration should be kept.
     */
    @Nullable
    public Properties getLog4jConfiguration(@Nonnull Properties initialConfiguration);

    /**
     * Called before JIRA has had a chance to start. This is before any validation (e.g. does JIRA.HOME exist?)
     * is performed.
     */
    public void beforeJiraStart();

    /**
     * Called after JIRA has started. This is called before any connection requests are accepted into JIRA.
     */
    public void afterJiraStart();
}
