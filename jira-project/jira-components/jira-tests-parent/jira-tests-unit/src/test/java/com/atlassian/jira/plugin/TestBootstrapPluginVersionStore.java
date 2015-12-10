package com.atlassian.jira.plugin;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit test of {@link BootstrapPluginVersionStore}.
 */
public class TestBootstrapPluginVersionStore
{
    private PluginVersionStore store;
    @Mock private PluginVersion mockPluginVersion;

    @Before
    public void setUpInstanceUnderTest()
    {
        MockitoAnnotations.initMocks(this);
        store = new BootstrapPluginVersionStore();
    }

    @Test
    public void testSave() throws Exception
    {
        // Invoke
        final long result = store.save(mockPluginVersion);

        // Check
        assertEquals(0, result);
        verifyNoMoreInteractions(mockPluginVersion);
    }
}
