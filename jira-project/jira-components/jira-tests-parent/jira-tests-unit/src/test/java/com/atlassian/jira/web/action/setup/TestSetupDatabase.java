package com.atlassian.jira.web.action.setup;

import com.atlassian.jira.config.database.DatabaseType;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import java.util.Map;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static org.junit.Assert.assertEquals;

public class TestSetupDatabase
{

    @Test
    public void testListOfDatabasesIncludesAllKnownDatabases() throws Exception
    {
        final Map<String, DatabaseType> sut = SetupDatabase.databaseTypeMap;

        final Iterable<DatabaseType> knownTypesExceptHsql = filter(DatabaseType.knownTypes(), new Predicate<DatabaseType>()
        {
            @Override
            public boolean apply(final DatabaseType input)
            {
                return input != DatabaseType.HSQL;
            }
        });

        assertEquals(ImmutableSet.copyOf(transform(
                knownTypesExceptHsql, new Function<DatabaseType, String>()
                {
                    @Override
                    public String apply(final DatabaseType input)
                    {
                        return input.getTypeName();
                    }
                }
        )), sut.keySet());

        for (DatabaseType databaseType : knownTypesExceptHsql)
        {
            assertEquals(databaseType, sut.get(databaseType.getTypeName()));
        }
    }
}