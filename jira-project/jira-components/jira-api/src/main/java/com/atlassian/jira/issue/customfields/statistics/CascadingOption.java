package com.atlassian.jira.issue.customfields.statistics;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.util.lang.Pair;

/**
 * Stores the pair of Option values associated with a Cascading Select value for a single issue.
 * Used in calculating statistics for cascading selects.
 *
 * @since v6.1
 */
@Internal
public class CascadingOption
{
    private Pair<Option, Option> options;

    public CascadingOption (Option parent, Option child)
    {
        this.options = Pair.nicePairOf(parent, child);
    }

    public Option getParent()
    {
        return this.options.first();
    }

    public Option getChild()
    {
        return this.options.second();
    }

}
