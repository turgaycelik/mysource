package com.atlassian.query.order;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

import java.io.Serializable;

import com.atlassian.fugue.Effect;
import com.atlassian.fugue.Option;
import com.atlassian.query.clause.Property;

/**
 * A simple data bean representing a portion of the sort order (related to a clause) for a search query.
 *
 * Together via the {@link com.atlassian.query.order.OrderBy} these will determine the sorting order of the results
 * returned by a {@link com.atlassian.query.Query}.
 *
 * @since 4.0
 */
public class SearchSort implements Serializable
{
    private final SortOrder order;
    private final String field;
    private final Option<Property> property;

    public SearchSort(String field)
    {
        this(field, (SortOrder)null);
    }

    /**
     * Used to construct a search sort for a field with a direction.
     *
     * @param field to sort by.
     * @param order direction to sort by, if null the default order for the field will be used.
     */
    public SearchSort(String field, SortOrder order)
    {
        this(field, Option.<Property>none(),order);
    }

    /**
     * Used to construct a search sort for a field with a direction and optional property
     * @param field to sort by.
     * @param property property associated with sort filed this should be taken into consideration when constructing sort field
     * @param order direction to sort by, if null the default order for the field will be used.
     */
    public SearchSort(String field, Option<Property> property,SortOrder order)
    {
        this.field = notBlank("field", field);
        this.order = order;
        this.property = property;
    }

    /**
     * Used to construct a search sort for a field with a direction.
     *
     * NOTE: it would be better if the order of these parameters was reversed but we are leaving it for backward compatibility.
     * @deprecated use {@link #SearchSort(String, SortOrder)} instead.
     *
     * @param order the order of the sort, if null, will be the default order for the system, if not one of
     * {@link com.atlassian.query.order.SortOrder#ASC} or {@link com.atlassian.query.order.SortOrder#DESC} it will
     * default to {@link com.atlassian.query.order.SortOrder#ASC}.
     * @param field to sort by.
     */
    public SearchSort(String order, String field)
    {
        this(field, Option.<Property>none(), SortOrder.parseString(order));
    }

    public SearchSort(SearchSort copy)
    {
        notNull("copy", copy);
        this.field = copy.field;
        this.order = copy.order;
        this.property = copy.property;
    }

    public String getOrder()
    {
        return (order == null) ? null : order.name();
    }

    public SortOrder getSortOrder()
    {
        return order;
    }

    public String getField()
    {
        return field;
    }

    public Option<Property> getProperty()
    {
        return property;
    }

    public boolean isReverse()
    {
        return order == SortOrder.DESC;
    }

    ///CLOVER:OFF
    public String toString()
    {
        final StringBuilder toString = new StringBuilder(field);
        property.foreach(new Effect<Property>()
        {
            @Override
            public void apply(final Property property)
            {
                toString.append(property.toString());
            }
        });
        if (order != null)
        {
            toString.append(" ").append(order);
        }

        return toString.toString();

    }
    ///CLOVER:ON

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final SearchSort that = (SearchSort) o;

        if (!field.equals(that.field))
        {
            return false;
        }
        if (order != that.order)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = order != null ? order.hashCode() : 0;
        result = 31 * result + field.hashCode();
        return result;
    }
}
