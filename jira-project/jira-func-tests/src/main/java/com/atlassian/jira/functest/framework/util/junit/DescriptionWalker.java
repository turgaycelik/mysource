package com.atlassian.jira.functest.framework.util.junit;

import com.atlassian.jira.util.Consumer;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.junit.runner.Description;

/**
 * Walks the JUnit4 description tree invoking callback on each element.
 *
 * @since v4.4
 */
public final class DescriptionWalker
{
    private DescriptionWalker()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static void walk(Consumer<Description> callback, Description... roots)
    {
        walk(callback, Predicates.<Description>alwaysTrue(), roots);
    }

    public static void walk(Consumer<Description> callback, Predicate<Description> filter, Description... roots)
    {
        for (Description root : roots)
        {
            if (filter.apply(root))
            {
                callback.consume(root);
            }
            for (Description child : root.getChildren())
            {
                walk(callback, filter, child);
            }
        }
    }
}
