package com.atlassian.jira.functest.framework.navigation;

import com.atlassian.jira.functest.framework.navigation.issue.AttachmentsBlock;
import com.atlassian.jira.functest.framework.page.ViewIssuePage;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Navigate issue functionality
 *
 * @since v3.13
 */
public interface IssueNavigation
{
    /**
     * Navigates to a specific issue.
     *
     * @param issueKey key of issue
     */
    ViewIssuePage viewIssue(String issueKey);

    /**
     * Navigates to a specific issues printable view.
     *
     * @param issueKey key of issue
     */
    void viewPrintable(String issueKey);

    /**
     * Navigates to a specific issue's XML view.
     *
     * @param issueKey key of issue
     */
    void viewXml(String issueKey);

    /**
     * Navigates to the Edit screen of a specific issue.
     *
     * @param issueKey key of issue
     */
    void gotoEditIssue(String issueKey);

    /**
     * Navigates straight to the Edit screen of a specific issue, bypassing the View screen.
     *
     * @param issueId id of issue
     */
    void gotoEditIssue(long issueId);

    /**
     * Deletes the specific issue.
     *
     * @param issueKey issue key
     */
    void deleteIssue(String issueKey);

    /**
     * Navigates to the the issue provided
     *
     * @param issueKey The issuekey to navigate to.
     */
    public void gotoIssue(String issueKey);

    /**
     * Navigates to the change history tab of the issue provided
     *
     * @param issueKey The issuekey to navigate to.
     */
    void gotoIssueChangeHistory(String issueKey);

    /**
     * Navigates to the work log tab of the issue provided
     *
     * @param issueKey The issuekey to navigate to.
     */
    void gotoIssueWorkLog(String issueKey);

    /**
     * Creates a new issue for the given project and returns the created issue key
     * <p/>
     * Assumes that the schemes are such that summary is the only required field
     *
     * @param projectName the project name - can be null and hence assume default
     * @param issueType the issue type - can be null and hence assume default
     * @param summary a summary for the issue
     * @return the newly created issue key
     */
    String createIssue(@Nullable String projectName, @Nullable String issueType, String summary);


    /**
     * Creates a new issue for the given project and returns the created issue key
     * <p/>
     *
     * @param projectName the project name - can be null and hence assume default
     * @param issueType the issue type - can be null and hence assume default
     * @param summary a summary for the issue
     * @param params a map containing any optional params to add to the issue
     * @return the newly created issue key
     */
    String createIssue(final String projectName, final String issueType, final String summary, final Map<String,String[]> params);

    /**
     * Creates a new sub task for the given parent issue and returns the created issue key
     * <p/>
     * Assumes that the schemes are such that summary is the only required field.
     * <p/>
     * Note: if sub tasks are not enabled, they will be enabled before attempting to create the sub task.
     *
     * @param parentIssueKey the parent issue
     * @param subTaskType the sub task type
     * @param subTaskSummary the summary
     * @param subTaskDescription the description; use {@code null} to omit this field
     * @return the newly created issue key
     */
    String createSubTask(String parentIssueKey, String subTaskType, String subTaskSummary, String subTaskDescription);

    /**
     * Creates a new sub task for the given parent issue and returns the created issue key
     * <p/>
     * Assumes that the schemes are such that summary is the only required field.
     * <p/>
     * Note: if sub tasks are not enabled, they will be enabled before attempting to create the sub task.
     *
     * @param parentIssueKey the parent issue
     * @param subTaskType the sub task type
     * @param subTaskSummary the summary
     * @param subTaskDescription the description; use {@code null} to omit this field
     * @param originalEstimate original estimate for time tracking field; may be left {@code null}
     * @return the newly created issue key
     */
    String createSubTask(String parentIssueKey, String subTaskType, String subTaskSummary, String subTaskDescription, String originalEstimate);

    /**
     * Sets the given components on the selected issue.
     *
     * @param issueKey issue key
     * @param components components
     */
    void setComponents(String issueKey, String... components);

    /**
     * Sets the environment field on the given issue.
     *
     * @param issueKey issue key
     * @param environment the text
     */
    void setEnvironment(String issueKey, String environment);

    /**
     * Sets the description field on the given issue.
     *
     * @param issueKey issue key
     * @param description the text
     */
    void setDescription(String issueKey, String description);

    /**
     * Sets the description field on the given issue.
     *
     * @param issueKey issue key
     * @param customFieldId the id of the custom field to set e.g. <code>customfield_10000</code>
     * @param text the text
     */
    void setFreeTextCustomField(String issueKey, String customFieldId, String text);

    /**
     * Sets the priority of the given issue
     * @param issueKey issue key
     * @param priority the Displayed priority value
     */
    void setPriority(String issueKey, String priority);

    /**
     * Assign this issue to a different user.
     *
     * @param issueKey The issue key of the issue to be assigned.
     * @param comment The comment to be added when assigning the specified issue.
     * @param userFullName the full name of the user this should be assigned to.
     */
    void assignIssue(String issueKey, String comment, String userFullName);

    /**
     * Assign an issue to a user.
     * @param issueKey The issue key of the issue to be assigned.
     * @param userFullName The full name of the user the issue will be assigned to.
     * @param comment The comment to be added when assigning the specified issue.
     * @param commentLevel The group or role that will be able to view the specified comment.
     */
    void assignIssue(String issueKey, String userFullName, String comment, String commentLevel);

    /**
     * Unassign an issue. Note: assumes unassigned issues are allowed.
     *
     * @param issueKey The issue key of the issue to be un-assigned.
     * @param comment The comment to be added when un-assigning the specified issue.
     */
    void unassignIssue(String issueKey, String comment);

    /**
     * Un-assign an issue. Note: assumes un-assigned issues are allowed.
     * @param issueKey The issue key of the issue to be un-assigned.
     * @param comment The comment to be added when un-assigning the specified issue.
     * @param commentLevel The group or role that will be able to view the specified comment.
     */
    void unassignIssue(String issueKey, String comment, String commentLevel);

    /**
     * Sets the given fix versions on the selected issue.
     *
     * @param issueKey issue key
     * @param fixVersions the names of the versions e.g. <code>New Version 4</code>
     */
    void setFixVersions(String issueKey, String... fixVersions);

    /**
     * Sets the given affects versions on the selected issue.
     *
     * @param issueKey issue key
     * @param affectsVersions the names of the versions e.g. <code>New Version 4</code>
     */
    void setAffectsVersions(String issueKey, String... affectsVersions);

    /**
     * Sets the multi-select field to the options
     *
     * @param issueKey issue key
     * @param fieldId the id of the field e.g. <code>customfield_10000</code>
     * @param options the named options, not the values
     */
    void setIssueMultiSelectField(String issueKey, String fieldId, String... options);

    /**
     * Adds a comment on the given issue, making it visible to all the users who can see the issue.
     * @param issueKey The issue key.
     * @param comment The body of the comment to be added.
     */
    void addComment(String issueKey, String comment);

    /**
     * Adds a comment on the given issue visible only to members the given role.
     *
     * @param issueKey The issue key.
     * @param comment The body of the comment to be added.
     * @param roleLevel role level; use <code>null</code> to not select any role level.
     */
    void addComment(String issueKey, String comment, String roleLevel);

    /**
     * Log work against an issue
     *
     * @param issueKey the issue to work against
     * @param timeLogged the duration string (i.e. "2h") representing the work don
     */
    void logWork(String issueKey, String timeLogged);

    /**
     * Reopens a resolved issue.
     *
     * @param issueKey the issue key to reopen.
     */
    void reopenIssue(String issueKey);

    /**
     * Performs given action on issue. Action need not have any dialog (like comment, set resolution field etc).
     *
     * @param issueKey the issue key to reopen.
     */
    void performIssueActionWithoutDetailsDialog(String issueKey, String actionName);

    /**
     * Unwatches the issue for the current user.
     *
     * @param issueKey the issue key
     */
    void unwatchIssue(String issueKey);

    /**
     * Removes the user's vote for the issue. Note: you can only remove your vote for an issue if it is not resolved.
     *
     * @param issueKey the issue key
     */
    void unvoteIssue(String issueKey);

    /**
     * Add the user's vote for the issue
     * @param issueKey the issue key
     */
    void voteIssue(String issueKey);

    /**
     * Starts watching an issue if not already watched.
     *
     * @param issueKey the issue to watch
     */
    void watchIssue(String issueKey);


    /**
     * Add watchers to an issue.
     *
     * @param issueKey key of the issue
     * @param usernames usernames of users to add as watchers
     * @return this issue navigation instance
     */
    IssueNavigation addWatchers(String issueKey, String... usernames);

    /**
     * Logs work on the issue with the given key.
     *
     * @param issueKey the key of the issue to log work on.
     * @param timeLogged formatted time spent e.g. 1h 30m.
     * @param newEstimate formatted new estimate e.g. 1d 2h.
     */
    void logWork(String issueKey, String timeLogged, String newEstimate);

    /**
     * Logs work on the issue with the given key.
     *
     * @param issueKey the key of the issue to log work on.
     * @param timeLogged formatted time spent e.g. 1h 30m.
     * @param comment comment to add while logging the work
     */
    void logWorkWithComment(String issueKey, String timeLogged, String comment);

    /**
     * Goes to the create issue form.
     *
     * @param projectName The project for the issue to be created in the form.
     * @param issueType The issue type for the issue to be created in the form.
     */
    void goToCreateIssueForm(@Nullable String projectName, @Nullable String issueType);

    /**
     * <p>Sets the original estimate on an issue and submits this change.</p> <p><em>NOTE:</em>This method assumes time
     * tracking is enabled</p>
     *
     * @param issueKey The issue that the original estimate will be set on.
     * @param newValue The value to set as the original estimate.
     */
    void setOriginalEstimate(String issueKey, String newValue);

    /**
     * <p>Sets the remaining estimate on an issue and submits this change.</p> <p><em>NOTE:</em>This method assumes time
     * tracking is enabled</p>
     *
     * @param issueKey The issue that the remaining estimate will be set on.
     * @param newValue The value to set as the remaining estimate.
     */
    void setRemainingEstimate(String issueKey, String newValue);

    /**
     * <p>Sets the original and remaining estimate on an issue and submits this change.</p>
     * <p><em>NOTE:</em>This method assumes time tracking is enabled</p>
     * @param issueKey The issue that the remaining estimate will be set on.
     * @param originalEstimate The value to set as the original estimate.
     * @param remainingEstimate The value to set as the remaining estimate.
     */
    void setEstimates(String issueKey, String originalEstimate, String remainingEstimate);

    /**
     * Resolves an issue with the given resolution.
     * <p><em>NOTE:</em>This method assumes time tracking is enabled</p>
     * @param issueKey the issue key to resolve.
     * @param resolution the name of the resolution. e.g. <code>Fixed</code>. Case sensitive!
     * @param comment a comment to add
     * @param originalEstimate The value to set as the original estimate.
     * @param remainingEstimate The value to set as the remaining estimate.
     */
    void resolveIssue(String issueKey, String resolution, String comment, String originalEstimate,
            String remainingEstimate);

    /**
     * Resolves an issue with the given resolution.
     *
     * @param issueKey the issue key to resolve.
     * @param resolution the name of the resolution. e.g. <code>Fixed</code>. Case sensitive!
     * @param comment a comment to add
     */
    void resolveIssue(String issueKey, String resolution, String comment);

    /**
     * Sets the due date for an issue to the date string passed in
     * @param issueKey
     * @param dateString
     */
    void setDueDate(String issueKey, String dateString);

    /**
     * Closes an issue with the given resolution.
     *
     * @param issueKey the issue key to resolve.
     * @param resolution the name of the resolution. e.g. <code>Fixed</code>. Case sensitive!
     * @param comment a comment to add
     */
    void closeIssue(String issueKey, String resolution, String comment);

    /**
     * Returns a representation of the attachments block on the view issue page for a specific issue.
     * @param issueKey the key of the issue.
     * @return An {@link com.atlassian.jira.functest.framework.navigation.issue.AttachmentsBlock} for the provided issue
     * key.
     */
    AttachmentsBlock attachments(String issueKey);

    /**
     * Go to edit labels of the issue.
     *
     * @param issueId Id of the issue
     * @return this issue navigation instance
     */
    IssueNavigation editLabels(int issueId);

    /**
     * Edit custom labels of the issue.
     *
     * @param issueId ID of the issue
     * @param customFieldId ID of the labels custom field
     * @return this issue navigation instance
     */
    IssueNavigation editCustomLabels(int issueId, int customFieldId);

    /**
     * As of JIRA 6.0, issue pager is rendered client side. It is no longer possible to generate
     * these issue pagers on page request and thus it is not possible to have this return-to-search link on the page.
     *
     * This method will simply navigate to issue navigator with jql="". You can either use the mentioned behaviour or either re-execute the search with the wanted JQL
     *
     * @return an issue navigator navigation instance
     * @deprecated
     */
    IssueNavigatorNavigation returnToSearch();

    /**
     * Gets the id of an issue.
     * @param issueKey The key of the issue in play.
     * @return A {@link String} containing the issue's id.
     */
    String getId(String issueKey);
}