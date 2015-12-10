package com.atlassian.jira.pageobjects.pages.admin.workflow;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;

import org.openqa.selenium.By;

/**
 * Represents the ViewWorkflowSteps page in JIRA.
 *
 * @since v4.4
 */
public class ViewWorkflowSteps extends AbstractWorkflowHeaderPage
{
    @ElementBy(id = "steps_table")
    private PageElement stepsElement;

    private final String url;
    private final String workflowName;

    public ViewWorkflowSteps(String workflowName, boolean draft)
    {
        this.workflowName = workflowName;
        url = String.format("/secure/admin/workflows/ViewWorkflowSteps.jspa?workflowMode=%s&workflowName=%s",
                draft ? "draft" : "live", encodeParameter(workflowName));
    }

    public ViewWorkflowSteps(String workflowName)
    {
        this(workflowName, false);
    }

    public ViewWorkflowSteps()
    {
        url = null;
        workflowName = null;
    }

    //Must be overwritten so that WD will find and execute it.
    @Init
    public void init()
    {
        super.init();
    }

    @Override
    public String getUrl()
    {
        if (url == null)
        {
            throw new IllegalStateException("Need to use other constructor");
        }
        return url;
    }

    @Override
    public TimedCondition isAt()
    {
        return Conditions.and(stepsElement.timed().isPresent(),
                getWorkflowHeader().isPresentCondition(workflowName));
    }

    public List<WorkflowStepItem> getWorkflowStepItems()
    {
        List<WorkflowStepItem> stepItems = new ArrayList<WorkflowStepItem>();

        final List<PageElement> stepInfo = stepsElement.find(By.tagName("tbody")).findAll(By.tagName("tr"));
        for (final PageElement step : stepInfo)
        {
            final List<PageElement> stepData = step.findAll(By.tagName("td"));
            String stepName = null;
            String stepNumber = null;
            List<Transition> transitions = new ArrayList<Transition>();
            List<PageElement> operations = null;

            for (final PageElement data : stepData)
            {
                if (data.find(By.cssSelector("[id^='step_link']")).isPresent())
                {
                    stepName = data.find(By.cssSelector("[id^='step_link']")).getText().trim();
                    stepNumber = data.find(By.className("smallgrey")).getText().replace("(", "").replace(")", "").trim();
                }
                else if (data.find(By.cssSelector("[id^='edit_action']")).isPresent())
                {
                    final List<PageElement> transitionElements = data.findAll(By.tagName("a"));
                    for (final PageElement transition : transitionElements)
                    {
                        transitions.add(new Transition(data.find(By.className("smallgrey")).getText().replace("(", "").replace(")", "").trim(), transition));
                    }
                }
                else if (data.find(By.className("operations-list")).isPresent())
                {
                    operations = data.findAll(By.tagName("li"));
                }
            }
            stepItems.add(new WorkflowStepItem(stepName, stepNumber, transitions, operations));
        }
        return stepItems;
    }

    private String encodeParameter(String value)
    {
        try
        {
            return URLEncoder.encode(value, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    public ViewWorkflowTransitionPage goToEditTransition(final PageElement transitionElement,
            final String mode,
            final String name,
            final String stepNumber,
            final String transitionNumber)
    {
        transitionElement.click();
        final PageElement workflowItems = elementFinder.find(By.className("workflow-browser-items"), TimeoutType.PAGE_LOAD);
        Poller.waitUntilTrue(workflowItems.timed().isPresent());
        return pageBinder.bind(ViewWorkflowTransitionPage.class, mode, name, stepNumber, transitionNumber);
    }

    public static class WorkflowStepItem
    {
        private String stepName;
        private String stepNumber;
        private List<Transition> transitions;
        private List<PageElement> operations;

        public WorkflowStepItem(final String stepName,
                final String stepNumber,
                final List<Transition> transitions,
                final List<PageElement> operations)
        {
            this.stepName = stepName;
            this.stepNumber = stepNumber;
            this.transitions = transitions;
            this.operations = operations;
        }

        public String getStepName()
        {
            return stepName;
        }

        public List<Transition> getTransitions()
        {
            return transitions;
        }

        public List<PageElement> getOperations()
        {
            return operations;
        }

        public String getStepNumber()
        {
            return stepNumber;
        }
    }

    public static class Transition
    {
        private String transitionNumber;
        private PageElement transition;

        public Transition(final String transitionNumber, final PageElement transition)
        {
            this.transitionNumber = transitionNumber;
            this.transition = transition;
        }

        public PageElement getTransition()
        {
            return transition;
        }

        public String getTransitionNumber()
        {
            return transitionNumber;
        }
    }
}

