/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 23, 2004
 * Time: 4:22:25 PM
 */
package com.atlassian.jira.workflow;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.loader.XMLWorkflowFactory;
import org.apache.log4j.Logger;

import java.util.Map;

public class JiraWorkflowFactory extends XMLWorkflowFactory
{
    private static final Logger log = Logger.getLogger(JiraWorkflowFactory.class);

    public void initDone() throws FactoryException
    {
        // let the XML workflow factory setup from workflows.xml first
        super.initDone();

        //the workflowDescriptorStore is used to load everything else.
    }

    public WorkflowDescriptor getWorkflow(String name) throws FactoryException
    {
        if (getWorkflowsFromParent().containsKey(name))
        {
            return super.getWorkflow(name);
        }

        return getWorkflowDescriptorStore().getWorkflow(name);
    }

    public String[] getWorkflowNames()
    {
        String[] osworkflowNames = super.getWorkflowNames();
        String[] workflowNames = getWorkflowDescriptorStore().getWorkflowNames();
        String[] retNames = new String[osworkflowNames.length + workflowNames.length];
        System.arraycopy(osworkflowNames, 0, retNames, 0, osworkflowNames.length);
        System.arraycopy(workflowNames, 0, retNames, osworkflowNames.length, workflowNames.length);

        return retNames;
    }

    public boolean saveWorkflow(String name, WorkflowDescriptor descriptor, boolean replace) throws FactoryException
    {
        if (name == null)
        {
            return false;
        }
        if (getWorkflowsFromParent().get(name) != null)
        {
            return false;
        }

        return getWorkflowDescriptorStore().saveWorkflow(name, descriptor, replace);
    }

    public boolean removeWorkflow(String name) throws FactoryException
    {
        return getWorkflowDescriptorStore().removeWorkflow(name);
    }

    public boolean isModifiable(String name)
    {
        if(name == null)
        {
            throw new IllegalArgumentException("The workflow name cannot be null");
        }
        // See if the workflow is loaded from XML
        if (getWorkflowsFromParent().containsKey(name))
        {
            return false;
        }

        final ImmutableWorkflowDescriptor workflow;
        try
        {
            workflow = getWorkflowDescriptorStore().getWorkflow(name);
        }
        catch (FactoryException e)
        {
            //this should never happen.
            log.error("Unexpected error constructing workflow", e);
            return false;
        }

        if (workflow != null)
        {
            return true;
        }

        throw new IllegalArgumentException("The workflow: '" + name + "' does not exist.");
    }

    WorkflowDescriptorStore getWorkflowDescriptorStore()
    {
        return ComponentAccessor.getComponentOfType(WorkflowDescriptorStore.class);
    }

    Map getWorkflowsFromParent()
    {
        return workflows;
    }
}