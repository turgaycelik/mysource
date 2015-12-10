package com.atlassian.jira.rest.v2.admin.workflowscheme;

import com.atlassian.jira.issue.fields.rest.json.beans.IssueTypeJsonBean;
import com.atlassian.jira.rest.v2.issue.UserBean;
import com.google.common.collect.Maps;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.net.URI;
import java.util.Map;

/**
 * @since v5.2
 */
public class WorkflowSchemeBean
{
    @JsonProperty
    private Long id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String description;

    @JsonProperty
    private String defaultWorkflow;

    @JsonProperty
    private Map<String, String> issueTypeMappings;

    @JsonProperty
    private String originalDefaultWorkflow;

    @JsonProperty
    private Map<String, String> originalIssueTypeMappings;

    @JsonProperty
    private Boolean draft;

    @JsonProperty
    private UserBean lastModifiedUser;

    @JsonProperty
    private String lastModified;

    @JsonProperty
    private URI self;

    @JsonProperty
    private Boolean updateDraftIfNeeded;

    @JsonProperty
    private Map<String, IssueTypeJsonBean> issueTypes;

    @JsonIgnore
    private boolean nameSet;

    @JsonIgnore
    private boolean descriptionSet;

    @JsonIgnore
    private boolean defaultSet;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getDefaultWorkflow()
    {
        return defaultWorkflow;
    }

    public void setDefaultWorkflow(String defaultWorkflow)
    {
        defaultSet = true;
        this.defaultWorkflow = defaultWorkflow;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        descriptionSet = true;
        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        nameSet = true;
        this.name = name;
    }

    public URI getSelf()
    {
        return self;
    }

    public void setSelf(URI self)
    {
        this.self = self;
    }

    public Boolean isDraft()
    {
        return draft;
    }

    public void setDraft(Boolean draft)
    {
        this.draft = draft;
    }

    public void setLastModifiedUser(UserBean lastModifiedUser)
    {
        this.lastModifiedUser = lastModifiedUser;
    }

    public UserBean getLastModifiedUser()
    {
        return lastModifiedUser;
    }

    public String getLastModified()
    {
        return lastModified;
    }

    public void setLastModified(String lastModified)
    {
        this.lastModified = lastModified;
    }

    public Boolean isUpdateDraftIfNeeded()
    {
        return updateDraftIfNeeded;
    }

    public void setUpdateDraftIfNeeded(Boolean createDraftIfNeeded)
    {
        this.updateDraftIfNeeded = createDraftIfNeeded;
    }

    public boolean isDefaultSet()
    {
        return defaultSet;
    }

    public boolean isDescriptionSet()
    {
        return descriptionSet;
    }

    public boolean isNameSet()
    {
        return nameSet;
    }

    public String getOriginalDefaultWorkflow()
    {
        return originalDefaultWorkflow;
    }

    public void setOriginalDefaultWorkflow(String originalDefaultWorkflow)
    {
        this.originalDefaultWorkflow = originalDefaultWorkflow;
    }

    public Map<String, String> getIssueTypeMappings()
    {
        return issueTypeMappings;
    }

    public void setIssueTypeMappings(Map<String, String> issueTypeMappings)
    {
        this.issueTypeMappings = issueTypeMappings;
    }

    public Map<String, String> getOriginalIssueTypeMappings()
    {
        return originalIssueTypeMappings;
    }

    public void setOriginalIssueTypeMappings(Map<String, String> originalIssueTypeMappings)
    {
        this.originalIssueTypeMappings = originalIssueTypeMappings;
    }

    void addIssueTypeMapping(String issueType, String workflow)
    {
        if (issueTypeMappings == null)
        {
            issueTypeMappings = Maps.newHashMap();
        }
        issueTypeMappings.put(issueType, workflow);
    }

    void addOriginalIssueTypeMapping(String issueType, String workflow)
    {
        if (originalIssueTypeMappings == null)
        {
            originalIssueTypeMappings = Maps.newHashMap();
        }
        originalIssueTypeMappings.put(issueType, workflow);
    }

    public Map<String, IssueTypeJsonBean> getIssueTypes()
    {
        return issueTypes;
    }

    public void setIssueTypes(Map<String, IssueTypeJsonBean> issueTypes)
    {
        this.issueTypes = issueTypes;
    }
}
