package com.atlassian.jira.datetime;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * JodaFormatterSupplier for use in tests.
 *
 * @since v4.4
 */
public class JodaFormatterSupplierStub implements JodaFormatterSupplier
{
    @Override
    public DateTimeFormatter get(Key key)
    {
        return DateTimeFormat.forPattern(key.pattern).withLocale(key.locale);
    }
}
