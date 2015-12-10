package com.atlassian.jira.mock.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ComponentConverter;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueKey;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.user.ApplicationUsers;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (c) 2002-2006 All rights reserved.
 */
public class MockIssue implements MutableIssue
{

    // Issue fields
    private Long id;
    private Long projectId;
    private String key;
    private String issueTypeId;
    private String summary;
    private String description;
    private String environment;
    private String reporterId;
    private String creatorId;
    private Timestamp created;
    private Timestamp updated;
    private Timestamp dueDate;
    private Long securityLevelId;
    private String priorityId;
    private String resolutionId;
    private String statusId;
    private Long votes;
    private Long watches;
    private Long originalEstimate;
    private Long estimate;
    private Long workflowId;
    private Long parentId;
    private GenericValue genericValue;
    private Set<Label> labels = new LinkedHashSet<Label>();

    private Map<String, ModifiedValue> modifiedFields;
    private GenericValue project;
    private GenericValue issueType;
    private GenericValue resolution;
    private IssueType issueTypeObject;
    private User assignee;
    private Collection<GenericValue> components;
    private Collection<ProjectComponent> componentObjects;
    private User reporter;
    private User creator;
    private Collection affectedVersions;
    private Resolution resolutionObject;
    private Collection fixVersions;
    private GenericValue securityLevel;
    private GenericValue priority;
    private GenericValue status;
    private Long timeSpent;
    private Status statusObject;
    private Collection subTaskObjects = new ArrayList();
    private Timestamp resolutionDate;
    private boolean stored;
    private Project projectObject;
    private Map<String, Object> externalFields;
    private Long number;

    public MockIssue()
    {
        modifiedFields = new HashMap<String, ModifiedValue>();
        externalFields = new HashMap<String, Object>();
        long now = System.currentTimeMillis();
        created = new Timestamp(now);
        updated = new Timestamp(now);
        dueDate = new Timestamp(now);
        resolutionDate = new Timestamp(now);
    }

    /**
     * This constructor does not have any time precision set by default use MockIssue(Long id, Long now) if you want to
     * preset the Issue's time values.
     */
    public MockIssue(Long id)
    {
        this(id, null);
    }

    /**
     * This constructor does not have any time precision set by default use MockIssue(Long id, Long now) if you want to
     * preset the Issue's time values.
     */
    public MockIssue(long id)
    {
        this(new Long(id), null);
    }

    /**
     * This constructor does not have any time precision set by default use MockIssue(Long id, Long now) if you want to
     * preset the Issue's time values.
     */
    public MockIssue(int id, String key)
    {
        this(new Long(id), null);
        setKey(key);
    }

    /**
     * Use this constructor if you want time precision in your tests
     */
    public MockIssue(Long id, Long now)
    {
        this.id = id;
        if (now != null)
        {
            created = new Timestamp(now.longValue());
            updated = new Timestamp(now.longValue());
            dueDate = new Timestamp(now.longValue());
        }
    }

    /**
     * Use this constructor if you have a gv
     */
    public MockIssue(GenericValue gv)
    {
        setGenericValue(gv);
    }

    public Long getId()
    {
        return id;
    }

    public GenericValue getProject()
    {
        return project;
    }

    public Project getProjectObject()
    {
        if (projectObject == null && projectId != null)
        {
            return new ProjectImpl(new MockGenericValue("project", projectId));
        }
        return projectObject;
    }

    public void setProjectObject(final Project projectObject)
    {
        this.projectObject = projectObject;
        if (projectObject != null)
        {
            this.project = projectObject.getGenericValue();
            this.projectId = projectObject.getId();
        }
        else
        {
            this.project = null;
            this.projectId = null;
        }
    }

    public void setProject(GenericValue project)
    {
        this.project = project;
        this.projectObject = (project != null) ? new ProjectImpl(project) : null;
    }

    public GenericValue getIssueType()
    {
        return issueType;
    }

    public IssueType getIssueTypeObject()
    {
        return issueTypeObject;
    }

    public void setIssueType(GenericValue issueType)
    {
        this.issueType = issueType;
    }

    public void setIssueTypeId(String issueTypeId)
    {
        this.issueTypeId = issueTypeId;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public User getAssigneeUser()
    {
        return assignee;
    }

    public User getAssignee()
    {
        return assignee;
    }

    public String getAssigneeId()
    {
        String key = ApplicationUsers.getKeyFor(assignee);
        if (key == null && assignee != null)
        {
            final String msg = String.format("Cannot find key for assignee. Please mock UserKeyService#getKeyForUsername(\"%s\")", assignee.getName());
            throw new NullPointerException(msg);
        }
        return key;
    }

    public void setAssignee(User assignee)
    {
        this.assignee = assignee;
    }

    @Override
    public void setComponentObjects(Collection<ProjectComponent> components)
    {
        if (components == null)
        {
            components = Collections.emptyList();
        }

        final List<GenericValue> gvList = new ArrayList<GenericValue>(components.size());
        for (ProjectComponent component : components)
        {
            gvList.add(component.getGenericValue());
        }

        this.componentObjects = components;
        this.components = gvList;
    }

    public Collection<GenericValue> getComponents()
    {
        return components;
    }

    public Collection<ProjectComponent> getComponentObjects()
    {
        return componentObjects;
    }

    public void setComponents(Collection<GenericValue> components)
    {
        if (components == null)
        {
            components = Collections.emptyList();
        }

        final List<ProjectComponent> objList = new ArrayList<ProjectComponent>(components.size());
        final ComponentConverter converter = new ComponentConverter();
        for (GenericValue gv : components)
        {
            objList.add(converter.convertToComponent(gv));
        }

        this.components = components;
        this.componentObjects = objList;
    }

    public void setAssigneeId(String assigneeId)
    {
        assignee = ApplicationUsers.toDirectoryUser(ApplicationUsers.byKey(assigneeId));
        if (assignee == null && assigneeId != null)
        {
            final String msg = String.format("Cannot find user for a key '%s'. Please mock UserManager#getUserByKey(\"%s\")", assigneeId, assigneeId);
            throw new NullPointerException(msg);
        }
    }

    public User getReporterUser()
    {
        return reporter;
    }

    public User getReporter()
    {
        return reporter;
    }

    public String getReporterId()
    {
        return reporterId;
    }

    @Override
    public User getCreator()
    {
        return creator;
    }

    @Override
    public String getCreatorId()
    {
        String key = ApplicationUsers.getKeyFor(creator);
        if (key == null && creator != null)
        {
            final String msg = String.format("Cannot find key for creator. Please mock UserKeyService#getKeyForUsername(\"%s\")", creator.getName());
            throw new NullPointerException(msg);
        }
        return key;
    }

    public void setCreatorId(String creatorId)
    {
        creator = ApplicationUsers.toDirectoryUser(ApplicationUsers.byKey(creatorId));
        if (creator == null && creatorId != null)
        {
            final String msg = String.format("Cannot find user for a key '%s'. Please mock UserManager#getUserByKey(\"%s\")", creatorId, creatorId);
            throw new NullPointerException(msg);
        }
    }

    public void setReporter(User reporter)
    {
        this.reporter = reporter;
    }

    public void setReporterId(String reporterId)
    {
        this.reporterId = reporterId;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getEnvironment()
    {
        return environment;
    }

    public void setEnvironment(String environment)
    {
        this.environment = environment;
    }

    public Collection getAffectedVersions()
    {
        return affectedVersions;
    }

    public Collection getFixVersions()
    {
        return fixVersions;
    }

    public Timestamp getDueDate()
    {
        return dueDate;
    }

    public GenericValue getSecurityLevel()
    {
        return null;
    }

    public String getIssueTypeId()
    {
        return issueTypeId;
    }

    public String getPriorityId()
    {
        return priorityId;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public String getResolutionId()
    {
        return resolutionId;
    }

    public Long getSecurityLevelId()
    {
        return securityLevelId;
    }

    public GenericValue getPriority()
    {
        return null;
    }

    public Priority getPriorityObject()
    {
        return null;
    }

    public String getStatusId()
    {
        return statusId;
    }

    public void setAffectedVersions(Collection affectedVersions)
    {
        this.affectedVersions = affectedVersions;
    }

    public void setFixVersions(Collection fixVersions)
    {
        this.fixVersions = fixVersions;
    }

    public void setDueDate(Timestamp dueDate)
    {
        this.dueDate = dueDate;
    }

    public void setSecurityLevel(GenericValue securityLevel)
    {
        this.securityLevel = securityLevel;
    }

    public void setPriority(GenericValue priority)
    {
        this.priority = priority;
    }

    @Override
    public void setPriorityObject(Priority priority)
    {
        if (priority == null)
        {
            setPriority(null);
        }
        else
        {
            setPriority(priority.getGenericValue());
        }
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public void setIssueTypeObject(IssueType issueTypeObject)
    {
        this.issueTypeObject = issueTypeObject;
    }

    public void setProjectId(Long projectId)
    {
        this.projectId = projectId;
    }

    public void setSecurityLevelId(Long securityLevelId)
    {
        this.securityLevelId = securityLevelId;
    }


    public void setPriorityId(String priorityId)
    {
        this.priorityId = priorityId;
    }

    public GenericValue getResolution()
    {
        return resolution;
    }

    public void setResolutionObject(Resolution resolutionObject)
    {
        this.resolutionObject = resolutionObject;
    }

    public Resolution getResolutionObject()
    {
        return resolutionObject;
    }

    public void setResolution(GenericValue resolution)
    {
        this.resolution = resolution;
    }

    public String getKey()
    {
        return this.key;
    }

    public void setKey(String key)
    {
        this.key = key;
        if (key == null)
        {
            number = null;
        }
        else
        {
            IssueKey issueKey = IssueKey.from(key);
            this.number = issueKey.getIssueNumber();
        }
    }

    @Override
    public void setNumber(final Long number)
    {
        this.number = number;
    }

    @Override
    public Long getNumber()
    {
        return number;
    }

    public Long getVotes()
    {
        return votes;
    }

    public void setVotes(Long votes)
    {
        this.votes = votes;
    }

    public Long getWatches()
    {
        return watches;
    }

    public void setWatches(Long watches)
    {
        this.watches = watches;
    }

    public Timestamp getCreated()
    {
        return created;
    }

    public void setCreated(Timestamp created)
    {
        this.created = created;
    }

    public Timestamp getUpdated()
    {
        return updated;
    }

    public Timestamp getResolutionDate()
    {
        return resolutionDate;
    }

    public void setResolutionDate(final Timestamp resolutionDate)
    {
        this.resolutionDate = resolutionDate;
    }

    public void setUpdated(Timestamp updated)
    {
        this.updated = updated;
    }

    public Long getWorkflowId()
    {
        return workflowId;
    }

    public Object getCustomFieldValue(CustomField customField)
    {
        return null;
    }

    public GenericValue getStatus()
    {
        return this.status;
    }

    public Status getStatusObject()
    {
        return this.statusObject;
    }

    public void setWorkflowId(Long workflowId)
    {
        this.workflowId = workflowId;
    }

    public void setStatusId(String statusId)
    {
        this.statusId = statusId;
    }

    public Map<String, ModifiedValue> getModifiedFields()
    {
        return this.modifiedFields;
    }

    public void setParentObject(Issue parentIssue)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public void setModifiedFields(Map modifiedFields)
    {
        this.modifiedFields = modifiedFields;
    }


    public void resetModifiedFields()
    {
        modifiedFields.clear();
    }

    public boolean isSubTask()
    {
        return false;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public boolean isCreated()
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public GenericValue getParent()
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public Issue getParentObject()
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public Collection getSubTasks()
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public Collection getSubTaskObjects()
    {
        return subTaskObjects;
    }

    public String getString(String name)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public Timestamp getTimestamp(String name)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public Long getLong(String name)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public GenericValue getGenericValue()
    {
        if (genericValue == null)
        {
            return getHackedGVThatReturnsId();
        }
        return genericValue;
    }

    protected GenericValue getHackedGVThatReturnsId()
    {
        MockGenericValue gv = new MockGenericValue("Issue");
        gv.set("id", getId());
        gv.set(IssueFieldConstants.ISSUE_KEY, getId());
        gv.set(IssueFieldConstants.UPDATED, getUpdated());
        return gv;
    }

    public void setGenericValue(GenericValue genericValue)
    {
        this.genericValue = genericValue;

        // adding these as necessary, if you need more, then add 'em...
        setId(genericValue.getLong("id"));
        setKey(genericValue.getString("key"));
        setProjectId(genericValue.getLong(IssueFieldConstants.PROJECT));
        setSummary(genericValue.getString(IssueFieldConstants.SUMMARY));
        setDescription(genericValue.getString(IssueFieldConstants.DESCRIPTION));
        setOriginalEstimate(genericValue.getLong(IssueFieldConstants.TIME_ORIGINAL_ESTIMATE));
        setEstimate(genericValue.getLong(IssueFieldConstants.TIME_ESTIMATE));
        setTimeSpent(genericValue.getLong(IssueFieldConstants.TIME_SPENT));
        setUpdated(genericValue.getTimestamp(IssueFieldConstants.UPDATED));

        setIssueTypeId(genericValue.getString("type"));
        setEnvironment(genericValue.getString(IssueFieldConstants.ENVIRONMENT));
        setAssigneeId(genericValue.getString(IssueFieldConstants.ASSIGNEE));
        setReporterId(genericValue.getString(IssueFieldConstants.REPORTER));
        setCreatorId(genericValue.getString(IssueFieldConstants.CREATOR));
        setDueDate(genericValue.getTimestamp(IssueFieldConstants.DUE_DATE));
        setSecurityLevelId(genericValue.getLong(IssueFieldConstants.SECURITY));
        setPriorityId(genericValue.getString(IssueFieldConstants.PRIORITY));
        setStatusId(genericValue.getString(IssueFieldConstants.STATUS));
        setResolutionId(genericValue.getString(IssueFieldConstants.RESOLUTION));
        setCreated(genericValue.getTimestamp(IssueFieldConstants.CREATED));
        setResolutionDate(genericValue.getTimestamp(IssueFieldConstants.RESOLUTION_DATE));
        setVotes(genericValue.getLong(IssueFieldConstants.VOTES));
        setWatches(genericValue.getLong(IssueFieldConstants.WATCHES));
        setWorkflowId(genericValue.getLong("workflowId"));

    }

    public void store()
    {
        this.stored = true;
    }

    public boolean isStored()
    {
        return stored;
    }

    public void setResolutionId(String resolutionId)
    {
        this.resolutionId = resolutionId;
    }

    public boolean isEditable()
    {
        return true;
    }

    public IssueRenderContext getIssueRenderContext()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Collection getAttachments()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void setCustomFieldValue(CustomField customField, Object value)
    {
    }

    public void setExternalFieldValue(String fieldId, Object value)
    {
        setExternalFieldValue(fieldId, null, value);
    }

    public void setExternalFieldValue(final String fieldId, final Object oldValue, final Object newValue)
    {
        ModifiedValue modifiedValue = new ModifiedValue(oldValue, newValue);
        externalFields.put(fieldId, newValue);
        modifiedFields.put(fieldId, modifiedValue);
    }

    public void setStatus(GenericValue status)
    {
        this.status = status;
    }

    @Override
    public void setStatusObject(Status status)
    {
        if (status == null)
        {
            setStatus(null);
        }
        else
        {
            setStatus(status.getGenericValue());
        }
    }

    public void setTimeSpent(Long timespent)
    {
        this.timeSpent = timespent;
    }

    public Long getEstimate()
    {
        return estimate;
    }

    public Long getTimeSpent()
    {
        return timeSpent;
    }

    public void setLabels(final Set<Label> labels)
    {
        this.labels = new LinkedHashSet<Label>(labels);
    }

    public Set<Label> getLabels()
    {
        return labels;
    }

    public Object getExternalFieldValue(String fieldId)
    {
        return externalFields.get(fieldId);
    }

    public Long getOriginalEstimate()
    {
        return originalEstimate;
    }

    public void setEstimate(Long estimate)
    {
        this.estimate = estimate;
    }

    public void setOriginalEstimate(Long originalEstimate)
    {
        this.originalEstimate = originalEstimate;
    }

    public void setSubTaskObjects(Collection subTaskObjects)
    {
        this.subTaskObjects = subTaskObjects;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final MockIssue issue = (MockIssue) o;

        if (affectedVersions != null ? !affectedVersions.equals(issue.affectedVersions) : issue.affectedVersions != null)
        {
            return false;
        }
        if (assignee != null ? !assignee.equals(issue.assignee) : issue.assignee != null)
        {
            return false;
        }
        if (components != null ? !components.equals(issue.components) : issue.components != null)
        {
            return false;
        }
        if (created != null ? !created.equals(issue.created) : issue.created != null)
        {
            return false;
        }
        if (description != null ? !description.equals(issue.description) : issue.description != null)
        {
            return false;
        }
        if (dueDate != null ? !dueDate.equals(issue.dueDate) : issue.dueDate != null)
        {
            return false;
        }
        if (environment != null ? !environment.equals(issue.environment) : issue.environment != null)
        {
            return false;
        }
        if (estimate != null ? !estimate.equals(issue.estimate) : issue.estimate != null)
        {
            return false;
        }
        if (fixVersions != null ? !fixVersions.equals(issue.fixVersions) : issue.fixVersions != null)
        {
            return false;
        }
        if (genericValue != null ? !genericValue.equals(issue.genericValue) : issue.genericValue != null)
        {
            return false;
        }
        if (!id.equals(issue.id))
        {
            return false;
        }
        if (issueType != null ? !issueType.equals(issue.issueType) : issue.issueType != null)
        {
            return false;
        }
        if (issueTypeId != null ? !issueTypeId.equals(issue.issueTypeId) : issue.issueTypeId != null)
        {
            return false;
        }
        if (issueTypeObject != null ? !issueTypeObject.equals(issue.issueTypeObject) : issue.issueTypeObject != null)
        {
            return false;
        }
        if (key != null ? !key.equals(issue.key) : issue.key != null)
        {
            return false;
        }
        if (originalEstimate != null ? !originalEstimate.equals(issue.originalEstimate) : issue.originalEstimate != null)
        {
            return false;
        }
        if (parentId != null ? !parentId.equals(issue.parentId) : issue.parentId != null)
        {
            return false;
        }
        if (priority != null ? !priority.equals(issue.priority) : issue.priority != null)
        {
            return false;
        }
        if (priorityId != null ? !priorityId.equals(issue.priorityId) : issue.priorityId != null)
        {
            return false;
        }
        if (project != null ? !project.equals(issue.project) : issue.project != null)
        {
            return false;
        }
        if (projectId != null ? !projectId.equals(issue.projectId) : issue.projectId != null)
        {
            return false;
        }
        if (reporter != null ? !reporter.equals(issue.reporter) : issue.reporter != null)
        {
            return false;
        }
        if (reporterId != null ? !reporterId.equals(issue.reporterId) : issue.reporterId != null)
        {
            return false;
        }
        if (resolution != null ? !resolution.equals(issue.resolution) : issue.resolution != null)
        {
            return false;
        }
        if (resolutionId != null ? !resolutionId.equals(issue.resolutionId) : issue.resolutionId != null)
        {
            return false;
        }
        if (resolutionObject != null ? !resolutionObject.equals(issue.resolutionObject) : issue.resolutionObject != null)
        {
            return false;
        }
        if (securityLevel != null ? !securityLevel.equals(issue.securityLevel) : issue.securityLevel != null)
        {
            return false;
        }
        if (securityLevelId != null ? !securityLevelId.equals(issue.securityLevelId) : issue.securityLevelId != null)
        {
            return false;
        }
        if (status != null ? !status.equals(issue.status) : issue.status != null)
        {
            return false;
        }
        if (statusId != null ? !statusId.equals(issue.statusId) : issue.statusId != null)
        {
            return false;
        }
        if (statusObject != null ? !statusObject.equals(issue.statusObject) : issue.statusObject != null)
        {
            return false;
        }
        if (summary != null ? !summary.equals(issue.summary) : issue.summary != null)
        {
            return false;
        }
        if (timeSpent != null ? !timeSpent.equals(issue.timeSpent) : issue.timeSpent != null)
        {
            return false;
        }
        if (updated != null ? !updated.equals(issue.updated) : issue.updated != null)
        {
            return false;
        }
        if (votes != null ? !votes.equals(issue.votes) : issue.votes != null)
        {
            return false;
        }
        if (watches != null ? !watches.equals(issue.watches) : issue.watches != null)
        {
            return false;
        }
        if (labels != null ? !labels.equals(issue.labels) : issue.labels != null)
        {
            return false;
        }
        if (workflowId != null ? !workflowId.equals(issue.workflowId) : issue.workflowId != null)
        {
            return false;
        }
        if (projectObject != null ? !projectObject.equals(issue.projectObject) : issue.projectObject != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public String toString()
    {
        return String.format("Mock Issue %d", id);
    }
}
