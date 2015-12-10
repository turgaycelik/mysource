package com.atlassian.jira.instrumentation.httpsession;

import com.atlassian.jira.instrumentation.InstrumentationName;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import static com.atlassian.jira.instrumentation.Instrumentation.pullGauge;

/**
 * Keeps track of HttpSession related stuff
 *
 * @since v4.4
 */
public class HttpSessionInstrumentationSupport implements HttpSessionListener, HttpSessionAttributeListener
{
    @Override
    public void sessionCreated(HttpSessionEvent se)
    {
        pullGauge(InstrumentationName.HTTP_SESSIONS).incrementAndGet();
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se)
    {
        pullGauge(InstrumentationName.HTTP_SESSIONS).decrementAndGet();
    }


    @Override
    public void attributeAdded(HttpSessionBindingEvent se)
    {
        pullGauge(InstrumentationName.HTTP_SESSION_OBJECTS).incrementAndGet();
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent se)
    {
        pullGauge(InstrumentationName.HTTP_SESSION_OBJECTS).decrementAndGet();
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent se)
    {
    }
}
