package com.atlassian.jira.rest.api.issue;

import com.atlassian.jira.rest.api.util.ErrorCollection;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Represents error of creating single element during batch operation.
 *
 * @since v6.0
 */

public class BulkOperationErrorResult
{
	@JsonProperty
	private final Integer status;
	@JsonProperty
	private final ErrorCollection elementErrors;
    @JsonProperty
    private final Integer failedElementNumber;

	public BulkOperationErrorResult(final Integer status, final ErrorCollection elementErrors, final Integer failedElementNumber) {
		this.status = status;
		this.elementErrors = elementErrors;
		this.failedElementNumber = failedElementNumber;
	}

	public Integer getStatus() {
		return status;
	}

	public ErrorCollection getElementErrors() {
		return elementErrors;
	}

	public Integer getFailedElementNumber() {
		return failedElementNumber;
	}
}