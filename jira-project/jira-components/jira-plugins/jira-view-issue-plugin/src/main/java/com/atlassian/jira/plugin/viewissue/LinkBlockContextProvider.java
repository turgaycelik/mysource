package com.atlassian.jira.plugin.viewissue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.LinkCollection;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.link.RemoteIssueLinkManager;
import com.atlassian.jira.plugin.viewissue.issuelink.DefaultIssueLinkRenderer;
import com.atlassian.jira.plugin.viewissue.issuelink.IssueLinkContext;
import com.atlassian.jira.plugin.viewissue.issuelink.IssueLinkTypeContext;
import com.atlassian.jira.plugin.viewissue.issuelink.LocalIssueLinkUtils;
import com.atlassian.jira.plugin.viewissue.issuelink.RemoteIssueLinkComparator;
import com.atlassian.jira.plugin.viewissue.issuelink.RemoteIssueLinkUtils;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.Math.max;

/**
 * Context Provider for the Link block on the view issue page.
 *
 * @since v4.4
 */
public class LinkBlockContextProvider implements CacheableContextProvider
{
    private static final int DEFAULT_DISPLAYED_LINK_COUNT = 5;

    private final IssueLinkManager issueLinkManager;
    private final JiraAuthenticationContext authenticationContext;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final PermissionManager permissionManager;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final RemoteIssueLinkManager remoteIssueLinkManager;
    private final PluginAccessor pluginAccessor;

    public LinkBlockContextProvider(IssueLinkManager issueLinkManager, JiraAuthenticationContext authenticationContext,
            FieldVisibilityManager fieldVisibilityManager, PermissionManager permissionManager, VelocityRequestContextFactory velocityRequestContextFactory,
            RemoteIssueLinkManager remoteIssueLinkManager, PluginAccessor pluginAccessor)
    {
        this.issueLinkManager = issueLinkManager;
        this.authenticationContext = authenticationContext;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.permissionManager = permissionManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.remoteIssueLinkManager = remoteIssueLinkManager;
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final MapBuilder<String, Object> paramsBuilder = MapBuilder.newBuilder(context);

        final Issue issue = (Issue) context.get("issue");
        final User loggedInUser = authenticationContext.getLoggedInUser();
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        final String baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl();

        final Map<String, List<IssueLinkContext>> localRelationshipIssueLinkMap = getLocalIssueLinks(issue, loggedInUser, baseUrl);
        final Map<String, List<IssueLinkContext>> remoteRelationshipIssueLinkMap = getRemoteIssueLinks(issue, baseUrl, i18n);
        final List<IssueLinkTypeContext> issueLinkTypeContexts = sortLinkTypes(localRelationshipIssueLinkMap, remoteRelationshipIssueLinkMap);
        final int displayedLinkCount = getDisplayedLinkCount(velocityRequestContextFactory.getJiraVelocityRequestContext());
        final String hiddenLinkStats = calculateHiddenLinkStats(issueLinkTypeContexts, displayedLinkCount);

        paramsBuilder.addAll(velocityRequestContextFactory.getDefaultVelocityParams(MapBuilder.<String,Object>emptyMap(), authenticationContext));
        paramsBuilder.add("issueLinkTypeContexts", issueLinkTypeContexts);
        paramsBuilder.add("displayedLinkCount", displayedLinkCount);
        paramsBuilder.add("hiddenLinkStats", hiddenLinkStats);
        paramsBuilder.add("hasIssueLinks", !issueLinkTypeContexts.isEmpty());
        paramsBuilder.add("canLink", permissionManager.hasPermission(Permissions.LINK_ISSUE, issue, loggedInUser));
        paramsBuilder.add("defaultLinkIcon", baseUrl + DefaultIssueLinkRenderer.DEFAULT_ICON_URL);

        return paramsBuilder.toMap();
    }

    private List<IssueLinkTypeContext> sortLinkTypes(Map<String, List<IssueLinkContext>> localIssueLinks, Map<String, List<IssueLinkContext>> remoteIssueLinks)
    {
        List<IssueLinkTypeContext> issueLinkTypeContexts = Lists.newArrayList();

        Set<String> localRelationshipSet = localIssueLinks.keySet();
        List<String> localRelationships = Lists.newArrayList(localRelationshipSet);
        Collections.sort(localRelationships, String.CASE_INSENSITIVE_ORDER);

        for (String relationship : localRelationships)
        {
            List<IssueLinkContext> issueLinkContexts = Lists.newArrayList(localIssueLinks.get(relationship));
            if (remoteIssueLinks.containsKey(relationship))
            {
                issueLinkContexts.addAll(remoteIssueLinks.get(relationship));
            }
            issueLinkTypeContexts.add(new IssueLinkTypeContext(relationship, issueLinkContexts));
        }

        Set<String> remoteOnlyRelationshipSet = Sets.difference(remoteIssueLinks.keySet(), localRelationshipSet);
        List<String> remoteOnlyRelationships = Lists.newArrayList(remoteOnlyRelationshipSet);
        Collections.sort(remoteOnlyRelationships, String.CASE_INSENSITIVE_ORDER);

        for (String relationship : remoteOnlyRelationships)
        {
            issueLinkTypeContexts.add(new IssueLinkTypeContext(relationship, remoteIssueLinks.get(relationship)));
        }

        return issueLinkTypeContexts;
    }

    private int getDisplayedLinkCount(VelocityRequestContext requestContext)
    {
        if (Boolean.parseBoolean(requestContext.getRequestParameter("expandLinks")))
        {
            return Integer.MAX_VALUE;
        }
        else
        {
            return DEFAULT_DISPLAYED_LINK_COUNT;
        }
    }

    /**
     * Produces statistics for remote issue links that are initially hidden.
     *
     * Example output: "1 is duplicated by, 2 Blog Links, 2 Tickets"
     *
     * @param linkTypeContexts relationships containing links
     * @param displayedLinkCount how many links to display
     * @return statistics of initially hidden links
     */
    private String calculateHiddenLinkStats(List<IssueLinkTypeContext> linkTypeContexts, int displayedLinkCount)
    {
        final List<String> stats = Lists.newArrayListWithExpectedSize(linkTypeContexts.size());

        int remainingLinksToDisplay = displayedLinkCount;
        for (IssueLinkTypeContext linkTypeContext : linkTypeContexts)
        {
            final int linkCount = linkTypeContext.getIssueLinkContexts().size();
            if (linkCount > remainingLinksToDisplay)
            {
                int hiddenLinkCount = linkCount - remainingLinksToDisplay;
                stats.add(hiddenLinkCount + " " + linkTypeContext.getRelationship());
                remainingLinksToDisplay = 0;
            }
            else
            {
                remainingLinksToDisplay -= linkCount;
            }
        }

        return Joiner.on(", ").join(stats);
    }

    private Map<String, List<IssueLinkContext>> getRemoteIssueLinks(Issue issue, String baseUrl, I18nHelper i18n)
    {
        List<RemoteIssueLink> remoteIssueLinks = Lists.newArrayList(remoteIssueLinkManager.getRemoteIssueLinksForIssue(issue));
        Collections.sort(remoteIssueLinks, new RemoteIssueLinkComparator(i18n.getText(RemoteIssueLinkUtils.DEFAULT_RELATIONSHIP_I18N_KEY)));
        return RemoteIssueLinkUtils.convertToIssueLinkContexts(remoteIssueLinks, issue.getId(), baseUrl, i18n, pluginAccessor);
    }

    private Map<String, List<IssueLinkContext>> getLocalIssueLinks(Issue issue, User loggedInUser, String baseUrl)
    {
        final LinkCollection linkCollection = issueLinkManager.getLinkCollection(issue, loggedInUser);
        return LocalIssueLinkUtils.convertToIssueLinkContexts(linkCollection, issue.getId(), baseUrl, fieldVisibilityManager);
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        final Issue issue = (Issue) context.get("issue");
        final User user = authenticationContext.getLoggedInUser();

        return issue.getId() + "/" + (user == null ? "" : user.getName());
    }
}
