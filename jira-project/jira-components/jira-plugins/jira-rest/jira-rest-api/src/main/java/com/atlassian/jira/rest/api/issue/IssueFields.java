package com.atlassian.jira.rest.api.issue;

import com.google.common.collect.Maps;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Issue fields bean.
 */
@JsonSerialize (using = FieldsSerializer.class)
public class IssueFields
{
    /**
     * Logger for this IssueFields instance.
     */
    private static final Logger log = LoggerFactory.getLogger(IssueFields.class);

    private static final String CUSTOMFIELD_ = "customfield_";

    public ResourceRef parent;
    public ResourceRef project;
    public String summary;
    @JsonProperty ("issuetype")
    public ResourceRef issueType;
    public ResourceRef assignee;
    public ResourceRef reporter;
    public ResourceRef priority;
    public List<String> labels;
    @JsonProperty ("timetracking")
    public TimeTracking timeTracking;
    @JsonProperty ("security")
    public ResourceRef securityLevel;
    public List<ResourceRef> versions;
    public String environment;
    public String description;
    @JsonProperty ("duedate")
    public String dueDate;
    public List<ResourceRef> fixVersions;
    public List<ResourceRef> components;
    public ResourceRef resolution;

    /**
     * Contains any fields that are not one of the above.
     */
    Map<String, Object> fields;

    public ResourceRef parent()
    {
        return this.parent;
    }

    public IssueFields parent(ResourceRef parent)
    {
        this.parent = parent;
        return this;
    }

    public ResourceRef project()
    {
        return this.project;
    }

    public IssueFields project(ResourceRef project)
    {
        this.project = project;
        return this;
    }

    public String summary()
    {
        return this.summary;
    }

    public IssueFields summary(String summary)
    {
        this.summary = summary;
        return this;
    }

    public ResourceRef issueType()
    {
        return this.issueType;
    }

    public IssueFields issueType(ResourceRef issueType)
    {
        this.issueType = issueType;
        return this;
    }

    public ResourceRef assignee()
    {
        return this.assignee;
    }

    public IssueFields assignee(ResourceRef assignee)
    {
        this.assignee = assignee;
        return this;
    }

    public ResourceRef reporter()
    {
        return this.reporter;
    }

    public IssueFields reporter(ResourceRef reporter)
    {
        this.reporter = reporter;
        return this;
    }

    public ResourceRef priority()
    {
        return this.priority;
    }

    public IssueFields priority(ResourceRef priority)
    {
        this.priority = priority;
        return this;
    }

    public ResourceRef resolution()
    {
        return this.resolution;
    }

    public IssueFields resolution(ResourceRef resolution)
    {
        this.resolution = resolution;
        return this;
    }

    public List<String> labels()
    {
        return this.labels;
    }

    public IssueFields labels(List<String> labels)
    {
        this.labels = labels;
        return this;
    }

    public TimeTracking timeTracking()
    {
        return this.timeTracking;
    }

    public IssueFields timeTracking(TimeTracking timeTracking)
    {
        this.timeTracking = timeTracking;
        return this;
    }

    public ResourceRef securityLevel()
    {
        return this.securityLevel;
    }

    public IssueFields securityLevel(ResourceRef securityLevel)
    {
        this.securityLevel = securityLevel;
        return this;
    }

    public List<ResourceRef> versions()
    {
        return this.versions;
    }

    public IssueFields versions(List<ResourceRef> affectsVersions)
    {
        this.versions = affectsVersions;
        return this;
    }

    public IssueFields versions(ResourceRef... affectsVersions)
    {
        this.versions = affectsVersions != null ? Arrays.asList(affectsVersions) : null;
        return this;
    }

    public String environment()
    {
        return this.environment;
    }

    public IssueFields environment(String environment)
    {
        this.environment = environment;
        return this;
    }

    public String description()
    {
        return this.description;
    }

    public IssueFields description(String description)
    {
        this.description = description;
        return this;
    }

    public String dueDate()
    {
        return this.dueDate;
    }

    public IssueFields dueDate(String dueDate)
    {
        this.dueDate = dueDate;
        return this;
    }

    public List<ResourceRef> fixVersions()
    {
        return this.fixVersions;
    }

    public IssueFields fixVersions(List<ResourceRef> fixVersions)
    {
        this.fixVersions = fixVersions;
        return this;
    }

    public IssueFields fixVersions(ResourceRef... fixVersions)
    {
        this.fixVersions = fixVersions != null ? Arrays.asList(fixVersions) : null;
        return this;
    }

    public List<ResourceRef> components()
    {
        return this.components;
    }

    public IssueFields components(List<ResourceRef> components)
    {
        this.components = components;
        return this;
    }

    public IssueFields components(ResourceRef... component)
    {
        this.components = component != null ? Arrays.asList(component) : null;
        return this;
    }

    public Object customField(Long customFieldId)
    {
        return fields != null ? fields.get(CUSTOMFIELD_ + customFieldId) : null;
    }

    public IssueFields customField(Long customFieldId, Object value)
    {
        if (fields == null)
        {
            fields = Maps.newHashMap();
        }

        fields.put(CUSTOMFIELD_ + customFieldId, value);
        return this;
    }

    public Map<Long, Object> customFields()
    {
        if (fields == null)
        {
            return Collections.emptyMap();
        }

        Map<Long, Object> customFieldById = Maps.newHashMapWithExpectedSize(fields.size());
        for (Map.Entry<String, Object> field : fields.entrySet())
        {
            String key = field.getKey();
            Object value = field.getValue();

            // custom fields all have the "customfield_" prefix
            if (key.startsWith(CUSTOMFIELD_))
            {
                customFieldById.put(Long.valueOf(key.substring(CUSTOMFIELD_.length())), value);
            }
        }

        return customFieldById;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @JsonAnySetter
    protected void customField(String fieldId, Object value)
    {
        if (!fieldId.startsWith(CUSTOMFIELD_))
        {
            log.debug("Field '{}' is not known, ignoring.", fieldId);
            return;
        }

        if (!(value instanceof List))
        {
            log.debug("Field '{}' does not contain an array of strings, ignoring.", fieldId);
            return;
        }

        List<String> strings = (List<String>) value;
        if (fields == null)
        {
            fields = Maps.newHashMap();
        }

        fields.put(fieldId, strings.toArray(new String[strings.size()]));
    }

}
