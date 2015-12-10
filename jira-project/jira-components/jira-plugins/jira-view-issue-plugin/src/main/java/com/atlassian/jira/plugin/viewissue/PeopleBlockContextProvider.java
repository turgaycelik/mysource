package com.atlassian.jira.plugin.viewissue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.fields.util.FieldPredicates;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugin.PluginParseException;
import org.apache.commons.lang.StringUtils;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Context Provider for the People Block pnn view issue
 *
 * @since v4.4
 */
public class PeopleBlockContextProvider implements CacheableContextProvider
{
    private final ApplicationProperties applicationProperties;
    private final AvatarService avatarService;
    private final JiraAuthenticationContext authenticationContext;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final IssueManager issueManager;
    private final PermissionManager permissionManager;
    private final UserFormatManager userFormatManager;
    private final VoteManager voteManager;
    private final WatcherManager watcherManager;

    public PeopleBlockContextProvider(
            final ApplicationProperties applicationProperties,
            final AvatarService avatarService,
            final JiraAuthenticationContext authenticationContext,
            final FieldScreenRendererFactory fieldScreenRendererFactory,
            final FieldVisibilityManager fieldVisibilityManager,
            final IssueManager issueManager,
            final PermissionManager permissionManager,
            final UserFormatManager userFormatManager,
            final VoteManager voteManager,
            final WatcherManager watcherManager)
    {
        this.applicationProperties = applicationProperties;
        this.avatarService = avatarService;
        this.authenticationContext = authenticationContext;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.issueManager = issueManager;
        this.permissionManager = permissionManager;
        this.userFormatManager = userFormatManager;
        this.voteManager = voteManager;
        this.watcherManager = watcherManager;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final Issue issue = (Issue) context.get("issue");
        final ApplicationUser user = authenticationContext.getUser();
        final Action action = (Action) context.get("action");

        final MapBuilder<String, Object> paramsBuilder = MapBuilder.newBuilder(context);

        paramsBuilder.add("issue", issue);
        paramsBuilder.add("user", user);
        paramsBuilder.add("peopleComponent", this);
        paramsBuilder.add("assigneeVisible", isAssigneeVisible(issue));
        paramsBuilder.add("reporterVisible", isReporterVisible(issue));
        paramsBuilder.add("showAssignToMe", showAssignToMe(user, issue));
        List<String> watchers = watcherManager.getCurrentWatcherUsernames(issue);
        paramsBuilder.add("watchers", watchers);
        paramsBuilder.add("watching", user != null && watchers.contains(user.getUsername()));
        paramsBuilder.add("voting", voteManager.hasVoted(user, issue));
        paramsBuilder.add("isResolved", issue.getResolutionObject() != null);
        final String reporterId = issue.getReporterId();
        paramsBuilder.add("isCurrentUserReporter", StringUtils.isNotBlank(reporterId) && user != null && reporterId.equals(user.getKey()));
        //need to be logged in in order to toggle watching/voting for an issue
        paramsBuilder.add("isLoggedIn", user != null);
        paramsBuilder.add("votingEnabled", applicationProperties.getOption(APKeys.JIRA_OPTION_VOTING));
        paramsBuilder.add("watchingEnabled", applicationProperties.getOption(APKeys.JIRA_OPTION_WATCHING));
        paramsBuilder.add("canManageWatcherList", permissionManager.hasPermission(Permissions.MANAGE_WATCHER_LIST, issue, user));
        paramsBuilder.add("canViewVotersAndWatchers", permissionManager.hasPermission(Permissions.VIEW_VOTERS_AND_WATCHERS, issue, user));
        paramsBuilder.add("userCustomFields", createUserFieldHelpers(issue, action));

        return paramsBuilder.toMap();
    }

    private boolean showAssignToMe(ApplicationUser currentUser, Issue issue)
    {
        return currentUser != null
                && !currentUser.getKey().equals(issue.getAssigneeId())
                && permissionManager.hasPermission(Permissions.ASSIGN_ISSUE, issue, currentUser)
                && permissionManager.hasPermission(Permissions.ASSIGNABLE_USER, issue, currentUser)
                && issueManager.isEditable(issue);
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        final Issue issue = (Issue)context.get("issue");
        final ApplicationUser user = authenticationContext.getUser();
        return issue.getId() + '/' + (user == null ? "" : user.getUsername());
    }

    private List<CustomFieldHelper> createUserFieldHelpers(final Issue issue, final Action action)
    {
        final FieldScreenRenderer screenRenderer = fieldScreenRendererFactory.getFieldScreenRenderer(issue, IssueOperations.VIEW_ISSUE_OPERATION, FieldPredicates.isCustomUserField());
        final List<FieldScreenRenderLayoutItem> fieldScreenRenderLayoutItems = screenRenderer.getAllScreenRenderItems();
        final List<CustomFieldHelper> userCustomFields = new ArrayList<CustomFieldHelper>();
        for (final FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderLayoutItems)
        {
            userCustomFields.add(new CustomFieldHelper(fieldScreenRenderLayoutItem, action, issue));
        }
        return userCustomFields;
    }

    private boolean isAssigneeVisible(Issue issue)
    {
        return isFieldVisible(issue, IssueFieldConstants.ASSIGNEE);
    }

    private boolean isReporterVisible(Issue issue)
    {
        return isFieldVisible(issue, IssueFieldConstants.REPORTER);
    }

    private boolean isFieldVisible(Issue issue, String field)
    {
        return !fieldVisibilityManager.isFieldHidden(issue.getProjectObject().getId(), field, issue.getIssueTypeObject().getId());
    }

    /**
     * Construct an HTML string to display an issue's assignee.
     *
     * @param issue The issue whose assignee is to be rendered.
     * @return an HTML string that displays {@code issue}'s assignee.
     */
    @SuppressWarnings ({ "UnusedDeclaration" })
    public String getAssigneeDisplayHtml(Issue issue)
    {
        if (issue == null)
        {
            return "";
        }

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("avatarURL", getAvatarURL(issue.getAssignee()));
        parameters.put("defaultFullName", authenticationContext.getI18nHelper().getText("common.status.unassigned"));

        return userFormatManager.formatUserkey(issue.getAssigneeId(), "avatarFullNameHover", "issue_summary_assignee", parameters);
    }

    /**
     * Construct an HTML string to display an issue's reporter.
     *
     * @param issue The issue whose reporter is to be rendered.
     * @return an HTML string that displays {@code issue}'s reporter.
     */
    @SuppressWarnings ({ "UnusedDeclaration" })
    public String getReporterDisplayHtml(Issue issue)
    {
        if (issue == null)
        {
            return "";
        }

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("avatarURL", getAvatarURL(issue.getReporter()));

        return userFormatManager.formatUserkey(issue.getReporterId(), "avatarFullNameHover", "issue_summary_reporter", parameters);
    }

    private String getAvatarURL(final User user)
    {
        ApplicationUser applicationUser = ApplicationUsers.from(user);
        return avatarService.getAvatarURL(authenticationContext.getUser(), applicationUser, Avatar.Size.NORMAL).toString();
    }

    /*
     * Simple helper class used by velocity to render user fields. Must be public so that velocity will access it.
     */
    public class CustomFieldHelper
    {
        private final FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem;
        private final Action action;
        private final Issue issue;

        private CustomFieldHelper(final FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem, final Action action, final Issue issue)
        {
            this.fieldScreenRenderLayoutItem = fieldScreenRenderLayoutItem;
            this.action = action;
            this.issue = issue;
        }

        public String getHtml()
        {
            return fieldScreenRenderLayoutItem.getViewHtml(action, null, issue);
        }

        public String getName()
        {
            return fieldScreenRenderLayoutItem.getOrderableField().getName();
        }

        public String getId()
        {
            return fieldScreenRenderLayoutItem.getOrderableField().getId();
        }

        public String getFieldType()
        {
            CustomField customField = (CustomField)fieldScreenRenderLayoutItem.getOrderableField();
            return customField.getCustomFieldType().getDescriptor().getKey();
        }

        public String getFieldTypeCompleteKey()
        {
            CustomField customField = (CustomField)fieldScreenRenderLayoutItem.getOrderableField();
            return customField.getCustomFieldType().getDescriptor().getCompleteKey();
        }
    }
}