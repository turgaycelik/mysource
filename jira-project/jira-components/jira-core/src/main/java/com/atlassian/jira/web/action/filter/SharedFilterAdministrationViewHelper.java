package com.atlassian.jira.web.action.filter;

import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.search.SharedEntitySearchContext;
import com.atlassian.jira.sharing.type.ShareTypeFactory;

/**
 *
 * @since v4.4.1
 */
public class SharedFilterAdministrationViewHelper extends FilterViewHelper
{
    public SharedFilterAdministrationViewHelper(final ShareTypeFactory shareTypeFactory,
            final JiraAuthenticationContext authCtx, final String applicationContext, final String actionUrlPrefix,
            final SearchRequestService searchRequestService)
    {
        super(shareTypeFactory, authCtx, applicationContext, actionUrlPrefix, searchRequestService);
    }

    @Override
    public SharedEntitySearchContext getEntitySearchContext()
    {
        return SharedEntitySearchContext.ADMINISTER;
    }
}
