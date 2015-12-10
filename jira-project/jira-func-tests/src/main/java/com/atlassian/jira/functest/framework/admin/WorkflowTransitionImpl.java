package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

import java.util.Map;

/**
 * Default implementation of {@link WorkflowTransition}.
 *
 * @since v4.3
 */
public class WorkflowTransitionImpl extends AbstractFuncTestUtil implements WorkflowTransition
{
    private static final String ADD_POST_FUNCTION_LINK_ID = "add_post_func";
    private static final String ADD_CONDITION_LINK_ID = "add_new_condition";
    private static final String ADD_VALIDATOR_LINK_ID = "add_new_validator";
    private static final String RADIO_TYPE_NAME = "type";
    private static final String ADD_BUTTON_NAME = "Add";


    public WorkflowTransitionImpl(WebTester tester, JIRAEnvironmentData environmentData, int logIndentLevel)
    {
        super(tester, environmentData, logIndentLevel);
    }

    @Override
    public boolean canAddWorkflowCondition(String workflowConditionKey)
    {
        goToAddWorkflowCondition();
        return workflowModuleRadioLocator(workflowConditionKey).exists();
    }

    @Override
    public WorkflowTransition goToAddWorkflowCondition()
    {
        openTab(Tabs.CONDITIONS);
        tester.clickLink(ADD_CONDITION_LINK_ID);
        return this;
    }

    @Override
    public WorkflowTransition addWorkflowCondition(String workflowConditionKey)
    {
        goToAddWorkflowCondition();
        selectWorkflowModule(workflowConditionKey);
        submitAdd();
        return this;
    }

    @Override
    public WorkflowTransition addWorkflowCondition(String workflowConditionKey, Map<String, String> configFormParams)
    {
        goToAddWorkflowCondition();
        selectWorkflowModule(workflowConditionKey);
        submitAdd();
        setConfigValues(configFormParams);
        submitAdd();
        return this;
    }


    @Override
    public WorkflowTransition goToAddWorkflowValidator()
    {
        openTab(Tabs.VALIDATORS);
        tester.clickLink(ADD_VALIDATOR_LINK_ID);
        return this;
    }

    @Override
    public boolean canAddWorkflowValidator(String workflowValidatorKey)
    {
        goToAddWorkflowValidator();
        return workflowModuleRadioLocator(workflowValidatorKey).exists();
    }

    @Override
    public WorkflowTransition addWorkflowValidator(String workflowValidatorKey)
    {
        goToAddWorkflowValidator();
        selectWorkflowModule(workflowValidatorKey);
        submitAdd();
        return this;
    }

    @Override
    public WorkflowTransition addWorkflowValidator(String workflowValidatorKey, Map<String, String> configFormParams)
    {
        goToAddWorkflowValidator();
        selectWorkflowModule(workflowValidatorKey);
        submitAdd();
        setConfigValues(configFormParams);
        submitAdd();
        return this;
    }

    @Override
    public boolean canAddWorkflowFunction(String workflowFunctionKey)
    {
        goToAddWorkflowFunction();
        return workflowModuleRadioLocator(workflowFunctionKey).exists();
    }

    @Override
    public WorkflowTransition goToAddWorkflowFunction()
    {
        openTab(Tabs.POST_FUNCTIONS);
        tester.clickLink(ADD_POST_FUNCTION_LINK_ID);
        return this;
    }

    @Override
    public WorkflowTransition addWorkflowFunction(String workflowFunctionKey)
    {
        goToAddWorkflowFunction();
        selectWorkflowModule(workflowFunctionKey);
        submitAdd();
        return this;
    }

    @Override
    public WorkflowTransition addWorkflowFunction(String workflowFunctionKey, Map<String, String> configFormParams)
    {
        goToAddWorkflowFunction();
        selectWorkflowModule(workflowFunctionKey);
        setConfigValues(configFormParams);
        submitAdd();
        return this;
    }

    @Override
    public boolean isTabOpen(Tabs tab)
    {
        return !locators.id(tab.linkId()).exists();
    }

    @Override
    public WorkflowTransition openTab(Tabs tab)
    {
        if (!isTabOpen(tab))
        {
            tester.clickLink(tab.linkId());
        }
        return this;
    }

    private void selectWorkflowModule(String workflowModuleKey)
    {
        tester.setWorkingForm(FunctTestConstants.JIRA_FORM_NAME);
        tester.setFormElement(RADIO_TYPE_NAME, workflowModuleKey);
    }

    private void setConfigValues(Map<String, String> configFormParams)
    {
        tester.setWorkingForm(FunctTestConstants.JIRA_FORM_NAME);
        for (Map.Entry<String,String> formEntry : configFormParams.entrySet())
        {
            tester.setFormElement(formEntry.getKey(), formEntry.getValue());
        }
    }

    private void submitAdd()
    {
        tester.submit(ADD_BUTTON_NAME);
    }

    private Locator workflowModuleRadioLocator(String moduleKey)
    {
        return locators.css(String.format("input[type=radio][name=type][value=%s]",moduleKey));
    }
}
