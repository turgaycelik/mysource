package com.atlassian.jira.issue.fields.rest;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinkTypeJsonBean;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.util.ErrorCollection;

/**
 * Finds an IssueLinkType by 'id' or 'name'.
 *
 * @since v5.0
 */
@PublicApi
public interface IssueLinkTypeFinder
{
    /**
     * Fetches an IssueLinkType from its manager based on either the 'id' or the 'name' property of the
     * IssueLinkTypeJsonBean. If the link type does not exist, the appropriate error will be added to the passed-in
     * ErrorCollection, and this method will return null.
     *
     *
     *
     * @param linkTypeBean the issue type to find
     * @param errors an ErrorCollection
     * @return an IssueLinkType, or null
     * @since v5.0
     */
    IssueLinkType findIssueLinkType(IssueLinkTypeJsonBean linkTypeBean, ErrorCollection errors);
}
