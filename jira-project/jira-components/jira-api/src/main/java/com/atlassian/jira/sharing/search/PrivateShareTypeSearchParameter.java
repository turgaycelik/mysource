package com.atlassian.jira.sharing.search;

import com.atlassian.jira.sharing.type.ShareType;

/**
 * Search parameter when looking for private SharedEntity instances. This essentially means that the SharedEntity has no associated share permissions.
 *
 * @since v3.13
 */
public class PrivateShareTypeSearchParameter extends AbstractShareTypeSearchParameter
{
    private static final ShareType.Name PRIVATE_TYPE = new ShareType.Name("private");

    public static final PrivateShareTypeSearchParameter PRIVATE_PARAMETER = new PrivateShareTypeSearchParameter();

    private PrivateShareTypeSearchParameter()
    {
        super(PrivateShareTypeSearchParameter.PRIVATE_TYPE);
    }
}
