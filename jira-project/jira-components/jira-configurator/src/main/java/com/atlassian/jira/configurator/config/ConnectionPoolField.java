package com.atlassian.jira.configurator.config;

import org.ofbiz.core.entity.config.ConnectionPoolInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Contains descriptions for the various connection pool settings.
 * These are paraphrased from the DBCP documentation.
 *
 * @since v5.1
 */
public enum ConnectionPoolField
{
    // Note on the special values for the length hint, here:
    //   0 means use a checkbox, so it is used for all Boolean fields
    //   11 is String.valueOf(Integer.MIN_VALUE).length(), so it is used for all Integer fields
    //   20 is String.valueOf(Long.MIN_VALUE).length(), so it is used for all Long fields
    // I would have used constants for these, but enum initialization is special
    // and they can not reference their own static fields in their constructors.

    // Scalability and Performance settings
    MAX_SIZE("Maximum Size", "Maximum active database connections to allow.", Settings.DEFAULT_POOL_MAX_SIZE),
    MAX_IDLE(11, "Maximum Idle", "Maximum idle database connections to keep around.", "uses Maximum Size"),
    MIN_SIZE(11, "Minimum Idle/Size", "Minimum idle database connections to keep around.", "uses Maximum Size"),
    INITIAL_SIZE("Initial Size", "Initial number of database connections to establish at start-up.", 0),
    MAX_WAIT("Maximum Wait Time", "Maximum time in milliseconds to wait for a connection before giving up.", Settings.DEFAULT_POOL_MAX_WAIT),
    POOL_STATEMENTS("Pool Statements", "Whether to pool compiled SQL statements.", false),
    MAX_OPEN_STATEMENTS(11, "Maximum Open Statements", "Maximum number of compiled SQL statements to pool, if statement pooling is enabled.", "0 (unlimited)"),

    // Eviction Policy settings
    VALIDATION_QUERY(40, "Validation Query", "SQL SELECT query that returns at least 1 one row when the connection is usable.", "none for most databases; \"" + Settings.MYSQL_VALIDATION_QUERY + "\" for MySql"),
    VALIDATION_QUERY_TIMEOUT(11, "Validation Query Timeout", "The time in milliseconds that the Validation Query is allowed to take before giving up on it.", "-1 (indefinite)"),
    TEST_ON_BORROW("Test On Borrow", "Whether to test connections with the Validation Query before loaning them out from the pool.", true),
    TEST_ON_RETURN("Test On Return", "Whether to test connections with the Validation Query when they are returned to the pool.", false),
    TEST_WHILE_IDLE(0, "Test While Idle", "Whether to test connections with the Validation Query during eviction testing.", "false for most databases, true for MySQL"),
    TIME_BETWEEN_EVICTION_RUNS(20, "Time Between Eviction Runs", "How frequently in milliseconds to check pooled connections for eviction candidates.", "-1 (never) for most databases; " + Settings.HSQL_TIME_BETWEEN_EVICTION_RUNS_MILLIS + " for MySQL"),
    MIN_EVICTABLE_IDLE_TIME(20, "Minimum Evictable Idle Time", "How long a connection must be idle before it may be evicted.", "-1 (disabled) for most databases; " + Settings.MYSQL_MIN_EVICTABLE_TIME_MILLIS + " for MySQL"),
    REMOVE_ABANDONED("Remove Abandoned", "Whether to check for connections that have been inappropriately abandoned without closing them.", true),
    REMOVE_ABANDONED_TIMEOUT("Remove Abandoned Timeout", "How long in seconds that a connection has to be idle for the Remove Abandoned setting to reclaim it.", 300);

    private static final List<ConnectionPoolField> SCALABILITY_AND_PERFORMANCE_FIELDS = Collections.unmodifiableList(Arrays.asList(
            MAX_SIZE,
            MAX_IDLE,
            MIN_SIZE,
            INITIAL_SIZE,
            MAX_WAIT,
            POOL_STATEMENTS,
            MAX_OPEN_STATEMENTS ));

    private static final List<ConnectionPoolField> EVICTION_POLICY_FIELDS = Collections.unmodifiableList(Arrays.asList(
            VALIDATION_QUERY,
            VALIDATION_QUERY_TIMEOUT,
            TEST_ON_BORROW,
            TEST_ON_RETURN,
            TEST_WHILE_IDLE,
            TIME_BETWEEN_EVICTION_RUNS,
            MIN_EVICTABLE_IDLE_TIME,
            REMOVE_ABANDONED,
            REMOVE_ABANDONED_TIMEOUT ));

    public static List<ConnectionPoolField> getScalabityAndPerformanceFields()
    {
        return SCALABILITY_AND_PERFORMANCE_FIELDS;
    }

    public static List<ConnectionPoolField> getEvictionPolicyFields()
    {
        return EVICTION_POLICY_FIELDS;
    }

    private final int lengthHint;
    private final String label;
    private final String description;
    private final String defaultValue;

    ConnectionPoolField(int lengthHint, String label, String description, String defaultValue)
    {
        this.lengthHint = lengthHint;
        this.label = label;
        this.description = description;
        this.defaultValue = defaultValue;
    }

    ConnectionPoolField(String label, String description, int defaultValue)
    {
        this(11, label, description, String.valueOf(defaultValue));
    }

    ConnectionPoolField(String label, String description, long defaultValue)
    {
        this(20, label, description, String.valueOf(defaultValue));
    }

    ConnectionPoolField(String label, String description, boolean defaultValue)
    {
        this(0, label, description, String.valueOf(defaultValue));
    }

    public int getLengthHint()
    {
        return lengthHint;
    }

    public String label()
    {
        return label;
    }

    public String description()
    {
        return description;
    }

    public String defaultValue()
    {
        return defaultValue;
    }
}
