package com.atlassian.jira.issue.customfields.manager;

import com.atlassian.jira.issue.customfields.option.SimpleOption;
import com.google.common.collect.ImmutableList;

import java.util.List;
import javax.annotation.Nonnull;

/**
 * @since v6.1
 */
class ImmutableSimpleOption implements SimpleOption<ImmutableSimpleOption>
{
    private final Long id;
    private final String value;
    private final List<ImmutableSimpleOption> children;

    ImmutableSimpleOption(SimpleOption<?> option)
    {
        this.id = option.getOptionId();
        this.value = option.getValue();

        final ImmutableList.Builder<ImmutableSimpleOption> builder = ImmutableList.builder();
        for (SimpleOption<?> child : option.getChildOptions())
        {
            builder.add(new ImmutableSimpleOption(child));
        }
        this.children = builder.build();
    }

    @Override
    public Long getOptionId()
    {
        return id;
    }

    @Override
    public String getValue()
    {
        return value;
    }

    @Nonnull
    @Override
    public List<ImmutableSimpleOption> getChildOptions()
    {
        return children;
    }
}
