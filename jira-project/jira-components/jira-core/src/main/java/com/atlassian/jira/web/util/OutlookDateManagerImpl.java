/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.util;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.util.concurrent.CopyOnWriteMap;

import java.util.Locale;
import java.util.concurrent.ConcurrentMap;

@EventComponent
public class OutlookDateManagerImpl implements OutlookDateManager
{
    private final ConcurrentMap<Locale, OutlookDate> outlookDates = CopyOnWriteMap.newHashMap();
    private final ApplicationProperties applicationProperties;
    private final I18nHelper.BeanFactory i18nHelperFactory;
    private final DateTimeFormatterFactory dateTimeFormatterFactory;

    public OutlookDateManagerImpl()
    {
        this(ComponentAccessor.getApplicationProperties(), null, ComponentAccessor.getComponentOfType(DateTimeFormatterFactory.class));
    }

    public OutlookDateManagerImpl(final ApplicationProperties applicationProperties, final I18nHelper.BeanFactory i18nHelperFactory, DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        this.applicationProperties = applicationProperties;
        this.i18nHelperFactory = i18nHelperFactory;
        this.dateTimeFormatterFactory = dateTimeFormatterFactory;
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        refresh();
    }

    public void refresh()
    {
        outlookDates.clear();
    }

    public OutlookDate getOutlookDate(final Locale locale)
    {
        OutlookDate result = outlookDates.get(locale);
        while (result == null)
        {
            outlookDates.putIfAbsent(locale, new OutlookDate(locale, applicationProperties, i18nHelperFactory, dateTimeFormatterFactory));
            result = outlookDates.get(locale);
        }
        return result;
    }
}
