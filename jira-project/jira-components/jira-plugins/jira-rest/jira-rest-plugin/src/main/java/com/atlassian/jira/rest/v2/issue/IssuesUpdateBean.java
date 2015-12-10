package com.atlassian.jira.rest.v2.issue;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * Input data for bulk issue create operaton
 *
 * @since v6.0
 */
@JsonIgnoreProperties (ignoreUnknown = true)
public class IssuesUpdateBean
{
    @JsonProperty
    private List<IssueUpdateBean> issueUpdates;

	public List<IssueUpdateBean> getIssueUpdates()
    {
        return issueUpdates;
    }
}
