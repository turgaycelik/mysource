package com.atlassian.jira.web.action.admin.user;

import com.atlassian.core.util.StringUtils;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.filter.SearchRequestAdminService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.plugin.userformat.FullNameUserFormat;
import com.atlassian.jira.plugin.userformat.UserFormats;
import com.atlassian.jira.plugin.userformat.UserFormatter;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.util.GroupToIssueSecuritySchemeMapper;
import com.atlassian.jira.security.util.GroupToNotificationSchemeMapper;
import com.atlassian.jira.security.util.GroupToPermissionSchemeMapper;
import com.atlassian.jira.util.GlobalPermissionGroupAssociationUtil;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.Collection;
import java.util.Collections;

@WebSudoRequired
public class ViewGroup extends JiraWebActionSupport
{
    private String name;
    private Group group;
    private GroupToPermissionSchemeMapper groupPermissionSchemeMapper;
    private GroupToNotificationSchemeMapper groupNotificationSchemeMapper;
    private GroupToIssueSecuritySchemeMapper groupIssueSecuritySchemeMapper;
    private final SearchRequestAdminService searchRequestAdminService;
    private final GlobalPermissionGroupAssociationUtil globalPermissionGroupAssociationUtil;
    private final UserFormatter fullNameUserFormatter;
    private CrowdService crowdService;

    public ViewGroup(final SearchRequestAdminService searchRequestAdminService, final UserFormats userFormats,
            final GlobalPermissionGroupAssociationUtil globalPermissionGroupAssociationUtil,
            final PermissionSchemeManager permissionSchemeManager, final PermissionManager permissionManager,
            final NotificationSchemeManager notificationSchemeManager, final CrowdService crowdService)
            throws GenericEntityException
    {
        this.searchRequestAdminService = searchRequestAdminService;
        this.fullNameUserFormatter = userFormats.formatter(FullNameUserFormat.TYPE);
        this.globalPermissionGroupAssociationUtil = globalPermissionGroupAssociationUtil;
        this.crowdService = crowdService;

        this.groupPermissionSchemeMapper = new GroupToPermissionSchemeMapper(permissionSchemeManager, permissionManager);
        this.groupNotificationSchemeMapper = new GroupToNotificationSchemeMapper(notificationSchemeManager);

        final IssueSecuritySchemeManager securitySchemeManager = ComponentAccessor.getComponentOfType(IssueSecuritySchemeManager.class);
        final IssueSecurityLevelManager securityLevelManager = ComponentAccessor.getComponentOfType(IssueSecurityLevelManager.class);
        try
        {
            groupIssueSecuritySchemeMapper = new GroupToIssueSecuritySchemeMapper(securitySchemeManager, securityLevelManager);
        }
        catch (final GenericEntityException e)
        {
            throw new RuntimeException(e);
        }

    }

    public String execute()
    {
        group = crowdService.getGroup(getName());
        if (group == null)
        {
            addErrorMessage("Group not found.");
        }
        return getResult();
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public Group getGroup()
    {
        return group;
    }

    public Collection getPermissionSchemes(final String groupName)
    {
        if (groupPermissionSchemeMapper != null)
        {
            return groupPermissionSchemeMapper.getMappedValues(groupName);
        }
        return Collections.EMPTY_LIST;
    }

    public Collection getNotificationSchemes(final String groupName)
    {
        if (groupNotificationSchemeMapper != null)
        {
            return groupNotificationSchemeMapper.getMappedValues(groupName);
        }
        return Collections.EMPTY_LIST;
    }

    public Collection getIssueSecuritySchemes(final String groupName)
    {
        if (groupIssueSecuritySchemeMapper != null)
        {
            return groupIssueSecuritySchemeMapper.getMappedValues(groupName);
        }
        return Collections.EMPTY_LIST;
    }

    public Collection getSavedFilters(final Group group)
    {
        final Collection filters = searchRequestAdminService.getFiltersSharedWithGroup(group);
        return filters == null ? Collections.EMPTY_LIST : filters;
    }

    public String getEscapeAmpersand(final String str)
    {
        return StringUtils.replaceAll(str, "&", "%26");
    }

    public boolean isUserAbleToDeleteGroup(final String groupName)
    {
        return globalPermissionGroupAssociationUtil.isUserAbleToDeleteGroup(getLoggedInUser(), groupName);
    }

    public String getFullUserName(final String userName)
    {
        if (userName != null)
        {
            return fullNameUserFormatter.formatUsername(userName, "view_group");
        }
        return null;
    }
}
