package com.atlassian.jira.my_home;

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.plugin.myjirahome.MyJiraHomeChangedEvent;
import com.atlassian.jira.plugin.myjirahome.MyJiraHomeUpdateException;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import org.junit.Test;

import javax.annotation.Nullable;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MyJiraHomeStorageImplTest
{
    private static final String MY_JIRA_HOME = "my.jira.home";

    private User mockUser = mock(User.class);
    private Preferences mockUserPreferences = mock(Preferences.class);
    private UserPreferencesManager mockUserPreferencesManager = mock(UserPreferencesManager.class);
    private EventPublisher mockEventPublisher = mock(EventPublisher.class);

    private MyJiraHomeStorageImpl storage = new MyJiraHomeStorageImpl(mockUserPreferencesManager, mockEventPublisher);

    @Test
    public void testLoadNoPreferencesForUserFallBackToEmptyString() throws Exception
    {
        assertThat(storage.load(mockUser), is(""));
    }

    @Test
    public void testLoadWrapNullByEmptyString()
    {
        expectUserPreferencesManagerGetPreference();
        expectUserPreferencesGetString(null);

        assertThat(storage.load(mockUser), is(""));
    }

    @Test
    public void testLoad()
    {
        expectUserPreferencesManagerGetPreference();
        expectUserPreferencesGetString(MY_JIRA_HOME);

        assertThat(storage.load(mockUser), is(MY_JIRA_HOME));
    }

    @Test
    public void testStoreExceptionWhileRemoval() throws AtlassianCoreException
    {
        expectUserPreferencesManagerGetPreference();
        doThrow(new AtlassianCoreException()).when(mockUserPreferences).remove(anyString());

        try
        {
            storage.store(mockUser, MY_JIRA_HOME);
        }
        catch (MyJiraHomeUpdateException e)
        {
            assertThat(e.getCause(), is(instanceOf(AtlassianCoreException.class)));
        }
    }

    @Test
    public void testStoreRemoveProperty() throws AtlassianCoreException
    {
        expectUserPreferencesManagerGetPreference();

        storage.store(mockUser, "");

        verifyHomeRemoved();
        verifyEventPublished();
    }


    @Test
    public void testStoreRemovePropertyWhitespacesOnly() throws AtlassianCoreException
    {
        expectUserPreferencesManagerGetPreference();

        storage.store(mockUser, "    ");

        verifyHomeRemoved();
        verifyEventPublished();
    }

    @Test
    public void testStoreExceptionWhileUpdate() throws Exception
    {
        expectUserPreferencesManagerGetPreference();
        doThrow(new AtlassianCoreException()).when(mockUserPreferences).setString(anyString(), anyString());

        try
        {
            storage.store(mockUser, MY_JIRA_HOME);
            fail("expected exception");
        }
        catch (MyJiraHomeUpdateException e)
        {
            assertThat(e.getCause(), is(instanceOf(AtlassianCoreException.class)));
        }
    }


    @Test
    public void testStoreUpdateProperty() throws Exception
    {
        expectUserPreferencesManagerGetPreference();

        storage.store(mockUser, MY_JIRA_HOME);

        verifyHomeUpdated(MY_JIRA_HOME);
        verifyEventPublished();
    }

    private void expectUserPreferencesManagerGetPreference()
    {
        when(mockUserPreferencesManager.getPreferences(mockUser)).thenReturn(mockUserPreferences);
    }

    private void expectUserPreferencesGetString(@Nullable final String myHome)
    {
        when(mockUserPreferences.getString(anyString())).thenReturn(myHome);
    }

    private void verifyHomeUpdated(final String newHome) throws AtlassianCoreException
    {
        verify(mockUserPreferences).setString(anyString(), eq(newHome));
    }

    private void verifyHomeRemoved() throws AtlassianCoreException
    {
        verify(mockUserPreferences).remove(anyString());
    }

    private void verifyEventPublished()
    {
        verify(mockEventPublisher).publish(any(MyJiraHomeChangedEvent.class));
    }

}
