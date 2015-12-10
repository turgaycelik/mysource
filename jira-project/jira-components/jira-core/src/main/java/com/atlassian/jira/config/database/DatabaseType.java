package com.atlassian.jira.config.database;

import com.atlassian.jira.config.database.jdbcurlparser.JdbcUrlParser;
import com.atlassian.jira.config.database.jdbcurlparser.MySqlUrlParser;
import com.atlassian.jira.config.database.jdbcurlparser.OracleUrlParser;
import com.atlassian.jira.config.database.jdbcurlparser.PostgresUrlParser;
import com.atlassian.jira.config.database.jdbcurlparser.SqlServerUrlParser;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;

public enum DatabaseType
{
    HSQL("HSQL", "hsql", "unknown", null, ImmutableList.of("org.hsqldb.jdbcDriver")),
    SQL_SERVER("SQL Server", "mssql", "Database", new SqlServerUrlParser(), ImmutableList.of("net.sourceforge.jtds.jdbc.Driver",
            // SQL Server with Microsoft JDBC drivers. Supported for manual setting but not used by default.
            "com.microsoft.jdbc.sqlserver.SQLServerDriver")),
    MY_SQL("MySQL", "mysql", "Database", new MySqlUrlParser(), ImmutableList.of("com.mysql.jdbc.Driver")),
    ORACLE("Oracle", "oracle10g", "SID", new OracleUrlParser(), ImmutableList.of("oracle.jdbc.OracleDriver")),
    POSTGRES("PostgreSQL", "postgres72", "Database", new PostgresUrlParser(), ImmutableList.of("org.postgresql.Driver")),
    UKNOWN("Uknown", "unknown", "unknown", null, ImmutableList.<String>of());

    private final String name;
    private final String typeName;
    private final JdbcUrlParser jdbcUrlParser;
    private final ImmutableList<String> drivers;
    private final String instanceFieldName;

    DatabaseType(final String name, final String typeName, final String instanceFieldName,
            final JdbcUrlParser jdbcUrlParser, final ImmutableList<String> drivers)
    {
        this.name = name;
        this.typeName = typeName;
        this.instanceFieldName = instanceFieldName;
        this.jdbcUrlParser = jdbcUrlParser;
        this.drivers = drivers;
    }

    public String toString()
    {
        return getDisplayName();
    }

    public String getDisplayName()
    {
        return name;
    }

    public String getTypeName()
    {
        return typeName;
    }

    public String getJdbcDriverClassName()
    {
        if (drivers.isEmpty())
        {
            throw new RuntimeException("No Driver class name defined for " + name());
        }
        return drivers.get(0);
    }

    public static Iterable<DatabaseType> knownTypes()
    {
        return Iterables.filter(allTypes(), not(equalTo(UKNOWN)));
    }

    public static Iterable<DatabaseType> allTypes()
    {
        return ImmutableList.copyOf(values());
    }

    public static DatabaseType forJdbcDriverClassName(final String jdbcDriverClass)
    {
        if (jdbcDriverClass == null)
        {
            return DatabaseType.HSQL;
        }
        return Iterables.tryFind(knownTypes(), new Predicate<DatabaseType>()
        {
            @Override
            public boolean apply(final DatabaseType databaseType)
            {
                return databaseType.drivers.contains(jdbcDriverClass);
            }
        }).or(new Supplier<DatabaseType>()
        {
            @Override
            public DatabaseType get()
            {
                throw new IllegalArgumentException("Unknown JDBC Driver Class " + jdbcDriverClass);
            }
        });
    }

    public JdbcUrlParser getJdbcUrlParser()
    {
        if (jdbcUrlParser == null)
        {
            throw new UnsupportedOperationException("Parser implementation not available for " + name());
        }
        return jdbcUrlParser;
    }

    public String getInstanceFieldName()
    {
        return instanceFieldName;
    }
}
