package com.atlassian.jira.event;

import java.util.HashMap;
import java.util.Map;

/**
 * A Noop listener manager.  Register this in ManagerFactory to disable email notifications, e.g. during bulk imports.
 */
public class SubvertedListenerManager implements ListenerManager
{
    public Map<String, JiraListener> getListeners()
    {
        return new HashMap<String, JiraListener>();
    }

    @Override
    public JiraListener createListener(String name, Class<? extends JiraListener> clazz)
    {
        return null;
    }

    @Override
    public void deleteListener(Class<? extends JiraListener> clazz)
    {
    }

    @Override
    public void refresh()
    {
    }

    @Override
    public void onRefreshListeners()
    {
    }
}
