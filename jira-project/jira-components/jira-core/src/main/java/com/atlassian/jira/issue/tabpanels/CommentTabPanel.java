package com.atlassian.jira.issue.tabpanels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.bc.admin.ApplicationPropertiesService;
import com.atlassian.jira.bc.admin.ApplicationProperty;
import com.atlassian.jira.bc.issue.comment.property.CommentPropertyService;
import com.atlassian.jira.config.CoreFeatures;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.action.IssueActionComparator;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.comments.CommentPermissionManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.renderer.comment.CommentFieldRenderer;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel3;
import com.atlassian.jira.plugin.issuetabpanel.GetActionsRequest;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.plugin.issuetabpanel.ShowPanelRequest;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.soy.SoyTemplateRendererProvider;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;

public class CommentTabPanel extends AbstractIssueTabPanel3
{
    private final ApplicationPropertiesService applicationPropertiesService;
    private final CommentManager commentManager;
    private final CommentPermissionManager commentPermissionManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final RendererManager rendererManager;
    private final IssueManager issueManager;
    private final DateTimeFormatter dateTimeFormatter;
    private final SoyTemplateRenderer soyTemplateRenderer;
    private final FeatureManager featureManager;
    private final CommentFieldRenderer commentFieldRenderer;
    private final CommentPropertyService commentPropertyService;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    private static final int NUMBER_OF_OLD_COMMENTS_TO_SHOW = 1;
    private static final int DEFAULT_MINIMUM_NUMBER_OF_HIDDEN_COMMENTS = 4;
    private static final int NUMBER_OF_NEW_COMMENTS_TO_SHOW = 5;
    private static final int NUMBER_OF_COMMENTS_TO_SHOW = NUMBER_OF_NEW_COMMENTS_TO_SHOW + NUMBER_OF_OLD_COMMENTS_TO_SHOW;

    public CommentTabPanel(final ApplicationPropertiesService applicationPropertiesService, final CommentManager commentManager,
            final CommentPermissionManager commentPermissionManager,
            final IssueManager issueManager, final FieldLayoutManager fieldLayoutManager,
            final RendererManager rendererManager, DateTimeFormatter dateTimeFormatter,
            final SoyTemplateRendererProvider soyTemplateRendererProvider, final FeatureManager featureManager,
            final CommentFieldRenderer commentFieldRenderer, final CommentPropertyService commentPropertyService,
            final JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.applicationPropertiesService = applicationPropertiesService;
        this.commentManager = commentManager;
        this.commentPermissionManager = commentPermissionManager;
        this.issueManager = issueManager;
        this.fieldLayoutManager = fieldLayoutManager;
        this.rendererManager = rendererManager;
        this.dateTimeFormatter = dateTimeFormatter;
        this.commentFieldRenderer = commentFieldRenderer;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.soyTemplateRenderer = soyTemplateRendererProvider.getRenderer();
        this.featureManager = featureManager;
        this.commentPropertyService = commentPropertyService;
    }

    @Override
    public boolean showPanel(final ShowPanelRequest request)
    {
        return true;
    }

    @Override
    public List<IssueAction> getActions(final GetActionsRequest request)
    {
        final List<IssueAction> commentActions = getAllComments(request);

        // This is a bit of a hack to indicate that there are no comments to display
        if (commentActions.isEmpty())
        {
            IssueAction action = new GenericMessageAction(descriptor.getI18nBean().getText("viewissue.nocomments"));
            return Collections.singletonList(action);
        }

        // TODO: We should retrieve them sorted correctly in the first place.
        Collections.sort(commentActions, IssueActionComparator.COMPARATOR);

        return limitComments(request, commentActions);
    }

    private List<IssueAction> getAllComments(final GetActionsRequest request)
    {
        final Issue issue = request.issue();
        final ApplicationUser user = request.loggedInUser();

        // Get the list of Comment objects for the given Issue that this user can see.
        final Collection<Comment> userComments = commentManager.getCommentsForUser(issue, user);
        // We need to turn these into CommentAction objects for display on the web.
        final List<IssueAction> commentActions = new ArrayList<IssueAction>();

        // We want to do these checks ONCE. Doing them for each iteration of the loop below is very inefficient.
        boolean issueIsInEditableWorkflow = issueManager.isEditable(issue);
        // Allow issueIsInEditableWorkflow shortcut this check
        boolean canDeleteAllComments = issueIsInEditableWorkflow && commentPermissionManager.hasDeleteAllPermission(user, issue);
        // If you can delete all comments, then you can delete your own comments, and we can shortcut this check
        boolean canDeleteOwnComments = canDeleteAllComments ||
                (issueIsInEditableWorkflow && commentPermissionManager.hasDeleteOwnPermission(user, issue));
        boolean canEditAllComments = issueIsInEditableWorkflow && commentPermissionManager.hasEditAllPermission(user, issue);
        boolean canEditOwnComments = canEditAllComments ||
                (issueIsInEditableWorkflow && commentPermissionManager.hasEditOwnPermission(user, issue));

        for (final Comment comment : userComments)
        {
            boolean canDelete = canDeleteAllComments || (canDeleteOwnComments && commentManager.isUserCommentAuthor(user, comment));
            boolean canEdit = canEditAllComments || (canEditOwnComments && commentManager.isUserCommentAuthor(user, comment));
            commentActions.add(new CommentAction(descriptor, comment, canEdit, canDelete, false, rendererManager,
                    fieldLayoutManager, dateTimeFormatter, commentFieldRenderer, commentPropertyService, jiraAuthenticationContext));
        }

        return commentActions;
    }

    private List<IssueAction> limitComments(final GetActionsRequest request, final List<IssueAction> commentActions)
    {
        ApplicationProperty applicationProperty = applicationPropertiesService.getApplicationProperty(APKeys.COMMENT_COLLAPSING_MINIMUM_HIDDEN);
        Integer minimumCommentsToHide = (applicationProperty != null) ? Integer.parseInt(applicationProperty.getCurrentValue())
                                                                      : DEFAULT_MINIMUM_NUMBER_OF_HIDDEN_COMMENTS;

        boolean limitComments = true;

        if ((featureManager.isEnabled(CoreFeatures.PREVENT_COMMENTS_LIMITING))
            || (request.isShowAll())
            || (minimumCommentsToHide == 0)
            || (commentActions.size() - NUMBER_OF_COMMENTS_TO_SHOW < minimumCommentsToHide)
            || focusedIssueWouldBeHidden(commentActions, request.getFocusId()))
        {
            limitComments = false;
        }

        if (limitComments)
        {
            try
            {
                int numberHiddenComments = commentActions.size() - NUMBER_OF_COMMENTS_TO_SHOW;
                String showOlder = soyTemplateRenderer.render("jira.webresources:soy-templates", "JIRA.Templates.IssueTabPanels.Comment.showOlder",
                        MapBuilder.<String, Object>build("issueKey", request.issue().getKey(), "numberHiddenComments", numberHiddenComments));

                final List<IssueAction> limitedCommentActions = new ArrayList<IssueAction>();

                for (int i = 0; i < NUMBER_OF_OLD_COMMENTS_TO_SHOW; i++)
                {
                    limitedCommentActions.add(getCollapsedCommentAction((CommentAction)commentActions.get(i)));
                }

                limitedCommentActions.add(new GenericMessageAction(showOlder));

                limitedCommentActions.addAll(commentActions.subList(commentActions.size() - NUMBER_OF_NEW_COMMENTS_TO_SHOW, commentActions.size()));

                return limitedCommentActions;
            }
            catch (SoyException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            return commentActions;
        }
    }

    /**
     * Determines whether the focused issue would be in the set of hidden comments.
     * @param commentActions list of comments
     * @param focusId id of the focused comment
     * @return whether comment would be hidden if collapsed
     */
    private boolean focusedIssueWouldBeHidden(final List<IssueAction> commentActions, String focusId)
    {
        if (focusId != null)
        {
            for (IssueAction comment : commentActions.subList(NUMBER_OF_OLD_COMMENTS_TO_SHOW, commentActions.size() - NUMBER_OF_NEW_COMMENTS_TO_SHOW))
            {
                if (((CommentAction)comment).getComment().getId().equals(Long.valueOf(focusId)))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param commentAction
     * @return a collapsed copy of a CommentAction
     */
    private CommentAction getCollapsedCommentAction(CommentAction commentAction)
    {
        return new CommentAction(descriptor,
                commentAction.getComment(),
                commentAction.isCanEditComment(),
                commentAction.isCanDeleteComment(),
                true,
                rendererManager,
                fieldLayoutManager,
                dateTimeFormatter,
                commentFieldRenderer,
                commentPropertyService,
                jiraAuthenticationContext);
    }
}
