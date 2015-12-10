package com.atlassian.jira.appconsistency.integrity.check;

import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.appconsistency.integrity.integritycheck.IntegrityCheck;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.I18nHelper;

public abstract class CheckImpl<T extends Amendment> implements Check<T>
{
    protected final int id;
    protected OfBizDelegator ofBizDelegator;
    private IntegrityCheck integrityCheck;

    protected CheckImpl(final OfBizDelegator ofBizDelegator, final int id)
    {
        this.id = id;
        this.ofBizDelegator = ofBizDelegator;
    }

    public Long getId()
    {
        return Integer.valueOf(id).longValue();
    }

    public IntegrityCheck getIntegrityCheck()
    {
        return integrityCheck;
    }

    public void setIntegrityCheck(final IntegrityCheck integrityCheck)
    {
        this.integrityCheck = integrityCheck;
    }

    protected I18nHelper getI18NBean()
    {
        return ComponentAccessor.getJiraAuthenticationContext().getI18nHelper();
    }
}
