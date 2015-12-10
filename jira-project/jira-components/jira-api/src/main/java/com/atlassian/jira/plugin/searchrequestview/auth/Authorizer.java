package com.atlassian.jira.plugin.searchrequestview.auth;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestParams;

/**
 * Authorise that a SearchRequest may be permitted.
 */
public interface Authorizer
{
    public static final Authorizer ALWAYS = new Authorizer()
    {
        public Result isSearchRequestAuthorized(User user, SearchRequest searchRequest, SearchRequestParams params)
        {
            return Result.OK;
        }
    };

    /**
     * Can the specified user perform this SearchRequest?
     * 
     * @param user
     *            who is trying to perform the
     * @param searchRequest
     *            they are attempting
     * @param params
     *            the parameters for the search (such as tempMax etc)
     * @return Result that says yay or nay, <b>must not be null</b>
     */
    Result isSearchRequestAuthorized(User user, SearchRequest searchRequest, SearchRequestParams params);

    /**
     * ResultObject that encapsulates the result and the reason if something is not OK.
     */
    public static interface Result
    {
        /**
         * Convenient placeholder for ok result.
         */
        public static final Result OK = new Result()
        {
            public boolean isOK()
            {
                return true;
            }

            public String getReason()
            {
                return null;
            }
        };

        /**
         * @return boolean whether it is ok to proceed with this SearchRequest
         */
        boolean isOK();

        /**
         * @return If not ok, return a String message why not.
         *         <p>
         *         Note: this is deliberately not i18n as it ends up in a 403 http code which should be ASCII only to be
         *         specs compliant.
         */
        String getReason();
    }

    /**
     * Convenient impl for failures
     */
    static class Failure implements Result
    {
        private final String reason;

        Failure(String key)
        {
            this.reason = key;
        }

        public boolean isOK()
        {
            return false;
        }

        public String getReason()
        {
            return reason;
        }
    }
}
