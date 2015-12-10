/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 23, 2004
 * Time: 4:02:21 PM
 */
package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class ViewWorkflowXml extends JiraWebActionSupport
{
    private final JiraWorkflow workflow;

    private String xml;
    private String name;

    public ViewWorkflowXml(JiraWorkflow workflow)
    {
        this.workflow = workflow;
        this.name = workflow.getName();
    }

    protected String doExecute() throws Exception
    {
        xml = WorkflowUtil.convertDescriptorToXML(workflow.getDescriptor());

        return SUCCESS;
    }

    public String getXml()
    {
        return xml;
    }

    public JiraWorkflow getWorkflow()
    {
        return workflow;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

}