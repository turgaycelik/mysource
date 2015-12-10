package com.atlassian.jira.workflow;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;

/**
 * @since v6.0
 */
public class WorkflowSchemes
{
    //No you don't.
    private WorkflowSchemes() {}

    public static <T extends WorkflowScheme> Function<T, String> nameFunction()
    {
        return new Function<T, String>()
        {
            @Override
            public String apply(T input)
            {
                return input.getName();
            }
        };
    }

    public static <T extends WorkflowScheme> Ordering<T> nameOrdering()
    {
        return Ordering.from(String.CASE_INSENSITIVE_ORDER)
                .onResultOf(WorkflowSchemes.<T>nameFunction());
    }
}
