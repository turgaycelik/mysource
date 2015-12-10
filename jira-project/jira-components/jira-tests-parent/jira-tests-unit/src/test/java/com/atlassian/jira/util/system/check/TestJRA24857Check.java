package com.atlassian.jira.util.system.check;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import com.atlassian.jira.ofbiz.OfBizConnectionFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelReader;
import org.ofbiz.core.entity.model.ModelViewEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v4.4.1
 */
public class TestJRA24857Check
{
    @Test
    public void testIsMySQL() throws SQLException
    {
        DelegatorInterface delegatorInterface = mock(DelegatorInterface.class);
        OfBizConnectionFactory factory = mock(OfBizConnectionFactory.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);

        when(factory.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("MySQL", "SomethingElse", null).thenThrow(new SQLException());

        JRA24857Check check = new JRA24857Check(factory, delegatorInterface);

        assertTrue(check.isMySQL());
        assertFalse(check.isMySQL());
        assertFalse(check.isMySQL());
        assertFalse(check.isMySQL());

        Mockito.verify(connection, times(4)).close();
    }

    @Test
    public void testGetJiraTables() throws Exception
    {
        DelegatorInterface delegatorInterface = mock(DelegatorInterface.class);
        OfBizConnectionFactory factory = mock(OfBizConnectionFactory.class);
        ModelReader reader = mock(ModelReader.class);

        when(delegatorInterface.getModelReader()).thenReturn(reader);
        when(reader.getEntityNames()).thenReturn(Lists.<String>newArrayList("one", "two", "three"));

        when(reader.getModelEntity("one")).thenReturn(tableEntity("one"));
        when(reader.getModelEntity("two")).thenReturn(tableEntity("two"));
        ModelViewEntity three = viewEntity("three");
        when(reader.getModelEntity("three")).thenReturn(three);

        JRA24857Check check = new JRA24857Check(factory, delegatorInterface);
        assertEquals(Sets.<String>newHashSet("one", "two"), check.getJiraTables());
    }

    @Test
    public void testGetJiraTablesExecption() throws Exception
    {
        DelegatorInterface delegatorInterface = mock(DelegatorInterface.class);
        OfBizConnectionFactory factory = mock(OfBizConnectionFactory.class);
        ModelReader reader = mock(ModelReader.class);

        when(delegatorInterface.getModelReader()).thenReturn(reader);
        when(reader.getEntityNames()).thenReturn(Lists.<String>newArrayList("one", "two", "three"));

        when(reader.getModelEntity("one")).thenReturn(tableEntity("one"));
        when(reader.getModelEntity("two")).thenReturn(tableEntity("two"));
        when(reader.getModelEntity("three")).thenThrow(new GenericEntityException());

        JRA24857Check check = new JRA24857Check(factory, delegatorInterface);
        assertTrue(check.getJiraTables().isEmpty());
    }

    @Test
    public void testGetMyISAMTables() throws Exception
    {
        DelegatorInterface delegatorInterface = mock(DelegatorInterface.class);
        OfBizConnectionFactory factory = mock(OfBizConnectionFactory.class);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(factory.getConnection()).thenReturn(connection);
        when(connection.getCatalog()).thenReturn("catalog");
        when(connection.prepareStatement(Matchers.<String>any(), anyInt(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, true, true, false);
        when(resultSet.getString(1)).thenReturn("one", "two", "three");

        JRA24857Check check = new JRA24857Check(factory, delegatorInterface);
        assertEquals(Sets.<String>newHashSet("one", "two", "three"), check.getMyISAMTables());

        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    public void testGetMyISAMTablesException() throws Exception
    {
        DelegatorInterface delegatorInterface = mock(DelegatorInterface.class);
        OfBizConnectionFactory factory = mock(OfBizConnectionFactory.class);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(factory.getConnection()).thenReturn(connection);
        when(connection.getCatalog()).thenReturn("catalog");
        when(connection.prepareStatement(Matchers.<String>any(), anyInt(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, true, true, false);
        when(resultSet.getString(1)).thenReturn("one", "two").thenThrow(new SQLException());

        JRA24857Check check = new JRA24857Check(factory, delegatorInterface);

        assertTrue(check.getMyISAMTables().isEmpty());

        verify(connection).prepareStatement(anyString(), eq(ResultSet.TYPE_FORWARD_ONLY), eq(ResultSet.CONCUR_READ_ONLY));
        verify(preparedStatement).setString(1, "catalog");
        verify(preparedStatement).setString(2, "MyISAM");

        verify(resultSet).close();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @Test
    public void testIsSessionUsingMySIAM() throws Exception
    {
        DelegatorInterface delegatorInterface = mock(DelegatorInterface.class);
        OfBizConnectionFactory factory = mock(OfBizConnectionFactory.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(factory.getConnection()).thenReturn(connection);
        when(connection.createStatement(anyInt(), anyInt())).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, true, true, true, false, true);
        when(resultSet.getString(1)).thenReturn("MyISAM", "Myisam", "other", null, "MyISAM").thenThrow(new SQLException());

        JRA24857Check check = new JRA24857Check(factory, delegatorInterface);

        assertTrue(check.isSessionUsingMySIAM());
        assertTrue(check.isSessionUsingMySIAM());
        assertFalse(check.isSessionUsingMySIAM());
        assertFalse(check.isSessionUsingMySIAM());
        assertFalse(check.isSessionUsingMySIAM());

        when(resultSet.getString(1)).thenThrow(new SQLException());
        assertFalse(check.isSessionUsingMySIAM());

        verify(connection, times(6)).createStatement(eq(ResultSet.TYPE_FORWARD_ONLY), eq(ResultSet.CONCUR_READ_ONLY));

        verify(resultSet, times(6)).close();
        verify(statement, times(6)).close();
        verify(connection, times(6)).close();
    }

    @Test
    public void testGetWarningMessageNotMySQL() throws Exception
    {
        DelegatorInterface delegatorInterface = mock(DelegatorInterface.class);
        OfBizConnectionFactory factory = mock(OfBizConnectionFactory.class);

        JRA24857Check check = new JRA24857Check(factory, delegatorInterface)
        {
            @Override
            boolean isMySQL()
            {
                return false;
            }
        };
        
        assertNull(check.getWarningMessage());
    }

    @Test
    public void testGetWarningMessageBadTables() throws Exception
    {
        final I18nMessage something = new I18nMessage("Something");
        DelegatorInterface delegatorInterface = mock(DelegatorInterface.class);
        OfBizConnectionFactory factory = mock(OfBizConnectionFactory.class);

        JRA24857Check check = new JRA24857Check(factory, delegatorInterface)
        {
            @Override
            boolean isMySQL()
            {
                return true;
            }

            @Override
            Set<String> getJiraTables()
            {
                return Sets.newHashSet("one");
            }

            @Override
            Set<String> getMyISAMTables()
            {
                return Sets.newHashSet("one", "two", "three");
            }

            @Override
            public I18nMessage createWarning()
            {
                return something;
            }
        };

        assertSame(something, check.getWarningMessage());
    }

    @Test
    public void testGetWarningMessageBadSession() throws Exception
    {
        final I18nMessage something = new I18nMessage("Something");
        DelegatorInterface delegatorInterface = mock(DelegatorInterface.class);
        OfBizConnectionFactory factory = mock(OfBizConnectionFactory.class);

        JRA24857Check check = new JRA24857Check(factory, delegatorInterface)
        {
            @Override
            boolean isMySQL()
            {
                return true;
            }

            @Override
            Set<String> getJiraTables()
            {
                return Sets.newHashSet("five");
            }

            @Override
            Set<String> getMyISAMTables()
            {
                return Sets.newHashSet("one", "two", "three");
            }

            @Override
            boolean isSessionUsingMySIAM()
            {
                return true;
            }

            @Override
            public I18nMessage createWarning()
            {
                return something;
            }
        };

        assertSame(something, check.getWarningMessage());
    }

    @Test
    public void testGetWarningMessageGoodMySQLConnection() throws Exception
    {
        DelegatorInterface delegatorInterface = mock(DelegatorInterface.class);
        OfBizConnectionFactory factory = mock(OfBizConnectionFactory.class);

        JRA24857Check check = new JRA24857Check(factory, delegatorInterface)
        {
            @Override
            boolean isMySQL()
            {
                return true;
            }

            @Override
            Set<String> getJiraTables()
            {
                return Sets.newHashSet("five");
            }

            @Override
            Set<String> getMyISAMTables()
            {
                return Sets.newHashSet("one", "two", "three");
            }

            @Override
            boolean isSessionUsingMySIAM()
            {
                return false;
            }

            @Override
            public I18nMessage createWarning()
            {
                fail("Should not be calling this.");
                return null;
            }
        };

        assertNull(check.getWarningMessage());
    }

    private static ModelEntity tableEntity(String name)
    {
        ModelEntity modelEntity = new ModelEntity();
        modelEntity.setTableName(name);
        return modelEntity;
    }

    private static ModelViewEntity viewEntity(String name)
    {
        ModelViewEntity entity = Mockito.mock(ModelViewEntity.class);
        when(entity.getPlainTableName()).thenReturn(name);
        return entity;
    }
}
