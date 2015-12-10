package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.action.IssueActionSupport;
import com.atlassian.jira.web.component.issuesummary.IssueSummaryLayoutBean;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;

/**
 * An abstract action that should be extended by any action which wants to 'select' an issue
 * <p/>
 */
public abstract class AbstractIssueSelectAction extends IssueActionSupport implements IssueSummaryAware
{
    /**
     * This is used by subclasses to indicate that the issue is not valid because the caller does not have permission to
     * view it.
     */
    public static final String ISSUE_PERMISSION_ERROR = "issue-permission-error";

    public static final String PREPOPULATED_ISSUE_OBJECT = AbstractIssueSelectAction.class.getCanonicalName() + "prepopulated.issue";

    private final SubTaskManager subTaskManager;

    protected Long id;
    private final SelectedIssue issue = new SelectedIssue(new IssueGetter());
    private GenericValue project;
    private String key;
    private String viewIssueKey;
    private Issue parentIssueObject = null;

    /**
     * Our terrible action hierarchy makes this call necessary
     */
    protected AbstractIssueSelectAction()
    {
        this(ComponentAccessor.getComponent(SubTaskManager.class));
    }

    protected AbstractIssueSelectAction(final SubTaskManager subTaskManager)
    {
        this.subTaskManager = subTaskManager;
    }

    public boolean isIssueExists()
    {
        return issue.exists();
    }

    /**
     * In comparison to isIssueExists() this method performs a permission check and returns true if the issue exists and
     * the user has sufficient permissions. It will also add to this action's error messages if the issue does not exist
     * of is not accessible.
     *
     * @return true if the issue exists and the user has sufficient permissions otherwise false.
     */
    public boolean isIssueValid()
    {
        if (!issue.exists())
        {
            addErrorMessage(getText("issue.wasdeleted"));
            return false;
        }

        if (!userCanBrowse(issue.object()))
        {
            addErrorMessage(getText("admin.errors.issues.no.permission.to.see"));
            return false;
        }

        return true;
    }

    /**
     * This method will read the issue from the currently active request and return it as a GetterResult. If there is
     * no issue in the currently active request, this method falls back to loading the issue from the IssueManager using
     * the <code>id</code> and <code>key</code> fields.
     * <p/>
     * If this action's <code>id</code> and <code>key</code> fields are both null, this method bypasses the request and
     * the database completely and just returns a GetterResult with a null result in it.
     *
     * @return a GetterResult
     */
    @Nonnull
    private SelectedIssue.GetterResult getIssueResultFromRequestOrDatabase()
    {
        // ok we dont have the issue object at present.  We may have come in via id or key so lets be smart about that.  Remember that CreateIssue
        // is also in play here so id and key can be null beleive it or not
        if (id == null && key == null)
        {
            return new SelectedIssue.GetterResult(null);
        }

        MutableIssue issueFromRequest = readPrepopulatedIssueFromRequest();
        if (issueFromRequest != null)
        {
            return new SelectedIssue.GetterResult(issueFromRequest);
        }

        MutableIssue issueFromManager = null;

        // let's try to read the issue by id first
        if (id != null)
        {
            issueFromManager = getIssueManager().getIssueObject(id);
        }

        // no luck? try to get the issue using the key.
        if (issueFromManager == null && key != null)
        {
            issueFromManager = getIssueManager().getIssueObject(key);
        }
        
        return new SelectedIssue.GetterResult(issueFromManager);
    }

    private MutableIssue readPrepopulatedIssueFromRequest()
    {
        // ok the QuickLinkServlet may have already read the issue so there is no need for us to have a go at it as well
        final MutableIssue issue = (MutableIssue) request.getAttribute(PREPOPULATED_ISSUE_OBJECT);
        if (issue != null)
        {
            // we only read this guy once and once only so as not to confuse ourselves
            request.setAttribute(PREPOPULATED_ISSUE_OBJECT, null);
        }
        return issue;
    }

    /**
     * Gets the current issue's GenericValue.  This will perform a permission and existence check on the issue
     *
     * @return A generic value which contains the data of the current issue.
     * @throws IssuePermissionException if the current user does not have permission to read the issue
     * @throws IssueNotFoundException if the issue does not exist
     * @see #getIssueObject()
     */
    @Nonnull public GenericValue getIssue() throws IssueNotFoundException, IssuePermissionException
    {
        assertIssueIsValid();

        return issue.genericValue();
    }

    /**
     * Returns the current {@link Issue}. This method performs the same security checks as {@link #getIssue()}.
     *
     * @return The current issue.
     * @see #getIssue()
     * @throws IssuePermissionException if the current user does not have permission to read the issue
     * @throws IssueNotFoundException if the issue does not exist
     */
    @Nonnull public MutableIssue getIssueObject() throws IssueNotFoundException, IssuePermissionException
    {
        assertIssueIsValid();

        return issue.object();
    }

    /**
     * Because some of the code works on the issueGV object (such as voting code) directly the issueObject variable
     * representation can get out of synch with the underlying GV
     * <p/>
     * You can call this method to refresh the issueObject back to the current issueGV value.
     *
     * @return a fresh IssueObject that is backed by the current issueGV object
     */
    protected MutableIssue refreshIssueObject()
    {
        final GenericValue issueGV = getIssue(); // this will run with permission checks
        setIssueObject(issueFactory().getIssue(issueGV));
        return issue.object();
    }

    /**
     * This can be called by sub class to read the current value of the issueObject variable WITHOUT causing a database
     * read
     * <p/>
     * We need this because the CreateIssue variants dont have an issue to read and use this mechanims to make the
     * create happen. The code instatiates an empty unsaved MutableIssue on create and clone and the like.
     * <p/>
     * This kinda sucks BUT here we are.
     *
     * @return the current value of the issueObject variable
     */
    protected MutableIssue getIssueObjectWithoutDatabaseRead()
    {
        return issue.object();
    }

    /**
     * Get Id of current issue.
     *
     * @return Integer Id, or null if issue not set
     */
    public Long getId()
    {
        if (id == null && issue.exists())
        {
            id = issue.object().getId();
        }

        return id;
    }

    /**
     * Get key of current issue.
     *
     * @return Issue key, or null if not set
     */
    public String getKey()
    {
        if (key == null && issue.exists())
        {
            //noinspection ConstantConditions
            key = issue.object().getKey();
        }

        return key;
    }

    /**
     * Once this is called, the underlying issueGV and id and key will be updated to reflect the issue in play.
     *
     * @param issueObject the MutableIssue that has been created or read from the database
     * @return that same issue object
     */
    protected MutableIssue setIssueObject(@Nullable final MutableIssue issueObject)
    {
        if (issueObject != null)
        {
            this.id = issueObject.getId();
            this.key = issueObject.getKey();
        }

        issue.setObject(issueObject);
        return issueObject;
    }

    private IssueFactory issueFactory()
    {
        return ComponentAccessor.getComponent(IssueFactory.class);
    }

    /**
     * Set the the current issue by its id.
     *
     * @param id Eg. from {@link com.atlassian.jira.issue.Issue#getId()}
     */
    public void setId(final Long id)
    {
        this.id = id;
        key = null;
    }

    /**
     * Set current issue by its key.
     *
     * @param key Issue key.
     */
    public void setKey(final String key)
    {
        // Ensure key is uppercase for cache or database search
        this.key = key.toUpperCase();
        id = null;
    }

    public GenericValue getProject()
    {
        if ((project == null) && (getIssue() != null))
        {
            project = getProjectManager().getProject(getIssue());
        }

        return project;
    }

    private Project getProjectObject()
    {
        if (getIssueObject() == null)
            return null;
        return getIssueObject().getProjectObject();
    }

    public GenericValue getSecurityLevel(final Long id) throws Exception
    {
        if (getIssue() != null)
        {
            final IssueSecurityLevelManager secur = ComponentAccessor.getIssueSecurityLevelManager();
            return secur.getIssueSecurity(id);
        }
        return null;
    }

    public String getViewIssueKey()
    {
        return viewIssueKey;
    }

    public void setViewIssueKey(final String viewIssueKey)
    {
        this.viewIssueKey = viewIssueKey;
    }

    protected String redirectToView() throws Exception
    {
        return getRedirect(getViewUrl());
    }

    /**
     * Gets the relative path to the current issue. It does not include the {@link javax.servlet.http.HttpServletRequest#getContextPath()
     * context path}.
     *
     * @return The relative path to the current issue.
     */
    public String getViewUrl()
    {
        return "/browse/" + getRedirectKey();
    }

    /**
     * Gets the relative path to the current issue. It does not include the {@link javax.servlet.http.HttpServletRequest#getContextPath()
     * context path}.
     *
     * @return The relative path to the current issue.
     */
    public String getIssuePath()
    {
        // JRADEV-3199: added the try/catch to minimize exceptions on JAC caused by (probably) web-crawlers
        try
        {
            return "/browse/" + getRedirectKey();
        }
        catch (IssueNotFoundException e)
        {
            log.trace(e);
            return "";
        }
    }

    private String getRedirectKey()
    {
        String key;
        if (TextUtils.stringSet(getViewIssueKey()))
        {
            key = getViewIssueKey();
        }
        else
        {
            //noinspection ConstantConditions
            key = issue.exists() ? issue.object().getKey() : "";
        }

        return key;
    }

    /**
     * Determines whether the current user can edit or resolve this issue.
     *
     * @return whether the current user can edit or resolve this issue.
     */
    public boolean isEditable()
    {
        return isEditable(getIssueObject());
    }

    /**
     * Determines whether the current user can edit or resolve an specified issue.
     *
     * @param issue The issue in play.
     * @return whether the current user can edit or resolve an specified issue.
     */
    public boolean isEditable(final Issue issue)
    {
        if (issue == null)
        {
            throw new IssueNotFoundException("Issue unexpectedly null");
        }

        final boolean hasPermission = isHasEditIssuePermission(issue);
        return hasPermission && isWorkflowAllowsEdit(issue);
    }

    public boolean isHasEditIssuePermission(final Issue issue)
    {
        if (issue == null)
        {
            throw new IssueNotFoundException("Issue unexpectedly null");
        }

        return hasIssuePermission(Permissions.EDIT_ISSUE, issue);
    }

    public boolean isWorkflowAllowsEdit(final Issue issue)
    {
        return getIssueManager().isEditable(issue);
    }

    public boolean cameFromIssue()
    {
        final HttpServletRequest request = ActionContext.getRequest();
        if (request != null)
        {
            final String header = request.getHeader("referer");
            if (StringUtils.isNotBlank(header))
            {
                return header.contains("browse/" + getIssueObject().getKey());
            }
        }

        return false;
    }

    public boolean cameFromParent()
    {
        final Issue parent = getIssueObject().getParentObject();
        if (parent == null)
        {
            return false;
        }

        final HttpServletRequest request = ActionContext.getRequest();
        if (request != null)
        {
            final String header = request.getHeader("referer");
            if (StringUtils.isNotBlank(header))
            {
                return header.contains("browse/" + parent.getKey());
            }
        }

        return false;
    }

    /**
     * This is used by the issue summary decorator to add the left-hand side decoration.
     */
    public Issue getSummaryIssue()
    {
        return getIssueObject();
    }

    /**
     * Get the default layout bean for most actions.  This will work for 99% of actions, but specific actions may want
     * different views, and can override this method.
     */
    public IssueSummaryLayoutBean getLayoutBean()
    {
        return new IssueSummaryLayoutBean(false);
    }


    protected SubTaskManager getSubTaskManager()
    {
        return subTaskManager;
    }

    /**
     * Determines whether the issue is a sub task - i.e. if it has any incoming sub-task issue links
     *
     * @return whether the issue is a sub task.
     */
    public boolean isSubTask()
    {
        final GenericValue issue = getIssue();
        if (issue == null)
        {
            addErrorMessage(getText("admin.errors.issues.current.issue.null"));
            return false;
        }

        return getSubTaskManager().isSubTask(issue);
    }

    /**
     * Tests whether the sub-tasks are turned on and whether the the current issue is a 'parent' issue, i.e. it is not a
     * sub-task, as we do not allow a sub-task hierarchy. Also test whether the user has CREATE issue permission for the
     * current project. Sub-tasks are always created in the same project as its parent issue.
     * <p/>
     * Since 3.4, we also need to check whether the project has any sub-task issue types
     */
    public boolean isSubTaskCreatable()
    {
        @SuppressWarnings ("unchecked")
        final Collection<Option> subTaskOptions = ComponentAccessor.getFieldManager().getIssueTypeField().
                getOptionsForIssue(getIssueObject(), true);
        return getSubTaskManager().isSubTasksEnabled() && !isSubTask() && isEditable() && hasProjectPermission(Permissions.CREATE_ISSUE, getProjectObject()) && (subTaskOptions != null) && !subTaskOptions.isEmpty();
    }

    /**
     * Returns the parent of the current {@link com.atlassian.jira.issue.Issue}
     *
     * @return the parent issue object
     */
    public Issue getParentIssueObject()
    {
        if (isSubTask())
        {
            if (parentIssueObject == null)
            {
                final GenericValue issue = getIssue();
                if (issue == null)
                {
                    addErrorMessage(getText("admin.errors.current.issue.null"));
                    return null;
                }

                parentIssueObject = getIssueManager().getIssueObject(getSubTaskManager().getParentIssueId(issue));
            }
        }
        return parentIssueObject;
    }

    @Override
    public void addErrorMessage(String message)
    {
        if (errorMessages == null || !errorMessages.contains(message))
        {
            super.addErrorMessage(message);
        }
    }

    /**
     * Ensures that the current issue is valid for the calling user. If it does not exist or the calling user does not
     * have sufficient permissions, throws an exception.
     *
     * @see #isIssueValid()
     * @throws com.atlassian.jira.exception.IssueNotFoundException if the issue does not exist
     * @throws com.atlassian.jira.exception.IssuePermissionException if the calling user does not have permission to
     * view the issue
     */
    protected void assertIssueIsValid() throws IssueNotFoundException, IssuePermissionException
    {
        if (!isIssueExists())
        {
            addErrorMessage(getText("issue.wasdeleted"));

            throw new IssueNotFoundException("Issue with id '" + id + "' or key '" + key + "' could not be found in the system");
        }

        if (!isIssueValid())
        {
            final Issue issueObject = issue.object();

            @SuppressWarnings ({ "ConstantConditions" }) // because issue.exists() at this point
            final String issueStr = issueObject.getKey() != null ? issueObject.getKey() : issueObject.toString();
            throw new IssuePermissionException("User '" + getLoggedInUser() + "' does not have permission to see issue '" + issueStr + "'");
        }
    }

    /**
     * @param issue a MutableIssue
     * @return true iff the logged in user has BROWSE permission for this issue
     */
    private boolean userCanBrowse(Issue issue)
    {
        return getPermissionManager().hasPermission(Permissions.BROWSE, issue, getLoggedInApplicationUser());
    }

    /**
     * Reads the selected issue from the database or from the request. Also updates the user history when an issue is
     * first loaded.
     */
    private class IssueGetter implements SelectedIssue.Getter
    {
        @Nonnull
        @Override
        public SelectedIssue.GetterResult get()
        {
            SelectedIssue.GetterResult issue = getIssueResultFromRequestOrDatabase();

            // JRA-25688. adds the issue to the user's history only when it is first read from the database
            if (issue.object != null && userCanBrowse(issue.object))
            {
                addIssueToHistory(issue.object);
            }

            return issue;
        }
    }
}
