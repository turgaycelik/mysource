package com.atlassian.jira.config.database;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.jira.util.RuntimeIOException;

import org.junit.Test;
import org.ofbiz.core.entity.config.JdbcDatasourceInfo;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link DatabaseConfigurationManagerImpl}.
 *
 * @since v4.4
 */
public class TestDatabaseConfigurationManagerImpl
{
    @Test
    public void testIsDatabaseSetupGetDatabaseConfigurationFails()
    {
        final AtomicBoolean called = new AtomicBoolean(false);
        DatabaseConfigurationManagerImpl dcmi = new DatabaseConfigurationManagerImpl(null, null, null)
        {
            @Override
            public DatabaseConfig getDatabaseConfiguration()
            {
                called.set(true);
                throw new RuntimeException();
            }
        };
        assertFalse(dcmi.isDatabaseSetup());
        assertTrue(called.get());
    }

    @Test
    public void testIsDatabaseSetupHappy()
    {
        final  DatabaseConfigurationLoader mockDbConfigLoader = createMock(DatabaseConfigurationLoader.class);
        final DatabaseConfig databaseConfig = new DatabaseConfig("fooDb", "skimar", new JndiDatasource("jndi;yo"));
        expect(mockDbConfigLoader.loadDatabaseConfiguration()).andReturn(databaseConfig);
        replay(mockDbConfigLoader);
        DatabaseConfigurationManagerImpl dcmi = new DatabaseConfigurationManagerImpl(null, mockDbConfigLoader, null);
        assertTrue(dcmi.isDatabaseSetup());
        verify(mockDbConfigLoader);
    }

    /**
     * Tests the negative cache of whether the database is setup by first finding the database not setup and then
     * calling again without first calling setDatabaseConfig() which would be the only way to invalidate that cache.
     */
    @Test
    public void testUnconfiguredCache()
    {
        final DatabaseConfig databaseConfig = new DatabaseConfig("fooDb", "skimar", new JndiDatasource("jndi;yo"));
        final AtomicBoolean shouldThrow = new AtomicBoolean(true);
        DatabaseConfigurationLoader mockDatabaseConfigurationLoader = new DatabaseConfigurationLoader()
        {
            @Override
            public boolean configExists()
            {
                return false;
            }

            @Override
            public DatabaseConfig loadDatabaseConfiguration() throws RuntimeException, RuntimeIOException
            {
                if (shouldThrow.get())
                {
                    throw new RuntimeException("indicates config is missing");
                }
                else
                {
                    return databaseConfig;
                }
            }

            @Override
            public void saveDatabaseConfiguration(DatabaseConfig config) throws RuntimeIOException
            {
            }
        };
        DatabaseConfigurationManagerImpl dcmi = new DatabaseConfigurationManagerImpl(null, mockDatabaseConfigurationLoader, null);
        // first time around it's not setup
        assertFalse(dcmi.isDatabaseSetup());
        // now make it so that database config is actually setup, but without calling setDatabaseConfig()
        shouldThrow.set(false);
        // the negative cache should answer here
        assertFalse(dcmi.isDatabaseSetup());
    }

    @Test
    public void testCreateInternalConfig()
    {
        JiraHome jiraHome = createStrictMock(JiraHome.class);
        expect(jiraHome.getHomePath()).andReturn("noPlaceLike/Home").anyTimes();
        replay(jiraHome);
        DatabaseConfigurationManagerImpl dcmi = new DatabaseConfigurationManagerImpl(jiraHome, null, null);
        final DatabaseConfig internalConfig = dcmi.getInternalDatabaseConfiguration();
        assertEquals("defaultDS", internalConfig.getDatasourceName());
        assertEquals("default", internalConfig.getDelegatorName());
        assertEquals("hsql", internalConfig.getDatabaseType());
        assertEquals("PUBLIC", internalConfig.getSchemaName());
        final JdbcDatasourceInfo jdbcDatasource = internalConfig.getDatasourceInfo().getJdbcDatasource();
        assertEquals("sa", jdbcDatasource.getUsername());
        assertEquals("", jdbcDatasource.getPassword());
        assertEquals("jdbc:hsqldb:noPlaceLike/Home/database/jiradb", jdbcDatasource.getUri());
        assertEquals("org.hsqldb.jdbcDriver", jdbcDatasource.getDriverClassName());
        verify(jiraHome);
    }

    @Test
    public void testSetDatabaseConfiguration()
    {
        DatabaseConfigurationLoader mockDcl = createStrictMock(DatabaseConfigurationLoader.class);
        final ComponentLocator mockComponentLocator = createMock(ComponentLocator.class);
        final DatabaseConfig databaseConfig = new DatabaseConfig("fooDb", "skimar", new JndiDatasource("jndi;yo"));
        mockDcl.saveDatabaseConfiguration(databaseConfig);
        expectLastCall();
        replay(mockDcl, mockComponentLocator);

        DatabaseConfigurationManagerImpl dcmi = new DatabaseConfigurationManagerImpl(null, mockDcl, mockComponentLocator);
        dcmi.setDatabaseConfiguration(databaseConfig);
        verify(mockDcl, mockComponentLocator);
    }

    @Test
    public void testGetDatabaseConfiguration()
    {
        DatabaseConfigurationLoader mockDcl = createStrictMock(DatabaseConfigurationLoader.class);
        final ComponentLocator mockComponentLocator = createMock(ComponentLocator.class);
        final DatabaseConfig databaseConfig = new DatabaseConfig("fooDb", "skimar", new JndiDatasource("jndi;yo"));
        expect(mockDcl.loadDatabaseConfiguration()).andReturn(databaseConfig).once();
        replay(mockDcl, mockComponentLocator);

        DatabaseConfigurationManagerImpl dcmi = new DatabaseConfigurationManagerImpl(null, mockDcl, mockComponentLocator);
        final DatabaseConfig returned = dcmi.getDatabaseConfiguration();
        assertEquals(databaseConfig.getDatabaseType(), returned.getDatabaseType());

        // now test cache by calling again and our strict mock should fail on the second call if the cache is broken
        final DatabaseConfig returned2 = dcmi.getDatabaseConfiguration();
        assertEquals(databaseConfig.getDatabaseType(), returned2.getDatabaseType());

        verify(mockDcl, mockComponentLocator);
    }

    @Test
    public void testDoNow()
    {
        DatabaseConfigurationManagerImpl dcmi = new DatabaseConfigurationManagerImpl(null, null, null)
        {
            @Override
            public boolean isDatabaseSetup()
            {
                return true;
            }

            @Override
            void configureOfbiz(DatabaseConfig databaseConfig)
            {
                // do nothing, we are just erasing the behaviour that links out to static ofbiz land
            }

            @Override
            public DatabaseConfig getDatabaseConfiguration()
            {
                return null;
            }
        };
        final AtomicBoolean ran = new AtomicBoolean(false);
        Runnable runMe = new Runnable()
        {
            @Override
            public void run()
            {
                ran.set(true);
            }
        };
        LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
        dcmi.doNowOrEnqueue("queueDesc", queue, runMe, "taskDesc");
        assertTrue(ran.get());
        assertTrue(queue.isEmpty());
    }


    @Test
    public void testEnqueue()
    {
        DatabaseConfigurationManagerImpl dcmi = new DatabaseConfigurationManagerImpl(null, null, null)
        {
            @Override
            public boolean isDatabaseSetup()
            {
                return false;
            }
        };
        final AtomicBoolean ran = new AtomicBoolean(false);
        Runnable runMe = new Runnable()
        {
            @Override
            public void run()
            {
                ran.set(true);
            }
        };
        LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
        dcmi.doNowOrEnqueue("queueDesc", queue, runMe, "taskDesc");
        assertFalse(ran.get());
        assertFalse(queue.isEmpty());
    }

}
