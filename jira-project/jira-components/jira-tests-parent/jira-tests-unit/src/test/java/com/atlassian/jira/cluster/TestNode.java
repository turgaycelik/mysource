package com.atlassian.jira.cluster;

import org.junit.Test;

import static com.atlassian.jira.cluster.Node.NodeState;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * Unit test of the Node domain type.
 */
@SuppressWarnings("ResultOfObjectAllocationIgnored")  // Constructor tests
public class TestNode
{
    // Constants
    private static final String NODE_ID = "someID";

    @Test
    public void testClusteredNodeShouldReportStatePassedToConstructor()
    {
        for (final NodeState nodeState : NodeState.values())
        {
            assertValid(nodeState);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDisallowNullNodeId()
    {
        new Node(null, NodeState.ACTIVE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDisallowBlankNodeId()
    {
        new Node("  ", NodeState.ACTIVE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDisallowNullState()
    {
        new Node(NODE_ID, null);
    }

    @Test
    public void testNotClustered()
    {
        final Node node = Node.NOT_CLUSTERED;
        assertThat("clustered", node.isClustered(), is(false));
        assertThat("nodeId", node.getNodeId(), nullValue());
        assertThat("state", node.getState(), sameInstance(NodeState.ACTIVE));
    }

    private static void assertValid(NodeState state)
    {
        final Node node = new Node(NODE_ID, state);
        assertThat("clustered", node.isClustered(), is(true));
        assertThat("nodeId", node.getNodeId(), is(NODE_ID));
        assertThat("state", node.getState(), sameInstance(state));
    }
}
