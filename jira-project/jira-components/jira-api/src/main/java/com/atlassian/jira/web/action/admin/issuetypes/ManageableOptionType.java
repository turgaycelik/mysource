package com.atlassian.jira.web.action.admin.issuetypes;

import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

public interface ManageableOptionType
{
    String getTitle();

    String getFieldId();

    String getActionPrefix();

    String getLocalHelpSuffix();

    String getTitleSingle();

    String getTitleLowerCase();

    boolean isIconEnabled();
    boolean isTypeEnabled();

    Collection getAllOptions();

    boolean isDefault(GenericValue constant);
}
