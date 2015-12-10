/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.web.action.filter;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.filter.FilterSubscriptionService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.UriValidator;
import com.atlassian.jira.web.bean.FilterUtils;
import com.atlassian.jira.web.component.cron.CronEditorBean;
import com.atlassian.jira.web.component.cron.CronEditorWebComponent;
import com.atlassian.jira.web.component.cron.generator.CronExpressionGenerator;
import com.atlassian.jira.web.component.cron.parser.CronExpressionParser;
import com.atlassian.jira.web.util.OutlookDateManager;

import com.opensymphony.util.TextUtils;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import webwork.action.ActionContext;

/**
 * Action for CRUD of a scheduled email subscription to search filter results.
 */
public class FilterSubscription extends AbstractFilterAction implements FilterOperationsAction
{
    private Long subId = null;
    private String groupName;
    private Boolean emailOnEmpty = Boolean.FALSE;
    private Date lastRun;
    private Date nextRun;
    private SubscriptionManager subscriptionManager = ComponentAccessor.getSubscriptionManager();
    private CronEditorBean cronEditorBean;
    private final FilterSubscriptionService filterSubscriptionService;


    public FilterSubscription(IssueSearcherManager issueSearcherManager, FilterSubscriptionService filterSubscriptionService,
            SearchService searchService, final SearchSortUtil searchSortUtil)
    {
        super(issueSearcherManager, searchService, searchSortUtil);
        this.filterSubscriptionService = filterSubscriptionService;
    }

    public String doDefault() throws Exception
    {
        if (subId == null && getFilterId() == null)
        {
            addErrorMessage(getText("filtersubscription.please.select.a.subscription.or.filter"));
            return ERROR;
        }
        else
        {
            if (subId != null)
            {
                com.atlassian.jira.issue.subscription.FilterSubscription subscription = subscriptionManager.getFilterSubscription(getLoggedInApplicationUser(), subId);
                if (subscription == null)
                {
                    return PERMISSION_VIOLATION_RESULT;
                }
                else
                {
                    groupName = subscription.getGroupName();
                    emailOnEmpty = subscription.isEmailOnEmpty();
                    lastRun = subscription.getLastRunTime();

                    //Get from the trigger
                    String cronExpression = subscriptionManager.getCronExpressionForSubscription(subscription);
                    CronExpressionParser cronExpresionParser = new CronExpressionParser(cronExpression);
                    cronEditorBean = cronExpresionParser.getCronEditorBean();

                    nextRun = null;// TODO new Timestamp(trigger.getNextFireTime().getTime());
                }
            }

            if (getFilterId() != null)
            {
                if (getFilter() == null)
                {
                    return ERROR;
                }
            }
        }
        return super.doDefault();
    }


    public void doValidation()
    {
        cronEditorBean = new CronEditorBean("filter.subscription.prefix", ActionContext.getParameters());
        CronEditorWebComponent component = new CronEditorWebComponent();
        addErrorCollection(component.validateInput(cronEditorBean, "cron.editor.name"));
        if (!hasAnyErrors())
        {
            JiraServiceContext serviceContext = getJiraServiceContext();
            String cronString = component.getCronExpressionFromInput(cronEditorBean);
            filterSubscriptionService.validateCronExpression(serviceContext, cronString);
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        String cronExpression = new CronExpressionGenerator().getCronExpressionFromInput(cronEditorBean);
        if (subId != null)
        {
            // we have a subscription id so we are editing it
            GenericValue subscription = subscriptionManager.getSubscription(getLoggedInApplicationUser(), subId);
            if (subscription == null)
            {
                return PERMISSION_VIOLATION_RESULT;
            }


            filterSubscriptionService.updateSubscription(getJiraServiceContext(), subId, getGroupName(), cronExpression, emailOnEmpty);
        }
        else
        {
            // no subscription id so we are creating a new one
            if (getFilter() == null)
            {
                return "securitybreach";
            }
            filterSubscriptionService.storeSubscription(getJiraServiceContext(), getFilterId(), getGroupName(), cronExpression, getEmailOnEmpty());
        }
        if (isInlineDialogMode())
        {
            return returnComplete(getReturnUrl());
        }
        else
        {
            return (getReturnUrl() == null ?
                    getRedirect("ViewSubscriptions.jspa?filterId=" + getFilterId()) :
                    getRedirect(getReturnUrl()));
        }
    }

    @RequiresXsrfCheck
    public String doDelete() throws Exception
    {
        GenericValue subscription = subscriptionManager.getSubscription(getLoggedInApplicationUser(), subId);

        if (subscription == null)
        {
            addErrorMessage(getText("subscriptions.error.delete.subscriptiondoesnotexist"));
            return ERROR;
        }
        ComponentAccessor.getSubscriptionManager().deleteSubscription(subId);
        return getRedirect("ViewSubscriptions.jspa?filterId=" + getFilterId());
    }

    @RequiresXsrfCheck
    public String doRunNow() throws Exception
    {
        GenericValue subscription = subscriptionManager.getSubscription(getLoggedInApplicationUser(), subId);

        if (subscription == null)
        {
            addErrorMessage(getText("subscriptions.error.runnow.subscriptiondoesnotexist"));
            return ERROR;
        }

        ComponentAccessor.getSubscriptionManager().runSubscription(getLoggedInApplicationUser(), subId);
        return getRedirect("ViewSubscriptions.jspa?filterId=" + getFilterId());
    }

    public boolean hasGroupPermission() throws GenericEntityException
    {
        return ComponentAccessor.getPermissionManager().hasPermission(Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS, getLoggedInApplicationUser());
    }

    public String getSubmitName() throws GenericEntityException
    {
        if (subId == null)
        {
            return getText("filtersubscription.subscribe");
        }
        else
        {
            return getText("common.forms.update");
        }
    }

    public String getCancelStr() throws GenericEntityException
    {
        if (getReturnUrl() != null)
        {
            return getReturnUrl();
        }
        else if (subId == null)
        {
            return "ManageFilters.jspa";
        }
        else
        {
            return "ViewSubscriptions.jspa?filterId=" + getFilterId();
        }
    }

    public String getSubId()
    {
        if (subId == null)
        {
            return null;
        }
        else
        {
            return subId.toString();
        }
    }

    public void setSubId(String subId)
    {
        if (TextUtils.stringSet(subId))
        {
            this.subId = new Long(subId);
        }
    }

    public String getGroupName()
    {
        return groupName;
    }

    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    public Boolean getEmailOnEmpty()
    {
        return emailOnEmpty;
    }

    public void setEmailOnEmpty(Boolean emailOnEmpty)
    {
        this.emailOnEmpty = emailOnEmpty;
    }

    public String getLastRun()
    {
        if (lastRun == null)
        {
            return null;
        }
        else
        {
            return String.valueOf(lastRun.getTime());
        }
    }

    public void setLastRun(String lastRun)
    {
        if (TextUtils.stringSet(lastRun))
        {
            this.lastRun = new Timestamp(Long.parseLong(lastRun));
        }
    }

    public String getNextRun()
    {
        if (nextRun == null)
        {
            return null;
        }
        else
        {
            return String.valueOf(nextRun.getTime());
        }
    }

    public void setNextRun(String nextRun)
    {
        if (TextUtils.stringSet(nextRun))
        {
            this.nextRun = new Timestamp(Long.parseLong(nextRun));
        }
    }

    public String getLastRunStr()
    {
        if (lastRun == null)
        {
            return null;
        }
        else
        {
            return ComponentAccessor.getComponent(OutlookDateManager.class).getOutlookDate(getLocale()).formatDMYHMS(lastRun);
        }
    }

    public String getNextRunStr()
    {
        if (nextRun == null)
        {
            return null;
        }
        else
        {
            return ComponentAccessor.getComponent(OutlookDateManager.class).getOutlookDate(getLocale()).formatDMYHMS(nextRun);
        }
    }

    public Collection getGroups()
    {
        return FilterUtils.getGroups(getLoggedInUser());
    }

    public CronEditorBean getCronEditorBean()
    {
        if (cronEditorBean == null)
        {
            cronEditorBean = new CronExpressionParser().getCronEditorBean();
        }
        return cronEditorBean;
    }

}
