package com.atlassian.jira.rest.api.util;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.List;

/**
 * Simple bean for holding a field reference and an error key as well as some optional parameters. Note that it is generally
 * preferable to use the {@link ErrorCollection} class instead.
 *
 * @since v4.2
 */
@XmlRootElement
public class ValidationError
{
    // The field the error relates to
    @XmlElement
    private String field;
    // The Error key...
    @XmlElement
    private String error;

    @XmlElement
    private List<String> params;

    @SuppressWarnings({"UnusedDeclaration", "unused"})
    private ValidationError() {}

    public ValidationError(String field, String error)
    {
        this.field = field;
        this.error = error;
    }

    public ValidationError(String field, String error, List<String> params)
    {
        this.field = field;
        this.error = error;
        this.params = params;
    }
    public ValidationError(String field, String error, String param)
    {
        this(field, error, Arrays.asList(param));
    }

    public String getField()
    {
        return field;
    }

    public String getError()
    {
        return error;
    }

    public List<String> getParams()
    {
        return params;
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(final Object o)
    {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
