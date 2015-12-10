package com.atlassian.jira.issue;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.api.IncompatibleReturnType;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.entity.WithId;
import com.atlassian.jira.entity.WithKey;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.ofbiz.OfBizValueWrapper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;

import org.ofbiz.core.entity.GenericValue;

/**
 * Main issue interface. Historically, JIRA has just passed around {@link GenericValue}s describing issue records. Issue
 * is essentially a GenericValue wrapper, with setters, getters and a {@link #store} operation for persisting changes
 * through the underlying GenericValue.
 * <p/>
 * Amongst other means, Issue objects may be obtained with {@link IssueManager#getIssueObject(String)}, or converted
 * using {@link IssueFactory}.
 */
@PublicApi
public interface Issue extends OfBizValueWrapper, IssueContext, WithId, WithKey
{
    @Override
    Long getId();

    /**
     * @deprecated please use getProjectObject (this still implicitly relies on the GV), but it is a start!
     */
    @Deprecated
    GenericValue getProject();

    /**
     * Gets the Project for this Issue.
     *
     * @return The Project for this Issue.
     */
    Project getProjectObject();

    /**
     * Gets the ID of the Project for this Issue.
     *
     * @return The ID of the Project for this Issue.
     */
    Long getProjectId();

    /**
     * Gets the IssueType for this Issue.
     *
     * @return The IssueType for this Issue.
     *
     * @deprecated Please use {@link #getIssueTypeObject()}. Deprecated since v4.0
     */
    @Deprecated
    GenericValue getIssueType();

    /**
     * Gets the IssueType for this Issue.
     *
     * @return The IssueType for this Issue.
     */
    IssueType getIssueTypeObject();

    /**
     * Gets the ID of the IssueType for this Issue.
     *
     * @return The ID of the IssueType for this Issue.
     */
    String getIssueTypeId();

    String getSummary();

    /**
     * Returns the Assignee User.
     * <p/>
     * If there is no assignee it returns null, else it is guaranteed to return a non-null User. If the User is no
     * longer available, it will create a dummy User object based on the username.
     * <p/>
     * Legacy synonym for {@link #getAssignee()}
     *
     * @return the Assignee User.
     * @see #getAssignee()
     * @since 4.3
     */
    User getAssigneeUser();

    /**
     * Returns the Assignee User.
     * <p/>
     * <b>Warning:</b> previous incarnations of this method returned <code>com.opensymphony.user.User</code>. This class
     * has now been removed from the JIRA API, meaning that the 5.0 version is not binary or source compatible with
     * earlier versions.
     *
     * @return the Assignee User.
     */
    @IncompatibleReturnType (since = "5.0", was = "com.opensymphony.user.User")
    User getAssignee();

    String getAssigneeId();

    /**
     * @return A collection of component GenericValues.
     *
     * @deprecated Use {@link #getComponentObjects()} instead. Since v5.2.
     */
    Collection<GenericValue> getComponents();

    /**
     * @since 4.2
     * @return collection of project components (as objects) that this issue is assigned to
     */
    Collection<ProjectComponent> getComponentObjects();

    /**
     * Returns the Reporter User.
     * <p/>
     * This will return a non-null User object even if the User has been deleted.
     * <p/>
     * Legacy synonym for {@link #getReporter()}.
     *
     * @return the Reporter User.
     * @since 4.3
     * @see #getReporter()
     */
    User getReporterUser();

    /**
     * Returns the Reporter User.
     * <p/>
     * <b>Warning:</b> previous incarnations of this method returned <code>com.opensymphony.user.User</code>. This class
     * has now been removed from the JIRA API, meaning that the 5.0 version is not binary or source compatible with
     * earlier versions.
     *
     * @return the Reporter User.
     */
    @IncompatibleReturnType (since = "5.0", was = "com.opensymphony.user.User")
    User getReporter();

    String getReporterId();

    User getCreator();

    String getCreatorId();

    String getDescription();

    String getEnvironment();

    /**
     * @return a collection of 'affects' {@link com.atlassian.jira.project.version.Version} objects.
     */
    Collection<Version> getAffectedVersions();

    /**
     * @return a collection of fix-for {@link com.atlassian.jira.project.version.Version} objects.
     */
    Collection<Version> getFixVersions();

    Timestamp getDueDate();

    /**
     * Returns the Security Level for this Issue.
     * @return the Security Level for this Issue.
     *
     * @deprecated Use {@link #getSecurityLevelId()} instead. Since v5.2.
     */
    GenericValue getSecurityLevel();

    /**
     * Returns the Security Level for this Issue.
     * @return the Security Level for this Issue.
     */
    Long getSecurityLevelId();

    /**
     * Returns the Priority for this Issue.
     * @return the Priority for this Issue.
     *
     * @deprecated Use {@link #getPriorityObject()} instead. Since v5.2.
     */
    @Nullable
    GenericValue getPriority();

    /**
     * Returns the Priority for this Issue.
     * @return the Priority for this Issue.
     */
    @Nullable
    Priority getPriorityObject();

    String getResolutionId();

    /**
     * Returns the Resolution for this Issue.
     * @return the Resolution for this Issue.
     *
     * @deprecated Use {@link #getResolutionObject()} instead. Since v5.2.
     */
    GenericValue getResolution();

    /**
     * Returns the Resolution for this Issue.
     * @return the Resolution for this Issue.
     */
    Resolution getResolutionObject();

    String getKey();

    Long getNumber();

    Long getVotes();

    Long getWatches();

    Timestamp getCreated();

    Timestamp getUpdated();

    /**
     * Returns the datetime that an issue was resolved on.  Will be null if it hasn't been resolved yet, or if an issue
     * has been returned to the 'unresolved' state.
     *
     * @return Timestamp of when an issue was resolved, or null
     */
    Timestamp getResolutionDate();

    Long getWorkflowId();

    /**
     * @param customField the CustomField
     * @return A custom field's value. Will be a List, User, Timestamp etc, depending on custom field type.
     */
    Object getCustomFieldValue(CustomField customField);

    /**
     * @deprecated since 4.2. Use {@link #getStatusObject} instead.
     */
    @Deprecated
    GenericValue getStatus();

    Status getStatusObject();

    /**
     * This is the "original estimate" of work to be performed on this issue, in milliseconds.
     *
     * @return the "original estimate" of work to be performed on this issue, in milliseconds.
     */
    Long getOriginalEstimate();

    /**
     * This is the "remaining estimate" of work left to be performed on this issue, in milliseconds.
     * <p/>
     * A better name would be getRemainingEstimate but for historical reasons it is called what it is called.
     *
     * @return the "remaining estimate" of work left to be performed on this issue, in milliseconds.
     */
    Long getEstimate();

    /**
     * This is the "total time spent" working on this issue, in milliseconds.
     *
     * @return the "total time spent" working on this issue, in milliseconds.
     */
    Long getTimeSpent();

    Object getExternalFieldValue(String fieldId);

    boolean isSubTask();

    Long getParentId();

    boolean isCreated();

    /**
     * If this issue is a subtask, return its parent.
     *
     * @return The parent Issue, or null if the issue is not a subtask.
     */
    Issue getParentObject();

    /**
     * @deprecated Use {@link #getParentObject()} instead.
     */
    @Deprecated
    GenericValue getParent();

    /**
     * @deprecated Use {@link #getSubTaskObjects()}
     */
    @Deprecated
    Collection<GenericValue> getSubTasks();

    /**
     * Gets all the issue's subtasks.
     *
     * @return A collection of {@link MutableIssue}s
     */
    Collection<Issue> getSubTaskObjects();

    boolean isEditable();

    IssueRenderContext getIssueRenderContext();

    /**
     * @return A collection of {@link com.atlassian.jira.issue.attachment.Attachment} objects
     */
    Collection<Attachment> getAttachments();

    /**
     * Returns a set of all the labels for this issue or an empty set if none exist yet.
     *
     * @return a set of all the labels for this issue or an empty set if none exist yet
     */
    Set<Label> getLabels();

    /**
     * For interactivity, implementations must be based on the issue key.
     * See the implementation in AbstractIssue for an example.
     *
     * @param o the other object
     * @return true if the other Issue object has the same key.
     */
    @Override
    boolean equals(Object o);

    /**
     * For interactivity, implementations must be based on the hashcode of the issue key.
     * See the implementation in AbstractIssue for an example.
     *
     * @return the Hashcode of the issue key (or 0 for null).
     */
    @Override
    int hashCode();
}
