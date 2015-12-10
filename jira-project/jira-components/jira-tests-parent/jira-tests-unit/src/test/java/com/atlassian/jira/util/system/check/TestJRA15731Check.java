package com.atlassian.jira.util.system.check;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.ofbiz.OfBizConnectionFactory;

import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test case for {@link JRA15731Check}.
 *
 * @since v4.4
 */
public class TestJRA15731Check
{

    @Mock
    private OfBizConnectionFactory mockFactory;

    @Mock
    private Connection mockConnection;

    @Mock
    private DatabaseMetaData mockMetaData;

    @Before
    public void initMocks() throws Exception
    {
        EasyMockAnnotations.initMocks(this);
        expect(mockFactory.getConnection()).andReturn(mockConnection);
        replay(mockFactory);
    }

    @Test
    public void shouldDetermineMySql() throws Exception
    {
        expect(mockConnection.getMetaData()).andReturn(mockMetaData);
        mockConnection.close(); // make sure connection is always closed!
        expectLastCall();
        expect(mockMetaData.getDatabaseProductName()).andReturn("MySQL");
        replay(mockConnection, mockMetaData);

        JRA15731Check tested = new JRA15731Check(mockFactory);
        assertTrue(tested.isMySQL());
        verify(mockConnection, mockMetaData);
    }


    @Test
    public void shouldDetermineNotMySqlIfConnectionThrowsSqlException() throws Exception
    {
        expect(mockConnection.getMetaData()).andThrow(new SQLException());
        mockConnection.close(); // make sure connection is always closed!
        expectLastCall();
        replay(mockConnection, mockMetaData); // no calls to meta data

        JRA15731Check tested = new JRA15731Check(mockFactory);
        assertFalse(tested.isMySQL());
        verify(mockConnection, mockMetaData);
    }

    @Test
    public void shouldDetermineNotMySqlIfMetaDataThrowsSqlException() throws Exception
    {
        expect(mockConnection.getMetaData()).andReturn(mockMetaData);
        mockConnection.close(); // make sure connection is always closed!
        expectLastCall();
        expect(mockMetaData.getDatabaseProductName()).andThrow(new SQLException());
        replay(mockConnection, mockMetaData);

        JRA15731Check tested = new JRA15731Check(mockFactory);
        assertFalse(tested.isMySQL());
        verify(mockConnection, mockMetaData);
    }


    @Test
    public void shouldDetermineNotMySqlIfConnectionThrowsRuntimeException() throws Exception
    {
        expect(mockConnection.getMetaData()).andThrow(new RuntimeException());
        mockConnection.close(); // make sure connection is always closed!
        expectLastCall();
        replay(mockConnection, mockMetaData); // no calls to meta data

        try
        {
            new JRA15731Check(mockFactory).getWarningMessage();
            fail("Expected runtime exception");
        }
        catch(RuntimeException ok)
        {
        }
        verify(mockConnection, mockMetaData);
    }


    @Test
    public void shouldHandleExceptionsFromCloseGracefully() throws Exception
    {
        expect(mockConnection.getMetaData()).andThrow(new SQLException());
        mockConnection.close(); // make sure connection is always closed!
        expectLastCall().andThrow(new SQLException());
        replay(mockConnection, mockMetaData); // no calls to meta data

        JRA15731Check tested = new JRA15731Check(mockFactory);
        assertFalse(tested.isMySQL());
        verify(mockConnection, mockMetaData);
    }
}
