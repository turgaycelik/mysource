package com.atlassian.jira.workflow;

import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.config.Configuration;

/**
 * @since v5.1
 */
public class DefaultWorkflowsRepository implements WorkflowsRepository
{
    private final Configuration configuration;

    public DefaultWorkflowsRepository(Configuration configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public boolean contains(String name)
    {
        // This is more efficient than the parent method as it does not create workflow objects
        // but simply looks at workflow names

        if (name == null)
        {
            throw new IllegalArgumentException("Name must not be null.");
        }

        // Cannot get access to a Map that stores the workflows - so need to loop over the
        // name array.
        try
        {
            for (int i = 0; i < getConfiguration().getWorkflowNames().length; i++)
            {
                String workflowName = getConfiguration().getWorkflowNames()[i];
                if (name.equals(workflowName))
                {
                    return true;
                }
            }
        }
        catch (FactoryException e)
        {
            throw new WorkflowException(e);
        }
        return false;
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }
}
