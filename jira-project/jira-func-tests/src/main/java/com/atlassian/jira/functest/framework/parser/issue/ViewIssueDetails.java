package com.atlassian.jira.functest.framework.parser.issue;

import java.util.List;
import java.util.Map;

/**
 * Holds data for the view issue page and contains issue details.
 *
 * @since v3.13
 */
public class ViewIssueDetails
{
    private String key;
    private int id;
    private String issueType;
    private String status;
    private String resolution;
    private String priority;
    private String summary;
    private String assignee;
    private String reporter;
    private String votes;
    private String watchers;
    private String projectName;
    private String createdDate;
    private String updatedDate;
    private String dueDate;
    private String resolutionDate;
    private String originalEstimate;
    private String remainingEstimate;
    private String timeSpent;
    private String environment;
    private String description;
    private String securityLevel;
    private List<String> availableWorkflowActions;
    private List<String> components;
    private List<String> affectsVersions;
    private List<String> fixVersions;
    private List<String> attachments;
    private List<String> labels;
    private Map<String, String> customFields;

    public String getSecurityLevel()
    {
        return securityLevel;
    }

    public void setSecurityLevel(final String securityLevel)
    {
        this.securityLevel = securityLevel;
    }

    public String getResolution()
    {
        return resolution;
    }

    public void setResolution(final String resolution)
    {
        this.resolution = resolution;
    }    

    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    public Map<String, String> getCustomFields()
    {
        return customFields;
    }

    public boolean customFieldValueContains(String customFieldName, String valueToCheck)
    {
        final String customFieldValue = getCustomFields().get(customFieldName);
        return customFieldValue != null && customFieldValue.indexOf(valueToCheck) != -1;
    }

    public void setCustomFields(final Map<String, String> customFields)
    {
        this.customFields = customFields;
    }

    public String getEnvironment()
    {
        return environment;
    }

    public void setEnvironment(final String environment)
    {
        this.environment = environment;
    }

    public List getAttachments()
    {
        return attachments;
    }

    public void setAttachments(final List<String> attachments)
    {
        this.attachments = attachments;
    }

    public String getOriginalEstimate()
    {
        return originalEstimate;
    }

    public void setOriginalEstimate(final String originalEstimate)
    {
        this.originalEstimate = originalEstimate;
    }

    public String getRemainingEstimate()
    {
        return remainingEstimate;
    }

    public void setRemainingEstimate(final String remainingEstimate)
    {
        this.remainingEstimate = remainingEstimate;
    }

    public String getTimeSpent()
    {
        return timeSpent;
    }

    public void setTimeSpent(final String timeSpent)
    {
        this.timeSpent = timeSpent;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName(final String projectName)
    {
        this.projectName = projectName;
    }

    public String getCreatedDate()
    {
        return createdDate;
    }

    public void setCreatedDate(final String createdDate)
    {
        this.createdDate = createdDate;
    }

    public String getUpdatedDate()
    {
        return updatedDate;
    }

    public void setUpdatedDate(final String updatedDate)
    {
        this.updatedDate = updatedDate;
    }

    public String getDueDate()
    {
        return dueDate;
    }

    public void setDueDate(final String dueDate)
    {
        this.dueDate = dueDate;
    }

    public String getResolutionDate()
    {
        return resolutionDate;
    }

    public void setResolutionDate(final String resolutionDate)
    {
        this.resolutionDate = resolutionDate;
    }

    public String getAssignee()
    {
        return assignee;
    }

    public void setAssignee(final String assignee)
    {
        this.assignee = assignee;
    }

    public String getReporter()
    {
        return reporter;
    }

    public void setReporter(final String reporter)
    {
        this.reporter = reporter;
    }

    public String getVotes()
    {
        return votes;
    }

    public void setVotes(final String votes)
    {
        this.votes = votes;
    }

    public String getWatchers()
    {
        return watchers;
    }

    public void setWatchers(final String watchers)
    {
        this.watchers = watchers;
    }

    public void setKey(final String key)
    {
        this.key = key;
    }

    public void setId(final int id)
    {
        this.id = id;
    }

    public void setIssueType(final String issueType)
    {
        this.issueType = issueType;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(final String status)
    {
        this.status = status;
    }

    public void setPriority(final String priority)
    {
        this.priority = priority;
    }

    public void setSummary(final String summary)
    {
        this.summary = summary;
    }

    public String getKey()
    {
        return key;
    }

    public int getId()
    {
        return id;
    }

    public String getIssueType()
    {
        return issueType;
    }

    public String getPriority()
    {
        return priority;
    }

    public String getSummary()
    {
        return summary;
    }

    public List getAvailableWorkflowActions()
    {
        return availableWorkflowActions;
    }

    public void setAvailableWorkflowActions(final List<String> availableWorkflowActions)
    {
        this.availableWorkflowActions = availableWorkflowActions;
    }

    public List getComponents()
    {
        return components;
    }

    public void setComponents(final List<String> components)
    {
        this.components = components;
    }

    public List getAffectsVersions()
    {
        return affectsVersions;
    }

    public void setAffectsVersions(final List<String> affectsVersions)
    {
        this.affectsVersions = affectsVersions;
    }

    public List getFixVersions()
    {
        return fixVersions;
    }

    public void setFixVersions(final List<String> fixVersions)
    {
        this.fixVersions = fixVersions;
    }

    public List<String> getLabels()
    {
        return labels;
    }

    public void setLabels(final List<String> labels)
    {
        this.labels = labels;
    }
}
