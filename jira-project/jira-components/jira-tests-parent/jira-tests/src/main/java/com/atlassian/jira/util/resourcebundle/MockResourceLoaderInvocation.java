package com.atlassian.jira.util.resourcebundle;

import com.atlassian.plugin.Plugin;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * @since v6.2.3.
 */
class MockResourceLoaderInvocation extends ResourceLoaderInvocation
{
    @Override
    ResourceBundleLoader.LoadResult load()
    {
        Map<String, String> state = getState(getLocale(), getMode());

        return new ResourceBundleLoader.LoadResult(state, Collections.<Plugin>emptyList());
    }

    static Map<String, String> getState(final Locale locale, final Mode mode)
    {
        Map<String, String> state = Maps.newHashMap();
        state.put("locale", locale.toString());
        state.put("mode", mode.toString());
        return state;
    }
}
