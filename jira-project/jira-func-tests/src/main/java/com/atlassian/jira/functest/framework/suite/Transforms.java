package com.atlassian.jira.functest.framework.suite;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Sorter;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Transforms based on JUnit4 sorters and filters.
 *
 * @since v4.4
 */
public final class Transforms
{

    private Transforms()
    {
        throw new AssertionError("Don't instantiate me!");
    }

    public static SuiteTransform fromSorter(final Sorter sorter)
    {
        return new SuiteTransform()
        {
            @Override
            public Iterable<Description> apply(@Nullable Iterable<Description> descriptions)
            {
                final List<Description> answer = Lists.newArrayList(descriptions);
                Collections.sort(answer, new Comparator<Description>()
                {
                    @Override
                    public int compare(Description first, Description second)
                    {
                        return sorter.compare(first, second);
                    }
                });
                return answer;
            }
        };
    }

    public static SuiteTransform fromFilter(final Filter filter)
    {
        return new SuiteTransform()
        {
            @Override
            public Iterable<Description> apply(@Nullable Iterable<Description> descriptions)
            {
                return Iterables.filter(descriptions, new Predicate<Description>()
                {
                    @Override
                    public boolean apply(Description input)
                    {
                        return filter.shouldRun(input);
                    }
                });
            }
        };
    }

}
