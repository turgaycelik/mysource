package com.atlassian.jira.security.util;

import com.atlassian.jira.scheme.SchemeManager;
import org.ofbiz.core.entity.GenericEntityException;

public abstract class AbstractGroupToSchemeMapper extends AbstractGroupMapper
{
    private SchemeManager schemeManager;

    public AbstractGroupToSchemeMapper(SchemeManager schemeManager) throws GenericEntityException
    {
        this.schemeManager = schemeManager;
        setGroupMapping(init());
    }

    protected SchemeManager getSchemeManager()
    {
        return schemeManager;
    }
}
