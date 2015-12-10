package com.atlassian.jira.auditing.handlers;

import com.atlassian.fugue.Option;
import com.atlassian.jira.auditing.ChangedValue;
import com.atlassian.jira.auditing.RecordRequest;
import com.google.common.base.Function;

import java.util.List;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;

/**
 * @since v6.2
 */
public class HandlerUtils
{
    public static Option<RecordRequest> requestIfThereAreAnyValues(final List<ChangedValue> changedValues, final Function<List<ChangedValue>, RecordRequest> function)
    {
        if (changedValues.isEmpty())
        {
            return none(); //ignoring this update since nothing important has changed
        }
        else
        {
            return option(function.apply(changedValues));
        }
    }
}
