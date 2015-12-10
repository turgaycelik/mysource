package com.atlassian.jira.rest.v2.admin.auditing;

import com.atlassian.jira.auditing.ChangedValue;
import com.google.common.base.Function;
import org.codehaus.jackson.annotate.JsonAutoDetect;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 *
 * @since v6.3
 */
@SuppressWarnings("unused")
@JsonAutoDetect
public class ChangedValueBean
{
    private String fieldName;
    private String changedFrom;
    private String changedTo;

    public ChangedValueBean()
    {
    }

    public ChangedValueBean(final ChangedValue changedValue)
    {
        fieldName = changedValue.getName();
        changedFrom = changedValue.getFrom();
        changedTo = changedValue.getTo();
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public String getChangedFrom()
    {
        return changedFrom;
    }

    public String getChangedTo()
    {
        return changedTo;
    }

    public ChangedValue toChangedValue()
    {
        notBlank("fieldName", fieldName);

        return new ChangedValue() {
            @Nonnull
            @Override
            public String getName() {
                return fieldName;
            }

            @Nullable
            @Override
            public String getFrom() {
                return changedFrom;
            }

            @Nullable
            @Override
            public String getTo() {
                return changedTo;
            }
        };
    }

    public static Function<ChangedValueBean, ChangedValue> mapToChangedValue()
    {
        return new Function<ChangedValueBean, ChangedValue>()
        {
            @Override
            public ChangedValue apply(@Nullable ChangedValueBean input) {
                return input != null ? input.toChangedValue() : null;
            }
        };
    }
}
