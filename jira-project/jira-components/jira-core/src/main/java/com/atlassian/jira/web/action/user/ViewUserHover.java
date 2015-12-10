package com.atlassian.jira.web.action.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.timezone.TimeZoneInfo;
import com.atlassian.jira.timezone.TimeZoneService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.query.Query;
import com.atlassian.util.concurrent.LazyReference;
import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import webwork.action.ServletActionContext;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.joda.time.DateTimeConstants.SATURDAY;
import static org.joda.time.DateTimeConstants.SUNDAY;

/**
 * Displays the contents of the user hover dialog.
 *
 * @since 4.2
 */
@SuppressWarnings ( { "UnusedDeclaration" })
public class ViewUserHover extends JiraWebActionSupport
{
    static private final ImmutableList<Integer> WEEKEND = ImmutableList.of(SATURDAY, SUNDAY);

    private String username;
    private ApplicationUser user;
    private URI avatarUrl;

    private final UserManager userManager;
    private final EmailFormatter emailFormatter;
    private final SearchService searchService;
    private final PermissionManager permissionManager;
    private final SimpleLinkManager simpleLinkManager;
    private final DateTimeFormatterFactory dateTimeFormatterFactory;
    private final TimeZoneService timeZoneService;
    @ClusterSafe("WebWork actions are ephemeral")
    private final LazyReference<SimpleDateFormat> hourOfDayFormatter = new HourOfDayFormatterRef();
    private final AvatarService avatarService;

    public ViewUserHover(final UserManager userManager, final EmailFormatter emailFormatter,
            final SearchService searchService, final PermissionManager permissionManager,
            final SimpleLinkManager simpleLinkManager,
            final DateTimeFormatterFactory dateTimeFormatterFactory,
            final TimeZoneService timeZoneService,
            final AvatarService avatarService)
    {
        this.userManager = userManager;
        this.emailFormatter = emailFormatter;
        this.searchService = searchService;
        this.permissionManager = permissionManager;
        this.simpleLinkManager = simpleLinkManager;
        this.dateTimeFormatterFactory = dateTimeFormatterFactory;
        this.timeZoneService = timeZoneService;
        this.avatarService = avatarService;
    }

    @Override
    public String doDefault() throws Exception
    {
        avatarUrl = avatarService.getAvatarURL(getLoggedInApplicationUser(), user, Avatar.Size.LARGE);
        return SUCCESS;
    }

    public ApplicationUser getUser()
    {
        return user;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(final String username)
    {
        this.username = username;
        this.user = userManager.getUserByName(username);
    }

    public String getAvatarUrl()
    {
        return avatarUrl.toString();
    }

    public String getAssigneeQuery()
    {
        final Query query = JqlQueryBuilder.newClauseBuilder()
                .assignee().eq(username)
                .and().resolution().isEmpty()
                .buildQuery();
        return searchService.getQueryString(getLoggedInUser(), query);
    }

    public String getFormattedEmail()
    {
        if (user != null)
        {
            return emailFormatter.formatEmailAsLink(user.getEmailAddress(), getLoggedInUser());
        }
        return "";
    }

    public String getTime()
    {
        return dateTimeFormatterFactory.formatter().forLoggedInUser().withStyle(DateTimeStyle.TIME).withZone(getUserTimeZone()).format(new Date());
    }

    public String getDayOfWeek()
    {
        SimpleDateFormat df = new SimpleDateFormat("EEEE", getLocale());
        df.setTimeZone(getUserTimeZone());

        return df.format(new Date());
    }

    /**
     * Returns the hour of day in the current user's time zone, in 24-hour format.
     *
     * @return the hour of day in the current user's time zone
     */
    public String getHourOfDay()
    {
        return hourOfDayFormatter.get().format(new Date());
    }

    @Nullable
    public Boolean getIsWeekend()
    {
        return WEEKEND.contains(new DateTime(new Date(), DateTimeZone.forTimeZone(getUserTimeZone())).getDayOfWeek()) ? Boolean.TRUE : null;
    }

    public boolean hasViewUserPermission()
    {
        //check if the user is allowed to view user profiles and if they can view issues!
        try
        {
            return permissionManager.hasPermission(Permissions.USE, getLoggedInUser()) &&
                    permissionManager.hasProjects(Permissions.BROWSE, getLoggedInUser());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public SimpleLink getFirstHoverLink()
    {
        final List<SimpleLink> simpleLinkList = getHoverLinks();
        if (simpleLinkList.isEmpty())
        {
            return null;
        }
        return simpleLinkList.get(0);
    }

    public List<SimpleLink> getRemainingLinks()
    {
        final List<SimpleLink> simpleLinkList = getHoverLinks();
        if (simpleLinkList.isEmpty())
        {
            return Collections.emptyList();
        }
        return simpleLinkList.subList(1, simpleLinkList.size());
    }

    public List<SimpleLink> getHoverLinks()
    {
        if (user != null) {
            final User remoteUser = getLoggedInUser();
            final HttpServletRequest servletRequest = ServletActionContext.getRequest();

            final Map<String, Object> params = MapBuilder.<String, Object>newBuilder().
                    add("profileUser", getUsername()).
                    add("jqlquery", getAssigneeQuery()).toMap();

            final JiraHelper helper = new JiraHelper(servletRequest, null, params);

            return simpleLinkManager.getLinksForSection("system.user.hover.links", remoteUser, helper);
        }

        return Collections.emptyList();
    }

    // If the logged in user is viewing his/her own avatar and they haven't set an avatar yet, return true.
    public boolean isShowUploadAvatarLink()
    {
        final ApplicationUser currentUser = getLoggedInApplicationUser();
        boolean isUserViewingOwnAvatar = user != null && user.equals(currentUser);
        return isUserViewingOwnAvatar
                && avatarService.canSetCustomUserAvatar(currentUser, user)
                && !avatarService.hasCustomUserAvatar(currentUser, user);
    }

    public String getTimeZoneCity()
    {
        return getUserTimeZoneInfo().getCity();
    }

    TimeZone getUserTimeZone()
    {
        return getUserTimeZoneInfo().toTimeZone();
    }

    private TimeZoneInfo getUserTimeZoneInfo()
    {
        return timeZoneService.getUserTimeZoneInfo(new JiraServiceContextImpl(user));
    }

    private class HourOfDayFormatterRef extends LazyReference<SimpleDateFormat>
    {
        @Override
        protected SimpleDateFormat create() throws Exception
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH");
            dateFormat.setTimeZone(getUserTimeZone());
            return dateFormat;
        }
    }
}

