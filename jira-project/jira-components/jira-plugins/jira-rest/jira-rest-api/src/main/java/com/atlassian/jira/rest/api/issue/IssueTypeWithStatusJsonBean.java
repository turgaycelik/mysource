package com.atlassian.jira.rest.api.issue;

import com.atlassian.jira.issue.fields.rest.json.beans.StatusJsonBean;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;

/**
 * @since v6.0
 */
public class IssueTypeWithStatusJsonBean
{

    @JsonProperty
    private String self;

    @JsonProperty
    private String id;

    @JsonProperty
    private String name;

    @JsonProperty
    private boolean subtask;

    @JsonProperty
    private Collection<StatusJsonBean> statuses;

    public IssueTypeWithStatusJsonBean(final String self, final String id, final String name, final boolean subtask, final Collection<StatusJsonBean> statuses)
    {
        this.self = self;
        this.id = id;
        this.name = name;
        this.subtask = subtask;
        this.statuses = statuses;
    }

    public String getSelf()
    {
        return self;
    }

    public void setSelf(final String self)
    {
        this.self = self;
    }

    public String getId()
    {
        return id;
    }

    public void setId(final String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public boolean isSubtask()
    {
        return subtask;
    }

    public void setSubtask(final boolean subtask)
    {
        this.subtask = subtask;
    }

    public Collection<StatusJsonBean> getStatuses()
    {
        return statuses;
    }

    public void setStatuses(final Collection<StatusJsonBean> statuses)
    {
        this.statuses = statuses;
    }
}
