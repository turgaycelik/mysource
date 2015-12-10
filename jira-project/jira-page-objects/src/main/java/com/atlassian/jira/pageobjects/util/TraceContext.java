package com.atlassian.jira.pageobjects.util;

import com.atlassian.pageobjects.elements.query.AbstractTimedCondition;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Function;
import com.google.inject.Inject;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriverException;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static com.google.common.collect.Lists.transform;

/**
 * Provides utility methods for waiting for ajax results. To send events from the javascript side, call JIRA.trace(key, args...).
 * To turn on logging: <pre>org.apache.log4j.Logger.getLogger(TraceContext.class).setLevel(org.apache.log4j.Level.DEBUG);</pre>
 */
public class TraceContext
{
    private static final Logger log = Logger.getLogger(TraceContext.class);

    @Inject
    private AtlassianWebDriver executor;

    @Inject
    private Timeouts timeouts;

    private static List<TraceEntry> allTraces = Collections.synchronizedList(new LinkedList<TraceEntry>());

    /**
     * Returns a tracer containing the current state of trace list.
     */
    public Tracer checkpoint()
    {
        try
        {
            // Attempt to retrieve all traces before snapshotting the checkpoint.
            retrieveTraces();
        }
        catch (WebDriverException ex)
        {
            // Ignore - this fails when the WebDriver browser has not yet loaded, eg if we are taking a checkpoint
            // before the first page load to wait for an ajax request that is fired from document.onready
        }
        int checkpoint = allTraces.size();
        log.debug("Returned checkpoint at position " + checkpoint);
        return new Tracer(checkpoint);
    }

    /**
     * Waits for the occurrence of a trace with the given key after the given tracer.
     * @param tracer checkpoint. Only traces after this checkpoint will be inspected.
     * @param key tracer key to watch for
     */
    public void waitFor(final Tracer tracer, final String key)
    {
        waitFor(tracer, key, null);
    }

    /**
     * Waits for the occurrence of a trace with the given key and optionally test argument against given argumentPattern
     * @param tracer checkpoint. Only traces after this checkpoint will be inspected.
     * @param key tracer key to watch for
     * @param argumentPattern test argument of JIRA.trace call(after key argument)
     */
    public void waitFor(final Tracer tracer, final String key, @Nullable final Pattern argumentPattern)
    {
        log.debug("Waiting for key " + key + " from position " + tracer.position + wrapArgumentPattern(argumentPattern));
        waitUntilTrue(condition(tracer, key, argumentPattern));
    }

    public boolean exists(final Tracer tracer, final String key)
    {
        return exists(tracer, key, null);
    }

    /**
     * Check whether trace record of given key exists, also test argument if argumentPattern is provided
     * @param tracer checkpoint. Only traces after this checkpoint will be inspected.
     * @param key tracer key to watch for
     * @param argumentPattern test argument of JIRA.trace call(after key argument)
     * @return
     */
    public boolean exists(final Tracer tracer, final String key, @Nullable final Pattern argumentPattern)
    {
        log.debug("Checking for key " + key + " from position " + tracer.position + wrapArgumentPattern(argumentPattern));
        retrieveTraces();
        for (int i = tracer.position; i < allTraces.size(); ++i)
        {
            final TraceEntry traceEntry = allTraces.get(i);
            if (traceEntry.id.equals(key))
            {
                // argument pattern must match if provided
                if (argumentPattern != null)
                {
                    final Object argument = traceEntry.args.get(0);

                    // not a String, so we can't test it
                    if (!(argument instanceof String))
                    {
                        continue;
                    }

                    if (!argumentPattern.matcher((String) argument).matches())
                    {
                        continue;
                    }
                }

                log.debug("Matched tracer key " + key + " at position " + i);
                return true;
            }
        }
        return false;
    }

    private String wrapArgumentPattern(final Pattern argumentPattern)
    {
        return (argumentPattern != null ? (" with argument pattern: " + argumentPattern.toString()) : "");
    }

    public List<Map<String, Object>> getArguments(final Tracer tracer, final String key)
    {
        log.debug("Retrieving arguments for key " + key + " from position " + tracer.position);
        retrieveTraces();
        for (int i = tracer.position; i < allTraces.size(); ++i)
        {
            if (allTraces.get(i).id.equals(key))
            {
                log.debug("Returning arguments for tracer key " + key + " at position " + i);
                return allTraces.get(i).args;
            }
        }
        return Collections.emptyList();
    }

    /**
     * Waits for the occurrence of a trace with the given key after the given tracer.
     * @param tracer checkpoint. Only traces after this checkpoint will be inspected.
     * @param key tracer key to watch for
     */
    public TimedCondition condition(final Tracer tracer, final String key)
    {
        return condition(tracer, key, null);
    }


    /**
     * Waits for the occurrence of a trace with the given key and optionally arguments after the given tracer.
     * @param tracer checkpoint. Only traces after this checkpoint will be inspected.
     * @param key tracer key to watch for
     * @param argumentPattern check consecutive arguments of JIRA.trace call(after key argument)
     */
    public TimedCondition condition(final Tracer tracer, final String key, final Pattern argumentPattern)
    {
        final long defTimeout = timeouts.timeoutFor(TimeoutType.AJAX_ACTION);
        final long interval = timeouts.timeoutFor(TimeoutType.EVALUATION_INTERVAL);
        return new AbstractTimedCondition(defTimeout, interval)
        {
            @Override
            public String toString()
            {
                return "Trace for key '" + key + "' " + super.toString() + wrapArgumentPattern(argumentPattern);
            }

            @Override
            protected Boolean currentValue()
            {
                return exists(tracer, key, argumentPattern);
            }
        };
    }

    private void retrieveTraces()
    {
        final int sizeBefore = allTraces.size();
        List<Map<String, Object>> traces = (List<Map<String, Object>>) executor.executeScript("return JIRA.trace.drain();");
        allTraces.addAll(transform(traces, TraceEntry.FROM_MAP));
        for (int i = sizeBefore; i < allTraces.size(); ++i)
        {
            log.debug("Retrieved trace entry " + allTraces.get(i).id + " at position " + i);
        }
    }

    private static class TraceEntry
    {
        static final Function<Map<String, Object>, TraceEntry> FROM_MAP = new Function<Map<String, Object>, TraceEntry>()
        {
            @Override
            public TraceEntry apply(@Nullable Map<String, Object> traceEntry)
            {
                return new TraceEntry(traceEntry);
            }
        };

        final String id;
        final Object timestamp;
        final List args;

        TraceEntry(Map<String, Object> entry)
        {
            this.id = (String) entry.get("id");
            this.timestamp = entry.get("ts");
            this.args = (List) entry.get("args");
        }
    }
}
