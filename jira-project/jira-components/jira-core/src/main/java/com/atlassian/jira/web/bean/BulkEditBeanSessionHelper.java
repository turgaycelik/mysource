package com.atlassian.jira.web.bean;

import com.atlassian.jira.web.SessionKeys;
import webwork.action.ActionContext;

/**
 * Helper class for static session storage.
 *
 * @since v5.0
 */
public class BulkEditBeanSessionHelper
{
    public void storeToSession(final BulkEditBean bulkEditBean)
    {
        ActionContext.getSession().put(SessionKeys.BULKEDITBEAN, bulkEditBean);
    }

    public BulkEditBean getFromSession()
    {
        return (BulkEditBean) ActionContext.getSession().get(SessionKeys.BULKEDITBEAN);
    }

    public void removeFromSession()
    {
        ActionContext.getSession().remove(SessionKeys.BULKEDITBEAN);
    }
}
