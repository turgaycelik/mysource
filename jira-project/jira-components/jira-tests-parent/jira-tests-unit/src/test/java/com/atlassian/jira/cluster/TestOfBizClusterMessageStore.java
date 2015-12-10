package com.atlassian.jira.cluster;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.atlassian.jira.cluster.OfBizClusterMessageStore.CLAIMED_BY_NODE;
import static com.atlassian.jira.cluster.OfBizClusterMessageStore.DESTINATION_NODE;
import static com.atlassian.jira.cluster.OfBizClusterMessageStore.ID;
import static com.atlassian.jira.cluster.OfBizClusterMessageStore.MESSAGE;
import static com.atlassian.jira.cluster.OfBizClusterMessageStore.MESSAGE_TIME;
import static com.atlassian.jira.cluster.OfBizClusterMessageStore.SOURCE_NODE;
import static java.util.Collections.EMPTY_LIST;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;



/**
 * Unit test of OfBizClusterMessageStore.
 */
public class TestOfBizClusterMessageStore
{
    // Fixture
    private OfBizClusterMessageStore messageStore;
    private Node node0 = Node.NOT_CLUSTERED;
    private Node node1 = new Node("node1", Node.NodeState.ACTIVE);
    private Node node2 = new Node("node2", Node.NodeState.ACTIVE);
    private Node node3 = new Node("node3", Node.NodeState.ACTIVE);
    @Mock private NodeStateManager mockNodeStateManager;
    private OfBizDelegator mockOfBizDelegator;
    private long firstRowMillis;

    @Before
    public void setUp() throws Exception
    {
        mockOfBizDelegator = new MockOfBizDelegator();
        
        MockitoAnnotations.initMocks(this);
        when(mockNodeStateManager.getNode()).thenReturn(node1);
        messageStore = new OfBizClusterMessageStore(mockOfBizDelegator);
    }

    @Test
    public void testGetMessagesEmpty() throws Exception
    {
        // Invoke and check
        assertEquals(EMPTY_LIST, messageStore.getMessages(node1, node0, null));
    }
    
    @Test
    public void testGetMessagesAll() throws Exception
    {
        loadData();

        // Invoke and check
        final List<ClusterMessage> messages = messageStore.getMessages(node1, node2, null);
        assertEquals(8, messages.size());
    }

    @Test
    public void testGetAndClaim() throws Exception
    {
        loadData();

        // Invoke and check
        List<ClusterMessage> messages = messageStore.getMessages(node1, node2, null);
        assertEquals(8, messages.size());

        // Invoke and check
        messages = messageStore.getMessages(node2, node3, null);
        // Should get the 1 All and the 1 ANY sent from node2
        assertEquals(2, messages.size());

        // Invoke and check
        messages = messageStore.getMessages(node2, node1, null);
        // Should get the 1 All and the 2 specific
        assertEquals(3, messages.size());
    }

    @Test
    public void testAfter() throws Exception
    {
        loadData();

        // Invoke and check
        List<ClusterMessage> messages = messageStore.getMessages(node1, node2, 0L);
        assertEquals(8, messages.size());

        // Invoke and check
        messages = messageStore.getMessages(node1, node2, 1L);
        // Should get 4, 6, 7, 10
        assertEquals(4, messages.size());

        // Invoke and check
        messages = messageStore.getMessages(node1, node2, 5L);
        // Should get 6, 7, 10
        assertEquals(3, messages.size());
    }

    private void loadData()
    {
        firstRowMillis = new Date().getTime();
        
        int r = 0;
        addRow(r = 1,  node1.getNodeId(), node2.getNodeId(), null, "Message " + r, new Timestamp(firstRowMillis + r * 1000));
        addRow(r = 2,  node2.getNodeId(), node1.getNodeId(), null, "Message " + r, new Timestamp(firstRowMillis + r * 1000));
        addRow(r = 3,  node2.getNodeId(), node1.getNodeId(), null, "Message " + r, new Timestamp(firstRowMillis + r * 1000));
        addRow(r = 4,  node1.getNodeId(), ClusterManager.ALL_NODES, null, "Message " + r, new Timestamp(firstRowMillis + r * 1000));
        addRow(r = 5,  node2.getNodeId(), ClusterManager.ALL_NODES, null, "Message " + r, new Timestamp(firstRowMillis + r * 1000));
        addRow(r = 6,  node1.getNodeId(), ClusterManager.ALL_NODES, null, "Message " + r, new Timestamp(firstRowMillis + r * 1000));
        addRow(r = 7,  node1.getNodeId(), node2.getNodeId(), null, "Message " + r, new Timestamp(firstRowMillis + r * 1000));
        addRow(r = 8,  node1.getNodeId(), ClusterManager.ANY_NODE, null, "Message " + r, new Timestamp(firstRowMillis + r * 1000));
        addRow(r = 9,  node1.getNodeId(), ClusterManager.ANY_NODE, null, "Message " + r, new Timestamp(firstRowMillis + r * 1000));
        addRow(r = 10, node1.getNodeId(), node2.getNodeId(), null, "Message " + r, new Timestamp(firstRowMillis + r * 1000));
        addRow(r = 11, node2.getNodeId(), ClusterManager.ANY_NODE, null, "Message " + r, new Timestamp(firstRowMillis + r * 1000));
        addRow(r = 12, node1.getNodeId(), ClusterManager.ANY_NODE, null, "Message " + r, new Timestamp(firstRowMillis + r * 1000));
    }

    private void addRow(final long id, final String source, final String dest, final String claim, final String message, final Timestamp timestamp)
    {
        final FieldMap fields = FieldMap.build(ID, id, SOURCE_NODE, source, DESTINATION_NODE, dest, CLAIMED_BY_NODE, claim, MESSAGE, message);
        fields.add(MESSAGE_TIME, timestamp);
        mockOfBizDelegator.createValue(OfBizClusterMessageStore.ENTITY, fields);
    }


}
