package com.atlassian.jira.rest.api.issue;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 *
 * @since v6.0
 */
public class IssuesUpdateRequest {

	@JsonProperty
	private List<IssueUpdateRequest> issueUpdates;

	public IssuesUpdateRequest(final List<IssueUpdateRequest> issueUpdates) {
		this.issueUpdates = issueUpdates;
	}

	public List<IssueUpdateRequest> getIssueUpdates()
	{
		return issueUpdates;
	}

}
