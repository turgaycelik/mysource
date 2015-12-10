package com.atlassian.jira.user.profile;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.gadgets.GadgetRequestContextFactory;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.profile.ViewProfilePanel;
import com.atlassian.jira.plugin.profile.ViewProfilePanelModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.timezone.TimeZoneService;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.GroupPermissionChecker;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.VelocityParamFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User Profile Tab Panel for displaying a summary/overview of a user
 *
 * @since v4.1
 */
public class SummaryUserProfilePanel implements ViewProfilePanel
{
    private static final Logger log = Logger.getLogger(SummaryUserProfilePanel.class);

    private ViewProfilePanelModuleDescriptor moduleDescriptor;
    private final JiraAuthenticationContext context;
    private final SearchService searchService;
    private final I18nHelper.BeanFactory i18nFactory;
    private final FieldVisibilityManager fieldVisibiltyManager;
    private final ApplicationProperties applicationProperties;
    private final VelocityRequestContext requestContext;
    private final PlugableUserProfileFragment plugablePanel;
    private final ActivityUserProfileFragment activityUserProfileFragment;


    public SummaryUserProfilePanel(final EmailFormatter emailFormatter, final JiraAuthenticationContext context,
            final PermissionManager permissionManager, final GroupPermissionChecker groupPermissionChecker,
            final SearchService searchService, final VelocityRequestContextFactory velocityRequestContextFactory,
            final PluginAccessor pluginAccessor,
            final ApplicationProperties applicationProperties, VelocityTemplatingEngine templatingEngine, VelocityParamFactory velocityParamFactory,
            I18nBean.BeanFactory i18nFactory, WebResourceManager webResourceManager, ApplicationProperties applicationProperties1,
            final FieldVisibilityManager fieldVisibiltyManager, UserPreferencesManager preferencesManager,
            LocaleManager localeManager, final CrowdService crowdService, final UserPropertyManager userPropertyManager,
            final AvatarManager avatarManager, UserManager userManager, TimeZoneService timeZoneManager, AvatarService avatarService, WebInterfaceManager webInterfaceManager)
    {
        this.context = context;
        this.searchService = searchService;
        this.i18nFactory = i18nFactory;
        this.fieldVisibiltyManager = fieldVisibiltyManager;
        this.applicationProperties = applicationProperties1;
        requestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();

        activityUserProfileFragment = new ActivityUserProfileFragment(pluginAccessor, ComponentAccessor.getOSGiComponentInstanceOfType(GadgetRequestContextFactory.class), i18nFactory);
        plugablePanel = new PlugableUserProfileFragment(context, templatingEngine, velocityParamFactory, webInterfaceManager);
    }

    public void init(ViewProfilePanelModuleDescriptor moduleDescriptor)
    {
        this.moduleDescriptor = moduleDescriptor;
    }


    public String getHtml(User profileUser)
    {
        return moduleDescriptor.getHtml(VIEW_TEMPLATE, getParams(profileUser));
    }

    private Map<String, Object> getParams(User profileUser)
    {
        final User currentUser = context.getLoggedInUser();
        final Map<String, Object> params = new HashMap<String, Object>();

        params.put("filters", getFilters(profileUser, currentUser));
        params.put("profileUser", profileUser);
        params.put("currentUser", currentUser);

        params.put("activityStreamFrag", activityUserProfileFragment);
        params.put("plugablePanel", plugablePanel);

        return params;
    }

    private List<SimpleLink> getFilters(final User profileUser, final User currentUser)
    {
        final I18nHelper i18n = i18nFactory.getInstance(currentUser);

        boolean isResolutionAvailable = isResolutionFieldAvailable(currentUser);

        final List<SimpleLink> links = new ArrayList<SimpleLink>();
        links.add(getAssigned(profileUser, i18n));
        if (isResolutionAvailable)
        {
            links.add(getAssignedAndOpen(profileUser, i18n));
        }
        links.add(getInProgress(profileUser, i18n));
        links.add(getReported(profileUser, i18n));
        if (isResolutionAvailable)
        {
            links.add(getReportedAndOpen(profileUser, i18n));
        }

        if (profileUser.equals(currentUser))
        {
            if (isVotingEnabled())
            {
                links.add(getVoted(i18n));
                if (isResolutionAvailable)
                {
                    links.add(getVotedAndOpen(i18n));
                }
            }

            if (isWatchingEnabled())
            {
                links.add(getWatched(i18n));
                if (isResolutionAvailable)
                {
                    links.add(getWatchedAndOpen(i18n));
                }
            }
        }

        return links;
    }

    //
    // Methods for constructing the preset filters for a user
    //

    private SimpleLink getAssignedAndOpen(final User profileUser, final I18nHelper i18n)
    {
        JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder().unresolved();

        final User currentUser = context.getLoggedInUser();
        if (profileUser.equals(currentUser))
        {
            builder.and().assigneeIsCurrentUser();
        }
        else
        {
            builder.and().assignee().eq(profileUser.getName());
        }

        final String jql = searchService.getQueryString(context.getLoggedInUser(), builder.buildQuery());
        final String url = requestContext.getBaseUrl() + "/secure/IssueNavigator.jspa?reset=true" + jql;

        return new SimpleLinkImpl("ass_open", i18n.getText("userfilters.assigned.and.open"),
                i18n.getText("userfilters.assigned.and.open.desc"), null, null, null, url, null);
    }

    private SimpleLink getAssigned(final User profileUser, final I18nHelper i18n)
    {
        JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder();

        final User currentUser = context.getLoggedInUser();
        if (profileUser.equals(currentUser))
        {
            builder.assigneeIsCurrentUser();
        }
        else
        {
            builder.assignee().eq(profileUser.getName());
        }

        final String jql = searchService.getQueryString(context.getLoggedInUser(), builder.buildQuery());
        final String url = requestContext.getBaseUrl() + "/secure/IssueNavigator.jspa?reset=true" + jql;

        return new SimpleLinkImpl("ass", i18n.getText("userfilters.assigned"), i18n.getText("userfilters.assigned.desc"),
                null, null, null, url, null);
    }

    private SimpleLink getInProgress(final User profileUser, final I18nHelper i18n)
    {
        JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder().status("3");

        final User currentUser = context.getLoggedInUser();
        if (profileUser.equals(currentUser))
        {
            builder.and().assigneeIsCurrentUser();
        }
        else
        {
            builder.and().assignee().eq(profileUser.getName());
        }

        final String jql = searchService.getQueryString(context.getLoggedInUser(), builder.buildQuery());
        final String url = requestContext.getBaseUrl() + "/secure/IssueNavigator.jspa?reset=true" + jql;

        return new SimpleLinkImpl("ass_inprog", i18n.getText("userfilters.assigned.and.inprogress"),
                i18n.getText("userfilters.assigned.and.inprogress.desc"), null, null, null, url, null);
    }

    private SimpleLink getReportedAndOpen(final User profileUser, final I18nHelper i18n)
    {
        JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder().unresolved();

        final User currentUser = context.getLoggedInUser();
        if (profileUser.equals(currentUser))
        {
            builder.and().reporterIsCurrentUser();
        }
        else
        {
            builder.and().reporter().eq(profileUser.getName());
        }

        final String jql = searchService.getQueryString(context.getLoggedInUser(), builder.buildQuery());
        final String url = requestContext.getBaseUrl() + "/secure/IssueNavigator.jspa?reset=true" + jql;

        return new SimpleLinkImpl("rep_open", i18n.getText("userfilters.reported.and.open"),
                i18n.getText("userfilters.reported.and.open.desc"), null, null, null, url, null);
    }

    private SimpleLink getReported(final User profileUser, final I18nHelper i18n)
    {
        JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder();

        final User currentUser = context.getLoggedInUser();
        if (profileUser.equals(currentUser))
        {
            builder.reporterIsCurrentUser();
        }
        else
        {
            builder.reporter().eq(profileUser.getName());
        }

        final String jql = searchService.getQueryString(context.getLoggedInUser(), builder.buildQuery());
        final String url = requestContext.getBaseUrl() + "/secure/IssueNavigator.jspa?reset=true" + jql;

        return new SimpleLinkImpl("rep", i18n.getText("userfilters.reported"), i18n.getText("userfilters.reported.desc"),
                null, null, null, url, null);
    }

    private boolean isVotingEnabled()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_VOTING);
    }

    private boolean isWatchingEnabled()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_WATCHING);
    }

    private boolean isResolutionFieldAvailable(User user)
    {
        return fieldVisibiltyManager.isFieldVisible(user, IssueFieldConstants.RESOLUTION);
    }

    private SimpleLink getVoted(final I18nHelper i18n)
    {
        JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder().issueInVotedIssues();

        final String jql = searchService.getQueryString(context.getLoggedInUser(), builder.buildQuery());
        final String url = requestContext.getBaseUrl() + "/secure/IssueNavigator.jspa?reset=true" + jql;

        return new SimpleLinkImpl("voted", i18n.getText("userfilters.voted"), i18n.getText("userfilters.voted.desc"),
                null, null, null, url, null);
    }

    private SimpleLink getVotedAndOpen(final I18nHelper i18n)
    {
        JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder().issueInVotedIssues().and().unresolved();

        final String jql = searchService.getQueryString(context.getLoggedInUser(), builder.buildQuery());
        final String url = requestContext.getBaseUrl() + "/secure/IssueNavigator.jspa?reset=true" + jql;

        return new SimpleLinkImpl("voted_open", i18n.getText("userfilters.voted.and.open"),
                i18n.getText("userfilters.voted.and.open.desc"), null, null, null, url, null);
    }

    private SimpleLink getWatched(final I18nHelper i18n)
    {
        JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder().issueInWatchedIssues();

        final String jql = searchService.getQueryString(context.getLoggedInUser(), builder.buildQuery());
        final String url = requestContext.getBaseUrl() + "/secure/IssueNavigator.jspa?reset=true" + jql;

        return new SimpleLinkImpl("watched", i18n.getText("userfilters.watched"),
                i18n.getText("userfilters.watched.desc"), null, null, null, url, null);
    }

    private SimpleLink getWatchedAndOpen(final I18nHelper i18n)
    {
        JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder().issueInWatchedIssues().and().unresolved();

        final String jql = searchService.getQueryString(context.getLoggedInUser(), builder.buildQuery());
        final String url = requestContext.getBaseUrl() + "/secure/IssueNavigator.jspa?reset=true" + jql;

        return new SimpleLinkImpl("watched_open", i18n.getText("userfilters.watched.and.open"),
                i18n.getText("userfilters.watched.and.open.desc"), null, null, null, url, null);
    }

}
