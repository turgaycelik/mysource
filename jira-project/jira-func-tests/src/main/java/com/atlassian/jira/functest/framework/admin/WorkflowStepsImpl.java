package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.HtmlPage;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.meterware.httpunit.WebLink;
import net.sourceforge.jwebunit.WebTester;
import org.xml.sax.SAXException;

/**
 * Default implementation of {@link WorkflowSteps}.
 *
 * @since v4.3
 */
public class WorkflowStepsImpl extends AbstractFuncTestUtil implements WorkflowSteps
{
    private static final String TRANSITION_LINK_ID_TEMPLATE = "edit_action_%d_%d";
    private static final String ADD_TRANSITION_LINK_PREFIX = "add_trans_";

    private final WorkflowTransition transition;

    public WorkflowStepsImpl(WebTester tester, JIRAEnvironmentData environmentData, int logIndentLevel)
    {
        super(tester, environmentData, logIndentLevel);
        this.transition = new WorkflowTransitionImpl(tester, environmentData, childLogIndentLevel());
    }

    @Override
    public WorkflowTransition editTransition(int stepId, int transitionId)
    {
        tester.clickLink(transitionLinkLocatorFor(stepId, transitionId));
        return transition;
    }

    @Override
    public WorkflowSteps add(String stepName, String linkedStatus)
    {
        tester.setFormElement("stepName", stepName);
        if (linkedStatus != null)
        {
            tester.selectOption("stepStatus", linkedStatus);
        }
        tester.submit("Add");
        return this;
    }

    @Override
    public WorkflowSteps addTransition(String stepName, String transitionName, String transitionDescription,
            String destinationStep, String transitionFieldScreen)
    {
        tester.clickLink(ADD_TRANSITION_LINK_PREFIX + getStepId(stepName));
        tester.setFormElement("transitionName", transitionName);
        tester.setFormElement("description", transitionDescription);
        tester.selectOption("destinationStep", destinationStep);
        if (transitionFieldScreen != null)
        {
            tester.selectOption("view", transitionFieldScreen);
        }

        tester.submit("Add");
        return this;
    }

    private String getStepId(String stepName)
    {
        WebLink[] links =  new HtmlPage(tester).getLinksWithExactText(stepName);
        if (links != null)
        {
            String workflowStepLinkId = links[0].getID();
            return workflowStepLinkId.substring(10);
        }
        return "not found";
    }

    private String transitionLinkLocatorFor(int stepId, int transitionId)
    {
        return String.format(TRANSITION_LINK_ID_TEMPLATE, stepId, transitionId);
    }

}
