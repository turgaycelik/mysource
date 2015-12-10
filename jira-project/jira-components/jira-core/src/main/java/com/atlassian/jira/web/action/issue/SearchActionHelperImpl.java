package com.atlassian.jira.web.action.issue;

import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.web.session.SessionPagerFilterManager;
import com.atlassian.jira.web.session.SessionSearchObjectManagerFactory;
import org.apache.log4j.Logger;

/**
 * Some utility code shared between the searching actions. 
 *
 * @since v4.0
 */
public final class SearchActionHelperImpl implements SearchActionHelper
{
    private static final Logger log = Logger.getLogger(SearchActionHelperImpl.class);

    private static final int DEFAULT_NUMBER_OF_ISSUES_PER_PAGE = 20;

    private final JiraAuthenticationContext authContext;
    private final SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory;
    private final UserPreferencesManager userPreferencesManager;

    public SearchActionHelperImpl(SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory, UserPreferencesManager userPreferencesManager, JiraAuthenticationContext authContext)
    {
        this.sessionSearchObjectManagerFactory = sessionSearchObjectManagerFactory;
        this.userPreferencesManager = userPreferencesManager;
        this.authContext = authContext;
    }

    @Override
    public PagerFilter getPagerFilter()
    {
        return getPagerFilter(null);
    }

    @Override
    public PagerFilter getPagerFilter(Integer tempMax)
    {
        PagerFilter pager = getSessionPagerFilterManager().getCurrentObject();

        if (pager == null)
        {
            pager = resetPager();
        }

        if (tempMax != null)
        {
            pager.setMax(tempMax);
        }

        return pager;
    }

    @Override
    public PagerFilter resetPager()
    {
        final PagerFilter pager = new PagerFilter();
        try
        {
            pager.setMax((int) getUserPreferences().getLong(PreferenceKeys.USER_ISSUES_PER_PAGE));
        }
        catch (final NumberFormatException nfe)
        {
            log.error("Unable to find '" + PreferenceKeys.USER_ISSUES_PER_PAGE + "' property setting. Defaulting to " + DEFAULT_NUMBER_OF_ISSUES_PER_PAGE);
            pager.setMax(DEFAULT_NUMBER_OF_ISSUES_PER_PAGE);
        }
        getSessionPagerFilterManager().setCurrentObject(pager);
        return pager;
    }

    @Override
    public void resetPagerTempMax()
    {
        resetPagerTempMax(null);
    }

    @Override
    public void resetPagerTempMax(Integer tempMax)
    {
        if (null != tempMax)
        {
            getPagerFilter(tempMax).setMax(tempMax);
        }
    }

    private Preferences getUserPreferences()
    {
        return userPreferencesManager.getPreferences(authContext.getLoggedInUser());
    }

    private SessionPagerFilterManager getSessionPagerFilterManager()
    {
        return sessionSearchObjectManagerFactory.createPagerFilterManager();
    }
}
