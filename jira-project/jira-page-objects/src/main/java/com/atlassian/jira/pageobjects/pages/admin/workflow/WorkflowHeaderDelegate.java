package com.atlassian.jira.pageobjects.pages.admin.workflow;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.jira.pageobjects.util.JavascriptRunner;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

import static org.junit.Assert.assertTrue;

/**
 * @since v5.1
 */
public class WorkflowHeaderDelegate implements WorkflowHeader
{
    public static Pattern WORKFLOW_ID = Pattern.compile("workflow-(.+)");

    @Inject
    private PageBinder binder;

    @Inject
    private PageElementFinder finder;

    @Inject
    private JavascriptRunner executor;

    @ElementBy(cssSelector = "header .workflow-name")
    private PageElement workflowName;

    @ElementBy(cssSelector = "header .workflow-description")
    private PageElement workflowDescription;

    @ElementBy(id = "loz-workflow-system")
    private PageElement systemLozenge;

    @ElementBy(id = "loz-workflow-draft")
    private PageElement draftLozenge;

    @ElementBy(id = "loz-workflow-active")
    private PageElement activeLozenge;

    @ElementBy(id = "loz-workflow-inactive")
    private PageElement inactiveLozenge;

    @ElementBy(id = "publish_draft_workflow")
    private PageElement publishTrigger;

    @ElementBy(id = "discard_draft_workflow")
    private PageElement discardTrigger;

    @ElementBy(id = "draft-workflow-trigger")
    private PageElement draftTrigger;

    @ElementBy(id = "view-draft-workflow-trigger")
    private PageElement viewDraftTrigger;

    @ElementBy (cssSelector = "header .shared-by")
    private PageElement sharedBy;

    @ElementBy (id = "edit-workflow-trigger")
    private PageElement renameTrigger;

    @ElementBy (id = "view_live_workflow")
    private PageElement viewLiveWorkflow;

    @Override
    public String getWorkflowName()
    {
        return getTextSafely(workflowName);
    }

    @Override
    public String getWorkflowDescription()
    {
        return getTextSafely(workflowDescription);
    }

    @Override
    public boolean isSystem()
    {
        return systemLozenge.isPresent();
    }

    @Override
    public ProjectSharedBy sharedBy()
    {
        return binder.bind(ProjectSharedBy.class, sharedBy);
    }

    @Override
    public List<String> getSharedProjects()
    {
        final ProjectSharedBy by = sharedBy();
        if (!by.isPresent())
        {
            return Collections.emptyList();
        }
        else
        {
            by.openDialog();
            List<String> ret = by.getProjects();
            by.closeDialog();
            return ret;
        }
    }

    @Override
    public boolean canEditNameOrDescription()
    {
        return renameTrigger.isPresent();
    }

    @Override
    public EditWorkflowNameAndDescriptionDialog editNameOrDescription()
    {
        assertTrue("Can't edit name or description on this page.", canEditNameOrDescription());
        renameTrigger.click();
        return binder.bind(EditWorkflowNameAndDescriptionDialog.class);
    }

    @Override
    public boolean canCreateOrEditDraft()
    {
        return draftTrigger.isPresent();
    }

    @Override
    public boolean canDiscard()
    {
        return discardTrigger.isPresent();
    }

    @Override
    public boolean canViewDraft()
    {
        return viewDraftTrigger.isPresent();
    }

    @Override
    public DiscardDraftDialog openDiscardDialog()
    {
        assertTrue("Discard link not on the current page.", canDiscard());
        discardTrigger.click();
        return binder.bind(DiscardDraftDialog.class, getWorkflowName());
    }

    @Override
    public boolean canPublish()
    {
        return publishTrigger.isPresent();
    }

    @Override
    public PublishDialog openPublishDialog()
    {
        assertTrue("Publish link not on the current page.", canPublish());
        publishTrigger.click();
        return binder.bind(PublishDialog.class, getWorkflowName());
    }

    @Override
    public boolean isDraft()
    {
        return draftLozenge.isPresent();
    }

    @Override
    public boolean hasLinkToLiveWorkflow()
    {
        return viewLiveWorkflow.isPresent();
    }

    @Override
    public ViewWorkflowSteps gotoLiveWorkflow()
    {
        assertTrue("Click to go to live workflow not present.", hasLinkToLiveWorkflow());
        final String name = getWorkflowName();

        viewLiveWorkflow.click();
        return binder.bind(ViewWorkflowSteps.class, name, false);
    }

    @Override
    public boolean isActive()
    {
        return activeLozenge.isPresent();
    }

    @Override
    public boolean isInactive()
    {
        return inactiveLozenge.isPresent();
    }

    @Override
    public List<String> getInfoMessages()
    {
        return getTextsFromElements(finder.findAll(By.cssSelector("header + .aui-message.info.last-edited")));
    }

    @Override
    public List<String> getWarningMessages()
    {
        return getTextsFromElements(finder.findAll(By.cssSelector("header + .aui-message.warning.last-edited")));
    }

    @Override
    public WorkflowMode<?> getCurrentMode()
    {
        PageElement pageElement = finder.find(By.cssSelector("#workflow-links .active"));
        String id = pageElement.getAttribute("id");
        Matcher matcher = WORKFLOW_ID.matcher(id);
        if (matcher.matches())
        {
            return WorkflowMode.fromString(matcher.group(1));
        }
        else
        {
            return null;
        }
    }

    @Override
    public <T extends WorkflowHeader> T setCurrentEditMode(WorkflowMode<T> mode)
    {
        final String wfName = getWorkflowName();
        final boolean draft = isDraft();
        setCurrentViewMode(mode);

        return mode.bind(binder, wfName, draft);
    }

    @Override
    public void setCurrentViewMode(WorkflowMode<?> mode)
    {
        if (getCurrentMode() != mode)
        {
            PageElement pageElement = finder.find(By.id(String.format("workflow-%s", mode.getName())));
            pageElement.click();

            if (mode == WorkflowMode.DIAGRAM)
            {
                clearWorkflowDesignerState();
            }
        }
    }

    @Override
    public <T extends WorkflowHeader> T createDraftInMode(WorkflowMode<T> mode)
    {
        assertTrue("Trying to edit a workflow but link to do so does not exist.", draftTrigger.isPresent());
        String workflowName = getWorkflowName();
        setCurrentViewMode(mode);
        draftTrigger.click();
        return mode.bindAfterCreateDraft(binder, workflowName);
    }

    @Override
    public <T extends WorkflowHeader> T createDraft(WorkflowMode<T> mode)
    {
        assertTrue("Trying to edit a workflow but link to do so does not exist.", draftTrigger.isPresent());
        String workflowName = getWorkflowName();

        draftTrigger.click();
        return mode.bindAfterCreateDraft(binder, workflowName);
    }

    private List<String> getTextsFromElements(List<PageElement> elements)
    {
        return Lists.newArrayList(Iterables.transform(elements, new Function<PageElement, String>()
        {
            @Override
            public String apply(PageElement input)
            {
                return StringUtils.stripToNull(input.getText());
            }
        }));
    }

    public TimedCondition isPresentCondition(String name)
    {
        TimedCondition present = workflowName.timed().isPresent();
        if (workflowName != null)
        {
            present = Conditions.and(present, workflowName.timed().hasText(name));
        }
        return present;
    }

    private static String getTextSafely(PageElement pageElement)
    {
        if (pageElement.isPresent())
        {
            return StringUtils.trimToNull(pageElement.getText());
        }
        else
        {
            return null;
        }
    }

    void clearWorkflowDesignerState()
    {
        executor.removeWorkflowDesignerUnloadHooks();
    }
}
