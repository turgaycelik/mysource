package com.atlassian.jira.cluster.lock;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.database.DatabaseSystemTimeReader;
import com.atlassian.jira.database.DatabaseSystemTimeReaderFactory;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.EntityEngineImpl;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericValue;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * @since 6.3
 */
@RunWith(MockitoJUnitRunner.class)
public class TestJiraClusterNodeHeartBeatDao
{
    private static final String NODE_ID = "theNodeId";

    @Mock
    private ClusterManager mockClusterManager;

    private OfBizDelegator mockOfBizDelegator = new MockOfBizDelegator();
    private EntityEngine mockEntityEngine = new EntityEngineImpl(mockOfBizDelegator);

    @Mock
    private DatabaseSystemTimeReaderFactory mockDbTimeReaderFactory;

    @Mock
    private DatabaseSystemTimeReader mockDbTimeReader;

    private JiraClusterNodeHeartBeatDao dao;

    @Before
    public void setUp()
    {
        when(mockClusterManager.getNodeId()).thenReturn(NODE_ID);
        when(mockClusterManager.isClustered()).thenReturn(true);
        when(mockDbTimeReaderFactory.getReader()).thenReturn(mockDbTimeReader);

        dao = new JiraClusterNodeHeartBeatDao(mockClusterManager, mockEntityEngine, mockDbTimeReaderFactory);
    }

    @Test
    public void testGetNodeIdClustered()
    {
        assertThat(dao.getNodeId(), equalTo(NODE_ID));
    }

    @Test
    public void testGetActiveNodesDatabaseTimeOffsetsNodeAheadOfDb()
    {
        long now = System.currentTimeMillis();

        //Node's system time is one minute ahead of database
        addRow(NODE_ID, now + TimeUnit.MINUTES.toMillis(1), now);

        long activeTime = now - TimeUnit.MINUTES.toMillis(5);
        Map<String, Long> offsetMap = dao.getActiveNodesDatabaseTimeOffsets(activeTime);

        assertEquals("Wrong offset results.",
                ImmutableMap.of(NODE_ID, TimeUnit.MINUTES.toMillis(1)),
                offsetMap);
    }

    @Test
    public void testGetActiveNodesDatabaseTimeOffsetsNodeBehindDb()
    {
        long now = System.currentTimeMillis();

        //Node's system time is two minutes behind database
        addRow(NODE_ID, now - TimeUnit.MINUTES.toMillis(2), now);

        long activeTime = now - TimeUnit.MINUTES.toMillis(5);
        Map<String, Long> offsetMap = dao.getActiveNodesDatabaseTimeOffsets(activeTime);

        assertEquals("Wrong offset results.",
                ImmutableMap.of(NODE_ID, TimeUnit.MINUTES.toMillis(-2)),
                offsetMap);
    }

    @Test
    public void testGetActiveNodesDatabaseTimeOffsetsNodeInactive()
    {
        long now = System.currentTimeMillis();

        //Node's DB time is 6 minutes in past, because it is earlier than active time it should not be returned
        long dbTime = now - TimeUnit.MINUTES.toMillis(6);
        long nodeSystemTime = dbTime;
        addRow(NODE_ID, nodeSystemTime, dbTime);

        long activeTime = now - TimeUnit.MINUTES.toMillis(5);
        Map<String, Long> offsetMap = dao.getActiveNodesDatabaseTimeOffsets(activeTime);

        assertEquals("Wrong offset results.",
                ImmutableMap.of(),
                offsetMap);
    }

    @Test
    public void testWriteHeartbeat()
    throws Exception
    {
        long now = System.currentTimeMillis();
        long dbTime = now;
        long nodeSystemTime = dbTime - TimeUnit.SECONDS.toMillis(30);

        when(mockDbTimeReader.getDatabaseSystemTimeMillis()).thenReturn(dbTime);

        dao.writeHeartBeat(nodeSystemTime);

        List<GenericValue> dbValues = mockOfBizDelegator.findAll(Entity.CLUSTER_NODE_HEARTBEAT.getEntityName());
        assertThat(dbValues.size(), is(1));

        GenericValue row = dbValues.get(0);
        assertThat(row.getString(ClusterNodeHeartbeat.NODE_ID), is(NODE_ID));
        assertThat(row.getLong(ClusterNodeHeartbeat.HEARTBEAT_TIME), is(nodeSystemTime));
        assertThat(row.getLong(ClusterNodeHeartbeat.DATABASE_TIME), is(dbTime));
    }

    private void addRow(String id, long heartbeatTime, long databaseTime)
    {
        final FieldMap fields = FieldMap.build(ClusterNodeHeartbeat.NODE_ID, id, ClusterNodeHeartbeat.HEARTBEAT_TIME, heartbeatTime, ClusterNodeHeartbeat.DATABASE_TIME, databaseTime);
        mockOfBizDelegator.createValue(Entity.CLUSTER_NODE_HEARTBEAT.getEntityName(), fields);
    }
}
