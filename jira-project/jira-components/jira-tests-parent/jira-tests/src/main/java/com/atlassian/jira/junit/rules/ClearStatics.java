package com.atlassian.jira.junit.rules;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.index.DefaultIndexManager;
import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.util.JiraKeyUtils;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import webwork.config.Configuration;

import java.lang.reflect.Field;

/**
 * Clear static variables in JIRA both before and after the test runs to improve test separation.
 * <p>
 * This clears out things like the component accessor's worker, the authentication context,
 * thread local searcher data, database configuration, and webwork configuration that may
 * pollute this or some other test.
 * </p><p>
 * Just add this to your test:
 * </p>
 * <code><pre>
 *     &#64;Rule public ClearStatics clearStatics = new ClearStatics();
 * </pre></code>
 *
 * @since 6.0
 */
public class ClearStatics extends TestWatcher
{

    @Override
    protected void finished(final Description description)
    {
        clearStatics();
    }

    @Override
    protected void starting(Description description)
    {
        clearStatics();
    }

    private static void clearStatics()
    {
        ComponentAccessor.initialiseWorker(null);
        DefaultOfBizConnectionFactory.getInstance().resetInstance();
        JiraAuthenticationContextImpl.clearRequestCache();
        DefaultIndexManager.flushThreadLocalSearchers();
        JiraKeyUtils.resetKeyMatcher();
        try
        {
            final Field field = Configuration.class.getDeclaredField("configurationImpl");
            field.setAccessible(true);
            field.set(Configuration.class,null);
        }
        catch (NoSuchFieldException e)
        {
            throw new AssertionError(e);
        }
        catch (IllegalAccessException e)
        {
            throw new AssertionError(e);
        }
    }

}
