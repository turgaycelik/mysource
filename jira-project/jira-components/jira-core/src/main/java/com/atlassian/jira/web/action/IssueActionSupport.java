package com.atlassian.jira.web.action;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.bc.security.login.LoginInfo;
import com.atlassian.jira.bc.security.login.LoginService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.VersionProxy;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.session.SessionPagerFilterManager;
import com.atlassian.jira.web.session.SessionSearchObjectManagerFactory;
import com.atlassian.jira.web.session.SessionSearchRequestManager;
import com.atlassian.jira.web.session.SessionSelectedIssueManager;
import com.atlassian.jira.web.session.SessionSelectedIssueManager.SelectedIssueData;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.atlassian.security.random.DefaultSecureTokenGenerator;
import com.atlassian.security.random.SecureTokenGenerator;

import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.loader.ActionDescriptor;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.component.ComponentAccessor.getComponent;

@SuppressWarnings("NonSerializableFieldInSerializableClass")  // We don't serialize actions, anyway
public class IssueActionSupport extends ProjectActionSupport
{
    private Workflow workflow;

    private final IssueManager issueManager;
    private final CustomFieldManager customFieldManager;
    protected final AttachmentManager attachmentManager;
    private final VersionManager versionManager;
    private final UserIssueHistoryManager userHistoryManager;
    private final TimeTrackingConfiguration timeTrackingConfiguration;
    private final SecureTokenGenerator secureTokenGenerator = DefaultSecureTokenGenerator.getInstance();

    private SearchRequest searchRequest;
    private SessionSearchRequestManager sessionSearchRequestManager;
    private SessionSelectedIssueManager sessionSelectedIssueManager;
    private SessionPagerFilterManager sessionPagerFilterManager;
    private SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory;
    private LoginService loginService;
    private Map<String, LoginInfo> loginInfoCache = new HashMap<String, LoginInfo>();

    private String formToken;

    public IssueActionSupport(final IssueManager issueManager, final CustomFieldManager customFieldManager,
            final AttachmentManager attachmentManager, final ProjectManager projectManager,
            final PermissionManager permissionManager, final VersionManager versionManager,
            final UserIssueHistoryManager userHistoryManager, TimeTrackingConfiguration timeTrackingConfiguration)
    {
        super(projectManager, permissionManager);
        this.issueManager = issueManager;
        this.customFieldManager = customFieldManager;
        this.attachmentManager = attachmentManager;
        this.versionManager = versionManager;
        this.userHistoryManager = userHistoryManager;
        this.timeTrackingConfiguration = timeTrackingConfiguration;
    }

    public IssueActionSupport()
    {
        this(
                getComponent(IssueManager.class),
                getComponent(CustomFieldManager.class),
                getComponent(AttachmentManager.class),
                getComponent(ProjectManager.class),
                getComponent(PermissionManager.class),
                getComponent(VersionManager.class),
                getComponent(UserIssueHistoryManager.class),
                getComponent(TimeTrackingConfiguration.class)
        );
    }

    public IssueManager getIssueManager()
    {
        return issueManager;
    }

    public CustomFieldManager getCustomFieldManager()
    {
        return customFieldManager;
    }

    public AttachmentManager getAttachmentManager()
    {
        return attachmentManager;
    }

    /**
     * This method will return the one in the current search request, or return null if one does not exist
     */
    public SearchRequest getSearchRequest()
    {
        if (searchRequest == null)
        {
            searchRequest = getSessionSearchRequestManager().getCurrentObject();
            if ((searchRequest != null) && searchRequest.isLoaded())
            {
                final SearchRequest requestFromDB = ComponentAccessor.getComponent(SearchRequestService.class).getFilter(getJiraServiceContext(),
                    searchRequest.getId());
                if (requestFromDB != null)
                {
                    if (searchRequest.isModified())
                    {
                        searchRequest.setPermissions(requestFromDB.getPermissions());
                    }
                    else
                    {
                        requestFromDB.setUseColumns(searchRequest.useColumns());
                        setSearchRequest(requestFromDB);
                        // This fixes a bug where the search request was being created too many times
                        searchRequest = requestFromDB;
                    }
                }
            }
        }
        return searchRequest;
    }

    /**
     * Reutrns the JQL representation of the curren search erquest in the session.
     *
     * @return the jql of the SearchRequest in the session, empty string otherwise.
     */
    public String getCurrentJQL()
    {
        final SearchRequest sr = getSearchRequest();
        if (sr != null)
        {
            final SearchService searchService = ComponentAccessor.getComponent(SearchService.class);
            return searchService.getJqlString(sr.getQuery());
        }
        return null;
    }

    /**
     * This method set the SearchRequest in the user's session
     *
     * @param searchRequest The seerachRequest to place in the session
     */
    protected void setSearchRequest(final SearchRequest searchRequest)
    {
        //we want the search request to be re-read, so set it to null here.
        this.searchRequest = null;
        getSessionSearchRequestManager().setCurrentObject(searchRequest);
    }

    protected SessionSearchRequestManager getSessionSearchRequestManager()
    {
        if (sessionSearchRequestManager == null)
        {
            sessionSearchRequestManager = getSessionSearchRequestManagerFactory().createSearchRequestManager(ActionContext.getRequest());
        }
        return sessionSearchRequestManager;
    }

    protected SessionSelectedIssueManager getSessionSelectedIssueManager()
    {
        if (sessionSelectedIssueManager == null)
        {
            sessionSelectedIssueManager = getSessionSearchRequestManagerFactory().createSelectedIssueManager(ActionContext.getRequest());
        }
        return sessionSelectedIssueManager;
    }

    protected SessionPagerFilterManager getSessionPagerFilterManager()
    {
        if (sessionPagerFilterManager == null)
        {
            sessionPagerFilterManager = getSessionSearchRequestManagerFactory().createPagerFilterManager(ActionContext.getRequest());
        }
        return sessionPagerFilterManager;
    }

    public SessionSearchObjectManagerFactory getSessionSearchRequestManagerFactory()
    {
        if (sessionSearchObjectManagerFactory == null)
        {
            sessionSearchObjectManagerFactory = ComponentAccessor.getComponentOfType(SessionSearchObjectManagerFactory.class);
        }
        return sessionSearchObjectManagerFactory;
    }

    protected void updateSearchRequest()
    {
        setSearchRequest(ComponentManager.getInstance().getSearchRequestService().updateFilter(getJiraServiceContext(), getSearchRequest()));
    }

    /**
     * Sets the current issue. This is similar to setting the current project - so that we can make things more convenient for users.
     *
     * @param issueObj The issue that you are currently viewing.
     */
    public void addIssueToHistory(final Issue issueObj)
    {
        final User remoteUser = getLoggedInUser();
        userHistoryManager.addIssueToHistory(remoteUser, issueObj);
    }

    /**
     * Returns a list of versions applicable to this issue (unreleased first).
     */
    public List<VersionProxy> getPossibleVersions(final GenericValue project) throws Exception
    {
        return getPossibleVersions(project, true);
    }

    /**
     * Returns a list of versions applicable to this issue (released first).
     */
    public List<VersionProxy> getPossibleVersionsReleasedFirst(final GenericValue project) throws Exception
    {
        return getPossibleVersions(project, false);
    }

    /**
     * Returns a list of versions applicable to this issue
     */
    private List<VersionProxy> getPossibleVersions(final GenericValue project, final boolean unreleasedFirst) throws Exception
    {
        final List<VersionProxy> unreleased = new ArrayList<VersionProxy>();
        final Iterator<Version> unreleasedIter = versionManager.getVersionsUnreleased(project.getLong("id"), false).iterator();
        if (unreleasedIter.hasNext())
        {
            unreleased.add(new VersionProxy(-2, getText("common.filters.unreleasedversions")));
            while (unreleasedIter.hasNext())
            {
                unreleased.add(new VersionProxy(unreleasedIter.next()));
            }
        }

        // reverse the order of the releasedIter versions.
        final List<VersionProxy> released = new ArrayList<VersionProxy>();
        final List<Version> releasedIter = new ArrayList<Version>(versionManager.getVersionsReleased(project.getLong("id"), false));
        if (!releasedIter.isEmpty())
        {
            released.add(new VersionProxy(-3, getText("common.filters.releasedversions")));
            Collections.reverse(releasedIter);
            for (final Version element : releasedIter)
            {
                released.add(new VersionProxy(element));
            }
        }

        final List<VersionProxy> versions = new ArrayList<VersionProxy>();
        if (unreleasedFirst)
        {
            versions.addAll(unreleased);
            versions.addAll(released);
        }
        else
        {
            versions.addAll(released);
            versions.addAll(unreleased);
        }

        return versions;
    }

    public String getUrlEncoded(final String s)
    {
        return JiraUrlCodec.encode(s);
    }

    public Workflow getWorkflow()
    {
        if (workflow == null)
        {
            workflow = ComponentAccessor.getWorkflowManager().makeWorkflow(getLoggedInApplicationUser());
        }

        return workflow;
    }

    public BigDecimal getHoursPerDay()
    {
        return timeTrackingConfiguration.getHoursPerDay();
    }

    public BigDecimal getDaysPerWeek()
    {
        return timeTrackingConfiguration.getDaysPerWeek();
    }

    public boolean isTimeTrackingEnabled()
    {
        return timeTrackingConfiguration.enabled();
    }

    /**
     * this formatting function is shared by the full view for navigator as well as view issue.
     *
     * @param v duration in seconds
     */
    public String getPrettyDuration(final Long v)
    {
        return ComponentAccessor.getComponent(JiraDurationUtils.class).getFormattedDuration(v);
    }

    /**
     * Since v4.2 this method invocation does nothing
     *
     * @param selectedIssueId  id of the currently saelected issue
     *
     * @deprecated  Use {@link com.atlassian.jira.web.session.SessionSelectedIssueManager#setCurrentObject(Object)} instead
     */
    @Deprecated
    public void setSelectedIssueId(final Long selectedIssueId)
    {
        // This doesn't set the selected issue on the SessionSelectedIssueManager because at this point
        // we don't have information on the selected issue's index or the next issue.
        // Instead, we rely on the NextPreviousPager (when returning from view issue) and Ajax calls to
        // set the select issue (for dialogs in the issue navigator).
    }

    public void clearSelectedIssue()
    {
        getSessionSelectedIssueManager().setCurrentObject(new SelectedIssueData(null, 0, null));
    }

    public Long getSelectedIssueId()
    {
        final SelectedIssueData selectedIssueData = getSessionSelectedIssueManager().getCurrentObject();
        return selectedIssueData == null ? null : selectedIssueData.getSelectedIssueId();
    }

    /**
     * Checks if the custom field is hidden in the project with id of projectId.
     *
     * @param projectId
     * @param customFieldId the data store id of the custom field
     *
     * @deprecated Use {@link FieldVisibilityManager#isCustomFieldHidden(java.lang.Long, java.lang.Long,
     *             java.lang.String)} instead.
     */
    @Deprecated
    public boolean isCustomFieldHidden(final Long projectId, final Long customFieldId, final String issueTypeId)
    {
        return getFieldVisibilityManager().isFieldHidden(projectId, FieldManager.CUSTOM_FIELD_PREFIX + customFieldId, issueTypeId);
    }

    /**
     * Checks if the field is hidden in the project with id of projectId.
     *
     * @param projectId
     * @param id        fieldId
     *
     * @deprecated Use {@link FieldVisibilityManager#isFieldHidden(java.lang.Long, java.lang.String, java.lang.String)}
     *             instead.
     */
    @Deprecated
    public boolean isFieldHidden(final Long projectId, final String id, final String issueTypeId)
    {
        return getFieldVisibilityManager().isFieldHidden(projectId, id, issueTypeId);
    }

    public boolean isFieldHidden(final Long projectId, final String id, final Integer issueTypeId)
    {
        return getFieldVisibilityManager().isFieldHidden(projectId, id, issueTypeId.toString());
    }

    /**
     * Get the i18n'ed name of a workflow action (eg. 'Start Progress').
     *
     * @param descriptor Descriptor eg. from {@link com.atlassian.jira.workflow.JiraWorkflow#getDescriptor()}
     */
    public String getWorkflowTransitionDisplayName(final ActionDescriptor descriptor)
    {
        return WorkflowUtil.getWorkflowTransitionDisplayName(descriptor);
    }

    public String getWorkflowTransitionDescription(final ActionDescriptor descriptor)
    {
        return StringUtils.trimToNull(WorkflowUtil.getWorkflowTransitionDescription(descriptor));
    }

    /**
     * This can be called to get a description of the last time the user logged in
     *
     * @param user the user in play (not the current user but any user)
     *
     * @return a string of their last login time or a not recorded message
     */
    public String getLastLogin(User user)
    {
        return getLoginDate(getLoginInfo(user).getLastLoginTime());
    }

    /**
     * This can be called to get a description of the second last time the user logged in
     *
     * @param user the user in play (not the current user but any user)
     *
     * @return a string of their last login time or a not recorded message
     */
    public String getPreviousLogin(User user)
    {
        return getLoginDate(getLoginInfo(user).getPreviousLoginTime());
    }

    /**
     * This can be called to get a description of the last time the user failed to logged in
     *
     * @param user the user in play (not the current user but any user)
     *
     * @return a string of their last failed login time or a not recorded message
     */
    public String getLastFailedLogin(User user)
    {
        return getLoginDate(getLoginInfo(user).getLastFailedLoginTime());
    }

    /**
     * This can be called to get a the number of times the user logged in
     *
     * @param user the user in play (not the current user but any user)
     *
     * @return a string of their number of times they have logged in or a not recorded message
     */
    public String getLoginCount(User user)
    {
        return getLoginLong(getLoginInfo(user).getLoginCount());
    }

    /**
     * This can be called to get a the number of times the user currently failed to logged in
     *
     * @param user the user in play (not the current user but any user)
     *
     * @return a string of their number of times they have currently failed to logged in or a not recorded message
     */
    public String getCurrentFailedLoginCount(User user)
    {
        return getLoginLong(getLoginInfo(user).getCurrentFailedLoginCount());
    }

    /**
     * This can be called to get a the total number of times the user has failed to logged in
     *
     * @param user the user in play (not the current user but any user)
     *
     * @return a string of their total number of times they have failed to logged in or a not recorded message
     */
    public String getTotalFailedLoginCount(User user)
    {
        return getLoginLong(getLoginInfo(user).getTotalFailedLoginCount());
    }

    /**
     * True if the user requires an elevated security check on the next login
     * 
     * @param user the user in play (not the current user but any user)
     * @return true if the user requires an elevated security check on the next login 
     */
    public boolean isElevatedSecurityCheckRequired(User user)
    {
        return getLoginInfo(user).isElevatedSecurityCheckRequired();
    }

    /**
     * True if the user has ever logged in to JIRA
     *
     * @param user the user in play (not the current user but any user)
     *
     * @return true if they have ever logged into JIRA
     */
    public boolean getEverLoggedIn(User user)
    {
        return getLoginInfo(user).getLoginCount() != null;
    }

    private LoginInfo getLoginInfo(final User user)
    {
        String username = user.getName();
        // See if we already retrieved LoginInfo for this user
        LoginInfo loginInfo = loginInfoCache.get(username);

        if (loginInfo == null)
        {
            // Get LoginInfo from DB
            loginInfo = getLoginService().getLoginInfo(username);
            // put LoginInfo into cache
            loginInfoCache.put(username, loginInfo);
        }
        return loginInfo;
    }

    LoginService getLoginService()
    {
        if (loginService == null)
        {
            loginService = ComponentAccessor.getComponentOfType(LoginService.class);
        }
        return loginService;
    }

    private String getLoginDate(final Long value)
    {
        if (value == null)
        {
            return getText("login.not.recorded");
        }
        return getOutlookDate().format(new Date(value));
    }

    private String getLoginLong(final Long value)
    {
        if (value == null)
        {
            return getText("login.not.recorded");
        }
        return String.valueOf(value);
    }

    private FieldVisibilityManager getFieldVisibilityManager()
    {
        return getComponentInstanceOfType(FieldVisibilityManager.class);
    }

    public String getFormToken()
    {
        if(formToken == null)
        {
            setFormToken(secureTokenGenerator.generateToken());
        }

        return formToken;
    }

    public void setFormToken(final String formToken)
    {
        this.formToken = formToken;
    }
}
