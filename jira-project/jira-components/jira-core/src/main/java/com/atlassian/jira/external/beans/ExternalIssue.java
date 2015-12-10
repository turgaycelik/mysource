package com.atlassian.jira.external.beans;

import com.atlassian.jira.issue.label.Label;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ExternalIssue
{
    ExternalProject getExternalProject();

    void setExternalProject(ExternalProject externalProject);

    /*
     * The version ID string
     */
    List<String> getAffectedVersions();

    void setAffectedVersions(List<String> affectedVersions);

    /*
     * The version ID string
     */
    List<String> getFixedVersions();

    void setFixedVersions(List<String> fixedVersions);

    /*
     * The component ID string
     */
    List<String> getExternalComponents();

    void setExternalComponents(List<String> externalComponents);

    List<ExternalComment> getExternalComments();

    void setExternalComments(List<ExternalComment> externalComments);

    List<ExternalCustomFieldValue> getExternalCustomFieldValues();

    void setExternalCustomFieldValues(List<ExternalCustomFieldValue> externalCustomFieldValues);

    String getId();

    void setId(String id);

    String getKey();

    void setKey(String key);

    String getSummary();

    void setSummary(String summary);

    String getReporter();

    void setReporter(String reporter);

    String getAssignee();

    void setAssignee(String assignee);

    String getCreator();

    String getDescription();

    void setDescription(String description);

    String getEnvironment();

    void setEnvironment(String environment);

    String getProject();

    void setProject(String project);

    String getIssueType();

    void setIssueType(String issueType);

    String getStatus();

    void setStatus(String status);

    String getPriority();

    void setPriority(String priority);

    String getResolution();

    void setResolution(String resolution);

    Date getCreated();

    void setCreated(Date created);

    Date getUpdated();

    void setUpdated(Date updated);

    Date getResolutionDate();

    void setResolutionDate(Date resolved);

    Date getDuedate();

    void setDuedate(Date duedate);

    Long getVotes();

    void setVotes(Long votes);

    String getOldId();

    void setOldId(String oldId);

    Long getOriginalEstimate();

    void setOriginalEstimate(Long originalEstimate);

    Long getTimeSpent();

    void setTimeSpent(Long timeSpent);

    Long getEstimate();

    void setEstimate(Long estimate);

    String getSecurityLevel();

    void setSecurityLevel(String securityLevel);

    void setLabels(Set<Label> labels);

    Set<Label> getLabels();

    void setField(String fieldName, final String value);

    String getField(String fieldName);
}
