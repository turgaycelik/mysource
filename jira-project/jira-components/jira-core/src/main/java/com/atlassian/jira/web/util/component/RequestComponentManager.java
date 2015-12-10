/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.util.component;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.jira.config.component.PicoContainerFactory;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;

import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.PicoContainer;

import webwork.action.ServletActionContext;
import webwork.action.factory.SessionMap;

public class RequestComponentManager
{
    private static final Logger log = Logger.getLogger(RequestComponentManager.class);

    /**
     * Get a container that contains the 'request' level information.  This container should only be used on a
     * per-request basis (ie destroyed after the request).  Any attempt to use this container after the
     * end of the request will result in indeterminate behaviour.
     * <p>
     * So far - the following components are registered:
     * <ul>
     * <li>{@link SessionMap}
     * </ul>
     *
     * @return A Pico container that contains the 'request' level information.
     */
    public MutablePicoContainer getContainer(PicoContainer parent)
    {
        //register any request level components that the webwork actions needs to use.
        try
        {
            HttpServletRequest request = ServletActionContext.getRequest();

            if (request != null)
            {
                return injectWorkflow(parent, request);
            }
        }
        catch (PicoCompositionException e)
        {
            log.error(e, e);
            if (e.getCause() != null)
            {
                log.error("Cause: " + e.getCause(), e.getCause());
            }
            throw e;
        }
        catch (Exception e)
        {
            // We need to catch this exception, as there are a lot of times
            log.error(e, e);

            //do nothing
        }
        return PicoContainerFactory.defaultJIRAContainer(parent);
    }

    MutablePicoContainer injectWorkflow(PicoContainer parent, HttpServletRequest request)
    {
        MutablePicoContainer requestContainer = PicoContainerFactory.defaultJIRAContainer(parent);
        // Extract the workflow name from the request parameters
        String workflowName = request.getParameter("workflowName");
        if (StringUtils.isEmpty(workflowName))
        {
            // nothing to do
            return requestContainer;
        }
        // Check if we want to edit the draft copy of an active workflow, or view/edit the "live" copy.
        // We will strictly expect a valid value for the "workflowMode" parameter, so that when it is missing we know
        // we have a page that hasn't been updated to handle draft workflows yet.
        String workflowMode = request.getParameter("workflowMode");
        if (workflowMode == null)
        {
            throw new IllegalStateException("Found a 'workflow' in the request parameters, but there is no "
                                            + "'workflowMode'. " + getFullRequestUrl(request));
        }
        JiraWorkflow workflow = getWorkflow(parent, workflowMode, workflowName, request);
        if (workflow == null)
        {
            throw new IllegalStateException("No " + workflowMode + " workflow was found for '" + workflowName + "'.");
        }

        requestContainer.addComponent(workflow);

        String workflowStep = request.getParameter("workflowStep");

        if (TextUtils.stringSet(workflowStep))
        {
            StepDescriptor step = workflow.getDescriptor().getStep(Integer.parseInt(workflowStep));

            if (step == null)
            {
                log.warn("No workflow step found for '" + workflowStep + "'");
                // We should return now to avoid NullPointerExceptions
                return requestContainer;
            }
            requestContainer.addComponent(step);

            String workflowTransition = request.getParameter("workflowTransition");

            if (TextUtils.stringSet(workflowTransition))
            {
                ActionDescriptor transition = step.getAction(Integer.parseInt(workflowTransition));

                if (transition != null)
                {
                    requestContainer.addComponent(transition);
                }
            }
        }
        else
        {
            // If step id is not supplied but the workflowTransition id is given, then
            // this must be a global action.
            String globalWorkflowTransition = request.getParameter("workflowTransition");
            if (TextUtils.stringSet(globalWorkflowTransition))
            {
                boolean actionFound = false;

                final List globalActions = workflow.getDescriptor().getGlobalActions();
                int globalActionId = Integer.parseInt(globalWorkflowTransition);
                for (final Object globalAction1 : globalActions)
                {
                    ActionDescriptor globalAction = (ActionDescriptor) globalAction1;
                    if (globalAction.getId() == globalActionId)
                    {
                        requestContainer.addComponent(globalAction);
                        actionFound = true;
                    }
                }

                if (!actionFound) // look in initial actions if we didn't find it yet
                {
                    final List initialActions = workflow.getDescriptor().getInitialActions();
                    int initialActionId = Integer.parseInt(globalWorkflowTransition);
                    for (final Object initialAction1 : initialActions)
                    {
                        ActionDescriptor initialAction = (ActionDescriptor) initialAction1;
                        if (initialAction.getId() == initialActionId)
                        {
                            requestContainer.addComponent(initialAction);
                            actionFound = true;
                        }
                    }
                }

                if (!actionFound)
                {
                    log.error("Could not find any actions matching this workflow transition: " + globalWorkflowTransition);
                }
            }
        }
        return requestContainer;
    }

    private JiraWorkflow getWorkflow(PicoContainer parent, String workflowMode, String workflowName, HttpServletRequest request)
    {
        // Use the WorkflowManager to get the required workflow
        WorkflowManager workflowManager = parent.getComponent(WorkflowManager.class);
        JiraWorkflow workflow;
        if (workflowMode.equals(JiraWorkflow.LIVE))
        {
            // get the "live" workflow
            workflow = workflowManager.getWorkflowClone(workflowName);
        }
        else if (workflowMode.equals(JiraWorkflow.DRAFT))
        {
            // get the draft workflow
            workflow = workflowManager.getDraftWorkflow(workflowName);
        }
        else
        {
            throw new IllegalStateException("Invalid workflow mode '" + workflowMode + "'. " + getFullRequestUrl(request));
        }

        return workflow;
    }

    private String getFullRequestUrl(HttpServletRequest request)
    {
        StringBuilder url = new StringBuilder(request.getRequestURL().toString());
        if (!StringUtils.isEmpty(request.getQueryString()))
        {
            url.append("?").append(request.getQueryString());
        }
        return url.toString();
    }
}
