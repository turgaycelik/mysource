package com.atlassian.jira.upgrade.util;

public class UsersGroupParamConverter extends XsltSearchRequestTransformer
{
    private static final String UPGRADE_XSL_FILENAME = "upgrade_build94_searchrequest.xsl";

    public UsersGroupParamConverter()
    {
        super(UPGRADE_XSL_FILENAME);
    }
}
