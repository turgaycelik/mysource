/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.mock.workflow;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.workflow.AbstractJiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.workflow.InvalidWorkflowDescriptorException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.loader.WorkflowLoader;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/**
 * A mock workflow that can be loaded from file. So not really that much mockness in it...
 *
 * @deprecated use {@link com.atlassian.jira.workflow.MockJiraWorkflow}, or Mockito mocks in your tests. Unless
 * you really really think you need to load your workflow from XML file. In which case still use the above alternatives.
 */
@Deprecated
public class MockXmlLoadableJiraWorkflow extends AbstractJiraWorkflow
{
    private String name;

    public MockXmlLoadableJiraWorkflow(WorkflowManager workflowManager, String filename) throws InvalidWorkflowDescriptorException, IOException, SAXException
    {
        super(workflowManager, getJiraWorkflow(filename));
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void reset()
    {
        // do nothing
    }

    public boolean isDraftWorkflow()
    {
        return false;
    }

    public static WorkflowDescriptor getJiraWorkflow(String filename) throws InvalidWorkflowDescriptorException, IOException, SAXException
    {
        InputStream isBrokenWorkflow = ClassLoaderUtils.getResourceAsStream(filename, WorkflowDescriptor.class);
        try
        {
            return WorkflowLoader.load(isBrokenWorkflow, true);
        }
        finally
        {
            IOUtils.closeQuietly(isBrokenWorkflow);
        }
    }

}
