package com.atlassian.jira.external.beans;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.label.Label;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

public class ExternalIssueImpl implements ExternalIssue
{
    // @TODO refactor Remote RPC objects to use this
    private static final Logger log = Logger.getLogger(ExternalIssueImpl.class);

    private String oldId;
    private String id;
    private String key;
    private String summary;
    private String reporter;
    private String assignee;
    private String description;
    private String environment;
    private String project;
    private String issueType;
    private String status;
    private String priority;
    private String resolution;
    private String securityLevel;
    private Date created;
    private Date updated;
    private Date resolutionDate;
    private Date duedate;
    private Long votes;
    private Long originalEstimate;
    private Long timeSpent;
    private Long estimate;
    private final String creator;

    private ExternalProject externalProject;
    private List<String> affectedVersions;
    private List<String> fixedVersions;
    private List<String> externalComponents;
    private List<ExternalComment> externalComments;
    private List<ExternalCustomFieldValue> externalCustomFieldValues;
    private Set<Label> labels;


    /**
     * Creates a new ExternalIssueImpl and sets the immutable creator value;
     *
     * @param creator the user key of the user creating the issue - null means the anonymous user
     */
    public ExternalIssueImpl(@Nullable final String creator)
    {
        this.creator = creator;
    }

    public void setLabels(final Set<Label> labels)
    {
        this.labels = labels;
    }

    public Set<Label> getLabels()
    {
        return this.labels;
    }

    public String getOldId()
    {
        return oldId;
    }

    public void setOldId(final String oldId)
    {
        this.oldId = oldId;
    }

    public ExternalProject getExternalProject()
    {
        return externalProject;
    }

    public void setExternalProject(final ExternalProject externalProject)
    {
        this.externalProject = externalProject;
    }

    public List<String> getAffectedVersions()
    {
        return affectedVersions;
    }

    public void setAffectedVersions(final List<String> affectedVersions)
    {
        this.affectedVersions = affectedVersions;
    }

    public List<String> getFixedVersions()
    {
        return fixedVersions;
    }

    public void setFixedVersions(final List<String> fixedVersions)
    {
        this.fixedVersions = fixedVersions;
    }

    public List<String> getExternalComponents()
    {
        return externalComponents;
    }

    public void setExternalComponents(final List<String> externalComponents)
    {
        this.externalComponents = externalComponents;
    }

    public List<ExternalComment> getExternalComments()
    {
        return externalComments;
    }

    public void setExternalComments(final List<ExternalComment> externalComments)
    {
        this.externalComments = externalComments;
    }

    public List<ExternalCustomFieldValue> getExternalCustomFieldValues()
    {
        return externalCustomFieldValues;
    }

    public void setExternalCustomFieldValues(final List<ExternalCustomFieldValue> externalCustomFieldValues)
    {
        this.externalCustomFieldValues = externalCustomFieldValues;
    }

    public String getId()
    {
        return id;
    }

    public void setId(final String id)
    {
        this.id = id;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(final String key)
    {
        this.key = key;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(final String summary)
    {
        this.summary = summary;
    }

    public String getReporter()
    {
        return reporter;
    }

    public void setReporter(final String reporter)
    {
        this.reporter = reporter;
    }

    public String getAssignee()
    {
        return assignee;
    }

    public void setAssignee(final String assignee)
    {
        this.assignee = assignee;
    }

    public String getCreator()
    {
        return  this.creator;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    public String getEnvironment()
    {
        return environment;
    }

    public void setEnvironment(final String environment)
    {
        this.environment = environment;
    }

    public String getProject()
    {
        return project;
    }

    public void setProject(final String project)
    {
        this.project = project;
    }

    public String getIssueType()
    {
        return issueType;
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

    public String getPriority()
    {
        return priority;
    }

    public void setPriority(final String priority)
    {
        this.priority = priority;
    }

    public String getResolution()
    {
        return resolution;
    }

    public void setResolution(final String resolution)
    {
        this.resolution = resolution;
    }

    public Date getCreated()
    {
        return created;
    }

    public void setCreated(final Date created)
    {
        this.created = created;
    }

    public Date getUpdated()
    {
        return updated;
    }

    public void setUpdated(final Date updated)
    {
        this.updated = updated;
    }

    public Date getResolutionDate()
    {
        return resolutionDate;
    }

    public void setResolutionDate(final Date resolutionDate)
    {
        this.resolutionDate = resolutionDate;
    }

    public Date getDuedate()
    {
        return duedate;
    }

    public void setDuedate(final Date duedate)
    {
        this.duedate = duedate;
    }

    public Long getVotes()
    {
        return votes;
    }

    public void setVotes(final Long votes)
    {
        this.votes = votes;
    }

    public Long getOriginalEstimate()
    {
        return originalEstimate;
    }

    public void setOriginalEstimate(final Long originalEstimate)
    {
        this.originalEstimate = originalEstimate;
    }

    public Long getTimeSpent()
    {
        return timeSpent;
    }

    public void setTimeSpent(final Long timeSpent)
    {
        this.timeSpent = timeSpent;
    }

    public Long getEstimate()
    {
        return estimate;
    }

    public void setEstimate(final Long estimate)
    {
        this.estimate = estimate;
    }

    public String getSecurityLevel()
    {
        return securityLevel;
    }

    public void setSecurityLevel(final String securityLevel)
    {
        this.securityLevel = securityLevel;
    }

    public void setField(String fieldName, final String value)
    {
        try
        {
            fieldName = processFieldName(fieldName);

            BeanUtils.setProperty(this, fieldName, value);
        }
        catch (final Exception e)
        {
            log.warn("Unable to set field using reflection for :" + fieldName + ":" + value);
        }
    }

    public String getField(String fieldName)
    {
        try
        {
            fieldName = processFieldName(fieldName);
            return BeanUtils.getProperty(this, fieldName);
        }
        catch (final Exception e)
        {
            log.warn("Unable to get field using reflection for :" + fieldName);
            return null;
        }
    }

    private String processFieldName(String fieldName)
    {
        if (ConstantsManager.ISSUE_TYPE_CONSTANT_TYPE.equalsIgnoreCase(fieldName))
        {
            fieldName = "issueType";
        }
        return fieldName;
    }

    @Override
    public String toString()
    {
        return "Issue {summary=" + summary + ", type=" + issueType + (project != null ? ", project=" + project : "") + "}";
    }
}
