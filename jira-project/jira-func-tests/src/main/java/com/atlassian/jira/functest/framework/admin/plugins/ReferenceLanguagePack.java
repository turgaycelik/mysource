package com.atlassian.jira.functest.framework.admin.plugins;

import com.atlassian.jira.functest.framework.Administration;

/**
 * Represents the JIRA reference language pack. We use this to test i18n key reloadability from language packs as
 * produced by TAC.
 *
 * @since v4.4
 */
public class ReferenceLanguagePack extends Plugin
{
    public final Administration administration;

    public static final String KEY = "com.atlassian.jira.jira-reference-language-pack";

    public ReferenceLanguagePack(final Administration administration)
    {
        super(administration);
        this.administration = administration;
    }

    @Override
    public String getKey()
    {
        return KEY;
    }
}
