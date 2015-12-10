package com.atlassian.jira.event;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.JiraManager;

import java.util.Map;

/**
 * Responsible for maintaining a event listeners.  These are generally configured in the database,
 * and implementing Listener classes must implement the {@link JiraListener} interface.
 */
@PublicApi
public interface ListenerManager extends JiraManager
{
    /**
     * Returns a map of listeners. The map contains mappings from the listener name to listener class.
     *
     * @return A map with name -> class mappings.
     */
    Map<String, JiraListener> getListeners();

    /**
     * Creates a new JiraListener.
     *
     * @param name The name of the Listener
     * @param clazz The class of the Listener.
     * @return the newly created JiraListener
     */
    JiraListener createListener(String name, Class<? extends JiraListener> clazz);

    /**
     * Deletes any listeners of the given class.
     *
     * @param clazz The class of the Listener.
     */
    void deleteListener(Class<? extends JiraListener> clazz);

    /**
     * Reloads the map of listeners from the db.
     */
    void refresh();

    /**
     * Invoked when another node in the cluster has refreshed its listeners.
     */
    void onRefreshListeners();
}
