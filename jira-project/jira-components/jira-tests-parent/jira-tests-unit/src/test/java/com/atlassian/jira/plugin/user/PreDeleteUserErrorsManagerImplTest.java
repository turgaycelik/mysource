package com.atlassian.jira.plugin.user;


import java.net.URI;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.tracker.PluginModuleTracker;

import com.google.common.collect.ImmutableList;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @since v6.0
 */
public class PreDeleteUserErrorsManagerImplTest
{
    private PreDeleteUserErrorsManagerImpl userDeletionModuleDescriptorManager;
    private PreDeleteUserErrors preDeleteUserErrors;
    private WebErrorMessage expectedWaring;

    @Before
    public void setUp() throws Exception
    {
        URI furtherInformation = new URI("http://www.atlassian.com");
        expectedWaring = new WebErrorMessageImpl("The user is involved in 5 Green Hopper Boards", "5 Green Hopper Boards", furtherInformation);
        preDeleteUserErrors = new MockPreDeleteUserErrors( ImmutableList.of(expectedWaring));
    }

    private PreDeleteUserErrorsManagerImpl getDeleteUserModuleDescriptorManager( final PreDeleteUserErrors pluginToCall )
    {
        return new PreDeleteUserErrorsManagerImpl(null,null)
        {

            @Override
            PluginModuleTracker<PreDeleteUserErrors, PreDeleteUserErrorsModuleDescriptor> createTracker(PluginAccessor pluginAccessor, PluginEventManager pluginEventManager)
            {
                return null;
            }

            Iterable<PreDeleteUserErrors> enabledModules()
            {
                return newArrayList( pluginToCall );

            }
        };
    }

    class MockPreDeleteUserErrors implements PreDeleteUserErrors
    {
        private final ImmutableList<WebErrorMessage> mocWebErrorMessages;

        public MockPreDeleteUserErrors(ImmutableList<WebErrorMessage> userOwnedEntites)
        {
            mocWebErrorMessages = userOwnedEntites;
        }
        @Override
        public List<WebErrorMessage> getPreDeleteUserErrors(final User user)
        {
            return mocWebErrorMessages;
        }
    }

    @Test
    public void testGetWarningsWhenPluginReturnsResult() throws Exception
    {
        userDeletionModuleDescriptorManager = getDeleteUserModuleDescriptorManager(preDeleteUserErrors);
        ImmutableList<WebErrorMessage> actual = userDeletionModuleDescriptorManager.getWarnings(null);
        assertEquals(ImmutableList.of(expectedWaring), actual);
    }

    @Test
    public void testGetWarningsWithNullWarnings() throws Exception
    {
        userDeletionModuleDescriptorManager = getDeleteUserModuleDescriptorManager(new MockPreDeleteUserErrors(null));
        ImmutableList<WebErrorMessage> actual = userDeletionModuleDescriptorManager.getWarnings(null);
        assertThat(actual, IsEmptyCollection.<WebErrorMessage>empty());
    }

    @Test
    public void testGetWarningsWithEmptyWarnings() throws Exception
    {
        userDeletionModuleDescriptorManager = getDeleteUserModuleDescriptorManager(new MockPreDeleteUserErrors(ImmutableList.<WebErrorMessage>of()));
        ImmutableList<WebErrorMessage> actual = userDeletionModuleDescriptorManager.getWarnings(null);
        assertThat(actual, IsEmptyCollection.<WebErrorMessage>empty());
    }
}
