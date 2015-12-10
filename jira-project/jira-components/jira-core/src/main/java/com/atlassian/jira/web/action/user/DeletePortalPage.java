package com.atlassian.jira.web.action.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.favourites.FavouritesService;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import webwork.action.ActionContext;

/**
 * Action for deleting {@link com.atlassian.jira.portal.PortalPage} objects.
 * Previously this was done by the behemoth {@link com.atlassian.jira.web.action.user.ConfigurePortalPages} but was
 * extracted as we now have a confirmation page for deletions.
 *
 * @since v3.13
 */
public class DeletePortalPage extends JiraWebActionSupport
{
    private final PortalPageService portalPageService;
    private final FavouritesService favouritesService;


    private Long pageId = null;
    private Long otherFavouriteCount;
    private PortalPage portalPage;

    public DeletePortalPage(PortalPageService portalPageService, FavouritesService favouritesService)
    {
        this.portalPageService = portalPageService;
        this.favouritesService = favouritesService;
    }

    /**
     * Validates the passed in pageId and populates errors if needed.
     */
    public String doDefault() throws Exception
    {
        doValidation();
        return hasAnyErrors() ? ERROR : INPUT;
    }

    /**
     * Performs the delete and redirects back to the manage page
     */
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        final JiraServiceContext serviceContext = getJiraServiceContext();
        portalPageService.deletePortalPage(serviceContext, pageId);
        return returnComplete("secure/ConfigurePortalPages!default.jspa");
    }

    /**
     * Validates passed in pageId and populates action with errors if needed.
     */
    public void doValidation()
    {
        if (pageId == null)
        {
            addErrorMessage(getText("admin.errors.user.page.id.must.be.set"));
            return;
        }

        final JiraServiceContext serviceContext = getJiraServiceContext();
        portalPageService.validateForDelete(serviceContext, pageId);
    }

    /**
     * Can the current pageId be deleted? This method assumes either
     * doDefault or doValidation has been called previously.
     *
     * @return <code>true</code> if this action contains no errors (i.e. validation was successful)
     */
    public boolean canDelete()
    {
        return !hasAnyErrors();
    }

    /**
     * Get the number of people who have added the dashboard as a favourite.  Used to warn against deleting.
     *
     * @return the number of users (not including owner) that has favourited the dashboard.
     */
    public int getOtherFavouriteCount()
    {
        if (otherFavouriteCount == null)
        {
            final PortalPage page = getPortalPage();

            // We want to know how many times it has been favourited by OTHER people
            final boolean isFavourite = favouritesService.isFavourite(getLoggedInApplicationUser(), page);
            final int count = isFavourite ? page.getFavouriteCount().intValue() - 1 : page.getFavouriteCount().intValue();
            otherFavouriteCount = new Long(count);
        }
        return otherFavouriteCount.intValue();
    }

    private PortalPage getPortalPage()
    {
        if (portalPage == null)
        {
            portalPage = portalPageService.getPortalPage(getJiraServiceContext(), pageId);
        }

        return portalPage;
    }

    public String getPageName()
    {
        final PortalPage page = getPortalPage();
        return page != null ? page.getName() : null;
    }

    public Long getPageId()
    {
        return pageId;
    }

    public void setPageId(Long pageId)
    {
        this.pageId = pageId;
    }

    //JRADEV-2637 Make sure the Delete Dashboard gets redirected to /secure/Dashboard.jspa
    public String getTargetUrl() {
         return ActionContext.getRequest().getContextPath()+"/secure/Dashboard.jspa";
    }
}
