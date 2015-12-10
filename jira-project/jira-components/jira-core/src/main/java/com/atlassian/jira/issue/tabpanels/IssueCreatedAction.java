package com.atlassian.jira.issue.tabpanels;

import com.atlassian.core.util.StringUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueAction;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.atlassian.jira.template.TemplateSources.file;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

/**
 * This class is used when displaying change history in the View
 * Issue page, on the 'Change History' tab panel.
 */
@SuppressWarnings ( { "UnusedDeclaration" })
public class IssueCreatedAction extends AbstractIssueAction
{
    private static final String PLUGIN_TEMPLATES = "templates/plugins/";
    private static final Logger log = Logger.getLogger(IssueCreatedAction.class);

    private final DateTimeFormatter formatter;
    private final I18nHelper i18nHelper;
    private final UserFormatManager userFormatManager;
    private final AvatarService avatarService;
    private final JiraAuthenticationContext authenticationContext;
    private final Issue issue;

    public IssueCreatedAction(IssueTabPanelModuleDescriptor descriptor, DateTimeFormatter formatter,
            UserFormatManager userFormatManager, I18nHelper i18nHelper, final AvatarService avatarService, final JiraAuthenticationContext authenticationContext, Issue issue)
    {
        super(descriptor);
        this.formatter = formatter;
        this.i18nHelper = i18nHelper;
        this.userFormatManager = userFormatManager;
        this.avatarService = avatarService;
        this.authenticationContext = authenticationContext;
        this.issue = issue;
    }

    public Date getTimePerformed()
    {
        return getCreatedDate();
    }

    protected void populateVelocityParams(Map params)
    {
        params.put("issuecreatedaction", this);
        params.put("stringUtils", new StringUtils());
        params.put("userformat", userFormatManager);
        params.put("i18n",i18nHelper);
    }

    public String getHtml()
    {
        final String templateName = "jira/issuetabpanels/issuecreated.vm";
        final VelocityTemplatingEngine templatingEngine = ComponentAccessor.getComponent(VelocityTemplatingEngine.class);
        try
        {
            final Map<String, Object> params = Maps.newHashMapWithExpectedSize(4);
            populateVelocityParams(params);
            return templatingEngine.render(file(PLUGIN_TEMPLATES + templateName)).applying(params).asHtml();
        }
        catch (VelocityException e)
        {
            log.error("Error while rendering velocity template for '" + templateName + "'.", e);
            return "Velocity template generation failed.";
        }
    }


    //-------------------------------------------------------------------------------- Methods used by velocity template


    public Long getId()
    {
        return issue.getId();
    }

    public String getCreatorId()
    {
        return issue.getCreatorId();
    }

    public Timestamp getCreatedDate()
    {
        return issue.getCreated();
    }

    public String getCreatedDateHtml()
    {
        return escapeHtml(formatter.forLoggedInUser().format(getCreatedDate()));
    }

    public String getCreatedDateIso8601Html()
    {
        return escapeHtml(formatter.withStyle(DateTimeStyle.ISO_8601_DATE_TIME).format(getCreatedDate()));
    }

    public String getUserProfileHtml()
    {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("avatarURL", getAvatarURL(issue.getCreator()));
        if (issue.getCreatorId() == null)
        {
            parameters.put("username", i18nHelper.getText("creator.types.nocreator"));
        }
        return userFormatManager.formatUserkey(issue.getCreatorId(), "profileLinkActionHeader", "issuecreator_"+issue.getId(), parameters);
    }

    private String getAvatarURL(final User user)
    {
        ApplicationUser applicationUser = ApplicationUsers.from(user);
        return avatarService.getAvatarURL(authenticationContext.getUser(), applicationUser, Avatar.Size.NORMAL).toString();
    }
}
