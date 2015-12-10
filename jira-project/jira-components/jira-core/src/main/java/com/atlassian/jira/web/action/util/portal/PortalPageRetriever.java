package com.atlassian.jira.web.action.util.portal;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.util.dbc.Assertions;

import java.util.List;

/**
 * Object used by actions to retrieve PortalPages. This class caches the results of lookups
 * to ensure they are quick.
 *
 * @since v3.13
 */
public class PortalPageRetriever
{
    private final PortalPageService portalPageService;
    private final Long sessionPageId;

    private Long requestedPageId = null;
    private PortalPage portalPage = null;
    private boolean doneLookup = false;

    public PortalPageRetriever(final PortalPageService portalPageService, final UserHistoryManager userHistoryManager, final JiraAuthenticationContext authenticationContext, final Long requestedPageId)
    {
        Assertions.notNull("portalPageService", portalPageService);

        final List<UserHistoryItem> history = userHistoryManager.getHistory(UserHistoryItem.DASHBOARD, authenticationContext.getLoggedInUser());
        if(history.isEmpty())
        {
            this.sessionPageId = null;
        }
        else
        {
            this.sessionPageId = Long.valueOf(history.get(0).getEntityId());
        }
        this.portalPageService = portalPageService;
        this.requestedPageId = requestedPageId;
    }

    public PortalPageRetriever(final PortalPageService portalPageService, final UserHistoryManager userHistoryManager, final JiraAuthenticationContext authenticationContext)
    {
        this(portalPageService, userHistoryManager, authenticationContext, null);
    }

    /**
     * Return the id of the PortalPage a user is currently using. It returns the id stored in this object
     * if it exists or the id from the session if it does not. It will return null of the id could not be
     * returned found in either.
     *
     * @return the id of PortalPage the user is currently operating under.
     */
    public Long getPageId()
    {
        if (requestedPageId != null)
        {
            return requestedPageId;
        }
        else
        {
            return sessionPageId;
        }
    }
    
    public Long getRequestedPageId()
    {
        return requestedPageId;
    }

    public void setRequestedPageId(final Long requestedPageId)
    {
        this.doneLookup = false;
        this.portalPage = null;
        this.requestedPageId = requestedPageId;
    }

    /**
     * Return the PortalPage a user is currently using. This method caches the result of the first lookup
     * and continues to return the same value. While the same value may be returned,
     * only the first call will ever put error messages into the passed context. null may be returned
     * if it is not possible to work user's current PortalPage.
     *
     * @param ctx the service context to execute the method
     * @return the user's current PortalPage or null if no such page exists.
     */
    public PortalPage getPortalPage(final JiraServiceContext ctx)
    {
        Assertions.notNull("ctx", ctx);

        if (!doneLookup)
        {
            doneLookup = true;

            Long pageId = getPageId();
            if (pageId != null)
            {
                portalPage = portalPageService.getPortalPage(ctx, pageId);
            }
            else
            {
                portalPage = null;
                ctx.getErrorCollection().addErrorMessage("dashboard.no.id.specified");
            }
        }
        return portalPage;
    }
}
