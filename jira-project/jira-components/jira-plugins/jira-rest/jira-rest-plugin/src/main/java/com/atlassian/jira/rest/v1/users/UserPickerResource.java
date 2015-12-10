package com.atlassian.jira.rest.v1.users;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.bc.user.search.UserSearchParams;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserFilter;
import com.atlassian.jira.user.UserFilterManager;
import com.atlassian.jira.util.DelimeterInserter;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import com.google.common.collect.Sets;
import com.opensymphony.util.TextUtils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

/**
 * REST end point for searching users in the user picker.
 *
 * @since v4.0
 */
@Deprecated
@Path("users/picker")
@AnonymousAllowed
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class UserPickerResource
{
    private static final Logger log = Logger.getLogger(UserPickerResource.class);

    private final JiraAuthenticationContext authContext;
    private final UserPickerSearchService service;
    private final ApplicationProperties applicationProperties;
    private final AvatarService avatarService;
    private final I18nHelper i18nHelper;
    private final UserFilterManager userFilterManager;
    private final FieldConfigManager fieldConfigManager;

    public UserPickerResource(JiraAuthenticationContext authContext, I18nHelper i18nHelper,
            UserPickerSearchService service, ApplicationProperties applicationProperties,
            AvatarService avatarService, UserFilterManager userFilterManager,
            FieldConfigManager fieldConfigManager)
    {
        this.authContext = authContext;
        this.service = service;
        this.applicationProperties = applicationProperties;
        this.avatarService = avatarService;
        this.i18nHelper = i18nHelper;
        this.userFilterManager = userFilterManager;
        this.fieldConfigManager = fieldConfigManager;
    }

    @GET
    public Response getUsersResponse(@QueryParam("fieldName") final String fieldName,
                                     @QueryParam("fieldConfigId") final String fieldConfigId,
                                     @QueryParam("projectId") final List<String> projectIdList,
                                     @QueryParam("query") final String query,
                                     @QueryParam("showAvatar") final boolean showAvatar,
                                     @QueryParam("exclude") final List<String> excludeUsers)
    {
        return Response.ok(getUsers(fieldName, fieldConfigId, projectIdList, query, showAvatar, excludeUsers)).cacheControl(NO_CACHE).build();
    }

    UserPickerResultsWrapper getUsers(final String fieldName, final String fieldConfigId, final List<String> projectIdList, final String query,
            final boolean showAvatar, List<String> excludeUsers)
    {
        final JiraServiceContext jiraServiceCtx = getContext();
        final UserPickerResultsWrapper results = new UserPickerResultsWrapper();

        if (excludeUsers == null) {
            excludeUsers = new ArrayList<String>();
        }
        
        if (!service.canPerformAjaxSearch(jiraServiceCtx))
            return results;

        final boolean canShowEmailAddresses = service.canShowEmailAddresses(jiraServiceCtx);
        final Collection<User> users;
        final UserSearchParams.Builder paramBuilder = UserSearchParams.builder().allowEmptyQuery(false).includeActive(true).includeInactive(false);

        // support user filtering by groups and roles
        final boolean isCustomField = (fieldName != null && fieldName.startsWith(FieldManager.CUSTOM_FIELD_PREFIX));
        if (isCustomField && fieldConfigId != null)
        {
            // if it's a custom field, we expect to also receive the fieldConfigId to properly infer the fieldConfig
            final UserFilter filter = getUserFilter(fieldConfigId, fieldName);
            if (filter == null)
            {
                return results;
            }
            paramBuilder.filter(filter).filterByProjectIds(getProjectIdSet(projectIdList));
        }
        users = service.findUsers(jiraServiceCtx, query, paramBuilder.build());

        final int limit = getLimit();
        int count = 0;
        int total = users.size();

        for (User user : users)
        {
            if (!excludeUsers.contains(user.getName()))
            {
                final String html = formatUser(fieldName, user, query, canShowEmailAddresses);
                results.addUser(new UserPickerUser(user.getName(), user.getDisplayName(), html, showAvatar ? avatarService.getAvatarURL(user, user.getName(), Avatar.Size.SMALL) : null));
                ++count;
            } else {
                --total;
            }


            if (count >= limit)
                break;
        }

        results.setTotal(total);
        results.setFooter(i18nHelper.getText("jira.ajax.autocomplete.user.more.results", String.valueOf(count), String.valueOf(total)));

        return results;
    }

    private UserFilter getUserFilter(final String fieldConfigId, final String fieldName)
    {
        final FieldConfig fieldConfig = getFieldConfig(fieldConfigId, fieldName);
        if (fieldConfig == null)
        {
            return null;
        }

        return userFilterManager.getFilter(fieldConfig);
    }

    private FieldConfig getFieldConfig(String fieldConfigId, String fieldName)
    {
        try
        {
            if (StringUtils.isNotBlank(fieldConfigId))
            {
                final FieldConfig fieldConfig = fieldConfigManager.getFieldConfig(Long.valueOf(fieldConfigId));

                if (fieldConfig != null)
                {
                    // double check customfield
                    final CustomField customField = fieldConfig.getCustomField();
                    if (customField == null || !fieldName.equals(customField.getId()))
                    {
                        log.warn("invalid parameters to /1/users/picker: fieldName "+fieldName+" does not match field config id "+fieldConfigId);
                    }
                    else
                    {
                        return fieldConfig;
                    }
                }
            }
        } catch (NumberFormatException nfe)
        {
            log.warn("Incorrect fieldConfigId '" + fieldConfigId + "' to /1/users/picker. Expecting a numeric id.");
        }
        return null;
    }

    private Set<Long> getProjectIdSet(List<String> projectIdList)
    {
        final Set<Long> projectIds = Sets.newHashSet();
        if (CollectionUtils.isNotEmpty(projectIdList))
        {
            for (String projectIdStr : projectIdList)
            {
                try
                {
                    final long projectId = Long.valueOf(projectIdStr);
                    if (projectId > 0)
                    {
                        projectIds.add(projectId);
                    }
                }
                catch (NumberFormatException nfe)
                {
                }
            }
        }
        return projectIds;
    }

    private String getElementId(String fieldName, String type, String field)
    {
        return " id=\"" + fieldName + "_" + type + "_" + field + "\" ";
    }

    // get the number of items to display.
    private int getLimit()
    {
        //Default limit to 20
        int limit = 20;
        try
        {
            limit = Integer.valueOf(applicationProperties.getDefaultBackedString(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT));
        }
        catch (Exception nfe)
        {
            log.error(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT + " does not exist or is an invalid number in jira-application.properties. Using default value 20.", nfe);
        }
        return limit;
    }


    /*
    * We use direct html instead of velocity to ensure the AJAX lookup is as fast as possible
    */
    private String formatUser(String fieldName, User user, String query, boolean canShoweEmailAddresses)
    {

        DelimeterInserter delimeterInserter = new DelimeterInserter("<b>", "</b>");
        //delimeterInserter.setConsideredWhitespace("-_/\\,.+=&^%$#*@!~`'\":;<>");

        String[] terms = {query};

        String userFullName = delimeterInserter.insert(TextUtils.htmlEncode(user.getDisplayName()), terms);
        String userName = delimeterInserter.insert(TextUtils.htmlEncode(user.getName()), terms);


        StringBuilder sb = new StringBuilder();
        sb.append("<div ");
        if(!StringUtils.isEmpty(fieldName))
        {
            sb.append(getElementId(fieldName, "i", TextUtils.htmlEncode(user.getName())));
        }
        sb.append("class=\"yad\" ");

        sb.append(">");

        sb.append(userFullName);
        if (canShoweEmailAddresses)
        {
            String userEmail = delimeterInserter.insert(TextUtils.htmlEncode(user.getEmailAddress()), terms);
            /*
             We dont mask the email address by design.  We dont think the email bots will be able to easily
             get email addresses from YUI generated divs and also its only an issue if "browse user" is given to group
             anyone.  So here is where we would change this if we change our mind in the future.
             */
            sb.append("&nbsp;-&nbsp;");
            sb.append(userEmail);
        }
        sb.append("&nbsp;(");
        sb.append(userName);
        sb.append(")");

        sb.append("</div>");
        return sb.toString();
    }


    JiraServiceContext getContext()
    {
        ApplicationUser user = authContext.getUser();
        return new JiraServiceContextImpl(user);
    }

    @XmlRootElement
    public static class UserPickerResultsWrapper
    {
        @XmlElement
        private List<UserPickerUser> users;
        
        @XmlElement
        private Integer total;
        
        @XmlElement
        private String footer;

        @SuppressWarnings({"UnusedDeclaration", "unused"})
        private UserPickerResultsWrapper() {}

        public UserPickerResultsWrapper(List<UserPickerUser> users, String footer, Integer total)
        {
            this.users = users;
            this.footer = footer;
            this.total = total;
        }

        public void addUser(final UserPickerUser user)
        {
            if (users == null)
            {
                users = new ArrayList<UserPickerUser>();
            }
            users.add(user);
        }

        public void setFooter(String footer)
        {
            this.footer = footer;
        }

        public void setTotal(Integer total) {
            this.total = total;
        }

        @Override
        public String toString()
        {
            return "UserPickerResultsWrapper{" +
                    "users=" + users +
                    ", total=" + total +
                    ", footer='" + footer + '\'' +
                    '}';
        }
    }

    @XmlRootElement
    public static class UserPickerUser
    {
        @XmlElement
        private String name;
        @XmlElement
        private String html;
        @XmlElement
        private String displayName;
        @XmlElement
        private URI avatarUrl;

        @SuppressWarnings({"UnusedDeclaration", "unused"})
        private UserPickerUser() {}

        public UserPickerUser(String name, String displayName, String html, URI avatarUrl)
        {
            this.name = name;
            this.displayName = displayName;
            this.html = html;
            this.avatarUrl = avatarUrl;
        }

        @Override
        public String toString()
        {
            return "UserPickerUser{" +
                    "name='" + name + '\'' +
                    ", html='" + html + '\'' +
                    ", displayName='" + displayName + '\'' +
                    ", avatarUrl=" + avatarUrl +
                    '}';
        }
    }
}
