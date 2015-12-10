package com.atlassian.jira.issue.fields.rest.json.beans;

import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;

/**
 * outer object for representing worklogs.  Allows for future expansion to allow pagination.
 * @since v5.0
 */
@JsonIgnoreProperties (ignoreUnknown = true)
public class WorklogWithPaginationBean
{
    @JsonProperty
    private Integer startAt;

    @JsonProperty
    private Integer maxResults;

    @JsonProperty
    private Integer total;

    @JsonProperty
    private Collection<WorklogJsonBean> worklogs;

    public Integer getStartAt()
    {
        return startAt;
    }

    public void setStartAt(Integer startAt)
    {
        this.startAt = startAt;
    }

    public Integer getMaxResults()
    {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults)
    {
        this.maxResults = maxResults;
    }

    public Integer getTotal()
    {
        return total;
    }

    public void setTotal(Integer total)
    {
        this.total = total;
    }

    public Collection<WorklogJsonBean> getWorklogs()
    {
        return worklogs;
    }

    public void setWorklogs(Collection<WorklogJsonBean> worklogs)
    {
        this.worklogs = worklogs;
    }

    public static final WorklogWithPaginationBean DOC_EXAMPLE;
    static
    {
        DOC_EXAMPLE = new WorklogWithPaginationBean();
        DOC_EXAMPLE.setMaxResults(1);
        DOC_EXAMPLE.setTotal(1);
        DOC_EXAMPLE.setStartAt(0);
        DOC_EXAMPLE.setWorklogs(Lists.<WorklogJsonBean>newArrayList(WorklogJsonBean.DOC_EXAMPLE));
    }
}
