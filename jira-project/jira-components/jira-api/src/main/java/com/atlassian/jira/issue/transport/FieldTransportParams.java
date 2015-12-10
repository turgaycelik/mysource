package com.atlassian.jira.issue.transport;

import com.atlassian.annotations.PublicApi;

/**
 * This contains String > Collection of Transport Objects
 */
@PublicApi
public interface FieldTransportParams extends CollectionParams
{
    Object getFirstValueForNullKey();
    Object getFirstValueForKey(String key);

}
