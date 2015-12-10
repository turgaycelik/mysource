package com.atlassian.jira.functest.framework.admin.plugins;

import com.atlassian.jira.functest.framework.Administration;
import com.atlassian.jira.functest.framework.LocatorFactory;
import com.atlassian.jira.functest.framework.Navigation;
import net.sourceforge.jwebunit.WebTester;

/**
 * Represents the JIRA Reference Plugin. This plugin contains simple implementations of the module types that JIRA
 * supports.
 *
 * It enables us to test that these extension points actually work and can enabled/disabled at runtime.
 *
 * @since v4.4
 */
public class ReferencePlugin extends Plugin
{
    public static final String KEY = "com.atlassian.jira.dev.reference-plugin";

    private final RestResources restResources;
    private final WorkflowCondition workflowCondition;
    private final WorkflowFunction workflowFunction;
    private final WorkflowValidator workflowValidator;
    private final ReferenceModuleType moduleType;
    private final ResourceAction resourceAction;
    private final IssueTabPanel issueTabPanel;
    private final EchoFunction echoFunction;

    public ReferencePlugin(WebTester tester, Administration administration, LocatorFactory locators, Navigation navigation)
    {
        super(administration);
        this.restResources = new RestResources(tester, administration);
        this.workflowCondition = new WorkflowCondition(administration);
        this.workflowFunction = new WorkflowFunction(administration);
        this.workflowValidator = new WorkflowValidator(administration);
        this.moduleType = new ReferenceModuleType(tester, administration, locators);
        this.resourceAction = new ResourceAction(tester, locators);
        this.issueTabPanel = new IssueTabPanel(administration, navigation, locators);
        this.echoFunction = new EchoFunction(administration);
    }

    public String getKey()
    {
        return KEY;
    }

    public RestResources restResources()
    {
       return restResources;
    }

    public WorkflowCondition workflowCondition()
    {
        return workflowCondition;
    }

    public WorkflowFunction workflowFunction()
    {
        return workflowFunction;
    }

    public WorkflowValidator workflowValidator()
    {
        return workflowValidator;
    }

    public ReferenceModuleType moduleType()
    {
        return moduleType;
    }

    public ResourceAction resourceAction()
    {
        return resourceAction;
    }

    public IssueTabPanel issueTabPanel()
    {
        return issueTabPanel;
    }

    public EchoFunction getEchoFunction()
    {
        return echoFunction;
    }
}
