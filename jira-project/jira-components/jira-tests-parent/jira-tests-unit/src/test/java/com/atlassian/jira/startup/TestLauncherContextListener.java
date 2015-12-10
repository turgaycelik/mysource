package com.atlassian.jira.startup;

import javax.servlet.ServletContextEvent;

import com.atlassian.jira.matchers.johnson.JohnsonEventMatchers;
import com.atlassian.jira.mock.servlet.MockServletContext;
import com.atlassian.jira.studio.startup.MockStartupHooks;
import com.atlassian.jira.studio.startup.StudioStartupHooks;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Test;

import static com.atlassian.jira.matchers.IterableMatchers.hasItemsThat;
import static com.atlassian.jira.matchers.IterableMatchers.iterableWithSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 * Test case for {@link com.atlassian.jira.startup.LauncherContextListener}.
 *
 * @since v5.1.6
 */
public class TestLauncherContextListener
{

    @After
    public void resetStudioStartupHooks()
    {
        MockStartupHooks.resetHooks();
    }

    @Test
    public void shouldNotEscapeRuntimeExceptionsOnStartup()
    {
        StudioStartupHooks hooks = mock(StudioStartupHooks.class);
        doThrow(new RuntimeException()).when(hooks).beforeJiraStart();
        MockStartupHooks.setHooks(hooks);
        final MockServletContext mockContext = new MockServletContext();
        new LauncherContextListener().contextInitialized(new ServletContextEvent(mockContext));
        assertTrue(JohnsonEventContainer.get(mockContext).hasEvents());
        assertFatalJohnsonEvent(mockContext);

    }

    @Test(expected = Error.class)
    public void shouldLetEscapeErrorOnStartup()
    {
        StudioStartupHooks hooks = mock(StudioStartupHooks.class);
        doThrow(new Error()).when(hooks).beforeJiraStart();
        MockStartupHooks.setHooks(hooks);
        final MockServletContext mockContext = new MockServletContext();
        new LauncherContextListener().contextInitialized(new ServletContextEvent(mockContext));

    }

    private void assertFatalJohnsonEvent(MockServletContext mockContext)
    {
        assertTrue(JohnsonEventContainer.get(mockContext).hasEvents());
        assertThat(getEvents(mockContext), Matchers.<Iterable<Event>>allOf(
                iterableWithSize(1, Event.class),
                hasItemsThat(Event.class, Matchers.<Event>allOf(
                        JohnsonEventMatchers.hasKey("startup-unexpected"),
                        JohnsonEventMatchers.containsDescription("Unexpected exception during JIRA startup. "
                                + "This JIRA instance will not be able to recover."),
                        JohnsonEventMatchers.hasLevel("fatal")
                ))));
    }

    @SuppressWarnings ("unchecked")
    private Iterable<Event> getEvents(MockServletContext mockContext)
    {
        return JohnsonEventContainer.get(mockContext).getEvents();
    }
}
