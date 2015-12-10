package com.atlassian.jira.issue.fields.rest.json.beans;

import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;

/**
 * @since v5.0
 */
@JsonIgnoreProperties (ignoreUnknown = true)
public class CommentsWithPaginationJsonBean
{
    @JsonProperty
    private Integer startAt;

    @JsonProperty
    private Integer maxResults;

    @JsonProperty
    private Integer total;

    @JsonProperty
    private Collection<CommentJsonBean> comments;

    public CommentsWithPaginationJsonBean()
    {
    }

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

    public Collection<CommentJsonBean> getComments()
    {
        return comments;
    }

    public void setComments(Collection<CommentJsonBean> comments)
    {
        this.comments = comments;
    }


    public static final CommentsWithPaginationJsonBean DOC_EXAMPLE;
    static
    {
        DOC_EXAMPLE = new CommentsWithPaginationJsonBean();
        DOC_EXAMPLE.setMaxResults(1);
        DOC_EXAMPLE.setTotal(1);
        DOC_EXAMPLE.setStartAt(0);
        DOC_EXAMPLE.setComments(Lists.<CommentJsonBean>newArrayList(CommentJsonBean.DOC_EXAMPLE));
    }
}
