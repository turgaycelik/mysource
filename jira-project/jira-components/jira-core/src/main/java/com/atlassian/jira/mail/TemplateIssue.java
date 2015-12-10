/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculatorFactory;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.JiraKeyUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

/**
 * Decorator for the Issue Object.
 * <p/>
 * This class will ensure backwards compatibility for templates accessing the issue object. This class also includes
 * helper methods for retrieving rendered fields (e.g. description, environment) and nicely formatted date fields
 * (e.g. Original Time Estimate, Time Spent, etc.).
 * <p/>
 * This object is passed to the template through the {@link TemplateContext} for the velocity email templates
 */
public class TemplateIssue implements Issue
{
    private static final Logger log = Logger.getLogger(TemplateIssue.class);

    private final Issue issue;
    private final FieldLayoutManager fieldLayoutManager;
    private final RendererManager rendererManager;
    private final CustomFieldManager customFieldManager;
    private final JiraDurationUtils jiraDurationUtils;
    private final AggregateTimeTrackingCalculatorFactory aggregateTimeTrackingCalculatorFactory;
    private AggregateTimeTrackingBean aggregateBean;

    public TemplateIssue(Issue issue, FieldLayoutManager fieldLayoutManager, RendererManager rendererManager,
                         CustomFieldManager customFieldManager, JiraDurationUtils jiraDurationUtils,
                         AggregateTimeTrackingCalculatorFactory aggregateTimeTrackingCalculatorFactory)
    {
        this.issue = issue;
        this.fieldLayoutManager = fieldLayoutManager;
        this.rendererManager = rendererManager;
        this.customFieldManager = customFieldManager;
        this.jiraDurationUtils = jiraDurationUtils;
        this.aggregateTimeTrackingCalculatorFactory = aggregateTimeTrackingCalculatorFactory;
    }

    public Long getId()
    {
        return issue.getId();
    }

    public GenericValue getProject()
    {
        return issue.getProject();
    }

    public Project getProjectObject()
    {
        return issue.getProjectObject();
    }

    public Long getProjectId()
    {
        return issue.getProjectId();
    }

    public GenericValue getIssueType()
    {
        return issue.getIssueType();
    }

    public IssueType getIssueTypeObject()
    {
        return issue.getIssueTypeObject();
    }

    public String getIssueTypeId()
    {
        return issue.getIssueTypeId();
    }

    public String getSummary()
    {
        return issue.getSummary();
    }

    public User getAssignee()
    {
        return issue.getAssignee();
    }

    public String getAssigneeId()
    {
        return issue.getAssigneeId();
    }

    public User getAssigneeUser()
    {
        // Issue.getAssigneeUser() is now guaranteed to return non-null User object if the username is not blank, but user is not found.
        return issue.getAssigneeUser();
    }

    public Collection<GenericValue> getComponents()
    {
        return issue.getComponents();
    }

    public Collection<ProjectComponent> getComponentObjects()
    {
        return issue.getComponentObjects();
    }

    public User getReporter()
    {
        return issue.getReporter();
    }

    public String getReporterId()
    {
        return issue.getReporterId();
    }

    @Override
    public User getCreator()
    {
        return issue.getCreator();
    }

    @Override
    public String getCreatorId()
    {
        return issue.getCreatorId();
    }

    /**
     * Return the reporter - an object will be returned even if the user has been deleted from the system.
     *
     * @return Object       a User or DummyUser object if the issue has an reporter
     */
    public User getReporterUser()
    {
        // Issue.getReporterUser() is now guaranteed to return non-null User object if user is deleted.
        return issue.getReporterUser();
    }

    /**
     * Retrieve the description of the issue.
     *
     * @return String       the issue description - rendered or text dependant on format
     */
    public String getDescription()
    {
        return issue.getDescription();
    }

    /**
     * Retrieves the html formatted description.
     * <p/>
     * A simple string (with linked bug keys displayed) is returned if a rendered version cannot be generated.
     *
     * @return String   the html formatted description.
     */
    public String getHtmlDescription()
    {
        // Try to generate rendered values for description
        try
        {
            FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(issue);
            FieldLayoutItem fieldLayoutItem = fieldLayout.getFieldLayoutItem(IssueFieldConstants.DESCRIPTION);
            String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;

            String renderedContent = rendererManager.getRenderedContent(rendererType, issue.getDescription(), issue.getIssueRenderContext());
            return renderedContent.replaceAll("<p>", "<p style='margin-top:0;margin-bottom:10px;'>");
        }
        catch (Exception e)
        {
            log.warn("Unable to produce rendered version of the description for the issue " + issue.getKey(), e);
            return JiraKeyUtils.linkBugKeys(issue.getDescription());
        }
    }

    /**
     * Retrieve the environment of the issue.
     * Depending on the format specified - attempt to return the rendered description or the simple text
     *
     * @return String       the issue environment - rendered or text dependant on format specified
     */
    public String getEnvironment()
    {
        return issue.getEnvironment();
    }

    /**
     * Retrieves the html formatted environment.
     * <p/>
     * A simple string (with linked bug keys displayed) is returned if a rendered version cannot be generated.
     *
     * @return String   the html formatted environment.
     */
    public String getHtmlEnvironment()
    {
        // Try to generate rendered values for environment
        try
        {
            FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(issue);
            FieldLayoutItem fieldLayoutItem = fieldLayout.getFieldLayoutItem(IssueFieldConstants.ENVIRONMENT);
            String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;

            String renderedContent = rendererManager.getRenderedContent(rendererType, issue.getEnvironment(), issue.getIssueRenderContext());
            return renderedContent.replaceAll("<p>", "<p style='margin-top:0;margin-bottom:10px;'>");
        }
        catch (Exception e)
        {
            log.warn("Unable to produce rendered version of the environment for the issue " + issue.getKey(), e);
            return JiraKeyUtils.linkBugKeys(issue.getEnvironment());
        }
    }

    public Collection<Version> getAffectedVersions()
    {
        return issue.getAffectedVersions();
    }

    public Collection<Version> getFixVersions()
    {
        return issue.getFixVersions();
    }

    public Timestamp getDueDate()
    {
        return issue.getDueDate();
    }

    public GenericValue getSecurityLevel()
    {
        return issue.getSecurityLevel();
    }

    public Long getSecurityLevelId()
    {
        return issue.getSecurityLevelId();
    }

    public GenericValue getPriority()
    {
        return issue.getPriority();
    }

    public Priority getPriorityObject()
    {
        return issue.getPriorityObject();
    }

    public String getResolutionId()
    {
        return issue.getResolutionId();
    }

    public GenericValue getResolution()
    {
        return issue.getResolution();
    }

    public Resolution getResolutionObject()
    {
        return issue.getResolutionObject();
    }

    public String getKey()
    {
        return issue.getKey();
    }

    @Override
    public Long getNumber()
    {
        return issue.getNumber();
    }

    public Long getVotes()
    {
        return issue.getVotes();
    }

    public Long getWatches()
    {
        return issue.getWatches();
    }

    public Timestamp getCreated()
    {
        return issue.getCreated();
    }

    public Timestamp getResolutionDate()
    {
        return issue.getResolutionDate();
    }

    public Timestamp getUpdated()
    {
        return issue.getUpdated();
    }

    public Long getWorkflowId()
    {
        return issue.getWorkflowId();
    }

    public Object getCustomFieldValue(CustomField customField)
    {
        return issue.getCustomFieldValue(customField);
    }

    public CustomField getCustomField(String id)
    {
        return customFieldManager.getCustomFieldObject(id);
    }

    /**
     * Get value for a custom field.
     *
     * @param id Custom field key (eg. 'customfield_10010').
     * @return custom field, or null if specified custom field doesn't exist or doesn't have a value in this issue.
     */
    public Object getCustomFieldValue(String id)
    {
        CustomField cf = customFieldManager.getCustomFieldObject(id);
        if (cf == null)
        {
            log.warn("Velocity template referenced nonexistent custom field '" + id + "'");
            return null;
        }
        return getCustomFieldValue(cf);
    }

    public GenericValue getStatus()
    {
        //noinspection deprecation
        return issue.getStatus();
    }

    public Status getStatusObject()
    {
        return issue.getStatusObject();
    }

    public Long getOriginalEstimate()
    {
        return issue.getOriginalEstimate();
    }

    public Long getEstimate()
    {
        return issue.getEstimate();
    }

    public Long getTimeSpent()
    {
        return issue.getTimeSpent();
    }

    public Long getAggregateOriginalEstimate()
    {
        return getAggregateBean().getOriginalEstimate();
    }

    public Long getAggregateRemainingEstimate()
    {
        return getAggregateBean().getRemainingEstimate();
    }

    public Long getAggregateTimeSpent()
    {
        return getAggregateBean().getTimeSpent();
    }

    private AggregateTimeTrackingBean getAggregateBean()
    {
        if (aggregateBean == null)
        {
            aggregateBean = aggregateTimeTrackingCalculatorFactory.getCalculator(issue).getAggregates(issue);
        }
        return aggregateBean;
    }

    public Object getExternalFieldValue(String fieldId)
    {
        return issue.getExternalFieldValue(fieldId);
    }

    public boolean isSubTask()
    {
        return issue.isSubTask();
    }

    public Long getParentId()
    {
        return issue.getParentId();
    }

    public boolean isCreated()
    {
        return issue.isCreated();
    }

    public Issue getParentObject()
    {
        return issue.getParentObject();
    }

    public GenericValue getParent()
    {
        return issue.getParent();
    }

    public Collection<GenericValue> getSubTasks()
    {
        return issue.getSubTasks();
    }

    public Collection<Issue> getSubTaskObjects()
    {
        return issue.getSubTaskObjects();
    }

    public boolean isEditable()
    {
        return issue.isEditable();
    }

    public IssueRenderContext getIssueRenderContext()
    {
        return issue.getIssueRenderContext();
    }

    public Collection<Attachment> getAttachments()
    {
        return issue.getAttachments();
    }

    public String getString(String name)
    {
        return issue.getString(name);
    }

    public Timestamp getTimestamp(String name)
    {
        return issue.getTimestamp(name);
    }

    public Set<Label> getLabels()
    {
        return issue.getLabels();
    }

    public Long getLong(String name)
    {
        return issue.getLong(name);
    }

    public GenericValue getGenericValue()
    {
        return issue.getGenericValue();
    }

    public void store()
    {
        // Do nothing
    }

    /**
     * Return a formatted time string of the original time estimate.
     *
     * @param i18n helper bean for internationalization
     * @return String       formatted string of original time estimate
     */
    public String getNiceTimeOriginalEstimate(I18nHelper i18n)
    {
        return convertToNiceTime(issue.getOriginalEstimate(), i18n);
    }

    /**
     * Return a formatted time string of the time estimate.
     *
     * @param i18n helper bean for internationalization
     * @return String       formatted string of time estimate
     */
    public String getNiceTimeEstimate(I18nHelper i18n)
    {
        return convertToNiceTime(issue.getEstimate(), i18n);
    }


    /**
     * Return a formatted time string of the time spent.
     *
     * @param i18n helper bean for internationalization
     * @return String       formatted string of time spent
     */
    public String getNiceTimeSpent(I18nHelper i18n)
    {
        return convertToNiceTime(issue.getTimeSpent(), i18n);
    }


    /**
     * Are any of the aggregates different?
     *
     * @return true if any of the aggregate time tracking values are different, false otherwise
     */
    public boolean isAnyAggregateDifferent()
    {
        return isTimeAndAggregateDifferent(getOriginalEstimate(), getAggregateOriginalEstimate()) ||
                isTimeAndAggregateDifferent(getEstimate(), getAggregateRemainingEstimate()) ||
                isTimeAndAggregateDifferent(getTimeSpent(), getAggregateTimeSpent());
    }

    /**
     * Are 2 Long durations different?
     *
     * @param time      Duration of this issue
     * @param aggregate Duration of this + subtasks
     * @return true if they are not equal, false otherwise
     */
    private boolean isTimeAndAggregateDifferent(Long time, Long aggregate)
    {
        if (time == null)
        {
            return aggregate != null;
        }

        return !time.equals(aggregate);
    }

    /**
     * Return a formatted time string of the aggregate original time estimate.
     *
     * @param i18n helper bean for internationalization
     * @return String       formatted string of original time estimate
     */
    public String getNiceTimeAggregateOriginalEstimate(I18nHelper i18n)
    {
        return convertToNiceTime(getAggregateOriginalEstimate(), i18n);
    }

    /**
     * Return a formatted time string of the aggregate time estimate.
     *
     * @param i18n helper bean for internationalization
     * @return String       formatted string of time estimate
     */
    public String getNiceTimeAggregateRemainingEstimate(I18nHelper i18n)
    {
        return convertToNiceTime(getAggregateRemainingEstimate(), i18n);
    }

    /**
     * Return a formatted time string of the aggregate time spent.
     *
     * @param i18n helper bean for internationalization
     * @return String       formatted string of time spent
     */
    public String getNiceTimeAggregateTimeSpent(I18nHelper i18n)
    {
        return convertToNiceTime(getAggregateTimeSpent(), i18n);
    }

    /**
     * Converts the given time to nice i18n form or returns i18n [none] if null is passed in.
     *
     * @param time time to convert to String
     * @param i18n internationalizing helper
     * @return time sa String
     */
    private String convertToNiceTime(Long time, I18nHelper i18n)
    {
        if (time == null)
        {
            return i18n.getText("viewissue.timetracking.unknown");
        }
        else
        {
            return jiraDurationUtils.getFormattedDuration(time, i18n.getLocale());
        }
    }

    @Override
    public String toString()
    {
        return "TemplateIssue[issue=" + issue + ']';
    }
}
