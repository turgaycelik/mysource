package com.atlassian.jira.pageobjects.pages.admin.workflow;

import com.atlassian.jira.pageobjects.dialogs.admin.DiscardDraftWorkflowSchemeDialog;
import com.atlassian.jira.pageobjects.dialogs.admin.ViewWorkflowTextDialog;
import com.atlassian.jira.pageobjects.framework.elements.PageElements;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.jira.pageobjects.util.TraceContext;
import com.atlassian.jira.pageobjects.util.Tracer;
import com.atlassian.jira.pageobjects.websudo.DecoratedJiraWebSudo;
import com.atlassian.jira.pageobjects.websudo.JiraSudoFormDialog;
import com.atlassian.jira.pageobjects.websudo.JiraWebSudo;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.atlassian.pageobjects.elements.query.Conditions.and;
import static com.atlassian.pageobjects.elements.query.Conditions.not;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static com.google.common.collect.ImmutableList.of;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.stripToNull;
import static org.openqa.selenium.By.cssSelector;

/**
 * @since v6.0
 */
public class EditWorkflowScheme extends AbstractJiraPage
{
    //This draft was last edited by Administrator at Today 11:11 AM.
    private static final Pattern TITLE = Pattern.compile("This draft was last edited by (.*) at (.*)\\.");
    private static final String COMPLETE_KEY = "edit.workflow.scheme.complete";
    private static final String CHANGED_KEY = "edit.workflow.scheme.changed";

    @ElementBy (id = "workflowscheme-editor")
    private PageElement editorElement;

    @ElementBy (id = "workflow-scheme-name")
    private PageElement nameElement;

    @ElementBy (id = "workflow-scheme-description")
    private PageElement descriptionElement;

    @ElementBy (className = "shared-by")
    private PageElement sharedBy;

    @ElementBy (cssSelector = "span.status-draft")
    private PageElement draftLozege;

    @ElementBy (id = "add-workflow-dropdown-trigger")
    private PageElement addWorkflowDropdown;

    @ElementBy (id = "add-workflow")
    private PageElement addExistingWorkflow;

    @ElementBy (id = "add-workflow-marketplace")
    private PageElement importBundleButton;

    @ElementBy (id = "publish-draft")
    private PageElement publish;

    @ElementBy (id = "discard-draft")
    private PageElement discard;

    @ElementBy (id = "view-original")
    private PageElement viewOriginal;

    @ElementBy (id = "view-draft")
    private PageElement viewDraft;

    @Inject
    private TraceContext traceContext;

    private final long schemeId;
    private final String workflow;

    public EditWorkflowScheme(final long schemeId)
    {
        this(schemeId, null);
    }

    public EditWorkflowScheme(final long schemeId, final String workflow)
    {
        this.schemeId = schemeId;
        this.workflow = workflow;
    }

    public ProjectSharedBy getSharedBy()
    {
        return pageBinder.bind(ProjectSharedBy.class, sharedBy);
    }

    public List<String> getSharedProjects()
    {
        final ProjectSharedBy dialog = getSharedBy();
        if (dialog.isPresent())
        {
            return dialog.getProjects();
        }
        else
        {
            return Collections.emptyList();
        }
    }

    public List<Workflow> getWorkflows()
    {
        final List<PageElement> rows = editorElement.findAll(cssSelector("table.workflows-list tbody tr"));
        final List<Workflow> workflows = Lists.newArrayList();
        for (final PageElement row : rows)
        {
            workflows.add(new Workflow(row));
        }
        return Collections.unmodifiableList(workflows);
    }

    public Workflow getWorkflow(final String workflow)
    {
        final PageElement pageElement = editorElement.find(cssSelector(format("tr[data-workflow-name='%s']", escapeForCss(workflow))));
        if (!pageElement.isPresent())
        {
            throw new IllegalArgumentException(format("Can't find workflow '%s' in the scheme.", workflow));
        }
        return new Workflow(pageElement);
    }

    private static String escapeForCss(final String string)
    {
        return string.replaceAll(Pattern.quote("'"), Matcher.quoteReplacement("\\'"));
    }

    @Override
    public String getUrl()
    {
        if (workflow != null)
        {
            return format("/secure/admin/EditWorkflowScheme.jspa?schemeId=%d#workflowName=%s", schemeId, workflow);
        }
        else
        {
            return format("/secure/admin/EditWorkflowScheme.jspa?schemeId=%d", schemeId);
        }
    }

    @Override
    public TimedCondition isAt()
    {
        return and(of(editorElement.timed().isPresent(),
                not(editorElement.timed().hasClass("workflowscheme-editor-loading"))));
    }

    public String getName()
    {
        return stripToNull(nameElement.getValue());
    }

    public String getDescription()
    {
        return descriptionElement.getValue();
    }

    public static String getTextSafe(final PageElement element)
    {
        return element.isPresent() ? stripToNull(element.getText()) : null;
    }

    public boolean isDraft()
    {
        return draftLozege.isPresent();
    }

    public String getLastModifiedTime()
    {
        //This draft was last edited by Administrator at Today 11:11 AM.
        return parseDraftTitle(2);
    }

    public String getLastModifiedUser()
    {
        return parseDraftTitle(1);
    }

    private String parseDraftTitle(final int group)
    {
        if (isDraft())
        {
            final Matcher title = TITLE.matcher(draftLozege.getAttribute("title"));
            if (title.matches())
            {
                return title.group(group);
            }
            else
            {
                throw new RuntimeException("Unexpected title \"" + draftLozege.getAttribute("title") + "\" on the draft lozenge.");
            }
        }
        return null;
    }

    public String getErrorMessage()
    {
        return getTextSafe(editorElement.find(By.className("aui-message")));
    }

    public EditWorkflowScheme setName(final String workflowSchemeName)
    {
        nameElement.clear().type(workflowSchemeName).type(Keys.RETURN);
        return this;
    }

    public EditWorkflowScheme setDescription(final String workflowSchemeDesc)
    {
        descriptionElement.clear().type(workflowSchemeDesc).type(Keys.RETURN);
        return this;
    }

    public EditWorkflowScheme addWorkflow(final String workflowName, final boolean makeDefault, final Iterable<String> issueTypes)
    {
        final AddWorkflowToSchemeDialog assignWorkflowToWorkflowSchemeDialog = addWorkflowDialog();
        assignWorkflowToWorkflowSchemeDialog.selectWorkflow(workflowName)
                .next()
                .setIssueTypes(makeDefault, issueTypes)
                .submit();

        return this;
    }

    public EditWorkflowScheme assignIssueTypesToWorkflowViaParameter(final boolean makeDefault, final Iterable<String> issueTypes)
    {
        final AssignWorkflowToSchemeDialog dialog = pageBinder.bind(AssignWorkflowToSchemeDialog.class, this);
        dialog.setIssueTypes(makeDefault, issueTypes).submit();
        return this;
    }

    public JiraWebSudo addWorkflowWebsudo(final String workflowName, final boolean makeDefault, final Iterable<String> issueTypes)
    {
        final AssignWorkflowToSchemeDialog dialog = addWorkflowDialog().selectWorkflow(workflowName).next().setIssueTypes(makeDefault, issueTypes);
        return doWebsudo(new Runnable()
        {
            @Override
            public void run()
            {
                dialog.submitNotWait();
            }
        });
    }

    public AddWorkflowToSchemeDialog addWorkflowDialog()
    {
        if (!canAddExistingWorfklow())
        {
            throw new IllegalArgumentException("Adding workflow's is currently disabled.");
        }

        addWorkflowDropdown.click();
        addExistingWorkflow.click();
        return pageBinder.bind(AddWorkflowToSchemeDialog.class, this);
    }

    public boolean canAddExistingWorfklow()
    {
        return addWorkflowDropdown.isEnabled() && !addExistingWorkflow.find(By.xpath("..")).hasClass("disabled");
    }

    public boolean canImportBundle()
    {
        return addWorkflowDropdown.isEnabled() && importBundleButton.isPresent();
    }

    public String getImportBundleButtonLabel()
    {
        return importBundleButton.getAttribute("text");
    }

    public boolean canViewDraft()
    {
        return canUse(viewDraft);
    }

    public EditWorkflowScheme viewDraft()
    {
        if (!canViewDraft())
        {
            throw new IllegalStateException("Unable to view draft as button is not present.");
        }
        viewDraft.click();
        return this;
    }

    public boolean canPublish()
    {
        return canUse(publish);
    }

    public StartDraftWorkflowSchemeMigrationPage publishDraft()
    {
        publish.click();
        return pageBinder.bind(StartDraftWorkflowSchemeMigrationPage.class, schemeId);
    }

    public boolean canDiscard()
    {
        return canUse(discard);
    }

    public EditWorkflowScheme discardDraft()
    {
        final DiscardDraftWorkflowSchemeDialog discardDialog = openDiscard();
        waitForAction(new Runnable()
        {
            @Override
            public void run()
            {
                discardDialog.submit();
            }
        });

        return this;
    }

    public JiraWebSudo discardDraftWebsudo()
    {
        final DiscardDraftWorkflowSchemeDialog workflowSchemeDialog = openDiscard();
        return doWebsudo(new Runnable()
        {
            @Override
            public void run()
            {
                workflowSchemeDialog.submit();
            }
        });
    }

    private DiscardDraftWorkflowSchemeDialog openDiscard()
    {
        if (!canDiscard())
        {
            throw new IllegalStateException("Unable to discard draft as button is not present.");
        }
        discard.click();
        return pageBinder.bind(DiscardDraftWorkflowSchemeDialog.class);
    }

    public EditWorkflowScheme viewOriginal()
    {
        if (!canViewOriginal())
        {
            throw new IllegalStateException("Unable to view original as button is not present.");
        }
        viewOriginal.click();
        return this;
    }

    public boolean canViewOriginal()
    {
        return canUse(viewOriginal);
    }

    public Function<Void, Boolean> changedAfterCondition()
    {
        return new ChangedMessageRule(traceContext);
    }

    private static boolean canUse(final PageElement element)
    {
        return element.isPresent() && element.isVisible();
    }

    private void clickAndWaitForAjax(final PageElement element)
    {
        waitForAction(new Runnable()
        {
            @Override
            public void run()
            {
                element.click();
            }
        });
    }

    void waitForAction(final Runnable runnable)
    {
        final Tracer checkpoint = traceContext.checkpoint();
        runnable.run();
        waitForAjax(checkpoint);
    }

    private void waitForAjax(final Tracer checkpoint)
    {
        traceContext.waitFor(checkpoint, COMPLETE_KEY);
    }

    public class Workflow
    {
        private final PageElement row;

        private Workflow(final PageElement row)
        {
            this.row = row;
        }

        public String getName()
        {
            return getTextSafe(row.find(By.className("workflow-name")));
        }

        public String getDescription()
        {
            return getTextSafe(row.find(By.className("workflow-description")));
        }

        public boolean isDefault()
        {
            return row.find(By.className("workflow-scheme-all-issue-types")).isPresent();
        }

        public List<String> getIssueTypes()
        {
            return PageElements.asText(row.findAll(By.cssSelector(".project-config-issuetype-name:not(.workflow-scheme-all-issue-types)")));
        }

        public ViewWorkflowTextDialog viewAsText()
        {
            row.find(By.className("workflow-text-view")).click();
            return pageBinder.bind(ViewWorkflowTextDialog.class);
        }

        public WorkflowDesigner viewAsDiagram()
        {
            row.find(By.className("workflow-diagram-view")).click();
            return pageBinder.bind(WorkflowDesigner.class);
        }

        public Workflow removeIssueTypes(final Iterable<String> issueTypeNames)
        {
            for (final String issueType: issueTypeNames)
            {
                clickAndWaitForAjax(getRemoveElementMustExist(issueType));
            }
            return this;
        }

        public JiraWebSudo removeIssueTypeWebsudo(final String issueType)
        {
            return doWebsudo(new Runnable()
            {
                @Override
                public void run()
                {
                    getRemoveElementMustExist(issueType).click();
                }
            });
        }

        private PageElement getRemoveElement(final String issueType)
        {
            return row.find(cssSelector(format("*[data-issue-type-name='%s'] a.remove-issue-type", escapeForCss(issueType))));
        }

        private PageElement getRemoveElementMustExist(final String issueType)
        {
            final PageElement element = getRemoveElement(issueType);
            if (!element.isPresent())
            {
                throw new IllegalArgumentException(format("Unable to find issue type '%s' assigned to workflow '%s.'", issueType, getName()));
            }
            return element;
        }

        public AssignWorkflowToSchemeDialog assignDialog()
        {
            clickAssignElement();
            return bindAssignDialog();
        }

        private AssignWorkflowToSchemeDialog bindAssignDialog()
        {
            return pageBinder.bind(AssignWorkflowToSchemeDialog.class, EditWorkflowScheme.this);
        }

        private void clickAssignElement()
        {
            final PageElement pageElement = getAssignElement();
            if (!pageElement.isPresent())
            {
                throw new IllegalArgumentException(format("Unable to find assign link for workflow '%s'.", getName()));
            }
            pageElement.click();
        }

        public boolean canAssign()
        {
            final PageElement assignElement = getAssignElement();
            return assignElement.isPresent() && assignElement.isVisible();
        }

        private PageElement getAssignElement()
        {
            return row.find(By.className("assign-issue-types"));
        }

        public Workflow assignIssueTypes(final boolean makeDefault, Iterable<String> issueTypeNames)
        {
            if (makeDefault)
            {
                issueTypeNames = Iterables.concat(Collections.<String>singleton(null), issueTypeNames);
            }

            assignDialog().setIssueTypes(issueTypeNames).submit();
            return this;
        }

        public JiraWebSudo assignIssueTypesWebsudo(final boolean makeDefault, Iterable<String> issueTypes)
        {
            if (makeDefault)
            {
                issueTypes = Iterables.concat(Collections.<String>singleton(null), issueTypes);
            }
            final AssignWorkflowToSchemeDialog dialog = assignDialog().setIssueTypes(issueTypes);
            return doWebsudo(new Runnable()
            {
                @Override
                public void run()
                {
                    dialog.submitNotWait();
                }
            });
        }

        public void delete()
        {
            clickAndWaitForAjax(getDeleteElementMustExist());
        }

        private PageElement getDeleteElementMustExist()
        {
            final PageElement pageElement = getDeleteElement();
            if (!pageElement.isPresent())
            {
                throw new IllegalArgumentException(format("Unable to find remove link for workflow '%s'.", getName()));
            }
            return pageElement;
        }

        public JiraWebSudo deleteWebsudo()
        {
            return doWebsudo(new Runnable()
            {
                @Override
                public void run()
                {
                    getDeleteElementMustExist().click();
                }
            });
        }

        public boolean canDelete()
        {
            final PageElement element = getDeleteElement();
            return element.isPresent() && element.isVisible();
        }

        private PageElement getDeleteElement()
        {
            return row.find(By.className("remove-all-issue-types"));
        }

        public boolean canDeleteIssueType(final String issueTypeName)
        {
            return getRemoveElement(issueTypeName).isPresent();
        }
    }

    private JiraWebSudo doWebsudo(final Runnable action)
    {
        final Tracer checkpoint = traceContext.checkpoint();
        action.run();
        final JiraSudoFormDialog bind = pageBinder.bind(JiraSudoFormDialog.class, JiraSudoFormDialog.ID_SMART_WEBSUDO);
        return new DecoratedJiraWebSudo(bind)
        {
            @Override
            protected void afterAuthenticate()
            {
                waitForAjax(checkpoint);
            }
        };
    }

    public static class WorkflowDesigner
    {
        @ElementBy (id = "view-workflow-dialog-workflow-schemes")
        private PageElement designer;

        @ElementBy (className = "jira-dialog-heading")
        private PageElement titleElement;

        public String getTitle()
        {
            return titleElement.getText();
        }

        public void close()
        {
            designer.find(By.id("aui-dialog-close")).click();
        }

        @WaitUntil
        final public void ready()
        {
            waitUntilTrue(and(designer.timed().isVisible(), titleElement.timed().isVisible()));
        }
    }

    private class ChangedMessageRule implements Function<Void, Boolean>
    {
        private final TraceContext context;
        private final Tracer tracer;

        private ChangedMessageRule(final TraceContext context)
        {
            this.context = context;
            this.tracer = context.checkpoint();
        }

        @Override
        public Boolean apply(@Nullable final Void input)
        {
            return context.exists(tracer, CHANGED_KEY);
        }
    }
}
