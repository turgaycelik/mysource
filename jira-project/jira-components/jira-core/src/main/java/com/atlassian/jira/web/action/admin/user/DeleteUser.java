package com.atlassian.jira.web.action.admin.user;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.user.UserService;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.plugin.user.PreDeleteUserErrorsManager;
import com.atlassian.jira.plugin.user.WebErrorMessage;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserDeleteVeto;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.BaseUrl;
import com.atlassian.jira.util.UrlBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class DeleteUser extends ViewUser
{
    private final SearchRequestService searchRequestService;
    private final UserService userService;
    private final UserUtil userUtil;
    private final UserDeleteVeto userDeleteVeto;
    private final PortalPageService portalPageService;
    private final PreDeleteUserErrorsManager preDeleteUserErrorsManager;
    private final BaseUrl baseUrlLocator;

    private UserService.DeleteUserValidationResult validationResult;
    private ArrayList<Project> projectsUserLeads;
    private ArrayList<ProjectComponent> componentsUserLeads;
    private Map<String, String> linkableWarnings;
    private Map<String, String> linkableErrors;
    private Map<String, String> projectLeadErrors;
    private Map<String, String> componentLeadWarnings;
    private UserManager.UserState userState;
    private MessageGenerator messageGen;

    boolean confirm;

    public DeleteUser(final CrowdService crowdService, final CrowdDirectoryService crowdDirectoryService,
            final SearchRequestService searchRequestService, final UserService userService, final UserUtil userUtil,
            final PortalPageService portalPageService, final UserPropertyManager userPropertyManager,
            final UserManager userManager, final UserDeleteVeto userDeleteVeto, final PreDeleteUserErrorsManager preDeleteUserErrorsManager,
            final BaseUrl baseUrlLocator)
    {
        super(crowdService, crowdDirectoryService, userPropertyManager, userManager);
        this.searchRequestService = searchRequestService;
        this.userService = userService;
        this.userUtil = userUtil;
        this.portalPageService = portalPageService;
        this.userDeleteVeto = userDeleteVeto;
        this.preDeleteUserErrorsManager = preDeleteUserErrorsManager;
        this.baseUrlLocator = baseUrlLocator;
    }

    protected void doValidation()
    {
        validationResult = userService.validateDeleteUser(getLoggedInApplicationUser(), getName());
        if (!validationResult.isValid())
        {
            addErrorCollection(validationResult.getErrorCollection());
        }
        super.doValidation();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        try
        {
            if (confirm)
            {
                userService.removeUser(getLoggedInApplicationUser(), validationResult);
            }
        }
        catch (Exception e)
        {
            addErrorMessage(getText("admin.errors.users.exception.trying.to.remove", e));
        }

        if (getHasErrorMessages())
        {
            return ERROR;
        }

        return returnCompleteWithInlineRedirect("UserBrowser.jspa");
    }

    public UserService.DeleteUserValidationResult getValidationResult()
    {
        if (validationResult == null)
        {
            validationResult = userService.validateDeleteUser(getLoggedInApplicationUser(), getName());
        }
        return validationResult;
    }

    public boolean isDeleteable()
    {
        try
        {
            return getValidationResult().isValid();
        }
        catch (Exception e)
        {
            log.error(e, e);
            return false;
        }
    }

    public boolean isNonSysAdminAttemptingToDeleteSysAdmin()
    {
        return userUtil.isNonSysAdminAttemptingToDeleteSysAdmin(getLoggedInUser(), getUser());
    }

    public Collection<Project> getProjectsUserLeads()
    {
        if (projectsUserLeads == null)
        {
            projectsUserLeads = new ArrayList<Project>(userUtil.getProjectsLeadBy(getUser()));
        }
        return projectsUserLeads;
    }

    public Collection<ProjectComponent> getComponentsUserLeads()
    {
        if (componentsUserLeads == null)
        {
            componentsUserLeads = new ArrayList<ProjectComponent>(userUtil.getComponentsUserLeads(getUser()));
        }
        return componentsUserLeads;
    }

    public boolean isConfirm()
    {
        return confirm;
    }

    public void setConfirm(boolean confirm)
    {
        this.confirm = confirm;
    }

    private MessageGenerator getMessageGenerator()
    {
        if (messageGen == null)
        {
            if (getUserState().isInMultipleDirectories())
            {
                messageGen = new DoNotGenerateErrorMessages();
            }
            else
            {
                messageGen = new GenerateErrorMessages(preDeleteUserErrorsManager, getApplicationUser());
            }
        }
        return messageGen;
    }

    public Map<String, String> getLinkableWarnings()
    {
        if (linkableWarnings == null)
        {
            linkableWarnings = getMessageGenerator().getWarnings();
        }
        return linkableWarnings;
    }

    public Map<String, String> getLinkableErrors()
    {
        if (linkableErrors == null)
        {
            linkableErrors = getMessageGenerator().getErrors();
        }
        return linkableErrors;
    }

    public Map<String, String> getProjectsUserLeadsError()
    {
        if (projectLeadErrors == null)
        {
            projectLeadErrors = getMessageGenerator().getProjectsUserLeadsError();
        }
        return projectLeadErrors;
    }

    public Map<String, String> getComponentsUserLeadsWarning()
    {
        if (componentLeadWarnings == null)
        {
            componentLeadWarnings = getMessageGenerator().getComponentsUserLeadsWarning();
        }
        return componentLeadWarnings;
    }

    public UserManager.UserState getUserState()
    {
        if (userState == null)
        {
            userState = userManager.getUserState(getUser());
        }
        return userState;
    }

    public boolean isSelfDestruct()
    {
        final ApplicationUser me = getLoggedInApplicationUser();
        return me != null && me.equals(getApplicationUser());
    }


    /** Generates various warning and error messages related to user deletion requests. */
    static interface MessageGenerator
    {
        /**
         * Provides generic warning messages with the (pre-translated) message as the key and a
         * target URL for a link as the value.  The value may be {@code null}, in which case
         * there is nothing interesting to link to for that warning.
         */
        Map<String, String> getWarnings();

        /**
         * Provides generic error messages with the (pre-translated) message as the key and a
         * target URL for a link as the value.  The value may be {@code null}, in which case
         * there is nothing interesting to link to for that error.
         */
        Map<String, String> getErrors();

        /**
         * Provides error information related to the target user being a project lead.  The keys
         * are project names, and the values are links to the project configuration summary page
         * for each project.
         */
        Map<String, String> getProjectsUserLeadsError();

        /**
         * Provides warning information related to the target user being a component lead.  The keys
         * are component names, and the values are links to the project configuration components
         * page for the project to which that component belongs.
         */
        Map<String, String> getComponentsUserLeadsWarning();
    }



    private class GenerateErrorMessages implements MessageGenerator
    {
        private final PreDeleteUserErrorsManager preDeleteUserErrorsManager;
        private final ImmutableList<WebErrorMessage> pluginErrorMessages;
        private final ApplicationUser user;

        public GenerateErrorMessages(final PreDeleteUserErrorsManager preDeleteUserErrorsManager, ApplicationUser user)
        {
            this.preDeleteUserErrorsManager = preDeleteUserErrorsManager;
            this.pluginErrorMessages = getPluginErrorMessages(user.getDirectoryUser());
            this.user = user;
        }

        public Map<String, String> getWarnings()
        {
            final Map<String, String> linkableWarningMessages = Maps.newHashMap();
            final ApplicationUser userForDelete = getApplicationUser();
            long numSharedFilters = searchRequestService.getNonPrivateFilters(userForDelete).size();
            if (numSharedFilters > 0)
            {
                UrlBuilder urlBuilder = newUrlBuilder();
                urlBuilder.addPaths("/secure/admin/filters/ViewSharedFilters.jspa");
                urlBuilder.addParameter("searchOwnerUserName", user.getName());

                linkableWarningMessages.put(getText("admin.deleteuser.filters.created.counted", numSharedFilters), urlBuilder.asUrlString());
            }

            long numFilterFavoritedByOthers = searchRequestService.getFiltersFavouritedByOthers(userForDelete).size();
            if (numFilterFavoritedByOthers > 0)
            {
                linkableWarningMessages.put(getText("admin.deleteuser.filters.favourited.counted", numFilterFavoritedByOthers), null);
            }

            long numNonPrivatePortalPages = portalPageService.getNonPrivatePortalPages(getApplicationUser()).size();
            if (numNonPrivatePortalPages > 0)
            {
                UrlBuilder urlBuilder = newUrlBuilder();
                urlBuilder.addPaths("/secure/admin/dashboards/ViewSharedDashboards.jspa");
                urlBuilder.addParameter("searchOwnerUserName", user.getName());

                linkableWarningMessages.put(getText("admin.deleteuser.portalpages.created.counted", numNonPrivatePortalPages), urlBuilder.asUrlString());
            }

            long numOtherFavouritedPortalPages = portalPageService.getPortalPagesFavouritedByOthers(userForDelete).size();
            if (numOtherFavouritedPortalPages > 0)
            {
                linkableWarningMessages.put(getText("admin.deleteuser.portalpages.favourited.counted", numNonPrivatePortalPages), null);
            }

            long numComponentsUserLeads = userUtil.getComponentsUserLeads(userForDelete).size();
            if (numComponentsUserLeads > 0)
            {
                linkableWarningMessages.put(getText("admin.deleteuser.components.lead.counted", numComponentsUserLeads), null);
            }
            return linkableWarningMessages;
        }


        public Map<String, String> getErrors()
        {
            final Map<String, String> webErrorMessages = Maps.newHashMap();
            for (WebErrorMessage pluginErrorMessage : pluginErrorMessages)
            {
                webErrorMessages.put(pluginErrorMessage.getSnippet(), pluginErrorMessage.getURI().toString());
            }

            long numAssignedIssues = 0;
            try
            {
                numAssignedIssues = userUtil.getNumberOfAssignedIssuesIgnoreSecurity(getLoggedInUser(), getUser());
            }
            catch (SearchException e)
            {
                log.error(e,e);
            }
            if (numAssignedIssues > 0)
            {
                UrlBuilder urlBuilder = newUrlBuilder();
                urlBuilder.addPaths("/secure/IssueNavigator.jspa");
                urlBuilder.addParameter("reset", "true");
                urlBuilder.addParameter("mode", "hide");
                urlBuilder.addParameter("sorter/order", "ASC");
                urlBuilder.addParameter("sorter/field", "priority");
                urlBuilder.addParameter("assigneeSelect", "specificuser");
                urlBuilder.addParameter("assignee", user.getName());

                webErrorMessages.put(getText("admin.deleteuser.assigned.issues.counted", numAssignedIssues), urlBuilder.asUrlString());
            }
            long numReportedIssues = 0;
            try
            {
                numReportedIssues = userUtil.getNumberOfReportedIssuesIgnoreSecurity(getLoggedInUser(), getUser());
            }
            catch (SearchException e)
            {
                log.error(e,e);
            }
            if (numReportedIssues > 0)
            {
                UrlBuilder urlBuilder = newUrlBuilder();
                urlBuilder.addPaths("/secure/IssueNavigator.jspa");
                urlBuilder.addParameter("reset", "true");
                urlBuilder.addParameter("mode", "hide");
                urlBuilder.addParameter("sorter/order", "ASC");
                urlBuilder.addParameter("sorter/field", "priority");
                urlBuilder.addParameter("reporterSelect", "specificuser");
                urlBuilder.addParameter("reporter", user.getName());

                webErrorMessages.put(getText("admin.deleteuser.reported.issues.counted", numReportedIssues), urlBuilder.asUrlString());
            }

            long numCommentedIssues = userDeleteVeto.getCommentCountByAuthor(getApplicationUser());
            if (numCommentedIssues > 0)
            {
                webErrorMessages.put(getText("admin.deleteuser.issue.comments.counted", numCommentedIssues), null);
            }

            long numProjectsUserLeads = userUtil.getProjectsLeadBy(getUser()).size();
            if (numProjectsUserLeads > 0)
            {
                webErrorMessages.put(getText("admin.deleteuser.projects.lead.counted", numProjectsUserLeads), null);

            }
            return webErrorMessages;
        }

        public Map<String, String> getProjectsUserLeadsError()
        {
            final Map<String, String> projectNamesAndURLs = Maps.newHashMap();
            for (Project project : getProjectsUserLeads())
            {
                UrlBuilder fullURL = newUrlBuilder();
                fullURL.addPaths("/plugins/servlet/project-config");
                fullURL.addPaths("/" + project.getKey() + "/summary");
                projectNamesAndURLs.put(project.getName(), fullURL.asUrlString());
            }
            return projectNamesAndURLs;
        }

        public Map<String, String> getComponentsUserLeadsWarning()
        {
            final Map<String, String> componentNamesAndURLs = Maps.newHashMap();
            for (ProjectComponent component : getComponentsUserLeads())
            {
                UrlBuilder fullURL = newUrlBuilder();
                fullURL.addPaths("/plugins/servlet/project-config");
                Project project = getProjectManager().getProjectObj(component.getProjectId());
                fullURL.addPaths("/" + project.getKey() + "/components");

                componentNamesAndURLs.put(component.getName(), fullURL.asUrlString());
            }
            return componentNamesAndURLs;
        }

        ImmutableList<WebErrorMessage> getPluginErrorMessages(User user)
        {
            return preDeleteUserErrorsManager.getWarnings(user);
        }

        private UrlBuilder newUrlBuilder()
        {
            String base = baseUrlLocator.getBaseUrl();
            if (StringUtils.isEmpty(base))
            {
                base = "/";
            }
            return new UrlBuilder(base);
        }
    }


    /**
     * When the user is in multiple user directories, it doesn't make any sense to display messages
     * about the consequences of deleting the user, because the user will continue to exist as
     * provided by the "unshadowed" version of that username from the alternate user directory.
     */
    static class DoNotGenerateErrorMessages implements MessageGenerator
    {
        DoNotGenerateErrorMessages() {}

        public Map<String, String> getWarnings()
        {
            return ImmutableMap.of();
        }

        public Map<String, String> getErrors()
        {
            return ImmutableMap.of();
        }

        public Map<String, String> getProjectsUserLeadsError()
        {
            return ImmutableMap.of();
        }

        public Map<String, String> getComponentsUserLeadsWarning()
        {
            return ImmutableMap.of();
        }
    }
}

