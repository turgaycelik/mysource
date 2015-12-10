package com.atlassian.jira.license;


import com.atlassian.jira.security.JiraAuthenticationContext;

import javax.annotation.Nonnull;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.dbc.Assertions.stripNotBlank;

public class LicenseRoleDefinitionImpl implements LicenseRoleDefinition
{
    private final LicenseRoleId licenseRoleId;
    private final String displayNameI18nKey;
    private final JiraAuthenticationContext context;

    public LicenseRoleDefinitionImpl(@Nonnull final LicenseRoleId licenseRoleId,
            @Nonnull final String displayNameI18nKey, @Nonnull final JiraAuthenticationContext context)
    {
        this.context = notNull("context", context);
        this.licenseRoleId = notNull("licenseRoleId", licenseRoleId);
        this.displayNameI18nKey = stripNotBlank("displayNameI18nKey", displayNameI18nKey);
    }

    @Nonnull
    @Override
    public LicenseRoleId getLicenseRoleId()
    {
        return licenseRoleId;
    }

    @Nonnull
    @Override
    public String getName()
    {
        return context.getI18nHelper().getText(displayNameI18nKey);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || !(o instanceof LicenseRoleDefinitionImpl))
        {
            return false;
        }

        //It is intentional that we only look at licenseRoleId.
        LicenseRoleDefinitionImpl that = (LicenseRoleDefinitionImpl) o;
        return this.licenseRoleId.equals(that.licenseRoleId);
    }

    @Override
    public int hashCode()
    {
        //It is intentional that we only look at licenseRoleId.
        return licenseRoleId.hashCode();
    }
}
