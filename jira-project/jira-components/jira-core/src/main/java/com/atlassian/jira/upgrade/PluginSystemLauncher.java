package com.atlassian.jira.upgrade;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.startup.FailedStartupCheck;
import com.atlassian.jira.startup.JiraLauncher;
import com.atlassian.jira.startup.JiraStartupChecklist;
import com.atlassian.jira.util.ExceptionUtil;

import org.apache.log4j.Logger;

/**
 * Starts the JIRA plugins system with ALL available plugins
 *
 * @since v4.4
 */
public class PluginSystemLauncher implements JiraLauncher
{
    private static final Logger log = Logger.getLogger(PluginSystemLauncher.class);

    @Override
    public void start()
    {
        try
        {
            ComponentManager.getInstance().start();
            clearTomcatResources();
        }
        catch (final Exception ex)
        {
            log.fatal("A fatal error occured during initialisation. JIRA has been locked.", ex);
            JiraStartupChecklist.setFailedStartupCheck(new FailedStartupCheck("Component Manager", ExceptionUtil.getMessage(ex)));
        }
    }

    /**
     * If we are running in tomcat that uses WebappClassLoader lets clear resources cache. This prevents from excessive
     * load of resources (tomcat6) or classes (tomcat7) binary data. For reference see
     * https://jdog.jira-dev.com/browse/JDEV-28620. More information about tomcat here:
     * http://issues.apache.org/bugzilla/show_bug.cgi?id=53081 https://issues.apache.org/bugzilla/show_bug.cgi?id=56293
     */
    private void clearTomcatResources()
    {
        try
        {
            final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            final Class<? extends ClassLoader> contextClassLoaderClass = contextClassLoader.getClass();
            if ("org.apache.catalina.loader.WebappClassLoader".equals(contextClassLoaderClass.getName()))
            {
                final Field resourceEntriesField = contextClassLoaderClass.getDeclaredField("resourceEntries");
                resourceEntriesField.setAccessible(true);
                final Map resourceEntries = (Map) resourceEntriesField.get(contextClassLoader);
                // this mimics the behaviour of WebappClassLoader where writes to this field are guarded by lock on the
                // resourceEntries
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (resourceEntries) {
                    resourceEntriesField.set(contextClassLoader, new HashMap());
                }
            }
        }
        //It is safe to ignore those as we just will not clean the cache
        catch (final NoSuchFieldException exception)
        {
            log.warn(String.format("Tomcat's WebappClassLoader cache for resourceEntries not cleared."
                            + "Probably using different Tomcat version than 6 or 7 Message: %s",
                    ExceptionUtil.getMessage(exception)
            ));
        }
        catch (final IllegalAccessException exception)
        {
            log.warn("Tomcat's WebappClassLoader cache for resourceEntries not cleared. This should never happen?",
                    exception);
        }
        catch (final SecurityException exception)
        {
            log.warn("Tomcat's WebappClassLoader cache for resourceEntries not cleared. Have we started to use security managers?",
                    exception);
        }
        catch (final Exception exception)
        {
            log.warn(String.format("Tomcat's WebappClassLoader cache for resourceEntries not cleared. Message: %s",
                    ExceptionUtil.getMessage(exception)));
        }

    }

    @Override
    public void stop()
    {
        ComponentManager.getInstance().stop();
    }
}
