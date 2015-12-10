package com.atlassian.jira.servlet;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.crowd.embedded.ofbiz.OfBizUserDao;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.system.status.ApplicationState;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;


/**
 * Servlet that provides application status information. This servlet should *not* be managed through Spring, because it
 * also needs to be functional if the Spring application context fails to start up.
 */
public class ApplicationStatusServlet extends HttpServlet
{
    private static final ComponentManager componentManager = ComponentManager.getInstance();

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException
    {
        ApplicationState state = getApplicationState();

        resp.setStatus(state == ApplicationState.ERROR ? SC_INTERNAL_SERVER_ERROR : SC_OK);
        resp.setContentType("application/json");
        resp.getWriter().append("{\"state\":\"").append(state.name()).append("\"}");
    }

    /**
     * Returns the application state of the server
     */
    private ApplicationState getApplicationState()
    {
        if (hasErrors())
        {
            return ApplicationState.ERROR;
        }

        if (!hasStarted())
        {
            return ApplicationState.STARTING;
        }

        if (isFirstRun())
        {
            return ApplicationState.FIRST_RUN;
        }

        if (!isUserCacheInitialized())
        {
            return ApplicationState.STARTING;
        }

        return ApplicationState.RUNNING;
    }

    /**
     * We go through all the johnson events and see if we find any ERROR or FATAL message.
     *
     * @return true if the server is reporting errors, false if not
     */
    public boolean hasErrors()
    {
        final JohnsonEventContainer johnsonEventContainer = JohnsonEventContainer.get(getServletContext());

        if (johnsonEventContainer != null && johnsonEventContainer.hasEvents())
        {
            final Collection<Event> events = johnsonEventContainer.getEvents();

            for (Event event : events)
            {
                if (event.getLevel() != null)
                {
                    final String level = event.getLevel().getLevel();

                    if (EventLevel.ERROR.equals(level) || EventLevel.FATAL.equals(level))
                    {
                        return true;
                    }
                }

            }
        }

        return false;
    }

    /**
     * We evaluate several flags inside the component manager to verify that the server is running or not
     *
     * @return true if the server is starting, false if not.
     */
    private boolean hasStarted()
    {
        ComponentManager.State state = componentManager.getState();

        return state.isComponentsRegistered() && state.isContainerInitialised()
                && state.isPluginSystemStarted() && state.isStarted();
    }

    /**
     * Checks if Jira is setup or not
     *
     * @return true if jira is not setup, false if is not
     */
    private boolean isFirstRun()
    {
        return !JiraUtils.isSetup();
    }

    /**
     * We want to know if the cache of users is initialized so users can login, if this cache is not populated is
     * useless to tell the load balancer we are ready There are certain phases of initialization that Pico could tell me
     * that everything is good and the ComponentAccessor not returning me the proper reference. That is why we need to
     * try/catch to avoid any weird exception sent to the load balancer
     *
     * @return true if the cache is populated, false if not
     */
    public boolean isUserCacheInitialized()
    {
        try
        {
            return ComponentAccessor.getComponent(OfBizUserDao.class).isCacheInitialized();
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
