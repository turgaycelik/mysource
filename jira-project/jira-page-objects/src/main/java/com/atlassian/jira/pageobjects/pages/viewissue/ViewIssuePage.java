package com.atlassian.jira.pageobjects.pages.viewissue;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.atlassian.jira.pageobjects.components.menu.IssueActions;
import com.atlassian.jira.pageobjects.dialogs.IssueActionsUtil;
import com.atlassian.jira.pageobjects.dialogs.quickedit.EditIssueDialog;
import com.atlassian.jira.pageobjects.model.DefaultIssueActions;
import com.atlassian.jira.pageobjects.model.WorkflowIssueAction;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.pages.viewissue.link.IssueLinkSection;
import com.atlassian.jira.pageobjects.pages.viewissue.link.activity.Comment;
import com.atlassian.jira.pageobjects.pages.viewissue.people.PeopleSection;
import com.atlassian.jira.pageobjects.pages.viewissue.watchers.WatchersComponent;
import com.atlassian.jira.pageobjects.util.TraceContext;
import com.atlassian.jira.pageobjects.util.Tracer;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import org.openqa.selenium.By;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Represents view issue page.
 *
 * @since v4.4
 */
public class ViewIssuePage extends AbstractJiraPage
{
    private static final String URI_TEMPLATE = "/browse/%s";
    private static final String URI_WITH_ANCHOR_TEMPLATE = URI_TEMPLATE + "#%s";

    private final String issueKey;
    private final String uri;

    @ElementBy (tagName = "body", timeoutType = TimeoutType.PAGE_LOAD)
    protected PageElement body;

    @ElementBy (id = "watcher-data")
    private PageElement watchers;

    @ElementBy (id = "vote-data")
    private PageElement voters;

    @ElementBy (id = "vote-toggle")
    private PageElement votingToggle;

    @ElementBy (id = "stalker")
    private PageElement stalkerBar;

    @ElementBy (cssSelector = "#stalker h1")
    private PageElement summary;

    @ElementBy (id = "key-val")
    private PageElement issueHeaderLink;

    @ElementBy (id = "activitymodule")
    private PageElement activityModule;

    //addComment button
    @ElementBy (id = "footer-comment-button")
    private PageElement addComment;

    //possible confusion with addComment
    @ElementBy (id = "addcomment")
    private PageElement addCommentModule;

    @ElementBy (id = "project-name-val")
    private PageElement project;

    @ElementBy (id = "issue-content")
    private PageElement issueContentContainer;

    @ElementBy (id = "type-val")
    private PageElement issueType;

    @ElementBy (id = "priority-val")
    private PageElement priority;

    @ElementBy (id = "status-val")
    private PageElement status;

    @ElementBy (id = "resolution-val")
    private PageElement resolution;

    @ElementBy (id = "opsbar-operations_more")
    private PageElement moreActions;

    @ElementBy (id = "opsbar-transitions_more")
    private PageElement moreTransitions;

    @ElementBy (id = "descriptionmodule")
    private PageElement descriptionModule;

    @ElementBy (id = "description-val")
    private PageElement descriptionValue;

    @ElementBy (cssSelector = "#description-val.editable-field")
    private PageElement descriptionEditable;

    @ElementBy (id = "due-date")
    private PageElement issueDueDate;


    @Inject
    private IssueActionsUtil issueActionsUtil;

    @Inject
    private PageBinder pageBinder;

    @Inject
    private PageElementFinder pageElementFinder;

    @Inject
    private TraceContext traceContext;

    private IssueMenu issueMenu;
    private MoreActionsMenu moreActionsMenu;
    private SubtaskModule subtasksModule;

    public ViewIssuePage(String issueKey)
    {
        this.issueKey = notNull(issueKey);
        this.uri = String.format(URI_TEMPLATE, issueKey);
    }

    public ViewIssuePage(String issueKey, String anchor)
    {
        this.issueKey = notNull(issueKey);
        this.uri = String.format(URI_WITH_ANCHOR_TEMPLATE, issueKey, anchor);
    }

    @Init
    public void initComponents()
    {
        issueMenu = pageBinder.bind(IssueMenu.class, this);
        moreActionsMenu = pageBinder.bind(MoreActionsMenu.class);
    }

    @Override
    public TimedCondition isAt()
    {
        return issueHeaderLink.timed().hasText(issueKey);
    }

    @Override
    public String getUrl()
    {
        return uri;
    }

    public String getProject()
    {
        return project.getText().trim();
    }

    public String readKeyFromPage()
    {
        return issueHeaderLink.getText();
    }

    /** @deprecated use {@link #getIssueMenu()} and then {@link IssueMenu#invoke(com.atlassian.jira.pageobjects.model.IssueOperation)} */
    public MoreActionsMenu getMoreActionsMenu()
    {
        return moreActionsMenu;
    }

    public IssueLinkSection getIssueLinkSection()
    {
        return pageBinder.bind(IssueLinkSection.class, issueKey);
    }

    public IssueMenu getIssueMenu()
    {
        return issueMenu;
    }

    public SubtaskModule getSubTasksModule()
    {
        return pageBinder.bind(SubtaskModule.class);
    }

    public PeopleSection getPeopleSection()
    {
        return pageBinder.bind(PeopleSection.class);
    }

    public DetailsSection getDetailsSection()
    {
        return pageBinder.bind(DetailsSection.class);
    }

    public Boolean hasDescription()
    {
        return descriptionModule.isPresent();
    }

    public Boolean hasEditableDescription()
    {
        return descriptionEditable.isPresent();
    }

    public PageElement getDescriptionValue()
    {
        return descriptionValue;
    }

    public ActivitySection getActivitySection()
    {
        return pageBinder.bind(ActivitySection.class, issueKey);
    }

    public AssignIssueDialog assignIssueViaKeyboardShortcut()
    {
        execKeyboardShortcut(DefaultIssueActions.ASSIGN_ISSUE.shortcut());
        return pageBinder.bind(AssignIssueDialog.class);
    }

    public EditIssueDialog editIssueViaKeyboardShortcut()
    {
        execKeyboardShortcut(DefaultIssueActions.EDIT_ISSUE.shortcut());
        return pageBinder.bind(EditIssueDialog.class);
    }

    public String getSummary()
    {
        return summary.getText();
    }

    public TimedQuery<String> getTimedSummary()
    {
        return summary.timed().getText();
    }

    public String getIssueKey()
    {
        return issueKey;
    }

    public String getIssueType()
    {
        return issueType.getText();
    }

    public TimedCondition isIssueTypeEditable()
    {
        return issueType.timed().hasClass("editable-field");
    }

    public boolean isAddCommentModuleActive()
    {
        return addCommentModule.hasClass("active");
    }

    public String getPriority()
    {
        return priority.getText();
    }

    public String getStatus()
    {
        return status.getText();
    }

    public String getResolution()
    {
        return resolution.getText();
    }

    public Iterable<Comment> getComments()
    {
        return Iterables.transform(activityModule.findAll(By.cssSelector("div.activity-comment")), new Function<PageElement, Comment>()
            {
            @Override
            public Comment apply(@Nullable final PageElement commentElement)
            {
                final PageElement commentRestriction = commentElement.find(By.xpath(".//span[contains(.,'Restricted to')]"));
                String restrictionString = null;
                if (commentRestriction.isPresent())
                {
                    restrictionString = commentRestriction.getText();
                }
                return new Comment(commentElement.find(By.className("action-body")).getText(), restrictionString);
            }
            });
    }

    public AddCommentSection comment()
    {
        addComment.click();
        return pageBinder.bind(AddCommentSection.class, this);
    }

    public ViewIssuePage closeIssue()
    {
        getIssueMenu().invoke(new WorkflowIssueAction(2, "Close Issue"));
        pageBinder.bind(CloseIssueDialog.class).submit();
        return pageBinder.bind(ViewIssuePage.class, getIssueKey());
    }

    public void closeIssue(ActionTrigger trigger)
    {
        Tracer tracer = traceContext.checkpoint();
        if (trigger == ActionTrigger.MENU)
        {
            moreTransitions.click();
        }
        issueActionsUtil.closeIssue(trigger);
        waitForAjaxRefresh(tracer);
    }

    public void resolveIssue(ActionTrigger trigger)
    {
        Tracer tracer = traceContext.checkpoint();
        if (trigger == ActionTrigger.MENU)
        {
            moreTransitions.click();
        }
        issueActionsUtil.resolveIssue(trigger);
        waitForAjaxRefresh(tracer);
    }

    public ResolveIssueDialog openResolveIssue(ActionTrigger trigger)
    {
        if (trigger == ActionTrigger.MENU)
        {
            moreTransitions.click();
        }
        issueActionsUtil.invokeActionTrigger(IssueActions.RESOLVE_ISSUE, trigger);
        return pageBinder.bind(ResolveIssueDialog.class);
    }

    public void startProgress(ActionTrigger trigger)
    {
        if (trigger == ActionTrigger.MENU)
        {
            moreTransitions.click();
        }
        Tracer tracer = traceContext.checkpoint();
        issueActionsUtil.startProgress(trigger);
        waitForAjaxRefresh(tracer);
    }


    public void stopProgress(ActionTrigger trigger)
    {
        if (trigger == ActionTrigger.MENU)
        {
            moreTransitions.click();
        }
        Tracer tracer = traceContext.checkpoint();
        issueActionsUtil.stopProgress(trigger);
        waitForAjaxRefresh(tracer);
    }

    public void stopWatching(ActionTrigger trigger)
    {
        if (trigger == ActionTrigger.MENU)
        {
            moreActions.click();
        }
        issueActionsUtil.stopWatching(trigger);
        waitUntilTrue(pageElementFinder.find(By.className("watch-state-off")).timed().isPresent());
    }

    public void startWatching(ActionTrigger trigger)
    {
        if (trigger == ActionTrigger.MENU)
        {
            moreActions.click();
        }
        issueActionsUtil.startWatching(trigger);
        waitUntilTrue(pageElementFinder.find(By.className("watch-state-on")).timed().isPresent());
    }

    public void deleteIssue(ActionTrigger trigger)
    {
        if (trigger == ActionTrigger.MENU)
        {
            moreActions.click();
        }
        issueActionsUtil.delete(trigger);
    }

    public EditIssueDialog editIssue()
    {
        pageElementFinder.find(By.id("edit-issue")).click();
        return pageBinder.bind(EditIssueDialog.class);
    }

    public void assignIssue(String user, ActionTrigger trigger)
    {
        Tracer tracer = traceContext.checkpoint();
        issueActionsUtil.assignIssue(user, trigger);
        waitForAjaxRefresh(tracer);
    }

    public void addLabels(List<String> labels, ActionTrigger trigger)
    {
        Tracer tracer = traceContext.checkpoint();
        if (trigger == ActionTrigger.MENU)
        {
            moreActions.click();
        }
        issueActionsUtil.addLabels(labels, trigger);
        waitForAjaxRefresh(tracer);
    }


    public void addComment(String comment, ActionTrigger trigger)
    {
        Tracer tracer;
        switch (trigger)
        {
            case MENU:
                issueActionsUtil.addComment(comment, trigger, CommentType.DRAWER);
                break;
            case ACTIONS_DIALOG:
                tracer = traceContext.checkpoint();
                issueActionsUtil.addComment(comment, trigger, CommentType.DIALOG);
                waitForAjaxRefresh(tracer);
                break;
            case KEYBOARD_SHORTCUT:
                tracer = traceContext.checkpoint();
                issueActionsUtil.addComment(comment, trigger, CommentType.DRAWER);
                waitForAjaxRefresh(tracer);
                break;
        }
    }

    public void editIssue(Map<String, String> values, ActionTrigger trigger)
    {
        Tracer tracer = traceContext.checkpoint();
        issueActionsUtil.editIssue(values, trigger);
        waitForAjaxRefresh(tracer);
    }

    // TODO actions on the stalker bar should be encapsulated in a separate page object
    public ViewIssuePage assignIssueToMe()
    {
        pageElementFinder.find(By.id("assign-to-me")).click();
        // TODO: It seems the following would work, but wouldn't without the printlines. Cached element states?
//        final PageElement loading = pageElementFinder.find(By.className("jira-page-loading-indicator"), TimeoutType.UI_ACTION);
//        System.out.println(loading.getAttribute("style"));
//        Poller.waitUntilTrue(loading.timed().isVisible());
//        System.out.println(loading.getAttribute("style"));
//        Poller.waitUntilFalse(loading.timed().isVisible());
        return this;
    }

    public ViewIssuePage waitForAjaxRefresh(Tracer tracer)
    {
        traceContext.waitFor(tracer, "jira.issue.refreshed");
        return this;
    }

    public WatchersComponent openWatchersDialog()
    {
        watchers.click();
        return pageBinder.bind(WatchersComponent.class);
    }

    public TimedQuery getIssueDueDate()
    {
        return issueDueDate.find(By.tagName("time")).timed().getText();
    }

    public boolean isEditModalDialogVisibleFor(String fieldName)
    {
        PageElement editDialog = pageElementFinder.find(By.id("modal-field-view"));
        editDialog.timed().isPresent().byDefaultTimeout();
        return editDialog.find(By.tagName("h2")).getText().contains(fieldName);
    }

    public boolean isInlineEditVisibleFor(final long fieldId)
    {
        String idSelector = "#customfield_" + fieldId + "-val";
        PageElement editableFieldActive = pageElementFinder.find(By.cssSelector(idSelector + ".editable-field.active"));
        return editableFieldActive.timed().isPresent().byDefaultTimeout();
    }

    public String getAssignee()
    {
        return pageElementFinder.find(By.cssSelector("#assignee-val span")).getAttribute("rel").trim();
    }

    public VotersComponent openVotersDialog()
    {
        voters.click();
        return pageBinder.bind(VotersComponent.class);
    }

    public void voteForIssue()
    {
        Tracer tracer = traceContext.checkpoint();
        votingToggle.click();
        waitUntilTrue(pageElementFinder.find(By.className("vote-state-on")).timed().isPresent());
    }
    public void unvoteForIssue()
    {
        Tracer tracer = traceContext.checkpoint();
        votingToggle.click();
        waitUntilTrue(pageElementFinder.find(By.className("vote-state-off")).timed().isPresent());
    }

    public void expandHiddenComments()
    {
        Tracer tracer = traceContext.checkpoint();
        pageElementFinder.find(By.className("collapsed-comments")).click();
        traceContext.waitFor(tracer, "jira.issue.comment.expanded");
    }
}
