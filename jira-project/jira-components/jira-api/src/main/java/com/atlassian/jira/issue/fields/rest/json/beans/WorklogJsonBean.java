package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.core.util.DateUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.rest.Dates;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.JiraDurationUtils;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

/**
 * @since v4.2
 */
@XmlRootElement (name="worklog")
public class WorklogJsonBean
{
    @XmlElement
    private URI self;

    @XmlElement
    private UserJsonBean author;

    @XmlElement
    private UserJsonBean updateAuthor;

    @XmlElement
    private String comment;

    @XmlElement
    private String created;

    @XmlElement
    private String updated;

    @XmlElement
    private VisibilityJsonBean visibility;

    @JsonIgnore
    private boolean isVisibilitySet;

    @XmlElement
    private String started;

    @XmlElement
    private String timeSpent;

    @XmlElement
    private Long timeSpentSeconds;

    @XmlElement
    private String id;

    @Deprecated
    public static List<WorklogJsonBean> asBeans(final List<Worklog> worklogs, final JiraBaseUrls uriInfo, final UserManager userManager, final TimeTrackingConfiguration timeTrackingConfiguration)
    {
        return asBeans(worklogs, uriInfo, userManager, timeTrackingConfiguration, ComponentAccessor.getComponent(JiraAuthenticationContext.class).getUser(), ComponentAccessor.getComponent(EmailFormatter.class));
    }

    public static List<WorklogJsonBean> asBeans(final List<Worklog> worklogs, final JiraBaseUrls uriInfo, final UserManager userManager, final TimeTrackingConfiguration timeTrackingConfiguration, final ApplicationUser loggedInUser, final EmailFormatter emailFormatter)
    {
        List<WorklogJsonBean> result = Lists.newArrayListWithCapacity(worklogs.size());
        for (Worklog from : worklogs)
        {
            result.add(getWorklog(from, uriInfo, userManager, timeTrackingConfiguration, loggedInUser, emailFormatter));
        }

        return result;
    }

    @Deprecated
    public static WorklogJsonBean getWorklog(final Worklog log, final JiraBaseUrls baseUrls, final UserManager userManager, final TimeTrackingConfiguration timeTrackingConfiguration)
    {
        return getWorklog(log, baseUrls, userManager, timeTrackingConfiguration, ComponentAccessor.getComponent(JiraAuthenticationContext.class).getUser(), ComponentAccessor.getComponent(EmailFormatter.class));
    }

    public static WorklogJsonBean getWorklog(final Worklog log, final JiraBaseUrls baseUrls, final UserManager userManager, final TimeTrackingConfiguration timeTrackingConfiguration, final ApplicationUser loggedInUser, final EmailFormatter emailFormatter)
    {
        final WorklogJsonBean bean = new WorklogJsonBean();
        addNonRenderableData(bean, log, baseUrls, userManager, loggedInUser, emailFormatter);
        bean.comment = log.getComment();
        bean.timeSpent = getTimeLoggedString(log.getTimeSpent(), timeTrackingConfiguration);
        bean.timeSpentSeconds = log.getTimeSpent();
        bean.created = Dates.asTimeString(log.getCreated());
        bean.updated = Dates.asTimeString(log.getUpdated());
        bean.started = Dates.asTimeString(log.getStartDate());
        return bean;
    }

    private static void addNonRenderableData(WorklogJsonBean bean, final Worklog log, final JiraBaseUrls baseUrls, final UserManager userManager, final ApplicationUser loggedInUser, final EmailFormatter emailFormatter)
    {
        try
        {
            bean.self = new URI(baseUrls.restApi2BaseUrl() + "issue/" + log.getIssue().getId().toString() + "/worklog/" + log.getId().toString());
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException("Failed to generate worklog self url", e);
        }
        bean.author = getUserBean(baseUrls, log.getAuthor(), userManager, loggedInUser, emailFormatter);
        bean.updateAuthor = getUserBean(baseUrls, log.getUpdateAuthor(), userManager, loggedInUser, emailFormatter);
        bean.id = Long.toString(log.getId());

        final String groupLevel = log.getGroupLevel();
        final ProjectRole roleLevel = log.getRoleLevel();
        if (groupLevel != null)
        {
            bean.visibility = new VisibilityJsonBean(VisibilityJsonBean.VisibilityType.group, groupLevel);
        }
        else if (roleLevel != null)
        {
            bean.visibility = new VisibilityJsonBean(VisibilityJsonBean.VisibilityType.role, roleLevel.getName());
        }
    }

    @Deprecated
    public static List<WorklogJsonBean> asRenderedBeans(final List<Worklog> worklogs, final JiraBaseUrls uriInfo,
            final String rendererType, final IssueRenderContext renderContext)
    {
        return asRenderedBeans(worklogs, uriInfo, rendererType, renderContext, ComponentAccessor.getComponent(JiraAuthenticationContext.class).getUser(), ComponentAccessor.getComponent(EmailFormatter.class));
    }

    public static List<WorklogJsonBean> asRenderedBeans(final List<Worklog> worklogs, final JiraBaseUrls uriInfo,
             final String rendererType, final IssueRenderContext renderContext, final ApplicationUser loggedInUser, final EmailFormatter emailFormatter)
    {
        List<WorklogJsonBean> result = Lists.newArrayListWithCapacity(worklogs.size());
        for (Worklog worklog : worklogs)
        {
            result.add(getRenderedWorklog(worklog, uriInfo, rendererType, renderContext, loggedInUser, emailFormatter));
        }

        return result;
    }

    @Deprecated
    public static WorklogJsonBean getRenderedWorklog(final Worklog log, final JiraBaseUrls baseUrls,
            String rendererType, IssueRenderContext renderContext)
    {
        return getRenderedWorklog(log, baseUrls, rendererType, renderContext, ComponentAccessor.getComponent(JiraAuthenticationContext.class).getUser(), ComponentAccessor.getComponent(EmailFormatter.class));
    }

    public static WorklogJsonBean getRenderedWorklog(final Worklog log, final JiraBaseUrls baseUrls,
             String rendererType, IssueRenderContext renderContext, final ApplicationUser loggedInUser, final EmailFormatter emailFormatter)
    {
        final WorklogJsonBean bean = new WorklogJsonBean();
        addNonRenderableData(bean, log, baseUrls, ComponentAccessor.getUserManager(), loggedInUser, emailFormatter);
        if (StringUtils.isNotBlank(rendererType))
        {
             RendererManager rendererManager = ComponentAccessor.getComponent(RendererManager.class);
             bean.comment = rendererManager.getRenderedContent(rendererType, log.getComment(), renderContext);
        }
        else
        {
              bean.comment = log.getComment();
        }

        JiraDurationUtils jiraDurationUtils = ComponentAccessor.getComponent(JiraDurationUtils.class);
        bean.timeSpent = jiraDurationUtils.getFormattedDuration(log.getTimeSpent(), ComponentAccessor.getJiraAuthenticationContext().getLocale());

        DateTimeFormatterFactory dateTimeFormatterFactory = ComponentAccessor.getComponent(DateTimeFormatterFactory.class);
        bean.created = log.getCreated() == null ? "" : dateTimeFormatterFactory.formatter().forLoggedInUser().format(log.getCreated());
        bean.updated = log.getUpdated() == null ? "" : dateTimeFormatterFactory.formatter().forLoggedInUser().format(log.getUpdated());
        bean.started = log.getStartDate() == null ? "" : dateTimeFormatterFactory.formatter().forLoggedInUser().format(log.getStartDate());
        return bean;
    }

    /**
     * Returns a UserBean for the user with the given name. If the user does not exist, the returned bean contains only
     * the username and no more info.
     *
     * @param uriInfo a UriInfo
     * @param username a String containing a user name
     * @param userManager Manager for users
     * @param emailFormatter
     * @param loggedInUser
     * @return a UserBean
     */
    protected static UserJsonBean getUserBean(final JiraBaseUrls uriInfo, String username, final UserManager userManager, final ApplicationUser loggedInUser, final EmailFormatter emailFormatter)
    {
        User user = userManager.getUser(username);
        if (user != null)
        {
            return UserJsonBean.shortBean(user, uriInfo, loggedInUser, emailFormatter);
        }
        else if (StringUtils.isNotBlank(username))
        {
            UserJsonBean userJsonBean = new UserJsonBean();
            userJsonBean.setName(username);
            return userJsonBean;
        }
        else
        {
            return null;
        }

    }

    public static final WorklogJsonBean DOC_EXAMPLE;
    static {
        try
        {
            DOC_EXAMPLE = new WorklogJsonBean();
            DOC_EXAMPLE.self = new URI("http://www.example.com/jira/rest/api/2/issue/10010/worklog/10000");
            DOC_EXAMPLE.author = UserJsonBean.USER_SHORT_DOC_EXAMPLE;
            DOC_EXAMPLE.updateAuthor = UserJsonBean.USER_SHORT_DOC_EXAMPLE;
            DOC_EXAMPLE.comment = "I did some work here.";
            DOC_EXAMPLE.visibility = new VisibilityJsonBean(VisibilityJsonBean.VisibilityType.group, "jira-developers");
            DOC_EXAMPLE.started = Dates.asTimeString(new Date());
            DOC_EXAMPLE.timeSpent = "3h 20m";
            DOC_EXAMPLE.timeSpentSeconds = 12000L;
            DOC_EXAMPLE.id = "100028";
        }
        catch (URISyntaxException impossible)
        {
            throw new RuntimeException(impossible);
        }
    }

    private static String getTimeLoggedString(long timeSpent, TimeTrackingConfiguration timeTrackingConfiguration)
    {
        final BigDecimal hoursPerDay = timeTrackingConfiguration.getHoursPerDay();
        final BigDecimal daysPerWeek = timeTrackingConfiguration.getDaysPerWeek();
        final BigDecimal secondsPerHour = BigDecimal.valueOf(DateUtils.Duration.HOUR.getSeconds());
        final long secondsPerDay = hoursPerDay.multiply(secondsPerHour).longValueExact();
        final long secondsPerWeek = daysPerWeek.multiply(hoursPerDay).multiply(secondsPerHour).longValueExact();
        return DateUtils.getDurationStringSeconds(timeSpent, secondsPerDay, secondsPerWeek);
    }

    public UserJsonBean getAuthor()
    {
        return author;
    }

    public UserJsonBean getUpdateAuthor()
    {
        return updateAuthor;
    }

    public String getComment()
    {
        return comment;
    }

    public Date getCreated()
    {
        return Dates.fromTimeString(created);
    }

    public Date getUpdated()
    {
        return Dates.fromTimeString(updated);
    }

    public Date getStarted()
    {
        return Dates.fromTimeString(started);
    }

    public String getTimeSpent()
    {
        return timeSpent;
    }

    public Long getTimeSpentSeconds()
    {
        return timeSpentSeconds;
    }

    @JsonProperty
    public VisibilityJsonBean getVisibility()
    {
        return visibility;
    }

    @JsonProperty
    public void setVisibility(VisibilityJsonBean visibility)
    {
        this.isVisibilitySet = true;
        this.visibility = visibility;
    }

    @JsonIgnore
    public boolean isVisibilitySet()
    {
        return isVisibilitySet;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public URI getSelf()
    {
        return self;
    }
}
