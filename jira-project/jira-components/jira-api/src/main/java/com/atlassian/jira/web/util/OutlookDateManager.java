/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.util;

import java.util.Locale;

/**
 * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatterFactory#formatter()} instead. Since v4.4.
 */
@Deprecated
public interface OutlookDateManager
{
    /**
     * Refreshes all the Outlook dates so they contain the new date format
     */
    public void refresh();

    /**
     * Returns an instance of {@link OutlookDate} for an specified locale.
     *
     * @param locale This parameter is ignored
     * @return An instance of {@link OutlookDate}
     * @deprecated Use {@link com.atlassian.jira.datetime.DateTimeFormatterFactory#formatter()} instead. Since v4.4.
     */
    @Deprecated
    public OutlookDate getOutlookDate(Locale locale);
}