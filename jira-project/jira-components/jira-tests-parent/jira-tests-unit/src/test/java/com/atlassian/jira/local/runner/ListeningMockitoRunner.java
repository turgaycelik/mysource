package com.atlassian.jira.local.runner;

import org.mockito.runners.MockitoJUnitRunner;

/**
 * An implementation of a listening runner that delegates to {@link org.mockito.runners.MockitoJUnitRunner} under
 * the covers
 *
 * @since v5.0
 */
public class ListeningMockitoRunner extends AbstractListeningRunner<Class<MockitoJUnitRunner>>
{
    public ListeningMockitoRunner(Class<?> classUnderTest)
    {
        super(classUnderTest, MockitoJUnitRunner.class);
    }
}
