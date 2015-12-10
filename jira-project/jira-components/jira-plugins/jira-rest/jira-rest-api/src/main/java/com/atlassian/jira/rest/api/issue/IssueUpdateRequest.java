package com.atlassian.jira.rest.api.issue;

import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadata;
import com.google.common.collect.Maps;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Issue update/create request.
 *
 * @since v5.0
 */
@JsonIgnoreProperties (ignoreUnknown = true)
public class IssueUpdateRequest
{
    @JsonProperty
    protected Map<String, List<FieldOperation>> update = Maps.newHashMap();
    @JsonProperty
    protected IssueFields fields;

    @JsonProperty
    protected ResourceRef transition;

    @JsonProperty
    private HistoryMetadata historyMetadata;

    public IssueFields fields()
    {
        return this.fields;
    }

    public IssueUpdateRequest fields(IssueFields fields)
    {
        this.fields = fields;
        return this;
    }

    public  Map<String, List<FieldOperation>> update()
    {
        return update;
    }

    public IssueUpdateRequest update(Map<String, List<FieldOperation>> update)
    {
      this.update = update;
      return this;
    }

    public IssueUpdateRequest transition(ResourceRef transition)
    {
        this.transition = transition;
        return this;
    }

    /**
     * Sets the update operations for a single field.
     *
     * @param field a String containing the field name
     * @param updates a List of FieldOperation
     * @return this
     */
    public IssueUpdateRequest update(String field, List<FieldOperation> updates)
    {
        this.update.put(field, updates);
        return this;
    }

    /**
     * Sets the update operations for a single field.
     *
     * @param field a String containing the field name
     * @param updates an array of FieldOperation
     * @return this
     */
    public IssueUpdateRequest update(String field, FieldOperation... updates)
    {
        this.update.put(field, Arrays.asList(updates));
        return this;
    }

    /**
     * Sets the history metadata for the update operation
     * @since JIRA 6.3
     */
    public IssueUpdateRequest historyMetadata(HistoryMetadata historyMetadata) {
        this.historyMetadata = historyMetadata;
        return this;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }


}
