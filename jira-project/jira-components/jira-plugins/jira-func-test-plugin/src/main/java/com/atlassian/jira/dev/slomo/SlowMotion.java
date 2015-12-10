package com.atlassian.jira.dev.slomo;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import com.atlassian.jira.config.properties.JiraProperties;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;

/**
 * @since v5.2
 */
class SlowMotion
{
    private static final Pattern SELF = Pattern.compile("/rest/func-test/.*/slomo");

    private static final Logger log = Logger.getLogger(SlowMotionFilter.class);
    private static final String PROPERTY_KEY_ATLASSIAN_SLOMO = "atlassian.slomo";
    private static final int DEFAULT_DELAY = 2000;

    private final AtomicInteger id = new AtomicInteger();
    private final JiraProperties jiraSystemProperties;
    private volatile List<PatternDelay> delays = Collections.emptyList();
    private volatile int defaultDelay = -1;

    SlowMotion(final JiraProperties jiraSystemProperties)
    {
        this.jiraSystemProperties = jiraSystemProperties;
    }

    void setDefaultDelay(int defaultDelay)
    {
        this.defaultDelay = defaultDelay;
        jiraSystemProperties.setProperty(PROPERTY_KEY_ATLASSIAN_SLOMO, Integer.toString(defaultDelay));
    }

    int getDefaultDelay()
    {
        if (defaultDelay < 0)
        {
            final String prop = jiraSystemProperties.getProperty(PROPERTY_KEY_ATLASSIAN_SLOMO);
            if (prop != null)
            {
                try
                {
                    defaultDelay = Math.max(0, parseInt(prop));
                }
                catch (NumberFormatException ignore)
                {
                    defaultDelay = DEFAULT_DELAY;
                    log.debug(format("Slowing everything down to %s because I couldn't parse %s as a number", DEFAULT_DELAY, prop));
                }
            }
            else
            {
                defaultDelay = 0;
            }
        }
        return defaultDelay;
    }

    PatternDelay create(PatternDelay input)
    {
        PatternDelay patternDelay = new PatternDelay(id.incrementAndGet(), input.getPattern(),
                input.getDelay(), input.isEnabled());

        List<PatternDelay> patternDelays = newArrayList(delays);
        patternDelays.add(patternDelay);
        setDelays(patternDelays);
        return patternDelay;
    }

    PatternDelay update(PatternDelay update)
    {
        List<PatternDelay> safeList = newArrayList(delays);
        for (ListIterator<PatternDelay> delayIterator = safeList.listIterator(); delayIterator.hasNext(); )
        {
            PatternDelay next = delayIterator.next();
            if (next.getId() == update.getId())
            {
                delayIterator.set(update);
                setDelays(safeList);
                return update;
            }
        }
        return null;
    }

    boolean delete(int id)
    {
        List<PatternDelay> safeList = newArrayList(delays);
        for (ListIterator<PatternDelay> delayIterator = safeList.listIterator(); delayIterator.hasNext(); )
        {
            PatternDelay next = delayIterator.next();
            if (next.getId() == id)
            {

                delayIterator.remove();
                setDelays(safeList);
                return true;
            }
        }
        return false;
    }

    private void setDelays(List<PatternDelay> patternDelays)
    {
        this.delays = unmodifiableList(patternDelays);
    }

    int getSlowDown(HttpServletRequest request)
    {
        if (SELF.matcher(request.getRequestURI()).find())
        {
            return 0;
        }

        for (PatternDelay motion : delays)
        {
            if (motion.isEnabled() && motion.matches(request))
            {
                return motion.getDelay();
            }
        }

        return getDefaultDelay();
    }

    PatternDelay get(int id)
    {
        for (PatternDelay delay : delays)
        {
            if (id == delay.getId())
            {
                return delay;
            }
        }
        return null;
    }

    Iterable<PatternDelay> getDelays()
    {
        return delays;
    }
}
