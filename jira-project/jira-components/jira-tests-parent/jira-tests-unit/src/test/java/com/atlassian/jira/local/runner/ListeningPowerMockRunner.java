package com.atlassian.jira.local.runner;

import org.powermock.modules.junit4.PowerMockRunner;

/**
 * An implementation of a listening runner that delegates to {@link org.powermock.modules.junit4.PowerMockRunner} under
 * the covers
 *
 * @since v4.3
 */
public class ListeningPowerMockRunner extends AbstractListeningRunner<Class<PowerMockRunner>>
{

    public ListeningPowerMockRunner(final Class<?> classUnderTest)
    {
        super(classUnderTest, PowerMockRunner.class);
    }
}
