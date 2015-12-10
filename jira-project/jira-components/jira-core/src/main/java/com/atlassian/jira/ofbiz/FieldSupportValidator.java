package com.atlassian.jira.ofbiz;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionBuilder;

import com.google.common.collect.MapMaker;

import static com.atlassian.jira.util.Functions.toGoogleFunction;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * This class is used to validate that a field is supported for a specific use case.
 * <p>
 * The actual plumbing is provided by the fieldResolver function, which maps the table.field
 * to whether it is supported or not. This function is called once per table.field and then
 * the result is cached.
 */
class FieldSupportValidator
{
    // computing map will populate our check automatically and atomically from the supplied function
    private final Map<DatabaseField, Support> map;
    private final String name;

    /**
     * Main constructor. Pass in the name,
     * @param name a descriptive name used in the exception messages.
     * @param unsupportedTypes a set of types that are not supported
     * @param fieldResolver a function that maps from the table name  to a Function that
     *        maps from the field name to the type. The type is then checked to maked sure
     *        it is not in the unsupportedTypes set, if it is then an
     *        {@link UnsupportedTypeException} is thrown. If the table or field is not
     *        found then return a null and a {@link MissingTableException} or
     *        {@link MissingFieldException} is thrown. The inputs to
     *        the function will never be null.
     */
    FieldSupportValidator(final String name, final Collection<String> unsupportedTypes, final Function<String, Function<String, String>> fieldResolver)
    {
        this(name, new Resolver(unsupportedTypes, fieldResolver));
    }

    FieldSupportValidator(final String name, final Resolver supportResolver)
    {
        map = new MapMaker().makeComputingMap(toGoogleFunction(supportResolver));
        this.name = name;
    }

    /**
     * Check all the fields.
     *
     * @param table the name of the table.
     * @param fields all the fields to check.
     * @throws UnsupportedTypeException if the field type is in the unsupportedTypes set.
     * @throws MissingTableException if the table is not found.
     * @throws MissingFieldException if a field is not found.
     */
    void checkAll(final String table, final Iterable<String> fields)
    {
        for (final String field : fields)
        {
            check(table, field);
        }
    }

    /**
     * Check the fields.
     *
     * @param table the name of the table.
     * @param field the field to check.
     * @throws UnsupportedTypeException if the field type is in the unsupportedTypes set.
     * @throws MissingTableException if the table is not found.
     * @throws MissingFieldException if the field is not found.
     */
    void check(final String table, final String field)
    {
        final DatabaseField key = new DatabaseField(table, field);
        map.get(key).check(name, key);
    }

    enum Support
    {
        SUPPORTED
        {
            @Override
            void check(final String name, final DatabaseField field)
            {}
        },
        UNSUPPORTED_TYPE
        {
            @Override
            void check(final String name, final DatabaseField field)
            {
                throw new UnsupportedTypeException(name, field);
            }
        },
        MISSING_TABLE
        {
            @Override
            void check(final String name, final DatabaseField field)
            {
                throw new MissingTableException(name, field);
            }
        },
        MISSING_FIELD
        {
            @Override
            void check(final String name, final DatabaseField field)
            {

                throw new MissingFieldException(name, field);
            }
        };

        abstract void check(String name, DatabaseField field);
    }

    /**
     * Adapter that takes a Function that resolves a table name and
     * returns a function that resolves a field name to a field type,
     * then works out if that is a supported type.
     */
    static class Resolver implements Function<DatabaseField, Support>
    {
        private final Set<String> unsupportedTypes;
        private final Function<String, Function<String, String>> fieldTypeResolver;

        public Resolver(final Collection<String> unsupportedTypes, final Function<String, Function<String, String>> fieldTypeResolver)
        {
            this.unsupportedTypes = CollectionBuilder.<String> newBuilder().addAll(unsupportedTypes).asSet();
            this.fieldTypeResolver = notNull("fieldTypeResolver", fieldTypeResolver);
        }

        public Support get(@Nonnull final DatabaseField input)
        {
            final Function<String, String> entity = fieldTypeResolver.get(input.table());
            if (entity == null)
            {
                return Support.MISSING_TABLE;
            }
            final String fieldType = entity.get(input.field());
            if (fieldType == null)
            {
                return Support.MISSING_FIELD;
            }
            if (unsupportedTypes.contains(fieldType))
            {
                return Support.UNSUPPORTED_TYPE;
            }
            return Support.SUPPORTED;
        }
    }

}

final class UnsupportedTypeException extends RuntimeException
{
    public UnsupportedTypeException(final String name, final DatabaseField field)
    {
        super(name + ": unsupported type for field: " + field);
    }
}

final class MissingTableException extends RuntimeException
{
    public MissingTableException(final String name, final DatabaseField field)
    {
        super(name + ": cannot find table: " + field);
    }
}

final class MissingFieldException extends RuntimeException
{
    public MissingFieldException(final String name, final DatabaseField field)
    {
        super(name + ": cannot find field: " + field);
    }
}

final class DatabaseField
{
    private final String table;
    private final String field;
    // only ever used as a hashed map key, might as well compute up front
    private final int hash;

    DatabaseField(final String table, final String field)
    {
        this.table = notNull("table", table);
        this.field = notNull("field", field);
        hash = computeHash();
    }

    @Override
    public int hashCode()
    {
        return hash;
    }

    private int computeHash()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + field.hashCode();
        result = prime * result + table.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        final DatabaseField other = (DatabaseField) obj;
        return table.equals(other.table) && field.equals(other.field);
    }

    @Override
    public String toString()
    {
        return table + "." + field;
    }

    String table()
    {
        return table;
    }

    String field()
    {
        return field;
    }
}
