package com.atlassian.jira.issue.customfields.option;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
* @since v6.1
*/
public class MockSimpleOption implements SimpleOption<MockSimpleOption>
{
    private static final Function<SimpleOption<?>, MockSimpleOption> TO_MOCK = new Function<SimpleOption<?>, MockSimpleOption>()
    {
        @Override
        public MockSimpleOption apply(final SimpleOption<?> input)
        {
            return new MockSimpleOption(input);
        }
    };

    public static Function<SimpleOption<?>, MockSimpleOption> toMock()
    {
        return TO_MOCK;
    }

    private Long id;
    private String value;
    private List<MockSimpleOption> options = Lists.newArrayList();
    private MockSimpleOption parent;

    public MockSimpleOption()
    {
    }

    public MockSimpleOption(SimpleOption<?> option)
    {
        this.id = option.getOptionId();
        this.value = option.getValue();
        for (SimpleOption<?> child : option.getChildOptions())
        {
            final MockSimpleOption childMock = new MockSimpleOption(child);
            childMock.parent = this;
            this.options.add(childMock);
        }
    }

    @Nullable
    @Override
    public Long getOptionId()
    {
        return id;
    }

    public MockSimpleOption setId(final Long id)
    {
        this.id = id;
        return this;
    }

    @Override
    public String getValue()
    {
        return value;
    }

    public MockSimpleOption setValue(final String value)
    {
        this.value = value;
        return this;
    }

    @Nonnull
    @Override
    public List<MockSimpleOption> getChildOptions()
    {
        return options;
    }

    public MockSimpleOption parent()
    {
        return parent;
    }

    public MockSimpleOption parent(final MockSimpleOption parent)
    {
        this.parent = parent;
        return this;
    }

    public MockSimpleOption addChild(String value)
    {
        final MockSimpleOption mockSimpleOption = new MockSimpleOption().setValue(value).parent(this);
        options.add(mockSimpleOption);
        return mockSimpleOption;
    }

    @Override
    public String toString()
    {
        return String.format("Option[%s, id: %d, children: %s]", getValue(), getOptionId(), getChildOptions());
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final MockSimpleOption that = (MockSimpleOption) o;

        if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
        if (options != null ? !options.equals(that.options) : that.options != null) { return false; }
        if (value != null ? !value.equals(that.value) : that.value != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (options != null ? options.hashCode() : 0);
        return result;
    }
}
