package com.atlassian.jira.appconsistency.integrity.check;

import java.util.List;

import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.appconsistency.integrity.amendment.DeleteEntityAmendment;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.user.ApplicationUser;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

/**
 * Finds and fixes all filtersubcriptions that have no corresponding search filter.
 */
public class FilterSubscriptionsSavedFilterCheck extends BaseFilterSubscriptionsCheck
{
    private static final Logger log = Logger.getLogger(FilterSubscriptionsSavedFilterCheck.class);

    public FilterSubscriptionsSavedFilterCheck(OfBizDelegator ofBizDelegator, int id)
    {
        super(ofBizDelegator, id);
    }

    public String getDescription()
    {
        return getI18NBean().getText("admin.integrity.check.filter.subscriptions.trigger.saved.filter.desc");
    }

    // Ensure that the filter subscriptions table does not contain references to search requests that have been deleted.
    protected void doRealCheck(boolean correct, GenericValue subscription, List<DeleteEntityAmendment> messages) throws IntegrityException
    {
        // try to find the related search request, if null then flag
        SearchRequest sr = getSearchRequest(subscription);
        if (sr == null)
        {
            if (correct)
            {
                // flag the current subscription for deletion
                messages.add(new DeleteEntityAmendment(Amendment.CORRECTION, getI18NBean().getText("admin.integrity.check.filter.subscriptions.trigger.saved.filter.message", subscription.getString("id")), subscription));
            }
            else
            {
                messages.add(new DeleteEntityAmendment(Amendment.ERROR, getI18NBean().getText("admin.integrity.check.filter.subscriptions.trigger.saved.filter.preview", subscription.getString("id")), subscription));
            }
        }
    }

    private SearchRequest getSearchRequest(GenericValue subscription)
    {
        ApplicationUser subscriptionUser = getSubscriptionUser(subscription);
        if (subscriptionUser == null)
        {
            log.warn("Problem retreiving user '" + subscription.getString("username") + "' for subscription");
            return null;
        }
        final JiraServiceContext ctx = new JiraServiceContextImpl(subscriptionUser);
        final SearchRequest filter = ComponentAccessor.getComponent(SearchRequestService.class).getFilter(ctx, subscription.getLong("filterID"));

        if (ctx.getErrorCollection().hasAnyErrors())
        {
            if (!ctx.getErrorCollection().getErrorMessages().isEmpty())
            {
                log.warn("Problem retreiving filter for subscription: " + ctx.getErrorCollection().getErrorMessages());
            }
            if (!ctx.getErrorCollection().getErrors().isEmpty())
            {
                log.warn("Problem retreiving filter for subscription: " + ctx.getErrorCollection().getErrors());
            }
        }
        return filter;
    }

    private ApplicationUser getSubscriptionUser(GenericValue subscription)
    {
        return  ComponentAccessor.getUserManager().getUserByKey(subscription.getString("username"));
    }
}
