package com.atlassian.jira.bc.issue.visibility;

import javax.annotation.concurrent.Immutable;

import com.atlassian.fugue.Option;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * This class represents an invalid visibility. It's returned by {@link Visibilities}
 * and contains an error message with an information about what was wrong during creation of the visibility.
 *
 * @since v6.4
 */
@Immutable
public final class InvalidVisibility implements Visibility
{
    private final String i18nErrorMessage;
    private final Option<String> param;

    InvalidVisibility(final String i18nErrorMessage)
    {
        this.i18nErrorMessage = i18nErrorMessage;
        param = Option.none();
    }

    InvalidVisibility(final String i18nErrorMessage, final String param)
    {
        this.i18nErrorMessage = i18nErrorMessage;
        this.param = Option.some(param);
    }

    public String getI18nErrorMessage()
    {
        return i18nErrorMessage;
    }

    public Option<String> getParam()
    {
        return param;
    }

    @Override
    public <T> T accept(final VisibilityVisitor<T> visitor)
    {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof InvalidVisibility))
        {
            return false;
        }
        InvalidVisibility rhs = (InvalidVisibility) obj;
        return new EqualsBuilder()
                .append(getI18nErrorMessage(), rhs.getI18nErrorMessage())
                .append(getParam(), rhs.getParam())
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
                .append(getI18nErrorMessage())
                .append(getParam())
                .toHashCode();
    }
}
