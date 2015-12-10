package com.atlassian.jira.dev.slomo;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * @since v5.2
 */
class PatternDelay
{
    private final int id;
    private final Pattern pattern;
    private final int timeout;
    private final boolean enabled;

    PatternDelay(int id, Pattern pattern, int timeout, boolean enabled)
    {
        this.id = id;
        this.pattern = pattern;
        this.timeout = timeout;
        this.enabled = enabled;
    }

    PatternDelay(Pattern pattern, int timeout, boolean enabled)
    {
        this(-1, pattern, timeout, enabled);
    }

    boolean matches(HttpServletRequest request)
    {
        return pattern.matcher(request.getRequestURI()).find();
    }

    boolean isEnabled()
    {
        return enabled;
    }

    Pattern getPattern()
    {
        return pattern;
    }

    int getDelay()
    {
        return timeout;
    }

    int getId()
    {
        return id;
    }
}
