package com.atlassian.jira.web.action.admin.workflow;

import java.io.PrintWriter;

import com.opensymphony.workflow.loader.WorkflowDescriptor;

public class MockWorkflowDescriptor extends WorkflowDescriptor
{
    private String workflowXml;

    public MockWorkflowDescriptor(String workflowXml)
    {
        this.workflowXml = workflowXml;
    }

    public void writeXML(PrintWriter out, int indent)
    {
        out.write(workflowXml);
    }
}
