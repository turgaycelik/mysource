package com.atlassian.jira.plugin.permission;

import com.atlassian.plugin.ModuleDescriptor;

public interface GlobalPermissionModuleDescriptor extends ModuleDescriptor<Void>
{

    public String getDescriptionI18nKey();

    public boolean isAnonymousAllowed();
}
