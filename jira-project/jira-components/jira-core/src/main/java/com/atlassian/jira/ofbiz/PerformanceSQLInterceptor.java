package com.atlassian.jira.ofbiz;

import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.util.lang.Pair;
import org.ofbiz.core.entity.jdbc.interceptors.connection.ConnectionPoolState;
import org.ofbiz.core.entity.jdbc.interceptors.connection.SQLConnectionInterceptor;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A SQL interceptor that stores all SQL call invocations as well as timing information in a thread local cache.
 *
 * @since v4.4
 */
public class PerformanceSQLInterceptor implements SQLConnectionInterceptor
{
    public static final String SQL_PERF_CACHE = "sql.perf.cache";

    private long startTime;

    @Override
    public void onConnectionTaken(Connection connection, ConnectionPoolState connectionPoolState)
    {
    }

    @Override
    public void onConnectionReplaced(Connection connection, ConnectionPoolState connectionPoolState)
    {
    }

    @Override
    public void beforeExecution(String sql, List<String> params, Statement statement)
    {
       startTime = System.currentTimeMillis();
    }

    @Override
    public void afterSuccessfulExecution(String sql, List<String> params, Statement statement, ResultSet resultSet, int i)
    {
        getCache().recordTime(OfBizLogHelper.formatSQL(sql, params), sql, System.currentTimeMillis() - startTime);
    }

    @Override
    public void onException(String sql, List<String> params, Statement statement, SQLException e)
    {
        getCache().recordTime(OfBizLogHelper.formatSQL(sql, params), sql, System.currentTimeMillis() - startTime);
    }

    private SQLPerfCache getCache()
    {
        final SQLPerfCache sqlPerfCache = (SQLPerfCache) JiraAuthenticationContextImpl.getRequestCache().get(SQL_PERF_CACHE);
        if (sqlPerfCache == null)
        {
            final SQLPerfCache perfCache = new SQLPerfCache();
            JiraAuthenticationContextImpl.getRequestCache().put(SQL_PERF_CACHE, perfCache);
            return perfCache;
        }
        return sqlPerfCache;
    }

    public static class SQLPerfCache implements Serializable
    {
        private static final int MAX_STATEMENTS = 500;

        transient long totalTime = 0;
        transient final Map<String, List<Long>> timePerStatement = new HashMap<String, List<Long>>();
        transient final List<Pair<String, String>> inCallOrder = new ArrayList<Pair<String, String>>();

        public void recordTime(String sqlWithParams, String rawSql, final long timeTaken)
        {
            //looks like we're doing a really DB intense operation like indexing, data import etc.  Probably
            //not worth it to record more that 500 statements. Cache will grow too big and kinda pointless for the
            // user to read through!
            int numOfStatements = inCallOrder.size();
            if (numOfStatements >= MAX_STATEMENTS)
            {
                if (numOfStatements == MAX_STATEMENTS)
                {
                    inCallOrder.add(Pair.of("More than 500 statements. Skipping remaining statements...", ""));
                }
                return;
            }

            totalTime += timeTaken;
            if (!timePerStatement.containsKey(rawSql))
            {
                timePerStatement.put(rawSql, new ArrayList<Long>());
            }
            timePerStatement.get(rawSql).add(timeTaken);

            inCallOrder.add(Pair.of(sqlWithParams, OfBizLogHelper.logTheCallStack()));
        }

        public long getTotalTimeMs()
        {
            return totalTime;
        }

        public long getNumStatements()
        {
            return inCallOrder.size();
        }

        public Map<String, List<Long>> getStatements()
        {
            ValueComparator vc = new ValueComparator(timePerStatement);
            final Map<String, List<Long>> result = new TreeMap<String, List<Long>>(vc);
            result.putAll(timePerStatement);
            return result;
        }

        public List<Pair<String, String>> getStatementsInCallOrder()
        {
            return inCallOrder;
        }

        /**
         * Sorts the list of SQL calls such that the statements
         * with the highest total invocation time come first!
         */
        static class ValueComparator implements Comparator<String>
        {
            private final Map<String, List<Long>> base;

            ValueComparator(final Map<String, List<Long>> base)
            {
                this.base = base;
            }

            @Override
            public int compare(String key, String key2)
            {
                final List<Long> timings1 = base.get(key);
                final List<Long> timings2 = base.get(key2);

                long totalTiming1 = 0;
                long totalTiming2 = 0;
                for (Long timing : timings1)
                {
                    totalTiming1 += timing;
                }
                for (Long timing : timings2)
                {
                    totalTiming2 += timing;
                }

                if (totalTiming1 < totalTiming2)
                {
                    return 1;
                }
                else if (totalTiming1 == totalTiming2)
                {
                    if (timings1.size() <= timings2.size())
                    {
                        return 1;
                    }
                    else
                    {
                        return -1;
                    }
                }
                else
                {
                    return -1;
                }
            }
        }
    }
}
