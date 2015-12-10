package com.atlassian.jira;

import static com.atlassian.jira.util.dbc.Assertions.not;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.dbc.Assertions.stateTrue;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.util.collect.CollectionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.concurrent.Immutable;

/**
 * Each data type can specify a collection of actual java types, represented via their {@link Class}, that this type is.
 *
 * @since v4.0
 */
@Immutable
@PublicApi
public final class JiraDataTypeImpl implements JiraDataType
{
    private final Collection<Class<?>> types;
    private final Collection<String> stringTypes;

    public JiraDataTypeImpl(final Class<?> type)
    {
        this(Collections.singleton(type));
    }

    public JiraDataTypeImpl(final Collection<? extends Class<?>> types)
    {
        notNull("types", types);
        not("types", types.isEmpty());
        this.types = CollectionUtil.copyAsImmutableList(types);
        final Collection<String> strTypes = new ArrayList<String>();
        for (final Class<?> type : types)
        {
            strTypes.add(type.getName());
        }
        stringTypes = Collections.unmodifiableCollection(strTypes);
    }

    public Collection<String> asStrings()
    {
        return stringTypes;
    }

    public boolean matches(final JiraDataType otherType)
    {
        notNull("otherType", otherType);
        stateTrue("otherType", otherType instanceof JiraDataTypeImpl);
        final JiraDataTypeImpl other = (JiraDataTypeImpl) otherType;
        if (types.contains(Object.class) || other.types.contains(Object.class))
        {
            return true;
        }
        else
        {
            for (final Class<?> type : types)
            {
                if (other.types.contains(type))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The actual java type's, represented via their {@link Class}, that this type is.
     * This is not provided on the interface, if you need it you must cast the object to this implementation type.
     *
     * @return the {@link Class}'s that this data type represents.
     */
    public Collection<Class<?>> getTypes()
    {
        return types;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final JiraDataTypeImpl that = (JiraDataTypeImpl) o;

        if (!types.equals(that.types))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return types.hashCode();
    }
}
