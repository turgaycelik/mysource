/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service;

import com.atlassian.annotations.PublicApi;
import com.atlassian.scheduler.JobRunnerResponse;

import javax.annotation.Nullable;

/**
 * Proxies calls to JiraService & manages delay between calls.
 *
 * @see JiraService
 */
@PublicApi
public interface JiraServiceContainer extends JiraService
{
    Long getId();

    long getDelay();

    void setDelay(long delay);

    long getLastRun();

    /**
     * Record when run.
     * @deprecated Since 6.2
     */
    void setLastRun();

    /**
     * Is this service due to run at the specified time.
     * @param time the time to check whether it is due.
     * @return true if due.
     */
    boolean isDueAt(long time);

    boolean isRunning();

    String getServiceClass();

    /**
     * @return the Class object of the underlying service, or null if service could not be loaded
     * @since 5.0
     */
    @Nullable
    Class<? extends JiraService> getServiceClassObject();

    boolean isUsable();

    /**
     *  Returns true if in a clustered environment this service should run locally on every node.
     *
     * @return true if in a clustered environment this service should run locally on every node.
     * @since v6.3
     */
    boolean isLocalService();
}
