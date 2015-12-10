package com.atlassian.jira.crowd.embedded;

import java.util.Map;

import com.atlassian.crowd.directory.SynchronisableDirectory;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.manager.directory.DirectorySynchroniser;
import com.atlassian.crowd.manager.directory.SynchronisationMode;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.util.collect.MapBuilder;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JiraDirectorySynchroniserTest
{
    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    private JiraDirectorySynchroniser jiraDirectorySynchroniser;
    @Mock
    private Directory directory;
    @Mock
    private DirectorySynchroniser directorySynchroniser;
    @Mock
    private SynchronisableDirectory remoteDirectory;

    @Before
    public void setUp()
    {
        jiraDirectorySynchroniser = new JiraDirectorySynchroniser();
    }

    @Test
    public void shouldUseIncrementalSyncWhenNoAttributesProvided() throws Exception
    {
        when(directory.getAttributes()).thenReturn(null);

        jiraDirectorySynchroniser.synchronizeDirectory(directorySynchroniser, directory, remoteDirectory);

        verify(directorySynchroniser).synchronise(remoteDirectory, SynchronisationMode.INCREMENTAL);
    }

    @Test
    public void shouldUseIncrementalSyncWhenNoValueIsProvidedForEnableIncSyncFlag() throws Exception
    {
        final Map<String, String> atrributes = MapBuilder.build(JiraDirectorySynchroniser.CROWD_SYNC_INCREMENTAL_ENABLED, null);
        when(directory.getAttributes()).thenReturn(atrributes);

        jiraDirectorySynchroniser.synchronizeDirectory(directorySynchroniser, directory, remoteDirectory);

        verify(directorySynchroniser).synchronise(remoteDirectory, SynchronisationMode.INCREMENTAL);
    }

    @Test
    public void shouldUseIncrementalSyncWhenFlagIsOn() throws Exception
    {
        final Map<String, String> atrributes = MapBuilder.build(JiraDirectorySynchroniser.CROWD_SYNC_INCREMENTAL_ENABLED, "true");
        when(directory.getAttributes()).thenReturn(atrributes);

        jiraDirectorySynchroniser.synchronizeDirectory(directorySynchroniser, directory, remoteDirectory);

        verify(directorySynchroniser).synchronise(remoteDirectory, SynchronisationMode.INCREMENTAL);
    }

    @Test
    public void shouldUseFullSyncWhenAnythingElseThanTrueIsSetForEnableIncSyncFlag() throws Exception
    {
        final Map<String, String> atrributes = MapBuilder.build(JiraDirectorySynchroniser.CROWD_SYNC_INCREMENTAL_ENABLED, "false");
        when(directory.getAttributes()).thenReturn(atrributes);

        jiraDirectorySynchroniser.synchronizeDirectory(directorySynchroniser, directory, remoteDirectory);

        verify(directorySynchroniser).synchronise(remoteDirectory, SynchronisationMode.FULL);
    }
}