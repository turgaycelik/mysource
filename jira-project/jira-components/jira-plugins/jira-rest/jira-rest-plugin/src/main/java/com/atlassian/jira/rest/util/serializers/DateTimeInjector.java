package com.atlassian.jira.rest.util.serializers;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import com.atlassian.jira.rest.Dates;
import com.atlassian.jira.util.I18nHelper;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Enables DateTime to be used as a QueryParam.
 */
@Provider
public class DateTimeInjector extends PerRequestTypeInjectableProvider<QueryParam, DateTime>
{
    private final I18nHelper i18nHelper;

    @Inject
    public DateTimeInjector(final I18nHelper i18n)
    {
        super(DateTime.class);
        i18nHelper = i18n;
    }

    @Override
    public Injectable<DateTime> getInjectable(final ComponentContext cc, final QueryParam a)
    {

        return new AbstractHttpContextInjectable<DateTime>()
        {
            @Override
            public DateTime getValue(HttpContext ctx)
            {
                final List<String> values = ctx.getRequest().getQueryParameters().get(a.value());

                if (values == null || values.isEmpty())
                {
                    return null;
                }
                if (values.size() > 1)
                {
                    final String badDateParameterMessage = i18nHelper.getText("BAD_DATE", a.value());
                    throw new WebApplicationException(Response.status(Status.BAD_REQUEST).
                            entity(badDateParameterMessage).build());
                }

                try
                {
                    final DateTime paramDate = Dates.fromISODateTimeString(values.get(0));
                    return paramDate;
                }
                catch (IllegalArgumentException exc)
                {
                    final String badDateMessage = i18nHelper.getText("BAD_DATE", values.get(0));
                    throw new WebApplicationException(Response.status(Status.BAD_REQUEST).
                            entity(badDateMessage).build());
                }
            }
        };
    }
}