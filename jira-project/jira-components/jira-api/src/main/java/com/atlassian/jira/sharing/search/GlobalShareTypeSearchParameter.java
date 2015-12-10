package com.atlassian.jira.sharing.search;

import com.atlassian.jira.sharing.type.ShareType;

/**
 * Represents the search parameters when searching for Global ShareTypes.
 *
 * @since v3.13
 */
public class GlobalShareTypeSearchParameter extends AbstractShareTypeSearchParameter
{
    public static final GlobalShareTypeSearchParameter GLOBAL_PARAMETER = new GlobalShareTypeSearchParameter();

    private GlobalShareTypeSearchParameter()
    {
        super(ShareType.Name.GLOBAL);
    }
}
