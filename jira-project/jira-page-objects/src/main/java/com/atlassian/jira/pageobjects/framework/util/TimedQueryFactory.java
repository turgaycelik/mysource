package com.atlassian.jira.pageobjects.framework.util;

import javax.inject.Inject;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.query.AbstractTimedQuery;
import com.atlassian.pageobjects.elements.query.ExpirationHandler;
import com.atlassian.pageobjects.elements.query.Queries;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.pageobjects.elements.timeout.Timeouts;

import com.google.common.base.Supplier;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An injectable componenta that builds conditions and queries for suppliers/predicates.
 *
 * @since 5.2
 */
public class TimedQueryFactory
{
    // this need to move to atlassian-selenium: SELENIUM-203

    private final Timeouts timeouts;
    private final PageBinder pageBinder;

    @Inject
    public TimedQueryFactory(Timeouts timeouts, PageBinder pageBinder)
    {
        this.timeouts = timeouts;
        this.pageBinder = pageBinder;
    }


    public <T> TimedQuery<T> forSupplier(Supplier<T> supplier)
    {
        return Queries.forSupplier(timeouts, supplier);
    }

    public <T> TimedQuery<T> forSupplier(final Supplier<T> supplier, TimeoutType timeoutType)
    {
        // copied from atlassian-selenium
        notNull("timeoutType", timeoutType);
        notNull("supplier", supplier);
        return new AbstractTimedQuery<T>(timeouts.timeoutFor(timeoutType),
                timeouts.timeoutFor(TimeoutType.EVALUATION_INTERVAL), ExpirationHandler.RETURN_CURRENT) {
            @Override
            protected T currentValue() {
                return supplier.get();
            }

            @Override
            protected boolean shouldReturn(T currentEval)
            {
                return true;
            }
        };
    }
}
