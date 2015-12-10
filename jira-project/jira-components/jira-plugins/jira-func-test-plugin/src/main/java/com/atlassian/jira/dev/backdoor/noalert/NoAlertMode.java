package com.atlassian.jira.dev.backdoor.noalert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Whether noalerts mode is on.
 *
 * @since v6.0
 */
public class NoAlertMode
{
    private static final Logger log = LoggerFactory.getLogger(NoAlertMode.class);
    private final AtomicBoolean noAlerts = new AtomicBoolean();

    public boolean isOn()
    {
        return noAlerts.get();
    }

    public void set(boolean noAlertMode)
    {
        log.debug("Setting NoAlert={}", noAlertMode);
        noAlerts.set(noAlertMode);
    }
}
