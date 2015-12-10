package com.atlassian.jira.plugin.searchrequestview;

import com.atlassian.jira.issue.search.SearchRequestInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface SearchRequestURLHandler
{
    /**
     * Parameter constants.
     *
     * @since v3.13
     */
    public static final class Parameter
    {
        public static final String NO_HEADERS = "noResponseHeaders";
        public static final String SEARCH_COUNT = "searchCount";
    }

    /**
     * @deprecated use {@link Parameter#NO_HEADERS} instead. Deprecated in v3.13
     */
    @Deprecated
    public static final String NO_HEADERS_PARAMETER = Parameter.NO_HEADERS;

    String getURLWithoutContextPath(final SearchRequestViewModuleDescriptor moduleDescriptor, final SearchRequestInfo searchRequestInfo);

    void handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws IOException;
}