package com.atlassian.jira.auditing.handlers;

import java.util.List;
import java.util.Locale;

import com.atlassian.jira.auditing.ChangedValue;
import com.atlassian.jira.auditing.ChangedValueImpl;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * @since v6.2
 */
public class ChangedValuesBuilder
{
    final List<ChangedValue> changedValues = Lists.newArrayListWithCapacity(4);

    public ChangedValuesBuilder addIfDifferent(String name, String from, String to) {
        if ((from == null && to != null) || (from != null && !from.equals(to)))
        {
            changedValues.add(createI18nChangeValue(name, from, to));
        }
        return this;
    }

    public ImmutableList<ChangedValue> build()
    {
        return ImmutableList.copyOf(changedValues);
    }

    private static ChangedValueImpl createI18nChangeValue(final String name, final String from, final String to)
    {
        return new ChangedValueImpl(getI18n().getText(name), from, to);
    }

    private static I18nHelper getI18n()
    {
        // You must not cache I18nHelper
        return ComponentAccessor.getI18nHelperFactory().getInstance(Locale.ENGLISH);
    }

    public ChangedValuesBuilder add(final String name, final String from, final String to)
    {
        changedValues.add(createI18nChangeValue(name, from, to));
        return this;
    }
}
