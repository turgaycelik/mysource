package com.atlassian.jira.rest.api.issue;

import com.google.common.collect.ImmutableList;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Response of create issue bulk operation
 *
 * @since v6.0
 */
@XmlRootElement
public class IssuesCreateResponse
{
    final public List<IssueCreateResponse> issues;
    final public List<BulkOperationErrorResult> errors;

    public IssuesCreateResponse(final List<IssueCreateResponse> issues, final List<BulkOperationErrorResult> errors) {
		this.issues = ImmutableList.copyOf(issues);
		this.errors = ImmutableList.copyOf(errors);
	}

}
