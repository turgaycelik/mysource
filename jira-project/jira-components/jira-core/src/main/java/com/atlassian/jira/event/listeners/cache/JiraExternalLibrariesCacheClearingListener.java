package com.atlassian.jira.event.listeners.cache;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.web.servlet.rpc.AxisServletProvider;
import webwork.action.factory.ActionFactory;
import webwork.util.ValueStack;

/**
 * Used to clear caches of external libraries that JIRA uses, such as webwork.
 *
 * @since v4.1
 */
@EventComponent
public class JiraExternalLibrariesCacheClearingListener
{
    private final AxisServletProvider axisProvider;

    public JiraExternalLibrariesCacheClearingListener(AxisServletProvider axisProvider)
    {
        this.axisProvider = axisProvider;
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        ActionFactory.getActionFactory().flushCaches();
        // clear the method cache for JRA-16750
        ValueStack.clearMethods();
        CoreFactory.globalRefresh();
        axisProvider.reset();
    }
    
}
