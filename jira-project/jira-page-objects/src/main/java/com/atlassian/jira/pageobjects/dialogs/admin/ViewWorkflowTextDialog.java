package com.atlassian.jira.pageobjects.dialogs.admin;

import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

import java.util.List;

/**
 * @since v5.2
 */
public class ViewWorkflowTextDialog extends FormDialog
{
    private List<WorkflowTransition> transitions;

    public ViewWorkflowTextDialog()
    {
        super("workflow-text-view");
    }

    @Init
    private void initialize()
    {
        this.transitions = actuallyGetTransitions();
    }

    private List<WorkflowTransition> actuallyGetTransitions()
    {
        List<PageElement> rows = findAll(By.cssSelector(".project-config-datatable tbody tr"));
        List<WorkflowTransition> transtions = Lists.newArrayListWithExpectedSize(rows.size());
        String sourceStatus = null;
        int sourceStatusCount = 0;

        for (PageElement element : rows)
        {
            int column = 0;
            List<PageElement> cols = element.findAll(By.cssSelector("td"));

            if (sourceStatusCount <= 0)
            {
                PageElement status = cols.get(column);
                sourceStatus = status.getText();
                String rowspan = StringUtils.trimToNull(status.getAttribute("rowspan"));
                if (rowspan == null)
                {
                    sourceStatusCount = 1;
                }
                else
                {
                    sourceStatusCount = Integer.parseInt(rowspan);
                }

                column += 1;
            }
            else
            {
                column = 0;
            }

            //Transition name
            PageElement transElement = cols.get(column);
            PageElement tranNameElement = transElement.find(By.cssSelector(".project-config-transname"));
            if (!tranNameElement.isPresent())
            {
                //Looks like we have a transition with outgoing elements.
                transtions.add(binder.bind(WorkflowTransition.class, sourceStatus, null, null, null));
            }
            else
            {
                String transitionName = tranNameElement.getText();
                //Screen name
                PageElement screenElement = transElement.find(By.cssSelector(".project-config-screen"));
                if (!screenElement.isPresent())
                {
                    screenElement = null;
                }

                //Target status.
                column += 2;
                String targetStatus = cols.get(column).getText();
                transtions.add(binder.bind(WorkflowTransition.class, sourceStatus, transitionName, screenElement, targetStatus));
            }
            sourceStatusCount--;
        }
        return transtions;
    }

    public List<WorkflowTransition> getTransitions()
    {
        return transitions;
    }

    public String getWorkflowName()
    {
        final PageElement h2 = find(By.tagName("h2"));
        return h2.isPresent() ? h2.getText() : null;
    }

    public static class WorkflowTransition
    {
        private final String name;
        private final String targetStatus;
        private final String sourceStatus;
        private final String screenName;
        private final String screenLink;

        public WorkflowTransition(String sourceStatus, String name, PageElement screenElement, String targetStatus)
        {
            this.name = name;
            this.targetStatus = targetStatus;
            this.sourceStatus = sourceStatus;
            this.screenName = screenElement != null ? screenElement.getText() : null;
            this.screenLink = screenElement != null ? screenElement.getAttribute("href") : null;
        }

        public String getSourceStatusName()
        {
            return sourceStatus;
        }

        public String getTransitionName()
        {
            return name;
        }

        public String getTargetStatusName()
        {
            return targetStatus;
        }

        public String getScreenName()
        {
            return screenName;
        }

        public String getScreenLink()
        {
            return screenLink;
        }

        public boolean hasScreenLink()
        {
            return screenLink != null;
        }

        public boolean hasScreen()
        {
            return screenName != null;
        }
    }
}
