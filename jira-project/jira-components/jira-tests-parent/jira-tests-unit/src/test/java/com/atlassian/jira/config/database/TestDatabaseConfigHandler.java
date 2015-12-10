package com.atlassian.jira.config.database;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.config.ConnectionPoolInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestDatabaseConfigHandler
{
    private DatabaseConfigHandler handler;

    @Before
    public void setUp()
    {
        handler = new DatabaseConfigHandler();
    }

    @After
    public void tearDown()
    {
        handler = null;
    }

    @Test
    public void testJndi() throws Exception
    {
        DatabaseConfig config = parseConfig("jndi");
        assertJndiConfig(config);
        DatabaseConfig newConfig = writeParseCycle(config);
        assertJndiConfig(newConfig);
    }

    @Test
    public void testJdbc() throws Exception
    {
        DatabaseConfig config = parseConfig("jdbc");
        assertJdbcCommonConfig(config);
        assertJdbcNoExtraPoolConfig(config);
        DatabaseConfig newConfig = writeParseCycle(config);
        assertJdbcCommonConfig(newConfig);
        assertJdbcNoExtraPoolConfig(newConfig);
    }

    @Test
    public void testJdbcExtra() throws Exception
    {
        DatabaseConfig config = parseConfig("jdbc-extra");
        assertJdbcCommonConfig(config);
        assertJdbcExtraPoolConfig(config);
        assertConnectionProperties(config);
        DatabaseConfig newConfig = writeParseCycle(config);
        assertJdbcCommonConfig(newConfig);
        assertJdbcExtraPoolConfig(newConfig);
    }

    @Test
    public void testJdbcEvenMoreExtra() throws Exception
    {
        DatabaseConfig config = parseConfig("jdbc-even-more-extra");
        assertJdbcCommonConfig(config);
        assertJdbcEvenMoreExtraPoolConfig(config);
        assertConnectionProperties(config);
        DatabaseConfig newConfig = writeParseCycle(config);
        assertJdbcCommonConfig(newConfig);
        assertJdbcEvenMoreExtraPoolConfig(newConfig);
    }



    private void assertJndiConfig(DatabaseConfig config)
    {
        assertEquals("badass-tenant", config.getDatasourceName());
        assertEquals("badass-tenant", config.getDelegatorName());
        assertEquals("mongodb", config.getDatabaseType());
        assertEquals("does mongo have schemas?", config.getSchemaName());
        assertNotNull(config.getDatasource());
        assertTrue(config.getDatasource() instanceof JndiDatasource);
        JndiDatasource jndi = (JndiDatasource) config.getDatasource();
        assertEquals("java:comp/env/jdbc/mongodb", jndi.getJndiName());
    }

    private void assertConnectionProperties(DatabaseConfig config)
    {
        JdbcDatasource jdbc = (JdbcDatasource) config.getDatasource();
        final Properties connectionProperties = jdbc.getConnectionProperties();
        // <connection-properties>foo=bar;equation=x=y</connection-properties>
        assertEquals(2, connectionProperties.size());
        assertEquals("bar", connectionProperties.get("foo"));
        assertEquals("x=y", connectionProperties.get("equation"));
    }

    private void assertJdbcCommonConfig(DatabaseConfig config)
    {
        assertEquals("badass-tenant", config.getDatasourceName());
        assertEquals("badass-tenant", config.getDelegatorName());
        assertEquals("mongodb", config.getDatabaseType());
        assertEquals("does mongo have schemas?", config.getSchemaName());
        assertNotNull(config.getDatasource());
        assertTrue(config.getDatasource() instanceof JdbcDatasource);
        JdbcDatasource jdbc = (JdbcDatasource) config.getDatasource();
        assertEquals("jdbc:mongodb:localhost:1234", jdbc.getJdbcUrl());
        assertEquals("org.hsqldb.jdbcDriver", jdbc.getDriverClassName());
        assertEquals("badass-user", jdbc.getUsername());
        assertEquals("badass-password", jdbc.getPassword());
    }

    private void assertJdbcNoExtraPoolConfig(DatabaseConfig config)
    {
        JdbcDatasource jdbc = (JdbcDatasource) config.getDatasource();
        // no pool setting implies default of 8
        final ConnectionPoolInfo poolInfo = jdbc.getConnectionPoolInfo();
        assertEquals(ConnectionPoolInfo.DEFAULT_POOL_MIN_SIZE, poolInfo.getMinSize());
        assertEquals(ConnectionPoolInfo.DEFAULT_POOL_MAX_SIZE, poolInfo.getMaxSize());
        assertEquals(ConnectionPoolInfo.DEFAULT_POOL_MAX_WAIT, poolInfo.getMaxWait());
        assertEquals(ConnectionPoolInfo.DEFAULT_POOL_SLEEP_TIME, poolInfo.getSleepTime());
        assertEquals(ConnectionPoolInfo.DEFAULT_POOL_LIFE_TIME, poolInfo.getLifeTime());
        assertEquals(ConnectionPoolInfo.DEFAULT_DEADLOCK_MAX_WAIT, poolInfo.getDeadLockMaxWait());
        assertEquals(ConnectionPoolInfo.DEFAULT_DEADLOCK_RETRY_WAIT, poolInfo.getDeadLockRetryWait());
        assertEquals(null, poolInfo.getValidationQuery());
        assertEquals(null, poolInfo.getMinEvictableTimeMillis());
        assertEquals(null, poolInfo.getTimeBetweenEvictionRunsMillis());

        // Deprecated methods
        assertEquals(ConnectionPoolInfo.DEFAULT_POOL_MAX_SIZE, jdbc.getPoolSize());
        assertEquals(null, jdbc.getValidationQuery());
        assertEquals(null, jdbc.getMinEvictableTimeMillis());
        assertEquals(null, jdbc.getTimeBetweenEvictionRunsMillis());
    }

    private void assertJdbcExtraPoolConfig(DatabaseConfig config)
    {
        JdbcDatasource jdbc = (JdbcDatasource) config.getDatasource();
        final ConnectionPoolInfo poolInfo = jdbc.getConnectionPoolInfo();
        assertEquals(ConnectionPoolInfo.DEFAULT_POOL_MIN_SIZE, poolInfo.getMinSize());
        assertEquals(50000, poolInfo.getMaxSize());
        assertEquals(ConnectionPoolInfo.DEFAULT_POOL_MAX_WAIT, poolInfo.getMaxWait());
        assertEquals(ConnectionPoolInfo.DEFAULT_POOL_SLEEP_TIME, poolInfo.getSleepTime());
        assertEquals(ConnectionPoolInfo.DEFAULT_POOL_LIFE_TIME, poolInfo.getLifeTime());
        assertEquals(ConnectionPoolInfo.DEFAULT_DEADLOCK_MAX_WAIT, poolInfo.getDeadLockMaxWait());
        assertEquals(ConnectionPoolInfo.DEFAULT_DEADLOCK_RETRY_WAIT, poolInfo.getDeadLockRetryWait());
        assertEquals("SELECT 'X' FROM DUAL", poolInfo.getValidationQuery());
        assertEquals(999, poolInfo.getMinEvictableTimeMillis().longValue());
        assertEquals(8888, poolInfo.getTimeBetweenEvictionRunsMillis().longValue());

        // Deprecated methods
        assertEquals(50000, jdbc.getPoolSize());
        assertEquals("SELECT 'X' FROM DUAL", jdbc.getValidationQuery());
        assertEquals(999, jdbc.getMinEvictableTimeMillis().longValue());
        assertEquals(8888, jdbc.getTimeBetweenEvictionRunsMillis().longValue());
    }

    private void assertJdbcEvenMoreExtraPoolConfig(DatabaseConfig config)
    {
        JdbcDatasource jdbc = (JdbcDatasource) config.getDatasource();
        final ConnectionPoolInfo poolInfo = jdbc.getConnectionPoolInfo();
        assertEquals(10, poolInfo.getMinSize());
        assertEquals(50000, poolInfo.getMaxSize());
        assertEquals(110000L, poolInfo.getMaxWait());
        assertEquals(130000L, poolInfo.getSleepTime());
        assertEquals(140000L, poolInfo.getLifeTime());
        assertEquals(150000L,  poolInfo.getDeadLockMaxWait());
        assertEquals(160000L, poolInfo.getDeadLockRetryWait());
        assertEquals("SELECT 'X' FROM DUAL", poolInfo.getValidationQuery());
        assertEquals(999, poolInfo.getMinEvictableTimeMillis().longValue());
        assertEquals(8888, poolInfo.getTimeBetweenEvictionRunsMillis().longValue());

        // Deprecated methods
        assertEquals(50000, jdbc.getPoolSize());
        assertEquals("SELECT 'X' FROM DUAL", jdbc.getValidationQuery());
        assertEquals(999, jdbc.getMinEvictableTimeMillis().longValue());
        assertEquals(8888, jdbc.getTimeBetweenEvictionRunsMillis().longValue());
    }


    /**
     * Do a write parse cycle, validates XML writing without us having to run assertions on XML
     */
    private DatabaseConfig writeParseCycle(DatabaseConfig config) throws Exception
    {
        Document document = DocumentHelper.createDocument();
        Element database = document.addElement("database");
        handler.writeTo(database, config);
        CharArrayWriter writer = new CharArrayWriter();
        new XMLWriter(writer).write(document);
        return parseConfig(new CharArrayReader(writer.toCharArray()));
    }

    private DatabaseConfig parseConfig(String filename) throws Exception
    {
        InputStream is = getClass().getResourceAsStream(getClass().getSimpleName() + "-" + filename + ".xml");
        try
        {
            return parseConfig(new InputStreamReader(is));
        }
        finally
        {
            IOUtils.closeQuietly(is);
        }
    }

    private DatabaseConfig parseConfig(Reader reader) throws Exception
    {
        SAXReader xmlReader = new SAXReader();
        Document document = xmlReader.read(reader);
        return handler.parse(document.getRootElement());
    }
}
