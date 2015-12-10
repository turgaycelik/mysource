package com.atlassian.jira.service.services.cluster;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.cluster.Node;
import com.atlassian.jira.cluster.NodeStateManager;
import com.atlassian.jira.cluster.NotClusteredException;
import com.atlassian.jira.service.AbstractService;

import com.opensymphony.module.propertyset.PropertySet;

import static com.atlassian.jira.cluster.Node.NodeState.ACTIVE;
import static com.atlassian.jira.cluster.Node.NodeState.PASSIVE;

/**
 * Scheduled task that checks the state of this node in the cluster.
 * <p>
 * If the node's state in the database is changed to {@code PASSIVE}, it is interpreted
 * as a request for the node to passivate itself.
 * </p>
 *
 * @since v6.2
 */
public class NodeStateCheckerService extends AbstractService
{
    private final NodeStateManager nodeStateManager;

    public NodeStateCheckerService(final NodeStateManager nodeStateManager)
    {
        this.nodeStateManager = nodeStateManager;
    }

    @Override
    public void init(final PropertySet properties) throws ObjectConfigurationException
    {
        super.init(properties);
    }

    @Override
    public void run()
    {
        final Node node = nodeStateManager.getNode();
        if (node.isClustered() && node.getState() == ACTIVE)
        {
            final Node refreshedNode = nodeStateManager.getNodeWithRefresh();
            if (refreshedNode.getState() == PASSIVE)
            {
                passivate();
            }
        }
    }

    private void passivate()
    {
        try
        {
            nodeStateManager.deactivate();
        }
        catch (final NotClusteredException e)
        {
            throw new IllegalStateException("We got a NotClusteredException trying to passivate in response to a " +
                    "clustered node state change, which is something we only check when we are clustered?!");
        }
    }

    @Override
    public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
    {
        return getObjectConfiguration("NODESTATECHECKERSERVICE",
                "services/com/atlassian/jira/service/services/cluster/nodestatecheckerservice.xml", null);
    }
}
