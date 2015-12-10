package com.atlassian.jira.mock.plugin.webfragments;

import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.ContextProvider;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.conditions.ConditionLoadingException;

import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Mock implementation of {@link com.atlassian.plugin.web.WebFragmentHelper}.
 *
 * @since v4.2
 */
public class MockWebFragmentHelper implements WebFragmentHelper
{
    private final I18nHelper i18nHelper;

    public MockWebFragmentHelper(final I18nHelper i18nHelper)
    {
        notNull("i18nHelper", i18nHelper);
        this.i18nHelper = i18nHelper;
    }

    public Condition loadCondition(final String className, final Plugin plugin) throws ConditionLoadingException
    {
        // TODO
        return null;
    }

    public ContextProvider loadContextProvider(final String className, final Plugin plugin)
            throws ConditionLoadingException
    {
        // TODO
        return null;
    }

    public String getI18nValue(final String key, final List<?> arguments, final Map<String, Object> context)
    {
        return i18nHelper.getText(key);
    }

    public String renderVelocityFragment(final String fragment, final Map<String, Object> context)
    {
        return fragment;
    }
}
