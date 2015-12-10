package com.atlassian.jira.sharing.search;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.sharing.type.ShareType;

/**
 * Interface for representing ShareType parameters in searches.
 * 
 * @since v3.13
 */
@PublicApi
public interface ShareTypeSearchParameter
{
    /**
     * Return the type of ShareType this parameter is associated with.
     * 
     * @return the ShareType this parameter is associated with.
     */
    ShareType.Name getType();
}
