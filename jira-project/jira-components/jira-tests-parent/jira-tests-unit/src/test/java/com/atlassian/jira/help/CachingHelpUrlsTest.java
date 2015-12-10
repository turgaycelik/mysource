package com.atlassian.jira.help;

import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.JiraStartedEvent;
import com.atlassian.jira.mock.plugin.MockPlugin;
import com.atlassian.jira.mock.plugin.NullModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;
import com.atlassian.plugin.event.events.PluginRefreshedEvent;
import com.google.common.collect.Lists;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.help.MockHelpUrlsLoader.MockHelpUrlsLoaderKey;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @since v6.2.4
 */
public class CachingHelpUrlsTest
{
    private MockHelpUrlsLoader loader;
    private CachingHelpUrls urls;

    private MockHelpUrlsLoaderKey oneKey;
    private MockHelpUrl oneUrl;
    private MockHelpUrls oneUrls;

    private MockHelpUrlsLoaderKey twoKey;
    private MockHelpUrls twoUrls;
    private MockHelpUrl twoUrl;

    private final Plugin plugin = new MockPlugin("test");
    private final ModuleDescriptor<?> descriptor = new NullModuleDescriptor(plugin, "random");

    @Before
    public void setUp() throws Exception
    {
        MockHelpUrlsLoader loader = new MockHelpUrlsLoader();

        oneKey = loader.createKey(1);
        oneUrls = loader.createUrls(oneKey);
        oneUrl = oneUrls.createUrl("one");

        twoKey = loader.createKey(2);
        twoUrls = loader.createUrls(twoKey);
        twoUrl = twoUrls.createUrl("two");

        this.urls = new CachingHelpUrls(this.loader = Mockito.spy(loader));
    }

    @Test
    public void getHelpUrlCaches()
    {
        //Set the "current user" to 1 and make sure we get the right URLs.
        loader.setCurrentId(1);
        assertThat(urls.getUrl("one"), new HelpUrlMatcher(oneUrl));
        assertThat(urls.getUrl("one"), new HelpUrlMatcher(oneUrl));

        //Set the "current user" to 2 and make sure we get the right URLs.
        loader.setCurrentId(2);
        assertThat(urls.getUrl("two"), new HelpUrlMatcher(twoUrl));
        assertThat(urls.getUrl("two"), new HelpUrlMatcher(twoUrl));

        //Make sure the lookup uses defaults if necessary.
        assertThat(urls.getUrl("one"), new HelpUrlMatcher(twoUrls.getDefaultUrl()));
        assertThat(urls.getUrl("one"), new HelpUrlMatcher(twoUrls.getDefaultUrl()));

        //Make sure the actual load only happened once.
        verify(loader, times(1)).apply(oneKey);
        verify(loader, times(1)).apply(twoKey);
    }


    @Test
    public void getHelpDefaultUrlCaches()
    {
        //Set the "current user" to 1 and make sure we get the right URLs.
        loader.setCurrentId(1);
        assertThat(urls.getDefaultUrl(), new HelpUrlMatcher(oneUrls.getDefaultUrl()));
        assertThat(urls.getDefaultUrl(), new HelpUrlMatcher(oneUrls.getDefaultUrl()));

        //Set the "current user" to 2 and make sure we get the right URLs.
        //Make sure the lookup uses defaults if necessary.
        loader.setCurrentId(2);
        assertThat(urls.getDefaultUrl(), new HelpUrlMatcher(twoUrls.getDefaultUrl()));
        assertThat(urls.getDefaultUrl(), new HelpUrlMatcher(twoUrls.getDefaultUrl()));

        //Make sure the actual load only happened once.
        verify(loader, times(1)).apply(oneKey);
        verify(loader, times(1)).apply(twoKey);
    }

    @Test
    public void getHelpKeysCaches()
    {
        //Set the "current user" to 1 and make sure we get the right URLs.
        loader.setCurrentId(1);
        assertThat(urls.getUrlKeys(), equalTo(oneUrls.getUrlKeys()));
        assertThat(urls.getUrlKeys(), equalTo(oneUrls.getUrlKeys()));

        //Set the "current user" to 2 and make sure we get the right URLs.
        //Make sure the lookup uses defaults if necessary.
        loader.setCurrentId(2);
        assertThat(urls.getUrlKeys(), equalTo(twoUrls.getUrlKeys()));
        assertThat(urls.getUrlKeys(), equalTo(twoUrls.getUrlKeys()));

        //Make sure the actual load only happened once.
        verify(loader, times(1)).apply(oneKey);
        verify(loader, times(1)).apply(twoKey);
    }

    @Test
    public void getUrlsCaches()
    {
        //Set the "current user" to 1 and make sure we get the right URLs.
        loader.setCurrentId(1);
        assertThat(urls, containsInAnyOrder(equalsTo(oneUrls)));
        assertThat(urls, containsInAnyOrder(equalsTo(oneUrls)));

        //Set the "current user" to 2 and make sure we get the right URLs.
        //Make sure the lookup uses defaults if necessary.
        loader.setCurrentId(2);
        assertThat(urls, containsInAnyOrder(equalsTo(twoUrls)));
        assertThat(urls, containsInAnyOrder(equalsTo(twoUrls)));

        //Make sure the actual load only happened once.
        verify(loader, times(1)).apply(oneKey);
        verify(loader, times(1)).apply(twoKey);
    }

    @Test
    public void clearCacheOnModuleEnabled()
    {
        assertCacheClear(new PluginModuleEnabledEvent(descriptor));
    }

    @Test
    public void clearCacheOnModuleDisabled()
    {
        assertCacheClear(new PluginModuleDisabledEvent(descriptor, false));
    }

    @Test
    public void clearCacheOnPluginDisabled()
    {
        assertCacheClear(new PluginDisabledEvent(plugin));
    }

    @Test
    public void clearCacheOnPluginEnabled()
    {
        assertCacheClear(new PluginEnabledEvent(plugin));
    }

    @Test
    public void clearCacheOnPluginRefreshed()
    {
        assertCacheClear(new PluginRefreshedEvent(plugin));
    }

    @Test
    public void clearCacheOnClearCacheEvent()
    {
        assertCacheClear(ClearCacheEvent.INSTANCE);
    }

    @Test
    public void clearCacheOnJiraStarted()
    {
        assertCacheClear(new JiraStartedEvent());
    }

    private <T> Collection<Matcher<? super T>> equalsTo(Iterable<? extends T> items)
    {
        List<Matcher<? super T>> matchers = Lists.newArrayList();
        for (T item : items)
        {
            matchers.add(Matchers.equalTo(item));
        }
        return matchers;
    }

    private void assertCacheClear(Object event)
    {
        //Set the "current user" to 1 and make sure we get the right URLs.
        loader.setCurrentId(1);
        assertThat(urls.getUrlKeys(), equalTo(oneUrls.getUrlKeys()));
        verify(loader, times(1)).apply(oneKey);

        String name = removeEnd(event.getClass().getSimpleName(), "Event");
        name = Character.toLowerCase(name.charAt(0)) + name.substring(1);

        try
        {
            Method method = urls.getClass().getMethod(name, event.getClass());
            try
            {
                method.invoke(urls, event);
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
            catch (InvocationTargetException e)
            {
                throw new RuntimeException(e);
            }
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }

        //Make sure the URL is loaded again.
        assertThat(urls.getUrlKeys(), equalTo(oneUrls.getUrlKeys()));
        verify(loader, times(2)).apply(oneKey);
    }
}
