package com.atlassian.velocity;

import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.template.velocity.VelocityEngineFactory;
import com.atlassian.jira.util.collect.CompositeMap;
import com.atlassian.util.concurrent.LazyReference;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static com.atlassian.jira.component.ComponentAccessor.getComponent;
import static com.atlassian.jira.datetime.DateTimeStyle.COMPLETE;

public class JiraVelocityManager extends DefaultVelocityManager
{
    private static final Logger log = Logger.getLogger(JiraVelocityManager.class);

    private final DateTimeFormatter dateTimeFormatter;

    private final VelocityEngineFactory velocityEngineFactory;

    public JiraVelocityManager(DateTimeFormatter dateTimeFormatter)
    {
        this.dateTimeFormatter = dateTimeFormatter != null ? dateTimeFormatter.forLoggedInUser().withStyle(COMPLETE) : null;
        this.velocityEngineFactory = getComponent(VelocityEngineFactory.class);
    }

    public JiraVelocityManager(final DateTimeFormatter dateTimeFormatter, final VelocityEngineFactory velocityEngineFactory)
    {
        this.velocityEngineFactory = velocityEngineFactory;
        this.dateTimeFormatter = dateTimeFormatter != null ? dateTimeFormatter.forLoggedInUser().withStyle(COMPLETE) : null;
    }

    /**
     * Returns a new DateFormat.
     *
     * @return a new DateFormat
     */
    @Override
    public DateFormat getDateFormat()
    {
        return new DelegateDateFormat(new DateFormatSupplier());
    }

    @SuppressWarnings ("unchecked")
    @Override
    protected VelocityContext createVelocityContext(final Map params)
    {
        // decorate the passed in map so we can modify it as the super call does...
        return super.createVelocityContext(CompositeMap.of(new HashMap<String, Object>(), (Map<String, Object>) params));
    }

    @Override
    @SuppressWarnings ("unchecked")
    protected Map<String, ?> createContextParams(final String baseurl, final Map contextParameters)
    {
        final Map<String, Object> result = new HashMap<String, Object>();
        result.put(Key.BASE_URL, baseurl);
        result.put(Key.FORMATTER, getDateFormat());
        return CompositeMap.of((Map<String, Object>) contextParameters, result);
    }

    @Override
    protected VelocityEngine getVe()
    {
        return velocityEngineFactory.getEngine();
    }

    static final class Key
    {
        static final String BASE_URL = "baseurl";
        static final String FORMATTER = "formatter";
    }

    class DateFormatSupplier extends LazyReference<DateFormat>
    {
        @Override
        protected DateFormat create()
        {
            return new SimpleDateFormat(dateTimeFormatter.getFormatHint());
        }
    }
}
