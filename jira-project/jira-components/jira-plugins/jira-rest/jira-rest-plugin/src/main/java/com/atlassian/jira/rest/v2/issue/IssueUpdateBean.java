package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadata;
import com.atlassian.jira.rest.api.issue.FieldOperation;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Bean representing a create/edit request
 * @since v5.0
 */
@JsonIgnoreProperties (ignoreUnknown = true)
public class IssueUpdateBean
{
    @JsonProperty
    private TransitionBean transition;

    @JsonProperty
    private Map<String, Object> fields;
    @JsonProperty
    private Map<String, List<FieldOperation>> update;

    /**
     * @since JIRA 6.3
     */
    @JsonProperty
    private HistoryMetadata historyMetadata;

    public Map<String, Object> fields()
    {
        return fields;
    }

    public Map<String, List<FieldOperation>> update()
    {
        return update;
    }

    public TransitionBean getTransition()
    {
        return transition;
    }

    public HistoryMetadata getHistoryMetadata()
    {
        return historyMetadata;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
