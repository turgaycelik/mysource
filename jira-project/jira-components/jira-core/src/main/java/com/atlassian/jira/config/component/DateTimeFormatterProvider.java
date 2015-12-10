package com.atlassian.jira.config.component;

import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;

import org.picocontainer.injectors.ProviderAdapter;

/**
 * @since v6.2
 */
public class DateTimeFormatterProvider extends ProviderAdapter
{
    public DateTimeFormatter provide(DateTimeFormatterFactory factory)
    {
        return AbstractDelegatedMethodInterceptor.createProxy(DateTimeFormatter.class, new DateTimeFormatterInterceptor(factory));
    }

    static class DateTimeFormatterInterceptor extends AbstractDelegatedMethodInterceptor<DateTimeFormatter>
    {
        private final DateTimeFormatterFactory factory;

        DateTimeFormatterInterceptor(final DateTimeFormatterFactory factory) {this.factory = factory;}

        @Override
        protected DateTimeFormatter getDelegate()
        {
            return factory.formatter();
        }
    }
}
