package com.atlassian.jira.issue.pager;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.web.session.SessionNextPreviousPagerManager;
import com.atlassian.jira.web.session.SessionSearchObjectManagerFactory;
import org.apache.log4j.Logger;

public class PagerManager
{
    private static final Logger log = Logger.getLogger(PagerManager.class);

    private final ApplicationProperties applicationProperties;
    private final SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory;

    public PagerManager(ApplicationProperties applicationProperties, SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory)
    {
        this.applicationProperties = applicationProperties;
        this.sessionSearchObjectManagerFactory = sessionSearchObjectManagerFactory;
    }

    /**
     * Update a pager if the search request passed is different from the searchrequest that is already there.
     *
     * @param pager         The current pager to update
     * @param searchRequest The current search request in the application
     * @param user          The current remote user
     * @param currentKey    The current issue key that we are viewing
     *
     * @deprecated As of JIRA 6.0, issue pager is no longer generated on the server side. This method will do nothing.
     */
    public void updatePager(NextPreviousPager pager, SearchRequest searchRequest, User user, String currentKey)
    {
        log.warn("NextPreviousPager is no longer supported as of JIRA 6.0 because issue pager is no longer generated on the server side. This method will do nothing. Please stop using it.");
    }

    /**
     * Returns pager stored in the session or a new pager if pager not found in the session.
     *
     * @return pager from session if found, otherwise new pager
     *
     * @deprecated As of JIRA 6.0, issue pager is no longer generated on the server side.
     */
    public NextPreviousPager getPager()
    {
        final SessionNextPreviousPagerManager sessionSearchRequestManager = sessionSearchObjectManagerFactory.createNextPreviousPagerManager();
        NextPreviousPager currentNextPreviousPager = sessionSearchRequestManager.getCurrentObject();
        if (currentNextPreviousPager == null)
        {
            currentNextPreviousPager = new NextPreviousPager(applicationProperties);
            sessionSearchRequestManager.setCurrentObject(currentNextPreviousPager);
        }
        return currentNextPreviousPager;
    }

    /**
     * Removes the pager from the session.
     */
    public void clearPager()
    {
        final SessionNextPreviousPagerManager sessionSearchRequestManager = sessionSearchObjectManagerFactory.createNextPreviousPagerManager();
        sessionSearchRequestManager.setCurrentObject(null);
    }
}
